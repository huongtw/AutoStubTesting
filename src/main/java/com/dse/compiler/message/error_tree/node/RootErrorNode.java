package com.dse.compiler.message.error_tree.node;

import com.dse.parser.object.ISourcecodeFileNode;

public class RootErrorNode extends ErrorNode {
    private ISourcecodeFileNode source;

    public ISourcecodeFileNode getSource() {
        return source;
    }

    public void setSource(ISourcecodeFileNode source) {
        this.source = source;
    }
}
