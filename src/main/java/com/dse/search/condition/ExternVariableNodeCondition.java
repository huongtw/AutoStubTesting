package com.dse.search.condition;

import com.dse.parser.object.INode;
import com.dse.parser.object.IVariableNode;
import com.dse.parser.object.VariableNode;
import com.dse.search.SearchCondition;

/**
 * Represent extern variable, e.g., "extern int MY_MAX_VALUE"
 *
 * @author DucAnh
 */
public class ExternVariableNodeCondition extends SearchCondition {

    @Override
    public boolean isSatisfiable(INode n) {
        return n instanceof VariableNode && ((IVariableNode) n).isExtern();
    }
}
