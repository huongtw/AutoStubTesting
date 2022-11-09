//package auto_testcase_generation.testdatagen;
//
//import auto_testcase_generation.cfg.ICFG;
//import auto_testcase_generation.cfg.testpath.*;
//import auto_testcase_generation.testdata.object.TestpathString_Marker;
//import auto_testcase_generation.testdatagen.coverage.CFGUpdater_Mark;
//import auto_testcase_generation.testdatagen.se.IPathConstraints;
//import auto_testcase_generation.testdatagen.se.ISymbolicExecution;
//import auto_testcase_generation.testdatagen.se.Parameter;
//import auto_testcase_generation.testdatagen.se.SymbolicExecution;
//import auto_testcase_generation.testdatagen.se.memory.ISymbolicVariable;
//import com.dse.config.IFunctionConfig;
//import com.dse.config.Paths;
//import com.dse.environment.Environment;
//import com.dse.parser.object.IFunctionNode;
//import com.dse.util.Utils;
//import com.dse.logger.AkaLogger;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Deprecated
//public class SdartAutomatedTestdataGeneration extends AbstractDirectedAutomatedTestdataGeneration {
//    private final static AkaLogger logger = AkaLogger.get(SdartAutomatedTestdataGeneration.class);
//
//    public SdartAutomatedTestdataGeneration(IFunctionNode fn) {
//        super(fn);
//    }
//
//    // @Deprecated
//    public void generateTestdata(IFunctionNode originalFunction) throws Exception {
//        // Configure: no limit loop, no limit recursive
//        // Ignore execution when there exists duplicated constraints; duplicate test
//        // data
//        // If code coverage is not changed in N test data execution times, perform
//        // static testing
//        //getExePath(originalFunction);
//        ICFG normalizedCfg = generateNormalizedCFG(originalFunction);
//
//        if (normalizedCfg != null) {
//            // IMPROVEMENT HERE - BEGIN
//            boolean generatedBySDART = false;
//            List<IPartialTestpath> analyzedTestpaths = new ArrayList<>();
//            List<String> existingTestdata = new ArrayList<>();
//            List<IPathConstraints> existingConstraints = new ArrayList<>();
//            int numOfNotIncreaseTestdata = 0;
//            boolean hasRunStaticTestdata = false;
//            Optimization optimization = new Optimization();
//            // IMPROVEMENT HERE - END
//
//            String testdata = "";
//            int iteration_in_one_depth = 0;
//            int depth = 0;
//            float currentBranchCoverage = 0.0f;
//            float currentStatementCoverage = 0.0f;
//
//            logger.info("STRATEGY: DART_IMPROVEMENT");
//
//            float tmp_currentStatementCoverage = 0.0f;
//            float tmp_currentBranchCoverage = 0.0f;
//
//            float tmp_previous_currentStatementCoverage;
//            float tmp_previous_currentBranchCoverage;
//
//            // while (depth < DEPTH * MAX_ITERATIONS_IN_ONE_DEPTH && currentBranchCoverage <
//            // 100f) {
//            // logger.info("\n\n\n" + "====================<b>DEPTH " + depth
//            // + "</b>==========================================================");
//            while (iteration_in_one_depth < DEPTH * MAX_ITERATIONS_IN_ONE_DEPTH && currentBranchCoverage < 100f) {
//                logger.info("\n\n\n" + "---------------------ITERATION " + iteration_in_one_depth + " (DEPTH = " + depth
//                        + ")" + "-------------------------");
//                /*
//                  STEP 1. INITIALIZE TEST DATA AT RANDOM
//                 */
//                boolean tmp_isGenerateRandomly = false;
//                if (testdata == null || testdata.length() == 0) {
//
//                    // IMPROVEMENT HERE-BEGIN
//                    final int THRESHOLD_RANDOM_TESTDATA = 3;
//                    if (numOfNotIncreaseTestdata == THRESHOLD_RANDOM_TESTDATA && !hasRunStaticTestdata) {
//                        hasRunStaticTestdata = true;
//                        // Perform static testing to generate new test data covering
//                        // unvisited branches
//                        if (currentBranchCoverage < 100f) {
//                            logger.debug("Use static analysis to detect a directed test data");
//                            PartialTestpaths uncoveredTestpaths = normalizedCfg
//                                    .getPartialTestpathcontainingUnCoveredBranches_Strategy1();
//
//                            while (uncoveredTestpaths.size() >= 1 && normalizedCfg.computeBranchCoverage() < 100f) {
//                                IPartialTestpath uncoveredTestpath = uncoveredTestpaths.get(0);
//
//                                if (analyzedTestpaths.contains(uncoveredTestpath)) {
//                                    uncoveredTestpaths.remove(0);
//                                } else {
//                                    analyzedTestpaths.add(uncoveredTestpath);
//                                    uncoveredTestpaths.remove(0);
//
//                                    Parameter paramaters = new Parameter();
//                                    paramaters.addAll(originalFunction.getArguments());
//                                    paramaters.addAll(originalFunction.getReducedExternalVariables());
//
//                                    logger.info("Performing symbolic execution on this test path");
//                                    ISymbolicExecution se = new SymbolicExecution(uncoveredTestpath, paramaters,
//                                            originalFunction);
//
//                                    if (!existingConstraints.contains(se.getConstraints())) {
//                                        existingConstraints.add(se.getConstraints());
//
//                                        String currentTestdata = new StaticSolutionGeneration()
//                                                .solve(se.getConstraints(), originalFunction);
//
//                                        if (currentTestdata.equals(IStaticSolutionGeneration.NO_SOLUTION)) {
//                                            logger.info("No solution. Seek another unvisited test path.");
//                                            testdata = "";
//                                            generatedBySDART = false;
//                                        } else if (currentTestdata.equals(IStaticSolutionGeneration.EVERY_SOLUTION)) {
//                                            // Just pick a random test data
//                                            currentTestdata = initializeTestdataAtRandom();
//                                            logger.info("May solution. Choose a solution. Next test data = <b>"
//                                                    + currentTestdata + "</b>");
//                                            testdata = currentTestdata;
//                                            generatedBySDART = true;
//                                        } else {
//                                            logger.info("Found a solution. Next test data = <b>" + currentTestdata
//                                                    + "</b>");
//                                            testdata = currentTestdata;
//                                            generatedBySDART = true;
//                                        }
//
//                                        // Execute new test data
//                                        if (testdata.length() > 0) {
//                                            if (!existingTestdata.contains(testdata)) {
//                                                existingTestdata.add(testdata);
//
//                                                ITestdataExecution testdataExecution = executeTestdata(originalFunction,
//                                                        testdata);
//                                                iteration_in_one_depth++;
//
//                                                tmp_previous_currentBranchCoverage = tmp_currentBranchCoverage;
//                                                tmp_previous_currentStatementCoverage = tmp_currentStatementCoverage;
//
//                                                String fullTestdata = Utils
//                                                        .readFileContent(Paths.CURRENT_PROJECT.TESTDATA_INPUT_FILE_PATH)
//                                                        .replace("\n", "; ");
//                                                if (testdataExecution != null) {
//                                                    // Update CFG
//                                                    CFGUpdater_Mark cfgUpdater = new CFGUpdater_Mark(
//                                                            testdataExecution.getEncodedTestpath(), normalizedCfg);
//
//                                                    currentNumOfVisitedBranches -= normalizedCfg.getVisitedBranches()
//                                                            .size();
//
//                                                    cfgUpdater.updateVisitedNodes();
//
//                                                    tmp_currentStatementCoverage = cfgUpdater.getCfg()
//                                                            .computeStatementCoverage() * 100;
//                                                    logger.info("Current statement coverage = "
//                                                            + tmp_currentStatementCoverage);
//
//                                                    tmp_currentBranchCoverage = cfgUpdater.getCfg()
//                                                            .computeBranchCoverage() * 100;
//                                                    logger.info(
//                                                            "Current branch coverage = " + tmp_currentBranchCoverage);
//
//                                                    currentBranchCoverage = normalizedCfg.computeBranchCoverage() * 100;
//                                                    currentStatementCoverage = normalizedCfg.computeStatementCoverage()
//                                                            * 100;
//
//                                                    currentNumOfVisitedBranches += normalizedCfg.getVisitedBranches()
//                                                            .size();
//                                                    AbstractAutomatedTestdataGeneration.visitedBranchesInfor.add(new Integer[]{
//                                                            ++tmp_iterations, currentNumOfVisitedBranches});
//
//                                                    // CASE 1. The latest test path is
//                                                    // incomplete (there arises an error
//                                                    // when the
//                                                    // test data execute)
//                                                    if (!cfgUpdater.isCompleteTestpath()) {
//                                                        logger.info("The testpath is uncomplete! We still update CFG");
//                                                        logger.info("Found a bug. Uncomplete test path");
//
//                                                        AbstractAutomatedTestdataGeneration.bugs.add(new Bug(fullTestdata,
//                                                                testdataExecution.getEncodedTestpath(),
//                                                                originalFunction));
//
//                                                        AbstractAutomatedTestdataGeneration.testdata.add(new TestdataInReport(
//                                                                fullTestdata, testdataExecution.getEncodedTestpath(),
//                                                                false, tmp_currentStatementCoverage,
//                                                                tmp_currentBranchCoverage,
//                                                                tmp_currentBranchCoverage > tmp_previous_currentBranchCoverage || (tmp_currentStatementCoverage > tmp_previous_currentStatementCoverage),
//                                                                false, true));
//
//                                                        testdata = "";
//                                                    }
//                                                    // CASE 2. No errors during test data
//                                                    // execution
//                                                    else {
//                                                        AbstractAutomatedTestdataGeneration.testdata.add(new TestdataInReport(
//                                                                fullTestdata, testdataExecution.getEncodedTestpath(),
//                                                                true, tmp_currentStatementCoverage,
//                                                                tmp_currentBranchCoverage,
//                                                                tmp_currentBranchCoverage > tmp_previous_currentBranchCoverage || (tmp_currentStatementCoverage > tmp_previous_currentStatementCoverage),
//                                                                false, true));
//                                                    }
//                                                }
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    } // IMPROVEMENT HERE-END
//                    else {
//                        testdata = initializeTestdataAtRandom();
//                        logger.info("Generate a random test data: <b>" + testdata + "</b>");
//                        tmp_isGenerateRandomly = true;
//                    }
//                } else
//                    tmp_isGenerateRandomly = false;
//
//                // IMPROVEMENT HERE - BEGIN
//                if (existingTestdata.contains(testdata)) {
//                    testdata = initializeTestdataAtRandom();
//                    logger.info("Duplicate. Generate a random test data: <b>" + testdata + "</b>");
//                    tmp_isGenerateRandomly = true;
//                    generatedBySDART = true;
//                } else
//                    existingTestdata.add(testdata);
//                // IMPROVEMENT HERE - END
//
//                /*
//                  STEP 2. EXECUTE TEST DATA TO GET TEST PATH
//                 */
//                ITestdataExecution testdataExecution = executeTestdata(originalFunction, testdata);
//                iteration_in_one_depth++;
//
//                tmp_previous_currentBranchCoverage = tmp_currentBranchCoverage;
//                tmp_previous_currentStatementCoverage = tmp_currentStatementCoverage;
//
//                String fullTestdata = Utils.readFileContent(Paths.CURRENT_PROJECT.TESTDATA_INPUT_FILE_PATH)
//                        .replace("\n", "; ");
//                if (testdataExecution != null) {
//                    // Update CFG
//                    CFGUpdater_Mark cfgUpdater = new CFGUpdater_Mark(testdataExecution.getEncodedTestpath(),
//                            normalizedCfg);
//
//                    currentNumOfVisitedBranches -= normalizedCfg.getVisitedBranches().size();
//
//                    // logger.debug("Previous Visited Nodes: " +
//                    // cfgUpdater.getPreviousVisitedNodes());
//                    // logger.debug("Previous Visited Branches: " +
//                    // cfgUpdater.getPreviousVisitedBranches());
//                    cfgUpdater.updateVisitedNodes();
//
//                    tmp_currentStatementCoverage = cfgUpdater.getCfg().computeStatementCoverage() * 100;
//                    logger.info("Current statement coverage = " + tmp_currentStatementCoverage);
//
//                    tmp_currentBranchCoverage = cfgUpdater.getCfg().computeBranchCoverage() * 100;
//                    logger.info("Current branch coverage = " + tmp_currentBranchCoverage);
//
//                    currentBranchCoverage = normalizedCfg.computeBranchCoverage() * 100;
//                    currentStatementCoverage = normalizedCfg.computeStatementCoverage() * 100;
//
//                    currentNumOfVisitedBranches += normalizedCfg.getVisitedBranches().size();
//                    AbstractAutomatedTestdataGeneration.visitedBranchesInfor
//                            .add(new Integer[]{++tmp_iterations, currentNumOfVisitedBranches});
//
//                    // CASE 1. The latest test path is uncomplete (there
//                    // arises an error when the
//                    // test data execute)
//                    if (!cfgUpdater.isCompleteTestpath()) {
//                        logger.info("The testpath is uncomplete! We still update CFG");
//                        logger.info("Found a bug. Uncomplete test path");
//
//                        AbstractAutomatedTestdataGeneration.bugs
//                                .add(new Bug(fullTestdata, testdataExecution.getEncodedTestpath(), originalFunction));
//
//                        AbstractAutomatedTestdataGeneration.testdata.add(new TestdataInReport(fullTestdata,
//                                testdataExecution.getEncodedTestpath(), false, tmp_currentStatementCoverage,
//                                tmp_currentBranchCoverage,
//                                tmp_currentBranchCoverage > tmp_previous_currentBranchCoverage || (tmp_currentStatementCoverage > tmp_previous_currentStatementCoverage),
//                                tmp_isGenerateRandomly, generatedBySDART)); // MODIFIED
//                        generatedBySDART = false; // ADDED
//                        numOfNotIncreaseTestdata = tmp_currentBranchCoverage > tmp_previous_currentBranchCoverage ? 0
//                                : numOfNotIncreaseTestdata + 1;// ADDED
//                    }
//                    // CASE 2. No errors during test data execution
//                    else {
//                        AbstractAutomatedTestdataGeneration.testdata.add(new TestdataInReport(fullTestdata,
//                                testdataExecution.getEncodedTestpath(), true, tmp_currentStatementCoverage,
//                                tmp_currentBranchCoverage,
//                                tmp_currentBranchCoverage > tmp_previous_currentBranchCoverage || (tmp_currentStatementCoverage > tmp_previous_currentStatementCoverage),
//                                tmp_isGenerateRandomly, generatedBySDART));// MODIFIED
//                        generatedBySDART = false; // ADDED
//                        numOfNotIncreaseTestdata = tmp_currentBranchCoverage > tmp_previous_currentBranchCoverage ? 0
//                                : numOfNotIncreaseTestdata + 1;// ADDED
//
//                        switch (Environment.getInstance().getTypeofCoverage()) {
//
//                            case IFunctionConfig.COVERAGE_TYPES.BRANCH:
//                            case IFunctionConfig.COVERAGE_TYPES.STATEMENT:
//
//                                ITestpathInCFG executedTestpath = cfgUpdater.getUpdatedCFGNodes();
//
//                                /*
//                                  STEP 3. GET PATH CONSTRAINTS
//                                 */
//                                if (currentBranchCoverage < 100f && executedTestpath != null) {
//                                    Parameter paramaters = new Parameter();
//                                    paramaters.addAll(originalFunction.getArguments());
//                                    paramaters.addAll(originalFunction.getReducedExternalVariables());
//
//                                    logger.info("Performing symbolic execution on this test path");
//                                    ISymbolicExecution se = new SymbolicExecution(executedTestpath, paramaters,
//                                            originalFunction);
//
//                                    logger.debug("Done. Constraints: \n" + se.getConstraints().toString()
//                                            .replace(ISymbolicVariable.PREFIX_SYMBOLIC_VALUE, "")
//                                            .replace(ISymbolicVariable.SEPARATOR_BETWEEN_STRUCTURE_NAME_AND_ITS_ATTRIBUTES,
//                                                    ".")
//                                            .replace(ISymbolicVariable.ARRAY_CLOSING, "]")
//                                            .replace(ISymbolicVariable.ARRAY_OPENING, "["));
//
//                                    /*
//                                      STEP 4. NEGATE PATH CONSTRAINTS
//                                     */
//                                    logger.info("NEGATE PATH CONSTRAINTS");
//                                    TheNextTestdata negatedInformation = generateTheNextTestdata(se, originalFunction,
//                                            optimization);
//                                    testdata = negatedInformation.getTestdata();
//                                }
//                                break;
//
//                            case IFunctionConfig.COVERAGE_TYPES.MCDC:
//                                throw new Exception("Dont code this kind of code coverage");
//                        }
//
//                    }
//                } else {
//                    logger.debug("Current test data causes errors.");
//                    AbstractAutomatedTestdataGeneration.testdata.add(new TestdataInReport(fullTestdata,
//                            new TestpathString_Marker(), false, tmp_currentStatementCoverage, tmp_currentBranchCoverage,
//                            false, tmp_isGenerateRandomly, generatedBySDART));// MODIFIED
//                    generatedBySDART = false; // ADDED
//                }
//            }
//            // depth++;
//            // }
//
//            // IMPROVEMENT HERE-BEGIN
//            AbstractAutomatedTestdataGeneration.removedConstraints += existingConstraints.size();
//            AbstractAutomatedTestdataGeneration.removedTestdata += existingTestdata.size();
//            // IMPROVEMENT HERE-END
//
//            AbstractAutomatedTestdataGeneration.statementCoverage = currentStatementCoverage;
//            AbstractAutomatedTestdataGeneration.branchCoverage = currentBranchCoverage;
//            AbstractAutomatedTestdataGeneration.numOfBranches += normalizedCfg.getUnvisitedBranches().size()
//                    + normalizedCfg.getVisitedBranches().size();
//            AbstractAutomatedTestdataGeneration.numOfVisitedBranches += normalizedCfg.getVisitedBranches().size();
//        }
//    }
//}
