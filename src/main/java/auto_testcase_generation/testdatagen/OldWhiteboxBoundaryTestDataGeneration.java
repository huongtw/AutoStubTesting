package auto_testcase_generation.testdatagen;

import auto_testcase_generation.cfg.ICFG;
import auto_testcase_generation.cfg.object.AbstractConditionLoopCfgNode;
import auto_testcase_generation.cfg.object.ConditionCfgNode;
import auto_testcase_generation.cfg.object.EndFlagCfgNode;
import auto_testcase_generation.cfg.object.ICfgNode;
import auto_testcase_generation.cfg.testpath.FullTestpath;
import com.dse.config.IFunctionConfig;
import com.dse.config.WorkspaceConfig;
import com.dse.environment.object.EnviroCoverageTypeNode;
import com.dse.guifx_v3.controllers.TestCasesNavigatorController;
import com.dse.guifx_v3.helps.CacheHelper;
import com.dse.guifx_v3.helps.UIController;
import com.dse.guifx_v3.objects.TestCasesTreeItem;
import com.dse.logger.AkaLogger;
import com.dse.parser.externalvariable.RelatedExternalVariableDetecter;
import com.dse.parser.object.*;
import com.dse.search.Search2;
import com.dse.testcase_execution.TestCaseExecutionThread;
import com.dse.testcase_execution.TestcaseExecution;
import com.dse.testcase_manager.TestCase;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.testdata.object.*;
import com.dse.util.CFGUtils;
import com.dse.util.SourceConstant;
import com.dse.util.VariableTypeUtils;
import javafx.application.Platform;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Deprecated
public class OldWhiteboxBoundaryTestDataGeneration extends AbstractAutomatedTestdataGeneration {

    private final static AkaLogger logger = AkaLogger.get(OldWhiteboxBoundaryTestDataGeneration.class);

    private final Map<String, Parameter> parameterMap = new HashMap<>();
    private int maxIterationsforEachLoop = 1;

    public OldWhiteboxBoundaryTestDataGeneration(ICommonFunctionNode fn, String coverageType) {
        super(fn);
        this.coverageType = coverageType;
    }

