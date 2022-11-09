package com.dse.guifx_v3.controllers;

import com.dse.coverage.CoverageManager;
import com.dse.environment.object.EnviroCoverageTypeNode;
import com.dse.guifx_v3.controllers.main_view.MDIWindowController;
import com.dse.guifx_v3.controllers.object.TestCasesNavigatorRow;
import com.dse.environment.Environment;
import com.dse.guifx_v3.objects.hint.Hint;
import com.dse.testcase_manager.TestPrototype;
import com.dse.thread.task.GenerateTestdataTask;
import com.dse.guifx_v3.helps.UIController;
import com.dse.guifx_v3.objects.DefaultTreeTableCell;
import com.dse.guifx_v3.objects.TestCasesTreeItem;
import com.dse.thread.task.testcase.DeleteTestCaseTask;
import com.dse.parser.object.ISourcecodeFileNode;
import com.dse.testcase_manager.CompoundTestCase;
import com.dse.testcase_manager.TestCase;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.testcasescript.TestcaseSearch;
import com.dse.testcasescript.object.*;
import com.dse.thread.AkaThread;
import com.dse.thread.AkaThreadManager;
import com.dse.logger.AkaLogger;
import com.dse.util.Utils;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class TestCasesNavigatorController implements Initializable {
    private final static AkaLogger logger = AkaLogger.get(TestCasesNavigatorController.class);

    /**
     * Singleton pattern like
     */
    private static AnchorPane navigatorTreeTable = null;
    private static TestCasesNavigatorController instance = null;

    private static void prepare() {
        FXMLLoader loader = new FXMLLoader(Object.class.getResource("/FXML/TestCasesNavigator.fxml"));
        try {
            Parent parent = loader.load();
            navigatorTreeTable = (AnchorPane) parent;
            instance = loader.getController();
        } catch (Exception e) {
            logger.error("Cant load test case navigator", e);
        }
    }

    public static AnchorPane getNavigatorTreeTable() {
        if (navigatorTreeTable == null) {
            prepare();
        }
        return navigatorTreeTable;
    }

    public static TestCasesNavigatorController getInstance() {
        if (instance == null) {
            prepare();
        }
        return instance;
    }

    @FXML
    private TreeTableView<ITestcaseNode> testCasesNavigator;
    @FXML
    private TreeTableColumn<ITestcaseNode, String> colName;
    @FXML
    private TreeTableColumn<ITestcaseNode, String> colStatus;
    @FXML
    private TreeTableColumn<ITestcaseNode, String> colCoverage;
    @FXML
    private Label lCoverage;
    @FXML
    private HBox colCoverageContent;

    private final BooleanProperty sourceCoverageView = new SimpleBooleanProperty();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Hint.tooltipNode(colCoverageContent, "Change coverage view mode");

        colCoverageContent.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                sourceCoverageView.set(!sourceCoverageView.get());
            }
        });

        sourceCoverageView.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean isSource) {
                lCoverage.setText(isSource ? "Source code" : "Function");
                colCoverage.setCellValueFactory(param -> loadProgress(param, isSource));
                refreshNavigatorTree();
            }
        });

        sourceCoverageView.set(true);

        testCasesNavigator.setRowFactory(param -> {
            TestCasesNavigatorRow row = new TestCasesNavigatorRow();
            row.setOnMouseClicked(event -> {
                try {
                    if (row.getItem() instanceof TestNewNode) {
                        if (event.getClickCount() == 2 && (!row.isEmpty())) {
                            TestNewNode testNewNode = (TestNewNode) row.getItem();
                            if (testNewNode.isPrototypeTestcase())
                                openPrototype(testNewNode);
                            else
                                openTestCase(testNewNode);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            row.setOnDragDetected(event -> {
                try {
                    if (!row.isEmpty()) {
                        if (row.getTreeItem().getValue() instanceof TestNewNode) {
                            TestNewNode testNewNode = (TestNewNode) row.getTreeItem().getValue();
                            Dragboard db = row.startDragAndDrop(TransferMode.ANY);
                            ClipboardContent content = new ClipboardContent();
                            List<ITestcaseNode> names = TestcaseSearch.searchNode(testNewNode, new TestNameNode());
                            if (names.size() == 1) {
                                content.putString(((TestNameNode) names.get(0)).getName());
                                db.setContent(content);
                            } else {
                                UIController.showErrorDialog("Can not add this test case to test compound", "Test compound generation", "Drag fail");
                            }
                            event.consume();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            return row;
        });

        // set the tree table to multi selection mode
        testCasesNavigator.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        // A listener for list selections, multiple selections in the TableView
        ListChangeListener<TreeItem<ITestcaseNode>> multiSelection = changed -> {
            try {
                testCasesNavigator.refresh();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        // register the listener on the ObservableList<TreeItem<ITestCaseNode>>
        testCasesNavigator.getSelectionModel().getSelectedItems().addListener(multiSelection);

        colName.setCellFactory(param -> new DefaultTreeTableCell<>());
        colName.setCellValueFactory((TreeTableColumn.CellDataFeatures<ITestcaseNode, String> param) -> {
            try {
                if (param.getValue() == null) {
                    return null;
                }
                ITestcaseNode node = param.getValue().getValue();
                String name = "";
                if (node instanceof TestcaseRootNode) {
                    name = new File(((TestcaseRootNode) node).getAbsolutePath()).getName();

                    int nThreads = AkaThreadManager.getTotalRunningThreads().size();
                    if (nThreads > 0)
                        name += " [" + AkaThreadManager.getTotalRunningThreads().size() + " threads]";

                } else if (node instanceof TestSubprogramNode) {
                    name = ((TestSubprogramNode) node).getSimpleNameToDisplayInTestcaseView();

                } else if (node instanceof TestUnitNode) {
                    // name of unit node is sometimes too long, need to shorten it.
                    name = ((TestUnitNode) node).getShortNameToDisplayInTestcaseTree();

                    ISourcecodeFileNode srcNode = UIController.searchSourceCodeFileNodeByPath((TestUnitNode) node,
                            Environment.getInstance().getProjectNode());

                    int nThreads = AkaThreadManager.getTotalRunningThreads(srcNode).size();
                    if (nThreads > 0)
                        name += " [" + AkaThreadManager.getTotalRunningThreads(srcNode).size() + " threads]";

                } else if (node instanceof TestNewNode) {
                    List<ITestcaseNode> nameNodes = TestcaseSearch.searchNode(node, new TestNameNode());
                    if (nameNodes.size() == 1) {
                        name = ((TestNameNode) nameNodes.get(0)).getName();

                    } else {
                        logger.debug("[Error] there are 2 TestNameNode in a test case");
                    }
                }

                return new ReadOnlyStringWrapper(name);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

        });

        colStatus.setCellValueFactory(this::loadStatus);
    }

    private StringProperty loadStatus(TreeTableColumn.CellDataFeatures<ITestcaseNode, String> param) {
        try {
            if (param.getValue() == null) {
                return null;
            }

            ITestcaseNode valueNode = param.getValue().getValue();

            if (valueNode instanceof TestNewNode) {
                //only test case need to represent the status and date
                TestNewNode testNewNode = (TestNewNode) param.getValue().getValue();
                List<ITestcaseNode> names = TestcaseSearch.searchNode(testNewNode, new TestNameNode());
                if (names.size() == 1) {
                    String name = ((TestNameNode) names.get(0)).getName();
                    String status = TestCaseManager.getStatusTestCaseByName(name);
                    if (status != null) {
                        return new SimpleStringProperty(status);
                    }
                } else {
                    logger.error("Unexpected result when display status of testcase: " + testNewNode.getName());
                }
            } else if (valueNode instanceof TestNormalSubprogramNode) {
                for (AkaThread thread : AkaThreadManager.akaThreadList)
                    if (thread.getTask() instanceof GenerateTestdataTask) {
                        if (((GenerateTestdataTask) thread.getTask()).getFunction().getAbsolutePath()
                                .equals(((TestNormalSubprogramNode) valueNode).getName())) {
                            if (((GenerateTestdataTask) thread.getTask()).isStillRunning())
                                return new SimpleStringProperty(((GenerateTestdataTask) thread.getTask()).getStatus());
                        }
                    }
                return new SimpleStringProperty("");

            } else if (valueNode instanceof TestUnitNode) {
                for (AkaThread thread : AkaThreadManager.akaThreadList)
                    if (thread.getTask() instanceof GenerateTestdataTask) {
                        String srcPath = Utils.getSourcecodeFile(((GenerateTestdataTask) thread.getTask()).getFunction()).getAbsolutePath();
                        if (((TestUnitNode) valueNode).getName().equals(srcPath)) {
                            if (((GenerateTestdataTask) thread.getTask()).isStillRunning())
                                return new SimpleStringProperty("generating");
                        }
                    }
                return new SimpleStringProperty("");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private StringProperty loadProgress(TreeTableColumn.CellDataFeatures<ITestcaseNode, String> param, boolean isSource) {
        try {
            if (param == null || param.getValue() == null || !(param.getValue().getValue() instanceof TestNewNode))
                return null;

            TestNewNode testNewNode = (TestNewNode) param.getValue().getValue();

            if (testNewNode.isPrototypeTestcase())
                return null;

            // only test case need to represent the coverage, status and date
            String name = testNewNode.getName();
            TestCase testCase = TestCaseManager.getBasicTestCaseByNameWithoutData(name);

            if (testCase == null)
                return null;

            String typeOfCoverage = Environment.getInstance().getTypeofCoverage();
            switch (typeOfCoverage) {
                case EnviroCoverageTypeNode.STATEMENT:
                case EnviroCoverageTypeNode.BRANCH:
                case EnviroCoverageTypeNode.BASIS_PATH:
                case EnviroCoverageTypeNode.MCDC: {
                    float coverage;
                    if (isSource)
                        coverage = CoverageManager.getProgress(testCase, typeOfCoverage);
                    else
                        coverage = CoverageManager.getFunctionProgress(testCase, typeOfCoverage);

                    return new SimpleStringProperty(Utils.round(coverage * 100, 4) + "%");
                }

                case EnviroCoverageTypeNode.STATEMENT_AND_BRANCH: {
                    float stmCoverage;
                    if (isSource)
                        stmCoverage = CoverageManager.getProgress(testCase, EnviroCoverageTypeNode.STATEMENT);
                    else
                        stmCoverage = CoverageManager.getFunctionProgress(testCase, EnviroCoverageTypeNode.STATEMENT);
                    float branchCoverage;
                    if (isSource)
                        branchCoverage = CoverageManager.getProgress(testCase, EnviroCoverageTypeNode.BRANCH);
                    else
                        branchCoverage = CoverageManager.getFunctionProgress(testCase, EnviroCoverageTypeNode.BRANCH);

                    return new SimpleStringProperty(Utils.round(stmCoverage * 100, 4) + "% / "
                            + Utils.round(branchCoverage * 100, 4) + "%");
                }

                case EnviroCoverageTypeNode.STATEMENT_AND_MCDC: {
                    float stmCoverage;
                    if (isSource)
                        stmCoverage = CoverageManager.getProgress(testCase, EnviroCoverageTypeNode.STATEMENT);
                    else
                        stmCoverage = CoverageManager.getFunctionProgress(testCase, EnviroCoverageTypeNode.STATEMENT);
                    float mcdcCoverage;
                    if (isSource)
                        mcdcCoverage = CoverageManager.getProgress(testCase, EnviroCoverageTypeNode.MCDC);
                    else
                        mcdcCoverage = CoverageManager.getFunctionProgress(testCase, EnviroCoverageTypeNode.MCDC);

                    return new SimpleStringProperty(Utils.round(stmCoverage * 100, 4) + "% / "
                            + Utils.round(mcdcCoverage * 100, 4) + "%");
                }

                default: {
                    return null;
                }
            }
        } catch (Exception e) {
            logger.error("Cant compute coverage progress of " + param.toString(), e);
        }

        return null;
    }

    // load content from root node
    public void loadContent(ITestcaseNode testcaseNode) {
        TestCasesTreeItem item = new TestCasesTreeItem(testcaseNode);
        this.testCasesNavigator.setRoot(item);
    }

    public void clear() {
        testCasesNavigator.setRoot(null);
    }

    public TreeTableView<ITestcaseNode> getTestCasesNavigator() {
        return testCasesNavigator;
    }

    public synchronized void refreshNavigatorTree() {
        testCasesNavigator.refresh();
    }

    public void refreshNavigatorTreeFromAnotherThread() {
        Platform.runLater(() -> testCasesNavigator.refresh());
    }

    public void openPrototype(TestNewNode testNewNode) {
        ITestcaseNode parent = testNewNode.getParent();
        List<ITestcaseNode> names = TestcaseSearch.searchNode(testNewNode, new TestNameNode());

        if (names.size() == 1) {
            String name = ((TestNameNode) names.get(0)).getName();
            if (parent instanceof TestNormalSubprogramNode) {
                TestPrototype testCase = TestCaseManager.getPrototypeByName(name);
                if (testCase != null) {
                    UIController.viewPrototype(testCase);
                }
            }
        } else {
            logger.error("There are more than two similar names in a prototype");
        }
    }

    public void openTestCase(TestNewNode testNewNode) {
        ITestcaseNode parent = testNewNode.getParent();
        List<ITestcaseNode> names = TestcaseSearch.searchNode(testNewNode, new TestNameNode());

        if (names.size() == 1) {
            String name = ((TestNameNode) names.get(0)).getName();
            if (parent instanceof TestNormalSubprogramNode) {
                TestCase testCase = TestCaseManager.getBasicTestCaseByName(name);
                if (testCase != null) {
                    UIController.viewTestCase(testCase);
                }
            } else if (parent instanceof TestCompoundSubprogramNode) {
                CompoundTestCase compoundTestCase = TestCaseManager.getCompoundTestCaseByName(name);
                if (compoundTestCase != null) {
                    UIController.viewTestCase(compoundTestCase);
                }
            }
        } else {
            logger.error("There are more than two similar names in a test case");
        }
    }

    /**
     * Delete single/compound test case or prototype test case
     *
     * @param testNewNode to get name of test case
     * @param treeItem to update test case navigator tree
     */
    public synchronized void deleteTestCase(TestNewNode testNewNode, TestCasesTreeItem treeItem) {
        final String name = testNewNode.getName();
        DeleteTestCaseTask deleteTask = new DeleteTestCaseTask(testNewNode);
        deleteTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                try {
                    // remove from the test case navigator
                    if (treeItem != null && treeItem.getParent() != null
                            && treeItem.getParent().getChildren() != null) {
                        treeItem.getParent().getChildren().remove(treeItem);
                    }

                    TreeTableView.TreeTableViewSelectionModel<ITestcaseNode> selectionModel =
                            TestCasesNavigatorController.getInstance().getTestCasesNavigator().getSelectionModel();

                    if (selectionModel != null) {
                        selectionModel.clearSelection();
                    }

                    // remove from MDI window
                    switch (deleteTask.get()) {
                        case BASIC:
                            MDIWindowController.getMDIWindowController().removeTestCaseTab(name);
                            break;

                        case COMPOUND:
                            MDIWindowController.getMDIWindowController().removeCompoundTestCaseTab(name);
                            break;

                        case PROTOTYPE:
                            MDIWindowController.getMDIWindowController().removePrototypeTab(name);
                            break;
                    }

                    // refresh compound testcase tree table views that were opened
                    UIController.refreshCompoundTestcaseViews();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        new AkaThread(deleteTask).start();
    }
}