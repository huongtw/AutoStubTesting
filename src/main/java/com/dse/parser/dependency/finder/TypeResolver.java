package com.dse.parser.dependency.finder;

import com.dse.parser.DeclaredVariableParser;
import com.dse.parser.object.*;
import com.dse.resolver.NewTypeResolver;
import com.dse.search.Search;
import com.dse.search.SearchCondition;
import com.dse.search.condition.AbstractFunctionNodeCondition;
import com.dse.search.condition.ClassvsStructvsNamespaceCondition;
import com.dse.search.condition.DefinitionFunctionNodeCondition;
import com.dse.util.IRegex;
import com.dse.util.SpecialCharacter;
import com.dse.util.VariableTypeUtils;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassType;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class TypeResolver {
    public static final String STRUCT_TYPE_START_FLAG = "{:";
    public static final String STRUCT_TYPE_END_FLAG = "}::";
    public static final String UNRESOLVED_TYPE_FLAG = "?";
    public static final String PROBLEM_TYPE_FLAG = ".ProblemType@";
    public static final String UNCREATED_SCOPE_FLAG = "A scope could not be created to represent the name";
    public static final String UNRESOLVED_CASTING_FLAG = "Attempt to use symbol failed: ";

    public boolean shoudCheck5652Flag = true;
    public boolean shouldCheckUnresolveType = true;

    private static final String VOID_PTR = "void*";
    private static final String NULL = "NULL";
    private static final String NULL_PTR = "nullptr";

    public boolean shouldTraceDeclaration = true;

    private INode context;

    public TypeResolver(INode context) {
        this.context = context;
    }

//    public String exec(IASTInitializerClause variable) {
//        return new NewTypeResolver(context, 0).solve(variable);

//        if (variable.getRawSignature().equals(NULL) || variable.getRawSignature().equals(NULL_PTR))
//            return VOID_PTR;
//
//        String variableType = variable.getExpressionType().toString();
//        /*
//         * TODO: template type?
//         */
//        if (variableType.contains(SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS))
//            variableType = variableType.substring(
//                    variableType.lastIndexOf(SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS)+2);
//
//        if (shoudCheck5652Flag) {
//            // Case: struct ... (C)
//            if (variableType.contains(STRUCT_TYPE_START_FLAG) && variableType.contains(STRUCT_TYPE_END_FLAG)) {
//                int start = 0;
//                int end = variableType.indexOf(STRUCT_TYPE_END_FLAG) + STRUCT_TYPE_END_FLAG.length();
//                String unresolved = variableType.substring(start, end);
//                variableType = variableType.replace(unresolved, "struct ");
//            }
//        }
//
//        if (shouldCheckUnresolveType) {
//            // Case: Typedef, Data type in another file, ...
//            // In several cases, CDT has trouble in resolving the real type of parameters.
//            // For example, ...
////            if (variableType.contains(UNRESOLVED_CASTING_FLAG)) {
////                IASTNode expr = variable;
////                if (expr instanceof IASTUnaryExpression) {
////                    expr = ((IASTUnaryExpression) variable).getOperand();
////                }
////                if (expr instanceof IASTFunctionCallExpression) {
////                    variableType = ((IASTFunctionCallExpression) expr).getFunctionNameExpression().getRawSignature();
////                }
////            }
//            if (variableType.contains(UNRESOLVED_TYPE_FLAG) || variableType.contains(PROBLEM_TYPE_FLAG)
//                    || variableType.contains(UNCREATED_SCOPE_FLAG) || variableType.contains(UNRESOLVED_CASTING_FLAG))
//                variableType = resolveProblemType(variable, variableType);
//        }
//
//        if (variableType.contains(UNRESOLVED_TYPE_FLAG) || variableType.contains(PROBLEM_TYPE_FLAG)
//                || variableType.contains(UNCREATED_SCOPE_FLAG) || variableType.contains(UNRESOLVED_CASTING_FLAG)
//                || variableType.contains(STRUCT_TYPE_START_FLAG) && variableType.contains(STRUCT_TYPE_END_FLAG))
//            return null;
//
//        String namespace = resolveNamespaceScope(variable);
//        variableType = namespace + variableType;
//
//        return variableType;
//    }

    private String resolveNamespaceScope(IASTExpression variable) {
        StringBuilder output = new StringBuilder();

        IType type = variable.getExpressionType();

        if (type instanceof IPointerType)
            type = ((IPointerType) type).getType();

        if (type instanceof CPPClassType) {
            IASTNode parent = ((CPPClassType) type).getDefinition();
            if (parent != null) {
                parent = parent.getParent();
            }

            boolean passCurrentStructureScope = false;

            while (parent != null) {
                // structure define in a namespace
                if (parent instanceof ICPPASTNamespaceDefinition || parent instanceof IASTSimpleDeclaration) {
                    String namespace = null;
                    if (parent instanceof ICPPASTNamespaceDefinition)
                        namespace = ((ICPPASTNamespaceDefinition) parent).getName().getRawSignature();
                    else {
                        IASTDeclSpecifier declSpecifier = ((IASTSimpleDeclaration) parent).getDeclSpecifier();
                        if (declSpecifier instanceof IASTCompositeTypeSpecifier) {
                            namespace = ((IASTCompositeTypeSpecifier) declSpecifier).getName().getRawSignature();
                            if (namespace.equals(((CPPClassType) type).getName())) {
                                if (!passCurrentStructureScope) {
                                    namespace = null;
                                    passCurrentStructureScope = true;
                                }
                            }
                        }
                    }
                    if (namespace != null)
                        output.insert(0, namespace + SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS);
                }
                parent = parent.getParent();
            }
        }

        return output.toString();
    }


    private String resolveProblemType(IASTExpression variable, String variableType) {
        IASTExpression operand = variable;

        if (operand instanceof IASTFieldReference) {
            String resolvedType = resolveProblemTypeStructureScope(variable);
            if (resolvedType != null)
                return resolvedType;
        }

        if (operand instanceof IASTBinaryExpression) {
            int operator = ((IASTBinaryExpression) variable).getOperator();
            if ((operator >= IASTBinaryExpression.op_lessThan && operator < IASTBinaryExpression.op_greaterEqual)
                    || operator == IASTBinaryExpression.op_logicalOr || operator == IASTBinaryExpression.op_logicalAnd
                    || operator == IASTBinaryExpression.op_equals || operator == IASTBinaryExpression.op_notequals)
                return VariableTypeUtils.BASIC.NUMBER.INTEGER.BOOL;
            else {
                //TODO: overloading operator
                variableType = VariableTypeUtils.BASIC.NUMBER.FLOAT.DOUBLE;

                IASTExpression operand1 = ((IASTBinaryExpression) operand).getOperand1();

                String resolvedType = new NewTypeResolver(context, 0).exec(operand1);

                if (resolvedType == null) {
                    IASTExpression operand2 = ((IASTBinaryExpression) operand).getOperand2();
                    resolvedType = new NewTypeResolver(context, 0).exec(operand2);
                }

                if (resolvedType != null)
                    variableType = resolvedType;

                return variableType;
            }
        }

        while (operand instanceof IASTUnaryExpression)
            operand = ((IASTUnaryExpression) operand).getOperand();

        if (operand != null) {
            if (operand instanceof IASTIdExpression && shouldTraceDeclaration) {
                String varName = operand.getRawSignature();
                String resolvedType = resolveProblemTypeLocalScope(varName);
                if (resolvedType == null) {
                    resolvedType = resolveProblemTypeGlobalScope(varName);
                }

                if (resolvedType != null) {
                    if (variableType.contains(PROBLEM_TYPE_FLAG) || variableType.contains(UNCREATED_SCOPE_FLAG))
                        return resolvedType;
                    else
                        return variableType.replace(UNRESOLVED_TYPE_FLAG, resolvedType);
                }

            } else if (operand instanceof IASTFunctionCallExpression) {
                IASTNode parent = operand;

                do {
                    parent = parent.getParent();
                } while (parent != null && !(parent instanceof IASTFunctionCallExpression || parent instanceof IASTSimpleDeclaration));

                if (parent instanceof IASTSimpleDeclaration) {
                    return ((IASTSimpleDeclaration) parent).getDeclSpecifier().getRawSignature();
                }
            }

            String resolvedType;

            resolvedType = resolveArrayAndPointer(variable);
            if (resolvedType != null)
                return resolvedType;

            resolvedType = resolveFunctionArgument(variable);
            if (resolvedType != null)
                return resolvedType;

            resolvedType = resolveParentExpression(variable);
            if (resolvedType != null)
                return resolvedType;
        }

        return variableType;
    }

    private String resolveArrayAndPointer(IASTExpression expression) {
        int level = 0;

        IASTNode parent = expression.getParent();
        IASTNode prev = expression;

        while (parent != null) {
            boolean isArrayIndex = parent instanceof IASTArraySubscriptExpression;
            boolean isStarUnary = parent instanceof IASTUnaryExpression
                    && ((IASTUnaryExpression) parent).getOperator() == IASTUnaryExpression.op_star;

            boolean isBracketUnary = parent instanceof IASTUnaryExpression
                    && ((IASTUnaryExpression) parent).getOperator() == IASTUnaryExpression.op_bracketedPrimary;

            if (isArrayIndex || isStarUnary || isBracketUnary) {
                if (isArrayIndex || isStarUnary) {
                    level++;
                    prev = parent;
                }

                parent = parent.getParent();
            } else {
                break;
            }

        }

        if (prev != expression) {
            String type = new NewTypeResolver(context, 0).exec((IASTExpression) prev);

            if (type != null) {
                for (int i = 0; i < level; i++)
                    type += VariableTypeUtils.POINTER_CHAR;
                return type;
            }
        }

        return null;
    }

    private String resolveParentExpression(IASTExpression expression) {
        IASTNode parent = expression.getParent();

//        while (parent != null) {
//            if (parent instanceof IASTBinaryExpression)
//                break;
//            else
//                parent = parent.getParent();
//        }

        if (parent instanceof IASTBinaryExpression) {
            IASTBinaryExpression binaryExpression = (IASTBinaryExpression) parent;

            IASTExpression variable;

            int operator = binaryExpression.getOperator();
            if (operator == IASTBinaryExpression.op_shiftLeft || operator == IASTBinaryExpression.op_shiftRight
                    || operator == IASTBinaryExpression.op_shiftLeftAssign
                    || operator == IASTBinaryExpression.op_shiftRightAssign)
                return VariableTypeUtils.BASIC.NUMBER.INTEGER.INT;

            if (expression == binaryExpression.getOperand2())
                variable = binaryExpression.getOperand1();
            else
                variable = binaryExpression.getOperand2();

            return new NewTypeResolver(context, 0).exec(variable);

        } else if (parent instanceof IASTEqualsInitializer) {

            while (!(parent instanceof IASTSimpleDeclaration) && parent != null) {
                parent = parent.getParent();
            }

            if (parent != null) {
                IASTSimpleDeclaration declaration = (IASTSimpleDeclaration) parent;
                IASTDeclSpecifier declSpecifier = declaration.getDeclSpecifier();

                return declSpecifier.getRawSignature();
            }
        }

        return null;
    }

    private String resolveProblemTypeGlobalScope(String varName) {
        if (context instanceof IFunctionNode) {
            IVariableNode varNode = (IVariableNode) new VariableFinder((IFunctionNode) context).find(varName);

            if (varNode != null)
                return varNode.getRealType().replace("*", "");
        }

        return null;
    }

    private String resolveProblemTypeLocalScope(String varName) {
        IASTNode ast = null;

        if (context instanceof CustomASTNode)
            ast = ((CustomASTNode<?>) context).getAST();
        else if (context instanceof ISourcecodeFileNode)
            ast = ((ISourcecodeFileNode) context).getAST();

        if (ast != null) {
            DeclaredVariableParser visitor = new DeclaredVariableParser();

            ast.accept(visitor);

            List<IASTNode> variables = visitor.getVariables();

            for (IASTNode variable : variables) {
                if (variable instanceof IASTSimpleDeclaration) {
                    IASTDeclarator[] declarators = ((IASTSimpleDeclaration) variable).getDeclarators();
                    for (IASTDeclarator declarator : declarators)
                        if (declarator.getName().getRawSignature().equals(varName))
                            return ((IASTSimpleDeclaration) variable).getDeclSpecifier().getRawSignature();
                } else if (variable instanceof IASTParameterDeclaration) {
                    IASTDeclarator declarator = ((IASTParameterDeclaration) variable).getDeclarator();
                    if (declarator.getName().getRawSignature().equals(varName))
                        return ((IASTParameterDeclaration) variable).getDeclSpecifier().getRawSignature();
                }
            }
        }

        return null;
    }

    private String resolveFunctionArgument(IASTExpression expression) {
        IASTNode parent = expression.getParent();

        if (parent instanceof IASTFunctionCallExpression) {
            String functionName = ((IASTFunctionCallExpression) parent).getFunctionNameExpression().getRawSignature();

            IASTInitializerClause[] arguments = ((IASTFunctionCallExpression) parent).getArguments();
            int argumentIndex = -1;

            for (int i = 0; i < arguments.length; i++) {
                if (arguments[i] == expression) {
                    argumentIndex = i;
                    break;
                }
            }

            if (argumentIndex >= 0) {

                VariableSearchingSpace searchingSpace = new VariableSearchingSpace(context);
                List<Level> space = searchingSpace.generateExtendSpaces();

                List<SearchCondition> conditions = Arrays.asList(new AbstractFunctionNodeCondition(), new DefinitionFunctionNodeCondition());

                String relativePath = functionName
                        .replace(SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS, File.separator);

                if (!relativePath.startsWith(File.separator))
                    relativePath = File.separator + relativePath;

                final String pattern = ".+\\Q" + relativePath + "\\E\\(.*\\)";

                for (Level level : space) {
                    for (INode node : level) {
                        List<INode> functions = Search.searchNodes(node, conditions);

                        ICommonFunctionNode functionNode = (ICommonFunctionNode) functions
                                .stream()
                                .filter(f -> f.getAbsolutePath().matches(pattern))
                                .findFirst()
                                .orElse(null);

                        if (functionNode != null)
                            return functionNode.getArguments().get(argumentIndex).getRawType();
                    }
                }
            }
        }

        return null;
    }

    private String resolveProblemTypeStructureScope(IASTExpression variable) {
        String variableType = null;

        if (variable instanceof IASTFieldReference) {
            IASTExpression owner = ((IASTFieldReference) variable).getFieldOwner();
            String attributeName = ((IASTFieldReference) variable).getFieldName().getRawSignature();

            while (owner instanceof IASTUnaryExpression)
                owner = ((IASTUnaryExpression) owner).getOperand();

            if (owner instanceof IASTIdExpression) {
                String ownerType = new NewTypeResolver(context, 0).exec(owner);

                if (ownerType != null) {
                    String ownerCoreType = VariableTypeUtils.deleteStorageClasses(ownerType);
                    ownerCoreType = VariableTypeUtils.deleteStructKeyword(ownerCoreType);
                    ownerCoreType = VariableTypeUtils.deleteUnionKeyword(ownerCoreType);
                    ownerCoreType = VariableTypeUtils.deleteVirtualAndInlineKeyword(ownerCoreType);
                    ownerCoreType = ownerCoreType.replaceAll(IRegex.ARRAY_INDEX, "")
                            .replaceAll(IRegex.POINTER, "").replace("&", "").trim();

                    String ownerPath = ownerCoreType.replace(SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS, File.separator);

                    INode structureNode = null;

                    List<Level> space = new VariableSearchingSpace(context).generateExtendSpaces();

                    for (Level level : space) {
                        for (Node node : level) {
                            List<INode> structureNodes = Search.searchNodes(node, new ClassvsStructvsNamespaceCondition(), ownerPath);
                            if (structureNodes.size() > 0) {
                                structureNode = structureNodes.get(0);
                            }
                        }
                    }

                    if (structureNode != null) {
                        for (INode child : structureNode.getChildren()) {
                            if (child.getName().equals(attributeName)) {
                                if (child instanceof AttributeOfStructureVariableNode) {
                                    variableType = ((AttributeOfStructureVariableNode) child).getFullType();
                                }
                            }
                        }
                    }
                }
            }
        }

        if (variableType != null && variableType.startsWith(SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS))
            variableType = variableType.substring(2);

        return variableType;
    }
}
