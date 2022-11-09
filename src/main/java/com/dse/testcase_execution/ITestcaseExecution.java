package com.dse.testcase_execution;

import com.dse.config.CommandConfig;
import com.dse.testcase_execution.testdriver.*;
import com.dse.testcase_manager.ITestCase;
import com.dse.logger.AkaLogger;

import java.io.IOException;

/**
 * Execute a test case
 */
public interface ITestcaseExecution {
    AkaLogger logger = AkaLogger.get(TestcaseExecution.class);

    /**
     * Execute the test data in debug mode. In this mode, there is no comparison between Expected Output and Real Output
     */
    int IN_DEBUG_MODE = 0;

    /**
     * Execute the test data in debug mode. In this mode, there is no comparison between Expected Output and Real Output
     */
    int IN_AUTOMATED_TESTDATA_GENERATION_MODE = 1;

//    /**
//     * Execute the test data entering by users. In this mode, there is no comparison between Expected Output and Real Output
//     */
//    int IN_EXECUTION_WITHOUT_GTEST_MODE = 2;

    /**
     * Cunit or Google Test
     *
     * Execute the test data in debug mode. In this mode, there is a comparison between Expected Output and Real Output
     */
    int IN_EXECUTION_WITH_FRAMEWORK_TESTING_MODE = 3;

    int getMode();

    void setMode(int mode);

    void execute() throws Exception;

    void initializeConfigurationOfTestcase(ITestCase testCase);

    TestDriverGeneration generateTestDriver(ITestCase testCase) throws Exception;

    IDriverGenMessage compileAndLink(CommandConfig customCommandConfig) throws IOException, InterruptedException;
}