package testcase_execution;

import com.dse.testcase_manager.TestCase;
import com.dse.testcase_manager.TestCaseManager;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

public class StructParametersTest extends AlgorithmProjectTest {
    private static final Logger logger = Logger.getLogger(StructParametersTest.class);

    @Test
    // compareGroup(Person[3],Person[3])
    public void compareGroup_aka_35777() throws Exception {
        init();
        loadEnvironment();
        String nameTestcase = "compareGroup_aka_35777";

        TestCase testCase = TestCaseManager.getBasicTestCaseByName(nameTestcase);
        execute(testCase, EXECUTION_MODE);

        assertTrue(new File(testCase.getExecutableFile()).exists());
        assertTrue(new File(testCase.getTestPathFile()).exists());
    }

    @Test
    // compareGroup(Person*,Person*,int,int)
    public void compareGroup_aka_45967() throws Exception {
        init();
        loadEnvironment();
        String nameTestcase = "compareGroup_aka_45967";

        TestCase testCase = TestCaseManager.getBasicTestCaseByName(nameTestcase);
        execute(testCase, EXECUTION_MODE);

        assertTrue(new File(testCase.getExecutableFile()).exists());
        assertTrue(new File(testCase.getTestPathFile()).exists());
    }

    @Test
    // compareGroup(Person*,Person*,int,int)
    public void compareGroup_aka_74197() throws Exception {
        init();
        loadEnvironment();
        String nameTestcase = "compareGroup_aka_74197";

        TestCase testCase = TestCaseManager.getBasicTestCaseByName(nameTestcase);
        execute(testCase, EXECUTION_MODE);

        assertTrue(new File(testCase.getExecutableFile()).exists());
        assertTrue(new File(testCase.getTestPathFile()).exists());
    }

    @Test
    // compareGroupv2(Person[],Person[],int,int)
    public void compareGroupv2_aka_92178() throws Exception {
        init();
        loadEnvironment();
        String nameTestcase = "compareGroupv2_aka_92178";

        TestCase testCase = TestCaseManager.getBasicTestCaseByName(nameTestcase);
        execute(testCase, EXECUTION_MODE);

        assertTrue(new File(testCase.getExecutableFile()).exists());
        assertTrue(new File(testCase.getTestPathFile()).exists());
    }

    @Test
    // compareTwoPeople(Person,Person)
    public void compareTwoPeople_aka_65114() throws Exception {
        init();
        loadEnvironment();
        String nameTestcase = "compareTwoPeople_aka_65114";

        TestCase testCase = TestCaseManager.getBasicTestCaseByName(nameTestcase);
        execute(testCase, EXECUTION_MODE);

        assertTrue(new File(testCase.getExecutableFile()).exists());
        assertTrue(new File(testCase.getTestPathFile()).exists());
    }

    @Test
    // printList(struct Node*)
    public void printList_aka_10495() throws Exception {
        init();
        loadEnvironment();
        String nameTestcase = "printList_aka_10495";

        TestCase testCase = TestCaseManager.getBasicTestCaseByName(nameTestcase);
        execute(testCase, EXECUTION_MODE);

        assertTrue(new File(testCase.getExecutableFile()).exists());
        assertTrue(new File(testCase.getTestPathFile()).exists());
    }

    @Test
    // partition(struct Node*,struct Node*,struct Node**,struct Node**)
    public void partition_aka_86895() throws Exception {
        init();
        loadEnvironment();
        String nameTestcase = "partition_aka_86895";

        TestCase testCase = TestCaseManager.getBasicTestCaseByName(nameTestcase);
        execute(testCase, EXECUTION_MODE);

        assertTrue(new File(testCase.getExecutableFile()).exists());
        assertTrue(new File(testCase.getTestPathFile()).exists());
    }
}
