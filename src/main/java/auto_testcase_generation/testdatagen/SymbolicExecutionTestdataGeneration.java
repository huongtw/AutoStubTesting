package auto_testcase_generation.testdatagen;

import auto_testcase_generation.cfg.object.*;
import auto_testcase_generation.cfg.testpath.FullTestpath;
import auto_testcase_generation.cfg.testpath.ITestpathInCFG;
import auto_testcase_generation.testdatagen.se.*;
import auto_testcase_generation.testdatagen.se.normalization.ConstraintNormalizer;
import auto_testcase_generation.testdatagen.se.solver.RunZ3OnCMD;
import auto_testcase_generation.testdatagen.se.solver.SmtLibGeneration;
import auto_testcase_generation.testdatagen.se.solver.solutionparser.Z3SolutionParser;
import auto_testcase_generation.utils.ASTUtils;
import com.dse.config.AkaConfig;
import com.dse.config.FunctionConfig;
import com.dse.config.WorkspaceConfig;
import com.dse.coverage.CoverageDataObject;
import com.dse.coverage.CoverageManager;
import com.dse.environment.Environment;
import com.dse.environment.object.EnviroCoverageTypeNode;
import com.dse.guifx_v3.controllers.TestCasesExecutionTabController;
import com.dse.guifx_v3.controllers.TestCasesNavigatorController;
import com.dse.guifx_v3.helps.CacheHelper;
import com.dse.guifx_v3.helps.TCExecutionDetailLogger;
import com.dse.guifx_v3.helps.UIController;
import com.dse.guifx_v3.helps.UILogger;
import com.dse.guifx_v3.objects.TestCaseExecutionDataNode;
import com.dse.guifx_v3.objects.TestCasesTreeItem;
import com.dse.logger.AkaLogger;
import com.dse.parser.dependency.Dependency;
import com.dse.parser.dependency.FunctionCallDependency;
import com.dse.parser.dependency.SizeOfArrayOrPointerDependency;
import com.dse.parser.dependency.finder.Level;
import com.dse.parser.dependency.finder.VariableSearchingSpace;
import com.dse.parser.object.*;
import com.dse.search.Search;
import com.dse.search.condition.AbstractFunctionNodeCondition;
import com.dse.search.condition.VariableNodeCondition;
import com.dse.testcase_execution.TestcaseExecution;
import com.dse.testcase_execution.result_trace.AssertionResult;
import com.dse.testcase_manager.ITestCase;
import com.dse.testcase_manager.TestCase;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.testcase_manager.TestPrototype;
import com.dse.testcase_manager.minimize.GreedyMinimizer;
import com.dse.testcase_manager.minimize.ITestCaseMinimizer;
import com.dse.testcase_manager.minimize.Scope;
import com.dse.testdata.InputCellHandler;
import com.dse.testdata.gen.module.InitialTreeGen;
import com.dse.testdata.gen.module.SimpleTreeDisplayer;
import com.dse.testdata.gen.module.TreeExpander;
import com.dse.testdata.object.RootDataNode;
import com.dse.testdata.object.ValueDataNode;
import com.dse.util.IRegex;
import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;
import com.dse.util.VariableTypeUtils;
import javafx.collections.ObservableList;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.eclipse.cdt.core.dom.ast.*;

import java.io.File;
import java.util.*;

public abstract class SymbolicExecutionTestdataGeneration extends AbstractAutomatedTestdataGeneration {
    private final static AkaLogger logger = AkaLogger.get(SymbolicExecutionTestdataGeneration.class);
    List<StubVar> stubCall = new ArrayList<>();
    List<Node> listStubNode = new ArrayList<>();

    public SymbolicExecutionTestdataGeneration(ICommonFunctionNode fn, String coverageType) {
        super(fn);
        this.coverageType = coverageType;
        listStubNode = new ArrayList<>(
                Search.searchNodes(Environment.getInstance().getProjectNode(), new AbstractFunctionNodeCondition()));
        for (Node child : listStubNode) {
            if (child instanceof FunctionNode) {
                String name = child.getName();
                String reducedDependName = name.substring(name.lastIndexOf("\\") + 1, name.lastIndexOf("("));
                String stubName = "akaut_stub_" + reducedDependName + "_Number_of_Calls";
                StubVar dpVar = new StubVar(stubName, 0);
                stubCall.add(dpVar);
            }
        }
        listStubNode.removeIf(node -> !(node instanceof FunctionNode));
        Parameters parameters = new Parameters();
        parameters.addAll(fn.getArgumentsAndGlobalVariables());

    }

