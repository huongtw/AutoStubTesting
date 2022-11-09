package com.dse.environment.object;

import com.dse.parser.object.INode;
import com.dse.util.PathUtils;

/**
 * VectorCast page 453
 * This command is used to tell VectorCAST which units to not stub, that is, of which units to use the “real
 * code.”
 */
public class EnviroDontStubNode extends AbstractEnvironmentNode {
    private String name; // absolute path

    private INode unit;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String exportToFile() {
        return ENVIRO_DONT_STUB + " " + PathUtils.toRelative(name);
    }

    public INode getUnit() {
        return unit;
    }

    public void setUnit(INode unit) {
        this.unit = unit;
    }
}
