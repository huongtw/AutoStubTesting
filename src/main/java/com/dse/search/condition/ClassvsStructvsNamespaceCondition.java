package com.dse.search.condition;

import com.dse.parser.object.ClassNode;
import com.dse.parser.object.INode;
import com.dse.parser.object.NamespaceNode;
import com.dse.parser.object.StructNode;
import com.dse.search.SearchCondition;

public class ClassvsStructvsNamespaceCondition extends SearchCondition {

    @Override
    public boolean isSatisfiable(INode n) {
        return n instanceof ClassNode || n instanceof StructNode || n instanceof NamespaceNode;
    }
}
