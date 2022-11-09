package com.dse.regression.objects;

import com.dse.regression.RegressionScriptManager;
import com.dse.testcase_manager.ITestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RegressionScript {
    private String name;
    private List<ITestCase> testCases = new ArrayList<>();
    private String scriptFilePath;

    public static RegressionScript getNewRandomNameProbePoint() {
        RegressionScript regressionScript = new RegressionScript();
        regressionScript.setName("REGRESSION_SCRIPT." + new Random().nextInt(100000));
        return regressionScript;
    }

    public void setName(String name) {
        this.name = name;
        this.scriptFilePath = RegressionScriptManager.getInstance().getRegressionScriptFilePath(this);
    }

    public String getName() {
        return name;
    }

    public List<ITestCase> getTestCases() {
        return testCases;
    }

    public String getScriptFilePath() {
        return scriptFilePath;
    }
}
