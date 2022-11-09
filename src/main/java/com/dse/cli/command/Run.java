package com.dse.cli.command;

import com.dse.testcase_execution.CompoundTestcaseExecution;
import com.dse.testcase_execution.ITestcaseExecution;
import com.dse.testcase_execution.TestcaseExecution;
import com.dse.coverage.CoverageManager;
import com.dse.environment.object.EnviroCoverageTypeNode;
import com.dse.testcase_execution.result_trace.AssertionResult;
import com.dse.testcase_execution.result_trace.gtest.Execution;
import com.dse.environment.Environment;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.report.ExecutionResultReport;
import com.dse.report.ReportManager;
import com.dse.testcase_manager.CompoundTestCase;
import com.dse.testcase_manager.ITestCase;
import com.dse.testcase_manager.TestCase;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.testcasescript.object.TestSubprogramNode;
import com.dse.util.Utils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.dse.cli.command.ICommand.RUN;

/**
 * This command is used to execute a specific testcase (ok)
 *  or multiples testcases with specific names (ok)
 *  or testcases of a subprogram (not yet)
 *  or testcases of a unit undertest (not yet)
 *  or all testcases in whole environment (not yet)
 */

@Command(name = RUN,
        mixinStandardHelpOptions = true,
        version = "1.0.0",
        description = "Run test case(s) in current environment.")
public class Run extends AbstractCommand<String[]> {
    @Option(names = {"-t", "--testcase"}, paramLabel = "<testcase>",
            required = true, arity = "1..*",
            description = "The test case name you need to run.")
    private String[] testCases;

    @Option(names = {"-u", "--unit"}, paramLabel = "<unit>",
            required = true, arity = "1",
            description = "The unit under test.")
    private String unit;

    @Option(names = {"-s", "--subprogram"}, paramLabel = "<subprogram>",
            required = true, arity = "1",
            description = "The subprogram under test.")
    private String subprogram;

    public Run() {
        super();
    }

    @Override
    public String[] call() throws Exception {
        logger.info("collecting test case(s) data");
        String[] selected = TestCaseCollector.get(unit, subprogram, testCases);

        for (String testCaseName : selected) {
            ITestCase testCase = TestCaseManager.getTestCaseByName(testCaseName);

            if (testCase != null && isValidTestCase(testCase)) {
                // put test cases in different threads
                testCase.deleteOldDataExceptValue();
                ExecutionResultReport report = runTestCase(testCase);

                // print execution result
                printExecutionResult(testCase, report, Environment.getInstance().getTypeofCoverage());
            }
        }

        return selected;
    }

    private void printExecutionResult(ITestCase testCase, ExecutionResultReport report, String typeOfCoverage) {
        logger.info("Execution Result of " + testCase.getName());

        AssertionResult expected = testCase.getExecutionResult();
        logger.info(String.format("Expected: (%d/%d) PASS", expected.getPass(), expected.getTotal()));

        if (testCase instanceof TestCase) {
            switch (typeOfCoverage){
                case EnviroCoverageTypeNode.STATEMENT:
                case EnviroCoverageTypeNode.BRANCH:
                case EnviroCoverageTypeNode.BASIS_PATH:
                case EnviroCoverageTypeNode.MCDC:{
                    String visitedPerTotal = CoverageManager.getDetailProgressCoverage((TestCase) testCase, typeOfCoverage);
                    float progress = CoverageManager.getProgress((TestCase) testCase, typeOfCoverage);
                    logger.info(String.format(typeOfCoverage + " coverage: %.2f%% (%s)", progress, visitedPerTotal));
                    break;
                }

                case EnviroCoverageTypeNode.STATEMENT_AND_BRANCH:{
                    String visitedPerTotal = CoverageManager.getDetailProgressCoverage((TestCase) testCase, EnviroCoverageTypeNode.STATEMENT);
                    float progress = CoverageManager.getProgress((TestCase) testCase, typeOfCoverage);
                    logger.info(String.format("Stm Coverage: %.2f%% (%s)", progress, visitedPerTotal));

                    visitedPerTotal = CoverageManager.getDetailProgressCoverage((TestCase) testCase, EnviroCoverageTypeNode.BRANCH);
                    progress = CoverageManager.getProgress((TestCase) testCase, typeOfCoverage);
                    logger.info(String.format("Branch Coverage: %.2f%% (%s)", progress, visitedPerTotal));
                    break;
                }

                case EnviroCoverageTypeNode.STATEMENT_AND_MCDC:{
                    String visitedPerTotal = CoverageManager.getDetailProgressCoverage((TestCase) testCase, EnviroCoverageTypeNode.STATEMENT);
                    float progress = CoverageManager.getProgress((TestCase) testCase, typeOfCoverage);
                    logger.info(String.format("Stm Coverage: %.2f%% (%s)", progress, visitedPerTotal));

                    visitedPerTotal = CoverageManager.getDetailProgressCoverage((TestCase) testCase, EnviroCoverageTypeNode.MCDC);
                    progress = CoverageManager.getProgress((TestCase) testCase, typeOfCoverage);
                    logger.info(String.format("MCDC Coverage: %.2f%% (%s)", progress, visitedPerTotal));
                    break;
                }
            }

        }

//        Execution execution = report.getExecution();
        double time = 0;
        if (testCase instanceof TestCase) {
//            com.dse.testcase_execution.result_trace.gtest.TestCase gtTestCase = execution.getTestCaseByName(testCase.getName());
//            time = gtTestCase.getTime();
            time = testCase.getExecutedTime();
        }

        logger.info(String.format("Duration: %ss", time));

        logger.info("for more information please view execution report at " + report.getPath());
    }

