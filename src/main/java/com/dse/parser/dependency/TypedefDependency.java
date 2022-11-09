package com.dse.parser.dependency;

import com.dse.parser.object.INode;

public class TypedefDependency extends Dependency {

    public TypedefDependency(INode startArrow, INode endArrow) {
        super(startArrow, endArrow);
    }

    public TypedefDependency() {}
}
