package com.dse.search;

import com.dse.parser.object.INode;

public interface ISearchCondition {

    boolean isSatisfiable(INode n);

}