package com.dse.testdata.object.stl;

import com.dse.testdata.object.ValueDataNode;

import java.util.ArrayList;
import java.util.List;

public abstract class STLDataNode extends ValueDataNode {

    /**
     * Parameter -> Argument
     * Eg: T -> int, V -> char
     */
    protected List<String> arguments;

    public List<String> getArguments() {
        return arguments;
    }

    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }

    @Override
    public STLDataNode clone() {
        STLDataNode clone = (STLDataNode) super.clone();
        clone.arguments = new ArrayList<>(arguments);
        return clone;
    }
}
