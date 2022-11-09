package com.dse.search.condition;

import com.dse.parser.object.EnumNode;
import com.dse.parser.object.EnumTypedefNode;
import com.dse.parser.object.INode;
import com.dse.parser.object.SpecialEnumTypedefNode;
import com.dse.search.SearchCondition;

/**
 * Demo a condition
 *
 * @author DucAnh
 */
public class EnumNodeCondition extends SearchCondition {

    @Override
    public boolean isSatisfiable(INode n) {
        return n instanceof EnumNode || n instanceof SpecialEnumTypedefNode || n instanceof EnumTypedefNode;
    }
}
