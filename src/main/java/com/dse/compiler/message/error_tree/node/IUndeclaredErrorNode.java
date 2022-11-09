package com.dse.compiler.message.error_tree.node;

import org.eclipse.cdt.core.dom.ast.IASTNode;

public interface IUndeclaredErrorNode extends IErrorNode {
    String getName();

    IASTNode getCall();

    int getLine();

    int getOffset();

}
