package com.dse.search.condition;

import com.dse.parser.object.INode;
import com.dse.parser.object.MacroDefinitionNode;
import com.dse.search.SearchCondition;

public class MacroDefinitionNodeCondition extends SearchCondition {

    @Override
    public boolean isSatisfiable(INode n) {
        return n instanceof MacroDefinitionNode;
    }
}
