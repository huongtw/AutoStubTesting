package com.dse.environment.object;

import java.util.ArrayList;
import java.util.List;

public class EnviroUserCodeDependenciesNode extends AbstractEnvironmentNode{
    private List<String> block = new ArrayList<>();

    public List<String> getBlock() {
        return block;
    }

    public void setBlock(List<String> block) {
        this.block = block;
    }

    @Override
    public String exportToFile() {
        /**
         ENVIRO.USER_CODE_DEPENDENCIES:
         Header section
         ENVIRO.END_USER_CODE_DEPENDENCIES:
         */
        StringBuilder output = new StringBuilder(ENVIRO_USER_CODE_DEPENDENCIES + "\n");

        for (String line: getBlock())
            output.append(line).append("\n");

        output.append(ENVIRO_END_USER_CODE_DEPENDENCIES);
        return output.toString();
    }
}
