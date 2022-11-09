package auto_testcase_generation.testdatagen;

import com.dse.config.FunctionConfig;
import com.dse.config.WorkspaceConfig;
import com.dse.guifx_v3.controllers.TestCasesNavigatorController;
import com.dse.guifx_v3.controllers.object.LoadingPopupController;
import com.dse.guifx_v3.helps.*;
import com.dse.guifx_v3.objects.TestCasesTreeItem;
import com.dse.testcase_manager.TestPrototype;
import com.dse.testcase_manager.minimize.*;
import com.dse.parser.object.ConstructorNode;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.MacroFunctionNode;
import com.dse.testcase_execution.TestcaseExecution;
import com.dse.testcase_manager.TestCase;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.testdata.gen.module.SimpleTreeDisplayer;
import com.dse.testdata.object.RootDataNode;
import com.dse.thread.AkaThread;
import com.dse.thread.AkaThreadManager;
import com.dse.thread.task.AutoGeneratedTestCaseExecTask;
import com.dse.thread.task.GenerateTestdataTask;
import com.dse.logger.AkaLogger;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;

public class RandomAutomatedTestdataGeneration extends AbstractAutomatedTestdataGeneration {
    private final static AkaLogger logger = AkaLogger.get(RandomAutomatedTestdataGeneration.class);

    /**
     * To avoid too many iterations
     */
    private long limitNumberOfIterations = 100000;

    public RandomAutomatedTestdataGeneration(ICommonFunctionNode fn) {
        super(fn);
    }

    /**
     * Start generating test cases for a function
     *
     * @param fn a function
     * @throws Exception
     */
    public void generateTestdata(ICommonFunctionNode fn) throws Exception {
        logger.debug("Generating test data for function " + fn.getName());
        logger.debug("Automated test data generation strategy: random");

        /**
         * Initialize function config if the function does not have any.
         */
        if (fn.getFunctionConfig() == null) {
            FunctionConfig functionConfig = new WorkspaceConfig().fromJson().getDefaultFunctionConfig();
            functionConfig.setFunctionNode(fn);
            fn.setFunctionConfig(functionConfig);
            functionConfig.createBoundOfArgument(functionConfig, fn);
        }

        if (fn.getFunctionConfig().getFunctionNode() == null)
            fn.getFunctionConfig().setFunctionNode(fn);

        long MAX_ITERATON = fn.getFunctionConfig().getTheMaximumNumberOfIterations();

        // to avoid too many iterations
        MAX_ITERATON = Math.min(MAX_ITERATON, limitNumberOfIterations);

        logger.debug("Maximum number of iterations = " + MAX_ITERATON);

        // clear cache before generate testcase
        TestCasesTreeItem treeItem = CacheHelper.getFunctionToTreeItemMap().get(fn);
        if (CacheHelper.getTreeItemToListTestCasesMap().get(treeItem) != null)
            CacheHelper.getTreeItemToListTestCasesMap().get(treeItem).clear();

        if (fn.isTemplate() || fn instanceof MacroFunctionNode || fn.hasVoidPointerArgument() || fn.hasFunctionPointerArgument()) {
            // Get all prototypes if the tested function has
            if (this.allPrototypes == null || this.allPrototypes.size() == 0)
                this.allPrototypes = getAllPrototypesOfTemplateFunction(fn);
            if (this.allPrototypes.size() == 0)
                return;

            for (TestPrototype prototype : allPrototypes)
                start(MAX_ITERATON, prototype, this.fn, this.execTasks, this.generatedTestcases,
                        this.functionExecThread, this.testCases, this.showReport, this.shouldRunParallel);
        } else {
            // The tested function is a normal function.
            start(MAX_ITERATON, null, this.fn, this.execTasks, this.generatedTestcases,
                    this.functionExecThread, this.testCases, this.showReport, this.shouldRunParallel);
        }

        // view coverage of generated set of test cases
        if (!shouldRunParallel) {
            onGenerateSuccess(showReport);
            LoadingPopupController.getInstance().close();
        }
    }

