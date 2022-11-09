package com.dse.thread;

import com.dse.parser.object.ICommonFunctionNode;

public abstract class TaskRelatedAutoGen<V> extends AbstractAkaTask<V> {
    private ICommonFunctionNode function;

    public ICommonFunctionNode getFunction() {
        return function;
    }

    public void setFunction(ICommonFunctionNode function) {
        this.function = function;
    }
}
