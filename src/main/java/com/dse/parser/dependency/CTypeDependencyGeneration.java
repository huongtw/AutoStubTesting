package com.dse.parser.dependency;

import com.dse.environment.Environment;
import com.dse.logger.AkaLogger;
import com.dse.parser.dependency.finder.Level;
import com.dse.parser.dependency.finder.VariableSearchingSpace;
import com.dse.parser.object.*;
import com.dse.search.Search;
import com.dse.search.SearchCondition;
import com.dse.search.condition.*;
import com.dse.util.*;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CTypeDependencyGeneration extends AbstractDependencyGeneration{

    private static final AkaLogger logger = AkaLogger.get(CTypeDependencyGeneration.class);

    private static final int MAX_ITERATIONS = 20;

    // If we found a type dependency,
    // we will add this dependency to corresponding node to tree
    boolean addToTreeAutomatically = false;

    private INode correspondingNode;

    public void dependencyGeneration(INode root) {
        if (!(root instanceof IVariableNode))
            return;

        IVariableNode variableNode = (IVariableNode) root;
        IVariableNode resolvedVarNode = (IVariableNode) root;

        String realType = resolvedVarNode.getRealType();
        String name = resolvedVarNode.getName();
        String absolutePath = resolvedVarNode.getAbsolutePath();

        logger.debug("Find type dependency for " + absolutePath);
        logger.debug("Type \"" + realType + "\"");

        if (name.equals("next") && realType.endsWith("*[256]"))
            System.out.println();

        // fast check
        correspondingNode = searchExistTypeDependency(variableNode);
        if (correspondingNode != null) {
            logger.debug("Exist in type dependency database");
            resolvedVarNode.setTypeDependencyState(true);

        } else {
            // if the variable is not resolved
            if (!resolvedVarNode.isTypeDependencyState()) {
                resolvedVarNode.setTypeDependencyState(true);
    
                try {
                    correspondingNode = performPrimitiveSearch(name, realType);

                    List<Level> spaces = new ArrayList<>();

                    if (correspondingNode == null) {
                        logger.debug("Is not primitive type. Initialize variable searching space.");
                        spaces = new VariableSearchingSpace(resolvedVarNode.getParent()).generateExtendSpaces();
                        correspondingNode = performSimpleSearch(spaces, resolvedVarNode);
                    }

                    if (correspondingNode == null)
                        correspondingNode = performFunctionPointerType(resolvedVarNode);

                    if (correspondingNode == null)
                        correspondingNode = performComplexSearch(spaces, resolvedVarNode);

                    if (correspondingNode == null)
                        correspondingNode = performScopeSearch(spaces, resolvedVarNode);

                    if (correspondingNode == null)
                        correspondingNode = performSystemSearch(resolvedVarNode);

                    if (correspondingNode == null && TemplateUtils.isTemplate(realType))
                        correspondingNode = performTemplateSearch(spaces, resolvedVarNode);

                    /*
                     * add to the dependency list
                     */
                    if (correspondingNode != null) {
                        if (addToTreeAutomatically) {
                            if (!(correspondingNode instanceof PredefinedTypeNode)) {
                                TypeDependency d = new TypeDependency();
                                d.setStartArrow(resolvedVarNode);
                                d.setEndArrow(correspondingNode);

                                if (!correspondingNode.getDependencies().contains(d)
                                        && !resolvedVarNode.getDependencies().contains(d)) {
                                    resolvedVarNode.getDependencies().add(d);
                                    correspondingNode.getDependencies().add(d);
                                }
                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                // if the variable is resolved
                logger.debug(resolvedVarNode.getAbsolutePath() + " is analyzed type dependency before");
                correspondingNode = resolvedVarNode.getCorrespondingNode();
            }

            if (correspondingNode != null)
                saveTypeDependency(variableNode, correspondingNode);
        }

        try {
            if (correspondingNode == null) {
                logger.debug("Can not resolve type \"" + realType + "\"");

            } else if (correspondingNode instanceof PredefinedTypeNode)
                logger.debug("The definition of variable is primitive type: "
                        + ((PredefinedTypeNode) correspondingNode).getType());
            else
                logger.debug("The definition of variable is defined in " + correspondingNode.getAbsolutePath());

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private INode searchExistTypeDependency(IVariableNode variableNode) {
        INode correspondingNode = null;

        // save the type dependency for later uses
        INode parent = variableNode.getParent();

        if (parent != null && parent.getAbsolutePath() != null && !parent.getAbsolutePath().isEmpty()) {
            String key = parent.getAbsolutePath() + File.separator + variableNode.getRawType();
            correspondingNode = Environment.getInstance().getResolvedNodes().get(key);
        }

        return correspondingNode;
    }

    private void saveTypeDependency(IVariableNode variableNode, INode correspondingNode) {
        // save the type dependency for later uses
        if (variableNode.getName().equals("hash_func"))
            System.out.println();
        INode parent = variableNode.getParent();
        if (parent != null && parent.getAbsolutePath() != null && parent.getAbsolutePath().length() > 0) {
            String key = variableNode.getParent().getAbsolutePath() + File.separator + variableNode.getRawType();
            if (!Environment.getInstance().getResolvedNodes().containsKey(key))
                Environment.getInstance().getResolvedNodes().put(key, correspondingNode);
        }
    }
    private INode performSystemSearch(IVariableNode resolvedVarNode) {
        logger.debug("performSystemSearch");
        INode correspondingNode = null;
        String coreType = TemplateUtils.getTemplateSimpleName(resolvedVarNode.getRawType());

        INode systemRoot = Environment.getInstance().getSystemRoot();
        List<INode> matches = Search.searchNodes(systemRoot, new StructurevsTypedefCondition())
                .stream()
                .filter(n -> n.getName().equals(coreType) && !(n.getParent() instanceof StructureNode))
                .collect(Collectors.toList());

        if (!matches.isEmpty()) {
            INode first = matches.get(0);

            if (first instanceof ClassNode && ((ClassNode) first).isTemplate()) {
                correspondingNode = first.getChildren().get(0);
            } else {
                correspondingNode = first;
            }
        }

        return correspondingNode;
    }


    private INode performPrimitiveSearch(String name, String rawType) {
        logger.debug("Perform primitive search with type \"" + rawType +"\"");
        AvailableTypeNode correspondingNode = null;
        rawType = rawType.replaceAll(IRegex.ARRAY_INDEX, IRegex.POINTER);
        String type = VariableTypeUtils.removeRedundantKeyword(rawType);

        if (VariableTypeUtils.isBasic(type) || VariableTypeUtils.isVoid(type)
                || VariableTypeUtils.isBoolMultiDimension(type)
                || VariableTypeUtils.isBoolMultiLevel(type)
                || VariableTypeUtils.isStrMultiDimension(type)
                || VariableTypeUtils.isStrMultiLevel(type)
                || VariableTypeUtils.isNumMultiDimension(type)
                || VariableTypeUtils.isNumMultiLevel(type)
                || VariableTypeUtils.isChMultiDimension(type)
                || VariableTypeUtils.isChMultiLevel(type)
                || VariableTypeUtils.isVoidPointer(type)) {
            logger.debug("Is primitive type");
            /*
             * If the type of variable is basic (int, char), one dimension
             * basic, two dimension basic, one level basic, two level basic
             */
            correspondingNode = new AvailableTypeNode();
            correspondingNode.setName(name);
            correspondingNode.setType(rawType);
            return correspondingNode;

        } else if (VariableTypeUtilsForStd.isSTL(type)) {
            logger.debug("Is stl type");
            correspondingNode = new STLTypeNode();
            correspondingNode.setName(name);
            correspondingNode.setType(rawType);
            return correspondingNode;

        }
        return correspondingNode;
    }
    /**
     * In this search strategy, we search the corresponding node based on its
     * path
     *
     * @param spaces          The variable searching space
     * @param resolvedVarNode   resolvedVarNode
     * @return performSimpleSearch node
     */
    private INode performSimpleSearch(List<Level> spaces, IVariableNode resolvedVarNode) {
        logger.debug("Perform simple search");
        INode correspondingNode = null;

        String type = resolvedVarNode.getRealType();
        type = type.replaceAll(IRegex.ARRAY_INDEX, IRegex.POINTER);
        type = VariableTypeUtils.removeRedundantKeyword(type);

        if (VariableTypeUtils.isStructureMultiLevel(type)
                || VariableTypeUtils.isStructureMultiDimension(type)
                || VariableTypeUtils.isStructureSimple(type)) {
            logger.debug("Is structure type");
            /**
             * Get the base type of structure object
             *
             * Ex: Student** ----> Student
             *
             * Ex: Student[3] ---> Student
             */
            String reducedType = type.trim().replaceAll("(\\*)+$", "");
            if (reducedType.indexOf("[") > 0)
                reducedType = reducedType.substring(0, reducedType.indexOf("["));
            String searchedPath = reducedType.replace(SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS, File.separator);

            // get all nodes in searching space
            List<INode> allNodesInSpace = getCandidatesNodesInSpaces(spaces);
            logger.debug("Num of candidate nodes = " + allNodesInSpace.size());

            // Find structure nodes
            List<INode> correspondingNodes = searchStructureNodeInSpace(allNodesInSpace, searchedPath);

            List<INode> nonEmptyCorrespondingNodes = correspondingNodes.stream()
                    .filter(s -> !(s instanceof IEmptyStructureNode))
                    .collect(Collectors.toList());
            if (nonEmptyCorrespondingNodes.size() == 1)
                return nonEmptyCorrespondingNodes.get(0);

            if (correspondingNodes.size() == 1)
                return correspondingNodes.get(0);
        }

        return correspondingNode;
    }

    private List<INode> getCandidatesNodesInSpaces(List<Level> spaces) {
        List<INode> allNodesInSearchingSpace = new ArrayList<>();

        List<SearchCondition> conditions = new ArrayList<>();
        conditions.add(new StructNodeCondition());
        conditions.add(new ClassNodeCondition());
        conditions.add(new UnionNodeCondition());
        conditions.add(new EnumNodeCondition());
        conditions.add(new TypedefNodeCondition());
        conditions.add(new AliasNodeCondition());

        for (Level l : spaces) {
            for (INode n : l) {
                if (n != null) {
                    List<INode> nodes = Search.searchNodes(n, conditions);
                    for (INode node : nodes)
                        if (!allNodesInSearchingSpace.contains(node))
                            allNodesInSearchingSpace.add(node);
                }
            }
        }
        return allNodesInSearchingSpace;
    }

    /**
     * Find struct/class/enum/union nodes ending with search path
     */
    private List<INode> searchStructureNodeInSpace(List<INode> allNodesInSearchingSpace, String searchedPath) {
        logger.debug("searchStructureNodeInSpace with type " + searchedPath);
        List<INode> correspondingNodes = new ArrayList<>();

        if (!searchedPath.startsWith(File.separator))
            searchedPath = File.separator + searchedPath;

        for (INode node : allNodesInSearchingSpace) {
            if (node instanceof StructNode || node instanceof ClassNode || node instanceof EnumNode ||
                    node instanceof UnionNode) {
                if (node.getAbsolutePath().endsWith(searchedPath))
                    if (!correspondingNodes.contains(node)) {
                        correspondingNodes.add(node);
                    }
            }
        }
        return correspondingNodes;
    }
    /**
     * We perform the complex search
     *
     * @param spaces searching space
     * @param node start
     * @return searched
     * @throws Exception may thrown
     */
    private INode performComplexSearch(List<Level> spaces, IVariableNode node) throws Exception {
        logger.debug("performComplexSearch");
        INode outputNode = null;

		/*
		 * Get the prefix of variable to start searching progress.
		 *
		 * Ex1: "X"-------------"X"
		 *
		 * Ex2: "::X"-------------"X"
		 *
		 * Ex2: "X::Y"-------------"X"
		 */
        String firstPrefix;
        String type = node.getCoreType();
        type = VariableTypeUtils.removeRedundantKeyword(type);

        if (type.startsWith(SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS))
            firstPrefix = type.split(SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS)[1];
        else if (type.contains(SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS))
            firstPrefix = type.split(SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS)[0];
        else
            firstPrefix = type;

        List<INode> candidateNodes = Search.searchInSpace(spaces, new ClassvsStructvsNamespaceCondition(),
                File.separator + firstPrefix);

        if (candidateNodes.size() == 1) {
            INode currentNode = candidateNodes.get(0);

            if (currentNode instanceof StructOrClassNode) {
                ArrayList<ArrayList<INode>> extendPaths = ((StructOrClassNode) currentNode).getExtendPaths();
                for (ArrayList<INode> path : extendPaths) {
                    String pathInString = getExtendPathInString(path);

                    if (type.startsWith(pathInString)) {
                        INode tmpNode = path.get(path.size() - 1);
                        String searchPath = type.replace(pathInString, "")
                                .replace(SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS, File.separator);
                        List<INode> possibles = Search.searchNodes(tmpNode, new NodeCondition(), searchPath);

                        if (!possibles.isEmpty())
                            return possibles.get(0);
                    }
                }
            } else if (currentNode instanceof NamespaceNode) {
                ArrayList<ArrayList<INode>> extendPaths = ((NamespaceNode) currentNode).getExtendPaths();

                for (ArrayList<INode> path : extendPaths) {
                    String pathInString = getExtendPathInString(path);

                    if (type.startsWith(pathInString)) {
                        INode tmpNode = path.get(path.size() - 1);
                        String searchPath = type.replace(pathInString, "").replace(SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS, File.separator);

                        List<INode> possibles = Search.searchNodes(tmpNode, new NodeCondition(), searchPath);

                        if (!possibles.isEmpty())
                            return possibles.get(0);
                    }
                }
            }
        } else if (candidateNodes.size() > 1) {
			/*
			 * We apply many filter to get the best matching node
			 */

            // FILTER 1. If the type of the input node start with "::".In this
            // case, the scope of the given node belong to its
            // parent as source code file.
            //
            // This occur in function
            // (SYMBOLIC_EXECUTION_TEST,nsTest4\func5(::XXX))
            List<INode> tmp = new ArrayList<>();

            if (type.startsWith(SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS)) {
                for (INode candidateNode : candidateNodes)
                    if (candidateNode.getParent() instanceof ISourcecodeFileNode)
                        tmp.add(candidateNode);

                // If only one matching node
                if (tmp.size() == 1)
                    outputNode = tmp.get(0);
                else if (tmp.size() == 0)
                    throw new Exception("Dont found the matching node with " + type);
                else {
                    logger.debug(tmp.toString());
                    throw new Exception("Detect may corresponding node of " + type);
                }

            } else {
                // FILTER 2. Get the output node based on the top-level parent
                // as namespace, structure (class, struct, etc.)
                INode topParentNodeofGivenNode = Utils.getTopLevelClassvsStructvsNamesapceNodeParent(node);
                if (topParentNodeofGivenNode != null)
                    for (INode candidateNode : candidateNodes) {
                        INode topParentNodeofCandidateNode = Utils
                                .getTopLevelClassvsStructvsNamesapceNodeParent(candidateNode);
                        if (topParentNodeofCandidateNode != null && topParentNodeofCandidateNode.getNewType()
                                .equals(topParentNodeofGivenNode.getNewType()))
                            tmp.add(candidateNode);
                    }
                else {
					/*
					 * FILTER 3. Get the nearest parent
					 *
					 * This occur in function (TSDV_R1,
					 * Level0MultipleNsTest(X,ns1::X,ns1::ns2::X))
					 */
                    INode givenNodeParent = node.getParent();
                    if (givenNodeParent instanceof IFunctionNode)
                        givenNodeParent = givenNodeParent.getParent();

                    for (INode candidateNode : candidateNodes) {
                        INode candidateParent = candidateNode.getParent();
                        if (candidateParent != null
                                && candidateParent.getNewType().equals(givenNodeParent.getNewType()))
                            tmp.add(candidateNode);
                    }
                }
				/*
				 * FILTER 4: Closest level
				 *
				 */
                if (tmp.size() > 1)
                    for (int i = tmp.size() - 1; i >= 0; i--) {
                        INode tmpItem = tmp.get(i);

						/*
						 * Return the class/struct/namespace/.cpp/.h/...
						 * containing the function (the input variable not is
						 * the funcdetail of this function)
						 */
                        INode givenNodeParent = node.getParent();
                        if (givenNodeParent instanceof IFunctionNode)
                            givenNodeParent = givenNodeParent.getParent();

                        if (!tmpItem.getParent().getAbsolutePath().equals(givenNodeParent.getAbsolutePath()))
                            tmp.remove(tmpItem);
                    }

                // If only one matching node
                if (tmp.size() == 1)
                    outputNode = tmp.get(0);
                else if (tmp.size() == 0)
                    throw new Exception("Dont found the matching node with " + type);
                else {
                    logger.debug(tmp.toString());
                    throw new Exception("Detect many corresponding node of " + type);
                }
            }
            if (outputNode == null) {
                logger.debug(candidateNodes.toString());
                throw new Exception("Detect may corresponding node of " + type);
            }
        }
        return outputNode;
    }

    private INode performTemplateSearch(List<Level> spaces, IVariableNode variableNode) {
        logger.debug("performTemplateSearch");
        String templateClassName = TemplateUtils.getTemplateSimpleName(variableNode.getRawType());

        for (Level l : spaces) {
            for (INode n : l) {
                List<INode> possibles = Search.searchNodes(n, new ClassNodeCondition());
                for (INode classNode : possibles) {
                    // Case Template Class
                    if (classNode instanceof ClassNode && ((ClassNode) classNode).isTemplate()) {
                        String simpleClassName = classNode.getName();

                        if (simpleClassName.contains(SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS)) {
                            int lastPos = simpleClassName.lastIndexOf(SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS) + 2;
                            simpleClassName = simpleClassName.substring(lastPos);
                        }

                        if (simpleClassName.equals(templateClassName)) {
                            if (classNode.getParent() instanceof ClassNode) {
                                IASTNode parentAST = ((ClassNode) classNode.getParent()).getAST();
                                IASTNode childAST = ((ClassNode) classNode).getAST();

                                if (parentAST == childAST)
                                    return classNode.getParent();
                            }

                            return classNode;
                        }
                    }
                }
            }
        }

        return null;
    }

    private String getExtendPathInString(ArrayList<INode> path) {
        StringBuilder s = new StringBuilder();
        for (INode child : path)
            s.append(child.getNewType()).append(SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS);
        return s.toString();
    }

    /**
     * Structure declare in scope but define in another location
     */
    private INode performScopeSearch(List<Level> spaces, IVariableNode resolvedVarNode) {
        logger.debug("performScopeSearch");
        String type = resolvedVarNode.getCoreType();
        type = VariableTypeUtils.removeRedundantKeyword(type);

        int iteration = 0;
//        do {
            iteration++;
            String searchPath = File.separator + type;

            List<INode> possibles = Search.searchInSpace(spaces, new StructurevsTypedefCondition(), searchPath);

            if (!possibles.isEmpty()) {
                correspondingNode = possibles.get(0);
                if (correspondingNode instanceof ITypedefDeclaration) {
                    VariableNode temp = new VariableNode();
                    temp.setCoreType(((ITypedefDeclaration) correspondingNode).getOldType());
                    correspondingNode = performScopeSearch(spaces, temp);
                }
            }

//            if (type.contains(SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS))
//                type = type.substring(0, type.indexOf(SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS) + 2);
//        } while (type.contains(SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS) && iteration < MAX_ITERATIONS);

        return correspondingNode;
    }

    private INode performFunctionPointerType(IVariableNode resolvedVarNode) {
        try {
            String type = resolvedVarNode.getRealType();

            IASTNode ast = Utils.convertToIAST(type);

            if (ast instanceof IASTDeclarationStatement) {
                ast = ((IASTDeclarationStatement) ast).getDeclaration();

                if (ast instanceof IASTSimpleDeclaration) {
                    IASTSimpleDeclaration declaration = (IASTSimpleDeclaration) ast;
                    IASTDeclarator declarator = declaration.getDeclarators()[0];
                    if (declarator instanceof IASTFunctionDeclarator) {
                        FunctionPointerTypeNode typeNode = new FunctionPointerTypeNode();
                        typeNode.setName(type);
                        typeNode.setAST(declaration);
                        typeNode.setParent(resolvedVarNode);

                        String returnType = declaration.getDeclSpecifier().getRawSignature();
                        typeNode.setReturnType(returnType);

                        String functionName = declarator.getNestedDeclarator().getName().getRawSignature();
                        typeNode.setFunctionName(functionName);

                        ICPPASTParameterDeclaration[] params = ((ICPPASTFunctionDeclarator) declarator).getParameters();
                        String[] paramTypes = new String[params.length];
                        for (int i = 0; i < params.length; i++) {
                            paramTypes[i] = params[i].getDeclSpecifier().getRawSignature();
                        }
                        typeNode.setArgumentTypes(paramTypes);

                        return typeNode;
                    }
                }
            }

            return null;
        } catch (IndexOutOfBoundsException ex) {
            return null;
        }
    }

    public INode getCorrespondingNode() {
        return correspondingNode;
    }

    public void setAddToTreeAutomatically(boolean addToTreeAutomatically) {
        this.addToTreeAutomatically = addToTreeAutomatically;
    }

    public boolean isAddToTreeAutomatically() {
        return addToTreeAutomatically;
    }
}
