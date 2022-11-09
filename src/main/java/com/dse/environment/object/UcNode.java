package com.dse.environment.object;

import java.util.ArrayList;
import java.util.List;

public class UcNode extends AbstractEnvironmentNode {
    private String name;
    private List<String> block = new ArrayList<>();

    public List<String> getBlock() {
        return block;
    }

    public void setBlock(List<String> block) {
        this.block = block;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return super.toString() + " : name of unit = " + getName() + "; code = \"" + block.toString() + "\"";
    }

    @Override
    public String exportToFile() {
        StringBuilder output = new StringBuilder(BEGIN_Uc + "\n");

        for (String line : getBlock())
            output.append(line).append("\n");

        output.append(END_Uc);
        return output.toString();
    }
}
