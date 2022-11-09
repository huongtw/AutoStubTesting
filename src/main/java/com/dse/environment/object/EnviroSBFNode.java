package com.dse.environment.object;

import com.dse.parser.object.INode;
import com.dse.util.PathUtils;

/**
 * From VectorCast, page 451.
 * This command indicates a Unit Under Test filename, whose individual functions are to be made
 * stubbable at run time. If you have more than one UUT to be stubbed by function, list each unit name
 * separately, one per line, each with its own ENVIRO.STUB_BY_FUNCTION command.
 */
public class EnviroSBFNode extends AbstractEnvironmentNode {
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
        return ENVIRO_STUB_BY_FUNCTION + " " + PathUtils.toRelative(name);
    }

    public INode getUnit() {
        return unit;
    }

    public void setUnit(INode unit) {
        this.unit = unit;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EnviroSBFNode) {
            if (((EnviroSBFNode) obj).getName().equals(this.getName())
                    /*&& ((EnviroSBFNode) obj).getUnit().getAbsolutePath().equals(this.getUnit().getAbsolutePath())*/
                    )

                return true;
            else
                return false;
        } else
            return false;
    }
}
