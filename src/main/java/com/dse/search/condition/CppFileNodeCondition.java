package com.dse.search.condition;

import com.dse.parser.object.CppFileNode;
import com.dse.parser.object.INode;
import com.dse.search.SearchCondition;

public class CppFileNodeCondition extends SearchCondition {

    @Override
    public boolean isSatisfiable(INode n) {
        return n instanceof CppFileNode;
    }
}
