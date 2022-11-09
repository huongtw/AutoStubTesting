package com.dse.search.condition;

import com.dse.parser.object.INode;
import com.dse.parser.object.SpecialStructTypedefNode;
import com.dse.parser.object.StructNode;
import com.dse.parser.object.StructTypedefNode;
import com.dse.search.SearchCondition;

/**
 * Demo a condition
 *
 * @author DucAnh
 */
public class StructNodeCondition extends SearchCondition {

    @Override
    public boolean isSatisfiable(INode n) {
        return n instanceof StructNode || n instanceof SpecialStructTypedefNode || n instanceof StructTypedefNode;
    }
}