    public class StubVar {
        public String stubName;
        public int count;

        StubVar(String dependencyName, int count) {
            this.stubName = dependencyName;
            this.count = count;
        }

        public void setCount(int n) {
            this.count = n;
        }
    }

    public void generateTestdata(ICommonFunctionNode fn) throws Exception {
        logger.debug("Generating test data for function " + fn.getName());
        logger.debug("Automated test data generation strategy: Directed-Dijkstra");

        if (fn.getFunctionConfig() == null) {
            FunctionConfig functionConfig = new WorkspaceConfig().fromJson().getDefaultFunctionConfig();
            functionConfig.setFunctionNode(fn);
            fn.setFunctionConfig(functionConfig);
            functionConfig.createBoundOfArgument(functionConfig, fn);
        }

        final long MAX_ITERATION = fn.getFunctionConfig().getTheMaximumNumberOfIterations(); // may change to any value
        logger.debug("Maximum number of iterations = " + MAX_ITERATION);

        // clear cache before generate testcase
        TestCasesTreeItem treeItem = CacheHelper.getFunctionToTreeItemMap().get(fn);
        if (CacheHelper.getTreeItemToListTestCasesMap().get(treeItem) != null)
            CacheHelper.getTreeItemToListTestCasesMap().get(treeItem).clear();

        //
        if (fn.isTemplate() || fn instanceof MacroFunctionNode || fn.hasVoidPointerArgument()
                || fn.hasFunctionPointerArgument()) {
            if (this.allPrototypes == null || this.allPrototypes.size() == 0)
                this.allPrototypes = getAllPrototypesOfTemplateFunction(fn);
            if (this.allPrototypes.size() == 0)
                return;
        }

        // testCases = TestCaseManager.getTestCasesByFunction(this.fn);
        testCases = new ArrayList<>();

        start(this.testCases, this.fn, this.coverageType, this.allPrototypes, this.generatedTestcases,
                this.analyzedTestpathMd5, showReport);
    }

    /**
     * Generate test data
     */
    protected abstract void start(List<TestCase> testCases, ICommonFunctionNode fn, String coverageType,
            List<TestPrototype> allPrototypes,
            List<String> generatedTestcases,
            List<String> analyzedTestpathMd5,
            boolean showReport) throws Exception;

    /**
     * Find a test case traversing the given test path
     *
     * @param testpath            a test path
     * @param fn                  the tested function
     * @param generatedTestcases  generated test cases
     * @param analyzedTestpathMd5 md5 of analyzed test paths (result of executing
     *                            the generated test case)
     * @return state of solving process (FOUND_DUPLICATED_TESTCASE/
     *         COULD_NOT_CONSTRUCT_TREE_FROM_TESTCASE/
     *         COUND_NOT_EXECUTE_TESTCASE/ BE_ABLE_TO_EXECUTE_TESTCASE)
     */
    protected int solve(List<ICfgNode> testpath, ICommonFunctionNode fn, List<String> generatedTestcases,
            List<String> analyzedTestpathMd5) {
        String tpStr = testpath.toString();
        String md5 = Utils.computeMd5(tpStr);
        if (analyzedTestpathMd5.contains(md5))
            return AUTOGEN_STATUS.FOUND_DUPLICATED_TESTPATH;

        analyzedTestpathMd5.add(md5);

        List<RandomValue> theNextTestdata = generateTheNextTestData(testpath, fn, generatedTestcases);

        if (theNextTestdata != null && theNextTestdata.size() > 0) {
            /*
             * Initialize a test case
             */
            String nameofTestcase = TestCaseManager.generateContinuousNameOfTestcase(getTestCaseNamePrefix(fn));
            TestCase testCase = TestCaseManager.createTestCase(nameofTestcase, fn);

            /*
             * Execute a test case
             */
            if (testCase != null) {
                TestCasesNavigatorController.getInstance().refreshNavigatorTreeFromAnotherThread();

                UILogger.getUiLogger().info("Executing test case: " + testCase);

                TestcaseExecution executor = new TestcaseExecution();
                executor.setFunction(fn);
                executor.setMode(TestcaseExecution.IN_AUTOMATED_TESTDATA_GENERATION_MODE);
                int executionStatus = iterateDirectly(testCase, executor, fn, theNextTestdata);

                if (executionStatus == AUTOGEN_STATUS.EXECUTION.BE_ABLE_TO_EXECUTE_TESTCASE) {
                    testCases.add(testCase);
                    testCase.updateToTestCasesNavigatorTree();
                }

                return executionStatus;
            } else
                return AUTOGEN_STATUS.OTHER_ERRORS;

        } else {
            logger.debug("There is no next test data");
            return AUTOGEN_STATUS.SOLVING_STATUS.FOUND_DUPLICATED_TESTCASE;
        }
    }

