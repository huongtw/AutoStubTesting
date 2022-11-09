package com.dse.cli.command;


import com.dse.environment.Environment;
import com.dse.guifx_v3.helps.UIController;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.ISourcecodeFileNode;
import com.dse.parser.object.ProjectNode;
import com.dse.regression.RegressionScriptManager;
import com.dse.regression.objects.RegressionScript;
import com.dse.exception.FunctionNodeNotFoundException;
import com.dse.testcase_manager.ITestCase;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.testcasescript.TestcaseSearch;
import com.dse.testcasescript.object.*;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.dse.cli.command.ICommand.REGRESS;

@CommandLine.Command(name = REGRESS,
        mixinStandardHelpOptions = true,
        version = "1.0.0",
        description = "Manage regression scripts in current environment.")

public class RegressionScriptCommand extends AbstractCommand<String[]> {
    @CommandLine.Option(names = {"-T", "--type"}, paramLabel = "<type>",
            required = true, arity = "1",
            description = "The type of action you want to do" +
                    "\n-create - to create new regression script" +
                    "\n-delete - to delete regression script" +
                    "\n-list-script - to list all regression script that existed in the environment" +
                    "\n-list-tc - to list all test case of a regression script" +
                    "\n-put - to put test case(s) to the regression script" +
                    "\n-remove - to remove test case(s) from the regression script")
    private String type;

    @CommandLine.Option(names = {"-u", "--unit"}, paramLabel = "<unit>",
            required = false, arity = "1",
            description = "The unit under test.")
    private String unit;

    @CommandLine.Option(names = {"-s", "--subprogram"}, paramLabel = "<subprogram>",
            required = false, arity = "1",
            description = "The subprogram under test.")
    private String subprogram;

    @CommandLine.Option(names = {"-t", "--testcase"}, paramLabel = "<testcase>",
            required = false, arity = "1",
            description = "The test case name.")
    private String testcase;

    @CommandLine.Option(names = {"-n", "--script-name"}, paramLabel = "<script-name>",
            required = false, arity = "1",
            description = "The name of a regression script.")
    private String regressionScriptName;

    @Override
    public String[] call() throws Exception {
        if (type != null) {
            if (type.equals(CREATE)) {
                createRegressionScript();
            } else if (type.equals(DELETE)) {
                deleteRegressionScript();
            } else if (type.equals(RUN)) {
                runRegressionScript();
            } else if (type.equals(LIST_SCRIPT)) {
                listOutScript();
            } else if (type.equals(LIST_TEST_CASE)) {
                listOutTestCase();
            } else if (type.equals(PUT)) {
                putTestCase();
            } else if (type.equals(REMOVE)) {
                removeTestCase();
            } else {
                logger.error("The type option is invalid.");
            }
        } else {
            logger.error("The type option is invalid.");
        }
        return new String[0];
    }

    // currently, create new regression script will create new script with random name
    // and return the name to command line
    private void createRegressionScript() {
        // regress -T=create
        if (args.length == 1) {
            RegressionScript regressionScript = RegressionScript.getNewRandomNameProbePoint();
            RegressionScriptManager.getInstance().add(regressionScript);
            RegressionScriptManager.getInstance().exportRegressionScript(regressionScript);
            logger.info(regressionScript.getName());
        } else {
            logger.error("Invalid number of the input arguments.");
        }
    }

    // currently delete regression script by name
    private void deleteRegressionScript() {
        // regress -T=delete -n=<script-name>
        if (args.length == 2 && regressionScriptName != null) {
            RegressionScript regressionScript = RegressionScriptManager.getInstance().getRegressionScriptByName(regressionScriptName);
            if (regressionScript == null) {
                logger.info("The regression script not found: " + regressionScriptName);
            } else {
                RegressionScriptManager.getInstance().remove(regressionScript);
                RegressionScriptManager.getInstance().deleteRegressionScriptFile(regressionScript);
                logger.info("The regression script is deleted: " + regressionScriptName);
            }
        } else {
            logger.error("Invalid input arguments.");
        }
    }

    private void listOutScript() {
        // regress -T=list-script
        if (args.length == 1) {
            for (RegressionScript script : RegressionScriptManager.getInstance().getAllRegressionScripts()) {
                logger.info(script.getName() + ": " + script.getScriptFilePath());
            }
        } else {
            logger.error("Invalid number of the input arguments.");
        }
    }

