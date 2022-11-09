package com.dse.search.condition;

import com.dse.parser.object.INode;
import com.dse.parser.object.ISourcecodeFileNode;
import com.dse.search.SearchCondition;

public class HeaderNodeCondition extends SearchCondition {

    @Override
    public boolean isSatisfiable(INode n) {
        return n instanceof ISourcecodeFileNode;
    }
}
