package com.dse.parser;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;

import java.util.ArrayList;
import java.util.List;

public class FunctionCallParser extends ASTVisitor {

    private List<IASTFunctionCallExpression> expressions = new ArrayList<>();

    public List<IASTFunctionCallExpression> getExpressions() {
        return expressions;
    }

    public List<IASTSimpleDeclaration> unexpectedCalledFunctions = new ArrayList<>();

    public List<IASTSimpleDeclaration> getUnexpectedCalledFunctions() {
        return unexpectedCalledFunctions;
    }

    public FunctionCallParser() {
        shouldVisitExpressions = true;
        shouldVisitNames = true;
    }

    @Override
    public int visit(IASTName name) {
        IASTNode parent = name.getParent().getParent();
        if (parent instanceof IASTSimpleDeclaration){
            String syntax = parent.getRawSignature();
            if (syntax.contains(")") && syntax.contains("(")) {
                unexpectedCalledFunctions.add((IASTSimpleDeclaration) parent);
            }
        }

        return PROCESS_CONTINUE;
    }

    @Override
    public int visit(IASTExpression expression)
    {

        if (expression instanceof IASTFunctionCallExpression)
            expressions.add((IASTFunctionCallExpression) expression);

        return PROCESS_CONTINUE;
    }

}
