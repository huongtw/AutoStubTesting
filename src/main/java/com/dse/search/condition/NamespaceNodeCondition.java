package com.dse.search.condition;

import com.dse.parser.object.INode;
import com.dse.parser.object.NamespaceNode;
import com.dse.search.SearchCondition;

public class NamespaceNodeCondition extends SearchCondition {

    @Override
    public boolean isSatisfiable(INode n) {
        return n instanceof NamespaceNode;
    }
}
