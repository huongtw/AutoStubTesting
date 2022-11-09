package com.dse.testcase_execution.testdriver;

import com.dse.util.Utils;

public class SimpleDriverGenerationForCpp extends SimpleDriverGeneration {

    @Override
    public String getTestDriverTemplate() {
        return Utils.readResourceContent(CPP_DEBUG_TEST_DRIVER_PATH);
    }

}
