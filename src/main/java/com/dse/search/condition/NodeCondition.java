package com.dse.search.condition;

import com.dse.parser.object.INode;
import com.dse.parser.object.Node;
import com.dse.search.SearchCondition;

public class NodeCondition extends SearchCondition {

    @Override
    public boolean isSatisfiable(INode n) {
        return n instanceof Node;
    }
}
