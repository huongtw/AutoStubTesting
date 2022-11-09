package com.dse.search.condition;

import com.dse.parser.object.INode;
import com.dse.parser.object.UnionTypedefNode;
import com.dse.search.SearchCondition;

/**
 * Created by DucToan on 14/07/2017.
 */
public class UnionTypedefNodeCondifion extends SearchCondition {
    @Override
    public boolean isSatisfiable(INode n) {
        return n instanceof UnionTypedefNode;
    }
}
