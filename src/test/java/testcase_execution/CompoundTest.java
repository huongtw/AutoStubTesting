package testcase_execution;

import com.dse.testcase_manager.CompoundTestCase;
import com.dse.testcase_manager.TestCaseManager;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

public class CompoundTest extends AlgorithmProjectTest {

    @Test
    // Tritype_aka_62393 + Tritype_aka_78633
    public void COMPOUND_aka_78749() throws Exception {
        init();
        loadEnvironment();
        String nameTestcase = "COMPOUND_aka_78749";

        CompoundTestCase testCase = TestCaseManager.getCompoundTestCaseByName(nameTestcase);
        executeACompoundTestcase(testCase, EXECUTION_MODE);

        assertTrue(new File(testCase.getExecutableFile()).exists());
        assertTrue(new File(testCase.getTestPathFile()).exists());
    }

    @Test
    // check_armstrong_aka_18277 + check_armstrong_aka_90825
    public void COMPOUND_aka_82892() throws Exception {
        init();
        loadEnvironment();
        String nameTestcase = "COMPOUND_aka_82892";

        CompoundTestCase testCase = TestCaseManager.getCompoundTestCaseByName(nameTestcase);
        executeACompoundTestcase(testCase, EXECUTION_MODE);

        assertTrue(new File(testCase.getExecutableFile()).exists());
        assertTrue(new File(testCase.getTestPathFile()).exists());
    }

    @Test
    //  check_armstrong_aka_90825 + check_armstrong_aka_18277
    public void COMPOUND_aka_70462() throws Exception {
        init();
        loadEnvironment();
        String nameTestcase = "COMPOUND_aka_70462";

        CompoundTestCase testCase = TestCaseManager.getCompoundTestCaseByName(nameTestcase);
        executeACompoundTestcase(testCase, EXECUTION_MODE);

        assertTrue(new File(testCase.getExecutableFile()).exists());
        assertTrue(new File(testCase.getTestPathFile()).exists());
    }
}
