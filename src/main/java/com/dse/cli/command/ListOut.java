package com.dse.cli.command;

import com.dse.environment.Environment;
import com.dse.guifx_v3.helps.UIController;
import com.dse.parser.object.*;
import com.dse.testcasescript.TestcaseSearch;
import com.dse.testcasescript.object.*;
import picocli.CommandLine;

import java.util.List;

import static com.dse.cli.command.ICommand.LIST;

@CommandLine.Command(name = LIST,
        mixinStandardHelpOptions = true,
        version = "1.0.0",
        description = "List out existed test cases or subprograms or units under test.")

public class ListOut extends AbstractCommand<String[]> {
    @CommandLine.Option(names = {"-T", "--type"}, paramLabel = "<type>",
            required = true, arity = "1",
            description = "The type you need to list out. \n-testcase - to list out testcases " +
                    "\n-subprogram - to list out subprograms" +
                    "\n-unit - to list out units under test" +
                    "\n-all - to list all types")
    private String type;

    @CommandLine.Option(names = {"-u", "--unit"}, paramLabel = "<unit>",
            required = false, arity = "1",
            description = "The unit under test.")
    private String unit;

    @CommandLine.Option(names = {"-s", "--subprogram"}, paramLabel = "<subprogram>",
            required = false, arity = "1",
            description = "The subprogram under test.")
    private String subprogram;

    public ListOut() {
        super();
    }


