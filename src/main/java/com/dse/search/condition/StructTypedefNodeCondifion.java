package com.dse.search.condition;

import com.dse.parser.object.INode;
import com.dse.parser.object.StructTypedefNode;
import com.dse.search.SearchCondition;

public class StructTypedefNodeCondifion extends SearchCondition {

    @Override
    public boolean isSatisfiable(INode n) {
        return n instanceof StructTypedefNode;
    }
}
