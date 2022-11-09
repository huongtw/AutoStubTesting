package com.dse.parser.dependency;

import com.dse.parser.object.INode;

public class GetterDependency extends Dependency {

    public GetterDependency(INode startArrow, INode endArrow) {
        super(startArrow, endArrow);
    }

}
