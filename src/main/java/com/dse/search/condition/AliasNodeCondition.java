package com.dse.search.condition;

import com.dse.parser.object.AliasDeclaration;
import com.dse.parser.object.INode;
import com.dse.search.SearchCondition;

public class AliasNodeCondition extends SearchCondition {

    @Override
    public boolean isSatisfiable(INode n) {
        return n instanceof AliasDeclaration;
    }
}
