package com.dse.search.condition;

import com.dse.parser.object.FolderNode;
import com.dse.parser.object.INode;
import com.dse.search.SearchCondition;


public class FolderNodeCondition extends SearchCondition {

    @Override
    public boolean isSatisfiable(INode n) {
        return n instanceof FolderNode;
    }
}
