package com.dse.testcase_execution.testdriver;

import com.dse.util.Utils;

public class SimpleDriverGenerationForC extends SimpleDriverGeneration {

    @Override
    public String getTestDriverTemplate() {
        return Utils.readResourceContent(C_DEBUG_TEST_DRIVER_PATH);
    }

}
