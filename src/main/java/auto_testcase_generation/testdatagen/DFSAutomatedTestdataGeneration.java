package auto_testcase_generation.testdatagen;

import auto_testcase_generation.cfg.ICFG;
import auto_testcase_generation.cfg.object.ConditionCfgNode;
import auto_testcase_generation.cfg.object.ICfgNode;
import auto_testcase_generation.cfg.testpath.TestPathRetriever;
import com.dse.logger.AkaLogger;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.testcase_manager.ITestCase;
import com.dse.testcase_manager.TestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class DFSAutomatedTestdataGeneration extends ConcolicAutomatedTestdataGeneration {

    private final static AkaLogger logger = AkaLogger.get(DFSAutomatedTestdataGeneration.class);

    private Stack<ICfgNode> dfsStack;

    private boolean isHaveNewTestCase = true;

    public DFSAutomatedTestdataGeneration(ICommonFunctionNode fn, String coverageType) {
        super(fn, coverageType);
    }

    @Override
    protected List<TestCase> generateInitialTestCases() {
        List<TestCase> initTestCases = new ArrayList<>();

        boolean isGenSuccess = false;

        int runtimeErr = 0;
        int compileErr = 0;

        while (!isGenSuccess && runtimeErr < 5 && compileErr < 5) {
            try {
                logger.debug("Start generating randomly a test case.");
                RandomAutomatedTestdataGeneration gen = new RandomAutomatedTestdataGeneration(fn);
                gen.setLimitNumberOfIterations(1);
                gen.setShouldRunParallel(false);
                gen.setShowReport(false);
                gen.getAllPrototypes().addAll(allPrototypes);
                gen.generateTestdata(fn);
                if (!gen.getTestCases().isEmpty()) {
                    TestCase testCase = gen.getTestCases().get(0);
                    String status = testCase.getStatus();
                    if (status.equals(ITestCase.STATUS_SUCCESS)) {
                        isGenSuccess = true;
                        initTestCases.add(testCase);
                    } else {
                        if (status.equals(ITestCase.STATUS_RUNTIME_ERR)) {
                            logger.debug("Execution returns a runtime error. Retry generating a test case.");
                            runtimeErr++;
                        } else if (status.equals(ITestCase.STATUS_FAILED)) {
                            logger.debug("Execution returns a compile error. Retry generating a test case.");
                            compileErr++;
                        }
                        initTestCases.add(0, testCase);
                    }
                }
            } catch (Exception e) {
                logger.error("Can't randomly generate a test case", e);
            }
        }

        return initTestCases;
    }

    @Override
    protected int generateDirectly(List<TestCase> testCases, ICommonFunctionNode functionNode, String cov,
                                        List<String> generatedTestcases, List<String> analyzedTestpathMd5) {
        // update dfs stack
        if (isHaveNewTestCase){
            dfsStack = new TestPathRetriever(testCases.get(testCases.size() - 1)).get();
            if (dfsStack == null) dfsStack = new Stack<>();
        } else {
            if (dfsStack.size() > 0) {
                while (!(dfsStack.peek() instanceof ConditionCfgNode))
                    dfsStack.pop();
                if (dfsStack.size() > 0)
                    dfsStack.pop();
            }
        }

        logger.debug("Stack: " + dfsStack);

        // compute coverage & update CFG
        ICFG currentCFG = updateCFG(testCases, functionNode, cov);

        if (currentCFG != null) {
            List<ICfgNode> newTestPath = getTestPathByCoverage(currentCFG, cov);
            logger.debug("Next test path: " + newTestPath);

            if (newTestPath.isEmpty()) {
                return AUTOGEN_STATUS.NO_SHORTEST_PATH;
            } else {
                int flag = solve(newTestPath, functionNode, generatedTestcases, analyzedTestpathMd5);
                isHaveNewTestCase = flag == AUTOGEN_STATUS.EXECUTION.BE_ABLE_TO_EXECUTE_TESTCASE;
                return flag;
            }
        } else
            return AUTOGEN_STATUS.NOT_ABLE_TO_GENERATE_CFG;
    }

    @Override
    protected List<ICfgNode> getTestPathByCoverage(ICFG currentCFG, String coverageType) {
        currentCFG.setIdforAllNodes();

        List<ICfgNode> backupStack = new ArrayList<>(dfsStack);
        List<ICfgNode> testpath = new ArrayList<>();

        // get the nearest ConditionCfgNode which wasn't visited their own all branches
        while (!dfsStack.isEmpty()){
            if (isVisitedAllBranch(dfsStack.peek()))
                dfsStack.pop();
            else
                break;
        }

        if (dfsStack.isEmpty()) {
            if (!backupStack.isEmpty()) {
                ICfgNode topNode = backupStack.get(backupStack.size() - 1);
                ICfgNode unvisitedInstruction = getNextUnvisitedNode(topNode);
                if (unvisitedInstruction != null) {
                    testpath.addAll(backupStack);
                    testpath.add(unvisitedInstruction);
                    return testpath;
                }
            }

            maximizeCov = true;
        } else {
            ICfgNode topNode = dfsStack.peek();
            ICfgNode unvisitedInstruction = getNextUnvisitedNode(topNode);
            if (isVisitedAllBranch(topNode))
                dfsStack.pop();

            testpath.addAll(dfsStack);
            testpath.add(unvisitedInstruction);

            return normalizePath(testpath);
        }

        return testpath;
    }

    /**
     * Check if a decision node visit all branch
     * @param n a decision node
     * @return true if visit all branch, false otherwise
     */
    private boolean isVisitedAllBranch(ICfgNode n) {
        if (n instanceof ConditionCfgNode) {
            return ((ConditionCfgNode) n).isVisitedFalseBranch() && ((ConditionCfgNode) n).isVisitedTrueBranch();
        } else if (n.isMultipleTarget()) {
            return n.getListTarget().stream().allMatch(ICfgNode::isVisited);
        } else {
            return true;
        }
    }

    /**
     * Find next unvisited node
     * @param n a cfg node
     * @return next unvisited node
     */
    private ICfgNode getNextUnvisitedNode(ICfgNode n) {
        ICfgNode unvisitedInstruction = null;

        if (n.isMultipleTarget()) {
            unvisitedInstruction = n.getListTarget().stream()
                    .filter(i -> !i.isVisited())
                    .findFirst()
                    .orElse(null);
        } else if (n instanceof ConditionCfgNode) {
            if (!((ConditionCfgNode) n).isVisitedTrueBranch()) {
                unvisitedInstruction = n.getTrueNode();
            } else if (!((ConditionCfgNode) n).isVisitedFalseBranch()) {
                unvisitedInstruction = n.getFalseNode();
            }
        } else if (n.getTrueNode() != null && n.getFalseNode() != null) {
            if (!n.getTrueNode().isVisited())
                unvisitedInstruction = n.getTrueNode();
            else if (!n.getFalseNode().isVisited())
                unvisitedInstruction = n.getFalseNode();
        }

        return unvisitedInstruction;
    }

    protected String getTestCaseNamePrefix(ICommonFunctionNode fn) {
        return fn.getSimpleName() + ITestCase.POSTFIX_TESTCASE_BY_DIRECTED_METHOD;
    }
}
