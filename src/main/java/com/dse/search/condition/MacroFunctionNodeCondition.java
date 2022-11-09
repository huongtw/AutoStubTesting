package com.dse.search.condition;

import com.dse.parser.object.INode;
import com.dse.parser.object.MacroFunctionNode;
import com.dse.search.SearchCondition;

public class MacroFunctionNodeCondition extends SearchCondition {

    @Override
    public boolean isSatisfiable(INode n) {
        return n instanceof MacroFunctionNode;
    }
}
