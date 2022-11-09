package testcase_execution;

import com.dse.testcase_manager.TestCase;
import com.dse.testcase_manager.TestCaseManager;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

public class ClassParametersTest extends AlgorithmProjectTest {
    private static final Logger logger = Logger.getLogger(ClassParametersTest.class);

    @Test
    // compare(Polygon,Polygon) + stub
    public void compare_aka_30134() throws Exception {
        init();
        loadEnvironment();
        String nameTestcase = "compare_aka_30134";

        TestCase testCase = TestCaseManager.getBasicTestCaseByName(nameTestcase);
        execute(testCase, EXECUTION_MODE);

        assertTrue(new File(testCase.getExecutableFile()).exists());
        assertTrue(new File(testCase.getTestPathFile()).exists());
    }

    @Test
    // compare(Polygon,Polygon)
    public void compare_aka_65068() throws Exception {
        init();
        loadEnvironment();
        String nameTestcase = "compare_aka_65068";

        TestCase testCase = TestCaseManager.getBasicTestCaseByName(nameTestcase);
        execute(testCase, EXECUTION_MODE);

        assertTrue(new File(testCase.getExecutableFile()).exists());
        assertTrue(new File(testCase.getTestPathFile()).exists());
    }

    @Test
    // compare(Polygon*,Polygon*,int,int)
    public void compare_aka_83652() throws Exception {
        init();
        loadEnvironment();
        String nameTestcase = "compare_aka_83652";

        TestCase testCase = TestCaseManager.getBasicTestCaseByName(nameTestcase);
        execute(testCase, EXECUTION_MODE);

        assertTrue(new File(testCase.getExecutableFile()).exists());
        assertTrue(new File(testCase.getTestPathFile()).exists());
    }

    @Test
    // compare(Polygon[3],Polygon[3])
    public void compare_aka_76885() throws Exception {
        init();
        loadEnvironment();
        String nameTestcase = "compare_aka_76885";

        TestCase testCase = TestCaseManager.getBasicTestCaseByName(nameTestcase);
        execute(testCase, EXECUTION_MODE);

        assertTrue(new File(testCase.getExecutableFile()).exists());
        assertTrue(new File(testCase.getTestPathFile()).exists());
    }

    @Test
    // comparev2(Polygon[],Polygon[],int,int)
    public void comparev2_aka_66700() throws Exception {
        init();
        loadEnvironment();
        String nameTestcase = "comparev2_aka_66700";

        TestCase testCase = TestCaseManager.getBasicTestCaseByName(nameTestcase);
        execute(testCase, EXECUTION_MODE);

        assertTrue(new File(testCase.getExecutableFile()).exists());
        assertTrue(new File(testCase.getTestPathFile()).exists());
    }
}
