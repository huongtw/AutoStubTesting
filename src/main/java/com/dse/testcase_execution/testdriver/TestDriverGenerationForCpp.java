package com.dse.testcase_execution.testdriver;

import com.dse.testcase_execution.DriverConstant;
import com.dse.util.Utils;

/**
 * Old name: TestdriverGenerationforCpp
 *
 * Generate test driver for function put in an .cpp file in executing test data entering by users
 * <p>
 * comparing EO and RO
 *
 * @author ducanhnguyen
 */
public class TestDriverGenerationForCpp extends AssertableTestDriverGeneration {

    @Override
    public String getTestDriverTemplate() {
        return Utils.readResourceContent(CPP_TEST_DRIVER_PATH);
    }

    protected String wrapScriptInTryCatch(String script) {
        return String.format(
                "try {\n" +
                        "%s\n" +
                        "} catch (std::exception& error) {\n" +
                        DriverConstant.MARK + "(\"Phat hien loi runtime\");\n" +
                        "}", script);
    }
}
