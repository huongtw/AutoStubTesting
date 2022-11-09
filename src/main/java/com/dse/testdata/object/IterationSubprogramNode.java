package com.dse.testdata.object;

public class IterationSubprogramNode extends SubprogramNode {
    int index = 0;

    public IterationSubprogramNode() {}

    public IterationSubprogramNode(String name) {
        super.setName(name);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public boolean isStubable() {
        return true;
    }
}
