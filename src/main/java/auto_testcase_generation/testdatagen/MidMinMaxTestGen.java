package auto_testcase_generation.testdatagen;


import com.dse.boundary.PrimitiveBound;
import com.dse.config.IFunctionConfigBound;
import com.dse.config.WorkspaceConfig;
import com.dse.guifx_v3.controllers.TestCasesNavigatorController;
import com.dse.guifx_v3.helps.CacheHelper;
import com.dse.guifx_v3.helps.UIController;
import com.dse.guifx_v3.helps.UILogger;
import com.dse.guifx_v3.objects.TestCasesTreeItem;
import com.dse.logger.AkaLogger;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.INode;
import com.dse.parser.object.IVariableNode;
import com.dse.parser.object.StructNode;
import com.dse.search.Search2;
import com.dse.testcase_execution.TestcaseExecution;
import com.dse.testcase_manager.TestCase;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.testdata.object.IDataNode;
import com.dse.testdata.object.RootDataNode;
import com.dse.thread.task.AutoGeneratedTestCaseExecTask;
import com.dse.util.VariableTypeUtils;
import javafx.application.Platform;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MidMinMaxTestGen extends AbstractAutomatedTestdataGeneration {
    private final static AkaLogger logger = AkaLogger.get(OldFullBoundedTestGen.class);
    private int maxIterationsforEachLoop;
    private List<IVariableNode> variables;
    private boolean shouldRunParallel = true;

    public MidMinMaxTestGen(ICommonFunctionNode fn, String coverageType) {
        super(fn);
        this.coverageType = coverageType;


        this.maxIterationsforEachLoop = 1;

        this.variables = fn.getArguments();

        TestCasesTreeItem treeItem = CacheHelper.getFunctionToTreeItemMap().get(fn);
        if (CacheHelper.getTreeItemToListTestCasesMap().get(treeItem) != null)
            CacheHelper.getTreeItemToListTestCasesMap().get(treeItem).clear();

        testCases = TestCaseManager.getTestCasesByFunction(this.fn);
    }

    public static void main(String[] args) throws Exception {
    }

    public void generateTestdata(ICommonFunctionNode fn) throws Exception {
        List<TestCase> oldTestcases = new ArrayList<>(testCases);

        for (int iteration = 0; iteration < maxIterationsforEachLoop; iteration++) {


            ExecutorService es = Executors.newFixedThreadPool(5);
            List<Callable<String>> tasks = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                testCaseGenerator task = new testCaseGenerator(i);
//                tasks.add(task);
                task.handleData();
            }
            es.invokeAll(tasks);
            logger.debug("Test case: " + this.testCases);
        }

        for (int i = testCases.size() - 1; i >= 0; i--)
            if (oldTestcases.contains(testCases.get(i))) {
                testCases.remove(i);
            }


        onGenerateSuccess(showReport);
    }

    public int getMaxIterationsforEachLoop() {
        return maxIterationsforEachLoop;
    }

    public void setMaxIterationsforEachLoop(int maxIterationsforEachLoop) {
        this.maxIterationsforEachLoop = maxIterationsforEachLoop;
    }


    protected synchronized TestCase createTestcase(int iteration, ICommonFunctionNode functionNode) {
        TestCase testCase = null;
        // create a new test case at each iteration
        String tail = "";
        if (iteration == 0) {
            tail = ".min";
        } else if (iteration == 1) {
            tail = ".max";
        } else {
            tail = ".mid";
        }
        String nameofTestcase = TestCaseManager.generateContinuousNameOfTestcase(functionNode.getSimpleName() + tail);

        testCase = TestCaseManager.createTestCase(nameofTestcase, functionNode);

        if (testCase != null) {
            TestCaseManager.getNameToBasicTestCaseMap().put(testCase.getName(), testCase);
            testCase.setCreationDateTime(LocalDateTime.now());

            String testpathFileName = nameofTestcase + "__iter__" + iteration; // to avoid the misunderstanding between system separator and its name
            String testpathFile = new WorkspaceConfig().fromJson().getTestpathDirectory() + File.separator + testpathFileName + ".tp";
            testCase.setTestPathFile(testpathFile);
        }
        return testCase;
    }

    protected void execute(TestCase testCase, List<AutoGeneratedTestCaseExecTask> execTasks) {

        try {
            TestcaseExecution executor = new TestcaseExecution();
            executor.setFunction(fn);
            executor.setMode(TestcaseExecution.IN_AUTOMATED_TESTDATA_GENERATION_MODE);
            AutoGeneratedTestCaseExecTask task = new AutoGeneratedTestCaseExecTask(executor, testCase, execTasks);
            task.setShowReport(showReport);
            UILogger.getUiLogger().info("Executing test case: " + testCase);
            task.run();
        } catch (Exception e) {
            e.printStackTrace();
            testCases.remove(testCase);
            testCase.setStatus(TestCase.STATUS_FAILED);
            logger.debug("[" + Thread.currentThread().getName() + "] " + "There is a problem with the current test case. Move to the next iteration.");
        }

    }

    private class testCaseGenerator implements Callable<String> {

        private static final int MIN_CODE = 0;
        private static final int MAX_CODE = 1;
        private static final int MID_CODE = 2;
        private final int iteration;

        public testCaseGenerator(int iteration) {
            this.iteration = iteration;
        }

        @Override
        public String call() throws Exception {
            handleData();
            return null;
        }

        private void handleData() throws Exception {

            List<RandomValue> randomValues = new ArrayList<RandomValue>();
            for (IVariableNode var : variables) {

                if (VariableTypeUtils.isStructureSimple(var.getRealType())) {
                    INode node = var.resolveCoreType();
                    if (node instanceof StructNode) {
                        StructNode typeNode = (StructNode) node;
                        ArrayList<IVariableNode> attributes = typeNode.getPublicAttributes();
                        for (IVariableNode attrVar : attributes) {
                            String originalName = var.getName() + "." + attrVar.getName();
                            String type = attrVar.getRealType();
                            IFunctionConfigBound b = fn.getFunctionConfig().getBoundOfOtherNumberVars();
                            genRandValue(originalName, type, attrVar, b, randomValues, true);
                        }
                    }
                } else {
                    String originalName = var.getName();
                    String type = var.getRealType();
                    IFunctionConfigBound b = fn.getFunctionConfig().getBoundOfArgumentsAndGlobalVariables().get(originalName);
                    genRandValue(originalName, type, null, b, randomValues, false);
                }

            }
            logger.debug("Rand: " + randomValues);

            TestCase testCase = createTestcase(this.iteration, fn);
            RootDataNode root = testCase.getRootDataNode();
            IDataNode sutRoot = Search2.findSubprogramUnderTest(root);
            recursiveExpandUutBranch(sutRoot, randomValues, testCase);
            //add testCase
            testCase.updateToTestCasesNavigatorTree();
            Platform.runLater(() -> TestCasesNavigatorController.getInstance().refreshNavigatorTree());
            testCases.add(testCase);
            //execute
            TestCasesNavigatorController.getInstance().refreshNavigatorTreeFromAnotherThread();
            execute(testCase, execTasks);
        }

        private void genRandValue(String originalName, String type, IVariableNode arguments, IFunctionConfigBound b, List<RandomValue> randomValues, boolean isStruct) {
            String min = "";
            String max = "";
            String midString = "";
            char[] minChar = new char[50];
            char[] maxChar = new char[50];
            char[] midChar = new char[50];
            //array
            IFunctionConfigBound tempBound = fn.getFunctionConfig().getBoundOfArray();
            int size = Integer.parseInt(((PrimitiveBound) tempBound).getUpper());
            String minList[] = new String[size];
            String maxList[] = new String[size];
            String midList[] = new String[size];

            if (VariableTypeUtils.isChOneDimension(type)) {
                if (isStruct) {
                    b = fn.getFunctionConfig().getBoundOfOtherCharacterVars();
                }

                int low = Integer.parseInt(((PrimitiveBound) b).getLower());
                int high = Integer.parseInt(((PrimitiveBound) b).getUpper());
                int avg = (low + high) / 2;
                Arrays.fill(minChar, (char) low);
                Arrays.fill(maxChar, (char) high);
                Arrays.fill(midChar, (char) avg);
            } else if (VariableTypeUtils.isNumBasic(type)) {
                if (isStruct) {
                    b = fn.getFunctionConfig().getBoundOfOtherNumberVars();
                }
                min = ((PrimitiveBound) b).getLower();
                max = ((PrimitiveBound) b).getUpper();
            } else if (VariableTypeUtils.isOneDimensionBasic(type)) {
                b = fn.getFunctionConfig().getBoundOfOtherNumberVars();
                Arrays.fill(minList, ((PrimitiveBound) b).getLower());
                Arrays.fill(maxList, ((PrimitiveBound) b).getUpper());
                for (int i = 0; i < midList.length; i++) {
                    midList[i] = Integer.toString((Integer.parseInt(((PrimitiveBound) b).getLower()) + Integer.parseInt(((PrimitiveBound) b).getUpper())) / 2);
                }
            } else if (VariableTypeUtils.isStructureSimple(type)) {
                INode node = arguments.resolveCoreType();
                if (node instanceof StructNode) {
                    StructNode typeNode = (StructNode) node;
                    ArrayList<IVariableNode> attributes = typeNode.getPublicAttributes();
                    for (IVariableNode attrVar : attributes) {
                        String originalName2 = originalName + "." + attrVar.getName();
                        String type2 = attrVar.getRealType();
                        IFunctionConfigBound b2 = fn.getFunctionConfig().getBoundOfOtherNumberVars();
                        genRandValue(originalName2, type2, attrVar, b2, randomValues, true);
                    }
                }
            }

            if (this.iteration == MIN_CODE) {
                if (VariableTypeUtils.isNumBasicFloat(type)) {
                    // TODO: so thuc
                    RandomValue rnd = new RandomValue(originalName, min);
                    randomValues.add(rnd);
                } else if (VariableTypeUtils.isNumBasic(type)) {
                    // TODO: so nguyen
                    RandomValue rnd = new RandomValue(originalName, min);
                    randomValues.add(rnd);
                } else if (VariableTypeUtils.isChOneDimension(type)) {
                    // TODO: ki tu
                    for (int i = 0; i < originalName.length(); i++) {
                        RandomValue rnd = new RandomValue(originalName, Character.toString(minChar[i]));
                        randomValues.add(rnd);
                    }
                } else if (VariableTypeUtils.isOneDimensionBasic(type)) {
                    //TODO mang
                    RandomValue rnd = new RandomValue(originalName, String.valueOf(minList.length));
                    randomValues.add(rnd);
                    for (int i = 0; i < minList.length; i++) {
                        rnd = new RandomValue(originalName + "[" + i + "]", minList[i]);
                        randomValues.add(rnd);
                    }
                }
            } else if (this.iteration == MAX_CODE) {
                if (VariableTypeUtils.isNumBasicFloat(type)) {
                    // TODO: so thuc
                    RandomValue rnd = new RandomValue(originalName, max);
                    randomValues.add(rnd);
                } else if (VariableTypeUtils.isNumBasic(type)) {
                    // TODO: so nguyen
                    RandomValue rnd = new RandomValue(originalName, max);
                    randomValues.add(rnd);
                } else if (VariableTypeUtils.isChOneDimension(type)) {
                    // TODO: ki tu
                    for (int i = 0; i < originalName.length(); i++) {
                        RandomValue rnd = new RandomValue(originalName, Character.toString(maxChar[i]));
                        randomValues.add(rnd);
                    }
                } else if (VariableTypeUtils.isOneDimensionBasic(type)) {
                    //TODO mang
                    RandomValue rnd = new RandomValue(originalName, String.valueOf(maxList.length));
                    randomValues.add(rnd);
                    for (int i = 0; i < maxList.length; i++) {
                        rnd = new RandomValue(originalName + "[" + i + "]", maxList[i]);
                        randomValues.add(rnd);
                    }
                }
            } else if (this.iteration == MID_CODE) {
                if (VariableTypeUtils.isNumBasicFloat(type)) {
                    // TODO: so thuc
                    double mid = (Double.parseDouble(max) + Double.parseDouble(min)) / 2;
                    RandomValue rnd = new RandomValue(originalName, String.valueOf(mid));
                    randomValues.add(rnd);
                } else if (VariableTypeUtils.isNumBasic(type)) {
                    // TODO: so nguyen
                    long mid = (Long.parseLong(max) + Long.parseLong(min)) / 2;
                    RandomValue rnd = new RandomValue(originalName, String.valueOf(mid));
                    randomValues.add(rnd);
                } else if (VariableTypeUtils.isChOneDimension(type)) {
                    // TODO: ki tu
                    for (int i = 0; i < originalName.length(); i++) {
                        RandomValue rnd = new RandomValue(originalName, Character.toString(midChar[i]));
                        randomValues.add(rnd);
                    }
                } else if (VariableTypeUtils.isOneDimensionBasic(type)) {
                    //TODO mang
                    RandomValue rnd = new RandomValue(originalName, String.valueOf(midList.length));
                    randomValues.add(rnd);
                    for (int i = 0; i < midList.length; i++) {
                        rnd = new RandomValue(originalName + "[" + i + "]", midList[i]);
                        randomValues.add(rnd);
                    }
                }
            }
        }
    }

}
