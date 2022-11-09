package com.dse.environment.object;

import com.dse.parser.object.INode;
import com.dse.util.PathUtils;

public class EnviroIgnoreNode extends AbstractEnvironmentNode {
    private String name;

    private INode unit;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return super.toString() + ": unit name = " + getName();
    }

    @Override
    public String exportToFile() {
        return ENVIRO_IGNORE + " " + PathUtils.toRelative(name);
    }

    public INode getUnit() {
        return unit;
    }

    public void setUnit(INode unit) {
        this.unit = unit;
    }
}
