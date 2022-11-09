package com.dse.parser.object;

import auto_testcase_generation.utils.ASTUtils;
import com.dse.parser.dependency.*;
import com.dse.util.*;
import com.google.gson.annotations.Expose;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTArrayDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTParameterDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTPointer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclaration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Represent attribute of class/struct/union/enum, arguments of a function,
 * global variables
 *
 * @author DucAnh
 */
public class VariableNode extends CustomASTNode<IASTNode> implements IVariableNode {

    /**
     * true if the variable node is analyzed type dependency generation before
     */
    protected boolean typeDependencyState = false;

    /**
     * The corresponding node of the given type
     */
    protected INode correspondingNode;

    /**
     * For example: "const int* a" ---> raw type  = "const int*"
     * Raw type
     */
    @Expose
    protected String rawType = "";

    /**
     * Remove storage class, &, *, [] from raw type
     *
     *Two cases:
     * + Case 1: Array, pointer, list, vector, stack, v.v. ---> get raw type of element
     * Ex: "const std::vector<int>" ---> coretype = "int"
     * + Case 2: primitive type ---> primitive type
     *
     * For example: "const int* a" ---> raw type  = "int"
     */
    protected String coreType = "";

    /**
     * Remove storage class including static, register, extern, mutable, const from
     * raw type.
     *
     * The attribute of the raw type is still kept.
     *
     * For example: "int* a" ---> raw type  = "int*"
     */
    protected String reducedRawType = "";

    /**
     * <pre> typedef void* XXX</pre>
     * <p>
     * void test(const XXX a){...}
     * <p>
     * rawtype = "const XXX", but realRawtype = "const void*"
     */
    protected String realRawType = "";

    protected int levelOfPointer = 0;

    protected boolean isReference = false;

    protected boolean isPrivate = false;

    protected String defaultValue;

    List<INode> visitedNodes = new ArrayList<>();

    @Override
    public List<String[]> getAllAttributes(INode n, int level) {
        List<String[]> attributes = new ArrayList<>();
        if (!(visitedNodes.contains(n))) {
            visitedNodes.add(n);

            if (n instanceof VariableNode) {
                INode resolvedNode = ((IVariableNode) n).resolveCoreType();
                if (resolvedNode instanceof StructureNode) {
                    StructureNode tmp = (StructureNode) resolvedNode;
                    attributes.add(
                            new String[] { getPrefixSpace(level) + n.getNewType(), ((IVariableNode) n).getRawType() });
                    attributes.addAll(getAllAttributes(tmp, ++level));
                } else {
                    attributes.add(
                            new String[] { getPrefixSpace(level) + n.getNewType(), ((IVariableNode) n).getRawType() });
                }
                level--;

            } else if (n instanceof StructureNode)
                for (IVariableNode item : ((StructureNode) n).getAttributes()) {
                    attributes.addAll(getAllAttributes(item, ++level));
                    level--;
                }
        }
        return attributes;
    }

    @Override
    public IASTDeclarator getASTDecName() {
        IASTNode ast = getAST();

        if (ast instanceof IASTSimpleDeclaration)
            return ((IASTSimpleDeclaration) ast).getDeclarators()[0];

        if (ast instanceof IASTParameterDeclaration)
            return ((IASTParameterDeclaration) ast).getDeclarator();

        return null;
    }

    @Override
    public IASTDeclSpecifier getASTType() {
        IASTNode ast = getAST();

        if (ast instanceof IASTFunctionStyleMacroParameter) {
            ast = Utils.convertToIAST("__MACRO_UNDEFINE_TYPE__ test");

            if (ast instanceof IASTDeclarationStatement)
                ast = ((IASTDeclarationStatement) ast).getDeclaration();
        }

        if (ast instanceof IASTSimpleDeclaration)
            return ((IASTSimpleDeclaration) ast).getDeclSpecifier();

        if (ast instanceof IASTParameterDeclaration)
            return ((IASTParameterDeclaration) ast).getDeclSpecifier();



        return null;
    }

    @Override
    public String getCoreType() {
        return coreType;
    }

    @Override
    public void setCoreType(String coreType) {
        this.coreType = coreType;
    }

