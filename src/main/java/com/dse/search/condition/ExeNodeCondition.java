package com.dse.search.condition;

import com.dse.parser.object.ExeNode;
import com.dse.parser.object.INode;
import com.dse.search.SearchCondition;

public class ExeNodeCondition extends SearchCondition {

    @Override
    public boolean isSatisfiable(INode n) {
        return n instanceof ExeNode;
    }
}
