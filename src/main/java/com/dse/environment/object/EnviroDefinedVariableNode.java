package com.dse.environment.object;

public class EnviroDefinedVariableNode extends AbstractEnvironmentNode {
    private String name;
    private String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return super.toString() + ": variable " + getName() + " = " + getValue();
    }

    @Override
    public String exportToFile() {
        if (getValue() != null && getValue().length() > 0)
            return ENVIRO_DEFINED_VARIABLE + " " + getName() + "=" + getValue();
        else
            return ENVIRO_DEFINED_VARIABLE + " " + getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        EnviroDefinedVariableNode that = (EnviroDefinedVariableNode) o;

        return name.equals(that.getName())
                && ((value == null && that.getValue() == null) || value.equals(that.getValue()));
    }
}
