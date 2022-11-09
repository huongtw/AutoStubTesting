package com.dse.search;

import com.dse.parser.object.INode;

public abstract class SearchCondition implements ISearchCondition {

    /*
     * (non-Javadoc)
     *
     * @see
     * com.fit.utils.search.ISearchCondition#isSatisfiable(com.fit.tree.object.
     * INode)
     */
    @Override
    public abstract boolean isSatisfiable(INode n);
}
