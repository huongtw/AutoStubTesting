package com.dse.compiler.message.error_tree.node;

import java.util.ArrayList;
import java.util.List;

public abstract class ErrorNode implements IErrorNode {
    protected IErrorNode parent;

    protected List<IErrorNode> children = new ArrayList<>();

    protected String message;

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public IErrorNode getParent() {
        return parent;
    }

    @Override
    public void setParent(IErrorNode parent) {
        this.parent = parent;

        if (!parent.getChildren().contains(this)) {
            if (parent instanceof ScopeErrorNode) {
                String parentMessage = parent.getMessage();
                parentMessage += message + "\n";
                parent.setMessage(parentMessage);
            }

            parent.getChildren().add(this);
        }
    }

    @Override
    public List<IErrorNode> getChildren() {
        return children;
    }

    @Override
    public void setChildren(List<IErrorNode> children) {
        this.children = children;
    }
}
