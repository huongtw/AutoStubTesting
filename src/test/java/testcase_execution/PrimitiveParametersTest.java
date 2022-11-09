package testcase_execution;

import com.dse.testcase_manager.TestCase;
import com.dse.testcase_manager.TestCaseManager;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

/**
 * Just use to test function in which the passing variables are primitive
 */
public class PrimitiveParametersTest extends AlgorithmProjectTest {
    private static final Logger logger = Logger.getLogger(PrimitiveParametersTest.class);

    @Test
    // Tritype(int,int,int)
    public void Tritype_aka_62393() throws Exception {
        init();
        loadEnvironment();
        String nameTestcase = "Tritype_aka_62393";

        TestCase testCase = TestCaseManager.getBasicTestCaseByName(nameTestcase);
        execute(testCase, EXECUTION_MODE);

        assertTrue(new File(testCase.getExecutableFile()).exists());
        assertTrue(new File(testCase.getTestPathFile()).exists());
    }

    @Test
    // reverse(long)
    public void reverse_aka_56846() throws Exception {
        init();
        loadEnvironment();
        String nameTestcase = "reverse_aka_56846";

        TestCase testCase = TestCaseManager.getBasicTestCaseByName(nameTestcase);
        execute(testCase, EXECUTION_MODE);

        assertTrue(new File(testCase.getExecutableFile()).exists());
        assertTrue(new File(testCase.getTestPathFile()).exists());
    }

    @Test
    // getPriority(char)
    public void getPriority_aka_12400() throws Exception {
        init();
        loadEnvironment();
        String nameTestcase = "getPriority_aka_12400";

        TestCase testCase = TestCaseManager.getBasicTestCaseByName(nameTestcase);
        execute(testCase, EXECUTION_MODE);

        assertTrue(new File(testCase.getExecutableFile()).exists());
        assertTrue(new File(testCase.getTestPathFile()).exists());
    }

    @Test
    // check_armstrong(long long)
    public void check_armstrong_aka_18277() throws Exception {
        init();
        loadEnvironment();
        String nameTestcase = "check_armstrong_aka_18277";

        TestCase testCase = TestCaseManager.getBasicTestCaseByName(nameTestcase);
        execute(testCase, EXECUTION_MODE);

        assertTrue(new File(testCase.getExecutableFile()).exists());
        assertTrue(new File(testCase.getTestPathFile()).exists());
    }

    @Test
    // check_armstrong(long long) + stub
    public void check_armstrong_aka_90825() throws Exception {
        init();
        loadEnvironment();
        String nameTestcase = "check_armstrong_aka_90825";

        TestCase testCase = TestCaseManager.getBasicTestCaseByName(nameTestcase);
        execute(testCase, EXECUTION_MODE);

        assertTrue(new File(testCase.getExecutableFile()).exists());
        assertTrue(new File(testCase.getTestPathFile()).exists());
    }
}