    @Override
    public void generateTestdata(ICommonFunctionNode fn) throws Exception {
        // clear cache before generate testcase
        TestCasesTreeItem treeItem = CacheHelper.getFunctionToTreeItemMap().get(fn);
        if (CacheHelper.getTreeItemToListTestCasesMap().get(treeItem) != null)
            CacheHelper.getTreeItemToListTestCasesMap().get(treeItem).clear();

        logger.debug("Generating test data with strategy: " + IFunctionConfig.TEST_DATA_GENERATION_STRATEGIES.WHITEBOX_BOUNDARY);
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

    private void buildParameterMap() throws Exception {
        List<IVariableNode> variables = new ArrayList<>(fn.getArguments());
        RelatedExternalVariableDetecter detector = new RelatedExternalVariableDetecter((IFunctionNode) fn);
        variables.addAll(detector.findVariables());

        String cov = EnviroCoverageTypeNode.MCDC;
        ICFG cfg = null;
        if (fn instanceof MacroFunctionNode) {
            IFunctionNode tmpFunctionNode = ((MacroFunctionNode) fn).getCorrespondingFunctionNode();
            cfg = CFGUtils.createCFG(tmpFunctionNode, cov);
        } else if (fn instanceof IFunctionNode) {
            cfg = CFGUtils.createCFG((IFunctionNode) fn, cov);
        }

        if (cfg != null) {
            FullTestpath tp = new FullTestpath();
            traverseCFG(cfg.getBeginNode(), tp, variables);
        }

        for (Parameter parameter : parameterMap.values()) {
            IVariableNode variable = parameter.variableNode;
            parameter.setNorm(chooseNormValue(variable, parameter.bounds));
        }
    }

    private void traverseCFG(ICfgNode stm, FullTestpath tp, final List<IVariableNode> variableNodes) {
        tp.add(stm);

        if (stm instanceof EndFlagCfgNode) {

        } else {
            ICfgNode trueNode = stm.getTrueNode();
            ICfgNode falseNode = stm.getFalseNode();

            if (stm instanceof ConditionCfgNode) {
                IASTNode ast = ((ConditionCfgNode) stm).getAst();
                if (ast instanceof IASTBinaryExpression) {
                    IASTBinaryExpression astBin = (IASTBinaryExpression) ast;
                    IASTExpression operand1 = astBin.getOperand1();
                    IASTExpression operand2 = astBin.getOperand2();

                    IVariableNode variable = getParameter(operand1, variableNodes);
                    if (variable != null) {
                        appendMap(variable, operand2);
                    } else {
                        variable = getParameter(operand2, variableNodes);
                        if (variable != null) {
                            appendMap(variable, operand1);
                        }
                    }
                }

                if (stm instanceof AbstractConditionLoopCfgNode) {
                    int currentIterations = tp.count(trueNode);
                    if (currentIterations < maxIterationsforEachLoop) {
                        traverseCFG(falseNode, tp, variableNodes);
                        traverseCFG(trueNode, tp, variableNodes);
                    } else {
                        traverseCFG(falseNode, tp, variableNodes);
                    }
                } else {
                    traverseCFG(falseNode, tp, variableNodes);
                    traverseCFG(trueNode, tp, variableNodes);
                }

            } else {
                traverseCFG(trueNode, tp, variableNodes);
            }
        }

        tp.remove(tp.size() - 1);
    }

    private void appendMap(IVariableNode variable, IASTExpression expr) {
        String name = variable.getName();
        Parameter parameter = parameterMap.get(name);
        if (parameter == null)
            parameter = new Parameter(variable);
        String type = variable.getRealType();
        type = VariableTypeUtils.removeRedundantKeyword(type);
        String valStr = expr.getRawSignature();
        if (VariableTypeUtils.isNumBasicInteger(type)
                || VariableTypeUtils.isStdInt(type)) {
            logger.debug(type + ": get long boundary values");
            try {
                long val = Long.parseLong(valStr);
                parameter.getBounds().add(valStr);
                parameter.getBounds().add(String.valueOf(val-1));
                parameter.getBounds().add(String.valueOf(val+1));
            } catch (NumberFormatException e) {

            }


        } else if (VariableTypeUtils.isNumBasicFloat(type)) {
            logger.debug(type + ": get double boundary values");
            final double delta = new WorkspaceConfig().fromJson()
                    .getDefaultFunctionConfig().getFloatAndDoubleDelta();
            try {
                double val = Double.parseDouble(valStr);
                parameter.getBounds().add(valStr);
                parameter.getBounds().add(String.valueOf(val-delta));
                parameter.getBounds().add(String.valueOf(val+delta));
            } catch (NumberFormatException e) {

            }

        } else if (VariableTypeUtils.isBoolBasic(type)) {
            parameter.getBounds().add("0");
            parameter.getBounds().add("1");
        } else if (VariableTypeUtils.isChBasic(type)) {
            try {
                long val = Long.parseLong(valStr);
                parameter.getBounds().add(valStr);
                parameter.getBounds().add(String.valueOf(val-1));
                parameter.getBounds().add(String.valueOf(val+1));
            } catch (NumberFormatException e) {
                if (valStr.length() == 1) {
                    int val = valStr.charAt(0);
                    parameter.getBounds().add(String.valueOf(val));
                    parameter.getBounds().add(String.valueOf(val-1));
                    parameter.getBounds().add(String.valueOf(val+1));
                }
            }
        }

        parameterMap.put(name, parameter);
    }

    private IVariableNode getParameter(IASTExpression expr, final List<IVariableNode> variableNodes) {
        if (expr instanceof IASTIdExpression)
            return variableNodes.stream()
                    .filter(v -> v.getName().equals(expr.getRawSignature()))
                    .findFirst()
                    .orElse(null);

        return null;
    }

    public String chooseNormValue(IVariableNode argument, List<String> boundaries) {
        String norm = "NA";

        String argType = argument.getRealType();
        argType = VariableTypeUtils.deleteStorageClasses(argType);
        argType = VariableTypeUtils.deleteReferenceOperator(argType);

        // Boolean
        if (VariableTypeUtils.isBoolBasic(argType)) {
            logger.debug(argType + ": isBoolBasic");
            return chooseBooleanNormValue(boundaries);
        }
        // Character
        else if (VariableTypeUtils.isChBasic(argType)) {
            logger.debug(argType + ": isChBasic");
            return chooseCharacterNormValue(boundaries);
        }
        // Number
        else if (VariableTypeUtils.isNumBasic(argType)) {
            logger.debug(argType + ": isNumBasic");
            return chooseNumberNormValue(argType, boundaries);
        }

        return norm;
    }

    public String chooseNumberNormValue(String argType, List<String> boundaries) {
        if (VariableTypeUtils.isNumBasicInteger(argType)) {
            logger.debug(argType + ": isNumBasicInteger");
            return chooseLongNormValue(boundaries);
        } else if (VariableTypeUtils.isNumBasicFloat(argType)) {
            logger.debug(argType + ": isNumBasicFloat");
            return chooseDoubleNormValue(boundaries);
        } else if (VariableTypeUtils.isStdInt(argType)) {
            return chooseLongNormValue(boundaries);
        }

        return "NA";
    }

    public String chooseLongNormValue(List<String> boundaries) {
        // todo: check if norm equals "NA"
        String norm = "NA";

        if (!boundaries.isEmpty()) {
            long max, min;
            max = Long.parseLong(boundaries.get(0));
            min = max;

            for (int i = 1; i < boundaries.size(); i++) {
                long cur = Long.parseLong(boundaries.get(i));
                if (cur > max) max = cur;
                if (cur < min) min = cur;
            }

            long normVal = (max + min) / 2;
            norm = String.valueOf(normVal);
        }

        return norm;
    }

    public String chooseDoubleNormValue(List<String> boundaries) {
        // todo: check if norm equals "NA"
        String norm = "NA";

        if (!boundaries.isEmpty()) {
            double max, min;
            max = Long.parseLong(boundaries.get(0));
            min = max;

            for (int i = 1; i < boundaries.size(); i++) {
                long cur = Long.parseLong(boundaries.get(i));
                if (cur > max) max = cur;
                if (cur < min) min = cur;
            }

            double normVal = (max + min) / 2;
            norm = String.valueOf(normVal);
        }

        return norm;
    }

    public String chooseBooleanNormValue(List<String> boundaries) {
        return "NA";
    }

    public String chooseCharacterNormValue(List<String> boundaries) {
        return "NA";
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
        String postFix = ".cbound";

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

    private static class Parameter {
        private final IVariableNode variableNode;
        private String norm;
        private final List<String> bounds = new ArrayList<>();

        public Parameter(IVariableNode variableNode) {
            this.variableNode = variableNode;
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
            return variableNode.getName();
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
