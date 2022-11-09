package com.dse.search.condition;

import com.dse.parser.object.DefinitionFunctionNode;
import com.dse.parser.object.INode;
import com.dse.search.SearchCondition;

/**
 * Demo a condition
 *
 * @author DucAnh
 */
public class DefinitionFunctionNodeCondition extends SearchCondition {

    @Override
    public boolean isSatisfiable(INode n) {
        return n instanceof DefinitionFunctionNode;
    }
}
