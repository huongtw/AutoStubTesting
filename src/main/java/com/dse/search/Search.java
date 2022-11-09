package com.dse.search;

import com.dse.environment.Environment;
import com.dse.parser.dependency.finder.Level;
import com.dse.parser.dependency.finder.VariableSearchingSpace;
import com.dse.parser.object.*;
import com.dse.search.condition.ClassNodeCondition;
import com.dse.search.condition.FunctionNodeCondition;
import com.dse.search.condition.SourcecodeFileNodeCondition;
import com.dse.search.condition.StructNodeCondition;
import com.dse.util.*;
import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Search implements ISearch {
    private static final int MAX_ITERATIONS = 20;

//    /**
//     * Tìm các con có tên xác định của một node
//     *
//     * @param parent
//     * @param name
//     * @return
//     */
//    public synchronized static INode searchFirstNodeByName(INode parent,
//                                                           String name) {
//        for (INode child : parent.getChildren()) {
//            String nameChild;
//            if (child instanceof IFunctionNode)
//                nameChild = ((FunctionNode) child).getSimpleName();
//            else
//                nameChild = child.getNewType();
//
//            if (nameChild.equals(name))
//                return child;
//        }
//        return null;
//    }

    /**
     * @param root       Root sub tree
     * @param conditions Danh sách điều kiện tìm kiếm
     * @return Danh sách node thỏa mãn điều kiện tìm kiếm
     */
    public synchronized static <T extends INode> List<T> searchNodes(INode root,
                                                                     List<SearchCondition> conditions) {
        List<T> output = new ArrayList<>();

        for (INode child : root.getChildren()) {
            boolean isSatisfiable = false;

            for (ISearchCondition con : conditions)
                if (con.isSatisfiable(child)) {
                    isSatisfiable = true;
                    break;
                }

            if (isSatisfiable) {
                try {
                    output.add((T) child);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            output.addAll(Search.searchNodes(child, conditions));
        }
        return output;
    }

    /**
     * @param root      Root sub tree
     * @param condition Điều kiện tìm kiếm
     * @return Danh sách node thỏa mãn điều kiện tìm kiếm
     */
    public synchronized static <T extends INode> List<T> searchNodes(INode root,
                                                                     ISearchCondition condition) {
        List<T> output = new ArrayList<>();
        try {
        for (INode child : root.getChildren()) {
            if (condition.isSatisfiable(child)) {
                try {
                    output.add((T) child);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            output.addAll(Search.searchNodes(child, condition));
        }
            return output;
        } catch (Exception e) {
            return output;
        }
    }

    /**
     * @param root      Root sub tree
     * @param condition Điều kiện tìm kiếm
     * @return Danh sách node thỏa mãn điều kiện tìm kiếm
     */
    public synchronized static <T extends INode> List<T> searchNodes(INode root,
                                                                     ISearchCondition condition, String relativePath) {
        List<T> output = Search.searchNodes(root, condition);
        relativePath = Utils.normalizePath(relativePath);
        if (Utils.isUnix() || Utils.isMac())
            if (!relativePath.startsWith(File.separator))
                relativePath = File.separator + relativePath;

        List<T> returnOuput = new ArrayList<>();
        for (INode node : output) {
            if (node.getAbsolutePath().endsWith(relativePath)) {
                try {
                    returnOuput.add((T) node);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return returnOuput;
    }

    public static <T extends INode> List<T> searchInSpace(List<Level> spaces, ISearchCondition c) {
        List<INode> children = new ArrayList<>();

        for (Level l : spaces) {
            for (INode n : l) {
                if (n != null) {
                    children.addAll(Search.searchNodes(n, c));
                }
            }
        }

        return children.stream()
                .distinct()
                .map(n -> (T) n)
                .collect(Collectors.toList());
    }


    public static List<INode> searchInSpace(List<Level> spaces, ISearchCondition c, String searchedPath) {
        List<INode> potentialCorrespondingNodes = new ArrayList<>();

        List<INode> children = new ArrayList<>();

        for (Level l : spaces) {
            for (INode n : l) {
                if (n != null) {
                    children.addAll(n.getChildren());
                }
            }
        }

        int iteration = 0;

        while (iteration <= MAX_ITERATIONS) {
            iteration++;

            List<INode> tempList = new ArrayList<>();

            for (INode child : children) {
                if (c.isSatisfiable(child)) {
                    if (child.getAbsolutePath().endsWith(searchedPath)) {
                        String[] targetItems, sourceItems;
                        if (Utils.isWindows()) {
                            // use "\\\\" for run Akautauto application on Windows
                            targetItems = searchedPath.split("\\\\");
                            sourceItems = child.getAbsolutePath().split("\\\\");
                        } else {
                            targetItems = searchedPath.split(File.separator);
                            sourceItems = child.getAbsolutePath().split(File.separator);
                        }
                        if (targetItems[targetItems.length - 1].equals(sourceItems[sourceItems.length - 1])) {
                            if (!potentialCorrespondingNodes.contains(child))
                                potentialCorrespondingNodes.add(child);
                        }
                    }
                }

                tempList.add(child);
            }

            /*
             * Case NamespaceTest.cpp/ns1/ns2/Level2MultipleNsTest(::X,::ns1::X,X)
             * ::X -> lowest level
             */
            if (searchedPath.startsWith(File.separator)
                    && searchedPath.indexOf(File.separator) == searchedPath.lastIndexOf(File.separator)) {
                potentialCorrespondingNodes.removeIf(node -> node.getParent() instanceof StructureNode
                        || node.getParent() instanceof NamespaceNode);
            }

            if (potentialCorrespondingNodes.size() > 0)
                break;
            else {
                children.clear();

                for (INode node : tempList)
                    if (node instanceof ISourcecodeFileNode
                            || node instanceof StructureNode
                            || node instanceof TypedefDeclaration
                            || node instanceof NamespaceNode)
                        children.addAll(node.getChildren());
            }
        }

        potentialCorrespondingNodes.removeIf(n -> {
            if (n instanceof ClassNode) {
                return ((ClassNode) n).isTemplate() && n.getParent() instanceof ClassNode
                        && ((ClassNode) n).getAST().equals(((ClassNode) n.getParent()).getAST());
            } else if (n instanceof ICommonFunctionNode) {
                return n.getParent() instanceof ICommonFunctionNode;
            }

            return false;
        });

        return potentialCorrespondingNodes;
    }

    public static String getScopeQualifier(INode node) {
        String qualifier = node.getName();

        INode parent = node.getParent();

        while (parent != null) {
            if (parent instanceof StructureNode || parent instanceof NamespaceNode) {
                // template class
                if (parent instanceof ClassNode && ((ClassNode) parent).isTemplate()
                        && qualifier.endsWith(TemplateUtils.deleteTemplateParameters(parent.getName()))) {
                    // do nothing
                } else
                    qualifier = parent.getName() + SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS + qualifier;
            }

            if (parent instanceof ISourcecodeFileNode)
                break;

            parent = parent.getParent();
        }

        if (node instanceof StructureNode && node.getParent() instanceof ISourcecodeFileNode) {
            if (!qualifier.startsWith(SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS)) {
                qualifier = SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS + qualifier;
            }
        }

        return qualifier;
    }

    public static List<INode> getDerivedNodesInSpace(StructOrClassNode structureNode, ICommonFunctionNode functionNode) {
        List<INode> derivedNodes = structureNode.getDerivedNodes();
        List<INode> nodesInSpace = new ArrayList<>();

        List<Level> space = new VariableSearchingSpace(functionNode).getSpaces();
        final List<SearchCondition> conditions = Arrays.asList(new ClassNodeCondition(), new StructNodeCondition());

        for (Level level : space) {
            for (INode node : level) {
                List<INode> structureNodes = Search.searchNodes(node, conditions);

                if (!structureNodes.isEmpty()) {
                    for (INode derivedNode : derivedNodes) {
                        if (structureNodes.contains(derivedNode)) {
                            if (!nodesInSpace.contains(derivedNode))
                                nodesInSpace.add(derivedNode);
                        }
                    }
                }
            }
        }

        return nodesInSpace;
    }

    public static List<INode> searchAllMatchFunctions(IFunctionPointerTypeNode tn) {
        INode root = Environment.getInstance().getProjectNode();
        List<INode> sourceCodes = searchNodes(root, new SourcecodeFileNodeCondition());
        sourceCodes.removeIf(n -> Environment.getInstance().getIgnores().contains(n));
        root = Environment.getInstance().getUserCodeRoot();
        List<INode> functionNodes = new ArrayList<>(searchNodes(root, new FunctionNodeCondition()));
        for (INode sourceNode : sourceCodes) {
            functionNodes.addAll(searchNodes(sourceNode, new FunctionNodeCondition()));
        }
        return functionNodes.stream()
                .filter(f -> FunctionPointerUtils.match(tn, (ICommonFunctionNode) f))
                .collect(Collectors.toList());
    }

    public static IVariableNode findAttributeByChain(StructureNode startNode, String[] chain) {
        String name = chain[0];
        IASTNode astName = Utils.convertToIAST(name);

        int dimension = 0;
        while (astName instanceof IASTArraySubscriptExpression) {
            IASTArraySubscriptExpression arrayExpr = (IASTArraySubscriptExpression) astName;
            astName = arrayExpr.getArrayExpression();
            name = astName.getRawSignature();
            dimension++;
        }

        IVariableNode variableNode = null;

        for (IVariableNode attribute : startNode.getAttributes()) {
            if (attribute.getName().equals(name)) {
                variableNode = attribute;
                break;
            }
        }

        if (variableNode != null) {
            int chainLength = chain.length;

            if (chainLength > 1) {
                INode typeNode = variableNode.resolveCoreType();

                if (typeNode instanceof StructureNode) {
                    chain = Arrays.copyOfRange(chain, 1, chainLength);
                    return findAttributeByChain((StructureNode) typeNode, chain);
                }
            }

            if (dimension > 0) {
                variableNode = variableNode.clone();
                String realType = variableNode.getRealType();
                while (dimension > 0) {
                    realType = realType.replaceFirst(IRegex.POINTER, SpecialCharacter.EMPTY);
                    dimension--;
                }
                variableNode.setRawType(realType);
                variableNode.setReducedRawType(realType);
            }
        }

        return variableNode;
    }
}
