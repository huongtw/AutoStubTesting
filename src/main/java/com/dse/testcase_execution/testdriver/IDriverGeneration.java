package com.dse.testcase_execution.testdriver;

import com.dse.testcase_execution.DriverConstant;

public interface IDriverGeneration {

    String CLONED_SOURCE_FILE_PATH_TAG = DriverConstant.INCLUDE_CLONE_TAG;

    String C_DEBUG_TEST_DRIVER_PATH = "/test-driver-templates/testdriver_debug.c";
    String CPP_DEBUG_TEST_DRIVER_PATH = "/test-driver-templates/testdriver_debug.cpp";
    String C_TEST_DRIVER_PATH = "/test-driver-templates/testdriver.c";
    String CPP_TEST_DRIVER_PATH = "/test-driver-templates/testdriver.cpp";

    void generate() throws Exception;

    String getTestDriver();
}
