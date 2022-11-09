package com.dse.testcase_manager;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

/**
 * A template function might have many real implementations.
 */
public class PrototypeOfFunction {
    @Expose
    private String functionNodePath; // macro + template

    @Expose
    private List<String> prototypes = new ArrayList<>(); // basic test case in test case folder

    public String getFunctionNodePath() {
        return functionNodePath;
    }

    public void setFunctionNodePath(String functionNodePath) {
        this.functionNodePath = functionNodePath;
    }

    public List<String> getPrototypes() {
        return prototypes;
    }

    public void setPrototypes(List<String> prototypes) {
        this.prototypes = prototypes;
    }
}