    /**
     *
     * @param MAX_ITERATON the maximum number of iterations
     * @param selectedPrototype the selected prototype if it exists
     * @param fn the tested function
     * @param execTasks
     * @param generatedTestcases the generated test case (to avoid generating executing duplicated test cases)
     * @param functionExecThread
     * @param outputs
     * @param showReport show report after test case generation
     */
    protected void start(final long MAX_ITERATON, TestPrototype selectedPrototype, ICommonFunctionNode fn,
                         List<AutoGeneratedTestCaseExecTask> execTasks,
                         List<String> generatedTestcases,
                         GenerateTestdataTask functionExecThread,
                         List<TestCase> outputs,
                         boolean showReport,
                         boolean shouldRunParallel) {
        for (int iteration = 0; iteration < MAX_ITERATON; iteration++) {
            logger.debug("Iteration " + (iteration + 1) + "/" + MAX_ITERATON);

            TestCase testCase;
            try {
                testCase = generateTestdata(iteration, fn, selectedPrototype, generatedTestcases);
                if (testCase != null)
                    outputs.add(testCase);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        TestCasesNavigatorController.getInstance().refreshNavigatorTreeFromAnotherThread();
        execute(outputs, showReport, execTasks, shouldRunParallel, functionExecThread);
        logger.debug("Done automated test data generation");

        // optimize the number of test cases
        ITestCaseMinimizer minimizer = new GreedyMinimizer();
        minimizer.clean(testCases, Scope.SOURCE);
    }

    protected TestCase generateTestdata(int iteration, ICommonFunctionNode fn, TestPrototype selectedPrototype,
                                        List<String> generatedTestcases) throws Exception {
        TestCase testCase = createTestcase(selectedPrototype, iteration, fn);
        if (testCase == null)
            return null;
        RootDataNode root = testCase.getRootDataNode();
        List<String> additionalHeaders = new ArrayList<>();
        String newTestCaseInStr = "";

        // generate random value for parameters
        ICommonFunctionNode sut = testCase.getFunctionNode();
        if (!(sut instanceof ConstructorNode)) {
            logger.debug("Generate random value for new test case");
            List<RandomValue> randomValuesForArguments = generateRandomValueForArguments(root, sut, additionalHeaders, selectedPrototype, testCase);
            if (randomValuesForArguments != null)
                newTestCaseInStr += randomValuesForArguments.toString();
            logger.debug("randomValuesForArguments: " + randomValuesForArguments);
        }

        // generate random value for instance
        List<RandomValue> randomValuesForInstance = generateRandomValuesForInstance(root, sut, testCase);
        if (randomValuesForInstance != null)
            newTestCaseInStr += randomValuesForInstance.toString();
        logger.debug("randomValuesForInstance = " + randomValuesForInstance);

        // generate random value for global variables
        List<RandomValue> randomValuesForGlobalVariables = generateRandomValuesForGlobal(root, sut, testCase);
        if (randomValuesForGlobalVariables.size() != 0)
            newTestCaseInStr += randomValuesForGlobalVariables.toString();
        logger.debug("randomValuesForGlobalVariables = " + randomValuesForGlobalVariables);

        if (generatedTestcases.contains(newTestCaseInStr)) {
            logger.debug("Duplicate test cases. Ignore.");
            if (testCase != null)
                testCase.deleteOldData();
            return null;
        }

        List<RandomValue> randomValuesForStubs = generateRandomValuesForStub(root, sut, testCase);
        if (randomValuesForStubs != null)
            newTestCaseInStr += randomValuesForStubs.toString();
        logger.debug("randomValuesForStubs = " + randomValuesForStubs);

        generatedTestcases.add(newTestCaseInStr);
        try {
            logger.debug(new SimpleTreeDisplayer().toString(root));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // add to navigator tree
        testCase.updateToTestCasesNavigatorTree();
        Platform.runLater(() -> TestCasesNavigatorController.getInstance().refreshNavigatorTree());

        //
        String additionalHeadersAll = "";
        for (String item : additionalHeaders)
            additionalHeadersAll += item;
        testCase.setAdditionalHeaders(additionalHeadersAll);

        return testCase;
    }

    public long getLimitNumberOfIterations() {
        return limitNumberOfIterations;
    }

    public void setLimitNumberOfIterations(long limitNumberOfIterations) {
        this.limitNumberOfIterations = limitNumberOfIterations;
    }

    public static AkaLogger getLogger() {
        return logger;
    }
}
