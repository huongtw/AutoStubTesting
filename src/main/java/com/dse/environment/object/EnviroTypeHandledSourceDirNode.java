package com.dse.environment.object;

import com.dse.util.PathUtils;

public class EnviroTypeHandledSourceDirNode extends AbstractEnvironmentNode {
    private String typeHandledSourceDir;

    public String getTypeHandledSourceDir() {
        return typeHandledSourceDir;
    }

    public void setTypeHandledSourceDir(String typeHandledSourceDir) {
        this.typeHandledSourceDir = typeHandledSourceDir;
    }

    @Override
    public String exportToFile() {
        return ENVIRO_TYPE_HANDLED_SOURCE_DIR + " " + PathUtils.toRelative(typeHandledSourceDir);
    }
}
