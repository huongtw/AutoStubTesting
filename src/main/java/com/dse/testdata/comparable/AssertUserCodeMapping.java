package com.dse.testdata.comparable;

import com.dse.environment.Environment;
import com.dse.testcase_execution.DriverConstant;
import com.dse.util.SourceConstant;
import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;
import org.eclipse.cdt.core.dom.ast.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.dse.testcase_execution.DriverConstant.ASSERT_METHOD;
import static com.dse.testdata.comparable.AssertMethod.*;
import static com.dse.testdata.comparable.UserCodeAssertMethod.*;

public class AssertUserCodeMapping {

    public static String convert(String content) throws Exception {
        IASTNode ast = Utils.convertToIAST(content);

        List<IASTNode> functionCalls = new ArrayList<>();

        ASTVisitor visitor = new ASTVisitor() {
            @Override
            public int visit(IASTExpression expression) {
                if (expression instanceof IASTFunctionCallExpression) {
                    functionCalls.add(expression);
                }
                return ASTVisitor.PROCESS_CONTINUE;
            }

            @Override
            public int visit(IASTDeclaration declaration) {
                if (declaration instanceof IASTSimpleDeclaration) {
                    if (declaration.getRawSignature().startsWith("AKA_ASSERT")) {
                        functionCalls.add(declaration);
                    }
                }

                return ASTVisitor.PROCESS_CONTINUE;
            }
        };

        visitor.shouldVisitExpressions = true;
        visitor.shouldVisitDeclarations = true;

        ast.accept(visitor);

        for (IASTNode expression : functionCalls) {
            String refactor = map(expression);
            if (refactor != null) {
                String oldContent = expression.getRawSignature();
                if (!oldContent.endsWith(SpecialCharacter.END_OF_STATEMENT))
                    oldContent += SpecialCharacter.END_OF_STATEMENT;

                content = content.replace(oldContent, refactor);
            }
        }

        content = content.replace(RETURN_TAG, SourceConstant.ACTUAL_OUTPUT);

        return content;
    }

