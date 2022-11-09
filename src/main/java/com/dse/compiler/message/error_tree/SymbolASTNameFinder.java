package com.dse.compiler.message.error_tree;

import com.dse.parser.object.IFunctionNode;
import com.dse.parser.object.INode;
import com.dse.parser.object.ISourcecodeFileNode;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;

public class SymbolASTNameFinder extends ASTVisitor {
    private final String target;

    private final Integer[] location;

    private IASTNode result;

    private CompileMessageParser.Undeclared type = CompileMessageParser.Undeclared.UNKNOWN;

    public SymbolASTNameFinder(INode node, String target, Integer[] location) {
        this.target = target;
        this.location = location;

        shouldVisitDeclSpecifiers = true;
        shouldVisitExpressions = true;
        shouldVisitNames = true;

        IASTNode ast = null;

        if (node instanceof IFunctionNode)
            ast = ((IFunctionNode) node).getAST();
        else if (node instanceof ISourcecodeFileNode) {
            ast = ((ISourcecodeFileNode) node).getAST();
        }

        if (ast != null)
            ast.accept(this);
    }

    @Override
    public int visit(IASTDeclSpecifier declSpec) {
        if (declSpec.getRawSignature().equals(target)) {
            int line = declSpec.getFileLocation().getStartingLineNumber();

            if (line == location[0]) {
                result = declSpec;
                type = CompileMessageParser.Undeclared.TYPE;
                return PROCESS_ABORT;
            }
        }

        return PROCESS_CONTINUE;
    }

    @Override
    public int visit(IASTExpression expression) {
        int line = expression.getFileLocation().getStartingLineNumber();

        if (line == location[0]) {
            if (expression instanceof IASTFunctionCallExpression) {
                IASTNode functionName = ((IASTFunctionCallExpression) expression)
                    .getFunctionNameExpression();

                if (functionName instanceof IASTIdExpression)
                    functionName = ((IASTIdExpression) functionName).getName().getLastName();

                if (functionName instanceof IASTFieldReference)
                    functionName = ((IASTFieldReference) functionName).getFieldName();

                if (functionName.getRawSignature().equals(target)) {
                    result = expression;
                    type = CompileMessageParser.Undeclared.FUNCTION;
                    return PROCESS_ABORT;
                }

            } else if (expression instanceof IASTIdExpression) {
                String property = expression.getPropertyInParent().getName();
                if (!(property.equals("IASTFunctionCallExpression.FUNCTION_NAME [IASTExpression]"))) {
                    if (expression.getRawSignature().equals(target)) {
                        result = expression;
                        type = CompileMessageParser.Undeclared.VARIABLE;
                        return PROCESS_ABORT;
                    }
                }
            }
        }

        return PROCESS_CONTINUE;
    }

    @Override
    public int visit(IASTName name) {
        if (name.getFileLocation() != null) {
            int line = name.getFileLocation().getStartingLineNumber();

            if (line == location[0]) {
                if (name.getRawSignature().equals(target)) {
                    if (name.getParent() instanceof IASTFieldReference || name.getParent() instanceof ICPPASTQualifiedName) {
                        result = name;

                        if (name.getParent() instanceof ICPPASTQualifiedName
                                && name.getParent().getParent() instanceof IASTNamedTypeSpecifier) {
                            type = CompileMessageParser.Undeclared.TYPE;
                            result = name.getParent().getParent();
                        } else
                            type = CompileMessageParser.Undeclared.VARIABLE;

                        return PROCESS_ABORT;
                    }
                }
            }
        }

        return PROCESS_CONTINUE;
    }

    public IASTNode getResult() {
        return result;
    }

    public Integer[] getLocation() {
        return location;
    }

    public CompileMessageParser.Undeclared getType() {
        return type;
    }

    public String getTarget() {
        return target;
    }
}
