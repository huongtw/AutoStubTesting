package com.dse.environment.object;

public class EnviroNameNode extends AbstractEnvironmentNode {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return super.toString() + ": name = " + getName();
    }

    @Override
    public String exportToFile() {
        return ENVIRO_NAME + " " + getName();
    }
}
