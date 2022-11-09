package com.dse.search.condition;

import com.dse.parser.object.CFileNode;
import com.dse.parser.object.INode;
import com.dse.search.SearchCondition;

public class CFileNodeCondition extends SearchCondition {

    @Override
    public boolean isSatisfiable(INode n) {
        return n instanceof CFileNode;
    }
}
