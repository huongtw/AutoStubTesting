package com.dse.parser;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;

import java.util.ArrayList;
import java.util.List;

public class DeclaredVariableParser extends ASTVisitor {
    private List<IASTNode> variables = new ArrayList<>();

    public DeclaredVariableParser() {
        shouldVisitParameterDeclarations = true;
        shouldVisitDeclarations = true;
    }


    @Override
    public int visit(IASTDeclaration declaration) {
        if (declaration instanceof IASTSimpleDeclaration) {
            variables.add(declaration);
        }

        return PROCESS_CONTINUE;
    }

    @Override
    public int visit(IASTParameterDeclaration parameterDeclaration) {
        variables.add(parameterDeclaration);

        return PROCESS_CONTINUE;
    }

    public void setVariables(List<IASTNode> variables) {
        this.variables = variables;
    }

    public List<IASTNode> getVariables() {
        return variables;
    }
}

