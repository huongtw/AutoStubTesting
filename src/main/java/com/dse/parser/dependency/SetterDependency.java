package com.dse.parser.dependency;

import com.dse.parser.object.INode;

public class SetterDependency extends Dependency {

    public SetterDependency(INode startArrow, INode endArrow) {
        super(startArrow, endArrow);
    }

}
