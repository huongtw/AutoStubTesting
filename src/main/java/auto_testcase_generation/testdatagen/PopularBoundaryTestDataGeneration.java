package auto_testcase_generation.testdatagen;

import com.dse.boundary.DataSizeModel;
import com.dse.boundary.MultiplePrimitiveBound;
import com.dse.boundary.PrimitiveBound;
import com.dse.config.FunctionConfig;
import com.dse.config.IFunctionConfig;
import com.dse.config.IFunctionConfigBound;
import com.dse.config.WorkspaceConfig;
import com.dse.environment.Environment;
import com.dse.guifx_v3.controllers.TestCasesNavigatorController;
import com.dse.guifx_v3.helps.CacheHelper;
import com.dse.guifx_v3.helps.UIController;
import com.dse.guifx_v3.objects.TestCasesTreeItem;
import com.dse.logger.AkaLogger;
import com.dse.parser.externalvariable.RelatedExternalVariableDetecter;
import com.dse.parser.object.ConstructorNode;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.IFunctionNode;
import com.dse.parser.object.IVariableNode;
import com.dse.search.Search2;
import com.dse.testcase_execution.TestCaseExecutionThread;
import com.dse.testcase_execution.TestcaseExecution;
import com.dse.testcase_manager.ITestCase;
import com.dse.testcase_manager.TestCase;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.testdata.object.*;
import com.dse.util.SourceConstant;
import com.dse.util.VariableTypeUtils;
import javafx.application.Platform;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PopularBoundaryTestDataGeneration extends AbstractAutomatedTestdataGeneration {
    private final static AkaLogger logger = AkaLogger.get(PopularBoundaryTestDataGeneration.class);

    private String type = IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.NORMAL_BOUND;
    private final Map<String, Parameter> parameterMap = new HashMap<>();
    private Map<String, IFunctionConfigBound> boundMap = new HashMap<>();

    public PopularBoundaryTestDataGeneration(ICommonFunctionNode fn, String coverageType) {
        super(fn);
        this.coverageType = coverageType;
    }

    @Override
    public void generateTestdata(ICommonFunctionNode fn) throws Exception {
        // clear cache before generate testcase
        TestCasesTreeItem treeItem = CacheHelper.getFunctionToTreeItemMap().get(fn);
        if (CacheHelper.getTreeItemToListTestCasesMap().get(treeItem) != null)
            CacheHelper.getTreeItemToListTestCasesMap().get(treeItem).clear();

        logger.debug("Generating test data with strategy: " + type);
        FunctionConfig config = (FunctionConfig) fn.getFunctionConfig();
        boundMap = config.getBoundOfArgumentsAndGlobalVariables();
        buildParameterMap(); // depend on type
        generateTestDataFromParameterMap();

        // execute all generated test cases
        setShouldRunParallel(true);

        List<TestCaseExecutionThread> tasks = new ArrayList<>();
        for (TestCase testCase : this.testCases) {
            TestCaseExecutionThread executionThread = new TestCaseExecutionThread(testCase);
            executionThread.setExecutionMode(TestcaseExecution.IN_AUTOMATED_TESTDATA_GENERATION_MODE);
            tasks.add(executionThread);
        }
        logger.debug("Create " + tasks.size() + " threads to execute " + tasks.size() + " test cases");

        // add these threads to executors
        // at the same time, we do not execute all of the requested test cases.
        int MAX_EXECUTING_TESTCASE = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(MAX_EXECUTING_TESTCASE);
        for (TestCaseExecutionThread task : tasks)
            executorService.submit(task);
        executorService.shutdown();

        try {
            executorService.awaitTermination(10, TimeUnit.MINUTES);
            UIController.viewCoverageOfMultipleTestcases(fn.getName(), testCases);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (Parameter param : parameterMap.values()) {
            logger.debug(param);
        }
    }

    private void buildParameterMap() {
        List<IVariableNode> variables = new ArrayList<>(fn.getArguments()); // get passed in args
        RelatedExternalVariableDetecter detector = new RelatedExternalVariableDetecter(
                (IFunctionNode) fn);
        variables.addAll(detector.findVariables()); // add globals variables
        for (IVariableNode variable : variables) {
            String name = variable.getName();
            Parameter parameter = new Parameter(name);
            IFunctionConfigBound bound = boundMap.get(name);
            // build parameter
            parameter.setNorm(chooseNormValue(variable, bound));
            // getBoundValues() is depend on type of data (long, char, float, double, v.v.)
            parameter.getBounds().addAll(getBoundaryValues(variable, bound));
            parameterMap.put(name, parameter);
        }
    }

    private boolean validateCharOrNumBoundaryValue(String value, PrimitiveBound validRange) {
        try {
            double doubleValue = Double.parseDouble(value);
            if (validRange != null) {
                return doubleValue <= validRange.getUpperAsDouble()
                        && doubleValue >= validRange.getLowerAsDouble();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * get bound values (bound and marginal) values for a argument
     *
     * @param bound bound of argument
     * @return list of bounds
     */
    public List<String> getBoundaryValues(IVariableNode argument, IFunctionConfigBound bound) {
        String argType = argument.getRealType();
//                String argType = argument.getRawType().replaceAll("\\b" + argument.getCoreType() +"\\b", realType);
        argType = VariableTypeUtils.deleteStorageClasses(argType);
        argType = VariableTypeUtils.deleteReferenceOperator(argType);

        // get data size for validating the value
        DataSizeModel dataSizeModel = Environment.getBoundOfDataTypes().getBounds();
        PrimitiveBound dataSize = dataSizeModel.get(argument.getReducedRawType());

        // Boolean
        if (VariableTypeUtils.isBoolBasic(argType)) {
            logger.debug(argType + ": isBoolBasic");
            return getBooleanBoundValues(bound);
        }
        // Character
        else if (VariableTypeUtils.isChBasic(argType)) {
            logger.debug(argType + ": isChBasic");
            return getCharacterBoundValues(bound, dataSize);
        }
        // Number
        else if (VariableTypeUtils.isNumBasic(argType)) {
            logger.debug(argType + ": isNumBasic");
            return getNumberBoundValues(argType, bound, dataSize);
        }

        return new ArrayList<>();
    }

    public List<String> getLongBoundValues(IFunctionConfigBound bound, PrimitiveBound dataSize) {
        List<String> values = new ArrayList<>();
        if (bound instanceof MultiplePrimitiveBound) {
            for (PrimitiveBound primitiveBound : ((MultiplePrimitiveBound) bound)) {
                long lower = Long.parseLong(primitiveBound.getLower());
                long upper = Long.parseLong(primitiveBound.getUpper());
                switch (type) {
                    case IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.ROBUSTNESS: {
                        long outLower = lower - 1;
                        long outUpper = upper + 1;
                        long inLower = lower + 1;
                        long inUpper = upper - 1;

                        if (validateCharOrNumBoundaryValue(String.valueOf(outLower), dataSize)) {
                            values.add(String.valueOf(outLower));
                        } else {
                            logger.debug("Generated invalid values due to out of the data size");
                        }

                        values.add(primitiveBound.getLower());
                        values.add(String.valueOf(inLower));
                        values.add(String.valueOf(inUpper));
                        values.add(primitiveBound.getUpper());

                        if (validateCharOrNumBoundaryValue(String.valueOf(outUpper), dataSize)) {
                            values.add(String.valueOf(outUpper));
                        } else {
                            logger.debug("Generated invalid values due to out of the data size");
                        }
                        break;
                    }

                    case IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.BVA: {
                        long inLower = lower + 1;
                        long inUpper = upper - 1;
                        values.add(primitiveBound.getLower());
                        values.add(String.valueOf(inLower));
                        values.add(String.valueOf(inUpper));
                        values.add(primitiveBound.getUpper());
                        break;
                    }

                    case IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.BVA_BOUNDARYCONDITION: {
                        long inLower = lower + 1;
                        long inUpper = upper - 1;
                        values.add(primitiveBound.getLower());
                        values.add(String.valueOf(inLower));
                        values.add(String.valueOf(inUpper));
                        values.add(primitiveBound.getUpper());
                        break;
                    }

                    case IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.NORMAL_BOUND: {
                        values.add(primitiveBound.getLower());
                        values.add(primitiveBound.getUpper());
                        break;
                    }

                    default:
                        logger.error("Invalid test data generation strategy. Strategy: " + type);
                }
            }
        }
        return values;
    }

    public List<String> getDoubleBoundValues(IFunctionConfigBound bound, PrimitiveBound dataSize) {
        List<String> values = new ArrayList<>();
        final double delta = new WorkspaceConfig().fromJson()
                .getDefaultFunctionConfig().getFloatAndDoubleDelta();
        logger.debug("Delta: " + delta);
        if (bound instanceof MultiplePrimitiveBound) {
            for (PrimitiveBound primitiveBound : ((MultiplePrimitiveBound) bound)) {
                double lower = Double.parseDouble(primitiveBound.getLower() + "f");
                double upper = Double.parseDouble(primitiveBound.getUpper() + "f");
                switch (type) {
                    case IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.ROBUSTNESS: {
                        double outLower = lower - delta;
                        double outUpper = upper + delta;
                        double inLower = lower + delta;
                        double inUpper = upper - delta;

                        if (validateCharOrNumBoundaryValue(String.valueOf(outLower), dataSize)) {
                            values.add(String.valueOf(outLower));
                        } else {
                            logger.debug("Generated invalid values due to out of the data size");
                        }

                        values.add(primitiveBound.getLower());
                        values.add(String.valueOf(inLower));
                        values.add(String.valueOf(inUpper));
                        values.add(primitiveBound.getUpper());

                        if (validateCharOrNumBoundaryValue(String.valueOf(outUpper), dataSize)) {
                            values.add(String.valueOf(outUpper));
                        } else {
                            logger.debug("Generated invalid values due to out of the data size");
                        }
                        break;
                    }

                    case IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.BVA: {
                        double inLower = lower + delta;
                        double inUpper = upper - delta;
                        values.add(primitiveBound.getLower());
                        values.add(String.valueOf(inLower));
                        values.add(String.valueOf(inUpper));
                        values.add(primitiveBound.getUpper());
                        break;
                    }

                    case IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.BVA_BOUNDARYCONDITION: {
                        double inLower = lower + delta;
                        double inUpper = upper - delta;
                        values.add(primitiveBound.getLower());
                        values.add(String.valueOf(inLower));
                        values.add(String.valueOf(inUpper));
                        values.add(primitiveBound.getUpper());
                        break;
                    }

                    case IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.NORMAL_BOUND: {
                        values.add(primitiveBound.getLower());
                        values.add(primitiveBound.getUpper());
                        break;
                    }

                    default:
                        logger.error("Invalid test data generation strategy. Strategy: " + type);
                }
            }
        }
        return values;
    }

    public List<String> getNumberBoundValues(String argType, IFunctionConfigBound bound, PrimitiveBound dataSize) {
        if (VariableTypeUtils.isNumBasicInteger(argType)
                || VariableTypeUtils.isStdInt(argType)) {
            logger.debug(argType + ": get long boundary values");
            return getLongBoundValues(bound, dataSize);
        } else if (VariableTypeUtils.isNumBasicFloat(argType)) {
            logger.debug(argType + ": get double boundary values");
            return getDoubleBoundValues(bound, dataSize);
        }

        return new ArrayList<>();
    }

    public List<String> getBooleanBoundValues(IFunctionConfigBound bound) {
        // todo: handle boolean values
        return new ArrayList<>();
    }

    public List<String> getCharacterBoundValues(IFunctionConfigBound bound, PrimitiveBound dataSize) {
        List<String> values = new ArrayList<>();
        if (bound instanceof MultiplePrimitiveBound) {
            for (PrimitiveBound primitiveBound : ((MultiplePrimitiveBound) bound)) {
                long lower = Long.parseLong(primitiveBound.getLower());
                long upper = Long.parseLong(primitiveBound.getUpper());
                switch (type) {
                    case IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.ROBUSTNESS: {
                        long outLower = lower - 1;
                        long outUpper = upper + 1;
                        long inLower = lower + 1;
                        long inUpper = upper - 1;

                        if (validateCharOrNumBoundaryValue(String.valueOf(outLower), dataSize)) {
                            values.add(String.valueOf(outLower));
                        } else {
                            logger.debug("Generated invalid values due to out of the data size");
                        }

                        values.add(primitiveBound.getLower());
                        values.add(String.valueOf(inLower));
                        values.add(String.valueOf(inUpper));
                        values.add(primitiveBound.getUpper());

                        if (validateCharOrNumBoundaryValue(String.valueOf(outUpper), dataSize)) {
                            values.add(String.valueOf(outUpper));
                        } else {
                            logger.debug("Generated invalid values due to out of the data size");
                        }
                        break;
                    }

                    case IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.BVA: {
                        long inLower = lower + 1;
                        long inUpper = upper - 1;
                        values.add(primitiveBound.getLower());
                        values.add(String.valueOf(inLower));
                        values.add(String.valueOf(inUpper));
                        values.add(primitiveBound.getUpper());
                        break;
                    }

                    case IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.BVA_BOUNDARYCONDITION: {
                        long inLower = lower + 1;
                        long inUpper = upper - 1;
                        values.add(primitiveBound.getLower());
                        values.add(String.valueOf(inLower));
                        values.add(String.valueOf(inUpper));
                        values.add(primitiveBound.getUpper());
                        break;
                    }

                    case IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.NORMAL_BOUND: {
                        values.add(primitiveBound.getLower());
                        values.add(primitiveBound.getUpper());
                        break;
                    }

                    default:
                        logger.error("Invalid test data generation strategy. Strategy: " + type);
                }
            }
        }
        return values;
    }

    public String chooseNormValue(IVariableNode argument, IFunctionConfigBound bound) {
        String norm = "NA";

        String argType = argument.getRealType();
//                String argType = argument.getRawType().replaceAll("\\b" + argument.getCoreType() +"\\b", realType);
        argType = VariableTypeUtils.deleteStorageClasses(argType);
        argType = VariableTypeUtils.deleteReferenceOperator(argType);

        // Boolean
        // todo: boolean type is not supported
        if (VariableTypeUtils.isBoolBasic(argType)) {
            logger.debug(argType + ": isBoolBasic");
            return chooseBooleanNormValue(bound);
        }
        // Character
        else if (VariableTypeUtils.isChBasic(argType)) {
            logger.debug(argType + ": isChBasic");
            return chooseCharacterNormValue(bound);
        }
        // Number
        else if (VariableTypeUtils.isNumBasic(argType)) {
            logger.debug(argType + ": isNumBasic");
            return chooseNumberNormValue(argType, bound);
        }

        return norm;
    }

    public String chooseNumberNormValue(String argType, IFunctionConfigBound bound) {
        if (VariableTypeUtils.isNumBasicInteger(argType)) {
            logger.debug(argType + ": isNumBasicInteger");
            return chooseLongNormValue(bound);
        } else if (VariableTypeUtils.isNumBasicFloat(argType)) {
            logger.debug(argType + ": isNumBasicFloat");
            return chooseDoubleNormValue(bound);
        } else if (VariableTypeUtils.isStdInt(argType)) {
            return chooseLongNormValue(bound);
        }

        return "NA";
    }

    public String chooseLongNormValue(IFunctionConfigBound bound) {
        // todo: check if norm equals "NA"
        String norm = "NA";

        if (bound instanceof MultiplePrimitiveBound) {
            PrimitiveBound primitiveBound = ((MultiplePrimitiveBound) bound).get(0);
            long lower = Long.parseLong(primitiveBound.getLower());
            long upper = Long.parseLong(primitiveBound.getUpper());
            long normValue = (lower + upper) / 2;
            norm = String.valueOf(normValue);
        }

        return norm;
    }

    public String chooseDoubleNormValue(IFunctionConfigBound bound) {
        // todo: check if norm equals "NA"
        String norm = "NA";

        if (bound instanceof MultiplePrimitiveBound) {
            PrimitiveBound primitiveBound = ((MultiplePrimitiveBound) bound).get(0);
            double lower = Double.parseDouble(primitiveBound.getLower() + "f");
            double upper = Double.parseDouble(primitiveBound.getUpper() + "f");
            double normValue = (lower + upper) / 2;
            norm = String.valueOf(normValue);
        }

        return norm;
    }

    public String chooseBooleanNormValue(IFunctionConfigBound bound) {

        return "NA";
    }

    public String chooseCharacterNormValue(IFunctionConfigBound bound) {
        // todo: check if norm equals "NA"
        String norm = "NA";

        if (bound instanceof MultiplePrimitiveBound) {
            PrimitiveBound primitiveBound = ((MultiplePrimitiveBound) bound).get(0);
            long lower = Long.parseLong(primitiveBound.getLower());
            long upper = Long.parseLong(primitiveBound.getUpper());
            long normValue = (lower + upper) / 2;
            norm = String.valueOf(normValue);
        }

        return norm;

    }

    /**
     * generate test cases and set data with binds of boundary, norm values.
     *
     * @throws Exception exception when create test case
     */
    private void generateTestDataFromParameterMap() throws Exception {
        // todo: check if there is any duplication in value of bounds

        for (String name : parameterMap.keySet()) { // for each argument or global variable
            Parameter parameter = parameterMap.get(name);
            // STEP 1: create a set of a bound value of the argument (or global variable)
            // and norm values of others
            for (String boundValue : parameter.getBounds()) {
                List<RandomValue> valuesOfArguments = new ArrayList<>();
                RandomValueForAssignment value = new RandomValueForAssignment(name, boundValue);
                valuesOfArguments.add(value);
                for (Parameter other : parameterMap.values()) {
                    if (other != parameter) {
                        RandomValueForAssignment otherValue = new RandomValueForAssignment(
                                other.getName(), other.getNorm());
                        valuesOfArguments.add(otherValue);
                    }
                }
                createTestCase(valuesOfArguments);
            }
        }

        // create norm test case
        List<RandomValue> valuesOfArguments = new ArrayList<>();
        for (Parameter parameter : parameterMap.values()) {
            RandomValueForAssignment otherValue = new RandomValueForAssignment(
                    parameter.getName(), parameter.getNorm());
            valuesOfArguments.add(otherValue);
        }
        createTestCase(valuesOfArguments);
    }

    private void createTestCase(List<RandomValue> boundDataValues) throws Exception {
        // create test case
        String postFix = "";
        switch (type) {
            case IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.NORMAL_BOUND:
                postFix = ITestCase.POSTFIX_TESTCASE_BY_NORMAL_BOUNDARY;
                break;
            case IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.BVA:
                postFix = ITestCase.POSTFIX_TESTCASE_BY_BVA;
                break;
            case IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.BVA_BOUNDARYCONDITION:
                postFix = ITestCase.POSTFIX_TESTCASE_BY_BVA;
                break;
            case IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.ROBUSTNESS:
                postFix = ITestCase.POSTFIX_TESTCASE_BY_ROBUSTNESS;
                break;
        }

        String nameOfTestcase = TestCaseManager.generateContinuousNameOfTestcase(
                fn.getSimpleName() + postFix);
        TestCase testCase = TestCaseManager.createTestCase(nameOfTestcase, fn);
        if (testCase != null) {
            // generate Random values for unsupported type (array, class, struct, v.v.)
            logger.debug("Generate random value for new test case");
            RootDataNode root = testCase.getRootDataNode();
            ICommonFunctionNode sut = testCase.getFunctionNode();
            List<RandomValue> randomValues = generateRandomValueForArguments(root, sut, new ArrayList<>(), null, testCase);
            randomValues.addAll(generateRandomValuesForGlobal(root, sut, testCase));

            // change generated random values to generated boundary values
            for (RandomValue value : boundDataValues) {
                for (RandomValue randomValue : randomValues) {
                    if (value.getNameUsedInExpansion().equals(randomValue.getNameUsedInExpansion())) {
                        randomValue.setValue(value.getValue());
                        break;
                    }
                }
            }

            TestCaseManager.getNameToBasicTestCaseMap().put(testCase.getName(), testCase);
            testCase.setCreationDateTime(LocalDateTime.now());

            String testPathFile = new WorkspaceConfig().fromJson()
                    .getTestpathDirectory() + File.separator + nameOfTestcase + ".tp";
            testCase.setTestPathFile(testPathFile);

            // set data for the test case with the boundValue and norm values of other parameters
            //  --- set values for arguments
            if (!(sut instanceof ConstructorNode)) {
                logger.debug("Set value for new test case");

                // get mapping between template type and real type
                SubprogramNode subprogramNode = Search2.findSubprogramUnderTest(root);
                if (subprogramNode instanceof TemplateSubprogramDataNode) {
                    this.realTypeMapping = ((TemplateSubprogramDataNode) subprogramNode).getRealTypeMapping();
                } else if (subprogramNode instanceof MacroSubprogramDataNode) {
                    this.realTypeMapping = ((MacroSubprogramDataNode) subprogramNode).getRealTypeMapping();
                }

                IDataNode sutRoot = Search2.findSubprogramUnderTest(root);
                // expand for arguments
                recursiveExpandUutBranch(sutRoot, randomValues, testCase);

                // expand for global variables
                RootDataNode globalRoot = Search2.findGlobalRoot(root);
                List<ValueDataNode> globalDataNodes = new ArrayList<>();
                for (IDataNode child : globalRoot.getChildren())
                    // not the instance
                    if (!(child.getName().startsWith(SourceConstant.INSTANCE_VARIABLE)))
                        if (child instanceof ValueDataNode) {
                            globalDataNodes.add((ValueDataNode) child);
                        }
                for (ValueDataNode globalDataNode : globalDataNodes) {
                    recursiveExpandUutBranch(globalDataNode, randomValues, testCase);
                }

                logger.debug("values of arguments and global variables: " + randomValues);

                // add to navigator tree
                testCase.updateToTestCasesNavigatorTree();
                Platform.runLater(() -> TestCasesNavigatorController.getInstance().refreshNavigatorTree());

                this.testCases.add(testCase);
            }
        }
    }

    public void setType(String type) {
        this.type = type;
    }

    private static class Parameter {
        private final String name;
        private String norm;
        private final List<String> bounds = new ArrayList<>();

        public Parameter(String name) {
            this.name = name;
        }

        public void setNorm(String norm) {
            this.norm = norm;
        }

        public String getNorm() {
            return norm;
        }

        public List<String> getBounds() {
            return bounds;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "Parameter{" +
                    "norm='" + norm + '\'' +
                    ", bounds=" + bounds +
                    '}';
        }
    }
}
