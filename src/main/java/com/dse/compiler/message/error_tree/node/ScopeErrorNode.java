package com.dse.compiler.message.error_tree.node;

import com.dse.parser.object.INode;

public abstract class ScopeErrorNode extends ErrorNode {
    protected INode scope;

    public INode getScope() {
        return scope;
    }

    public void setScope(INode scope) {
        this.scope = scope;
    }
}