    private void runRegressionScript() {
        // regress -T=run -n=<script-name>
        if (args.length == 2 && regressionScriptName != null) {
            RegressionScript regressionScript = RegressionScriptManager.getInstance().getRegressionScriptByName(regressionScriptName);
            if (regressionScript == null) {
                logger.info("The regression script not found: " + regressionScriptName);
            } else {
                String path = regressionScript.getScriptFilePath();
                File scriptFile = new File(path);
                if (scriptFile.isFile()) {
                    logger.info("Start execute regression script " + scriptFile.getName());
                    List<String> commands = readData(path);
                    for (String cmd : commands) {
                        try {
                            logger.info(cmd);
                            ICommand<?> command = ICommand.parse(cmd);
                            if (command != null) {
                                command.execute();
                            }
                            logger.info("");

                        } catch (Exception ex) {
                            ex.printStackTrace();
                            logger.error(ex.getMessage());
                        }
                    }
                    logger.info("Finish");

                } else {
                    logger.error("File not found: " + path);
                }
            }
        } else {
            logger.error("Invalid input arguments.");
        }
    }

    // list out test cases of a regression script
    private void listOutTestCase() {
        // regress -T=list-tc -n=<script-name>
        if (args.length == 2 && regressionScriptName != null) {
            RegressionScript regressionScript = RegressionScriptManager.getInstance().getRegressionScriptByName(regressionScriptName);
            if (regressionScript == null) {
                logger.info("The regression script not found: " + regressionScriptName);
            } else {
                for (ITestCase iTestCase : regressionScript.getTestCases()) {
                    logger.info(iTestCase.getName());
                }
            }
        } else {
            logger.error("Invalid input arguments.");
        }
    }