    private static String map(IASTNode expression) throws Exception {
        StringBuilder builder = new StringBuilder();

        String functionName;
        String[] arguments;

        if (expression instanceof IASTFunctionCallExpression) {
            IASTFunctionCallExpression functionCall = (IASTFunctionCallExpression) expression;
            functionName = functionCall.getFunctionNameExpression().getRawSignature();

            arguments = Arrays.stream(functionCall.getArguments())
                    .map(IASTNode::getRawSignature)
                    .toArray(String[]::new);

        } else {
            IASTSimpleDeclaration declaration = (IASTSimpleDeclaration) expression;
            functionName = declaration.getDeclSpecifier().getRawSignature();
            arguments = Arrays.stream(declaration.getDeclarators())
                    .map(d -> d.getNestedDeclarator().getRawSignature())
                    .toArray(String[]::new);

        }

        boolean isC = Environment.getInstance().isC();

        String assertMethod;

        switch (functionName) {
            case AKA_ASSERT_EQUAL:
                assertMethod = ASSERT_EQUAL;
                break;

            case AKA_ASSERT_NOT_EQUAL:
                assertMethod = ASSERT_NOT_EQUAL;
                break;

            case AKA_ASSERT_DOUBLE_EQUAL:
                assertMethod = ASSERT_EQUAL;
                break;

            case AKA_ASSERT_DOUBLE_NOT_EQUAL:
                assertMethod = ASSERT_NOT_EQUAL;
                break;

            case AKA_ASSERT_STR_EQUAL:
                assertMethod = ASSERT_EQUAL;
                break;

            case AKA_ASSERT_STR_NOT_EQUAL:
                assertMethod = ASSERT_NOT_EQUAL;
                break;

            case AKA_ASSERT_PTR_EQUAL:
                assertMethod = ASSERT_EQUAL;
                break;

            case AKA_ASSERT_PTR_NOT_EQUAL:
                assertMethod = ASSERT_NOT_EQUAL;
                break;

            case AKA_ASSERT_TRUE:
                assertMethod = ASSERT_TRUE;
                break;

            case AKA_ASSERT_FALSE:
                assertMethod = ASSERT_FALSE;
                break;

            case AKA_ASSERT_NULL:
                assertMethod = ASSERT_NULL;
                break;

            case AKA_ASSERT_NOT_NULL:
                assertMethod = ASSERT_NOT_NULL;
                break;

            case AKA_ASSERT_LOWER:
                assertMethod = ASSERT_LOWER;
                break;

            case AKA_ASSERT_LOWER_OR_EQUAL:
                assertMethod = ASSERT_LOWER_OR_EQUAL;
                break;

            case AKA_ASSERT_GREATER:
                assertMethod = ASSERT_GREATER;
                break;

            case AKA_ASSERT_GREATER_OR_EQUAL:
                assertMethod = ASSERT_GREATER_OR_EQUAL;
                break;

            default:
                return null;
        }

//        builder.append(OPEN_BRACKET);
//
//        for (int i = 0; i < arguments.length; i++) {
//            builder.append(arguments[i]);
//            if (i < arguments.length - 1)
//                builder.append(SEPARATOR);
//        }
//
//        builder.append(CLOSE_BRACKET)
//                .append(SpecialCharacter.END_OF_STATEMENT);

        switch (functionName) {
            case AKA_ASSERT_EQUAL:
            case AKA_ASSERT_NOT_EQUAL:
            case AKA_ASSERT_STR_EQUAL:
            case AKA_ASSERT_STR_NOT_EQUAL:
            case AKA_ASSERT_LOWER:
            case AKA_ASSERT_LOWER_OR_EQUAL:
            case AKA_ASSERT_GREATER:
            case AKA_ASSERT_GREATER_OR_EQUAL:
                if (arguments.length == 2) {
                    String resultExportStm = getExportExeResultStm(arguments[0], arguments[1], arguments[1], assertMethod);
                    builder.append(resultExportStm);
                } else
                    throw new Exception(expression.getRawSignature());

                break;

            case AKA_ASSERT_DOUBLE_EQUAL:
            case AKA_ASSERT_DOUBLE_NOT_EQUAL:
                if (arguments.length == 3 && isC) {
                    String resultExportStm = getExportExeDoubleResultStm(arguments[0], arguments[1], arguments[1], assertMethod);
                    builder.append(resultExportStm);
                } else if (arguments.length == 2 && !isC) {
                    String resultExportStm = getExportExeResultStm(arguments[0], arguments[1], arguments[1], assertMethod);
                    builder.append(resultExportStm);
                } else
                    throw new Exception(expression.getRawSignature());

                break;

            case AKA_ASSERT_PTR_EQUAL:
            case AKA_ASSERT_PTR_NOT_EQUAL:
                if (arguments.length == 2) {
                    String resultExportStm = getExportExePtrResultStm(arguments[0], arguments[1], arguments[1], assertMethod);
                    builder.append(resultExportStm);
                } else
                    throw new Exception(expression.getRawSignature());

                break;

            case AKA_ASSERT_TRUE:
                if (arguments.length == 1) {
                    String expectedName = SourceConstant.EXPECTED_PREFIX + arguments[0];
                    String resultExportStm = getExportExeResultStm(arguments[0], expectedName, "true", assertMethod);
                    builder.append(resultExportStm);
                } else
                    throw new Exception(expression.getRawSignature());

                break;

            case AKA_ASSERT_FALSE:
                if (arguments.length == 1) {
                    String expectedName = SourceConstant.EXPECTED_PREFIX + arguments[0];
                    String resultExportStm = getExportExeResultStm(arguments[0], expectedName, "false", assertMethod);
                    builder.append(resultExportStm);
                } else
                    throw new Exception(expression.getRawSignature());

                break;

            case AKA_ASSERT_NULL:
                if (arguments.length == 1) {
                    String expectedName = SourceConstant.EXPECTED_PREFIX + arguments[0];
                    String resultExportStm = getExportExePtrResultStm(arguments[0], expectedName, "NULL", assertMethod);
                    builder.append(resultExportStm);
                } else
                    throw new Exception(expression.getRawSignature());

                break;

            case AKA_ASSERT_NOT_NULL:
                if (arguments.length == 1) {
                    String expectedName = SourceConstant.EXPECTED_PREFIX + arguments[0];
                    String resultExportStm = getExportExePtrResultStm(arguments[0], expectedName, "1", assertMethod);
                    builder.append(resultExportStm);
                } else
                    throw new Exception(expression.getRawSignature());

                break;
        }

        return builder.toString();
    }

    private static String getExportExeResultStm(String actualName, String expectedName, String expectedValue, String assertMethod) {
        return String.format(ASSERT_METHOD + "(\"%s\", %s, \"%s\", %s, \"%s\");\n",
                actualName, actualName, expectedName, expectedValue, assertMethod);
    }

    private static String getExportExePtrResultStm(String actualName, String expectedName, String expectedValue, String assertMethod) {
        return String.format(DriverConstant.ASSERT_PTR_METHOD + "(\"%s\", %s, \"%s\", %s, \"%s\");\n",
                actualName, actualName, expectedName, expectedValue, assertMethod);
    }

    private static String getExportExeDoubleResultStm(String actualName, String expectedName, String expectedValue, String assertMethod) {
        return String.format(DriverConstant.ASSERT_DOUBLE_METHOD + "(\"%s\", %s, \"%s\", %s, \"%s\");\n",
                actualName, actualName, expectedName, expectedValue, assertMethod);
    }

    private static final char OPEN_BRACKET = '(';
    private static final char CLOSE_BRACKET = ')';
    private static final String SEPARATOR = ", ";
    private static final String RETURN_TAG = "{{RETURN}}";
}
