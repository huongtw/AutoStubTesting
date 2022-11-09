package auto_testcase_generation.testdatagen;

import auto_testcase_generation.cfg.ICFG;
import auto_testcase_generation.cfg.object.AbstractConditionLoopCfgNode;
import auto_testcase_generation.cfg.object.ConditionCfgNode;
import auto_testcase_generation.cfg.object.EndFlagCfgNode;
import auto_testcase_generation.cfg.object.ICfgNode;
import auto_testcase_generation.cfg.testpath.FullTestpath;
import auto_testcase_generation.testdatagen.se.PathConstraint;
import auto_testcase_generation.testdatagen.se.PathConstraints;
import auto_testcase_generation.testdatagen.se.solver.RunZ3OnCMD;
import auto_testcase_generation.testdatagen.se.solver.SmtLibGeneration;
import auto_testcase_generation.testdatagen.se.solver.solutionparser.Z3SolutionParser;
import com.dse.boundary.MultiplePrimitiveBound;
import com.dse.boundary.PrimitiveBound;
import com.dse.config.*;
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
import com.dse.util.Utils;
import com.dse.util.VariableTypeUtils;
import javafx.application.Platform;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.util.Pair;
import org.eclipse.cdt.core.dom.ast.*;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BoundaryConditionTestDataGeneration extends AbstractAutomatedTestdataGeneration {

    private final static AkaLogger logger = AkaLogger.get(BoundaryConditionTestDataGeneration.class);

    private final Map<String, Parameter> parameterMap = new HashMap<>();
    private final ArrayList<ConditionParameters> conditionParameterLíst = new ArrayList<>();
    private Map<String, IFunctionConfigBound> boundMap = new HashMap<>();
    private int maxIterationsforEachLoop = 1;

    public BoundaryConditionTestDataGeneration(ICommonFunctionNode fn, String coverageType) {
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
                    int operator = astBin.getOperator();
                    IVariableNode variable1 = getParameter(operand1, variableNodes);
                    IVariableNode variable2 = getParameter(operand2, variableNodes);
                    if (variable1 != null ^ variable2 != null) {
                        if ((operand1 instanceof IASTIdExpression && !(operand2 instanceof IASTLiteralExpression))
                                || (!(operand1 instanceof IASTLiteralExpression) && operand2 instanceof IASTIdExpression)) {
                            parseExpression(operand1, operand2, operator, variableNodes);
                        } else {
                            if (variable1 != null) {
                                appendMap(variable1, operand2);
                                List<Parameter> parameters = new ArrayList<>(Arrays.asList(parameterMap.get(variable1.getName())));
                                ConditionParameters conditionParameter = new ConditionParameters(parameters);
                                conditionParameterLíst.add(conditionParameter);
                                for (String value : parameterMap.get(variable1.getName()).getBounds()) {
                                    List<String> testData = new ArrayList<>(Arrays.asList(value));
                                    conditionParameter.getValues().add(testData);
                                }

                            } else {
                                appendMap(variable2, operand1);
                                List<Parameter> parameters = new ArrayList<>(Arrays.asList(parameterMap.get(variable2.getName())));
                                ConditionParameters conditionParameter = new ConditionParameters(parameters);
                                conditionParameterLíst.add(conditionParameter);
                                for (String value : parameterMap.get(variable2.getName()).getBounds()) {
                                    List<String> testData = new ArrayList<>(Arrays.asList(value));
                                    conditionParameter.getValues().add(testData);
                                }
                            }
                        }

                    } else if (variable1 != null) {  // && variable2 != null
                        createBound(variable1);
                        createBound(variable2);
                        List<Parameter> parameters = new ArrayList<>(
                                Arrays.asList(parameterMap.get(variable1.getName()), parameterMap.get(variable2.getName()))
                        );
                        ConditionParameters conditionParameter = new ConditionParameters(parameters);
                        conditionParameterLíst.add(conditionParameter);
                        Parameter parameter1 = conditionParameter.getParameter(0);
                        Parameter parameter2 = conditionParameter.getParameter(1);
                        String type = parameter1.getType();
                        switch (operator) {
                            case IASTBinaryExpression.op_lessThan:
                            case IASTBinaryExpression.op_greaterThan:
                            case IASTBinaryExpression.op_lessEqual:
                            case IASTBinaryExpression.op_greaterEqual:
                                if (VariableTypeUtils.isNumBasicInteger(type) || VariableTypeUtils.isStdInt(type)) {
                                    long min1 = Long.parseLong(parameter1.bounds.get(0));
                                    long min2 = Long.parseLong(parameter2.bounds.get(0));

                                    //min
                                    if (min1 > min2) {
                                        conditionParameter.addTestData(String.valueOf(min1), String.valueOf(min2));
                                        conditionParameter.addTestData(String.valueOf(min1), String.valueOf(min1 - 1));
                                        conditionParameter.addTestData(String.valueOf(min1), String.valueOf(min1));
                                        conditionParameter.addTestData(String.valueOf(min1), String.valueOf(min1 + 1));
                                        conditionParameter.addTestData(String.valueOf(min1 + 1), String.valueOf(min1));
                                        conditionParameter.addTestData(String.valueOf(min1 + 1), String.valueOf(min1 + 1));
                                    } else if (min1 < min2) {
                                        conditionParameter.addTestData(String.valueOf(min1), String.valueOf(min2));
                                        conditionParameter.addTestData(String.valueOf(min2 - 1), String.valueOf(min2));
                                        conditionParameter.addTestData(String.valueOf(min2), String.valueOf(min2));
                                        conditionParameter.addTestData(String.valueOf(min2 + 1), String.valueOf(min2));
                                        conditionParameter.addTestData(String.valueOf(min2), String.valueOf(min2 + 1));
                                        conditionParameter.addTestData(String.valueOf(min2 + 1), String.valueOf(min2 + 1));
                                    } else {
                                        conditionParameter.addTestData(String.valueOf(min1), String.valueOf(min2));
                                        conditionParameter.addTestData(String.valueOf(min1), String.valueOf(min2 + 1));
                                        conditionParameter.addTestData(String.valueOf(min1 + 1), String.valueOf(min2));
                                        conditionParameter.addTestData(String.valueOf(min1 + 1), String.valueOf(min2 + 1));
                                        conditionParameter.addTestData(String.valueOf(min1 + 1), String.valueOf(min2 + 2));
                                        conditionParameter.addTestData(String.valueOf(min1 + 2), String.valueOf(min2 + 1));
                                    }
                                    //repre -
                                    List<Pair<String, String>> repreRangeParam1 = parameter1.getRepresentativesRange();
                                    List<Pair<String, String>> repreRangeParam2 = parameter2.getRepresentativesRange();
                                    if (min1 < 0 ^ min2 < 0) {
                                        Pair<String, String> testData;
                                        if (min1 < 0) {
                                            conditionParameter.addTestData(repreRangeParam1.get(0).getSecond(),
                                                    repreRangeParam2.get(0).getFirst());

                                        } else {
                                            conditionParameter.addTestData(repreRangeParam1.get(0).getFirst(),
                                                    repreRangeParam2.get(0).getSecond());
                                        }
                                    } else if (min1 < 0) { // && min2<0
                                        long minValue = Math.max(Long.parseLong(repreRangeParam1.get(0).getFirst()),
                                                Long.parseLong(repreRangeParam2.get(0).getFirst())) + 1;
                                        long maxValue = Math.min(Long.parseLong(repreRangeParam1.get(0).getSecond()),
                                                Long.parseLong(repreRangeParam2.get(0).getSecond())) - 1;
                                        if (minValue > maxValue) {
                                            maxValue = Math.max(Long.parseLong(repreRangeParam1.get(0).getSecond()),
                                                    Long.parseLong(repreRangeParam2.get(0).getSecond())) - 1;
                                        }

                                        long randomValue = (long) Math.random() * (maxValue - minValue) + minValue;
                                        if (maxValue == Long.parseLong(repreRangeParam1.get(0).getSecond())) {
                                            conditionParameter.addTestData(String.valueOf(randomValue), String.valueOf(randomValue - 1));
                                            conditionParameter.addTestData(String.valueOf(randomValue), String.valueOf(randomValue));
                                            conditionParameter.addTestData(String.valueOf(randomValue), String.valueOf(randomValue + 1));
                                        } else {
                                            conditionParameter.addTestData(String.valueOf(randomValue - 1), String.valueOf(randomValue));
                                            conditionParameter.addTestData(String.valueOf(randomValue), String.valueOf(randomValue));
                                            conditionParameter.addTestData(String.valueOf(randomValue + 1), String.valueOf(randomValue));
                                        }
                                        //norm
                                        conditionParameter.addTestData(String.valueOf(0), String.valueOf(-1));
                                        conditionParameter.addTestData(String.valueOf(-1), String.valueOf(0));
                                        conditionParameter.addTestData(String.valueOf(0), String.valueOf(0));
                                    }

                                    //repre +
                                    long minValue = Math.max(Long.parseLong(repreRangeParam1.get(repreRangeParam1.size() - 1).getFirst()),
                                            Long.parseLong(repreRangeParam2.get(repreRangeParam2.size() - 1).getFirst())) + 1;
                                    long maxValue = Math.min(Long.parseLong(repreRangeParam1.get(repreRangeParam1.size() - 1).getSecond()),
                                            Long.parseLong(repreRangeParam2.get(repreRangeParam2.size() - 1).getSecond())) - 1;
                                    if (minValue > maxValue) {
                                        minValue = Math.min(Long.parseLong(repreRangeParam1.get(repreRangeParam1.size() - 1).getFirst()),
                                                Long.parseLong(repreRangeParam2.get(repreRangeParam2.size() - 1).getFirst())) + 1;
                                    }
                                    long randomValue = (long) Math.random() * (maxValue - minValue) + minValue;
                                    if (minValue == Long.parseLong(parameter1.getRepresentativesRange().get(0).getFirst())) {
                                        conditionParameter.addTestData(String.valueOf(randomValue), String.valueOf(randomValue - 1));
                                        conditionParameter.addTestData(String.valueOf(randomValue), String.valueOf(randomValue));
                                        conditionParameter.addTestData(String.valueOf(randomValue), String.valueOf(randomValue + 1));
                                    } else {
                                        conditionParameter.addTestData(String.valueOf(randomValue - 1), String.valueOf(randomValue));
                                        conditionParameter.addTestData(String.valueOf(randomValue), String.valueOf(randomValue));
                                        conditionParameter.addTestData(String.valueOf(randomValue + 1), String.valueOf(randomValue));
                                    }
                                    //max
                                    long max1 = Long.parseLong(parameter1.getBounds().get(parameter1.getBounds().size() - 1));
                                    long max2 = Long.parseLong(parameter2.getBounds().get(parameter2.getBounds().size() - 1));
                                    if (max1 < max2) {
                                        conditionParameter.addTestData(String.valueOf(max1 - 1), String.valueOf(max1 - 1));
                                        conditionParameter.addTestData(String.valueOf(max1 - 1), String.valueOf(max1));
                                        conditionParameter.addTestData(String.valueOf(max1), String.valueOf(max1 - 1));
                                        conditionParameter.addTestData(String.valueOf(max1), String.valueOf(max1));
                                        conditionParameter.addTestData(String.valueOf(max1), String.valueOf(max1 + 1));
                                        conditionParameter.addTestData(String.valueOf(max1), String.valueOf(max2));
                                    } else if (max1 > max2) {
                                        conditionParameter.addTestData(String.valueOf(max2 - 1), String.valueOf(max2 - 1));
                                        conditionParameter.addTestData(String.valueOf(max2 - 1), String.valueOf(max2));
                                        conditionParameter.addTestData(String.valueOf(max2), String.valueOf(max2 - 1));
                                        conditionParameter.addTestData(String.valueOf(max2), String.valueOf(max2));
                                        conditionParameter.addTestData(String.valueOf(max2 + 1), String.valueOf(max2));
                                        conditionParameter.addTestData(String.valueOf(max1), String.valueOf(max2));
                                    } else {
                                        conditionParameter.addTestData(String.valueOf(max1 - 2), String.valueOf(max2 - 1));
                                        conditionParameter.addTestData(String.valueOf(max1 - 1), String.valueOf(max2 - 2));
                                        conditionParameter.addTestData(String.valueOf(max1 - 1), String.valueOf(max2 - 1));
                                        conditionParameter.addTestData(String.valueOf(max1 - 1), String.valueOf(max2));
                                        conditionParameter.addTestData(String.valueOf(max1), String.valueOf(max2 - 1));
                                        conditionParameter.addTestData(String.valueOf(max1), String.valueOf(max2));
                                    }
                                }
                                break;
                            case IASTBinaryExpression.op_notequals:
                                if (VariableTypeUtils.isNumBasicInteger(type) || VariableTypeUtils.isStdInt(type)) {
                                    long min1 = Long.parseLong(parameter1.bounds.get(0));
                                    long min2 = Long.parseLong(parameter2.bounds.get(0));

                                    //min
                                    if (min1 > min2) {
                                        conditionParameter.addTestData(String.valueOf(min1), String.valueOf(min2));
                                        conditionParameter.addTestData(String.valueOf(min1), String.valueOf(min1 + 1));
                                    } else if (min1 < min2) {
                                        conditionParameter.addTestData(String.valueOf(min1), String.valueOf(min2));
                                        conditionParameter.addTestData(String.valueOf(min2 + 1), String.valueOf(min2));
                                    } else {
                                        conditionParameter.addTestData(String.valueOf(min1), String.valueOf(min2 + 1));
                                        conditionParameter.addTestData(String.valueOf(min1 + 1), String.valueOf(min2));
                                    }
                                    //norm
                                    if (min1 < 0 ^ min2 < 0) {
                                        if (min1 < 0) {
                                            conditionParameter.addTestData(parameter1.norm, String.valueOf(Long.parseLong(parameter1.norm) + 1));
                                        } else {
                                            conditionParameter.addTestData(String.valueOf(Long.parseLong(parameter2.norm) + 1), parameter2.norm);
                                        }
                                    } else if (min1 < 0) { //a,b: signed
                                        conditionParameter.addTestData(parameter1.norm, String.valueOf(Long.parseLong(parameter1.norm) + 1));
                                        conditionParameter.addTestData(String.valueOf(Long.parseLong(parameter2.norm) + 1), parameter2.norm);
                                    } else {  //a,b: unsigned
                                        //repre +
                                        List<Pair<String, String>> repreRangeParam1 = parameter1.getRepresentativesRange();
                                        List<Pair<String, String>> repreRangeParam2 = parameter2.getRepresentativesRange();
                                        long minValue = Math.max(Long.parseLong(repreRangeParam1.get(repreRangeParam1.size() - 1).getFirst()),
                                                Long.parseLong(repreRangeParam2.get(repreRangeParam2.size() - 1).getFirst())) + 1;
                                        long maxValue = Math.min(Long.parseLong(repreRangeParam1.get(repreRangeParam1.size() - 1).getSecond()),
                                                Long.parseLong(repreRangeParam2.get(repreRangeParam2.size() - 1).getSecond())) - 1;
                                        if (minValue > maxValue) {
                                            minValue = Math.min(Long.parseLong(repreRangeParam1.get(repreRangeParam1.size() - 1).getFirst()),
                                                    Long.parseLong(repreRangeParam2.get(repreRangeParam2.size() - 1).getFirst())) + 1;
                                        }
                                        long randomValue = (long) Math.random() * (maxValue - minValue) + minValue;
                                        if (minValue == Long.parseLong(parameter1.getRepresentativesRange().get(0).getFirst())) {
                                            conditionParameter.addTestData(String.valueOf(randomValue), String.valueOf(randomValue - 1));
//                                            conditionParameter.addTestData(String.valueOf(randomValue), String.valueOf(randomValue)));
                                            conditionParameter.addTestData(String.valueOf(randomValue), String.valueOf(randomValue + 1));
                                        } else {
                                            conditionParameter.addTestData(String.valueOf(randomValue - 1), String.valueOf(randomValue));
//                                            conditionParameter.addTestData(String.valueOf(randomValue), String.valueOf(randomValue)));
                                            conditionParameter.addTestData(String.valueOf(randomValue + 1), String.valueOf(randomValue));
                                        }
                                    }


                                    //max
                                    long max1 = Long.parseLong(parameter1.getBounds().get(parameter1.getBounds().size() - 1));
                                    long max2 = Long.parseLong(parameter2.getBounds().get(parameter2.getBounds().size() - 1));
                                    if (max1 < max2) {
                                        conditionParameter.addTestData(String.valueOf(max1), String.valueOf(max1 - 1));
                                        conditionParameter.addTestData(String.valueOf(max1), String.valueOf(max2));
                                    } else if (max1 > max2) {
                                        conditionParameter.addTestData(String.valueOf(max2 - 1), String.valueOf(max2));
                                        conditionParameter.addTestData(String.valueOf(max1), String.valueOf(max2));
                                    } else {
                                        conditionParameter.addTestData(String.valueOf(max1 - 1), String.valueOf(max2));
                                        conditionParameter.addTestData(String.valueOf(max1), String.valueOf(max2 - 1));
                                    }
                                }
                                break;
                        }

                    } else {
                        parseExpression(operand1, operand2, operator, variableNodes);
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

    private void parseExpression(IASTExpression operand1, IASTExpression operand2, int operator, final List<IVariableNode> variableNodes) {
        Expression expression1 = new Expression(operand1, variableNodes);
        Expression expression2 = new Expression(operand2, variableNodes);
        createTC(expression1, expression2, operator, variableNodes);
    }

    private void createTC(Expression expression1, Expression expression2, int operator, final List<IVariableNode> variableNodes) {
        Set<Parameter> parameterSet = new HashSet<>();
        parameterSet.addAll(expression1.getExpParamSet());
        parameterSet.addAll(expression2.getExpParamSet());
        List<Parameter> parameters = new ArrayList<>(parameterSet);
        ConditionParameters conditionParameters = new ConditionParameters(parameters);
        conditionParameterLíst.add(conditionParameters);
        if (expression1.getOperand() instanceof IASTLiteralExpression
                ^ expression2.getOperand() instanceof IASTLiteralExpression) {
            String valStr;
            Expression expression;
            if (expression2.getOperand() instanceof IASTLiteralExpression) {
                valStr = expression2.getOperand().getRawSignature();
                expression = expression1;
            } else {
                valStr = expression1.getOperand().getRawSignature();
                expression = expression2;
            }
            long val = Long.parseLong(valStr);
            solveExp(expression, expression.getMin(), null, null, variableNodes, conditionParameters);
            solveExp(expression, String.valueOf(val - 1), null, null, variableNodes, conditionParameters);
            solveExp(expression, valStr, null, null, variableNodes, conditionParameters);
            solveExp(expression, String.valueOf(val + 1), null, null, variableNodes, conditionParameters);
            solveExp(expression, expression.getMax(), null, null, variableNodes, conditionParameters);

        } else {
            long min1 = Long.parseLong(expression1.getMin());
            long min2 = Long.parseLong(expression2.getMin());
            long max1 = Long.parseLong(expression1.getMax());
            long max2 = Long.parseLong(expression2.getMax());
            switch (operator) {
                case IASTBinaryExpression.op_lessThan:
                case IASTBinaryExpression.op_greaterThan:
                case IASTBinaryExpression.op_lessEqual:
                case IASTBinaryExpression.op_greaterEqual:
                    if (min1 > min2) {
                        solveExp(expression1, String.valueOf(min1), expression2, String.valueOf(min2), variableNodes, conditionParameters);
                        solveExp(expression1, String.valueOf(min1), expression2, String.valueOf(min1 - 1), variableNodes, conditionParameters);
                        solveExp(expression1, String.valueOf(min1), expression2, String.valueOf(min1), variableNodes, conditionParameters);
                        solveExp(expression1, String.valueOf(min1), expression2, String.valueOf(min1 + 1), variableNodes, conditionParameters);
                        solveExp(expression1, String.valueOf(min1 + 1), expression2, String.valueOf(min1), variableNodes, conditionParameters);
                        solveExp(expression1, String.valueOf(min1 + 1), expression2, String.valueOf(min1 + 1), variableNodes, conditionParameters);
                    } else if (min1 < min2) {
                        solveExp(expression1, String.valueOf(min1), expression2, String.valueOf(min2), variableNodes, conditionParameters);
                        solveExp(expression1, String.valueOf(min2 - 1), expression2, String.valueOf(min2), variableNodes, conditionParameters);
                        solveExp(expression1, String.valueOf(min2), expression2, String.valueOf(min2), variableNodes, conditionParameters);
                        solveExp(expression1, String.valueOf(min2 + 1), expression2, String.valueOf(min2), variableNodes, conditionParameters);
                        solveExp(expression1, String.valueOf(min2), expression2, String.valueOf(min2 + 1), variableNodes, conditionParameters);
                        solveExp(expression1, String.valueOf(min2 + 1), expression2, String.valueOf(min2 + 1), variableNodes, conditionParameters);
                    } else {
                        solveExp(expression1, String.valueOf(min1), expression2, String.valueOf(min2), variableNodes, conditionParameters);
                        solveExp(expression1, String.valueOf(min1), expression2, String.valueOf(min2 + 1), variableNodes, conditionParameters);
                        solveExp(expression1, String.valueOf(min1 + 1), expression2, String.valueOf(min2), variableNodes, conditionParameters);
                        solveExp(expression1, String.valueOf(min1 + 1), expression2, String.valueOf(min2 + 1), variableNodes, conditionParameters);
                        solveExp(expression1, String.valueOf(min1 + 1), expression2, String.valueOf(min2 + 2), variableNodes, conditionParameters);
                        solveExp(expression1, String.valueOf(min1 + 2), expression2, String.valueOf(min2 + 1), variableNodes, conditionParameters);
                    }
                    //repre -
                    if (min1 < 0 ^ min2 < 0) {
                        Pair<String, String> testData;
                        if (min1 < 0) {
                            solveExp(expression1, String.valueOf(-1), expression2, String.valueOf(2), variableNodes, conditionParameters);
                        } else {
                            solveExp(expression1, String.valueOf(2), expression2, String.valueOf(-1), variableNodes, conditionParameters);
                        }
                    } else if (min1 < 0) { // && min2<0
                        long minValue = Math.max(min1, min2) + 3;
                        long maxValue = -3;
                        if (minValue <= maxValue) {
                            long randomValue = (long) Math.random() * (maxValue - minValue) + minValue;
                            solveExp(expression1, String.valueOf(randomValue), expression2, String.valueOf(randomValue - 1), variableNodes, conditionParameters);
                            solveExp(expression1, String.valueOf(randomValue), expression2, String.valueOf(randomValue), variableNodes, conditionParameters);
                            solveExp(expression1, String.valueOf(randomValue), expression2, String.valueOf(randomValue + 1), variableNodes, conditionParameters);
                        }

                        //norm
                        solveExp(expression1, String.valueOf(0), expression2, String.valueOf(-1), variableNodes, conditionParameters);
                        solveExp(expression1, String.valueOf(-1), expression2, String.valueOf(0), variableNodes, conditionParameters);
                        solveExp(expression1, String.valueOf(0), expression2, String.valueOf(0), variableNodes, conditionParameters);
                    }

                    //repre +
                    if (max1 > 0 && max2 > 0) {
                        long minValue = 3;
                        long maxValue = Math.min(max1, max2) - 3;
                        if (minValue <= maxValue) {
                            long randomValue = (long) Math.random() * (maxValue - minValue) + minValue;
                            solveExp(expression1, String.valueOf(randomValue), expression2, String.valueOf(randomValue - 1), variableNodes, conditionParameters);
                            solveExp(expression1, String.valueOf(randomValue), expression2, String.valueOf(randomValue), variableNodes, conditionParameters);
                            solveExp(expression1, String.valueOf(randomValue), expression2, String.valueOf(randomValue + 1), variableNodes, conditionParameters);
                        }
                    }
                    //max
                    if (max1 < max2) {
                        solveExp(expression1, String.valueOf(max1 - 1), expression2, String.valueOf(max1 - 1), variableNodes, conditionParameters);
                        solveExp(expression1, String.valueOf(max1 - 1), expression2, String.valueOf(max1), variableNodes, conditionParameters);
                        solveExp(expression1, String.valueOf(max1), expression2, String.valueOf(max1 - 1), variableNodes, conditionParameters);
                        solveExp(expression1, String.valueOf(max1), expression2, String.valueOf(max1), variableNodes, conditionParameters);
                        solveExp(expression1, String.valueOf(max1), expression2, String.valueOf(max1 + 1), variableNodes, conditionParameters);
                        solveExp(expression1, String.valueOf(max1), expression2, String.valueOf(max2), variableNodes, conditionParameters);
                    } else if (max1 > max2) {
                        solveExp(expression1, String.valueOf(max2 - 1), expression2, String.valueOf(max2 - 1), variableNodes, conditionParameters);
                        solveExp(expression1, String.valueOf(max2 - 1), expression2, String.valueOf(max2), variableNodes, conditionParameters);
                        solveExp(expression1, String.valueOf(max2), expression2, String.valueOf(max2 - 1), variableNodes, conditionParameters);
                        solveExp(expression1, String.valueOf(max2), expression2, String.valueOf(max2), variableNodes, conditionParameters);
                        solveExp(expression1, String.valueOf(max2 + 1), expression2, String.valueOf(max2), variableNodes, conditionParameters);
                        solveExp(expression1, String.valueOf(max1), expression2, String.valueOf(max2), variableNodes, conditionParameters);
                    } else {
                        solveExp(expression1, String.valueOf(max1 - 2), expression2, String.valueOf(max2 - 1), variableNodes, conditionParameters);
                        solveExp(expression1, String.valueOf(max1 - 1), expression2, String.valueOf(max2 - 2), variableNodes, conditionParameters);
                        solveExp(expression1, String.valueOf(max1 - 1), expression2, String.valueOf(max2 - 1), variableNodes, conditionParameters);
                        solveExp(expression1, String.valueOf(max1 - 1), expression2, String.valueOf(max2), variableNodes, conditionParameters);
                        solveExp(expression1, String.valueOf(max1), expression2, String.valueOf(max2 - 1), variableNodes, conditionParameters);
                        solveExp(expression1, String.valueOf(max1), expression2, String.valueOf(max2), variableNodes, conditionParameters);
                    }
                    break;

                case IASTBinaryExpression.op_notequals:
                    //min
                    if (min1 > min2) {
                        solveExp(expression1, String.valueOf(min1), expression2, String.valueOf(min2), variableNodes, conditionParameters);
                        solveExp(expression1, String.valueOf(min1), expression2, String.valueOf(min1 + 1), variableNodes, conditionParameters);
                    } else if (min1 < min2) {
                        solveExp(expression1, String.valueOf(min1), expression2, String.valueOf(min2), variableNodes, conditionParameters);
                        solveExp(expression1, String.valueOf(min2 + 1), expression2, String.valueOf(min2), variableNodes, conditionParameters);
                    } else {
                        solveExp(expression1, String.valueOf(min1), expression2, String.valueOf(min2 + 1), variableNodes, conditionParameters);
                        solveExp(expression1, String.valueOf(min1 + 1), expression2, String.valueOf(min2), variableNodes, conditionParameters);
                    }
                    //norm
                    if (min1 < 0 ^ min2 < 0) {
                        if (min1 < 0) {
                            solveExp(expression1, String.valueOf(0), expression2, String.valueOf(1), variableNodes, conditionParameters);
                        } else {
                            solveExp(expression1, String.valueOf(1), expression2, String.valueOf(0), variableNodes, conditionParameters);
                        }
                    } else if (min1 < 0) { //a,b: signed
                        solveExp(expression1, String.valueOf(0), expression2, String.valueOf(1), variableNodes, conditionParameters);
                        solveExp(expression1, String.valueOf(1), expression2, String.valueOf(0), variableNodes, conditionParameters);
                    } else {  //a,b: unsigned
                        //repre +
                        long minValue = 3;
                        long maxValue = Math.min(max1, max2) - 3;
                        if (minValue <= maxValue) {
                            long randomValue = (long) Math.random() * (maxValue - minValue) + minValue;
                            solveExp(expression1, String.valueOf(randomValue), expression2, String.valueOf(randomValue - 1), variableNodes, conditionParameters);
                            solveExp(expression1, String.valueOf(randomValue), expression2, String.valueOf(randomValue + 1), variableNodes, conditionParameters);
                        }
                    }

                    //max
                    if (max1 < max2) {
                        solveExp(expression1, String.valueOf(max1), expression2, String.valueOf(max1 - 1), variableNodes, conditionParameters);
                        solveExp(expression1, String.valueOf(max1), expression2, String.valueOf(max2), variableNodes, conditionParameters);
                    } else if (max1 > max2) {
                        solveExp(expression1, String.valueOf(max2 - 1), expression2, String.valueOf(max2), variableNodes, conditionParameters);
                        solveExp(expression1, String.valueOf(max1), expression2, String.valueOf(max2), variableNodes, conditionParameters);
                    } else {
                        solveExp(expression1, String.valueOf(max1 - 1), expression2, String.valueOf(max2), variableNodes, conditionParameters);
                        solveExp(expression1, String.valueOf(max1), expression2, String.valueOf(max2 - 1), variableNodes, conditionParameters);
                    }
                    break;
            }
        }
    }

    private void solveExp(Expression expression1, String value1, Expression expression2, String value2, final List<IVariableNode> variableNodes, ConditionParameters conditionParameters) {
        String constraintName1 = expression1.getOperand().getRawSignature() + " == " + value1;
        PathConstraint constraint1 = new PathConstraint(constraintName1, null, PathConstraint.CREATE_FROM_DECISION);
        PathConstraints constraints = new PathConstraints();
        constraints.add(constraint1);
        if (expression2 != null) {
            String constraintName2 = expression2.getOperand().getRawSignature() + " == " + value2;
            PathConstraint constraint2 = new PathConstraint(constraintName2, null, PathConstraint.CREATE_FROM_DECISION);
            constraints.add(constraint2);
        }
        SmtLibGeneration smt = new SmtLibGeneration(variableNodes, constraints, fn, null);
        try {
            smt.generate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.debug("SMT-LIB file:\n" + smt.getSmtLibContent());
        String constraintFile = new WorkspaceConfig().fromJson().getConstraintFolder()
                + File.separator + new RandomDataGenerator().nextInt(0, 99999) + ".smt2";
        logger.debug("constraintFile: " + constraintFile);
        Utils.writeContentToFile(smt.getSmtLibContent(), constraintFile);

        // solve
        logger.debug("Calling solver z3");
        String z3Path = new AkaConfig().fromJson().getZ3Path();
        if (new File(z3Path).exists()) {
            RunZ3OnCMD z3Runner = new RunZ3OnCMD(z3Path, constraintFile);
            try {
                z3Runner.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }

            logger.debug("Original solution:\n" + z3Runner.getSolution());
            String staticSolution = new Z3SolutionParser().getSolution(z3Runner.getSolution());

            if (!staticSolution.trim().isEmpty()) {
                String md5 = Utils.computeMd5(staticSolution);
                if (!generatedTestcases.contains(md5)) {
                    generatedTestcases.add(md5);
                    logger.debug("the next test data = " + staticSolution);
                    logger.debug("Convert to standard format");
                    ValueToTestcaseConverter_UnknownSize converter = new ValueToTestcaseConverter_UnknownSize(staticSolution);
                    List<RandomValue> randomValues = converter.convert();
                    logger.debug(randomValues);
                    List<String> valueList = new ArrayList<>(conditionParameters.getParameters().size());
                    List<Parameter> parameterList = conditionParameters.getParameters();
                    for (int i = 0; i < parameterList.size(); i++) {
                        for (RandomValue randomValue : randomValues) {
                            if (parameterList.get(i).getName().equals(randomValue.getNameUsedInExpansion())) {
                                valueList.add(randomValue.getValue());
                            }
                        }
                    }
                    conditionParameters.addTestData(valueList);
                }
            }
        }
    }


    private void createBound(IVariableNode variable) {
        String name = variable.getName();
        IFunctionConfigBound bound = boundMap.get(name);
        if (bound instanceof MultiplePrimitiveBound) {
            for (PrimitiveBound primitiveBound : ((MultiplePrimitiveBound) bound)) {
                String type = variable.getRealType();
                type = VariableTypeUtils.removeRedundantKeyword(type);
                Parameter parameter = parameterMap.get(name);
                if (parameter == null)
                    parameter = new Parameter(variable, type);

                if (VariableTypeUtils.isNumBasicInteger(type)
                        || VariableTypeUtils.isStdInt(type)) {
                    logger.debug(type + ": get long boundary values");
                    try {
                        parameter.getBounds().add(primitiveBound.getLower());
                        parameter.getBounds().add(primitiveBound.getUpper());
                        parameter.setNorm(String.valueOf(
                                (Long.parseLong(primitiveBound.getLower()) + Long.parseLong(primitiveBound.getUpper()))
                                        / 2));
                        ;
                        switch (primitiveBound.toString()) {
                            case "[0..255]":
                                parameter.getRepresentativesRange().add(new Pair<String, String>("128", "253"));
                                break;
                            case "[0..65535]":
                                parameter.getRepresentativesRange().add(new Pair<String, String>("256", "65533"));
                                break;
                            case "[0..4294967295]":
                                parameter.getRepresentativesRange().add(new Pair<String, String>("65536", "4294967295"));
                                break;
                            case "[-128..127]":
                                parameter.getRepresentativesRange().add(new Pair<String, String>("-126", "-1"));
                                parameter.getRepresentativesRange().add(new Pair<String, String>("2", "125"));
                                break;
                            case "[-32768..32767]":
                                parameter.getRepresentativesRange().add(new Pair<String, String>("-32766", "-129"));
                                parameter.getRepresentativesRange().add(new Pair<String, String>("255", "32765"));
                                break;
                            case "[-2147483648..2147483647]":
                                parameter.getRepresentativesRange().add(new Pair<String, String>("-2147483646", "-32769"));
                                parameter.getRepresentativesRange().add(new Pair<String, String>("65536", "2147483645"));
                                break;

                        }
                    } catch (NumberFormatException e) {

                    }


                } else if (VariableTypeUtils.isNumBasicFloat(type)) {
                    logger.debug(type + ": get double boundary values");
                    final double delta = new WorkspaceConfig().fromJson()
                            .getDefaultFunctionConfig().getFloatAndDoubleDelta();
                    try {
                        parameter.getBounds().add(primitiveBound.getLower());
                        parameter.getBounds().add(primitiveBound.getUpper());


                    } catch (NumberFormatException e) {

                    }

                } else if (VariableTypeUtils.isBoolBasic(type)) {
                    parameter.getBounds().add("0");
                    parameter.getBounds().add("1");
                } else if (VariableTypeUtils.isChBasic(type)) {
                    parameter.getBounds().add(primitiveBound.getLower());
                    parameter.getBounds().add(primitiveBound.getUpper());
                }

                parameterMap.put(name, parameter);
            }
        }
    }

    private void appendMap(IVariableNode variable, IASTExpression expr) {
        String name = variable.getName();
        IFunctionConfigBound bound = boundMap.get(name);
        if (bound instanceof MultiplePrimitiveBound) {
            for (PrimitiveBound primitiveBound : ((MultiplePrimitiveBound) bound)) {
                String type = variable.getRealType();
                type = VariableTypeUtils.removeRedundantKeyword(type);
                Parameter parameter = parameterMap.get(name);
                if (parameter == null)
                    parameter = new Parameter(variable, type);

                String valStr = expr.getRawSignature();
                if (VariableTypeUtils.isNumBasicInteger(type)
                        || VariableTypeUtils.isStdInt(type)) {
                    logger.debug(type + ": get long boundary values");
                    try {
                        long val = Long.parseLong(valStr);
                        parameter.getBounds().add(primitiveBound.getLower());
                        parameter.getBounds().add(String.valueOf(val - 1));
                        parameter.getBounds().add(valStr);
                        parameter.getBounds().add(String.valueOf(val + 1));
                        parameter.getBounds().add(primitiveBound.getUpper());
                    } catch (NumberFormatException e) {

                    }


                } else if (VariableTypeUtils.isNumBasicFloat(type)) {
                    logger.debug(type + ": get double boundary values");
                    final double delta = new WorkspaceConfig().fromJson()
                            .getDefaultFunctionConfig().getFloatAndDoubleDelta();
                    try {
                        double val = Double.parseDouble(valStr);
                        parameter.getBounds().add(primitiveBound.getLower());
                        parameter.getBounds().add(String.valueOf(val - delta));
                        parameter.getBounds().add(valStr);
                        parameter.getBounds().add(String.valueOf(val + delta));
                        parameter.getBounds().add(primitiveBound.getUpper());
                    } catch (NumberFormatException e) {

                    }

                } else if (VariableTypeUtils.isBoolBasic(type)) {
                    parameter.getBounds().add("0");
                    parameter.getBounds().add("1");
                } else if (VariableTypeUtils.isChBasic(type)) {
                    try {
                        long val = Long.parseLong(valStr);
                        parameter.getBounds().add(valStr);
                        parameter.getBounds().add(String.valueOf(val - 1));
                        parameter.getBounds().add(String.valueOf(val + 1));
                    } catch (NumberFormatException e) {
                        if (valStr.length() == 1) {
                            int val = valStr.charAt(0);
                            parameter.getBounds().add(primitiveBound.getLower());
                            parameter.getBounds().add(String.valueOf(val));
                            parameter.getBounds().add(String.valueOf(val - 1));
                            parameter.getBounds().add(String.valueOf(val + 1));
                            parameter.getBounds().add(primitiveBound.getUpper());
                        }
                    }
                }

                parameterMap.put(name, parameter);
            }

        }

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

        for (ConditionParameters conditionParameter : conditionParameterLíst) {
            for (List<String> value : conditionParameter.getValues()) {
                List<RandomValue> valuesOfArguments = new ArrayList<>();
                for (Parameter parameter : conditionParameter.getParameters()) {
                    RandomValueForAssignment valuei = new RandomValueForAssignment(
                            parameter.getName(),
                            value.get(conditionParameter.getParameters().indexOf(parameter)));
                    valuesOfArguments.add(valuei);
                }
                for (Parameter other : parameterMap.values()) {
                    if (!conditionParameter.getParameters().contains(other)) {
                        RandomValueForAssignment otherValue = new RandomValueForAssignment(
                                other.getName(), other.getNorm());
                        valuesOfArguments.add(otherValue);
                    }
                }
                createTestCase(valuesOfArguments);
            }


        }

//        for (String name : parameterMap.keySet()) { // for each argument or global variable
//            Parameter parameter = parameterMap.get(name);
//            // STEP 1: create a set of a bound value of the argument (or global variable)
//            // and norm values of others
//            for (String boundValue : parameter.getBounds()) {
//                List<RandomValue> valuesOfArguments = new ArrayList<>();
//                RandomValueForAssignment value = new RandomValueForAssignment(name, boundValue);
//                valuesOfArguments.add(value);
//                for (Parameter other : parameterMap.values()) {
//                    if (other != parameter) {
//                        RandomValueForAssignment otherValue = new RandomValueForAssignment(
//                                other.getName(), other.getNorm());
//                        valuesOfArguments.add(otherValue);
//                    }
//                }
//                createTestCase(valuesOfArguments);
//            }
//        }

        // create norm test case
//        List<RandomValue> valuesOfArguments = new ArrayList<>();
//        for (Parameter parameter : parameterMap.values()) {
//            RandomValueForAssignment otherValue = new RandomValueForAssignment(
//                    parameter.getName(), parameter.getNorm());
//            valuesOfArguments.add(otherValue);
//        }
//        createTestCase(valuesOfArguments);
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

    private class Expression {
        private final IASTExpression expression;
        private String max;
        private String min;
        private final Set<Parameter> expParamSet = new HashSet<>();

        private Expression(IASTExpression expression, final List<IVariableNode> variableNodes) {
            this.expression = expression;
            max = calcMax(this.expression, variableNodes);
            min = calcMin(this.expression, variableNodes);
        }

        private String calcMax(IASTExpression expression, final List<IVariableNode> variableNodes) {
            if (expression instanceof IASTBinaryExpression) {
                IASTExpression operand1 = ((IASTBinaryExpression) expression).getOperand1();
                String valueStr1 = calcMax(operand1, variableNodes);
                long a = Long.parseLong(valueStr1);
                IASTExpression operand2 = ((IASTBinaryExpression) expression).getOperand2();
                int operator = ((IASTBinaryExpression) expression).getOperator();
                String valueStr2;
                long b;
                switch (operator) {
                    case IASTBinaryExpression.op_plus:
                        valueStr2 = calcMax(operand2, variableNodes);
                        b = Long.parseLong(valueStr2);
                        return String.valueOf(a + b);
                    case IASTBinaryExpression.op_minus:
                        valueStr2 = calcMin(operand2, variableNodes);
                        b = Long.parseLong(valueStr2);
                        return String.valueOf(a - b);
                    default:
                        break;
                }

            } else if (expression instanceof IASTUnaryExpression) {
                IASTExpression operand = ((IASTUnaryExpression) expression).getOperand();
                int operator = ((IASTUnaryExpression) expression).getOperator();
                String valueStr;
                long b;
                switch (operator) {
                    case IASTUnaryExpression.op_plus:
                    case IASTUnaryExpression.op_bracketedPrimary:
                        valueStr = calcMax(operand, variableNodes);
                        b = Long.parseLong(valueStr);
                        return String.valueOf(b);
                    case IASTUnaryExpression.op_minus:
                        valueStr = calcMin(operand, variableNodes);
                        b = Long.parseLong(valueStr);
                        return String.valueOf(-b);
                    case IASTUnaryExpression.op_prefixIncr:
                        valueStr = calcMax(operand, variableNodes);
                        b = Long.parseLong(valueStr);
                        return String.valueOf(b + 1);
                    case IASTUnaryExpression.op_prefixDecr:
                        valueStr = calcMax(operand, variableNodes);
                        b = Long.parseLong(valueStr);
                        return String.valueOf(b - 1);
                }
            } else if (expression instanceof IASTIdExpression) {
                IVariableNode variableNode = getParameter(expression, variableNodes);
                if (variableNode != null) {
                    String name = variableNode.getName();
                    IFunctionConfigBound bound = boundMap.get(name);
                    if (bound instanceof MultiplePrimitiveBound) {
                        for (PrimitiveBound primitiveBound : ((MultiplePrimitiveBound) bound)) {
                            String type = variableNode.getRealType();
                            type = VariableTypeUtils.removeRedundantKeyword(type);
                            Parameter parameter = parameterMap.get(name);
                            if (parameter == null)
                                parameter = new Parameter(variableNode, type);

                            if (VariableTypeUtils.isNumBasicInteger(type)
                                    || VariableTypeUtils.isStdInt(type)) {
                                logger.debug(type + ": get long boundary values");
                                try {
                                    parameter.getBounds().add(primitiveBound.getLower());
                                    parameter.getBounds().add(primitiveBound.getUpper());
                                    parameter.setNorm(String.valueOf(
                                            (Long.parseLong(primitiveBound.getLower()) + Long.parseLong(primitiveBound.getUpper()))
                                                    / 2));
                                    ;
                                    switch (primitiveBound.toString()) {
                                        case "[0..255]":
                                            parameter.getRepresentativesRange().add(new Pair<String, String>("128", "253"));
                                            break;
                                        case "[0..65535]":
                                            parameter.getRepresentativesRange().add(new Pair<String, String>("256", "65533"));
                                            break;
                                        case "[0..4294967295]":
                                            parameter.getRepresentativesRange().add(new Pair<String, String>("65536", "4294967295"));
                                            break;
                                        case "[-128..127]":
                                            parameter.getRepresentativesRange().add(new Pair<String, String>("-126", "-1"));
                                            parameter.getRepresentativesRange().add(new Pair<String, String>("2", "125"));
                                            break;
                                        case "[-32768..32767]":
                                            parameter.getRepresentativesRange().add(new Pair<String, String>("-32766", "-129"));
                                            parameter.getRepresentativesRange().add(new Pair<String, String>("255", "32765"));
                                            break;
                                        case "[-2147483648..2147483647]":
                                            parameter.getRepresentativesRange().add(new Pair<String, String>("-2147483646", "-32769"));
                                            parameter.getRepresentativesRange().add(new Pair<String, String>("65536", "2147483645"));
                                            break;

                                    }
                                } catch (NumberFormatException e) {

                                }


                            } else if (VariableTypeUtils.isNumBasicFloat(type)) {
                                logger.debug(type + ": get double boundary values");
                                final double delta = new WorkspaceConfig().fromJson()
                                        .getDefaultFunctionConfig().getFloatAndDoubleDelta();
                                try {
                                    parameter.getBounds().add(primitiveBound.getLower());
                                    parameter.getBounds().add(primitiveBound.getUpper());


                                } catch (NumberFormatException e) {

                                }

                            } else if (VariableTypeUtils.isBoolBasic(type)) {
                                parameter.getBounds().add("0");
                                parameter.getBounds().add("1");
                            } else if (VariableTypeUtils.isChBasic(type)) {
                                parameter.getBounds().add(primitiveBound.getLower());
                                parameter.getBounds().add(primitiveBound.getUpper());
                            }

                            parameterMap.put(name, parameter);
                            expParamSet.add(parameter);
                            return primitiveBound.getUpper();
                        }
                    }
                }
            } else if (expression instanceof IASTLiteralExpression) {
                return expression.getRawSignature();
            }
            return null;
        }

        private String calcMin(IASTExpression expression, final List<IVariableNode> variableNodes) {
            if (expression instanceof IASTBinaryExpression) {
                IASTExpression operand1 = ((IASTBinaryExpression) expression).getOperand1();
                String valueStr1 = calcMin(operand1, variableNodes);
                long a = Long.parseLong(valueStr1);
                IASTExpression operand2 = ((IASTBinaryExpression) expression).getOperand2();
                int operator = ((IASTBinaryExpression) expression).getOperator();
                String valueStr2;
                long b;
                switch (operator) {
                    case IASTBinaryExpression.op_plus:
                        valueStr2 = calcMin(operand2, variableNodes);
                        b = Long.parseLong(valueStr2);
                        return String.valueOf(a + b);
                    case IASTBinaryExpression.op_minus:
                        valueStr2 = calcMax(operand2, variableNodes);
                        b = Long.parseLong(valueStr2);
                        return String.valueOf(a - b);
                    default:
                        break;
                }

            } else if (expression instanceof IASTUnaryExpression) {
                IASTExpression operand = ((IASTUnaryExpression) expression).getOperand();
                int operator = ((IASTUnaryExpression) expression).getOperator();
                String valueStr;
                long b;
                switch (operator) {
                    case IASTUnaryExpression.op_plus:
                    case IASTUnaryExpression.op_bracketedPrimary:
                        valueStr = calcMin(operand, variableNodes);
                        b = Long.parseLong(valueStr);
                        return String.valueOf(b);
                    case IASTUnaryExpression.op_minus:
                        valueStr = calcMax(operand, variableNodes);
                        b = Long.parseLong(valueStr);
                        return String.valueOf(-b);
                    case IASTUnaryExpression.op_prefixIncr:
                        valueStr = calcMin(operand, variableNodes);
                        b = Long.parseLong(valueStr);
                        return String.valueOf(b + 1);
                    case IASTUnaryExpression.op_prefixDecr:
                        valueStr = calcMin(operand, variableNodes);
                        b = Long.parseLong(valueStr);
                        return String.valueOf(b - 1);
                }
            } else if (expression instanceof IASTIdExpression) {
                IVariableNode variableNode = getParameter(expression, variableNodes);
                if (variableNode != null) {
                    String name = variableNode.getName();
                    IFunctionConfigBound bound = boundMap.get(name);
                    if (bound instanceof MultiplePrimitiveBound) {
                        for (PrimitiveBound primitiveBound : ((MultiplePrimitiveBound) bound)) {
                            String type = variableNode.getRealType();
                            type = VariableTypeUtils.removeRedundantKeyword(type);
                            Parameter parameter = parameterMap.get(name);
                            if (parameter == null)
                                parameter = new Parameter(variableNode, type);

                            if (VariableTypeUtils.isNumBasicInteger(type)
                                    || VariableTypeUtils.isStdInt(type)) {
                                logger.debug(type + ": get long boundary values");
                                try {
                                    parameter.getBounds().add(primitiveBound.getLower());
                                    parameter.getBounds().add(primitiveBound.getUpper());
                                    parameter.setNorm(String.valueOf(
                                            (Long.parseLong(primitiveBound.getLower()) + Long.parseLong(primitiveBound.getUpper()))
                                                    / 2));
                                    ;
                                    switch (primitiveBound.toString()) {
                                        case "[0..255]":
                                            parameter.getRepresentativesRange().add(new Pair<String, String>("128", "253"));
                                            break;
                                        case "[0..65535]":
                                            parameter.getRepresentativesRange().add(new Pair<String, String>("256", "65533"));
                                            break;
                                        case "[0..4294967295]":
                                            parameter.getRepresentativesRange().add(new Pair<String, String>("65536", "4294967295"));
                                            break;
                                        case "[-128..127]":
                                            parameter.getRepresentativesRange().add(new Pair<String, String>("-126", "-1"));
                                            parameter.getRepresentativesRange().add(new Pair<String, String>("2", "125"));
                                            break;
                                        case "[-32768..32767]":
                                            parameter.getRepresentativesRange().add(new Pair<String, String>("-32766", "-129"));
                                            parameter.getRepresentativesRange().add(new Pair<String, String>("255", "32765"));
                                            break;
                                        case "[-2147483648..2147483649]":
                                            parameter.getRepresentativesRange().add(new Pair<String, String>("-2147483646", "-32769"));
                                            parameter.getRepresentativesRange().add(new Pair<String, String>("65536", "2147483647"));
                                            break;

                                    }
                                } catch (NumberFormatException e) {

                                }


                            } else if (VariableTypeUtils.isNumBasicFloat(type)) {
                                logger.debug(type + ": get double boundary values");
                                final double delta = new WorkspaceConfig().fromJson()
                                        .getDefaultFunctionConfig().getFloatAndDoubleDelta();
                                try {
                                    parameter.getBounds().add(primitiveBound.getLower());
                                    parameter.getBounds().add(primitiveBound.getUpper());


                                } catch (NumberFormatException e) {

                                }

                            } else if (VariableTypeUtils.isBoolBasic(type)) {
                                parameter.getBounds().add("0");
                                parameter.getBounds().add("1");
                            } else if (VariableTypeUtils.isChBasic(type)) {
                                parameter.getBounds().add(primitiveBound.getLower());
                                parameter.getBounds().add(primitiveBound.getUpper());
                            }

                            parameterMap.put(name, parameter);
                            expParamSet.add(parameter);
                            return primitiveBound.getLower();
                        }
                    }
                }
            } else if (expression instanceof IASTLiteralExpression) {
                return expression.getRawSignature();
            }
            return null;
        }

        public IASTExpression getOperand() {
            return expression;
        }

        public Set<Parameter> getExpParamSet() {
            return expParamSet;
        }

        public String getMax() {
            return max;
        }

        public String getMin() {
            return min;
        }
    }

    private class ConditionParameters {
        private final List<Parameter> parameters;
        private final List<List<String>> values = new ArrayList<>();

        private ConditionParameters() {
            this.parameters = new ArrayList<>();
        }

        private ConditionParameters(List<Parameter> parameters) {
            this.parameters = parameters;
        }

        public ArrayList<Parameter> getParameters() {
            return (ArrayList<Parameter>) parameters;
        }

        public Parameter getParameter(int index) {
            return parameters.get(index);
        }

        public List<List<String>> getValues() {
            return values;
        }

        public void addTestData(List<String> testData) {
            values.add(testData);
        }

        public void addTestData(String value1, String value2) {
            values.add(new ArrayList<>(Arrays.asList(value1, value2)));
        }
    }


    private class Parameter {
        private final IVariableNode variableNode;
        private final String type;
        private String norm;
        private final List<String> bounds = new ArrayList<>();
        private final List<Pair<String, String>> representativesRange = new ArrayList<>();

        public Parameter(IVariableNode variableNode, String type) {
            this.variableNode = variableNode;
            this.type = type;
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

        public List<Pair<String, String>> getRepresentativesRange() {
            return representativesRange;
        }

        public String getName() {
            return variableNode.getName();
        }

        public String getType() {
            return type;
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
