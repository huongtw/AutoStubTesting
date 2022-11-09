package com.dse.environment.object;

import java.util.ArrayList;
import java.util.List;

public class StubUserCodeNode extends AbstractEnvironmentNode {
    private String unitName = "";
    private String subFunctionName = "";
    private List<String> block = new ArrayList<>();

    public void setBlock(List<String> block) {
        this.block = block;
    }

    public List<String> getBlock() {
        return block;
    }

    public void setSubFunctionName(String subFunctionName) {
        this.subFunctionName = subFunctionName;
    }

    public String getSubFunctionName() {
        return subFunctionName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public String getUnitName() {
        return unitName;
    }

    @Override
    public String toString() {
        return super.toString() + "; unit name = " + getUnitName() + " ;subfunction name = " + getSubFunctionName() + " ;block = " + getBlock();
    }
}
