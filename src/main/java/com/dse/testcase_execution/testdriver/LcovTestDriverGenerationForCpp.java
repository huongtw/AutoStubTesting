package com.dse.testcase_execution.testdriver;


import com.dse.util.Utils;

import java.io.File;

public class LcovTestDriverGenerationForCpp extends TestDriverGenerationForCpp {
    public void generate() throws Exception {
        File origin = new File(testCase.getSourceCodeFile());
        if (!origin.exists()) {
            TestDriverGenerationForCpp originTestDriver = new TestDriverGenerationForCpp();
            originTestDriver.setTestCase(testCase);
            originTestDriver.generate();
            testDriver = originTestDriver.getTestDriver();
        } else
            testDriver = Utils.readFileContent(origin);

//        testDriver = testDriver.replace(".akaignore", ".lcov.akaignore");
    }
}
