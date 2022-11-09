package com.dse.testcase_execution.testdriver;

import com.dse.testcase_execution.DriverConstant;
import com.dse.util.Utils;

/**
 * Generate test driver for function put in an .cpp file in executing test data entering by users without
 * comparing EO and RO
 *
 * @author ducanhnguyen
 */
public class TestDriverGenerationForCppDebugger extends DebugTestDriverGeneration {
    @Override
    public String getTestDriverTemplate() {
        return Utils.readResourceContent(CPP_DEBUG_TEST_DRIVER_PATH);
    }

    @Override
    protected String wrapScriptInTryCatch(String script) {
        return String.format(
                "try {\n" +
                        "%s\n" +
                        "} catch (std::exception& error) {\n" +
                        DriverConstant.MARK + "(\"Phat hien loi runtime\");\n" +
                        "}", script);
    }
}
