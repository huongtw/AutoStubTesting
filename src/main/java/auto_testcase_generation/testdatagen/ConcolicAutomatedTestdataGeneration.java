package auto_testcase_generation.testdatagen;

import auto_testcase_generation.cfg.ICFG;
import auto_testcase_generation.cfg.object.*;
import auto_testcase_generation.testdatagen.testdatainit.CompleteIteratingThroughLoop;
import com.dse.config.AkaConfig;
import com.dse.config.WorkspaceConfig;
import com.dse.coverage.CoverageDataObject;
import com.dse.coverage.CoverageManager;
import com.dse.coverage.SourcecodeCoverageComputation;
import com.dse.environment.Environment;
import com.dse.environment.object.EnviroCoverageTypeNode;
import com.dse.guifx_v3.controllers.TestCasesExecutionTabController;
import com.dse.guifx_v3.controllers.TestCasesNavigatorController;
import com.dse.guifx_v3.helps.TCExecutionDetailLogger;
import com.dse.guifx_v3.helps.UIController;
import com.dse.guifx_v3.helps.UILogger;
import com.dse.guifx_v3.objects.TestCaseExecutionDataNode;
import com.dse.logger.AkaLogger;
import com.dse.parser.object.*;
import com.dse.testcase_execution.TestcaseExecution;
import com.dse.testcase_execution.result_trace.AssertionResult;
import com.dse.testcase_manager.ITestCase;
import com.dse.testcase_manager.TestCase;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.testcase_manager.TestPrototype;
import com.dse.testcase_manager.minimize.GreedyMinimizer;
import com.dse.testcase_manager.minimize.ITestCaseMinimizer;
import com.dse.testcase_manager.minimize.Scope;
import com.dse.testdata.gen.module.SimpleTreeDisplayer;
import com.dse.testdata.object.RootDataNode;
import com.dse.util.CFGUtils;
import com.dse.util.Utils;
import com.dse.util.VariableTypeUtils;
import javafx.collections.ObservableList;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public abstract class ConcolicAutomatedTestdataGeneration extends SymbolicExecutionTestdataGeneration {

    private final static AkaLogger logger = AkaLogger.get(ConcolicAutomatedTestdataGeneration.class);

    public ConcolicAutomatedTestdataGeneration(ICommonFunctionNode fn, String coverageType) {
        super(fn, coverageType);
    }

    protected List<TestCase> generateInitialTestCases() {
        try {
            RandomAutomatedTestdataGeneration gen = new RandomAutomatedTestdataGeneration(fn);
            gen.setLimitNumberOfIterations(5);
            gen.setShouldRunParallel(false);
            gen.setShowReport(false);
            gen.getAllPrototypes().addAll(allPrototypes);
            gen.generateTestdata(fn);
            return gen.getTestCases();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Generate test data
     */
    protected void start(List<TestCase> testCases, ICommonFunctionNode fn, String coverageType,
                         List<TestPrototype> allPrototypes,
                         List<String> generatedTestcases,
                         List<String> analyzedTestpathMd5,
                         boolean showReport) {
        List<String> oldTestcases = new ArrayList<>();
        for (TestCase tc : testCases)
            if (tc != null && tc.getPath() != null)
                oldTestcases.add(tc.getPath());

        /*
         * Generate randomly (not in parallel mode)
         */
        logger.debug("Random test data generation");
        List<TestCase> initTestCases = generateInitialTestCases();
        testCases.addAll(initTestCases);

        /*
         * Generate directly
         */
        if (new File(new AkaConfig().fromJson().getZ3Path()).exists()) {
            long MAX_TESTCASES = fn.getFunctionConfig().getTheMaximumNumberOfIterations();
            int time=0;
            for (long i = 0; i < MAX_TESTCASES;) {
                logger.debug("Iterate " + i + " directly");
                logger.debug("Num of the existing test cases up to now = " + testCases.size());
                int iterationStatus = generateDirectly(testCases, fn, coverageType, generatedTestcases, analyzedTestpathMd5);
                time++;

                switch (iterationStatus) {
                    case AUTOGEN_STATUS.OTHER_ERRORS: {
                        logger.debug("Unexpected error when generating test case. Nove to the next iteration.");
                        i++;
                        break;
                    }
                    case AUTOGEN_STATUS.NOT_ABLE_TO_GENERATE_CFG: {
                        logger.debug("Unable to generate cfg of function " + fn.getAbsolutePath() + ". Move to the next iteration.");
                        i++;
                        break;
                    }
                    case AUTOGEN_STATUS.NO_SHORTEST_PATH: {
                        logger.debug("Not path to discover. Exit.");
                        i++;
                        break;
                    }
                    case AUTOGEN_STATUS.FOUND_DUPLICATED_TESTPATH: {
                        logger.debug("The generated test path existed before. Move to the next iteration.");
                        i++;
                        break;
                    }
                    case AUTOGEN_STATUS.SOLVING_STATUS.FOUND_DUPLICATED_TESTCASE: {
                        logger.debug("The generated test case existed before. Move to the next iteration.");
                        i++;
                        break;
                    }
                    case AUTOGEN_STATUS.EXECUTION.COULD_NOT_CONSTRUCT_TREE_FROM_TESTCASE: {
                        logger.debug("Could not construct tree from generated test case. Move to the next iteration.");
                        i++;
                        break;
                    }
                    case AUTOGEN_STATUS.EXECUTION.COUND_NOT_EXECUTE_TESTCASE: {
                        logger.debug("Could not execute test case. Move to the text iteration.");
                        i++;
                        break;
                    }
                    case AUTOGEN_STATUS.EXECUTION.BE_ABLE_TO_EXECUTE_TESTCASE: {
                        logger.debug("Be able to execute test case. Save. Move to the text iteration.");
                        TestCasesNavigatorController.getInstance().refreshNavigatorTreeFromAnotherThread();
                        break;
                    }
                }
            }
            logger.debug("TIME: "+ time);
        }

        // optimize the number of test cases
        try {
            // remove the generated test cases before auto gen
            for (int i = testCases.size() - 1; i >= 0; i--)
                if (oldTestcases.contains(testCases.get(i).getPath())) {
                    testCases.remove(i);
                }

            List<TestCase> newTestCases = testCases.stream()
                    .filter(tc -> tc.getPath() != null && new File(tc.getPath()).exists())
                    .collect(Collectors.toList());

            ITestCaseMinimizer minimizer = new GreedyMinimizer();
//            minimizer.clean(newTestCases, Scope.SOURCE);

            onGenerateSuccess(showReport);

        } catch (Exception ex) {
            ex.printStackTrace();
//            int nTestcases = TestCaseManager.getTestCasesByFunction(fn).size();
//            UILogger.getUiLogger().info("[DONE] Generate test cases for function " + fn.getName() + ". Total of test cases in this function: " + nTestcases);
        }
    }

    /**
     * @param testCases           a list of existing test cases
     * @param functionNode        the tested function
     * @param cov                 the target coverage
     * @param generatedTestcases  the generated test cases
     * @param analyzedTestpathMd5 md5 of analyzed test paths
     * @return status of test case generation
     */
    protected int generateDirectly(List<TestCase> testCases, ICommonFunctionNode functionNode, String cov,
                                   List<String> generatedTestcases,
                                   List<String> analyzedTestpathMd5) {
        // compute coverage & update CFG
        ICFG currentCFG = updateCFG(testCases, functionNode, cov);

        if (currentCFG != null) {
            List<ICfgNode> testPath = getTestPathByCoverage(currentCFG, cov);

            if (testPath.isEmpty())
                return AUTOGEN_STATUS.NO_SHORTEST_PATH;
            else {
                return solve(testPath, functionNode, generatedTestcases, analyzedTestpathMd5);
            }
        } else
            return AUTOGEN_STATUS.NOT_ABLE_TO_GENERATE_CFG;
    }

//    protected void generateDirectlyMoreForLoop(List<TestCase> testCases, ICommonFunctionNode functionNode, String cov,
//                                          List<String> generatedTestcases, List<String> analyzedTestpathMd5) throws Exception {
//        for (int i = 1; i < 3; i++){
//            // compute coverage & update CFG
//            ICFG currentCFG = updateCFG(testCases, functionNode, cov);
//
//            if (currentCFG != null) {
//                List<ICfgNode> testPath = getTestPathByCoverage(currentCFG, cov, i);
//
//                if (testPath.isEmpty())
//                    logger.debug("AUTOGEN_STATUS.NO_SHORTEST_PATH");
//                else
//                    solve(testPath, functionNode, generatedTestcases, analyzedTestpathMd5);
//            } else
//                logger.debug("AUTOGEN_STATUS.NOT_ABLE_TO_GENERATE_CFG;");
//        }
//    }

    protected ICFG updateCFG(List<TestCase> testCases, ICommonFunctionNode functionNode, String cov) {
        ICFG currentCFG = null;

        String allTestpaths = "";
        for (TestCase testCase : testCases)
            if (testCase.getTestPathFile() != null && new File(testCase.getTestPathFile()).exists())
                allTestpaths += Utils.readFileContent(testCase.getTestPathFile()) + "\n";

        if (allTestpaths.length() > 0) {
            /*
             * Compute coverage of all test cases up to now
             */
            logger.debug("Start computing total coverage");
            ISourcecodeFileNode sourcecodeNode = Utils.getSourcecodeFile(functionNode);
            SourcecodeCoverageComputation sourcecodeCoverageComputation = new SourcecodeCoverageComputation();
            sourcecodeCoverageComputation.setCoverage(cov);
            sourcecodeCoverageComputation.setConsideredSourcecodeNode(sourcecodeNode);
            sourcecodeCoverageComputation.setTestpathContent(allTestpaths);
            sourcecodeCoverageComputation.compute();
            logger.debug("Total coverage computation... DONE");

            /*
             * Get cfg corresponding to the tested function
             */
            List<ICFG> CFGs = sourcecodeCoverageComputation.getAllCFG();
            for (ICFG cfg : CFGs) {
                if (cfg.getFunctionNode().getAbsolutePath().equals(functionNode.getAbsolutePath())) {
                    currentCFG = cfg;
                    break;
                }
            }
        } else {
            /*
             * There is no previous test case, just need to construct a cfg
             */
            try {
                if (functionNode instanceof MacroFunctionNode) {
                    IFunctionNode tmpFunctionNode = ((MacroFunctionNode) functionNode).getCorrespondingFunctionNode();
                    currentCFG = CFGUtils.createCFG(tmpFunctionNode, cov);
                    currentCFG.setFunctionNode(tmpFunctionNode);
                } else if (functionNode instanceof AbstractFunctionNode) {
                    currentCFG = CFGUtils.createCFG((IFunctionNode) functionNode, cov);
                    currentCFG.setFunctionNode((IFunctionNode) functionNode);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return currentCFG;
    }

    @Override
    protected String getTestCaseNamePrefix(ICommonFunctionNode fn)  {
        return fn.getSimpleName() + ITestCase.POSTFIX_TESTCASE_BY_DIRECTED_METHOD;
    }

    protected abstract List<ICfgNode> getTestPathByCoverage(ICFG currentCFG, String coverageType);

    public static String toDescription(ICfgNode node) {
        return String.format("%s(id = %s)", node.getContent(), node.getId());
    }

    protected boolean haveLoop(List<ICfgNode> path) {
        return path.stream().anyMatch(n -> n instanceof AbstractConditionLoopCfgNode);
    }

    protected List<ICfgNode> completePathForFixedLoop(final List<ICfgNode> shortestPath,
                                                      DijkstraShortestPath dijkstraAlg,
                                                      Map<String, ICfgNode> mapping) {
        final List<ICfgNode> clonePath = new ArrayList<>(shortestPath);
        try {
            return CompleteIteratingThroughLoop
                    .randomIterateForLoop(fn, dijkstraAlg, mapping, clonePath, 0, shortestPath.size() - 1, 0, null);
        } catch (Exception e) {
            logger.error("Can't generate complete path for loop", e);
            return shortestPath;
        }
    }
}