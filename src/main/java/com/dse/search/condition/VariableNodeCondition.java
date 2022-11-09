package com.dse.search.condition;

import com.dse.parser.object.CloneVariableNode;
import com.dse.parser.object.INode;
import com.dse.parser.object.VariableNode;
import com.dse.search.SearchCondition;

public class VariableNodeCondition extends SearchCondition {

    @Override
    public boolean isSatisfiable(INode n) {
        return n instanceof VariableNode && !(n instanceof CloneVariableNode);
    }
}
