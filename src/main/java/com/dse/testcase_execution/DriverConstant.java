package com.dse.testcase_execution;

public interface DriverConstant {

    String ASSERT_ENABLE = "ASSERT_ENABLE";

    String CALL_COUNTER = "AKA_fCall";

    String TEST_NAME = "AKA_test_case_name";

    String TEST_PATH_FILE_TAG = "{{INSERT_PATH_OF_TEST_PATH_HERE}}";

    String RUN_TEST = "AKA_run_test";

    String MARK = "AKA_mark";

    String EXEC_TRACE_FILE_TAG = "{{INSERT_PATH_OF_EXE_RESULT_HERE}}";

    String ASSERT_METHOD = "AKA_assert_method";

    String ASSERT_DOUBLE_METHOD = "AKA_assert_double_method";

    String ASSERT_PTR_METHOD = "AKA_assert_ptr_method";

    String ASSERT = "AKA_assert";

    String ASSERT_DOUBLE = "AKA_assert_double";

    String ASSERT_PTR = "AKA_assert_ptr";

    String ADDITIONAL_HEADERS_TAG = "/*{{INSERT_ADDITIONAL_HEADER_HERE}}*/";

    String INCLUDE_CLONE_TAG = "/*{{INSERT_CLONE_SOURCE_FILE_PATHS_HERE}}*/";

    String TEST_SCRIPTS_TAG = "/*{{INSERT_TEST_SCRIPTS_HERE}}*/";

    String COMPOUND_SET_UP = "/* Compound test case setup */";

    String COMPOUND_TEAR_DOWN = "/* Compound test case teardown */";

    String ADD_TESTS_TAG = "/*{{ADD_TESTS_STM}}*/";

}
