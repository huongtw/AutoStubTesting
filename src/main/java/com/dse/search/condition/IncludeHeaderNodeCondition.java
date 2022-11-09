package com.dse.search.condition;

import com.dse.parser.object.INode;
import com.dse.parser.object.IncludeHeaderNode;
import com.dse.search.SearchCondition;

public class IncludeHeaderNodeCondition extends SearchCondition {

    @Override
    public boolean isSatisfiable(INode n) {
        return n instanceof IncludeHeaderNode;
    }
}