    // put test case into a regression script
    private void putTestCase() throws FunctionNodeNotFoundException {
        // regress -T=put -n=<script-name> -u=<unit>
        // regress -T=put -n=<script-name> -u=<unit> -s=<subprogram>
        // regress -T=put -n=<script-name> -u=<unit> -s=<subprogram> -t=<testcase>
        if (regressionScriptName == null) {
            logger.error("Need to specific name of regression script.");
        } else {
            RegressionScript regressionScript = RegressionScriptManager.getInstance().getRegressionScriptByName(regressionScriptName);
            if (regressionScript == null) {
                logger.error("The regression script not found: " + regressionScriptName);
            } else {
                TestcaseRootNode root = Environment.getInstance().getTestcaseScriptRootNode();
                List<ITestcaseNode> units = TestcaseSearch.searchNode(root, new TestUnitNode());
                ProjectNode projectNode = Environment.getInstance().getProjectNode();
                List<ITestCase> testCases = new ArrayList<>();

                if (unit != null && subprogram != null && testcase != null) {
                    // regress -T=put -n=<script-name> -u=<unit> -s=<subprogram> -t=<testcase>
                    // To put a specific test case to the regression script
                    TestUnitNode unitNode = null;
                    for (ITestcaseNode node : units) {
                        ISourcecodeFileNode sourcecodeFileNode = UIController.searchSourceCodeFileNodeByPath((TestUnitNode) node, projectNode);
                        if (sourcecodeFileNode.getName().equals(unit)) {
                            unitNode = (TestUnitNode) node;
                            break;
                        }
                    }
                    if (unitNode != null) {
                        List<ITestcaseNode> subprogramNodes = TestcaseSearch.searchNode(unitNode, new TestNormalSubprogramNode());
                        TestNormalSubprogramNode subprogramNode = null;
                        for (ITestcaseNode node : subprogramNodes) {
                            ICommonFunctionNode functionNode = UIController.searchFunctionNodeByPath(((TestNormalSubprogramNode) node).getName());
                            if (functionNode.getSimpleName().equals(subprogram)) {
                                subprogramNode = (TestNormalSubprogramNode) node;
                                break;
                            }
                        }
                        if (subprogramNode != null) {
                            List<ITestcaseNode> testcaseNodes = TestcaseSearch.searchNode(subprogramNode, new TestNameNode());
                            TestNameNode testNameNode = null;
                            for (ITestcaseNode node : testcaseNodes) {
                                if (((TestNameNode) node).getName().equals(testcase)) {
                                    testNameNode = (TestNameNode) node;
                                    break;
                                }
                            }
                            if (testNameNode != null) {
                                // TODO: check - Lam change get without data
                                ITestCase iTestCase = TestCaseManager.getTestCaseByNameWithoutData(testNameNode.getName());
                                if (iTestCase != null) {
                                    testCases.add(iTestCase);
                                } else {
                                    logger.error("Test case not found: " + testcase);
                                }
                            } else {
                                logger.error("Test case not found: " + testcase);
                            }

                            putTestCasesToRegressionScript(regressionScript, testCases);
                        } else {
                            logger.error("Subprogram not found: " + subprogram);
                        }

                    } else {
                        logger.error("Unit not found: " + unit);
                    }
                } else if (unit != null && subprogram != null) {
                    // regress -T=put -n=<script-name> -u=<unit> -s=<subprogram>
                    // To put all test case of a subprogram to the regression script
                    TestUnitNode unitNode = null;
                    for (ITestcaseNode node : units) {
                        ISourcecodeFileNode sourcecodeFileNode = UIController.searchSourceCodeFileNodeByPath((TestUnitNode) node, projectNode);
                        if (sourcecodeFileNode.getName().equals(unit)) {
                            unitNode = (TestUnitNode) node;
                            break;
                        }
                    }
                    if (unitNode != null) {
                        List<ITestcaseNode> subprogramNodes = TestcaseSearch.searchNode(unitNode, new TestNormalSubprogramNode());
                        TestNormalSubprogramNode subprogramNode = null;
                        for (ITestcaseNode node : subprogramNodes) {
                            ICommonFunctionNode functionNode = UIController.searchFunctionNodeByPath(((TestNormalSubprogramNode) node).getName());
                            if (functionNode.getSimpleName().equals(subprogram)) {
                                subprogramNode = (TestNormalSubprogramNode) node;
                                break;
                            }
                        }
                        if (subprogramNode != null) {
                            List<ITestcaseNode> testcaseNodes = TestcaseSearch.searchNode(subprogramNode, new TestNameNode());
                            for (ITestcaseNode node : testcaseNodes) {
                                // TODO: check - Lam change get without data
                                ITestCase iTestCase = TestCaseManager.getTestCaseByNameWithoutData(((TestNameNode) node).getName());
                                if (iTestCase != null) {
                                    testCases.add(iTestCase);
                                } else {
                                    logger.error("Test case not found: " + ((TestNameNode) node).getName());
                                }
                            }

                            putTestCasesToRegressionScript(regressionScript, testCases);
                        } else {
                            logger.error("Subprogram not found: " + subprogram);
                        }

                    } else {
                        logger.error("Unit not found: " + unit);
                    }

                } else if (unit != null) {
                    // regress -T=put -n=<script-name> -u=<unit>
                    // To put all test case of a unit to the regression script
                    TestUnitNode unitNode = null;
                    for (ITestcaseNode node : units) {
                        ISourcecodeFileNode sourcecodeFileNode = UIController.searchSourceCodeFileNodeByPath((TestUnitNode) node, projectNode);
                        if (sourcecodeFileNode.getName().equals(unit)) {
                            unitNode = (TestUnitNode) node;
                            break;
                        }
                    }
                    if (unitNode != null) {
                        List<ITestcaseNode> testcaseNodes = TestcaseSearch.searchNode(unitNode, new TestNameNode());
                        for (ITestcaseNode node : testcaseNodes) {
                            // TODO: check - Lam change get without data
                            ITestCase iTestCase = TestCaseManager.getTestCaseByNameWithoutData(((TestNameNode) node).getName());
                            if (iTestCase != null) {
                                testCases.add(iTestCase);
                            } else {
                                logger.error("Test case not found: " + ((TestNameNode) node).getName());
                            }
                        }
                        putTestCasesToRegressionScript(regressionScript, testCases);

                    } else {
                        logger.error("Unit not found: " + unit);
                    }

                } else {
                    logger.error("Invalid input arguments.");
                    logger.info("To put all test case of a unit to the regression script");
                    logger.info("regress -T=put -n=<script-name> -u=<unit>");
                    logger.info("To put all test case of a subprogram to the regression script");
                    logger.info("regress -T=put -n=<script-name> -u=<unit> -s=<subprogram>");
                    logger.info("To put a specific test case to the regression script");
                    logger.info("regress -T=put -n=<script-name> -u=<unit> -s=<subprogram> -t=<testcase>");
                }

                // after put test case to regression script, save the regression script file
                RegressionScriptManager.getInstance().exportRegressionScript(regressionScript);
            }
        }

    }

