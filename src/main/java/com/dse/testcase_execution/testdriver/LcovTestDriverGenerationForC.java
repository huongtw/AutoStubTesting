package com.dse.testcase_execution.testdriver;


import com.dse.testcase_manager.TestCase;
import com.dse.util.Utils;

import java.io.File;

public class LcovTestDriverGenerationForC extends TestDriverGenerationForC {

    private String includeClonedFile;

    public void generate() throws Exception {
        File origin = new File(testCase.getSourceCodeFile());
        if (!origin.exists()) {
            TestDriverGenerationForC originTestDriver = new TestDriverGenerationForC();
            originTestDriver.setTestCase(testCase);
            originTestDriver.generate();
            testDriver = originTestDriver.getTestDriver();
        } else
            testDriver= Utils.readFileContent(origin);

        //String path = getCloneSourceCodeFile((TestCase) testCase);
         //includeClonedFile = String.format("#include \"%s\"\n", path);

       // testDriver = testDriver.replace(".akaignore", ".lcov.akaignore");
    }

    public String getIncludeClonedFile() {
        return includeClonedFile;
    }
}
