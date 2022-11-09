package com.dse.resolver;

import com.dse.compiler.message.error_tree.node.*;
import com.dse.parser.object.*;
import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;
import com.dse.util.VariableTypeUtils;
import org.eclipse.cdt.core.dom.ast.*;

import java.util.Arrays;
import java.util.List;

public class UndeclaredTypeResolver extends UndeclaredResolver<IASTDeclSpecifier> {

    private List<IASTDeclarator> declarators;

    public UndeclaredTypeResolver(IUndeclaredTypeErrorNode errorNode) {
        this.errorNode = (UndeclaredErrorNode<? extends INode>) errorNode;

        findScope();

        findAllDeclarators();
    }

    @Override
    public void resolve() {
        String methods = SpecialCharacter.EMPTY;

        String attributes = SpecialCharacter.EMPTY;

        INode sourceFile = Utils.getSourcecodeFile(errorNode.getLocation());

        int offset = calculateOffset();

        for (IASTDeclarator declarator : declarators) {
            String varName = declarator.getName().getRawSignature();

            IASTNode ast = null;
            if (scope instanceof IFunctionNode)
                ast = ((IFunctionNode) scope).getAST();
            else if (scope instanceof ISourcecodeFileNode)
                ast = ((ISourcecodeFileNode) scope).getAST();

            if (ast != null) {

                VariableOccursionDetector detector = new VariableOccursionDetector();
                detector.setVariableName(varName);

                ast.accept(detector);

                List<IASTNode> occurs = detector.getOccurs();

                for (IASTNode occur : occurs) {
                    if (occur instanceof IASTExpression) {
//                        TypeResolver typeResolver = new TypeResolver(scope);
//                        typeResolver.shouldTraceDeclaration = false;
//
//                        String type = typeResolver.exec((IASTExpression) occur);

                        String type = new NewTypeResolver(scope).exec((IASTExpression) occur);

                        if (VariableTypeUtils.getSimpleRealType(type).equals(errorNode.getName()))
                            type = null;

                        if (type != null) {
                            ResolvedSolution solution = new ResolvedSolution();

                            solution.setErrorNode(errorNode);
                            solution.setSourcecodeFile(sourceFile.getAbsolutePath());
                            solution.setResolvedSourceCode(String.format("#define %s %s\n", callExpression.getRawSignature(), type));
                            solution.setOffset(offset);

                            solutions.add(solution);
                        } else if (occur.getParent() instanceof IASTFieldReference) {
                            IASTFieldReference fieldReference = (IASTFieldReference) occur.getParent();

                            if (fieldReference.getParent() instanceof IASTFunctionCallExpression) {
                                String method = generateMethod(fieldReference);

                                if (method != null && !methods.contains(method))
                                    methods += method + SpecialCharacter.LINE_BREAK;

                            } else {
                                String fieldType = new NewTypeResolver(scope).exec(fieldReference);

                                if (fieldType != null) {
                                    String fieldName = fieldReference.getFieldName().getRawSignature();

                                    String fieldDeclaration = fieldType + " " + fieldName + ";";

                                    if (!attributes.contains(fieldDeclaration))
                                        attributes += fieldDeclaration + SpecialCharacter.LINE_BREAK;
                                }
                            }
                        }
                    }
                }
            }
        }

        if (!attributes.isEmpty() && !methods.isEmpty()) {
            String declaration = String.format("class %s {\n" +
                    "public:\n" +
                    "%s\n" +
                    "%s\n" +
                    "};", errorNode.getName(), attributes, methods);

            ResolvedSolution solution = new ResolvedSolution();

            solution.setErrorNode(errorNode);
            solution.setSourcecodeFile(sourceFile.getAbsolutePath());
            solution.setResolvedSourceCode(declaration);
            solution.setOffset(offset);

            solutions.add(solution);
        }

        System.out.println();
    }

    private String generateMethod(IASTFieldReference fieldReference) {
        IASTFunctionCallExpression expression = (IASTFunctionCallExpression) fieldReference.getParent();

        String returnType = new ReturnTypePrediction(scope).getReturnType(expression);

        IASTInitializerClause[] args = expression.getArguments();
        MissingFuntionGenerator generator;

        String functionName = fieldReference.getFieldName().getRawSignature();

        if (returnType != null) {
            generator = new MissingFuntionGenerator(returnType, functionName, args);
            generator.setContext(scope);
            ResolvedFunctionNode functionNode = generator.exec();

            if (functionNode != null) {
                return functionNode.getAST().getRawSignature();
            }
        }

        return null;
    }

    private void findAllDeclarators() {
        if (errorNode.getCall() instanceof IASTDeclSpecifier) {
            callExpression = (IASTDeclSpecifier) errorNode.getCall();

            IASTNode parent = callExpression.getParent();

            if (parent instanceof IASTParameterDeclaration) {
                IASTDeclarator declarator = ((IASTParameterDeclaration) parent).getDeclarator();
                declarators.add(declarator);
            } else if (parent instanceof IASTSimpleDeclaration) {
                IASTDeclarator[] declarators = ((IASTSimpleDeclaration) parent).getDeclarators();
                this.declarators = Arrays.asList(declarators);
            }
        }
    }

//    @Override
//    protected int calculateOffset() {
//        INode location = errorNode.getLocation();
//
//        int offset = 0;
//
//        if (location instanceof SourcecodeFileNode) {
//            if (scope instanceof IFunctionNode) {
//                offset = ((IFunctionNode) scope).getAST().getFileLocation().getNodeOffset();
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
}
