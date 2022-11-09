package com.dse.cli.command;

import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.IFunctionNode;
import com.dse.report.*;
import com.dse.testcase_manager.ITestCase;
import com.dse.testcase_manager.TestCase;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.testcasescript.object.TestSubprogramNode;
import com.dse.util.Utils;
import picocli.CommandLine;
import picocli.CommandLine.*;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Collections;

import static com.dse.cli.command.ICommand.REPORT;

@Command(name = REPORT,
        mixinStandardHelpOptions = true,
        version = "1.0.0",
        description = "Generate report in current environment.")
public class Report extends AbstractCommand<String> {
    @Option(names = {"-t", "--testcase"}, paramLabel = "<testcase>", arity = "1",
            description = "Specific test case name")
    private String testCase;

    @Option(names = {"-u", "--unit"}, paramLabel = "<unit>", arity = "1", required = true,
            description = "The unit under test.")
    private String unit;

    @Option(names = {"-s", "--subprogram"}, paramLabel = "<subprogram>", arity = "1",
            description = "The subprogram under test.")
    private String subprogram;

    @Option(names = {"-c", "--category"}, arity = "1", required = true,
            description = "Report category code" +
                    "\n\tRtd: Test data report" +
                    "\n\tRer: Execution result report" +
                    "\n\tRf : Full report" +
                    "\n\tRtm: Test case management report" +
                    "\n\tRc : Coverage report.")
    private Category category;

    @Option(names = {"-o", "--out"}, paramLabel = "<outputDirectory>", arity = "1",
            description = "The output directory path.")
    private File outputDirectory;

    @Option(names = {"-v", "--view"}, paramLabel = "<view>",
            description = "Auto view generated report in explorer.")
    private boolean view;

    public Report() {
        super();
    }


    @Override
    public String call() throws Exception {
        if (!outputDirectory.isDirectory()) {
            logger.error("output option is not a directory");
            return null;
        }

        IReport report = null;

        switch (category) {
            case COVERAGE:
                logger.error("akacli hasn't support to view coverage yet");
                break;
            case TEST_CASE_MANAGEMENT:
                logger.info("collecting unit data");
                report = new TestCaseManagementReport(unit, LocalDateTime.now());
                String outputFilePath = outputDirectory.getAbsolutePath();
                if (!outputFilePath.endsWith(File.separator))
                    outputFilePath += File.separator;
                outputFilePath += unit + ".html";
                report.setPath(outputFilePath);
                break;
            case EXECUTION:
            case TEST_DATA:
            case FULL:
                report = generateTestCaseLevelReport();
                break;
        }

        if (report == null) {
            logger.error("failed to generate report");
            return null;
        }

        ReportManager.export(report);
        logger.info("generate report successfully");

        if (view)
            Utils.openFolderorFileOnExplorer(report.getPath());
        //open -e/mnt/e/akautauto/target/local/working-directory/sfdfx4.env
        //report -cRf -uUtils.cpp -o/mnt/d/Study/PM/Homework report -cRtm -uUtils.cpp -o/mnt/d/Study/PM/Homework
        return testCase;
    }

    private IReport generateTestCaseLevelReport() throws Exception {
        if (this.testCase != null && this.unit != null && this.subprogram != null) {
            logger.info("collecting test case data");

            ITestCase testCase = TestCaseManager.getTestCaseByName(this.testCase);

            if (testCase != null && isValidTestCase(testCase)) {
                IReport report = null;

                switch (category) {
                    case TEST_DATA:
                        report = new TestCaseDataReport(Collections.singletonList(testCase), LocalDateTime.now());
                        break;
                    case EXECUTION:
                        report = new ExecutionResultReport(testCase, LocalDateTime.now());
                        break;
                    case FULL:
                        report = new FullReport(testCase, LocalDateTime.now());
                        break;
                }

                if (report != null) {
                    String outputFilePath = outputDirectory.getAbsolutePath();
                    if (!outputFilePath.endsWith(File.separator))
                        outputFilePath += File.separator;
                    outputFilePath += this.testCase + ".html";
                    report.setPath(outputFilePath);
                }

                return report;
            }
        }

        throw new Exception("empty required option(s) parameter");
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

    @Override
    public CommandLine registerConverter(CommandLine cmd) {
        return cmd.registerConverter(Category.class, CategoryConverter::convert);
    }

    private static class CategoryConverter {
        public static Category convert(String value) throws Exception {
            switch (value) {
                case "Rtd": return Category.TEST_DATA;
                case "Rer": return Category.EXECUTION;
                case "Rf" : return Category.FULL;
                case "Rtm": return Category.TEST_CASE_MANAGEMENT;
                case "Rc" : return Category.COVERAGE;
            }

            throw new Exception("wrong report category code");
        }
    }
}
