package com.dse.resolver;

import com.dse.compiler.message.error_tree.node.IErrorNode;
import com.dse.compiler.message.error_tree.node.RootErrorNode;
import com.dse.compiler.message.error_tree.node.ScopeErrorNode;
import com.dse.compiler.message.error_tree.node.UndeclaredErrorNode;
import com.dse.parser.object.*;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;

import java.util.ArrayList;
import java.util.List;

public abstract class UndeclaredResolver<T extends IASTNode> implements IUndeclaredResolver  {

    protected UndeclaredErrorNode<? extends INode> errorNode;

    protected List<ResolvedSolution> solutions = new ArrayList<>();

    protected T callExpression;

    protected INode scope;

    @Override
    public List<ResolvedSolution> getSolutions() {
        return solutions;
    }

    public void setSolutions(List<ResolvedSolution> solutions) {
        this.solutions = solutions;
    }

    public UndeclaredErrorNode<? extends INode> getErrorNode() {
        return errorNode;
    }

    public void setErrorNode(UndeclaredErrorNode<? extends INode> errorNode) {
        this.errorNode = errorNode;
    }

    public T getCallExpression() {
        return callExpression;
    }

    public void setCallExpression(T callExpression) {
        this.callExpression = callExpression;
    }

    public INode getScope() {
        return scope;
    }

    public void setScope(INode scope) {
        this.scope = scope;
    }

    protected void findScope() {
        IErrorNode parent = errorNode.getParent();

        if (parent instanceof ScopeErrorNode) {
            scope = ((ScopeErrorNode) parent).getScope();
        } else if (parent instanceof RootErrorNode) {
            scope = ((RootErrorNode) parent).getSource();
        }
    }

    protected int calculateOffset() {
        INode location = errorNode.getLocation();
        int offset = 0;

        if (location instanceof ISourcecodeFileNode) {
            if (scope instanceof IFunctionNode)
                offset = calculateOffsetInFunction();
            else
                offset = calculateOffsetInFile();

        } else if (location instanceof NamespaceNode) {
            offset = calculateOffsetInNamespace();

        } else if (location instanceof StructureNode) {
            offset = calculateOffsetInStructure();
        }

        return offset;
    }

    protected int calculateOffsetInFunction() {
        return  ((IFunctionNode) scope).getAST().getFileLocation().getNodeOffset();
    }

    private int calculateOffsetInFile() {
        int offset = 0;

        IASTNode statement = null;

        IASTNode current = callExpression;

        while (current != null) {
            if (current instanceof IASTStatement || current instanceof IASTDeclaration) {
                statement = current;
                break;
            }

            current = current.getParent();
        }

        assert statement != null;
        offset = statement.getFileLocation().getNodeOffset();

        return offset;
    }

    private int calculateOffsetInNamespace() {
        INode location = errorNode.getLocation();
        int offset = 0;

        ICPPASTNamespaceDefinition definition = ((NamespaceNode) location).getAST();
        IASTDeclaration[] declarations = definition.getDeclarations();

        if (declarations.length > 0) {
            IASTDeclaration last = declarations[declarations.length - 1];
            offset = last.getFileLocation().getNodeOffset();
            offset += last.getFileLocation().getNodeLength();
        } else {
            offset = definition.getFileLocation().getNodeOffset();
            String definitionRawCode = definition.getRawSignature();
            int openBracketPos = definitionRawCode.indexOf("{") + 1;
            offset += openBracketPos;
        }

        return offset;
    }

    private int calculateOffsetInStructure() {
        INode location = errorNode.getLocation();
        int offset = 0;

        IASTSimpleDeclaration declaration = ((StructureNode) location).getAST();

        IASTDeclSpecifier declSpecifier = declaration.getDeclSpecifier();

        if (declSpecifier instanceof IASTCompositeTypeSpecifier) {
            IASTDeclaration[] declarations = ((IASTCompositeTypeSpecifier) declSpecifier).getMembers();

            if (declarations.length > 0) {
                IASTDeclaration last = declarations[declarations.length - 1];
                offset = last.getFileLocation().getNodeOffset();
                offset += last.getFileLocation().getNodeLength();
            } else {
                offset = declaration.getFileLocation().getNodeOffset();
                String definitionRawCode = declaration.getRawSignature();
                int openBracketPos = definitionRawCode.indexOf("{") + 1;
                offset += openBracketPos;
            }
        }

        return offset;
    }
}
