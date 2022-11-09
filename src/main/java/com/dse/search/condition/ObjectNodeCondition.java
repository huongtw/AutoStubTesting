package com.dse.search.condition;

import com.dse.parser.object.*;
import com.dse.search.SearchCondition;

public class ObjectNodeCondition extends SearchCondition {

    @Override
    public boolean isSatisfiable(INode n) {
        return n instanceof ObjectNode;
    }

}
