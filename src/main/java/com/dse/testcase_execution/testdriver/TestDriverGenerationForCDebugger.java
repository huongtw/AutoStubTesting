package com.dse.testcase_execution.testdriver;

import com.dse.util.Utils;

/**
 * Generate test driver for function put in an .cpp file in debug mode
 *
 * @author ducanhnguyen
 */
public class TestDriverGenerationForCDebugger extends DebugTestDriverGeneration {
    @Override
    public String getTestDriverTemplate() {
        return Utils.readResourceContent(C_DEBUG_TEST_DRIVER_PATH);
    }

    @Override
    protected String wrapScriptInTryCatch(String script) {
        return script;
    }
}
