package testcase_execution;

import com.dse.testcase_manager.TestCase;
import com.dse.testcase_manager.TestCaseManager;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

public class PointerParametersTest extends AlgorithmProjectTest {
    private static final Logger logger = Logger.getLogger(PointerParametersTest.class);

    @Test
    // ArrayCmp(int,unsigned char*,unsigned char*)
    public void ArrayCmp_aka_41486() throws Exception {
        init();
        loadEnvironment();
        String nameTestcase = "ArrayCmp_aka_41486";

        TestCase testCase = TestCaseManager.getBasicTestCaseByName(nameTestcase);
        execute(testCase, EXECUTION_MODE);

        assertTrue(new File(testCase.getExecutableFile()).exists());
        assertTrue(new File(testCase.getTestPathFile()).exists());
    }

    @Test
    // string_length1(char*)
    public void string_length1_aka_25967() throws Exception {
        init();
        loadEnvironment();
        String nameTestcase = "string_length1_aka_25967";

        TestCase testCase = TestCaseManager.getBasicTestCaseByName(nameTestcase);
        execute(testCase, EXECUTION_MODE);

        assertTrue(new File(testCase.getExecutableFile()).exists());
        assertTrue(new File(testCase.getTestPathFile()).exists());
    }


}
