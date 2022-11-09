package com.dse.search.condition;

import auto_testcase_generation.testdatagen.se.expression.OperatorNode;
import com.dse.parser.object.INode;
import com.dse.search.SearchCondition;

public class OperatorCondition extends SearchCondition {

    @Override
    public boolean isSatisfiable(INode n) {
        return n instanceof OperatorNode;
    }
}
