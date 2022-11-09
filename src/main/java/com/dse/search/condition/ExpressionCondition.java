package com.dse.search.condition;

import auto_testcase_generation.testdatagen.se.expression.ExpressionNode;
import com.dse.parser.object.INode;
import com.dse.search.SearchCondition;

public class ExpressionCondition extends SearchCondition {

    @Override
    public boolean isSatisfiable(INode n) {
        return n instanceof ExpressionNode;
    }
}