    private void putTestCasesToRegressionScript(RegressionScript regressionScript, List<ITestCase> testCases) {
        for (ITestCase iTestCase : testCases) {
            if (! regressionScript.getTestCases().contains(iTestCase)) {
                regressionScript.getTestCases().add(iTestCase);
            } else {
                logger.info("The test case " + iTestCase.getName() +
                        "has been contained by the regression script.");
            }
        }
    }

    private void removeTestCase() throws FunctionNodeNotFoundException {
        // regress -T=remove -n=<script-name> -u=<unit>
        // regress -T=remove -n=<script-name> -u=<unit> -s=<subprogram>
        // regress -T=remove -n=<script-name> -u=<unit> -s=<subprogram> -t=<testcase>
        if (regressionScriptName == null) {
            logger.error("Need to specific name of regression script.");
        } else {
            RegressionScript regressionScript = RegressionScriptManager.getInstance().getRegressionScriptByName(regressionScriptName);
            if (regressionScript == null) {
                logger.error("The regression script not found: " + regressionScriptName);
            } else {
                TestcaseRootNode root = Environment.getInstance().getTestcaseScriptRootNode();
                List<ITestcaseNode> units = TestcaseSearch.searchNode(root, new TestUnitNode());
                ProjectNode projectNode = Environment.getInstance().getProjectNode();
                List<ITestCase> testCases = new ArrayList<>();

                if (unit != null && subprogram != null && testcase != null) {
                    // regress -T=remove -n=<script-name> -u=<unit> -s=<subprogram> -t=<testcase>
                    // To remove a specific test case from the regression script
                    TestUnitNode unitNode = null;
                    for (ITestcaseNode node : units) {
                        ISourcecodeFileNode sourcecodeFileNode = UIController.searchSourceCodeFileNodeByPath((TestUnitNode) node, projectNode);
                        if (sourcecodeFileNode.getName().equals(unit)) {
                            unitNode = (TestUnitNode) node;
                            break;
                        }
                    }
                    if (unitNode != null) {
                        List<ITestcaseNode> subprogramNodes = TestcaseSearch.searchNode(unitNode, new TestNormalSubprogramNode());
                        TestNormalSubprogramNode subprogramNode = null;
                        for (ITestcaseNode node : subprogramNodes) {
                            ICommonFunctionNode functionNode = UIController.searchFunctionNodeByPath(((TestNormalSubprogramNode) node).getName());
                            if (functionNode.getSimpleName().equals(subprogram)) {
                                subprogramNode = (TestNormalSubprogramNode) node;
                                break;
                            }
                        }
                        if (subprogramNode != null) {
                            List<ITestcaseNode> testcaseNodes = TestcaseSearch.searchNode(subprogramNode, new TestNameNode());
                            TestNameNode testNameNode = null;
                            for (ITestcaseNode node : testcaseNodes) {
                                if (((TestNameNode) node).getName().equals(testcase)) {
                                    testNameNode = (TestNameNode) node;
                                    break;
                                }
                            }
                            if (testNameNode != null) {
                                // TODO: check - Lam change get without data
                                ITestCase iTestCase = TestCaseManager.getTestCaseByNameWithoutData(testNameNode.getName());
                                if (iTestCase != null) {
                                    testCases.add(iTestCase);
                                } else {
                                    logger.error("Test case not found: " + testcase);
                                }
                            } else {
                                logger.error("Test case not found: " + testcase);
                            }

                            removeTestCasesFromRegressionScript(regressionScript, testCases);
                        } else {
                            logger.error("Subprogram not found: " + subprogram);
                        }

                    } else {
                        logger.error("Unit not found: " + unit);
                    }
                } else if (unit != null && subprogram != null) {
                    // regress -T=remove -n=<script-name> -u=<unit> -s=<subprogram>
                    // To remove all test case of a subprogram from the regression script
                    TestUnitNode unitNode = null;
                    for (ITestcaseNode node : units) {
                        ISourcecodeFileNode sourcecodeFileNode = UIController.searchSourceCodeFileNodeByPath((TestUnitNode) node, projectNode);
                        if (sourcecodeFileNode.getName().equals(unit)) {
                            unitNode = (TestUnitNode) node;
                            break;
                        }
                    }
                    if (unitNode != null) {
                        List<ITestcaseNode> subprogramNodes = TestcaseSearch.searchNode(unitNode, new TestNormalSubprogramNode());
                        TestNormalSubprogramNode subprogramNode = null;
                        for (ITestcaseNode node : subprogramNodes) {
                            ICommonFunctionNode functionNode = UIController.searchFunctionNodeByPath(((TestNormalSubprogramNode) node).getName());
                            if (functionNode.getSimpleName().equals(subprogram)) {
                                subprogramNode = (TestNormalSubprogramNode) node;
                                break;
                            }
                        }
                        if (subprogramNode != null) {
                            List<ITestcaseNode> testcaseNodes = TestcaseSearch.searchNode(subprogramNode, new TestNameNode());
                            for (ITestcaseNode node : testcaseNodes) {
                                // TODO: check - Lam change get without data
                                ITestCase iTestCase = TestCaseManager.getTestCaseByNameWithoutData(((TestNameNode) node).getName());
                                if (iTestCase != null) {
                                    testCases.add(iTestCase);
                                } else {
                                    logger.error("Test case not found: " + ((TestNameNode) node).getName());
                                }
                            }
                            removeTestCasesFromRegressionScript(regressionScript, testCases);

                        } else {
                            logger.error("Subprogram not found: " + subprogram);
                        }

                    } else {
                        logger.error("Unit not found: " + unit);
                    }

                } else if (unit != null) {
                    // regress -T=remove -n=<script-name> -u=<unit>
                    // To remove all test case of a unit from the regression script
                    TestUnitNode unitNode = null;
                    for (ITestcaseNode node : units) {
                        ISourcecodeFileNode sourcecodeFileNode = UIController.searchSourceCodeFileNodeByPath((TestUnitNode) node, projectNode);
                        if (sourcecodeFileNode.getName().equals(unit)) {
                            unitNode = (TestUnitNode) node;
                            break;
                        }
                    }
                    if (unitNode != null) {
                        List<ITestcaseNode> testcaseNodes = TestcaseSearch.searchNode(unitNode, new TestNameNode());
                        for (ITestcaseNode node : testcaseNodes) {
                            // TODO: check - Lam change get without data
                            ITestCase iTestCase = TestCaseManager.getTestCaseByNameWithoutData(((TestNameNode) node).getName());
                            if (iTestCase != null) {
                                testCases.add(iTestCase);
                            } else {
                                logger.error("Test case not found: " + ((TestNameNode) node).getName());
                            }
                        }
                        removeTestCasesFromRegressionScript(regressionScript, testCases);

                    } else {
                        logger.error("Unit not found: " + unit);
                    }

                } else {
                    logger.error("Invalid input arguments.");
                    logger.info("To remove all test case of a unit from the regression script");
                    logger.info("regress -T=put -n=<script-name> -u=<unit>");
                    logger.info("To remove all test case of a subprogram from the regression script");
                    logger.info("regress -T=put -n=<script-name> -u=<unit> -s=<subprogram>");
                    logger.info("To remove a specific test case from the regression script");
                    logger.info("regress -T=put -n=<script-name> -u=<unit> -s=<subprogram> -t=<testcase>");
                }

                // after remove test case from regression script, save the regression script file
                RegressionScriptManager.getInstance().exportRegressionScript(regressionScript);
            }
        }
    }

    private void removeTestCasesFromRegressionScript(RegressionScript regressionScript, List<ITestCase> testCases) {
        for (ITestCase iTestCase : testCases) {
            if (regressionScript.getTestCases().contains(iTestCase)) {
                regressionScript.getTestCases().remove(iTestCase);
                logger.info("Remove test case: " + iTestCase.getName() + " from regression script " + regressionScriptName);
            } else {
                logger.info("The regression script does not contain test case: " + iTestCase.getName());
            }
        }
    }

    /**
     * Read data from file path
     *
     * @param path path to file
     * @return data in string
     */
    private static List<String> readData(String path) {
        List<String> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {

            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                data.add(sCurrentLine);
            }

        } catch (IOException e) {
            // // e.printStackTrace();
        }
        return data;
    }

    private static String CREATE = "create";
    private static String DELETE = "delete";
    private static String RUN = "run";
    private static String LIST_SCRIPT = "list-script";
    private static String LIST_TEST_CASE = "list-tc";
    private static String PUT = "put";
    private static String REMOVE = "remove";
}
