package com.dse.cli.command;

import com.dse.environment.Environment;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.testcase_manager.ITestCase;
import com.dse.testcase_manager.TestCase;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.testcasescript.object.TestSubprogramNode;
import com.dse.util.Utils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import static com.dse.cli.command.ICommand.DELETE_TC;

/**
 * This command is used to delete a specific testcase (ok)
 *  or multiples testcases with specific names (ok)
 *  or testcases of a subprogram (not yet)
 *  or testcases of a unit undertest (not yet)
 *  or all testcases in whole environment (not yet)
 */

@Command(name = DELETE_TC,
        mixinStandardHelpOptions = true,
        version = "1.0.0",
        description = "Delete an exist test case(s).")
public class DeleteTestCase extends AbstractCommand<String[]> {
    @Option(names = {"-t", "--testcase"}, paramLabel = "<testcase>",
            required = true, arity = "1..*",
            description = "The test case(s) name you need to delete.")
    private String[] testCases;

    @Option(names = {"-u", "--unit"}, paramLabel = "<unit>",
            required = true, arity = "1",
            description = "The unit under test.")
    private String unit;

    @Option(names = {"-s", "--subprogram"}, paramLabel = "<subprogram>",
            required = true, arity = "1",
            description = "The subprogram under test.")
    private String subprogram;

    public DeleteTestCase() {
        super();
    }

    @Override
    public String[] call() throws Exception {
        String[] selected = TestCaseCollector.get(unit, subprogram, testCases);

        for (String testCaseName : selected) {
            deleteTestCase(testCaseName);
        }

        return selected;
    }

    private void deleteTestCase(String testCaseName) {
        ITestCase testCase = TestCaseManager.getTestCaseByNameWithoutData(testCaseName);

        String unit = TestSubprogramNode.COMPOUND_SIGNAL;
        String subprogram = TestSubprogramNode.COMPOUND_SIGNAL;

        if (testCase instanceof TestCase) {
            ICommonFunctionNode functionNode = ((TestCase) testCase).getFunctionNode();
            subprogram = functionNode.getSimpleName();
            unit = Utils.getSourcecodeFile(functionNode).getName();
        }

        if (this.unit.equals(unit) && this.subprogram.equals(subprogram)) {
            TestCaseManager.removeBasicTestCase(testCaseName);
            TestCaseManager.removeCompoundTestCase(testCaseName);
            Environment.getInstance().saveTestcasesScriptToFile();

        } else {
            logger.error(testCase.getName() + " not found in subprogram " + this.subprogram + " of unit " + this.unit);
            logger.info("do you mean subprogram: " + subprogram + ", unit: " + unit + "?");
        }
    }
}
