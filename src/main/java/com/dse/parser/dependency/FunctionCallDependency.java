package com.dse.parser.dependency;

import com.dse.parser.object.INode;

public class FunctionCallDependency extends Dependency {

    public FunctionCallDependency(INode startArrow, INode endArrow) {
        super(startArrow, endArrow);
    }

    public boolean fromNode(INode func) {
        return this.startArrow == func;
    }

    public boolean toNode(INode func) {
        return this.endArrow == func;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FunctionCallDependency) {
            FunctionCallDependency dependency = (FunctionCallDependency) obj;
            return fromNode(dependency.getStartArrow()) && toNode(dependency.getEndArrow());
        }

        return false;
    }
}
