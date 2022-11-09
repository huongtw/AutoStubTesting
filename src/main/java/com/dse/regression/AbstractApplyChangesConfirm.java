package com.dse.regression;

import com.dse.config.WorkspaceConfig;
import com.dse.environment.Environment;
import com.dse.exception.FunctionNodeNotFoundException;
import com.dse.guifx_v3.helps.UIController;
import com.dse.guifx_v3.objects.DefaultTreeTableCell;
import com.dse.logger.AkaLogger;
import com.dse.parser.dependency.Dependency;
import com.dse.parser.dependency.ExtendDependency;
import com.dse.parser.dependency.FunctionCallDependency;
import com.dse.parser.dependency.TypeDependency;
import com.dse.parser.object.*;
import com.dse.probe_point_manager.ProbePointManager;
import com.dse.probe_point_manager.objects.CheckBoxTreeTableRowForProbePoint;
import com.dse.probe_point_manager.objects.ProbePoint;
import com.dse.regression.objects.Reason;
import com.dse.search.Search;
import com.dse.search.condition.FunctionNodeCondition;
import com.dse.search.condition.NodeCondition;
import com.dse.search.condition.SourcecodeFileNodeCondition;
import com.dse.testcase_manager.TestCase;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.testcasescript.TestcaseSearch;
import com.dse.testcasescript.object.*;
import com.dse.thread.task.IncrementalBuildEnvironmentLoaderTask;
import com.dse.util.PathUtils;
import com.dse.util.Utils;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public abstract class AbstractApplyChangesConfirm implements Initializable {

    private final static AkaLogger logger = AkaLogger.get(IncrementalBuildingConfirmWindowController.class);

    @FXML
    protected TextArea taSummery;

    @FXML
    protected TreeTableView<ITestcaseNode> ttvDeletedTestCases;

    @FXML
    protected TreeTableView<ITestcaseNode> ttvAffectedTestCases;

    @FXML
    protected TreeTableView<ITestcaseNode> ttvNoneAffectionTestCases;

    @FXML
    protected TreeTableView<ITestcaseNode> ttvUnknownAffectTestCases;

    @FXML
    protected TreeTableColumn<ITestcaseNode, String> colNameDeleted;
    @FXML
    protected TreeTableColumn<ITestcaseNode, String> colNameAffected;
    @FXML
    protected TreeTableColumn<ITestcaseNode, String> colNameNoneAffection;
    @FXML
    protected TreeTableColumn<ITestcaseNode, String> colNameUnknownAffection;
    @FXML
    protected TextArea taReason;
    @FXML
    protected TextArea taInvalidProbePoints;

    protected Stage stage;
    protected List<TestNameNode> deletedTestCases = new ArrayList<>();
    protected List<TestNameNode> affectedTestCases = new ArrayList<>();
    protected List<TestNameNode> noneAffectionTestCases = new ArrayList<>();
    protected List<TestNameNode> unknownAffectionTestCases = new ArrayList<>();
    protected List<INode> affectedNodes = new ArrayList<>();
    protected boolean isConfirm = false;

    protected IncrementalBuildEnvironmentLoaderTask.AllowEditModeListener listener;

    // this list contains all function nodes that need to remove all probe point
    protected List<IFunctionNode> needToRemoveAllPPFunctionNodes = new ArrayList<>();

    public void initialize(URL location, ResourceBundle resources) {
        ITestcaseNode testcaseScripRootNode = Environment.getBackupEnvironment().getTestcaseScriptRootNode();
        TestCasesTreeItemForIncrementalBuild ttvDeletedTCRoot = new TestCasesTreeItemForIncrementalBuild(testcaseScripRootNode);
        ttvDeletedTestCases.setRoot(ttvDeletedTCRoot);
        // CheckBoxTreeTableRowForProbePoint can be used here
        ttvDeletedTestCases.setRowFactory(param -> getRow());
        colNameDeleted.setCellFactory(param -> new DefaultTreeTableCell<>());

        TestCasesTreeItemForIncrementalBuild ttvAffectedTCRoot = new TestCasesTreeItemForIncrementalBuild(testcaseScripRootNode);
        ttvAffectedTestCases.setRoot(ttvAffectedTCRoot);
        ttvAffectedTestCases.setRowFactory(param -> getRow());

        colNameAffected.setCellFactory(param -> new DefaultTreeTableCell<>());

        TestCasesTreeItemForIncrementalBuild ttvNoneAffectTCRoot = new TestCasesTreeItemForIncrementalBuild(testcaseScripRootNode);
        ttvNoneAffectionTestCases.setRoot(ttvNoneAffectTCRoot);
        ttvNoneAffectionTestCases.setRowFactory(param -> getRow());
        colNameNoneAffection.setCellFactory(param -> new DefaultTreeTableCell<>());

        TestCasesTreeItemForIncrementalBuild ttvUnknowAffectTCRoot = new TestCasesTreeItemForIncrementalBuild(testcaseScripRootNode);
        ttvUnknownAffectTestCases.setRoot(ttvUnknowAffectTCRoot);
        ttvUnknownAffectTestCases.setRowFactory(param -> getRow());
        colNameUnknownAffection.setCellFactory(param -> new DefaultTreeTableCell<>());

        //todo: scan all single test cases in the backup environment
        // (done)detect test cases of deleted functions
        // detect test cases failed to import from disk (due to function not found error)
        // (done)detect test cases that is affected (rely on affected nodes)
        // (done)detect test cases that is affected and none edffected (rely on test path)
        // (done)the remains is unknown affect testcases

        ReasonManager.clear();

        // search all testcase name in the backup TestcaseRootNode
        TestcaseRootNode root = Environment.getBackupEnvironment().getTestcaseScriptRootNode();
        List<ITestcaseNode> poolTestCases = TestcaseSearch.searchNode(root, new TestNameNode());
        deletedTestCases = detectTestCasesOfDeletedSubprograms(poolTestCases);
        setCellValueFactoryForColName(colNameDeleted, deletedTestCases);

        detectAffectedNodes();

        // test cases of affected nodes are affected
        detectAffectedTestCasesRelyOnAffectedNodes(poolTestCases, affectedTestCases);

        // with the remain test cases in environment, use test path to find out affected, none affected test case,
        // after that, remain test cases in poolTestCases are unknown affect test cases (because they don't have test path, haven't executed yet)
        detectAffectedNoneAffectedTestCases(poolTestCases, affectedTestCases, noneAffectionTestCases);

        setCellValueFactoryForColName(colNameAffected, affectedTestCases);
        setCellValueFactoryForColName(colNameNoneAffection, noneAffectionTestCases);
        for (ITestcaseNode node : poolTestCases) {
            unknownAffectionTestCases.add((TestNameNode) node);
        }
        setCellValueFactoryForColName(colNameUnknownAffection, unknownAffectionTestCases);

        for (TestNameNode nameNode : noneAffectionTestCases) {
            Reason reason = new Reason(nameNode, Reason.STATUS_NONE_AFFECTION, null);
            ReasonManager.putToReasonMap(nameNode, reason);
        }

        for (TestNameNode nameNode : unknownAffectionTestCases) {
            Reason reason = new Reason(nameNode, Reason.STATUS_NA, null);
            ReasonManager.putToReasonMap(nameNode, reason);
        }

        addListeners(ttvDeletedTestCases);
        addListeners(ttvAffectedTestCases);
        addListeners(ttvNoneAffectionTestCases);
        addListeners(ttvUnknownAffectTestCases);

        // warning about will be deleted probe points
        StringBuilder builder = new StringBuilder();
        List<ProbePoint> probePoints = new ArrayList<>();
        for (IFunctionNode functionNode : needToRemoveAllPPFunctionNodes) {
            builder.append(functionNode.getFullName()).append(":\n");
            List<ProbePoint> list = ProbePointManager.getInstance().getFunPpMap().get(functionNode);
            if (list != null) {
                for (ProbePoint pp : list) {
                    probePoints.add(pp);
                    builder.append("\t+").append(pp.getName()).append("\n");
                }
            }
            builder.append("\n");
        }
        taInvalidProbePoints.setText(builder.toString());

        builder = new StringBuilder();
        builder.append("If Ok some changes will be apply: \n");
        builder.append("    +").append(deletedTestCases.size()).append(" test cases will be deleted.\n");
        builder.append("    +").append(affectedTestCases.size()).append(" test cases will be affected.\n");
        builder.append("    +").append(unknownAffectionTestCases.size()).append(" test cases are not known whether are affected or not.\n");
        builder.append("    +").append(probePoints.size()).append(" probe points will be invalid.\n");
        setTextSummery(builder.toString());
    }

    private CheckBoxTreeTableRowForProbePoint<ITestcaseNode> getRow() {
        CheckBoxTreeTableRowForProbePoint<ITestcaseNode> row = new CheckBoxTreeTableRowForProbePoint<>();
        row.getCheckBox().setDisable(true);
        return row;
    }

    private void addListeners(TreeTableView<ITestcaseNode> treeTableView) {
        treeTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.getValue() != null) {
                ITestcaseNode testcaseNode = newValue.getValue();
                if (testcaseNode instanceof TestNormalSubprogramNode) {
                    TestNormalSubprogramNode cast = (TestNormalSubprogramNode) testcaseNode;
                    try {
                        ICommonFunctionNode functionNode = UIController.searchFunctionNodeByPathInBackupEnvironment(cast.getName());
                        taReason.setText(ReasonManager.getFullReason(functionNode));
                    } catch (FunctionNodeNotFoundException e) {
                        logger.debug("FunctionNodeNotFound: " + cast.getName());
                    }
                } else if (testcaseNode instanceof TestNewNode) {
                    List<ITestcaseNode> nameNodes = TestcaseSearch.searchNode(testcaseNode, new TestNameNode());
                    if (nameNodes.size() == 1) {
                        taReason.setText(ReasonManager.getFullReason(nameNodes.get(0)));
                    } else {
                        logger.error("There are 2 TestNameNode in a test case");
                    }
                } else {
                    taReason.setText("");
                }
            }
        });
    }

    // add IFunctionNode to affectedNodes to know test cases that are affected
    private void addToAffectedNodes(List<INode> affectedNodes, INode node) {
        List<String> deletedPaths = ChangesBetweenSourcecodeFiles.deletedPaths;
        // nodes are deleted can not be added to affectedNodes
        if (!deletedPaths.contains(node.getAbsolutePath()) && !affectedNodes.contains(node)) {
            // need to handled dependency types: FunctionCallDependency, ExtendDependency, TypeDependency
            // (done) handle FunctionCallDependency
            // (done) handle ExtendDependency
            // (done) handle TypeDependency for arguments

            // if a function node is affected then all functions that call this function are affected nodes too
            if (node instanceof IFunctionNode) {
                affectedNodes.add(node);
                for (Dependency dependency : node.getDependencies()) {
                    if (dependency instanceof FunctionCallDependency && dependency.getEndArrow() == node) {
                        INode start = dependency.getStartArrow();
                        if (!ChangesBetweenSourcecodeFiles.modifiedNodes.contains(start)) {
                            Reason reason = new Reason(start, Reason.STATUS_AFFECTED, node);
                            ReasonManager.putToReasonMap(start, reason);

                            addToAffectedNodes(affectedNodes, start);
                        }
                    }
                }
            } else if (node instanceof StructureNode) {
                // StructureNode is about: ClassNode, EnumNode, EnumTypeDefNode, SpecialEnumTypedefNode,
                // SpecialUnionTypedefNode, StructNode, StructOrClassNode, StructTypedefNode, UnionNode, UnionTypedefNode

                // if a StructureNode is affected then all StructureNodes that extend this structureNode are affected nodes too
                // and all functions that have arguments is instance of that structure nodes are affected nodes too
                for (Dependency dependency : node.getDependencies()) {
                    if (dependency instanceof TypeDependency && dependency.getEndArrow() == node) {
                        INode start = dependency.getStartArrow();
                        logger.debug("TypeDependency:");
                        if (start instanceof InternalVariableNode) {
                            InternalVariableNode internalVariableNode = (InternalVariableNode) start;
                            logger.debug("Start: " + start.getAbsolutePath());
                            logger.debug("End: " + dependency.getEndArrow().getAbsolutePath());
                            INode functionNode = internalVariableNode.getParent();

                            Reason reason = new Reason(functionNode, Reason.STATUS_AFFECTED, node);
                            ReasonManager.putToReasonMap(functionNode, reason);

                            addToAffectedNodes(affectedNodes, functionNode);
                        }
                    } else if (dependency instanceof ExtendDependency && dependency.getEndArrow() == node) {
                        // if a Structure Node "A" is affected then all Structure nodes that extend "A" are affected too
                        INode start = dependency.getStartArrow();
                        logger.debug("ExtendDependency:");
                        if (start instanceof StructureNode) {
                            StructureNode child = (StructureNode) start;
                            logger.debug("Start: " + child.getAbsolutePath());
                            logger.debug("End: " + node.getAbsolutePath());

                            Reason reason = new Reason(start, Reason.STATUS_AFFECTED, node);
                            ReasonManager.putToReasonMap(start, reason);

                            addToAffectedNodes(affectedNodes, child);
                        }
                    }
                }

                // if the Structure Node is a Class Node or Struct Node then all methods + constructors + destructors
                // of that Node are affected
                if (node instanceof StructOrClassNode) {
                    List<INode> methods = Search.searchNodes(node, new FunctionNodeCondition());
                    for (INode method : methods) {
                        Reason reason = new Reason(method, Reason.STATUS_AFFECTED, node);
                        ReasonManager.putToReasonMap(method, reason);

                        addToAffectedNodes(affectedNodes, method);
                    }
                }
            }
        }
    }

    /**
     * Detect all Function nodes that use affected nodes.
     * And add modified function node to needToRemoveAllPPFunctionNodes
     */
    private void detectAffectedNodes() {
        List<INode> tmpModifiedNodes = ChangesBetweenSourcecodeFiles.modifiedNodes;
        // these modifiedNodes are in the temporary environment, has incorrect, lacks of dependencies
        // so need to get correspond modifiedNodes in the backup environment
        List<INode> modifiedNodes = new ArrayList<>();
        Environment backupEnv = Environment.getBackupEnvironment();
        for (INode node : tmpModifiedNodes) {
            List<INode> nodes = Search.searchNodes(backupEnv.getProjectNode(), new NodeCondition(), node.getAbsolutePath());
            if (nodes.size() == 1) {
                INode correspondNode = nodes.get(0);
                modifiedNodes.add(correspondNode);

                if (correspondNode instanceof IFunctionNode) {
                    if (!needToRemoveAllPPFunctionNodes.contains((IFunctionNode) correspondNode)) {
                        needToRemoveAllPPFunctionNodes.add((IFunctionNode) correspondNode);
                    }
                }
            } else if (nodes.size() > 1) {
                // temporary fix
                for (INode n : nodes) {
                    if (!(n instanceof DefinitionFunctionNode)) {
                        modifiedNodes.add(n);

                        if (n instanceof IFunctionNode) {
                            if (!needToRemoveAllPPFunctionNodes.contains((IFunctionNode) n)) {
                                needToRemoveAllPPFunctionNodes.add((IFunctionNode) n);
                            }
                        }
                        break;
                    }
                }
            } else {
                logger.debug("Failed to search correspond node in the backup Environment.");
            }
        }
        // todo: put reason for deleted subprogram

        for (INode modifiedNode : modifiedNodes) {
            Reason reason = new Reason(modifiedNode, Reason.STATUS_MODIFIED, null);
            ReasonManager.putToReasonMap(modifiedNode, reason);
        }

        for (INode modifiedNode : modifiedNodes) {
            if (modifiedNode instanceof IFunctionNode || modifiedNode instanceof StructureNode)
                addToAffectedNodes(affectedNodes, modifiedNode);
        }
    }

    /**
     * Detect test case of deleted subprograms.
     * And add deleted functions to needToRemoveAllPPFunctionNodes
     *
     * @param poolTestCases pool of test cases (all existing test cases)
     * @return list of should be deleted test cases in the pool
     */
    private List<TestNameNode> detectTestCasesOfDeletedSubprograms(List<ITestcaseNode> poolTestCases) {
        List<TestNameNode> testNameNodes = new ArrayList<>();
        List<String> deletedPaths = ChangesBetweenSourcecodeFiles.deletedPaths;
        // search all subprogram node in the backup TestcaseRootNode
        TestcaseRootNode root = Environment.getBackupEnvironment().getTestcaseScriptRootNode();
        List<ITestcaseNode> subprograms = TestcaseSearch.searchNode(root, new TestNormalSubprogramNode());
        for (ITestcaseNode subprogram : subprograms) {
            String path = ((TestNormalSubprogramNode) subprogram).getName();
            if (deletedPaths.contains(path)) {
                List<ITestcaseNode> nameNodes = TestcaseSearch.searchNode(subprogram, new TestNameNode());
                try {
                    ICommonFunctionNode functionNode = UIController.searchFunctionNodeByPathInBackupEnvironment(path);
                    if (functionNode instanceof IFunctionNode && !needToRemoveAllPPFunctionNodes.contains(functionNode)) {
                        needToRemoveAllPPFunctionNodes.add((IFunctionNode) functionNode);
                    }
                    Reason reason = new Reason(functionNode, Reason.STATUS_DELETED, null);
                    ReasonManager.putToReasonMap(functionNode, reason);

                    for (ITestcaseNode node : nameNodes) {
                        if (poolTestCases.contains(node)) {
                            testNameNodes.add((TestNameNode) node);
                            poolTestCases.remove(node);

                            Reason reason2 = new Reason(node, Reason.STATUS_DELETED, functionNode);
                            ReasonManager.putToReasonMap(node, reason2);
                        }
                    }
                } catch (FunctionNodeNotFoundException e) {
                    logger.error("FunctionNodeNotFound: " + path);
                }
            }
        }

        return testNameNodes;
    }


    private void detectAffectedTestCasesRelyOnAffectedNodes(List<ITestcaseNode> poolTestCases, List<TestNameNode> affectedTestCases) {
        List<String> affectedNodePaths = new ArrayList<>();
        for (INode affectedNode : affectedNodes) {
            String path = affectedNode.getAbsolutePath();
            if (Utils.isWindows()) path = Utils.doubleNormalizePath(path);
            affectedNodePaths.add(path);
        }

        // search all subprogram node in the backup TestcaseRootNode
        TestcaseRootNode root = Environment.getBackupEnvironment().getTestcaseScriptRootNode();
        List<ITestcaseNode> subprograms = TestcaseSearch.searchNode(root, new TestNormalSubprogramNode());
        for (ITestcaseNode subprogram : subprograms) {
            String path = ((TestNormalSubprogramNode) subprogram).getName();
            if (affectedNodePaths.contains(path)) {
                try {
                    ICommonFunctionNode functionNode = UIController.searchFunctionNodeByPathInBackupEnvironment(path);
                    List<ITestcaseNode> nameNodes = TestcaseSearch.searchNode(subprogram, new TestNameNode());
                    for (ITestcaseNode node : nameNodes) {
                        if (poolTestCases.contains(node)) {
                            affectedTestCases.add((TestNameNode) node);
                            poolTestCases.remove(node);

                            Reason reason = new Reason(node, Reason.STATUS_AFFECTED, functionNode);
                            ReasonManager.putToReasonMap(node, reason);
                        }
                    }
                } catch (FunctionNodeNotFoundException e) {
                    logger.error("FunctionNodeNotFound: " + path);
                }
            }
        }
    }

    private void detectAffectedNoneAffectedTestCases(List<ITestcaseNode> poolTestCases, List<TestNameNode> affectedTestCases,
                                                     List<TestNameNode> noneAffectedTestCases) {

        // for each testcase in poolTestCases, read test path file if existed
        for (ITestcaseNode node : poolTestCases) {
            String name = ((TestNameNode) node).getName();
            if (name != null && name.length() > 0 && node.getParent() != null && node.getParent().getParent() instanceof TestNormalSubprogramNode) {
                File testpathFile = new File(new WorkspaceConfig().fromJson().getTestpathDirectory() + File.separator + name + ".tp");
                if (testpathFile.exists()) {
                    String content = Utils.readFileContent(testpathFile);
                    boolean isAffected = false;
                    for (INode affectedNode : affectedNodes) {
                        // for each absolute path of modified function, affected function, check if the path is in the test path file
                        if (content.contains(affectedNode.getAbsolutePath())) {
                            // add the TestNameNode to affectedTestCaseNames
                            affectedTestCases.add((TestNameNode) node);
                            isAffected = true;
                            break;
                        }
                    }
                    if (!isAffected) {
                        // add the TestNameNode to noneAffectedTestCaseNames
                        noneAffectedTestCases.add((TestNameNode) node);
                    }
                } else {
                    logger.debug("Test path file not found: " + testpathFile);
                }
            }
        }

        for (ITestcaseNode testcaseNode : affectedTestCases) {
            poolTestCases.remove(testcaseNode);
        }

        for (ITestcaseNode testcaseNode : noneAffectedTestCases) {
            poolTestCases.remove(testcaseNode);
        }
    }

    private String getLabelForSubprogramNode(TestSubprogramNode node) {
        String absolutePath = node.getName();
        if (ChangesBetweenSourcecodeFiles.deletedPaths.contains(absolutePath))
            return LABEL_DELETED;

        for (INode n : ChangesBetweenSourcecodeFiles.modifiedNodes) {
            if (PathUtils.equals(n.getAbsolutePath(), absolutePath)) {
                return LABEL_MODIFIED;
            }
        }

        for (INode n : affectedNodes) {
            if (PathUtils.equals(n.getAbsolutePath(), absolutePath)) {
                return LABEL_AFFECTED;
            }
        }
        return "";
    }

    private void setCellValueFactoryForColName(TreeTableColumn<ITestcaseNode, String> col, List<TestNameNode> testNameNodes) {
        col.setCellValueFactory((TreeTableColumn.CellDataFeatures<ITestcaseNode, String> param) -> {
            ITestcaseNode node = param.getValue().getValue();
            String name = "";
            if (node instanceof TestcaseRootNode) {
                name = new File(((TestcaseRootNode) node).getAbsolutePath()).getName();

            } else if (node instanceof TestSubprogramNode) {
                name = getLabelForSubprogramNode((TestSubprogramNode) node);
                try {
                    name += ((TestSubprogramNode) node).getSimpleNameToDisplayInTestcaseView();
                } catch (FunctionNodeNotFoundException e) {
                    e.printStackTrace();
                }
            } else if (node instanceof TestUnitNode) {
                // name of unit node is sometimes too long, need to shorten it.
                name = ((TestUnitNode) node).getShortNameToDisplayInTestcaseTree();

            } else if (node instanceof TestNewNode) {
                List<ITestcaseNode> nameNodes = TestcaseSearch.searchNode(node, new TestNameNode());
                name = ((TestNameNode) nameNodes.get(0)).getName();
                if (nameNodes.size() == 1) {
                    if (testNameNodes.contains(nameNodes.get(0))) {
                        ((CheckBoxTreeItem) param.getValue()).setSelected(true);
                    }
                } else {
                    logger.debug("[Error] there are 2 TestNameNode in a test case");
                }
            }

            return new ReadOnlyStringWrapper(name);
        });
    }

    protected void applyChange() {
        // delete "will be delete" test cases (2 parts: from disk and from MDI window)
        for (TestNameNode nameNode : deletedTestCases) {
            String name = nameNode.getName();
            // remove from disk
            TestCaseManager.removeBasicTestCase(name);
        }

        // restatus affected test cases
        for (TestNameNode nameNode : affectedTestCases) {
            String name = nameNode.getName();
            TestCase testCase = TestCaseManager.getBasicTestCaseByName(name);
            if (testCase != null) {
                testCase.setStatus(TestCase.STATUS_NA);
                // status displayed on test case navigator tree is read from test case file
                TestCaseManager.exportBasicTestCaseToFile(testCase);
            }
        }

        // update probe points, contains 3 steps:
        // +step 1 before reparse project: make all probe point of deleted, modified functions invalid
        // +step 2 when loading probe points: update line in source code file
        // +step 3 after clone environment: insert all include lines of probe points to source code file

        // Step 1: delete all probe point of functions in needToRemoveAllPPFunctionNodes
        for (IFunctionNode functionNode : needToRemoveAllPPFunctionNodes) {
            ProbePointManager.getInstance().moveAllProbePointsToInvalid(functionNode);
        }

        // update workspace with modification
        new WorkspaceUpdater().update();
    }

    @FXML
    public void ok() {
    }

    private List<ISourcecodeFileNode> getUpdatedModifiedSourceCodeFiles() {
        List<ISourcecodeFileNode> sourcecodeFileNodes = new ArrayList<>();
        for (INode iNode : ChangesBetweenSourcecodeFiles.modifiedSourcecodeFiles.keySet()) {
            List<INode> nodes = Search.searchNodes(Environment.getInstance().getProjectNode()
                    , new SourcecodeFileNodeCondition(),
                    iNode.getAbsolutePath());
            // todo: check the search method for window
            if (nodes.size() == 1) {
                sourcecodeFileNodes.add((ISourcecodeFileNode) nodes.get(0));
            } else {
                logger.error("Find " + nodes.size() + " source code file node. Path "
                        + iNode.getAbsolutePath());
            }
        }
        return sourcecodeFileNodes;
    }

    @FXML
    public void cancel() {
        isConfirm = false;
        if (stage != null) {
            stage.close();
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setTextSummery(String textSumery) {
        taSummery.setText(textSumery);
    }

    public Stage getStage() {
        return stage;
    }

    public boolean isConfirm() {
        return isConfirm;
    }

    public void setListener(IncrementalBuildEnvironmentLoaderTask.AllowEditModeListener listener) {
        this.listener = listener;
    }

    public final static String LABEL_DELETED = "[DELETED] ";
    public final static String LABEL_MODIFIED = "[MODIFIED] ";
    public final static String LABEL_AFFECTED = "[AFFECTED] ";
}
