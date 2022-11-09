package com.dse.search.condition;

import com.dse.parser.object.ClassNode;
import com.dse.parser.object.INode;
import com.dse.search.SearchCondition;

public class ClassNodeCondition extends SearchCondition {

    @Override
    public boolean isSatisfiable(INode n) {
        return n instanceof ClassNode;
    }
}
