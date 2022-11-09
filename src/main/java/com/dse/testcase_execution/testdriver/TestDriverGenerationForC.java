package com.dse.testcase_execution.testdriver;

import com.dse.util.Utils;

/**
 * Generate test driver for function put in an .c file in executing test data entering by users
 *
 * comparing EO and RO
 *
 * @author ducanhnguyen
 */
public class TestDriverGenerationForC extends AssertableTestDriverGeneration {

    @Override
    public String getTestDriverTemplate() {
        return Utils.readResourceContent(C_TEST_DRIVER_PATH);
    }

    protected String wrapScriptInTryCatch(String script) {
        // no try-catch
        return script;
    }

}
