package com.dse.testcase_execution.testdriver;

import com.dse.parser.object.INode;
import com.dse.project_init.ProjectClone;
import com.dse.testcase_manager.TestCase;
import com.dse.util.Utils;

public abstract class SimpleDriverGeneration extends DriverGeneration {

    protected TestCase testCase;

    public void setTestCase(TestCase testCase) {
        this.testCase = testCase;
    }

    @Override
    public void generate() {
        INode sourceNode = Utils.getSourcecodeFile(testCase.getFunctionNode());
        String cloneFilePath = ProjectClone.getClonedFilePath(sourceNode.getAbsolutePath());

        String akaName = "AKA_TC_" + testCase.getName().replace(".", "_").toUpperCase();
        String tempDefinition = "#define " + akaName + "\n";

        String includeStm = String.format("#include \"%s\"\n", cloneFilePath);

        testDriver = getTestDriverTemplate()
                .replace(CLONED_SOURCE_FILE_PATH_TAG, tempDefinition + includeStm);
    }
}
