package testcase_execution;

import com.dse.environment.PhysicalTreeImporter;
import com.dse.parser.object.Node;
import com.dse.testcase_execution.CompoundTestcaseExecution;
import com.dse.testcase_execution.ITestcaseExecution;
import com.dse.testcase_execution.TestcaseExecution;
import com.dse.config.AkaConfig;
import com.dse.config.WorkspaceConfig;
import com.dse.environment.EnvironmentAnalyzer;
import com.dse.environment.WorkspaceLoader;
import com.dse.environment.object.EnvironmentRootNode;
import com.dse.environment.Environment;
import com.dse.parser.object.FunctionNode;
import com.dse.parser.object.INode;
import com.dse.parser.object.ProjectNode;
import com.dse.testcase_manager.CompoundTestCase;
import com.dse.testcase_manager.TestCase;
import com.dse.util.Utils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public abstract class AbstractTestCaseExecutionTest {
    private static final Logger logger = Logger.getLogger(AbstractTestCaseExecutionTest.class);
    public static final int EXECUTION_MODE = ITestcaseExecution.IN_EXECUTION_WITH_FRAMEWORK_TESTING_MODE;

    public static String PREV_ENV_PATH = "/mnt/e/LAB/akautauto/local/working-directory/TEST_ENV";
    public static String PREV_PRJ_PATH = "/mnt/e/LAB/akautauto/datatest/lamnt/data_entering/data_type";
    public static String NEW_ENV_PATH = "/Users/ducanhnguyen/Documents/akautauto/local/working-directory/TEST_ENV";
    public static String NEW_PRJ_PATH = "/Users/ducanhnguyen/Documents/akautauto/datatest/lamnt/data_entering/data_type";

    public static String WORKING_DIRECTORY = "local/working-directory";
    public static String ENVIRONMENT_PATH = "local/working-directory/TEST_ENV.env";
    public static String TEST_ENVIR_PATH = "local/working-directory/TEST_ENV.tst";
    public static String TEST_CASES_DIRECTORY = "local/working-directory/TEST_ENV/test-cases";
    public static String PHYSICAL_TREE_PATH = "local/working-directory/TEST_ENV/physical_tree.json";
    public static String ENVIRONMENT_DIRECTORY = "local/working-directory/TEST_ENV";

    /**
     * Init the value of attributes
     */
    protected abstract void init();

    /**
     * Replace the path
     */
    protected void refactor() {
        new AkaConfig().fromJson().setOpeningWorkspaceDirectory(NEW_ENV_PATH).addOpeningWorkspaces(NEW_ENV_PATH).exportToJson();
        new AkaConfig().fromJson().setOpenWorkspaceConfig(NEW_ENV_PATH + File.separator + WorkspaceConfig.WORKSPACE_CONFIG_NAME).exportToJson();
        refactor(ENVIRONMENT_PATH, PREV_ENV_PATH, NEW_ENV_PATH, PREV_PRJ_PATH, NEW_PRJ_PATH);
        refactor(TEST_ENVIR_PATH, PREV_ENV_PATH, NEW_ENV_PATH, PREV_PRJ_PATH, NEW_PRJ_PATH);

        recursiveRefactor(new File(ENVIRONMENT_DIRECTORY), PREV_ENV_PATH, NEW_ENV_PATH, PREV_PRJ_PATH, NEW_PRJ_PATH);
    }

    private void recursiveRefactor(File node, String prevEnvirRoot, String newEnvirRoot, String prevProjectRoot, String newProjectRoot) {
        for (File f : node.listFiles()) {
            if (f.isDirectory()) {
                recursiveRefactor(f, prevEnvirRoot, newEnvirRoot, prevProjectRoot, newProjectRoot);
            } else {
                String content = Utils.readFileContent(f);
                content = content.replace(prevEnvirRoot, newEnvirRoot).replace(prevProjectRoot, newProjectRoot);
                Utils.writeContentToFile(content, f.getAbsolutePath());
            }
        }
    }

    private void refactor(String path, String prevEnvirRoot, String newEnvirRoot, String prevProjectRoot, String newProjectRoot) {
        String content = Utils.readFileContent(path);
        content = content.replace(prevEnvirRoot, newEnvirRoot).replace(prevProjectRoot, newProjectRoot);
        Utils.writeContentToFile(content, path);
    }

    protected void loadEnvironment() {
        refactor();

        WorkspaceLoader.logger.setLevel(Level.OFF);

        String physicalTreePath = new WorkspaceConfig().fromJson().getPhysicalJsonFile();
        File physicalTreeFile = new File(physicalTreePath);

        INode root = new PhysicalTreeImporter().importTree(physicalTreeFile);
        Environment.getInstance().setProjectNode((ProjectNode) root);
        logger.debug("Load project physical tree successful");

        WorkspaceLoader loader = new WorkspaceLoader((Node) root);
        loader.setPhysicalTreePath(new File(PHYSICAL_TREE_PATH));
//        loader.setWorkspace(new File(WORKING_DIRECTORY));
//        loader.load(loader.getPhysicalTreePath());
        root = loader.getRoot();
        assertTrue(root instanceof ProjectNode);

        EnvironmentAnalyzer environmentAnalyzer = new EnvironmentAnalyzer();
        environmentAnalyzer.analyze(new File(ENVIRONMENT_PATH));
        Environment.getInstance().setEnvironmentRootNode((EnvironmentRootNode) environmentAnalyzer.getRoot());

        Environment.getInstance().loadTestCasesScript(new File(TEST_ENVIR_PATH));

        Environment.getInstance().setProjectNode((ProjectNode) root);
    }

    /**
     * Execute a test case
     *
     * @param testCase
     * @throws Exception
     */
    protected void executeACompoundTestcase(CompoundTestCase testCase, int mode) throws Exception {
        testCase.deleteOldDataExceptValue();

        CompoundTestcaseExecution executor = new CompoundTestcaseExecution();
        executor.setTestCase(testCase);
        executor.setMode(ITestcaseExecution.IN_EXECUTION_WITH_FRAMEWORK_TESTING_MODE);
        executor.execute();
    }

    /**
     * Execute a single test case
     *
     * @param testCase
     * @throws Exception
     */
    protected void execute(TestCase testCase, int mode) throws Exception {
        testCase.deleteOldDataExceptValue();

        FunctionNode functionNode = (FunctionNode) testCase.getRootDataNode().getFunctionNode();
        assertNotNull(functionNode);

        TestcaseExecution executor = new TestcaseExecution();
        executor.setMode(mode);
        executor.setFunction(functionNode);
//        executor.setCfg(CFGUtils.createCFG(functionNode));
        executor.setTestCase(testCase);
        executor.setMode(mode);
        executor.execute();
    }
//    @Test
//    //@Ignore
//    public void testAll() throws Exception {
//        init();
//        loadEnvironment();
//
//        // test single test case
//        for (File testcaseFile : new File(TEST_CASES_DIRECTORY).listFiles()) {
//            if (testcaseFile.getAbsolutePath().endsWith(".json")) { // just check to ensure the format of test case
//                String nameTestcase = testcaseFile.getName().replace(".json", "");
//                TestCase testCase = TestCaseManager.getBasicTestCaseByName(nameTestcase);
//                execute(testCase, ITestcaseExecution.IN_TESTDATA_EXECUTION_WITHOUT_GOOGLETEST_MODE);
//
//                assertTrue(new File(testCase.getExecutableFile()).exists());
//                assertTrue(new File(testCase.getTestPathFile()).exists());
//            }
//        }
//
//        // test compound test cases
//        for (File testcaseFile : new File(TEST_CASES_DIRECTORY + File.separator + "compounds").listFiles()) {
//            if (testcaseFile.getAbsolutePath().endsWith(".json")) { // just check to ensure the format of test case
//                String nameTestcase = testcaseFile.getName().replace(".json", "");
//                TestCase testCase = TestCaseManager.getBasicTestCaseByName(nameTestcase);
//                execute(testCase, ITestcaseExecution.IN_TESTDATA_EXECUTION_WITHOUT_GOOGLETEST_MODE);
//
//                assertTrue(new File(testCase.getExecutableFile()).exists());
//                assertTrue(new File(testCase.getTestPathFile()).exists());
//            }
//        }
//    }
}
