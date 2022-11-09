package com.dse.resolver;

import com.dse.compiler.message.error_tree.node.IErrorNode;
import org.eclipse.cdt.core.dom.ast.IASTNode;

public class ResolvedSolution {
    private String resolvedSourceCode;

    private String sourcecodeFile;

    private int offset;

    private IErrorNode errorNode;

    public String getResolvedSourceCode() {
        return resolvedSourceCode;
    }

    public void setResolvedSourceCode(String resolvedSourceCode) {
        this.resolvedSourceCode = resolvedSourceCode;
    }

    public String getSourcecodeFile() {
        return sourcecodeFile;
    }

    public void setSourcecodeFile(String sourcecodeFile) {
        this.sourcecodeFile = sourcecodeFile;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getOffset() {
        return offset;
    }

    public IErrorNode getErrorNode() {
        return errorNode;
    }

    public void setErrorNode(IErrorNode errorNode) {
        this.errorNode = errorNode;
    }

    @Override
    public String toString() {
        return resolvedSourceCode;
    }
}
