package com.dse.resolver;

import com.dse.compiler.message.error_tree.node.*;
import com.dse.parser.object.*;
import com.dse.util.IRegex;
import com.dse.util.Utils;
import com.dse.util.VariableTypeUtils;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class UndeclaredVariableResolver extends UndeclaredResolver<IASTNode> {

    public UndeclaredVariableResolver(IUndeclaredVariableErrorNode errorNode) {
        this.errorNode = (UndeclaredErrorNode<? extends INode>) errorNode;

        this.callExpression = errorNode.getCall();

        findScope();
    }

    @Override
    protected int calculateOffsetInFunction() {
        int offset;

        IASTNode statement = ((IFunctionNode) scope).getAST().getBody();

        if (statement instanceof IASTCompoundStatement) {
            IASTStatement[] statements = ((IASTCompoundStatement) statement).getStatements();

            if (statements.length > 0)
                statement = statements[0];
        }

        offset = statement.getFileLocation().getNodeOffset();

        return offset;
    }

    //    protected int calculateOffset() {
//        INode location = errorNode.getLocation();
//
//        int offset = 0;
//
//
//        if (location instanceof SourcecodeFileNode) {
//            if (scope instanceof IFunctionNode) {
//                IASTNode statement = ((IFunctionNode) scope).getAST().getBody();
//
//                if (statement instanceof IASTCompoundStatement) {
//                    IASTStatement[] statements = ((IASTCompoundStatement) statement).getStatements();
//
//                    if (statements.length > 0)
//                        statement = statements[0];
//                }
//
//                offset = statement.getFileLocation().getNodeOffset();
//
//            } else {
//                offset = calculateOffsetInFile();
//            }
//
//        } else if (location instanceof NamespaceNode) {
//            ICPPASTNamespaceDefinition definition = ((NamespaceNode) location).getAST();
//            IASTDeclaration[] declarations = definition.getDeclarations();
//
//            if (declarations.length > 0) {
//                IASTDeclaration last = declarations[declarations.length - 1];
//
//                offset = last.getFileLocation().getNodeOffset();
//
//                offset += last.getFileLocation().getNodeLength();
//            }
//
//        } else if (location instanceof StructureNode) {
//            IASTSimpleDeclaration declaration = ((StructureNode) location).getAST();
//
//            IASTDeclSpecifier declSpecifier = declaration.getDeclSpecifier();
//
//            if (declSpecifier instanceof IASTCompositeTypeSpecifier) {
//                IASTDeclaration[] declarations = ((IASTCompositeTypeSpecifier) declSpecifier).getMembers();
//
//                if (declarations.length > 0) {
//                    IASTDeclaration last = declarations[declarations.length - 1];
//
//                    offset = last.getFileLocation().getNodeOffset();
//
//                    offset += last.getFileLocation().getNodeLength();
//                }
//            }
//        }
//
//        return offset;
//    }

    @Override
    public void resolve() {
        List<VariableNode> variableNodes = generate();

        INode location = errorNode.getLocation();
        INode sourceFile = Utils.getSourcecodeFile(location);

        final int offset = calculateOffset();

        solutions = variableNodes
                .stream()
                .map(var -> {
                    ResolvedSolution solution = new ResolvedSolution();

                    solution.setSourcecodeFile(sourceFile.getAbsolutePath());
                    solution.setErrorNode(errorNode);
                    solution.setResolvedSourceCode(var.getAST().getRawSignature());
                    solution.setOffset(offset);

                    return solution;
                })
                .collect(Collectors.toList());
    }

    public List<VariableNode> generate() {
        if (callExpression instanceof IASTIdExpression)
            return resolveLocalCase();
        if (callExpression instanceof IASTName && callExpression.getParent() instanceof IASTFieldReference)
            return resolveStructureCase();
        if (callExpression instanceof IASTName && callExpression.getParent() instanceof ICPPASTQualifiedName)
            return resolveNamespaceCase();

        return new ArrayList<>();
    }

    private List<VariableNode> resolveLocalCase() {
        String type = new NewTypeResolver(scope).exec((IASTIdExpression) callExpression);

        if (type != null) {
            type = type.replaceAll(IRegex.ARRAY_INDEX, IRegex.POINTER);
            type = VariableTypeUtils.deleteStorageClasses(type);
            type = VariableTypeUtils.deleteVirtualAndInlineKeyword(type);

            VariableNode variableNode = generateVariable(type);

            return Collections.singletonList(variableNode);
        } else  {
            VariableTypePrediction prediction = new VariableTypePrediction(scope, errorNode.getName());

            String[] possibleTypes = prediction.getResults();

            List<VariableNode> results = new ArrayList<>();

            for (String possibleType : possibleTypes) {
                VariableNode variableNode = generateVariable(possibleType);
                results.add(variableNode);
            }

            return results;
        }
    }

    private List<VariableNode> resolveNamespaceCase() {
        List<VariableNode> results = new ArrayList<>();

        IASTNode expression = callExpression;

        while (!(expression instanceof IASTExpression))
            expression = expression.getParent();

        String type = new NewTypeResolver(scope).exec((IASTExpression) expression);

        if (type != null) {
            type = type.replaceAll(IRegex.ARRAY_INDEX, IRegex.POINTER);
            type = VariableTypeUtils.deleteStorageClasses(type);
            type = VariableTypeUtils.deleteVirtualAndInlineKeyword(type);

            VariableNode variableNode = generateVariable(type);

            return Collections.singletonList(variableNode);
        } else {
            VariableTypePrediction prediction = new VariableTypePrediction(scope, errorNode.getName());

            String[] possibleTypes = prediction.getResults();

            for (String possibleType : possibleTypes) {
                VariableNode variableNode = generateVariable(possibleType);
                results.add(variableNode);
            }
        }

        return results;
    }


    private List<VariableNode> resolveStructureCase() {
        List<VariableNode> results = new ArrayList<>();

        if (callExpression.getParent() instanceof IASTFieldReference) {
            String type = new NewTypeResolver(scope).exec((IASTFieldReference) callExpression.getParent());

            if (type != null) {
                type = type.replaceAll(IRegex.ARRAY_INDEX, IRegex.POINTER);
                type = VariableTypeUtils.deleteStorageClasses(type);
                type = VariableTypeUtils.deleteVirtualAndInlineKeyword(type);

                VariableNode variableNode = generateVariable(type);

                return Collections.singletonList(variableNode);
            } else {
                VariableTypePrediction prediction = new VariableTypePrediction(scope, errorNode.getName());

                String[] possibleTypes = prediction.getResults();

                for (String possibleType : possibleTypes) {
                    VariableNode variableNode = generateVariable(possibleType);
                    results.add(variableNode);
                }
            }
        }

        return results;
    }

    private VariableNode generateVariable(String type) {
        String variableDeclaration = type + " " + errorNode.getName();

        IASTNode ast = Utils.convertToIAST(variableDeclaration);

        if (ast instanceof IASTDeclarationStatement)
            ast = ((IASTDeclarationStatement) ast).getDeclaration();

        VariableNode variableNode = new VariableNode();
        variableNode.setAST(ast);

        return variableNode;
    }
}
