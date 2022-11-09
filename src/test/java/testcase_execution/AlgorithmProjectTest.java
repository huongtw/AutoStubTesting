package testcase_execution;

import org.apache.log4j.Logger;

/**
 * Use to test environment ALGO_TEST
 */
public class AlgorithmProjectTest extends AbstractTestCaseExecutionTest {
    private static final Logger logger = Logger.getLogger(AlgorithmProjectTest.class);

    protected void init() {
        NEW_ENV_PATH = "/Users/ducanhnguyen/Documents/akautauto/local/working-directory/ALGO_TEST";
        NEW_PRJ_PATH = "/Users/ducanhnguyen/Documents/akautauto/datatest/duc-anh/Algorithm";
        PREV_ENV_PATH = "/mnt/e/LAB/akautauto/local/working-directory/ALGO_TEST";
        PREV_PRJ_PATH = "/mnt/e/LAB/akautauto/datatest/duc-anh/Algorithm";

        ENVIRONMENT_PATH = "local/working-directory/ALGO_TEST.env";
        TEST_ENVIR_PATH = "local/working-directory/ALGO_TEST.tst";
        TEST_CASES_DIRECTORY = "local/working-directory/ALGO_TEST/test-cases";
        PHYSICAL_TREE_PATH = "local/working-directory/ALGO_TEST/physical_tree.json";
        ENVIRONMENT_DIRECTORY = "local/working-directory/ALGO_TEST";
    }
}
