package com.dse.resolver;

import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;

import java.util.ArrayList;
import java.util.List;

public class VariableOccursionDetector extends ASTVisitor {
    private String variableName;

    private List<IASTNode> occurs = new ArrayList<>();

    public VariableOccursionDetector() {
        shouldVisitExpressions = true;
        shouldVisitNames = true;
    }

    @Override
    public int visit(IASTExpression expression) {
        if (expression instanceof IASTIdExpression) {
            String property = expression.getPropertyInParent().getName();
            if (!(property.equals(FUNCTION_NAME_PROPERTY))) {
                if (expression.getRawSignature().equals(variableName)) {
                    occurs.add(expression);
                }
            }
        }

        return PROCESS_CONTINUE;
    }

    @Override
    public int visit(IASTName name) {
        if (name.getRawSignature().equals(variableName)) {
            if (name.getParent() instanceof IASTFieldReference || name.getParent() instanceof ICPPASTQualifiedName) {
                occurs.add(name);
            }
        }

        return PROCESS_CONTINUE;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public void setOccurs(List<IASTNode> occurs) {
        this.occurs = occurs;
    }

    public List<IASTNode> getOccurs() {
        return occurs;
    }

    private static final String FUNCTION_NAME_PROPERTY = "IASTFunctionCallExpression.FUNCTION_NAME [IASTExpression]";
}
