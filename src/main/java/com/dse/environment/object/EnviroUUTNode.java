package com.dse.environment.object;

import com.dse.parser.object.INode;
import com.dse.util.PathUtils;

/**
 * VectorCast, page 451.
 * This command indicates a Unit Under Test filename. If you have more than one UUT, list each unit name
 * separately, one per line, each with its own ENVIRO.UUT command. If a unit is listed twice, in two
 * different commands (i.e. ENVIRO.UUT and ENVIRO.STUB), then the first time the unit’s name is
 * encountered takes precedence over other appearances.
 */
public class EnviroUUTNode extends AbstractEnvironmentNode {
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
        return super.toString() + ": uut = " + getName();
    }

    @Override
    public String exportToFile() {
        return ENVIRO_UUT + " " + PathUtils.toRelative(name);
    }

    public INode getUnit() {
        return unit;
    }

    public void setUnit(INode unit) {
        this.unit = unit;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EnviroUUTNode) {
            if (((EnviroUUTNode) obj).getName().equals(this.getName())
                    /*&& ((EnviroUUTNode) obj).getUnit().getAbsolutePath().equals(this.getUnit().getAbsolutePath())
                    */)
                return true;
            else
                return false;
        } else
            return false;
    }
}