    @Override
    public FunctionNode getGetterNode() {
        for (Dependency d : getDependencies())
            if (d instanceof GetterDependency)
                return (FunctionNode) d.getEndArrow();
        return null;
    }

    @Override
    public int getLevelOfPointer() {
        return levelOfPointer;
    }

    @Override
    public void setLevelOfPointer(int levelOfPointer) {
        this.levelOfPointer = levelOfPointer;
    }

    private String getPrefixSpace(int level) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < level; i++)
            output.append("   ");
        return output.toString();
    }

    @Override
    public String getFullType() {
        StringBuilder prefixPath = new StringBuilder();

        INode currentVar = resolveCoreType();



        String realType = getCoreType();
        if (currentVar instanceof AvailableTypeNode)
            realType = ((AvailableTypeNode) currentVar).getType();
        else if (currentVar instanceof IVariableNode)
            realType = ((IVariableNode) currentVar).getCoreType();
        else if (TemplateUtils.isTemplateClass(getRawType())) {
            realType = getRawType();
        }

        if (currentVar != null && !currentVar.getDependencies().isEmpty()) {
            INode finalCurrentVar = currentVar;
            Dependency aliasD = currentVar.getDependencies().stream()
                    .filter(d -> d instanceof AliasDependency
                            && d.getEndArrow().equals(finalCurrentVar)
                    ).findFirst()
                    .orElse(null);
            if (aliasD != null) {
                realType = getCoreType();
                currentVar = aliasD.getStartArrow();
            }
        }

        if (VariableTypeUtils.isBasic(realType) || VariableTypeUtils.isOneDimensionBasic(realType)
                || VariableTypeUtils.isTwoDimensionBasic(realType) || VariableTypeUtils.isOneLevelBasic(realType)
                || VariableTypeUtils.isTwoLevelBasic(realType) || VariableTypeUtilsForStd.isSTL(realType))
            prefixPath.append(getCoreType());
        else {
            INode originalVar = currentVar;

            if (currentVar instanceof AliasDeclaration) {
                prefixPath = new StringBuilder(currentVar.getNewType());
                currentVar = currentVar.getParent();
            }

            while ((currentVar instanceof StructureNode || currentVar instanceof NamespaceNode)) {
                if (prefixPath.length() > 0)
                    prefixPath.insert(0,
                            currentVar.getNewType() + SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS);
                else
                    prefixPath = new StringBuilder(currentVar.getNewType());
                currentVar = currentVar.getParent();
            }
            /*
             * Add :: in case the scope if file level
             */
            if (originalVar instanceof StructureNode)
                if (!prefixPath.toString().contains(SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS))
                    prefixPath.insert(0, SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS);

            /*
             * Add template argument <core type> in case template class
             */
            if (TemplateUtils.isTemplate(getRawType())) {
                int openPos = getRawType().indexOf(TemplateUtils.OPEN_TEMPLATE_ARG);
                int closePos = getRawType().lastIndexOf(TemplateUtils.CLOSE_TEMPLATE_ARG) + 1;
                String templateParameter = getRawType().substring(openPos, closePos);

                prefixPath.append(templateParameter);
            }
        }

        return prefixPath.toString();
    }

    @Override
    public String getRawType() {
        return rawType;
    }

    @Override
    public void setRawType(String rawType) {
        this.rawType = rawType;
    }

    /**
     * Get real raw type.
     *
     * For example:
     *
     * Raw type: "const XXX*", but "typedef Student XXX",
     * real raw type: "Student*"
     * @return
     */
    @Override
    public String getRealType() {
//		realRawType = rawType;
//
//		INode root = Environment.getInstance().getProjectNode();
//
//		List<INode> allTypedef = Search.searchNodes(root, new TypedefNodeCondition());
//		for (INode typedef : allTypedef) {
//			if (typedef instanceof TypedefDeclaration) {
//				realRawType = realRawType.replaceFirst(Utils.toRegex(typedef.getNewType()), ((TypedefDeclaration) typedef).getOldType());
//			}
//		}
//
//		List<INode> allAlias = Search.searchNodes(root, new AliasNodeCondition());
//		for (INode alias : allAlias) {
//			if (alias instanceof AliasDeclaration) {
//				realRawType = realRawType.replaceFirst(Utils.toRegex(alias.getNewType()), ((AliasDeclaration) alias).getOldType());
//			}
//		}

        realRawType = VariableTypeUtils.getRealType(this);

        return realRawType;
//
//		if (!typeDependencyState) {
//			TypeDependencyGeneration gen = new TypeDependencyGeneration();
//			gen.setAddToTreeAutomatically(false);
//			gen.dependencyGeneration(this);
//			correspondingNode = gen.getCorrespondingNode();
//		}
//
//		final String DEFAULT_REAL_TYPE = rawType;
//		if (correspondingNode != null) {
//			if (correspondingNode instanceof AvailableTypeNode) {
//				String realRawType = ((AvailableTypeNode) correspondingNode).getType();
//				return realRawType;
//
//			} else if (correspondingNode instanceof VariableNode) {
//				String realRawType = ((IVariableNode) correspondingNode).getReducedRawType();
//				return realRawType;
//
//			} else if (correspondingNode instanceof StructureNode) {
////				String originalDefinitionName = correspondingNode.getNewType();
//
//				// get real raw type
//				realRawType = DEFAULT_REAL_TYPE;
//                List<INode> allTypedef = Search.searchNodes(Environment.getInstance().getProjectNode(), new TypedefNodeCondifion());
//                for (INode typedef : allTypedef)
//                    if (typedef instanceof TypedefDeclaration){
//                        realRawType = realRawType.replaceFirst(Utils.toRegex(typedef.getNewType()), ((TypedefDeclaration) typedef).getOldType());
//                    }
//				return realRawType;
//			} else
////			if (realCoreType.length() > 0 && coreType.length() > 0) {
////				realRawType = DEFAULT_REAL_TYPE.replaceFirst(Utils.toRegex(coreType), realCoreType);
////				return realRawType;
////			} else
//				return DEFAULT_REAL_TYPE;
//		} else
//			return DEFAULT_REAL_TYPE;

    }

    @Override
    public String getReducedRawType() {
        return reducedRawType;
    }

    @Override
    public void setReducedRawType(String reducedRawType) {
        this.reducedRawType = reducedRawType;
    }

    @Override
    public FunctionNode getSetterNode() {
        for (Dependency d : getDependencies())
            if (d instanceof SetterDependency)
                return (FunctionNode) d.getEndArrow();
        return null;
    }

    @Override
    public int getSizeOfArray() {
        // only for one dimension array
        try {
            return Utils.toInt(Utils.getIndexOfArray(getRawType()).get(0));
        } catch (Exception ex) {
            try {
                IASTDeclarator declarator = getASTDecName();
                String size = ((IASTArrayDeclarator) declarator)
                        .getArrayModifiers()[0]
                        .getConstantExpression()
                        .getRawSignature();
                return Integer.parseInt(size);
            } catch (Exception e) {
                return -1;
            }
        }
    }

    @Override
    public boolean isPrivate() {
        return isPrivate;
    }

    @Override
    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    @Override
    public boolean isReference() {
        return isReference;
    }

    @Override
    public void setReference(boolean isReference) {
        this.isReference = isReference;
    }

    /**
     * Get real type of variable.
     * <p>
     * Ex: "typedex XXX int; XXX a" -------------> type of a is "int", not "XXX"
     * <p>
     * Note:
     * <p>
     * <p>
     *
     * <pre>
     * typedef int int_t; // declares int_t to be an alias for the type int typedef
     * char char_t, *char_p, (*fp)(void); // declares char_t to be an alias for char
     * // char_p to be an alias for char* // fp to be an alias for char(*)(void)
     *
     * <pre>
     *
     * @return
     */
    @Override
    public INode resolveCoreType() {
        if (correspondingNode == null) {
            CTypeDependencyGeneration gen = new CTypeDependencyGeneration();
            gen.setAddToTreeAutomatically(false);
            gen.dependencyGeneration(this);
            correspondingNode = gen.getCorrespondingNode();
        }
        return correspondingNode;
    }

    public static void main(String[] args) {
        IASTDeclarationStatement ast = ASTUtils.generateDeclarationStatement("int", "n", "n");
        VariableNode var = new InternalVariableNode();
        var.setAST(ast.getDeclaration());
        INode correspondingNode = var.resolveCoreType();
    }

    @Override
    public void setAST(IASTNode aST) {
        this.AST = aST;

        /**
         * set name of variable
         */
        String name;
        List<IASTDeclarator> declarators = new ArrayList<>();
        if (getAST() instanceof IASTSimpleDeclaration) {
            IASTSimpleDeclaration astNode = (IASTSimpleDeclaration) getAST();
            name = astNode.getDeclarators()[0].getName().toString();

            for (IASTDeclarator declarator : ((IASTSimpleDeclaration) getAST()).getDeclarators())
                if (declarator != null)
                    declarators.add(declarator);

        } else if (getAST() instanceof IASTParameterDeclaration) {
            name = ((IASTParameterDeclaration) getAST()).getDeclarator().getName().toString();
            declarators.add(((IASTParameterDeclaration) getAST()).getDeclarator());
        } else
            name = getAST().getRawSignature();
        setName(name.trim());

        /**
         * get the level of pointer
         */
        if (declarators.size() != 1)
            return;
        IASTDeclarator firstDeclarator = declarators.get(0);
        int pointerLv = 0;
        for (IASTNode operator : firstDeclarator.getPointerOperators())
            if (operator.getRawSignature().equals("&"))
                setReference(true);
            else if (operator.getRawSignature().equals("*"))
                pointerLv++;
        setLevelOfPointer(pointerLv);

        /**
         * set raw type of variable
         */
        String nameRegex = "\\s*\\b" + name + "\\b\\s*";
        IASTDeclSpecifier astType = getASTType();
        String rawType = astType.getRawSignature();
        if (astType instanceof IASTNamedTypeSpecifier) {
            rawType = String.valueOf(((IASTNamedTypeSpecifier) astType).getName().toCharArray());
        } else if (astType instanceof IASTSimpleDeclSpecifier) {
            rawType = astType.toString();
        }
        rawType = rawType
                + getASTDecName().getRawSignature()
                    .replaceFirst("(?s)" + nameRegex + "(?!.*?" +  nameRegex + ")", "");
//        String rawType = getAST().getRawSignature()
//                // remove name of variable
//                .replaceFirst("(?s)" + nameRegex + "(?!.*?" +  nameRegex + ")", "")
                // "char *" --> "char*"
        rawType = rawType
                .replaceAll("\\s*\\*\\s*", "*")
                // "int [ 3]" -> "int[3]"
                .replaceAll("\\s*\\[\\s*", "[")
                .replaceAll("\\s*\\]\\s*", "]")
                // "std::vector<  int >" --> "std::vector<int>"
                .replaceAll("\\s*<\\s*", "<")
                .replaceAll("\\s*>\\s*", ">")
                // "std :: vector<int>" --> "std::vector<int>"
                .replaceAll("\\s*::\\s*", "::")
                // "int & " --> "int&"
                .replaceAll("\\s*\\&\\s*", "&")
                .replaceAll("\\s+", " ")
                .replace(";", "")
                .trim();

//        rawType = rawType.replaceFirst("(?s)" + nameRegex + "(?!.*?" +  nameRegex + ")", "");

        int assignPos = rawType.indexOf("=");
        if (assignPos > 0) {
            this.defaultValue = rawType.substring(assignPos + 1).trim();
            rawType = rawType.substring(0, assignPos).trim();
        }

        this.setRawType(rawType);

        /**
         * set core type
         */
        IASTNode ast = getAST();
        IASTDeclarator declarator = null;
        IASTDeclSpecifier declSpecifier = null;

        // Ex: "Set* xxx"
        if (ast instanceof CPPASTSimpleDeclaration) {
            // declarator = "*xxx"
            declarator = ((CPPASTSimpleDeclaration) ast).getDeclarators()[0];

            // declSpecifier = "Set"
            declSpecifier = ((CPPASTSimpleDeclaration) ast).getDeclSpecifier();
        } else if (ast instanceof CPPASTParameterDeclaration) {
            declarator = ((CPPASTParameterDeclaration) ast).getDeclarator();
            declSpecifier = ((CPPASTParameterDeclaration) ast).getDeclSpecifier();
        }
        if (declarator != null && declSpecifier != null)
            if (declarator.getChildren().length > 0) {
                if (declarator.getChildren()[0] instanceof CPPASTPointer) {
                    if (declSpecifier.getRawSignature().equals("void")) {
                        // rawtype = "void*"
                        setCoreType("void*");
                    } else {
                        // rawtype = "<structure>*"
                        setCoreType(VariableTypeUtils.removeRedundantKeyword(declSpecifier.getRawSignature()));
                    }
                } else if (declarator instanceof CPPASTArrayDeclarator){
                    // rawType = "int[3]", declarator = "a[3], declSpecifier = "int"
                    setCoreType(VariableTypeUtils.removeRedundantKeyword(declSpecifier.getRawSignature()));
                } else {
                    // rawtype = "<structure>"
                    setCoreType(VariableTypeUtils.removeRedundantKeyword(declSpecifier.getRawSignature()));
                }
            } else {
                String coreType = VariableTypeUtils.removeRedundantKeyword(rawType);
                coreType = VariableTypeUtils.deletePointerOperator(coreType);

                // Handle "std::vector<IndividualStore::hoaqua>" ----> "IndividualStore::hoaqua"
                if (getASTType() instanceof IASTNamedTypeSpecifier && TemplateUtils.isTemplateClass(rawType))
                    coreType = coreType.substring(coreType.indexOf(TemplateUtils.OPEN_TEMPLATE_ARG) + 1,
                            coreType.lastIndexOf(TemplateUtils.CLOSE_TEMPLATE_ARG));
                setCoreType(coreType.trim());
            }

        /**
         * set reduced raw type
         */
        String reducedRawType = VariableTypeUtils.deleteStorageClasses(rawType);
        setReducedRawType(reducedRawType);
    }

    @Override
    public String toString() {
        return getNewType();
    }

    @Override
    public boolean isExtern() {
        if (getAST() instanceof CPPASTSimpleDeclaration) {
            IASTDeclSpecifier declSpecifier = ((CPPASTSimpleDeclaration) getAST()).getDeclSpecifier();

            return declSpecifier.getStorageClass() == IASTDeclSpecifier.sc_extern;
        }
        return false;
    }

    @Override
    public IASTInitializer getInitializer() {
        if (getAST() instanceof CPPASTSimpleDeclaration) {
            CPPASTSimpleDeclaration ast = (CPPASTSimpleDeclaration) getAST();
            IASTDeclarator[] declarators = ast.getDeclarators();

            if (declarators.length > 0) {
                IASTDeclarator firstDeclarator = declarators[0];
                return firstDeclarator.getInitializer();
            }
        }
        return null;
    }

    @Override
    public String getAbsolutePath() {
        // Exist absolutePath before
        if (this.getParent() == null || this.getParent().getAbsolutePath() == null || this.getParent().getAbsolutePath().isEmpty())
            return absolutePath;
        else { // Generate first time
            String absolutePath = this.getParent().getAbsolutePath() + File.separator + this.getName();
            setAbsolutePath(absolutePath);
        }

        return absolutePath;
    }

    @Override
    public IVariableNode clone() {
        IVariableNode clone = new CloneVariableNode();
        clone.setAbsolutePath(getAbsolutePath());
        clone.setChildren(getChildren());
        clone.setDependencies(getDependencies());
        clone.setId(getId());
        clone.setName(getName());
        clone.setRawType(getRawType());
        clone.setParent(getParent());
        clone.setCorrespondingNode(this.getCorrespondingNode());

        if (getAST() != null)
            clone.setAST(getAST());
        else {
            clone.setRawType(getRawType());
            clone.setCoreType(getCoreType());
            clone.setReducedRawType(getReducedRawType());
        }

        return clone;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VariableNode) {
            VariableNode objCast = (VariableNode) obj;
            return (objCast.getRawType().equals(getRawType()) && objCast.getName().equals(getName()));
        } else
            return false;
    }

    public boolean isTypeDependencyState() {
        return typeDependencyState;
    }

    public void setTypeDependencyState(boolean typeDependencyState) {
        this.typeDependencyState = typeDependencyState;
    }

    public INode getCorrespondingNode() {
        return correspondingNode;
    }

    public void setCorrespondingNode(INode correspondingNode) {
        this.correspondingNode = correspondingNode;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public boolean isConst() {
        return getASTType() != null && getASTType().isConst();
    }
}
