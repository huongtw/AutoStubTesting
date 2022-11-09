package com.dse.search.condition;

import com.dse.parser.object.INode;
import com.dse.parser.object.UnknowObjectNode;
import com.dse.search.SearchCondition;

public class UnknownFileNodeCondition extends SearchCondition {

    @Override
    public boolean isSatisfiable(INode n) {
        return n instanceof UnknowObjectNode;
    }
}
