package com.dse.environment.object;

public class EnviroWhiteBoxNode extends AbstractEnvironmentNode {
    private boolean isActive = false;

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return super.toString() + ": active = " + isActive;
    }

    @Override
    public String exportToFile() {
        return ENVIRO_WHITE_BOX + " " + (isActive ? "YES" : "NO");
    }
}
