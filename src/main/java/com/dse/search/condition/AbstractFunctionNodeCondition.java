package com.dse.search.condition;

import com.dse.parser.object.AbstractFunctionNode;
import com.dse.parser.object.INode;
import com.dse.search.SearchCondition;

public class AbstractFunctionNodeCondition extends SearchCondition {

    @Override
    public boolean isSatisfiable(INode n) {
        return n instanceof AbstractFunctionNode;
    }
}
