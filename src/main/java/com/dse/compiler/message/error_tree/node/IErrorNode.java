package com.dse.compiler.message.error_tree.node;

import java.util.List;

public interface IErrorNode {
    void setParent(IErrorNode parent);

    IErrorNode getParent();

    String getMessage();

    void setMessage(String message);

    void setChildren(List<IErrorNode> children);

    List<IErrorNode> getChildren();
}
