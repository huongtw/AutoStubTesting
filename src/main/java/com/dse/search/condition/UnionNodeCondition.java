package com.dse.search.condition;

import com.dse.parser.object.INode;
import com.dse.parser.object.UnionNode;
import com.dse.search.SearchCondition;

public class UnionNodeCondition extends SearchCondition {

    @Override
    public boolean isSatisfiable(INode n) {
        return n instanceof UnionNode;
    }
}
