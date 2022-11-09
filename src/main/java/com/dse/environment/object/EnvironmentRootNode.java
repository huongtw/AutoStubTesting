package com.dse.environment.object;

public class EnvironmentRootNode extends AbstractEnvironmentNode {
    private String environmentScriptPath;

    @Override
    public String exportToFile() {
        StringBuilder output = new StringBuilder(new EnviroNewNode().exportToFile() + "\n");

        for (IEnvironmentNode child : getChildren()) {
            if (child instanceof EnviroEndNode || child instanceof EnviroNewNode) {
                // These two nodes are special nodes to mark the beginning and the end of environment script.
            } else {
                output.append(child.exportToFile()).append("\n");
            }
        }

        output.append(new EnviroEndNode().exportToFile());
        return output.toString();
    }

    public String getEnvironmentScriptPath() {
        return environmentScriptPath;
    }

    public void setEnvironmentScriptPath(String environmentScriptPath) {
        this.environmentScriptPath = environmentScriptPath;
    }
}