    protected abstract String getTestCaseNamePrefix(ICommonFunctionNode fn);

    /**
     * @param testCase
     * @param executor
     * @param fn
     * @param theNextTestdata
     * @return COULD_NOT_CONSTRUCT_TREE_FROM_TESTCASE/ COUND_NOT_EXECUTE_TESTCASE/
     *         BE_ABLE_TO_EXECUTE_TESTCASE
     */
    protected int iterateDirectly(TestCase testCase, TestcaseExecution executor, ICommonFunctionNode fn,
            List<RandomValue> theNextTestdata) {
        RootDataNode root = testCase.getRootDataNode();

        try {
            logger.debug("recursiveExpandUutBranch");
            recursiveExpandUutBranch(root.getRoot(), theNextTestdata, testCase);
        } catch (Exception e) {
            e.printStackTrace();
            return AUTOGEN_STATUS.EXECUTION.COULD_NOT_CONSTRUCT_TREE_FROM_TESTCASE;
        }

        try {
            logger.debug(new SimpleTreeDisplayer().toString(root));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return executeTestCase(testCase, executor, "");
    }

    protected int executeTestCase(TestCase testCase, TestcaseExecution executor, String additionalHeaders) {
        try {
            testCase.setStatus(TestCase.STATUS_EXECUTING);
            TestCasesNavigatorController.getInstance().refreshNavigatorTreeFromAnotherThread();

            testCase.setAdditionalHeaders(additionalHeaders);

            String coverage = Environment.getInstance().getTypeofCoverage();

            // add and initialize corresponding TestCaseExecutionDataNode
            ICommonFunctionNode functionNode = testCase.getFunctionNode();
            TCExecutionDetailLogger.addTestCase(functionNode, testCase);
            TestCaseExecutionDataNode executionDataNode = TCExecutionDetailLogger
                    .getExecutionDataNodeByTestCase(testCase);
            TCExecutionDetailLogger.logDetailOfTestCase(testCase, "Name: " + testCase.getName());
            String value = testCase.getRootDataNode().getRoot().getInputForGoogleTest(false);
            TCExecutionDetailLogger.logDetailOfTestCase(testCase, "Value: " + value);
            executionDataNode.setValue(value);

            // Execute random values
            executor.setTestCase(testCase);
            executor.execute();
            // save test case to file
            testCase.setPathDefault();
            TestCaseManager.exportBasicTestCaseToFile(testCase);
            logger.debug("Save the testcase " + testCase.getName() + " to file " + testCase.getPath());

            if (testCase.getStatus().equals(TestCase.STATUS_SUCCESS)
                    || testCase.getStatus().equals(TestCase.STATUS_RUNTIME_ERR)) {
                // export highlighted source code and coverage to file
                CoverageManager.exportCoveragesOfTestCaseToFile(testCase, coverage);

                // read coverage information from file to display on GUI
                List<TestCase> testcases = new ArrayList<>();
                testcases.add(testCase);

                switch (coverage) {
                    case EnviroCoverageTypeNode.STATEMENT:
                    case EnviroCoverageTypeNode.BRANCH:
                    case EnviroCoverageTypeNode.BASIS_PATH:
                    case EnviroCoverageTypeNode.MCDC: {
                        CoverageDataObject coverageData = CoverageManager
                                .getCoverageOfMultiTestCaseAtSourcecodeFileLevel(testcases, coverage);
                        double cov = Utils.round(coverageData.getVisited() * 1.0f / coverageData.getTotal() * 100, 4);
                        String msg = coverage + " cov: " + cov + "%";
                        TCExecutionDetailLogger.logDetailOfTestCase(testCase, msg);
                        executionDataNode.setCoverage(msg);
                        break;
                    }
                    case EnviroCoverageTypeNode.STATEMENT_AND_BRANCH: {
                        String msg = "";
                        // stm cov
                        CoverageDataObject stmCovData = CoverageManager
                                .getCoverageOfMultiTestCaseAtSourcecodeFileLevel(testcases,
                                        EnviroCoverageTypeNode.STATEMENT);
                        double stmCov = Utils.round(stmCovData.getVisited() * 1.0f / stmCovData.getTotal() * 100, 4);
                        msg = EnviroCoverageTypeNode.STATEMENT + " cov: " + stmCov + "%; ";

                        // branch cov
                        CoverageDataObject branchCovData = CoverageManager
                                .getCoverageOfMultiTestCaseAtSourcecodeFileLevel(testcases,
                                        EnviroCoverageTypeNode.BRANCH);
                        double branchCov = Utils
                                .round(branchCovData.getVisited() * 1.0f / branchCovData.getTotal() * 100, 4);
                        msg += EnviroCoverageTypeNode.BRANCH + " cov: " + branchCov + "%";

                        TCExecutionDetailLogger.logDetailOfTestCase(testCase, msg);
                        executionDataNode.setCoverage(msg);
                        break;
                    }
                    case EnviroCoverageTypeNode.STATEMENT_AND_MCDC: {
                        String msg = "";
                        // stm coverage
                        CoverageDataObject stmCovData = CoverageManager
                                .getCoverageOfMultiTestCaseAtSourcecodeFileLevel(testcases,
                                        EnviroCoverageTypeNode.STATEMENT);
                        double stmCov = Utils.round(stmCovData.getVisited() * 1.0f / stmCovData.getTotal() * 100, 4);
                        msg = EnviroCoverageTypeNode.STATEMENT + " cov: " + stmCov + "%; ";

                        // mcdc coverage
                        CoverageDataObject branchCovData = CoverageManager
                                .getCoverageOfMultiTestCaseAtSourcecodeFileLevel(testcases,
                                        EnviroCoverageTypeNode.MCDC);
                        double mcdcCov = Utils.round(branchCovData.getVisited() * 1.0f / branchCovData.getTotal() * 100,
                                4);
                        msg += EnviroCoverageTypeNode.MCDC + " cov: " + mcdcCov + "%";

                        TCExecutionDetailLogger.logDetailOfTestCase(testCase, msg);
                        executionDataNode.setCoverage(msg);
                        break;
                    }
                }

                // display on Execution View
                TestCasesExecutionTabController testCasesExecutionTabController = TCExecutionDetailLogger
                        .getTCExecTabControllerByFunction(functionNode);
                if (testCasesExecutionTabController != null) {
                    ObservableList<TestCaseExecutionDataNode> data = testCasesExecutionTabController.getData();
                    executionDataNode.setId(data.size());
                    data.add(executionDataNode);
                }

                testCase.setExecutionResult(new AssertionResult());

                // update testcase on disk
                TestCaseManager.exportBasicTestCaseToFile(testCase);
                // export coverage of testcase to file
                CoverageManager.exportCoveragesOfTestCaseToFile(testCase,
                        Environment.getInstance().getTypeofCoverage());
                // save to tst file and navigator tree
                TestCasesNavigatorController.getInstance().refreshNavigatorTreeFromAnotherThread();

                return AUTOGEN_STATUS.EXECUTION.BE_ABLE_TO_EXECUTE_TESTCASE;
            } else {
                logger.debug("Do not add test case " + testCase.getName() + " because we can not execute it");
                return AUTOGEN_STATUS.EXECUTION.COUND_NOT_EXECUTE_TESTCASE;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return AUTOGEN_STATUS.EXECUTION.COUND_NOT_EXECUTE_TESTCASE;
        }
    }

    protected List<RandomValue> generateTheNextTestData(List<ICfgNode> testpath, ICommonFunctionNode functionNode,
            List<String> generatedTestcases) {
        List<RandomValue> theNextTestdata = new ArrayList<>();

        Parameters parameters = new Parameters();
        Parameters stubParameters = new Parameters();
        // reset stubCall before next generation
        for (StubVar stVar : stubCall) {
            stVar.setCount(0);
        }
        List<StubVar> stubVarCount = collectStubVar(stubParameters, testpath);
        parameters.addAll(functionNode.getArgumentsAndGlobalVariables());
        parameters.addAll(stubParameters);
        //

        try {
            ITestpathInCFG testpathInCFG = new FullTestpath();
            testpathInCFG.getAllCfgNodes().addAll(testpath);
            ISymbolicExecution se = new SymbolicExecution(testpathInCFG, parameters, functionNode);
            logger.debug("Constraints: " + se.getConstraints());
            logger.debug("New variables: " + se.getNewVariables().toString());
            logger.debug("Table mapping: " + se.getTableMapping().toString());

            // add new variables to parameters
            List<NewVariableInSe> newVariables = new ArrayList<>(se.getNewVariables());
            newVariables.sort(new Comparator<NewVariableInSe>() {
                @Override
                public int compare(NewVariableInSe o1, NewVariableInSe o2) {
                    return o1.getOriginalName().compareTo(o2.getOriginalName());
                }
            });
            for (NewVariableInSe newVar : newVariables) {
                boolean isExist = parameters.stream().anyMatch(v -> v.getName().equals(newVar.getOriginalName()));
                if (!isExist) {
                    IVariableNode var = findCorrespondingVar(newVar, parameters);
                    if (var == null) {
                        var = new VariableNode();
                        var.setRawType(VariableTypeUtils.BASIC.CHARACTER.CHAR);
                        var.setCoreType(VariableTypeUtils.BASIC.CHARACTER.CHAR);
                        var.setName(newVar.getOriginalName());
                    }
                    parameters.add(var);
                }
            }

            addSizeOfArrayOrPointerDependencies(se, parameters, functionNode);

            // generate smt-lib2
            SmtLibGeneration smt = new SmtLibGeneration(parameters, se.getNormalizedPathConstraints(), functionNode,
                    se.getNewVariables());
            smt.generate();
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
                z3Runner.execute();

                String originalSolution = z3Runner.getSolution();
                String staticSolution = new Z3SolutionParser().getSolution(originalSolution);

                logger.debug("Original Solution:\n" + originalSolution);
                logger.debug("Static Solution:\n" + staticSolution);

                onZ3Solution(se, parameters, originalSolution);

                if (!staticSolution.trim().isEmpty()) {
                    // Add stub count and reset it
                    for (StubVar stVar : stubVarCount) {
                        String stubCountString = stVar.stubName + "=" + stVar.count + ";";
                        staticSolution = staticSolution.concat(stubCountString);
                    }

                    String md5 = Utils.computeMd5(staticSolution);
                    if (!generatedTestcases.contains(md5)) {
                        generatedTestcases.add(md5);
                        logger.debug("The next test data = " + staticSolution);
                        logger.debug("Convert to standard format");
                        ValueToTestcaseConverter_UnknownSize converter = new ValueToTestcaseConverter_UnknownSize(
                                staticSolution);
                        List<RandomValue> randomValues = converter.convert();
                        logger.debug(randomValues);
                        // xu ly truong hop so sanh hai con tro (can toi uu lai)
                        // TODO: doi getConstraints() thanh normalize....
                        for (PathConstraint constraint : (PathConstraints) se.getConstraints()) {
                            // normalize constraint
                            ConstraintNormalizer norm = new ConstraintNormalizer();
                            norm.setOriginalSourcecode(constraint.getConstraint());
                            norm.normalize();
                            // convert to AST
                            IASTNode astConstraint = Utils.convertToIAST(norm.getNormalizedSourcecode());
                            ASTVisitor visitor = new ASTVisitor() {
                                @Override
                                public int visit(IASTExpression expression) {
                                    if (expression instanceof IASTBinaryExpression) {
                                        if (((IASTBinaryExpression) expression)
                                                .getOperator() == IASTBinaryExpression.op_equals) {
                                            String leftOperand = ((IASTBinaryExpression) expression).getOperand1()
                                                    .getRawSignature();
                                            String rightOperand = ((IASTBinaryExpression) expression).getOperand2()
                                                    .getRawSignature();
                                            // swap leftOp with rightOp if neccessary
                                            if (rightOperand.contains(leftOperand)) {
                                                String tmp = rightOperand;
                                                rightOperand = leftOperand;
                                                leftOperand = tmp;
                                            }
                                            if (!rightOperand.equals("NULL")) {
                                                RandomValueForAssignment newValue = new RandomValueForAssignment(
                                                        leftOperand, rightOperand);
                                                randomValues.add(newValue);
                                            }
                                        }
                                    }
                                    return super.visit(expression);
                                }
                            };
                            visitor.shouldVisitExpressions = true;
                            astConstraint.accept(visitor);
                        }

                        theNextTestdata = randomValues;
                    } else {
                        logger.debug("The next test data exists! Ignoring...");
                        theNextTestdata = new ArrayList<>();
                    }
                }
            } else
                throw new Exception("Z3 path " + z3Path + " does not exist");
        } catch (Exception e) {
            UIController.showErrorDialog(e.getMessage(), "Something wrong", "");
            e.printStackTrace();
        }

//        theNextTestdata = handleStringInTestData(theNextTestdata);
        return theNextTestdata;
    }

    private IVariableNode findCorrespondingVar(NewVariableInSe newVar, List<IVariableNode> variableNodes) {
        String name = newVar.getOriginalName().trim();
        boolean isChildOfArray = name.endsWith("]");
        String encodedName = name.replaceAll(IRegex.ARRAY_INDEX, SpecialCharacter.EMPTY);
        String[] nameItems = encodedName.split("\\.");
        IVariableNode lastNode = variableNodes.stream()
                .filter(v -> v.getName().equals(nameItems[0]))
                .findFirst()
                .orElse(null);
        if (lastNode != null) {
            for (int i = 1; i < nameItems.length; i++) {
                List<Level> space = new VariableSearchingSpace(lastNode).getSpaces();
                List<INode> nodes = Search.searchInSpace(space, new VariableNodeCondition(), nameItems[i]);
                if (!nodes.isEmpty()) {
                    lastNode = (IVariableNode) nodes.get(0);
                } else {
                    return null;
                }
            }

            if (isChildOfArray) {
                try {
                    ValueDataNode dataNode = new InitialTreeGen().genInitialTree((VariableNode) lastNode,
                            new RootDataNode());
                    int dim = Utils.getIndexOfArray(name).size();
                    ValueDataNode childNode;
                    if (dim > 1) {
                        childNode = (ValueDataNode) new TreeExpander().generateArrayItem(name, dataNode);
                    } else {
                        new InputCellHandler().commitEdit(dataNode, "1");
                        childNode = (ValueDataNode) dataNode.getChildren().get(0);
                    }
                    IVariableNode v = childNode.getCorrespondingVar();
                    v.setName(newVar.getOriginalName());
                    return v;
                } catch (Exception e) {
                    return null;
                }
            } else {
                IVariableNode v = lastNode.clone();
                v.setName(newVar.getOriginalName());
                return v;
            }
        }

        return null;
    }

    private List<StubVar> collectStubVar(Parameters stubParameters, List<ICfgNode> testpath) {
        for (int i = 0; i < testpath.size(); i++) {
            ICfgNode node = testpath.get(i);
            if (node instanceof NormalCfgNode) {
                IASTNode nodeAst = ((NormalCfgNode) node).getAst();
                if (nodeAst instanceof IASTBinaryExpression) {
                    handleRecursiveBinaryExp((IASTBinaryExpression) nodeAst, stubParameters);
                } else if (nodeAst instanceof IASTDeclarationStatement) {
                    IASTDeclaration declaration = ((IASTDeclarationStatement) nodeAst).getDeclaration();
                    if (declaration instanceof IASTSimpleDeclaration) {
                        IASTDeclarator declarator = ((IASTSimpleDeclaration) declaration).getDeclarators()[0];
                        IASTInitializer initializer = declarator.getInitializer();
                        if (initializer instanceof IASTEqualsInitializer) {
                            IASTInitializerClause clause = ((IASTEqualsInitializer) initializer).getInitializerClause();
                            if (clause instanceof IASTFunctionCallExpression) {
                                handleStubVar(stubParameters, (IASTExpression) clause);
                            } else if (clause instanceof IASTUnaryExpression) {
                                handleRecursiveUnaryExp((IASTUnaryExpression) clause, stubParameters);
                            } else if (clause instanceof IASTBinaryExpression) {
                                handleRecursiveBinaryExp((IASTBinaryExpression) clause, stubParameters);
                            }
                        }

                    }
                } else if (nodeAst instanceof IASTUnaryExpression) {
                    handleRecursiveUnaryExp((IASTUnaryExpression) nodeAst, stubParameters);
                }
                if (nodeAst instanceof IASTExpressionStatement) {
                    IASTExpression op = ((IASTExpressionStatement) nodeAst).getExpression();
                    if (op instanceof IASTBinaryExpression) {
                        handleRecursiveBinaryExp((IASTBinaryExpression) op, stubParameters);
                    }
                }
            }

        }
        List<StubVar> stubDependencyCall = stubCall;
        return stubDependencyCall;
    }

    private void handleRecursiveBinaryExp(IASTBinaryExpression nodeAst, Parameters stubParameters) {
        IASTExpression op1 = ((IASTBinaryExpression) nodeAst).getOperand1();
        IASTExpression op2 = ((IASTBinaryExpression) nodeAst).getOperand2();
        if (op1 instanceof IASTFunctionCallExpression) {
            handleStubVar(stubParameters, op1);
        } else if (op1 instanceof IASTBinaryExpression) {
            handleRecursiveBinaryExp((IASTBinaryExpression) op1, stubParameters);
        } else if (op1 instanceof IASTUnaryExpression) {
            handleRecursiveUnaryExp((IASTUnaryExpression) op1, stubParameters);
        }
        if (op2 instanceof IASTFunctionCallExpression) {
            handleStubVar(stubParameters, op2);
        } else if (op2 instanceof IASTBinaryExpression) {
            handleRecursiveBinaryExp((IASTBinaryExpression) op2, stubParameters);
        } else if (op2 instanceof IASTUnaryExpression) {
            handleRecursiveUnaryExp((IASTUnaryExpression) op2, stubParameters);
        }
    }

    private void handleRecursiveUnaryExp(IASTUnaryExpression nodeAst, Parameters stubParameters) {
        IASTExpression op = ((IASTUnaryExpression) nodeAst).getOperand();
        if (op instanceof IASTFunctionCallExpression) {
            handleStubVar(stubParameters, op);
        } else if (op instanceof IASTUnaryExpression) {
            int operator = ((IASTUnaryExpression) op).getOperator();
            if (operator == 7 || operator == 11) {
                IASTExpression operand = ((IASTUnaryExpression) op).getOperand();
                if (operand instanceof IASTUnaryExpression) {
                    handleRecursiveUnaryExp((IASTUnaryExpression) operand, stubParameters);
                } else if (operand instanceof IASTBinaryExpression) {
                    handleRecursiveBinaryExp((IASTBinaryExpression) operand, stubParameters);
                }
            }
        } else if (op instanceof IASTBinaryExpression) {
            handleRecursiveBinaryExp((IASTBinaryExpression) op, stubParameters);
        }
    }

    private void handleStubVar(Parameters stubParams, IASTNode op) {
        String originalName = op.getRawSignature();
        for (int j = 0; j < listStubNode.size(); j++) {
            Node stubNode = listStubNode.get(j);
            if (stubNode instanceof FunctionNode) {
                String reducedOpName = originalName.substring(0, originalName.indexOf("("));
                String reducedStubName = stubNode.getName().substring(0, stubNode.getName().indexOf("("));
                if (reducedOpName.equals(reducedStubName)) {

                    int count = stubCall.get(j).count + 1;
                    stubCall.get(j).setCount(count);
                    String stubName = "akaut_stub_" + reducedStubName + "_call" + count;

                    VariableNode stubVar = new InternalVariableNode();
                    String stubNodeDeclSpecifier = ((FunctionNode) stubNode).getAST().getDeclSpecifier().toString();
                    IASTPointerOperator[] pointerOps = ((FunctionNode) stubNode).getAST().getDeclarator()
                            .getPointerOperators();
                    for (int i = 0; i < pointerOps.length; i++) {
                        stubNodeDeclSpecifier += "*";
                    }
                    IASTDeclarationStatement declareAst = ASTUtils.generateDeclarationStatement(stubNodeDeclSpecifier);
                    stubVar.setAST(declareAst.getDeclaration());
                    stubVar.setName(stubName);
                    stubParams.add(stubVar);
                    break;
                }
            }
        }
    }

    protected boolean foundSolution(ISymbolicExecution se, Parameters parameters, ICommonFunctionNode functionNode)
            throws Exception {
        SmtLibGeneration smt = new SmtLibGeneration(parameters, se.getNormalizedPathConstraints(), functionNode,
                se.getNewVariables());
        smt.generate();
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
            z3Runner.execute();
            if (!z3Runner.getSolution().contains("unsat")) {
                return true;
            }

        }
        return false;
    }

