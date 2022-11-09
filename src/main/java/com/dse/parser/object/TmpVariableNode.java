package com.dse.parser.object;

public class TmpVariableNode extends VariableNode {
    private String name;

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setParent(INode parent) {
        // parent does not know about its child
        this.parent = parent;
    }

    @Override
    public String getName() {
        return name;
    }
}
