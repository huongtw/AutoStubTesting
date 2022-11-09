package com.dse.search.condition;

import com.dse.parser.object.INode;
import com.dse.parser.object.TypedefDeclaration;
import com.dse.search.SearchCondition;

public class TypedefNodeCondition extends SearchCondition {

    @Override
    public boolean isSatisfiable(INode n) {
        return n instanceof TypedefDeclaration;
    }
}
