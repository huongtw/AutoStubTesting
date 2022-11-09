package com.dse.resolver;

import com.dse.environment.Environment;
import com.dse.parser.DeclaredVariableParser;
import com.dse.parser.dependency.finder.Level;
import com.dse.parser.dependency.finder.MethodFinder;
import com.dse.parser.dependency.finder.VariableSearchingSpace;
import com.dse.parser.object.*;
import com.dse.search.Search;
import com.dse.search.SearchCondition;
import com.dse.search.condition.ClassvsStructvsNamespaceCondition;
import com.dse.search.condition.ExternVariableNodeCondition;
import com.dse.search.condition.GlobalVariableNodeCondition;
import com.dse.search.condition.NamespaceNodeCondition;
import com.dse.util.IRegex;
import com.dse.util.SpecialCharacter;
import com.dse.util.VariableTypeUtils;
import org.eclipse.cdt.core.dom.ast.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NewTypeResolver {
    private static final int MAX_ITERATOR = 20;
    private static final String STRUCT_TYPE_START_FLAG = "{:";
    private static final String STRUCT_TYPE_END_FLAG = "}::";
    private static final String UNRESOLVED_TYPE_FLAG = "?";
    private static final String PROBLEM_TYPE_FLAG = ".ProblemType@";
    private static final String UNCREATED_SCOPE_FLAG = "A scope could not be created to represent the name";
    private static final String UNRESOLVED_CASTING_FLAG = "Attempt to use symbol failed: ";
    private static final String VOID_PTR = "void*";
    private static final String NULL = "NULL";
    private static final String NULL_PTR = "nullptr";
    private final INode context;
    public boolean shouldSolveByParent = true;
    private int iterator;

    public NewTypeResolver(INode context, int iterator) {
        this.context = context;
        this.iterator = iterator;
        this.iterator++;
    }

    public NewTypeResolver(INode context) {
        this.context = context;
        this.iterator = 0;
        this.iterator++;
    }

    public String exec(IASTInitializerClause clause) {
        return solve(clause);
    }

    public String solve(IASTInitializerClause clause) {
        if (iterator > MAX_ITERATOR)
            return null;

        String type = null;

        if (clause instanceof IASTExpression) {
            type = solveExpression((IASTExpression) clause);

        } else if (clause instanceof IASTInitializerList) {
            type = solveInitializerList((IASTInitializerList) clause);

        }

        if (type == null && shouldSolveByParent)
            type = solveByParent(clause);

        if (type != null && type.contains(" "))
            return null;

        return type;
    }

    private String solveInitializerList(IASTInitializerList initializerList) {
        IASTInitializerClause[] clauses = initializerList.getClauses();

        String type = null;

        for (IASTInitializerClause clause : clauses) {
            String itemType = new NewTypeResolver(context, iterator).solve(clause);

            if (itemType != null) {
                type = itemType;
                break;
            }
        }

        return type;
    }

    private String solveExpression(IASTExpression expr) {
        String type = expr.getExpressionType().toString();

        if (type.contains(SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS)) {
            int lastNameIdx = type.lastIndexOf(SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS)
                    + SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS.length();

            type = type.substring(lastNameIdx);
        }

        // Case: struct ... (C)
        if (type.contains(STRUCT_TYPE_START_FLAG) && type.contains(STRUCT_TYPE_END_FLAG)) {
            int start = 0;
            int end = type.indexOf(STRUCT_TYPE_END_FLAG) + STRUCT_TYPE_END_FLAG.length();
            String unresolved = type.substring(start, end);
            type = type.replace(unresolved, "struct ");
        }

        // Case: Typedef, Data type in another file, ...
        // In several cases, CDT has trouble in resolving the real type of parameters.
        // For example, ...
        if (type.contains(UNRESOLVED_TYPE_FLAG) || type.contains(PROBLEM_TYPE_FLAG)
                || type.contains(UNCREATED_SCOPE_FLAG) || type.contains(UNRESOLVED_CASTING_FLAG))
            type = resolveProblemType(expr);

        if (type != null
                && (type.contains(UNRESOLVED_TYPE_FLAG) || type.contains(PROBLEM_TYPE_FLAG)
                || type.contains(UNCREATED_SCOPE_FLAG) || type.contains(UNRESOLVED_CASTING_FLAG)
                || type.contains(STRUCT_TYPE_START_FLAG) && type.contains(STRUCT_TYPE_END_FLAG)))
            type = null;

        if (type == null) {
            String exprRaw = expr.getRawSignature();
            if (exprRaw.equals(NULL) || exprRaw.equals(NULL_PTR))
                type = VOID_PTR;
        }

        return type;
    }

    private String resolveProblemType(IASTExpression expr) {
        ExpressionType exprType = getExpressionType(expr);

        String type = null;

        switch (exprType) {
            case LITERAL:
                type = solveLiteralExpression((IASTLiteralExpression) expr);
                break;

            case ARRAY_INDEX:
                type = solveArrayIndexExpression((IASTArraySubscriptExpression) expr);
                break;

            case BINARY:
                type = solveBinaryExpression((IASTBinaryExpression) expr);
                break;

            case UNARY:
                type = solveUnaryExpression((IASTUnaryExpression) expr);
                break;

            case CAST:
                type = solveCastExpression((IASTCastExpression) expr);
                break;

            case CONDITION:
                type = solveConditionExpression((IASTConditionalExpression) expr);
                break;

            case FUNCTION_CALL:
                type = solveFunctionCallExpression((IASTFunctionCallExpression) expr);
                break;

            case ID:
                type = solveIdExpression((IASTIdExpression) expr);
                break;

            case FIELD_REF:
                type = solveFieldReference((IASTFieldReference) expr);
                break;
        }

        return type;
    }

    private String solveLiteralExpression(IASTLiteralExpression literalExpr) {
        int kind = literalExpr.getKind();

        String type;

        switch (kind) {
            case IASTLiteralExpression.lk_true:
            case IASTLiteralExpression.lk_false:
                type = VariableTypeUtils.BASIC.NUMBER.INTEGER.BOOL;
                break;

            case IASTLiteralExpression.lk_char_constant:
                type = VariableTypeUtils.BASIC.CHARACTER.CHAR;
                break;

            case IASTLiteralExpression.lk_integer_constant:
                type = VariableTypeUtils.BASIC.NUMBER.INTEGER.INT;
                break;

            case IASTLiteralExpression.lk_float_constant:
                type = VariableTypeUtils.BASIC.NUMBER.FLOAT.DOUBLE;
                break;

            case IASTLiteralExpression.lk_string_literal:
                type = "char*";
                break;

            case IASTLiteralExpression.lk_this:
                INode parent = context;

                while (parent != null && !(parent instanceof StructureNode))
                    parent = parent.getParent();

                if (parent != null)
                    type = Search.getScopeQualifier(parent);
                else
                    type = null;

                break;

            default:
                type = null;
        }

        return type;
    }

    private String solveCastExpression(IASTCastExpression castExpr) {
        IASTTypeId typeId = castExpr.getTypeId();

        return typeId.getDeclSpecifier().getRawSignature();
    }

    private String solveConditionExpression(IASTConditionalExpression conditionExpr) {
        IASTExpression negative = conditionExpr.getNegativeResultExpression();

        String type = new NewTypeResolver(context, iterator).solve(negative);

        if (type == null) {
            IASTExpression positive = conditionExpr.getPositiveResultExpression();

            type = new NewTypeResolver(context, iterator).solve(positive);
        }

        return type;
    }

    private String solveUnaryExpression(IASTUnaryExpression unaryExpr) {
        int operator = unaryExpr.getOperator();
        IASTExpression operand = unaryExpr.getOperand();

        String type;

        switch (operator) {
            case IASTUnaryExpression.op_not:
                type = VariableTypeUtils.BASIC.NUMBER.INTEGER.BOOL;
                break;

            case IASTUnaryExpression.op_sizeof:
                type = VariableTypeUtils.BASIC.NUMBER.INTEGER.SIZE__T;
                break;

            case IASTUnaryExpression.op_bracketedPrimary:
                type = new NewTypeResolver(context, iterator).solve(operand);
                break;

            case IASTUnaryExpression.op_amper:
                type = new NewTypeResolver(context, iterator).solve(operand);
                type += VariableTypeUtils.REFERENCE;
                break;

            case IASTUnaryExpression.op_star:
                type = new NewTypeResolver(context, iterator).solve(operand);
                type += VariableTypeUtils.POINTER_CHAR;
                break;

            case IASTUnaryExpression.op_minus:
            case IASTUnaryExpression.op_plus:
            case IASTUnaryExpression.op_prefixDecr:
            case IASTUnaryExpression.op_prefixIncr:
            case IASTUnaryExpression.op_postFixDecr:
            case IASTUnaryExpression.op_postFixIncr:
                // TODO: overloading operator
                type = VariableTypeUtils.BASIC.NUMBER.INTEGER.INT;
                break;

            default:
                // TODO: unsupported operator
                type = null;

        }

        return type;
    }

    private String solveByParent(IASTInitializerClause clause) {
        IASTNode parent = clause.getParent();

        String type = null;

        if (parent instanceof IASTEqualsInitializer) {
            while (parent != null && !(parent instanceof IASTSimpleDeclaration)) {
                parent = parent.getParent();
            }

            if (parent != null) {
                IASTSimpleDeclaration declaration = (IASTSimpleDeclaration) parent;
                IASTDeclSpecifier declSpecifier = declaration.getDeclSpecifier();

                type = declSpecifier.getRawSignature();
            }

        } else if (parent instanceof IASTBinaryExpression) {
            IASTBinaryExpression binaryExpr = (IASTBinaryExpression) parent;

            IASTExpression operand;

            if (clause == binaryExpr.getOperand1())
                operand = binaryExpr.getOperand2();
            else
                operand = binaryExpr.getOperand1();

            int operator = binaryExpr.getOperator();

            switch (operator) {
                case IASTBinaryExpression.op_assign:
                case IASTBinaryExpression.op_plusAssign:
                case IASTBinaryExpression.op_moduloAssign:
                case IASTBinaryExpression.op_divideAssign:
                case IASTBinaryExpression.op_multiplyAssign:
                case IASTBinaryExpression.op_minusAssign:
                case IASTBinaryExpression.op_binaryXorAssign:
                case IASTBinaryExpression.op_binaryOrAssign:
                case IASTBinaryExpression.op_binaryAndAssign:
                case IASTBinaryExpression.op_plus:
                case IASTBinaryExpression.op_modulo:
                case IASTBinaryExpression.op_divide:
                case IASTBinaryExpression.op_multiply:
                case IASTBinaryExpression.op_minus:
                case IASTBinaryExpression.op_binaryAnd:
                case IASTBinaryExpression.op_binaryOr:
                case IASTBinaryExpression.op_binaryXor:
                case IASTBinaryExpression.op_max:
                case IASTBinaryExpression.op_min:
                case IASTBinaryExpression.op_equals:
                case IASTBinaryExpression.op_notequals:
                case IASTBinaryExpression.op_greaterEqual:
                case IASTBinaryExpression.op_lessEqual:
                case IASTBinaryExpression.op_greaterThan:
                case IASTBinaryExpression.op_lessThan:
                case IASTBinaryExpression.op_logicalAnd:
                case IASTBinaryExpression.op_logicalOr:
                    type = new NewTypeResolver(context, iterator).solve(operand);
                    break;
            }

        } else if (parent instanceof IASTFunctionCallExpression) {
            IASTFunctionCallExpression callExpr = (IASTFunctionCallExpression) parent;
            IASTInitializerClause[] arguments = callExpr.getArguments();

            int argumentIdx = -1;

            for (int i = 0; i < arguments.length; i++)
                if (arguments[i] == clause)
                    argumentIdx = i;

            INode function = new MethodFinder(context, iterator).find(callExpr);

            if (function instanceof ICommonFunctionNode) {
                IVariableNode parameter = ((ICommonFunctionNode) function).getArguments().get(argumentIdx);
                type = VariableTypeUtils.getFullRawType((VariableNode) parameter);
            }

        } else if (parent instanceof IASTReturnStatement) {
            if (context instanceof IFunctionNode)
                type = ((IFunctionNode) context).getReturnType();
        }

        return type;
    }

    private String solveBinaryExpression(IASTBinaryExpression binaryExpr) {
        int operator = binaryExpr.getOperator();

        IASTExpression operand1 = binaryExpr.getOperand1();
        IASTExpression operand2 = binaryExpr.getOperand2();

        String type;

        switch (operator) {
            case IASTBinaryExpression.op_assign:
            case IASTBinaryExpression.op_plusAssign:
            case IASTBinaryExpression.op_moduloAssign:
            case IASTBinaryExpression.op_divideAssign:
            case IASTBinaryExpression.op_multiplyAssign:
            case IASTBinaryExpression.op_minusAssign:
            case IASTBinaryExpression.op_binaryXorAssign:
            case IASTBinaryExpression.op_binaryOrAssign:
            case IASTBinaryExpression.op_binaryAndAssign:
            case IASTBinaryExpression.op_plus:
            case IASTBinaryExpression.op_modulo:
            case IASTBinaryExpression.op_divide:
            case IASTBinaryExpression.op_multiply:
            case IASTBinaryExpression.op_minus:
            case IASTBinaryExpression.op_binaryAnd:
            case IASTBinaryExpression.op_binaryOr:
            case IASTBinaryExpression.op_binaryXor:
            case IASTBinaryExpression.op_max:
            case IASTBinaryExpression.op_min:
                type = new NewTypeResolver(context, iterator).solve(operand1);

                if (type == null)
                    type = new NewTypeResolver(context, iterator).solve(operand2);

                break;

            case IASTBinaryExpression.op_shiftLeft:
            case IASTBinaryExpression.op_shiftRight:
            case IASTBinaryExpression.op_shiftLeftAssign:
            case IASTBinaryExpression.op_shiftRightAssign:
                type = new NewTypeResolver(context, iterator).solve(operand1);
                break;

            case IASTBinaryExpression.op_equals:
            case IASTBinaryExpression.op_notequals:
            case IASTBinaryExpression.op_greaterEqual:
            case IASTBinaryExpression.op_lessEqual:
            case IASTBinaryExpression.op_greaterThan:
            case IASTBinaryExpression.op_lessThan:
            case IASTBinaryExpression.op_logicalAnd:
            case IASTBinaryExpression.op_logicalOr:
                type = VariableTypeUtils.BASIC.NUMBER.INTEGER.BOOL;
                break;

            default:
                type = null;
        }

        return type;
    }

    private String solveArrayIndexExpression(IASTArraySubscriptExpression arrayExpr) {
        String type = null;

        IASTExpression childExpr = arrayExpr.getArrayExpression();

        if (childExpr != null) {
            type = new NewTypeResolver(context, iterator).solve(childExpr);

            if (type != null) {
                type = type.replaceAll(IRegex.ARRAY_INDEX, SpecialCharacter.POINTER).trim();

                if (type.endsWith(SpecialCharacter.POINTER))
                    type = type.substring(0, type.length() - 1);
                else
                    type = null;
            }
        }

        return type;
    }

    private String solveFunctionCallExpression(IASTFunctionCallExpression funcCallExpr) {
        String type = null;

        INode foundNode = new MethodFinder(context).find(funcCallExpr);

        if (foundNode instanceof ICommonFunctionNode) {
            ICommonFunctionNode functionNode = (ICommonFunctionNode) foundNode;
            type = functionNode.getReturnType();
        }

        return type;
    }

    private String solveFieldReference(IASTFieldReference fieldRef) {
        String type = null;

        IASTNode parent = fieldRef.getParent();

        boolean isMethod = parent instanceof IASTFunctionCallExpression;
        isMethod = isMethod && ((IASTFunctionCallExpression) parent).getFunctionNameExpression() == fieldRef;

        // method case
        if (isMethod) {
            IASTFunctionCallExpression methodCallExpr = (IASTFunctionCallExpression) fieldRef.getParent();
            type = new NewTypeResolver(context, iterator).solve(methodCallExpr);

        }
        // attribute case
        else {
            IASTExpression owner = fieldRef.getFieldOwner();

            final String fieldName = fieldRef.getFieldName().getRawSignature();

            String ownerCoreType = new NewTypeResolver(context, iterator).solve(owner);

            if (ownerCoreType != null) {
                ownerCoreType = getStructureCoreType(ownerCoreType);

                VariableSearchingSpace searchingSpace = new VariableSearchingSpace(context);

                List<Level> space = searchingSpace.getSpaces();

                String relativePath = ownerCoreType.replace(SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS, File.separator);

                if (!relativePath.startsWith(File.separator))
                    relativePath = File.separator + relativePath;

                List<INode> nodes = Search.searchInSpace(space, new ClassvsStructvsNamespaceCondition(), relativePath);

                // Filter searched list
                nodes.removeIf(n -> {
                    if (n instanceof NamespaceNode)
                        return true;

                    if (n instanceof StructureNode) {
                        List<IVariableNode> attributes = ((StructureNode) n).getAttributes();

                        for (IVariableNode attribute : attributes) {
                            if (attribute.getName().equals(fieldName))
                                return false;
                        }
                    }

                    return true;
                });

                if (!nodes.isEmpty()) {
                    StructureNode structureNode = (StructureNode) nodes.get(0);

                    List<IVariableNode> attributes = structureNode.getAttributes();

                    for (IVariableNode attribute : attributes) {
                        if (attribute.getName().equals(fieldName)) {
                            type = VariableTypeUtils.getFullRawType((VariableNode) attribute);
                            break;
                        }
                    }
                }
            }
        }

        return type;
    }

    private String getStructureCoreType(String rawType) {
        String coreType = VariableTypeUtils.deleteClassKeyword(rawType);
        coreType = VariableTypeUtils.deleteReferenceOperator(coreType);
        coreType = VariableTypeUtils.deleteStorageClasses(coreType);
        coreType = VariableTypeUtils.deleteStructKeyword(coreType);
        coreType = VariableTypeUtils.deleteUnionKeyword(coreType);
        coreType = VariableTypeUtils.deleteVirtualAndInlineKeyword(coreType);
        coreType = coreType
                .replaceAll(IRegex.ARRAY_INDEX, SpecialCharacter.EMPTY)
                .replaceAll(IRegex.POINTER, SpecialCharacter.EMPTY);
        coreType = coreType.trim();

        return coreType;
    }

    private String solveIdExpression(IASTIdExpression idExpr) {
        String type = solveIdExprCurrentScope(idExpr);

        if (type == null)
            type = solveIdExprStructureScope(idExpr);

        if (type == null)
            type = solveIdExprNamespaceScope(idExpr);

        return type;
    }

    private String solveIdExprCurrentScope(IASTIdExpression idExpr) {
        String name = idExpr.getName().getRawSignature();

        String type = null;

        DeclaredVariableParser parser = new DeclaredVariableParser();
        IASTNode ast = null;

        if (context instanceof IFunctionNode) {
            ast = ((IFunctionNode) context).getAST();
        } else if (context instanceof CustomASTNode) {
            ast = ((CustomASTNode<?>) context).getAST();
            parser.shouldVisitParameterDeclarations = false;
        }

        if (ast != null)
            ast.accept(parser);

        List<IASTDeclarator> declarators = new ArrayList<>();

        for (IASTNode variable : parser.getVariables()) {
            if (variable instanceof IASTSimpleDeclaration) {
                IASTSimpleDeclaration declaration = (IASTSimpleDeclaration) variable;

                for (IASTDeclarator declarator : declaration.getDeclarators()) {
                    String variableName = declarator
                            .getName()
                            .getRawSignature();

                    if (variableName.equals(name))
                        declarators.add(declarator);
                }

            } else if (variable instanceof IASTParameterDeclaration) {
                IASTParameterDeclaration declaration = (IASTParameterDeclaration) variable;

                IASTDeclarator declarator = declaration.getDeclarator();

                String variableName = declarator.getName().getRawSignature();

                if (variableName.equals(name))
                    declarators.add(declarator);
            }
        }

        if (!declarators.isEmpty()) {
            IASTDeclarator declarator = declarators.get(0);

            if (context instanceof IFunctionNode) {
                final int line = getLineInFile(idExpr);

                declarator = declarators.stream()
                        .filter(d -> getLineInFile(d) <= line)
                        .min((o1, o2) -> {
                            int line1 = getLineInFile(o1);
                            int line2 = getLineInFile(o2);

                            return Integer.compare(line - line1, line - line2);
                        })
                        .orElse(declarators.get(0));
            }

            type = getDeclaratorType(declarator);
        }

        return type;
    }

    private String solveIdExprStructureScope(IASTIdExpression idExpr) {
        String name = idExpr.getName().getRawSignature();

        String type = null;

        StructureNode structure = null;

        if (context instanceof StructureNode)
            structure = (StructureNode) context;
        else if (context instanceof IFunctionNode) {
            INode realParent = context.getParent();

            if (((IFunctionNode) context).getRealParent() != null)
                realParent = ((IFunctionNode) context).getRealParent();

            if (realParent instanceof StructureNode)
                structure = (StructureNode) realParent;
        }

        if (structure != null) {
            List<IVariableNode> attributes = structure.getAttributes();

            for (IVariableNode attribute : attributes) {
                if (attribute.getName().equals(name)) {
                    type = VariableTypeUtils.getFullRawType((VariableNode) attribute);
                    break;
                }
            }

        }

        return type;
    }

    private String solveIdExprNamespaceScope(IASTIdExpression idExpr) {
        String name = idExpr.getName().getRawSignature();

        String type = null;

        NamespaceNode namespace = null;

        INode parent = context;

        if (parent instanceof IFunctionNode) {
            if (((IFunctionNode) parent).getRealParent() != null)
                parent = ((IFunctionNode) parent).getRealParent();
        }

        while (parent != null) {
            if (parent instanceof NamespaceNode) {
                namespace = (NamespaceNode) parent;
                break;
            } else
                parent = parent.getParent();
        }

        if (namespace != null) {
            String namespaceName = namespace.getName();

            INode projectRoot = Environment.getInstance().getProjectNode();
            List<INode> sameNamespaces = Search.searchNodes(projectRoot, new NamespaceNodeCondition());

            INode dataRoot = Environment.getInstance().getUserCodeRoot();
            sameNamespaces.addAll(Search.searchNodes(dataRoot, new NamespaceNodeCondition()));

            sameNamespaces.removeIf(n -> !n.getName().equals(namespaceName));

            sameNamespaces.remove(namespace);
            sameNamespaces.add(0, namespace);

            List<SearchCondition> conditions = Arrays.asList(new GlobalVariableNodeCondition(), new ExternVariableNodeCondition());

            for (INode node : sameNamespaces) {
                List<INode> variables = Search.searchNodes(node, conditions);

                boolean found = false;

                for (INode variable : variables) {
                    if (variable.getName().equals(name)) {
                        type = VariableTypeUtils.getFullRawType((VariableNode) variable);
                        found = true;
                        break;
                    }
                }

                if (found)
                    break;
            }
        }

        return type;
    }

    private int getLineInFile(IASTNode ast) {
        IASTFileLocation location = ast.getFileLocation();
        return location.getStartingLineNumber();
    }

    private String getDeclaratorType(IASTDeclarator declarator) {
        String type = null;

        IASTNode parent = declarator.getParent();

        VariableNode variableNode = new VariableNode();
        variableNode.setAST(parent);

        return variableNode.getRawType();

//        while (parent != null) {
//            if (parent instanceof IASTSimpleDeclaration) {
//                type = ((IASTSimpleDeclaration) parent).getDeclSpecifier().getRawSignature();
//                break;
//            } else if (parent instanceof IASTParameterDeclaration) {
//                type = ((IASTParameterDeclaration) parent).getDeclSpecifier().getRawSignature();
//                break;
//            } else
//                parent = parent.getParent();
//        }
//
//        return type;
    }

    private ExpressionType getExpressionType(IASTExpression expr) {
        if (expr instanceof IASTArraySubscriptExpression)
            return ExpressionType.ARRAY_INDEX;
        else if (expr instanceof IASTBinaryExpression)
            return ExpressionType.BINARY;
        else if (expr instanceof IASTUnaryExpression)
            return ExpressionType.UNARY;
        else if (expr instanceof IASTCastExpression)
            return ExpressionType.CAST;
        else if (expr instanceof IASTConditionalExpression)
            return ExpressionType.CONDITION;
        else if (expr instanceof IASTFieldReference)
            return ExpressionType.FIELD_REF;
        else if (expr instanceof IASTFunctionCallExpression)
            return ExpressionType.FUNCTION_CALL;
        else if (expr instanceof IASTIdExpression)
            return ExpressionType.ID;
        else if (expr instanceof IASTLiteralExpression)
            return ExpressionType.LITERAL;
        else
            return ExpressionType.UNSUPPORTED;
    }
    private enum ExpressionType {
        ARRAY_INDEX, //
        BINARY, //
        UNARY, //
        CAST, //
        CONDITION, //
        FIELD_REF,
        FUNCTION_CALL, //
        ID, //
        LITERAL, //
        UNSUPPORTED
    }
}
