package com.dse.search.condition;

import com.dse.parser.object.INode;
import com.dse.search.SearchCondition;
import com.dse.testdata.object.DataNode;

public class DataNodeCondition extends SearchCondition {

    @Override
    public boolean isSatisfiable(INode n) {
        return n instanceof DataNode;
    }
}
