package com.dse.testcase_execution.testdriver;

import com.dse.parser.object.INode;
import com.dse.project_init.LcovProjectClone;
import com.dse.project_init.ProjectClone;
import com.dse.testcase_manager.TestCase;
import com.dse.util.Utils;

public abstract class DebugTestDriverGeneration extends TestDriverGeneration {

    @Override
    public void generate() throws Exception {
        super.generate();

        testCase.generateDebugCommands();
    }

//    @Override
//    protected String getCloneSourceCodeFile(TestCase testcase) {
//        INode sourcecodeFile = Utils.getSourcecodeFile(testcase.getFunctionNode());
//        return LcovProjectClone.getLcovClonedFilePath(sourcecodeFile.getAbsolutePath());
//    }
}
