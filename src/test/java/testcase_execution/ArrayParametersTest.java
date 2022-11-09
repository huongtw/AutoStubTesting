package testcase_execution;

import com.dse.testcase_manager.TestCase;
import com.dse.testcase_manager.TestCaseManager;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

public class ArrayParametersTest extends AlgorithmProjectTest {
    private static final Logger logger = Logger.getLogger(ArrayParametersTest.class);

    @Test
    // uninit_var(int[3],int[3])
    public void uninit_var_aka_15387() throws Exception {
        init();
        loadEnvironment();
        String nameTestcase = "uninit_var_aka_15387";

        TestCase testCase = TestCaseManager.getBasicTestCaseByName(nameTestcase);
        execute(testCase, EXECUTION_MODE);

        assertTrue(new File(testCase.getExecutableFile()).exists());
        assertTrue(new File(testCase.getTestPathFile()).exists());
    }

    @Test
    // find_maximum(int[],int)
    public void find_maximum_aka_78363() throws Exception {
        init();
        loadEnvironment();
        String nameTestcase = "find_maximum_aka_78363";

        TestCase testCase = TestCaseManager.getBasicTestCaseByName(nameTestcase);
        execute(testCase, EXECUTION_MODE);

        assertTrue(new File(testCase.getExecutableFile()).exists());
        assertTrue(new File(testCase.getTestPathFile()).exists());
    }

    @Test
    // concatenate(char[],char[])
    public void concatenate_aka_10268() throws Exception {
        init();
        loadEnvironment();
        String nameTestcase = "concatenate_aka_10268";

        TestCase testCase = TestCaseManager.getBasicTestCaseByName(nameTestcase);
        execute(testCase, EXECUTION_MODE);

        assertTrue(new File(testCase.getExecutableFile()).exists());
        assertTrue(new File(testCase.getTestPathFile()).exists());
    }
}