    private boolean isValidTestCase(ITestCase testCase) {
        String unit = TestSubprogramNode.COMPOUND_SIGNAL;
        String subprogram = TestSubprogramNode.COMPOUND_SIGNAL;

        if (testCase instanceof TestCase) {
            ICommonFunctionNode functionNode = ((TestCase) testCase).getFunctionNode();
            subprogram = functionNode.getSimpleName();
            unit = Utils.getSourcecodeFile(functionNode).getName();
        }

        if (this.unit.equals(unit) && this.subprogram.equals(subprogram)) {
            return true;

        } else {
            logger.error(testCase.getName() + " not found in subprogram " + this.subprogram + " of unit " + this.unit);
            logger.info("Do you mean subprogram: " + subprogram + ", unit: " + unit);
            return false;
        }
    }

    private ExecutionResultReport runTestCase(ITestCase testCase) {
        try {
            if (testCase instanceof TestCase)
                runBasicTestCase((TestCase) testCase);
            else if (testCase instanceof CompoundTestCase)
                runCompoundTestCase((CompoundTestCase) testCase);

            testCase.setStatus(ITestCase.STATUS_SUCCESS);

            // Generate report
            ExecutionResultReport report = new ExecutionResultReport(testCase, LocalDateTime.now());
            ReportManager.export(report);

            // export
            TestCaseManager.exportTestCaseToFile(testCase);

            return report;

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private ITestcaseExecution preprocessorTestCase(TestCase testCase) throws Exception {
        ICommonFunctionNode function = testCase.getRootDataNode().getFunctionNode();
//        ICFG cfg = CFGUtils.createCFG(function, Environment.getInstance().getTypeofCoverage());

        // execute test case
        TestcaseExecution executor = new TestcaseExecution();
        executor.setFunction(function);
        if (testCase.getFunctionNode() == null)
            testCase.setFunctionNode(function);
        executor.setTestCase(testCase);

        executor.setMode(ITestcaseExecution.IN_EXECUTION_WITH_FRAMEWORK_TESTING_MODE);

        return executor;
    }

    private ITestcaseExecution preprocessorCompoundTestCase(CompoundTestCase testCase) {
        CompoundTestcaseExecution executor = new CompoundTestcaseExecution();
        executor.setTestCase(testCase);

        executor.setMode(ITestcaseExecution.IN_EXECUTION_WITH_FRAMEWORK_TESTING_MODE);

        return executor;
    }

    private void runBasicTestCase(TestCase testCase) throws Exception {
        ITestcaseExecution executor = preprocessorTestCase(testCase);
        executor.execute();

        // export coverage of testcase to file
        CoverageManager.exportCoveragesOfTestCaseToFile(testCase, Environment.getInstance().getTypeofCoverage());
    }

    private void runCompoundTestCase(CompoundTestCase testCase) throws Exception {
        ITestcaseExecution executor = preprocessorCompoundTestCase(testCase);
        executor.execute();
    }

    // use for regression script importation
    public List<ITestCase> collectTestCases() {
        try {
            List<ITestCase> testCaseList = new ArrayList<>();
            String unit = "";
            String subprogram = "";
            List<String> testCases = new ArrayList<>();
            for (String arg : getArgs()) {
                if (arg.startsWith("-u")) {
                    unit = arg.replace("-u=", "");
                } else if (arg.startsWith("-s")) {
                    subprogram = arg.replace("-s=", "");
                } else if (arg.startsWith("-t")) {
                    String testCaseName = arg.replace("-t=", "");
                    testCases.add(testCaseName);
                }
            }

            String[] selected = TestCaseCollector.get(unit, subprogram, testCases.toArray(new String[0]));
            for (String name : selected) {
                ITestCase testCase = TestCaseManager.getTestCaseByName(name);
                if (testCase != null) {
                    testCaseList.add(testCase);
                } else {
                    logger.error("Test Case not found: " + name);
                }
            }
            return testCaseList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}