    @Override
    public String[] call() throws Exception {
//        logger.info("arguments: " + args.length);
        if (type != null) {
            TestcaseRootNode root = Environment.getInstance().getTestcaseScriptRootNode();
            List<ITestcaseNode> units = TestcaseSearch.searchNode(root, new TestUnitNode());
            ProjectNode projectNode = Environment.getInstance().getProjectNode();

            if (type.equals(LIST_TYPE_UNIT)) {
                /**
                 * list out all unit in the Environment
                 */
                if (validateWithTypeUnit()) {
                    for (ITestcaseNode node : units) {
                        ISourcecodeFileNode sourcecodeFileNode = UIController.searchSourceCodeFileNodeByPath((TestUnitNode) node, projectNode);
                        logger.info("UNIT:" + sourcecodeFileNode.getName());
                    }
                }
            } else if (type.equals(LIST_TYPE_SUBPROGRAM)) {
                /**
                 * List out all subprogram in the Environment
                 *      list -T=subprogram
                 * or in a specific unit
                 *      list -T=subprogram -u=<unit>
                 */
                if (validateWithTypeSubprogram()) {
                    if (args.length == 1) {
                        for (ITestcaseNode node : units) {
                            List<ITestcaseNode> subprogramNodes = TestcaseSearch.searchNode(node, new TestNormalSubprogramNode());
                            for (ITestcaseNode subprogramNode : subprogramNodes) {
                                ICommonFunctionNode functionNode = UIController.searchFunctionNodeByPath(((TestNormalSubprogramNode) subprogramNode).getName());
                                logger.info("SUBPROGRAM:" + functionNode.getSimpleName());
                            }
                        }
                    } else if (args.length == 2 && unit != null) {
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
                            for (ITestcaseNode subprogramNode : subprogramNodes) {
                                ICommonFunctionNode functionNode = UIController.searchFunctionNodeByPath(((TestNormalSubprogramNode) subprogramNode).getName());
                                logger.info("SUBPROGRAM:" + functionNode.getSimpleName());
                            }
                        } else {
                            logger.error("Unit not found: " + unit);
                        }
                    }
                }

            } else if (type.equals(LIST_TYPE_TESTCASE)) {
                /**
                 * List out all test case in the Environment
                 *      list -T=testcase
                 * or of a specific unit
                 *      list -T=testcase -u=<unit>
                 * or of a specific subprogram
                 *      list -T=testcase -u=<unit> -s=<subprogram>
                 */
                if (validateWithTypeTestcase()) {

                    if (args.length == 1) {
                        for (ITestcaseNode node : units) {
                            List<ITestcaseNode> subprogramNodes = TestcaseSearch.searchNode(node, new TestNormalSubprogramNode());
                            for (ITestcaseNode subprogramNode : subprogramNodes) {
                                List<ITestcaseNode> testcaseNodes = TestcaseSearch.searchNode(subprogramNode, new TestNameNode());
                                for (ITestcaseNode testcaseNode : testcaseNodes) {
                                    logger.info("TESTCASE:" + ((TestNameNode) testcaseNode).getName());
                                }
                            }
                        }
                    } else if (args.length == 2 && unit != null) {
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
                            for (ITestcaseNode subprogramNode : subprogramNodes) {
                                List<ITestcaseNode> testcaseNodes = TestcaseSearch.searchNode(subprogramNode, new TestNameNode());
                                for (ITestcaseNode testcaseNode : testcaseNodes) {
                                    logger.info("TESTCASE:" + ((TestNameNode) testcaseNode).getName());
                                }
                            }
                        } else {
                            logger.error("Unit not found: " + unit);
                        }

                    } else if (args.length == 3 && unit != null && subprogram != null) {
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
                                for (ITestcaseNode testcaseNode : testcaseNodes) {
                                    logger.info("TESTCASE:" + ((TestNameNode) testcaseNode).getName());
                                }
                            } else {
                                logger.error("Subprogram not found: " + subprogram);
                            }
                        } else {
                            logger.error("Unit not found: " + unit);
                        }
                    }
                }

            } else if (type.equals(LIST_TYPE_ALL)) {
                /**
                 * List out all unit, subprogram, test case in the Environment
                 *      list -T=all
                 * or in a specific unit
                 *      list -T=all -u=<unit>
                 * or in a specific subprogram
                 *      list -T=all -u=<unit> -s=<subprogram>
                 */
                if (validateWithTypeAll()) {
                    if (args.length == 1) {
                        for (ITestcaseNode node : units) {
                            ISourcecodeFileNode sourcecodeFileNode = UIController.searchSourceCodeFileNodeByPath((TestUnitNode) node, projectNode);
                            logger.info("UNIT:" + sourcecodeFileNode.getName());
                            List<ITestcaseNode> subprogramNodes = TestcaseSearch.searchNode(node, new TestNormalSubprogramNode());
                            for (ITestcaseNode subprogramNode : subprogramNodes) {
                                ICommonFunctionNode functionNode = UIController.searchFunctionNodeByPath(((TestNormalSubprogramNode) subprogramNode).getName());
                                logger.info("   SUBPROGRAM:" + functionNode.getSimpleName());
                                List<ITestcaseNode> testcaseNodes = TestcaseSearch.searchNode(subprogramNode, new TestNameNode());
                                for (ITestcaseNode testcaseNode : testcaseNodes) {
                                    logger.info("       TESTCASE:" + ((TestNameNode) testcaseNode).getName());
                                }
                            }
                        }
                    } else if (args.length == 2 && unit != null) {
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
                            for (ITestcaseNode subprogramNode : subprogramNodes) {
                                ICommonFunctionNode functionNode = UIController.searchFunctionNodeByPath(((TestNormalSubprogramNode) subprogramNode).getName());
                                logger.info("SUBPROGRAM:" + functionNode.getSimpleName());
                                List<ITestcaseNode> testcaseNodes = TestcaseSearch.searchNode(subprogramNode, new TestNameNode());
                                for (ITestcaseNode testcaseNode : testcaseNodes) {
                                    logger.info("   TESTCASE:" + ((TestNameNode) testcaseNode).getName());
                                }
                            }

                        } else {
                            logger.error("Unit not found: " + unit);
                        }
                    } else if (args.length == 3 && unit != null && subprogram != null) {
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
                                for (ITestcaseNode testcaseNode : testcaseNodes) {
                                    logger.info("TESTCASE:" + ((TestNameNode) testcaseNode).getName());
                                }
                            } else {
                                logger.error("Subprogram not found: " + subprogram);
                            }
                        } else {
                            logger.error("Unit not found: " + unit);
                        }
                    }
                }
            }

        }
        return new String[0];
    }

    private boolean validateWithTypeUnit() {
        if (args.length > 1) {
            logger.error("Too many arguments: \n list -T=unit");
            return false;
        } else {
            return true;
        }
    }

    private boolean validateWithTypeSubprogram() {
        if (args.length == 1 || (args.length == 2 && unit != null)) {
            return true;
        } else {
            logger.error("Invalid arguments: \n list -T=subprogram \n list -T=subprogram -u=<unit>");
            return false;
        }
    }

    private boolean validateWithTypeTestcase() {
        if (args.length == 1 || (args.length == 2 && unit != null)
                || (args.length == 3 && unit != null && subprogram != null)) {
            return true;
        } else {
            logger.error("Invalid arguments: \n list -T=testcase" +
                    "\n list -T=testcase -u=<unit>" +
                    "\n list -T=testcase -u=<unit> -s=<subprogram>");
            return false;
        }
    }

    private boolean validateWithTypeAll() {
        if (args.length == 1 || (args.length == 2 && unit != null)
                || (args.length == 3 && unit != null && subprogram != null)) {
            return true;
        } else {
            logger.error("Invalid arguments: \n list -T=all" +
                    "\n list -T=all -u=<unit>" +
                    "\n list -T=all -u=<unit> -s=<subprogram>");
            return false;
        }
    }

    private static String LIST_TYPE_ALL = "all";
    private static String LIST_TYPE_TESTCASE = "testcase";
    private static String LIST_TYPE_SUBPROGRAM = "subprogram";
    private static String LIST_TYPE_UNIT = "unit";
}
