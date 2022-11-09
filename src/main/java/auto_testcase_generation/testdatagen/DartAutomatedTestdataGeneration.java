//package auto_testcase_generation.testdatagen;
//
//import auto_testcase_generation.cfg.ICFG;
//import auto_testcase_generation.cfg.testpath.ITestpathInCFG;
//import auto_testcase_generation.testdata.object.TestpathString_Marker;
//import auto_testcase_generation.testdatagen.coverage.CFGUpdater_Mark;
//import auto_testcase_generation.testdatagen.se.ISymbolicExecution;
//import auto_testcase_generation.testdatagen.se.Parameter;
//import auto_testcase_generation.testdatagen.se.SymbolicExecution;
//import auto_testcase_generation.testdatagen.se.memory.ISymbolicVariable;
//import com.dse.config.IFunctionConfig;
//import com.dse.config.Paths;
//import com.dse.environment.object.EnviroCoverageTypeNode;
//import com.dse.environment.Environment;
//import com.dse.parser.object.IFunctionNode;
//import com.dse.util.Utils;
//import com.dse.logger.AkaLogger;
//
//@Deprecated
//public class DartAutomatedTestdataGeneration extends AbstractDirectedAutomatedTestdataGeneration {
//    private final static AkaLogger logger = AkaLogger.get(DartAutomatedTestdataGeneration.class);
//
//    public DartAutomatedTestdataGeneration(IFunctionNode fn) {
//        super(fn);
//    }
//
//
//    /**
//     * Step 1. Generate test data at random <br/>
//     * Step 2. Execute test data to get test path. If error occurs, back to Step 1,
//     * else Step 2 <br/>
//     * Step 3. Get constraints<br/>
//     * Step 4. Negate constraints (from the last constraint). If exist, back to Step
//     * 2, else back to step 1<br/>
//     *
//     * @param originalFunction
//     * @throws Exception
//     */
//    public void generateTestdata(IFunctionNode originalFunction) throws Exception {
//        // Config: no limit loop, no limit recursive
////        getExePath(originalFunction);
//        ICFG normalizedCfg = generateNormalizedCFG(originalFunction);
//
//        if (normalizedCfg != null) {
//
//            String testdata = "";
//            int iteration_in_one_depth;
//            int depth = 0;
//            float currentBranchCoverage = 0.0f;
//            float currentStatementCoverage = 0.0f;
//
//            logger.info("STRATEGY: DART");
//
//            float tmp_currentStatementCoverage = 0.0f;
//            float tmp_currentBranchCoverage = 0.0f;
//
//            float tmp_previous_currentStatementCoverage;
//            float tmp_previous_currentBranchCoverage;
//            Optimization optimization = new Optimization();
//
//            while (depth < DEPTH && currentBranchCoverage < 100f) {
//                logger.info("\n\n\n" + "====================<b>DEPTH " + depth
//                        + "</b>==========================================================");
//                iteration_in_one_depth = 0;
//
//                while (iteration_in_one_depth < MAX_ITERATIONS_IN_ONE_DEPTH && currentBranchCoverage < 100f) {
//                    logger.info("\n\n\n" + "---------------------ITERATION " + iteration_in_one_depth + " (DEPTH = "
//                            + depth + ")" + "-------------------------");
//                    iteration_in_one_depth++;
//                    /*
//                      STEP 1. INITIALIZE TEST DATA AT RANDOM
//                     */
//                    boolean tmp_isGenerateRandomly;
//                    if (testdata == null || testdata.length() == 0) {
//                        testdata = initializeTestdataAtRandom();
//                        logger.info("Generate a random test data: <b>" + testdata + "</b>");
//                        tmp_isGenerateRandomly = true;
//                    } else
//                        tmp_isGenerateRandomly = false;
//
//                    optimization.getGeneratedTestdata().add(testdata);
//                    /*
//                      STEP 2. EXECUTE TEST DATA TO GET TEST PATH
//                     */
//                    ITestdataExecution testdataExecution = executeTestdata(originalFunction, testdata);
//
//                    tmp_previous_currentBranchCoverage = tmp_currentBranchCoverage;
//                    tmp_previous_currentStatementCoverage = tmp_currentStatementCoverage;
//
//                    String fullTestdata = Utils.readFileContent(Paths.CURRENT_PROJECT.TESTDATA_INPUT_FILE_PATH)
//                            .replace("\n", "; ");
//                    if (testdataExecution != null) {
//                        // Update CFG
//                        CFGUpdater_Mark cfgUpdater = new CFGUpdater_Mark(testdataExecution.getEncodedTestpath(),
//                                normalizedCfg);
//
//                        currentNumOfVisitedBranches -= normalizedCfg.getVisitedBranches().size();
//
//                        // logger.debug("Previous Visited Nodes: " +
//                        // cfgUpdater.getPreviousVisitedNodes());
//                        // logger.debug("Previous Visited Branches: " +
//                        // cfgUpdater.getPreviousVisitedBranches());
//                        cfgUpdater.updateVisitedNodes();
//
//                        tmp_currentStatementCoverage = cfgUpdater.getCfg().computeStatementCoverage() * 100;
//                        logger.info("Current statement coverage = " + tmp_currentStatementCoverage);
//
//                        tmp_currentBranchCoverage = cfgUpdater.getCfg().computeBranchCoverage() * 100;
//                        logger.info("Current branch coverage = " + tmp_currentBranchCoverage);
//
//                        currentBranchCoverage = normalizedCfg.computeBranchCoverage() * 100;
//                        currentStatementCoverage = normalizedCfg.computeStatementCoverage() * 100;
//
//                        currentNumOfVisitedBranches += normalizedCfg.getVisitedBranches().size();
//                        AbstractAutomatedTestdataGeneration.visitedBranchesInfor
//                                .add(new Integer[]{++tmp_iterations, currentNumOfVisitedBranches});
//
//                        // CASE 1. The latest test path is incomplete (there
//                        // arises an error when the
//                        // test data executes)
//                        if (!cfgUpdater.isCompleteTestpath()) {
//                            logger.info("The testpath is uncomplete! We still update CFG");
//                            logger.info("Found a bug. Uncomplete test path");
//
//                            AbstractAutomatedTestdataGeneration.bugs.add(
//                                    new Bug(fullTestdata, testdataExecution.getEncodedTestpath(), originalFunction));
//
//                            AbstractAutomatedTestdataGeneration.testdata.add(new TestdataInReport(fullTestdata,
//                                    testdataExecution.getEncodedTestpath(), false, tmp_currentStatementCoverage,
//                                    tmp_currentBranchCoverage,
//                                    tmp_currentBranchCoverage > tmp_previous_currentBranchCoverage || (tmp_currentStatementCoverage > tmp_previous_currentStatementCoverage),
//                                    tmp_isGenerateRandomly, false));
//
//                            testdata = "";
//                        }
//                        // CASE 2. No errors during test data execution
//                        else {
//                            AbstractAutomatedTestdataGeneration.testdata.add(new TestdataInReport(fullTestdata,
//                                    testdataExecution.getEncodedTestpath(), true, tmp_currentStatementCoverage,
//                                    tmp_currentBranchCoverage,
//                                    tmp_currentBranchCoverage > tmp_previous_currentBranchCoverage || (tmp_currentStatementCoverage > tmp_previous_currentStatementCoverage),
//                                    tmp_isGenerateRandomly, false));
//
//                            switch (Environment.getInstance().getTypeofCoverage()) {
//
//                                case EnviroCoverageTypeNode.BRANCH:
//                                case EnviroCoverageTypeNode.BASIS_PATH:
//                                case EnviroCoverageTypeNode.STATEMENT:
//
//                                    ITestpathInCFG executedTestpath = cfgUpdater.getUpdatedCFGNodes();
//
//                                    /*
//                                      STEP 3. GET PATH CONSTRAINTS
//                                     */
//                                    if (currentBranchCoverage < 100f && executedTestpath != null) {
//                                        Parameter paramaters = new Parameter();
//                                        paramaters.addAll(originalFunction.getArguments());
//                                        paramaters.addAll(originalFunction.getReducedExternalVariables());
//
//                                        logger.info("Performing symbolic execution on this test path");
//                                        ISymbolicExecution se = new SymbolicExecution(executedTestpath, paramaters,
//                                                originalFunction);
//
//                                        logger.debug("Done. Constraints: \n" + se.getConstraints().toString()
//                                                .replace(ISymbolicVariable.PREFIX_SYMBOLIC_VALUE, "")
//                                                .replace(
//                                                        ISymbolicVariable.SEPARATOR_BETWEEN_STRUCTURE_NAME_AND_ITS_ATTRIBUTES,
//                                                        ".")
//                                                .replace(ISymbolicVariable.ARRAY_CLOSING, "]")
//                                                .replace(ISymbolicVariable.ARRAY_OPENING, "["));
//
//                                        /*
//                                          STEP 4. NEGATE PATH CONSTRAINTS
//                                         */
//                                        logger.info("NEGATE PATH CONSTRAINTS");
//                                        TheNextTestdata negatedInformation = generateTheNextTestdata(se, originalFunction,
//                                                optimization);
//                                        testdata = negatedInformation.getTestdata();
//                                    }
//                                    break;
//
//                                case EnviroCoverageTypeNode.MCDC:
//                                    throw new Exception("Dont code this kind of code coverage");
//                            }
//                        }
//                    } else {
//                        logger.debug("Current test data causes errors.");
//                        AbstractAutomatedTestdataGeneration.testdata.add(new TestdataInReport(fullTestdata,
//                                new TestpathString_Marker(), false, tmp_currentStatementCoverage,
//                                tmp_currentBranchCoverage, false, tmp_isGenerateRandomly, false));
//                    }
//                }
//                depth++;
//            }
//            AbstractAutomatedTestdataGeneration.statementCoverage = currentStatementCoverage;
//            AbstractAutomatedTestdataGeneration.branchCoverage = currentBranchCoverage;
//            AbstractAutomatedTestdataGeneration.numOfBranches += normalizedCfg.getUnvisitedBranches().size()
//                    + normalizedCfg.getVisitedBranches().size();
//            AbstractAutomatedTestdataGeneration.numOfVisitedBranches += normalizedCfg.getVisitedBranches().size();
//        }
//    }
//
//
//
//}