    protected void addSizeOfArrayOrPointerDependencies(ISymbolicExecution se, Parameters parameters,
            ICommonFunctionNode functionNode) throws Exception {
        if (foundSolution(se, parameters, functionNode)) {
            Set<SizeOfArrayOrPointerDependency> dependencies = new HashSet<>();

            List<IVariableNode> fnArguments = functionNode.getArguments();
            if (fnArguments != null) {
                for (IVariableNode arg : fnArguments) {
                    if (arg.isTypeDependencyState()) {
                        List<Dependency> argDependencies = arg.getDependencies();
                        if (argDependencies != null) {
                            for (Dependency dependency : argDependencies) {
                                if (dependency instanceof SizeOfArrayOrPointerDependency) {
                                    dependencies.add((SizeOfArrayOrPointerDependency) dependency);
                                }
                            }
                        }
                    }
                }
            }

            for (SizeOfArrayOrPointerDependency dependency : dependencies) {
                INode startArrow = dependency.getStartArrow();
                INode endArrow = dependency.getEndArrow();
                String constraintName = endArrow.getName() + " >= " + startArrow.getName();
                if (se.getConstraints() instanceof PathConstraints) {
                    PathConstraint constraint = new PathConstraint(constraintName, null,
                            PathConstraint.CREATE_FROM_DECISION);
                    ((PathConstraints) se.getConstraints()).add(constraint);
                    if (!foundSolution(se, parameters, functionNode)) {
                        ((PathConstraints) se.getConstraints()).remove(constraint);
                    }
                }
            }
        }
    }

    private List<RandomValue> handleStringInTestData(List<RandomValue> testdata) {
        List<RandomValue> additionalTestData = new ArrayList<>();
        for (RandomValue rv : testdata) {
            String k = rv.getNameUsedInExpansion();
            String v = rv.getValue();
            if (v.startsWith("\"") && v.endsWith("\"")) {
                int size = v.length() - 2;
                additionalTestData.add(new RandomValueForSizeOf(k, Integer.toString(size)));
                for (int i = 0; i < size; i++) {
                    additionalTestData
                            .add(new RandomValueForSizeOf(k + "[" + i + "]", Integer.toString(v.charAt(1 + i))));
                }
            }
        }
        testdata.removeIf(rv -> (rv.getNameUsedInExpansion().startsWith("\"") && rv.getNameUsedInExpansion()
                .endsWith("\"")));
        testdata.addAll(additionalTestData);
        return testdata;
    }

    protected void onZ3Solution(ISymbolicExecution se, Parameters parameters, String solution) {

    }
}