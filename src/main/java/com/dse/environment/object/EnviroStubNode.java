package com.dse.environment.object;

import com.dse.util.PathUtils;

/**
 * VectorCast page 453.
 * This command is used to tell VectorCAST which dependent units it should stub by IMPLEMENTATION
 * during the environment creation.
 *
 * - If none of the dependent units are to be stubbed by implementation, then use ENVIRO.STUB:NONE.
 *
 * - If all of the dependent units are to be stubbed by implementation, then use ENVIRO.STUB:ALL.
 *
 * - ENVIRO.STUB: unit name to stub by implementation
 *
 * - ENVIRO.STUB:ALL_BY_PROTOTYPE: all units are not stubbed by implementation.
 * It can be combined with ENVIRO.STUB:<unit> to indicate that all units be
 * stubbed by prototype except <unit>, which should be stubbed by implementation.
 *
 * Similarly, it can be
 * combined with ENVIRO.DONT_STUB:<unit> to indicate that all units be stubbed by prototype except
 * <unit>, which should be non-stubbed.
 *
 * - By default, units that are not specified by name are not stubbed by implementation.
 */
public class EnviroStubNode extends AbstractEnvironmentNode {
    private String type;

    private String name; // absolute path

    public void setStub(String stub) {
        if (stub.equals(""))
            this.setType(NONE); // by default
        else if (stub.equals(ALL_BY_IMPLEMENTATION) || stub.equals(ALL_BY_PROTOTYPE))
            this.setType(stub);
        else{
            // this is the name of unit which is stubbed
            this.setName(stub);
        }
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return super.toString() + " :stub type = " + getType() + "; stub unit = " + getName();
    }

    @Override
    public String exportToFile() {
        /*
          Example 1: "ENVIRO.STUB: ALL_BY_PROTOTYPE"
          Example 2: "ENVIRO.STUB: ALL"
          Example 3: "ENVIRO.STUB: NONE"
          Example 4: "ENVIRO.STUB: C:\test.c"
         */
        if (getName() != null && getName().length() > 0)
            return ENVIRO_STUB + " " + PathUtils.toRelative(name);
        else if (getType() != null && getType().length() > 0)
            return ENVIRO_STUB + " " + getType();
        else
            return "";
    }

    public static final String NONE = "NONE";
    public static final String ALL_BY_IMPLEMENTATION = "ALL";
    public static final String ALL_BY_PROTOTYPE = "ALL_BY_PROTOTYPE";
}
