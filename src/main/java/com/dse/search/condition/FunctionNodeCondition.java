package com.dse.search.condition;

import com.dse.parser.object.IFunctionNode;
import com.dse.parser.object.INode;
import com.dse.search.SearchCondition;

/**
 * Demo a condition
 *
 * @author DucAnh
 */
public class FunctionNodeCondition extends SearchCondition {

    @Override
    public boolean isSatisfiable(INode n) {
        return n instanceof IFunctionNode;
    }
}
