package com.dse.parser.dependency;

import com.dse.parser.object.INode;

public class TypeDependency extends Dependency {

    public TypeDependency(INode startArrow, INode endArrow) {
        super(startArrow, endArrow);
    }

    public TypeDependency() {}
}
