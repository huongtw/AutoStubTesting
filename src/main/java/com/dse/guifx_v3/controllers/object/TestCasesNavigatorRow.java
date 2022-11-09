package com.dse.guifx_v3.controllers.object;

import auto_testcase_generation.cte.UI.Controller.CteController;
import auto_testcase_generation.cte.UI.CteCoefficent;
import auto_testcase_generation.cte.booleanChangeListen.BooleanChangeEvent;
import auto_testcase_generation.cte.booleanChangeListen.BooleanChangeListener;
import auto_testcase_generation.testdatagen.RandomAutomatedTestdataGeneration;
import com.dse.coverage.gcov.GcovInfo;
import com.dse.environment.Environment;
import com.dse.exception.FunctionNodeNotFoundException;
import com.dse.coverage.gcov.LCOVTestReportGeneration;
import com.dse.parser.dependency.Dependency;
import com.dse.parser.dependency.FunctionCallDependency;
import com.dse.report.excel_report.ExcelReport;
import com.dse.report.csv.CSVReport;
import com.dse.thread.task.GenerateTestdataTask;
import com.dse.thread.task.OpenWorkspaceTask;
import com.dse.thread.task.testcase.InsertCompoundTestCaseTask;
import com.dse.thread.task.testcase.InsertPrototypeTask;
import com.dse.thread.task.testcase.InsertTestCaseFromCte;
import com.dse.thread.task.testcase.InsertTestCaseTask;
import com.dse.thread.task.testcase_selection.ClassifyTestCaseTask;
import com.dse.thread.task.view_report.ViewManagementReportTask;
import com.dse.testcase_execution.TestcaseExecution;
import com.dse.config.FunctionConfig;
import com.dse.config.FunctionConfigDeserializer;
import com.dse.config.FunctionConfigSerializer;
import com.dse.config.WorkspaceConfig;
import com.dse.config.*;
import com.dse.debugger.controller.DebugController;
import com.dse.exception.OpenFileException;
import com.dse.guifx_v3.controllers.CompoundTestCaseTreeTableViewController;
import com.dse.guifx_v3.controllers.TestCasesNavigatorController;
import com.dse.guifx_v3.controllers.main_view.BaseSceneController;
import com.dse.guifx_v3.controllers.main_view.MDIWindowController;
import com.dse.guifx_v3.controllers.main_view.MenuBarController;
import com.dse.guifx_v3.helps.*;
import com.dse.guifx_v3.objects.CheckBoxTreeTableRow;
import com.dse.guifx_v3.objects.TestCasesTreeItem;
import com.dse.guifx_v3.objects.popups.SomeTestCasesAreNotSuccessPopupController;
import com.dse.parser.object.*;
import com.dse.project_init.ProjectClone;
import com.dse.report.FullReport;
import com.dse.report.IReport;
import com.dse.report.ReportManager;
import com.dse.search.Search2;
import com.dse.testcase_execution.TestCaseExecutionThread;
import com.dse.testcase_manager.*;
import com.dse.testcasescript.SelectionUpdater;
import com.dse.testcasescript.TestcaseSearch;
import com.dse.testcasescript.object.*;
import com.dse.testdata.object.IDataNode;
import com.dse.thread.AkaThread;
import com.dse.thread.AkaThreadManager;
import com.dse.logger.AkaLogger;
import com.dse.util.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.eclipse.cdt.core.dom.ast.IASTNode;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

// For context menu of test case navigator (when right click)
public class TestCasesNavigatorRow extends CheckBoxTreeTableRow<ITestcaseNode> {
    private final static AkaLogger logger = AkaLogger.get(TestCasesNavigatorRow.class);

    public TestCasesNavigatorRow() {
        super();
        setEventWhenClickCheckbox();
    }

    private void setEventWhenClickCheckbox() {
        // When we click the checkbox of an item, we need to update the selected state of its children
        // Two case: (1) the children are expanded before, (2) the children are not expanded
        getCheckBox().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                TestCasesTreeItem treeItem = (TestCasesTreeItem) getTreeItem();
                treeItem.loadChildren(true);

                if (treeItem.isSelected()) {
                    SelectionUpdater.check(treeItem.getValue());
                } else {
                    SelectionUpdater.uncheck(treeItem.getValue());
                }

                // when we select in tree, it may trigger menubar. Some menu items may be activated.
                MenuBarController.getMenuBarController().refresh();
            }
        });
    }


    @Override
    public void updateItem(ITestcaseNode item, boolean empty) {
        super.updateItem(item, empty);
        if (getTreeItem() != null && getTreeItem().getValue() != null) {
            // initialize popup
            setContextMenu(new ContextMenu());
            TestCasesTreeItem treeItem = (TestCasesTreeItem) getTreeItem();

            // update the selected status
            treeItem.setSelected(treeItem.getValue().isSelectedInTestcaseNavigator());

            // add options to popup
            ObservableList<TreeItem<ITestcaseNode>> list = getTreeTableView().getSelectionModel().getSelectedItems();
            if (list.contains(getTreeItem())) {
                if (list.size() > 1) {
                    addMenuItemForMultiSelection(item);
                } else {
                    addMenuItemsForSingleSelection(item, treeItem);
                }
            }
        } else {
            setContextMenu(null);
        }
    }

    private void addMenuItemsForSingleSelection(ITestcaseNode node, TestCasesTreeItem item) {
        if (node instanceof TestInitSubprogramNode) {
            addSelectAllChildren(node);
            addDeselectAllChildren(node);

        } else if (node instanceof TestNormalSubprogramNode) {
            addInsertTestCase(node, item);
            addExecute(node);
            addCreatePrototypeForTemplateAndMacroFunction(node, item);
            addViewDependency(node);
//                addInsertMinMidMaxOption(node, item);
//                addInsertBasisPathTestCasesOption(node);
//            addExecuteOption(node);
            addOpenSourceOption(node);
            addDeleteOption(node);
            addCteOption(node,item);
//                addExpandAllChildrenOption(node);
//                addCollapseAllChildrenOption(node);
//                addDeselectCoverageOption(node);
            addConfigureFunction(node);
            addAutomatedTestdataGenerationOptionForAFunction(node, item);
            addStopAutomatedTestdataGenerationOption(node, item);
            addGenerateCSVReport((TestNormalSubprogramNode) node);
            addViewTestCasesExecution(node);
            addSelectAllChildren(node);
            addDeselectAllChildren(node);
            addResetAllFunctionConfigToDefault(node);
            addDeleteMultiTestCases(node);
            addDeleteMultiPrototypes(node);
            addImportMultipleTestcase(node, item);

        } else if (node instanceof TestCompoundSubprogramNode) {
            addInsertTestCase(node, item);
            addExecute(node);
//            addExecuteOption(node);
            addDeleteOption(node);
            addExpandAllChildrenOption(node);
            addCollapseAllChildrenOption(node);
            addDeselectCoverageOption(node);
            addViewTestCaseManagementReport(node);
            addGenerateNewTestcaseReportInExcel();
            addSelectAllChildren(node);
            addDeselectAllChildren(node);

        } else if (node instanceof TestUnitNode) {
            addOpenSourceOption(node);
            addExecute(node);
            addOpenSourceInDebugMode(node);
            addViewCoverageOptionToAFile(node, item);
            addViewDependency(node);
            addViewTestCaseManagementReport(node);
            addGenerateNewTestcaseReportInExcel();
            addViewInstrumentedSourcecodeOption(node, item);
            addGenerateTestdataAutomaticallyOptionForAnUnit(node, item);
            addStopAutomatedTestdataGenerationOption(node, item);
            addSelectAllChildren(node);
            addDeselectAllChildren(node);
            addResetAllFunctionConfigToDefault(node);
            addDeleteMultiTestCases(node);
            addDeleteMultiPrototypes(node);
            addCteOption(node, item);

        } else if (node instanceof TestNewNode) {
            TestNewNode cast = (TestNewNode) node;
            if (cast.getParent() instanceof TestCompoundSubprogramNode) {
                addOpenTestCase(cast);
                addAddToCompound(cast);
                addDuplicateTestCase(cast, item);
                addDeleteTestCase(cast, item);
                addExecuteTestCase(cast);
                addExecuteTestCaseWithDebugMode(cast);
                addGenerateTestCaseReport(cast);
                addGenerateNewTestcaseReportInExcel();
                addViewTestCaseJson(cast);

            } else if (cast.getParent() instanceof TestNormalSubprogramNode) {
                if (cast.isPrototypeTestcase()) {
                    addOpenPrototype(cast);
                    addDeletePrototype(cast, item);
                    addGenerateTestdataAutomaticallyForPrototype(cast, item);
                    addViewTestCaseJson(cast);

                } else {
                    addOpenTestCase(cast);
                    addAddToCompound(cast);
                    addDuplicateTestCase(cast, item);
                    addDeleteTestCase(cast, item);
                    addExecuteTestCase(cast);
                    addViewLCOVReport(cast);
                    addExecuteTestCaseWithDebugMode(cast);
                    addExportMultipleTestcase(cast);
                    addGenerateTestCaseReport(cast);
                    addGenerateNewTestcaseReportInExcel();
                    addViewReport((TestNewNode) node);
                    viewTestdriver(cast);
                    viewTestpath(cast);
                    viewCommands(cast);
                    addViewCoverageOptionToATestcase(node, item);
                    addViewTestCaseJson(cast);
                }
            }
        } else if (node instanceof TestcaseRootNode) {
            addTurnOnViewCoverageMode(node);
            openEnvironmentFolder(node);
            addExecute(node);
//            addSelectCoverage(node);
            addViewTestCaseManagementReport(node);
            addGenerateNewTestcaseReportInExcel();
            addGenerateTestdataAutomaticallyOptionForAnUnit(node, item);
            addStopAutomatedTestdataGenerationOption(node, item);
            addSelectAllChildren(node);
            addDeselectAllChildren(node);
            addDeleteMultiTestCases(node);
            addDeleteMultiPrototypes(node);
            addResetAllFunctionConfigToDefault(node);
        }
    }

    private void addMenuItemForMultiSelection(ITestcaseNode node) {
        addExportMultipleTestcase(node);
        if (node instanceof TestNewNode)
            addGenerateMultipleTestcaseReportInExcel((TestNewNode) node);
        addExecuteMultiple(node);
        addDeleteMultiTestCases(node);
        addDeleteMultiPrototypes(node);
    }

    private void addExportMultipleTestcase(ITestcaseNode node) {
        MenuItem miExport = new MenuItem("Export all selected test cases");
        getContextMenu().getItems().add(miExport);
        miExport.setOnAction(event -> {
            Stage primaryStage = UIController.getPrimaryStage();
            DirectoryChooser workingDirectoryChooser = new DirectoryChooser();
            workingDirectoryChooser.setTitle("Choose test cases directory");
            File file = workingDirectoryChooser.showDialog(primaryStage);

            if (file != null) {
                logger.debug("Export testcases to directory: " + file.getAbsolutePath());
                exportDeliverableTestCase(file);
            }
        });
        if (UIController.getMode().equals(UIController.MODE_EDIT))
            miExport.setDisable(true);
        else if (UIController.getMode().equals(UIController.MODE_VIEW))
            miExport.setDisable(false);
    }

    private void addImportMultipleTestcase(ITestcaseNode node, TestCasesTreeItem item) {
        if (node instanceof TestSubprogramNode) {
            MenuItem miImport = new MenuItem("Import all selected test cases");
            getContextMenu().getItems().add(miImport);
            miImport.setOnAction(event -> {
                Stage primaryStage = UIController.getPrimaryStage();
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Open Multiple Files");
                fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("All Files", "*.*"));
                //Adding action on the menu item
                List<File> files = fileChooser.showOpenMultipleDialog(primaryStage);
                if (files != null) {
                    List<TestCase> testCases = importDeliverableTestCase((TestSubprogramNode) node, files);

                    getTreeTableView().getSelectionModel().clearSelection();

                    for (TestCase testCase : testCases) {
                        // display testcase on testcase navigator tree
                        TestNewNode testNewNode = testCase.getTestNewNode();
                        node.getChildren().add(testNewNode);
                        testNewNode.setParent(node);
                        item.setExpanded(true);
                        TestCasesTreeItem newTestCaseTreeItem = new TestCasesTreeItem(testNewNode);
                        item.getChildren().add(newTestCaseTreeItem);
                        getTreeTableView().getSelectionModel().select(newTestCaseTreeItem);

                        // render testcase view in MDI window
                        UIController.viewTestCase(testCase);
                    }

                    Environment.getInstance().saveTestcasesScriptToFile();
                }
            });
            if (UIController.getMode().equals(UIController.MODE_EDIT))
                miImport.setDisable(true);
            else if (UIController.getMode().equals(UIController.MODE_VIEW))
                miImport.setDisable(false);
        }
    }

    private void exportDeliverableTestCase(File directory) {
        // get all test cases
        List<TestNewNode> testNewNodes = new ArrayList<>();
        for (TreeItem<ITestcaseNode> treeItem : getTreeTableView().getSelectionModel().getSelectedItems()) {
            addTestCaseToList(testNewNodes, treeItem.getValue());
        }
        logger.debug("You are requesting to export " + testNewNodes.size() + " test cases");

        // put test cases in different threads
        for (TestNewNode testNewNode : testNewNodes) {
            List<ITestcaseNode> names = TestcaseSearch.searchNode(testNewNode, new TestNameNode());
            try {
                TestCase testCase = TestCaseManager.getBasicTestCaseByName(((TestNameNode) names.get(0)).getName());
                TestSubprogramNode subprogramNode = (TestSubprogramNode) testNewNode.getParent();
                String function = subprogramNode.getSimpleNameToDisplayInTestcaseView();
                TestUnitNode unitNode = (TestUnitNode) testNewNode.getParent().getParent();
                String unit = unitNode.getShortNameToDisplayInTestcaseTree();
                TestCaseManager.exportDeliverableTestCase(unit, function, testCase, directory);
            } catch (Exception ex) {
                logger.error(ex);
            }
        }
    }

    private List<TestCase> importDeliverableTestCase(TestSubprogramNode subprogramNode, List<File> files) {
        List<TestCase> testCases = new ArrayList<>();
        try {
            ICommonFunctionNode sut = UIController.searchFunctionNodeByPath(subprogramNode.getName());

            // put test cases in different threads
            for (File file : files) {
                try {
                    String function = subprogramNode.getSimpleNameToDisplayInTestcaseView();
                    TestUnitNode unitNode = (TestUnitNode) subprogramNode.getParent();
                    String unit = unitNode.getShortNameToDisplayInTestcaseTree();
                    TestCase testCase = TestCaseManager.importDeliverableTestCase(unit, function, sut, file);
                    testCases.add(testCase);
                } catch (Exception ex) {
                    logger.error(ex);
                }
            }
        } catch (FunctionNodeNotFoundException e) {
            logger.error(e);
        }
        return testCases;
    }

//    // By default, users can select the type of code coverage when creating the environment.
//    // However, users can change the type of code coverage later by using this option.
//    // IMPORTANT:
//    // All the information of the current environment will be deleted before switching the new type of code coverage.
//    private void addSelectCoverage(ITestcaseNode node) {
//        if (node instanceof TestcaseRootNode) {
//            Menu miSelectCoverage = new Menu("Select coverage");
//            getContextMenu().getItems().add(miSelectCoverage);
//
//            // statement coverage
//            {
//                MenuItem miSelectStatementCov = new MenuItem("Statement coverage");
//                miSelectCoverage.getItems().add(miSelectStatementCov);
//                miSelectStatementCov.setOnAction(event ->
//                        updateCoverage(EnviroCoverageTypeNode.STATEMENT));
//            }
//
//            // branch coverage
//            {
//                MenuItem miSelectBranchCov = new MenuItem("Branch coverage");
//                miSelectCoverage.getItems().add(miSelectBranchCov);
//                miSelectBranchCov.setOnAction(event ->
//                        updateCoverage(EnviroCoverageTypeNode.BRANCH));
//            }
//
//            // mcdc coverage
//            {
//                MenuItem miSelectMcdcCov = new MenuItem("MCDC coverage");
//                miSelectCoverage.getItems().add(miSelectMcdcCov);
//                miSelectMcdcCov.setOnAction(event ->
//                        updateCoverage(EnviroCoverageTypeNode.MCDC));
//            }
//
//            // basis path coverage
//            {
//                MenuItem miSelectBasisPathCov = new MenuItem("Basis path coverage");
//                miSelectCoverage.getItems().add(miSelectBasisPathCov);
////                    miSelectBasisPathCov.setDisable(true);
//                miSelectBasisPathCov.setOnAction(event ->
//                        updateCoverage(EnviroCoverageTypeNode.BASIS_PATH));
//            }
//
//            // Statement + branch coverage coverage
//            {
//                MenuItem miSelectStatementAndBranchCov = new MenuItem("Statement + branch coverage");
//                miSelectCoverage.getItems().add(miSelectStatementAndBranchCov);
////                    miSelectStatementAndBranchCov.setDisable(true);
//                miSelectStatementAndBranchCov.setOnAction(event ->
//                        updateCoverage(EnviroCoverageTypeNode.STATEMENT_AND_BRANCH));
//            }
//            // Statement + mcdc coverage coverage
//            {
//                MenuItem miSelectStatementAndMcdcCov = new MenuItem("Statement + mcdc coverage");
////                    miSelectStatementAndMcdcCov.setDisable(true);
//                miSelectCoverage.getItems().add(miSelectStatementAndMcdcCov);
//                miSelectStatementAndMcdcCov.setOnAction(event ->
//                        updateCoverage(EnviroCoverageTypeNode.STATEMENT_AND_MCDC));
//            }
//        }
//    }
//
//    private void updateCoverage(String coverage) {
//        if (coverage.equals(Environment.getInstance().getTypeofCoverage())) {
//            UIController.showSuccessDialog("The current coverage is " + coverage + ". There is no change!", "Cover coverage configuration", "No change to the environment");
//        } else {
//            // show a dialog to confirm
//            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
//            alert.setTitle("Warning");
//            alert.setHeaderText("Change coverage");
//            alert.setContentText("All the information related to the current environment will be deleted. Only test cases are kept. Do you want to continue?");
//            Optional<ButtonType> result = alert.showAndWait();
//            if (result.get() == ButtonType.OK) {
//                // if users decide to change code coverage
//
//                // update the environment script
//                List<IEnvironmentNode> enviroNodes = EnvironmentSearch.searchNode(Environment.getInstance().getEnvironmentRootNode(), new EnviroCoverageTypeNode());
//                if (enviroNodes.size() == 1) {
//                    EnviroCoverageTypeNode covNode = (EnviroCoverageTypeNode) enviroNodes.get(0);
//                    covNode.setCoverageType(coverage);
//                    Environment.getInstance().saveEnvironmentScriptToFile();
//
//                    // update windows
//                    BaseSceneController.getBaseSceneController().updateInformation();
//                    MDIWindowController.getMDIWindowController().removeViewsAfterChangeCoverageType();
//
//                    logger.debug("Update the environment script " + Environment.getInstance().getEnvironmentRootNode().getEnvironmentScriptPath());
//                }
//
//                // delete all information of test cases
//                WorkspaceConfig workspaceConfig = new WorkspaceConfig().fromJson();
//                Utils.deleteFileOrFolder(new File(workspaceConfig.getCoverageDirectory()));
//                Utils.deleteFileOrFolder(new File(workspaceConfig.getDebugDirectory()));
//                Utils.deleteFileOrFolder(new File(workspaceConfig.getExecutableFolderDirectory()));
//                Utils.deleteFileOrFolder(new File(workspaceConfig.getTestcaseCommandsDirectory()));
//                Utils.deleteFileOrFolder(new File(workspaceConfig.getTestDriverDirectory()));
//                Utils.deleteFileOrFolder(new File(workspaceConfig.getReportDirectory()));
//                Utils.deleteFileOrFolder(new File(workspaceConfig.getTestpathDirectory()));
//                Utils.deleteFileOrFolder(new File(workspaceConfig.getExecutionResultDirectory()));
//
//                Environment.getInstance().getCfgsForBranchAndStatement().clear();
//                Environment.getInstance().getCfgsForMcdc().clear();
//
//                // delete all test driver of test cases inside the testing project
//                // deleteAkaFile(ITestCase.AKA_SIGNAL, new File(workspaceConfig.getTestingProject()));
//
////                    new ProjectClone.CloneThread().start();
//            }
//        }
//    }
//
//    /**
//     * Delete all files which contains a signal in its name
//     *
//     * @param akaSignal
//     * @param root
//     */
//    private void deleteAkaFile(String akaSignal, File root) {
//        for (File f : root.listFiles())
//            if (f.getName().contains(akaSignal))
//                f.delete();
//            else if (f.isDirectory()) {
//                deleteAkaFile(akaSignal, f);
//            }
//    }

    private void addViewTestCaseManagementReport(ITestcaseNode node) {
        MenuItem menuItem = new MenuItem("View Test Case Management Report");
        getContextMenu().getItems().add(menuItem);

        menuItem.setOnAction(event -> {
            ViewManagementReportTask task = new ViewManagementReportTask(Collections.singletonList(node));
            new AkaThread(task).start();
        });
    }

    private void openEnvironmentFolder(ITestcaseNode node) {
        if (!(node instanceof TestcaseRootNode))
            return;

        MenuItem miOpenEnvFolder = new MenuItem();
        miOpenEnvFolder.setText("Open environment");
        getContextMenu().getItems().add(miOpenEnvFolder);
        miOpenEnvFolder.setOnAction(event -> {
            OpenWorkspaceTask task = new OpenWorkspaceTask();
            task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    boolean success;

                    try {
                        success = task.get();
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                        success = false;
                    }

                    if (!success)
                        UIController.showErrorDialog("Unable to open environment folder", "Open environment folder", "Fail");
                }
            });
            new AkaThread(task).start();
        });
    }

    private void addTurnOnViewCoverageMode(ITestcaseNode node) {
        MenuItem miActive = new MenuItem();
        getContextMenu().getItems().add(miActive);
        boolean isActive = Environment.getInstance().isCoverageModeActive();
        if (isActive) {
            miActive.setText("Turn Off Coverage Mode / MultiSelection");
        } else {
            miActive.setText("Turn On Coverage Mode / MultiSelection");
        }
        miActive.setOnAction(event -> {
            if (isActive) {
                SelectionUpdater.reset(Environment.getInstance().getTestcaseScriptRootNode());

                Environment.getInstance().setCoverageModeActive(false);
                miActive.setText("Turn On Coverage Mode / MultiSelection");
            } else {
                SelectionUpdater.reset(Environment.getInstance().getTestcaseScriptRootNode());

                Environment.getInstance().setCoverageModeActive(true);
                miActive.setText("Turn Off Coverage Mode / MultiSelection");
            }
            TestCasesNavigatorController.getInstance().refreshNavigatorTree();
        });
    }

    private void addAddToCompound(TestNewNode testNewNode) {
        MenuItem miAddToCompound = new MenuItem("Add this test case to the opening test compound");
        miAddToCompound.setOnAction(event -> {
            CompoundTestCaseTreeTableViewController controller = Environment.getInstance().getCurrentTestcompoundController();
            if (controller == null) {
                UIController.showErrorDialog("You need to open a test compound first", "Add test case to test compound failed", "Test compound generation");
            } else {
                List<ITestcaseNode> names = TestcaseSearch.searchNode(testNewNode, new TestNameNode());
                if (names.size() == 1) {
                    String name = ((TestNameNode) names.get(0)).getName();
                    controller.addSlot(name);
                }
            }
        });
        getContextMenu().getItems().add(miAddToCompound);
    }

    private void addExecuteMultiple(ITestcaseNode node) {

        MenuItem miExecMultiWithGoogleTest = new MenuItem("Execute all selected test cases");
        getContextMenu().getItems().add(miExecMultiWithGoogleTest);
        miExecMultiWithGoogleTest.setOnAction(event -> {
            System.out.println("--------------------Print 1--------------------");
            executeMultiTestcase(TestcaseExecution.IN_EXECUTION_WITH_FRAMEWORK_TESTING_MODE);
        });
        if (UIController.getMode().equals(UIController.MODE_EDIT))
            miExecMultiWithGoogleTest.setDisable(true);
        else if (UIController.getMode().equals(UIController.MODE_VIEW))
            miExecMultiWithGoogleTest.setDisable(false);
    }

    private void functionNodeNotFoundExeptionHandle(FunctionNodeNotFoundException fe) {
        UIController.showErrorDialog(
                "Does not find the function " + fe.getFunctionPath(),
                "Error", "Not found");
        logger.error("FunctionNodeNotFound: " + fe.getFunctionPath());
    }

    private void addExecute(ITestcaseNode node) {

        MenuItem mi = new MenuItem("Execute all selected test cases");
        getContextMenu().getItems().add(mi);

        mi.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("--------------------Print 2--------------------");
                try {
                    List<ITestcaseNode> selectedTestcases = SelectionUpdater.getAllSelectedTestcases(node);
                    if (selectedTestcases.size() == 0) {
                        UIController.showErrorDialog("You must select at least one test case", "Test case execution", "Error");
                    } else {
                        Alert confirmAlert = UIController.showYesNoDialog(Alert.AlertType.CONFIRMATION, "Test case execution", "Confirmation",
                                "You select " + selectedTestcases.size() + " test cases. Do you want to execute them all?");
                        Optional<ButtonType> option = confirmAlert.showAndWait();
                        if (option.get() == ButtonType.YES) {
                            for (ITestcaseNode selectedTc : selectedTestcases)
                                if (selectedTc instanceof TestNameNode) {
                                    if (!(((TestNewNode) selectedTc.getParent()).isPrototypeTestcase()))
                                        executeTestcaseInASpecifiedMode((TestNewNode) selectedTc.getParent()
                                        );
                                }
                        } else {
                            confirmAlert.close();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        if (UIController.getMode().equals(UIController.MODE_EDIT))
            mi.setDisable(true);
        else if (UIController.getMode().equals(UIController.MODE_VIEW))
            mi.setDisable(false);
    }

    private void addResetAllFunctionConfigToDefault(ITestcaseNode node) {
        MenuItem miDeleteMulti = new MenuItem("Delete all selected function configs");
        getContextMenu().getItems().add(miDeleteMulti);

        miDeleteMulti.setOnAction(event -> {
            try {
                List<ITestcaseNode> selectedFunctions = SelectionUpdater.getAllSelectedFunctions(node);
                for (ITestcaseNode selectedFunction : selectedFunctions)
                    if (selectedFunction instanceof TestSubprogramNode) {
                        ICommonFunctionNode functionNode = UIController.searchFunctionNodeByPath(((TestSubprogramNode) selectedFunction).getName());

                        String functionConfigPath = new WorkspaceConfig().fromJson().getFunctionConfigDirectory() + File.separator + functionNode.getNameOfFunctionConfigJson() + ".json";
                        if (new File(functionConfigPath).exists()) {
                            Utils.deleteFileOrFolder(new File(functionConfigPath));
                        }
                        functionNode.setFunctionConfig(null);
                    }

                UIController.showSuccessDialog("Delete all function configs of selected functions successfully",
                        "Success", "Delete function config");
            } catch (FunctionNodeNotFoundException fe) {
                fe.printStackTrace();
                UIController.showErrorDialog("Raise error when deleting function configs of selected functions" +
                                "\nFunctionNodeNotFound " + fe.getFunctionPath(),
                        "Fail", "Delete function config");
            } catch (Exception e) {
                e.printStackTrace();
                UIController.showErrorDialog("Raise error when deleting function configs of selected functions",
                        "Fail", "Delete function config");
            }
        });
    }

    private void deleteTestCase(TestNewNode testNewNode, TestCasesTreeItem item) {
        TestCasesNavigatorController.getInstance().deleteTestCase(testNewNode, item);
    }

    private void addDeleteMultiPrototypes(ITestcaseNode node) {
        MenuItem miDeleteMulti = new MenuItem("Delete all selected prototypes");
        getContextMenu().getItems().add(miDeleteMulti);

        miDeleteMulti.setOnAction(event -> {
            try {
                // get all prototypes
                List<TreeItem<ITestcaseNode>> treeItems = new ArrayList<>();
                for (TreeItem<ITestcaseNode> treeItem : getTreeTableView().getSelectionModel().getSelectedItems()) // return the index where the mouse is clicked
                    if (treeItem.getValue().isSelectedInTestcaseNavigator()) { // just check to confirm
                        addTreeItemContainsTestCaseToList(treeItems, treeItem);
                    }

                for (TreeItem<ITestcaseNode> treeItem : treeItems)
                    if (treeItem.getValue() instanceof TestNewNode) {
                        TestNewNode newNode = (TestNewNode) treeItem.getValue();
                        if (newNode.isPrototypeTestcase())
                            deleteTestCase(newNode, (TestCasesTreeItem) treeItem);
                    }
                UIController.showSuccessDialog("All selected prototypes are deleted", "Delete prototypes", "Success");
            } catch (Exception e) {
                e.printStackTrace();
                UIController.showErrorDialog("There occurs problem while deleting selected prototypes", "Delete prototypes", "Success");
            }
        });
    }

    private void addDeleteMultiTestCases(ITestcaseNode node) {
        MenuItem miDeleteMulti = new MenuItem("Delete all test cases in selection");
        getContextMenu().getItems().add(miDeleteMulti);

        miDeleteMulti.setOnAction(event -> {
            try {
                // get all test cases
                List<TreeItem<ITestcaseNode>> treeItems = new ArrayList<>();
                for (TreeItem<ITestcaseNode> treeItem : getTreeTableView().getSelectionModel().getSelectedItems()) {
                    addTreeItemContainsTestCaseToList(treeItems, treeItem);
                }

                for (TreeItem<ITestcaseNode> treeItem : treeItems)
                    if (treeItem.getValue() instanceof TestNewNode) {
                        TestNewNode newNode = (TestNewNode) treeItem.getValue();
                        if (!(newNode.isPrototypeTestcase()))
                            deleteTestCase(newNode, (TestCasesTreeItem) treeItem);
                    }
                UIController.showSuccessDialog("All selected test cases are deleted", "Delete test cases", "Success");
            } catch (Exception e) {
                e.printStackTrace();
                UIController.showErrorDialog("There occurs problem while deleting selected test cases", "Delete test cases", "Success");
            }

        });
    }

    private void executeMultiTestcase(int mode) {
        // get all test cases
        List<TestNewNode> testNewNodes = new ArrayList<>();
        for (TreeItem<ITestcaseNode> treeItem : getTreeTableView().getSelectionModel().getSelectedItems()) {
            addTestCaseToList(testNewNodes, treeItem.getValue());
        }
        logger.debug("You are requesting to execute " + testNewNodes.size() + " test cases");

        // put test cases in different threads
        List<TestCaseExecutionThread> tasks = new ArrayList<>();
        List<TestCase> testCases = new ArrayList<>();
        for (TestNewNode testNewNode : testNewNodes) {
            List<ITestcaseNode> names = TestcaseSearch.searchNode(testNewNode, new TestNameNode());
            if (names.size() == 1) {
                TestCase testCase = TestCaseManager.getBasicTestCaseByName(((TestNameNode) names.get(0)).getName());
                testCases.add(testCase);

                if (testCase != null) {
                    testCase.deleteOldDataExceptValue();
                    TestCaseExecutionThread executionThread = new TestCaseExecutionThread(testCase);
                    executionThread.setExecutionMode(mode);
                    tasks.add(executionThread);
                }
            }
        }
        logger.debug("Create " + tasks.size() + " threads to execute " + tasks.size() + " test cases");

        // add these threads to executors
        // at the same time, we do not execute all of the requested test cases.
        int MAX_EXECUTING_TESTCASE = 1;
        ExecutorService executorService = Executors.newFixedThreadPool(MAX_EXECUTING_TESTCASE);
        for (TestCaseExecutionThread task : tasks)
            executorService.submit(task);
        executorService.shutdown();

        try {
            executorService.awaitTermination(10, TimeUnit.MINUTES);
            UIController.viewCoverageOfMultipleTestcases("Summary Coverage Report", testCases);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void addTestCaseToList(List<TestNewNode> testNewNodes, ITestcaseNode selectedNode) {
        if (selectedNode instanceof TestNewNode) {
            if (!testNewNodes.contains(selectedNode)) {
                testNewNodes.add((TestNewNode) selectedNode);
            }
        } else {
            for (ITestcaseNode child : selectedNode.getChildren()) {
                addTestCaseToList(testNewNodes, child);
            }
        }
    }

    private void addTreeItemContainsTestCaseToList(List<TreeItem<ITestcaseNode>> treeItems, TreeItem<ITestcaseNode> item) {
        if (item.getValue() instanceof TestNewNode) {
            if (!treeItems.contains(item)) {
                treeItems.add(item);
            }
        } else {
            for (TreeItem<ITestcaseNode> child : item.getChildren()) {
                addTreeItemContainsTestCaseToList(treeItems, child);
            }
        }
    }

    private void viewCommands(TestNewNode testNewNode) {
        MenuItem mi = new MenuItem("View command of this test case (after execution)");
        getContextMenu().getItems().add(mi);

        mi.setOnAction(event -> {
            ITestcaseNode parent = testNewNode.getParent();
            List<ITestcaseNode> names = TestcaseSearch.searchNode(testNewNode, new TestNameNode());

            if (names.size() == 1) {
                String name = ((TestNameNode) names.get(0)).getName();
                if (name != null && name.length() > 0 && parent instanceof TestNormalSubprogramNode) {
                    // find the corresponding source code file
                    File commandFile = new File(new WorkspaceConfig().fromJson().getTestcaseCommandsDirectory() + File.separator + name + ".json");
                    if (commandFile != null) {
                        try {
                            Utils.openFolderorFileOnExplorer(commandFile.getAbsolutePath());
                        } catch (OpenFileException e) {
                            UIController.showErrorDialog("The command file of this test case is not found", "Open command file", "Not found");
                        }
                    } else {
                        UIController.showErrorDialog("The command file of this test case is not found", "Open command file", "Not found");
                    }
                }
            }
        });
    }

    private void viewTestpath(TestNewNode testNewNode) {
        MenuItem mi = new MenuItem("View test path (after execution)");
        getContextMenu().getItems().add(mi);

        mi.setOnAction(event -> {
            ITestcaseNode parent = testNewNode.getParent();
            List<ITestcaseNode> names = TestcaseSearch.searchNode(testNewNode, new TestNameNode());

            if (names.size() == 1) {
                String name = ((TestNameNode) names.get(0)).getName();
                if (name != null && name.length() > 0 && parent instanceof TestNormalSubprogramNode) {
                    // find the corresponding source code file
                    File testpathFile = new File(new WorkspaceConfig().fromJson().getTestpathDirectory() + File.separator + name + ".tp");
                    if (testpathFile != null) {
                        try {
                            Utils.openFolderorFileOnExplorer(testpathFile.getAbsolutePath());
                        } catch (OpenFileException e) {
                            UIController.showErrorDialog("The test path of this test case is not found", "Open test path", "Not found");
                        }
                    } else {
                        UIController.showErrorDialog("The test path of this test case is not found", "Open test path", "Not found");
                    }
                }
            }
        });
    }

    private void viewTestdriver(TestNewNode testNewNode) {
        MenuItem mi = new MenuItem("View test driver (after execution)");
        getContextMenu().getItems().add(mi);

        mi.setOnAction(event -> {

            ITestcaseNode parent = testNewNode.getParent();
            List<ITestcaseNode> names = TestcaseSearch.searchNode(testNewNode, new TestNameNode());

            if (names.size() == 1) {
                String name = ((TestNameNode) names.get(0)).getName();
                if (name != null && name.length() > 0 && parent instanceof TestNormalSubprogramNode) {
                    TestCase testCase = TestCaseManager.getBasicTestCaseByName(name);
                    try {
                        Utils.openFolderorFileOnExplorer(testCase.getSourceCodeFile());
                    } catch (OpenFileException e) {
                        UIController.showErrorDialog("The test driver of this test case is not found", "Open test driver", "Not found");
                    }
//

                }
            }
        });
    }

    private void addExecuteTestCase(TestNewNode testNewNode) {
        // execute in google test mode
        MenuItem miExecute = new MenuItem("Execute");
        getContextMenu().getItems().add(miExecute);
        miExecute.setOnAction(event -> {
            executeTestcaseInASpecifiedMode(testNewNode);
        });
        if (UIController.getMode().equals(UIController.MODE_EDIT))
            miExecute.setDisable(true);
        else if (UIController.getMode().equals(UIController.MODE_VIEW))
            miExecute.setDisable(false);
    }

    private void addViewLCOVReport(TestNewNode testNewNode) {
        // Generate LCOV Report
        MenuItem mi = new MenuItem("View LCOV Report");
        getContextMenu().getItems().add(mi);
        mi.setOnAction(event -> {
            logger.info("Choosing View LCOV Report ...");
            ViewLCOVReport(testNewNode);
        });
    }

    private void ViewLCOVReport(TestNewNode testNewNode) {
        String name = testNewNode.getName();
        if (name == null || name.length() == 0) {
            logger.info("Can't determine the name of testcase");
            return;
        }

        TestCase testCase = null;
        ITestcaseNode parent = testNewNode.getParent();
        if (parent instanceof TestNormalSubprogramNode) {
            try {

                ICommonFunctionNode function = UIController.searchFunctionNodeByPath(((TestNormalSubprogramNode) parent).getName());
                logger.debug("The function corresponding to the clicked node: " + function.getAbsolutePath());
                testCase = TestCaseManager.getBasicTestCaseByName(name);
                if (testCase != null)
                    testCase.setFunctionNode(function);
            } catch (FunctionNodeNotFoundException fe) {
                fe.printStackTrace();
                logger.error("FunctionNodeNotFound: " + fe.getFunctionPath());
            }
            if (testCase != null) {
                LCOVTestReportGeneration task = new LCOVTestReportGeneration(testCase);
                new AkaThread(task).start();

                final String reportName = testCase.getName() + " 's LCOV Report";
                task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent event) {
                        GcovInfo info = task.getInfo();
                        logger.info("Open report at " + info.getReportPath());
                        logger.info("Open with Css at " + info.getCssPath());
                        MDIWindowController.getMDIWindowController()
                                .viewReport(reportName, info.getReportContent(), info.getCssPath());

                    }
                });
            } else logger.error("Cannot Determine Testcase!");
        }

    }

    private void executeTestcaseInASpecifiedMode(TestNewNode testNewNode) {
        String name = testNewNode.getName();
        if (name == null || name.length() == 0)
            return;

        ITestCase testCase = null;
        ITestcaseNode parent = testNewNode.getParent();
        if (parent instanceof TestNormalSubprogramNode) {
            try {
                ICommonFunctionNode function = UIController.searchFunctionNodeByPath(((TestNormalSubprogramNode) parent).getName());
                logger.debug("The function corresponding to the clicked node: " + function.getAbsolutePath());
                testCase = TestCaseManager.getBasicTestCaseByName(name);

                if (testCase != null)
                    ((TestCase) testCase).setFunctionNode(function);
            } catch (FunctionNodeNotFoundException fe) {
                fe.printStackTrace();
                logger.error("FunctionNodeNotFound: " + fe.getFunctionPath());
            }

        } else if (parent instanceof TestCompoundSubprogramNode) {
            testCase = TestCaseManager.getCompoundTestCaseByName(name);
        }

        if (testCase != null) {
            LoadingPopupController loadingPopup = LoadingPopupController.newInstance("Run " + testCase.getName());
            loadingPopup.initOwnerStage(UIController.getPrimaryStage());
            loadingPopup.show();

            testCase.deleteOldDataExceptValue();
            TestCaseExecutionThread task = new TestCaseExecutionThread(testCase);
            task.setExecutionMode(com.dse.testcase_execution.ITestcaseExecution.IN_EXECUTION_WITH_FRAMEWORK_TESTING_MODE);

            task.setOnPreSucceededEvent(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    loadingPopup.close();
                }
            });

            AkaThreadManager.executedTestcaseThreadPool.execute(task);
        }
    }


    private void addConfigureFunction(ITestcaseNode node) {
        if (node instanceof TestNormalSubprogramNode) {
            MenuItem mi = new MenuItem("Configure Function");
            getContextMenu().getItems().add(mi);

            mi.setOnAction(event -> {
                try {
                    ICommonFunctionNode function = UIController.searchFunctionNodeByPath(((TestNormalSubprogramNode) node).getName());
                    logger.debug("The function corresponding to the clicked node: " + function.getAbsolutePath());
                    MDIWindowController.getMDIWindowController().viewFunctionConfiguration(function);

                } catch (FunctionNodeNotFoundException fe) {
                    UIController.showErrorDialog(
                            "Does not find the function " + ((TestNormalSubprogramNode) node).getName(),
                            "Error", "Not found");
                    logger.error("FunctionNodeNotFound: " + fe.getFunctionPath());
                }
            });
        }
    }

    private void addViewTestCasesExecution(ITestcaseNode node) {
        if (node instanceof TestNormalSubprogramNode) {
            MenuItem mi = new MenuItem("View Test Cases Execution");
            getContextMenu().getItems().add(mi);

            mi.setOnAction(event -> {
                try {
                    ICommonFunctionNode function = UIController.searchFunctionNodeByPath(((TestNormalSubprogramNode) node).getName());
                    logger.debug("The function corresponding to the clicked node: " + function.getAbsolutePath());
                    MDIWindowController.getMDIWindowController().viewTestCasesExecution(function);
                } catch (FunctionNodeNotFoundException fe) {
                    UIController.showErrorDialog(
                            "Does not find the function " + ((TestNormalSubprogramNode) node).getName(),
                            "Error", "Not found");
                    logger.error("FunctionNodeNotFound: " + fe.getFunctionPath());
                    logger.debug("The function not found || macro function");
                }

            });
        }
    }

    /**
     * The function is a template function or
     * a normal function (not a template function)
     *
     * @param selectedFunction the function need to generate test cases
     * @param item             Testcase tree item for updating test cases navigator tree
     */
    private void generateTestdataAutomaticallyForFunction(TestNormalSubprogramNode selectedFunction, TestCasesTreeItem item, boolean showReport) {
        try {
            ICommonFunctionNode functionNode = UIController.searchFunctionNodeByPath(selectedFunction.getName());
            if (functionNode.isTemplate() || functionNode instanceof MacroFunctionNode) {
                generateTestcaseAutomaticallyForTemplateFunction(functionNode, showReport, item);

            } else if (functionNode.hasVoidPointerArgument() || functionNode.hasFunctionPointerArgument()) {
                ExecutorService executorService = Executors.newSingleThreadExecutor(); // must be one to avoid concurrent problem
                List<Task> tasks = new ArrayList<>();

                for (ITestcaseNode child : selectedFunction.getChildren())
                    if (child instanceof TestNewNode &&
                            ((TestNewNode) child).getName().startsWith(TestPrototype.PROTOTYPE_SIGNAL)) {

                        boolean autogen = true;
                        if (Environment.getInstance().isCoverageModeActive() && !child.isSelectedInTestcaseNavigator())
                            autogen = false;

                        if (autogen) {
                            Task t = new Task() {
                                @Override
                                protected Object call() throws Exception {
                                    String name = ((TestNewNode) child).getName();
                                    TestPrototype tc = TestCaseManager.getPrototypeByName(name);
                                    if (tc != null)
                                        generateTestcaseAutomaticallyForNormalFunction(functionNode, showReport, item, tc);
                                    else
                                        logger.debug(name + " not found");
                                    return null;
                                }
                            };
                            tasks.add(t);
                        }
                    }
                if (tasks.size() > 0) {
                    // has at least one prototype
                    for (Task task : tasks) {
                        executorService.execute(task);
                    }
                } else {
                    // there is no prototype, then choose the default configuration
                    generateTestcaseAutomaticallyForNormalFunction(functionNode, showReport, item, null);
                }

            } else {
                generateTestcaseAutomaticallyForNormalFunction(functionNode, showReport, item, null);
            }
        } catch (FunctionNodeNotFoundException fe) {
            UIController.showErrorDialog(
                    "Does not find the function " + fe.getFunctionPath(),
                    "Error", "Not found");
            logger.error("FunctionNodeNotFound: " + fe.getFunctionPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * For a function
     *
     * @param subprogramNode TestNormalSubprogramNode
     * @param item           for updating test cases navigator tree
     */
    private void addAutomatedTestdataGenerationOptionForAFunction(ITestcaseNode subprogramNode, TestCasesTreeItem item) {
        if (subprogramNode instanceof TestNormalSubprogramNode) {
            MenuItem mi = new MenuItem("Generate test data automatically");
            getContextMenu().getItems().add(mi);
            TestNormalSubprogramNode normalSubprogramNode = (TestNormalSubprogramNode) subprogramNode;

            mi.setOnAction(event -> {
                Alert confirmAlert = UIController.showYesNoDialog(Alert.AlertType.CONFIRMATION, "View Coverage Report?", "Confirmation",
                        "Do you want to view coverage report after finish automatic test case generation?");
                Optional<ButtonType> option = confirmAlert.showAndWait();
                if (option.get() == ButtonType.YES) {
                    askZ3AndRun(normalSubprogramNode, item, true);
                    confirmAlert.close();
                } else if (option.get() == ButtonType.NO) {
                    askZ3AndRun(normalSubprogramNode, item, false);
                    confirmAlert.close();
                } else {
                    confirmAlert.close();
                }
            });

            if (UIController.getMode().equals(UIController.MODE_EDIT))
                mi.setDisable(true);
            else if (UIController.getMode().equals(UIController.MODE_VIEW))
                mi.setDisable(false);
        }
    }

    private void askZ3AndRun(TestNormalSubprogramNode normalSubprogramNode, TestCasesTreeItem item, boolean showReport) {
        if (new File(new AkaConfig().fromJson().getZ3Path()).exists()) {
            generateTestCaseAutomaticForSubprogram(normalSubprogramNode, item, showReport);
        } else {
            Alert z3Alert = UIController.showYesNoDialog(Alert.AlertType.CONFIRMATION, "Set up solver",
                    "Z3 solver has not been set up",
                    "You must set up Z3 solver to use directed automated gen (File > Set Z3-SMTSolver). Do you want to proceed?");
            Optional<ButtonType> z3Option = z3Alert.showAndWait();
            if (z3Option.get() == ButtonType.YES) {
                generateTestCaseAutomaticForSubprogram(normalSubprogramNode, item, showReport);
            }
            z3Alert.close();
        }
    }

    private void generateTestcaseAutomaticallyForTemplateFunction(ICommonFunctionNode functionNode, boolean shouldViewTestCaseExecution, TestCasesTreeItem item) {
        // if a function is a template function, we will generate test cases for all prototypes
        String templatePath = functionNode.getTemplateFilePath();
        if (!(new File(templatePath).exists())) {
            UIController.showErrorDialog("You must create a prototype for function " + functionNode.getAbsolutePath() + " before generating test data automatically.\n" +
                            "Aka generates test data for these prototypes",
                    "Autogen for a function", "Fail");
            return;
        }

        List<TestPrototype> prototypes = RandomAutomatedTestdataGeneration.getAllPrototypesOfTemplateFunction(functionNode);
        if (prototypes.size() == 0)
            UIController.showErrorDialog("Not find out any prototypes of this template function. Please right click > Insert a prototype",
                    "Autogen for a function", "Fail");
        else {
            for (TestPrototype selectedPrototype : prototypes) {
                UILogger.getUiLogger().logToBothUIAndTerminal("Prototype: " + selectedPrototype.getName());
                generateTestcaseAutomaticallyForNormalFunction(functionNode, shouldViewTestCaseExecution, item, selectedPrototype);
            }
        }
    }

//    private void generateTestcaseAutomaticallyForVoidPointerFunction(ICommonFunctionNode functionNode, boolean shouldViewReport, TestCasesTreeItem item
//            , TestCase selectedPrototype) {
//        UILogger uiLogger = UILogger.getUiLogger();
//        Platform.runLater(() -> BaseSceneController.getBaseSceneController().showMessagesPane());
//        uiLogger.info("Start generate test cases for function: " + functionNode.getSingleSimpleName());
//
//        if (functionNode.isTemplate() && selectedPrototype == null)
//            return;
//        if (functionNode instanceof MacroFunctionNode && selectedPrototype == null)
//            return;
//        if (functionNode.hasVoidPointerArgument() && selectedPrototype == null)
//            return;
//
//        // refresh executions
//        TCExecutionDetailLogger.clearExecutions(functionNode);
//
//        MDIWindowController.getMDIWindowController().removeTestCasesExecutionTabByFunction(functionNode);
//        TCExecutionDetailLogger.initFunctionExecutions(functionNode);
//
//        uiLogger.info("Getting function configuration...");
//        FunctionConfig functionConfig = null;
//
//        // search for the function config file of the current function node
//        String functionConfigDir = new WorkspaceConfig().fromJson().getFunctionConfigDirectory() +
//                File.separator + functionNode.getNameOfFunctionConfigJson() + ".json";
//        if (new File(functionConfigDir).exists()) {
//            GsonBuilder gsonBuilder = new GsonBuilder();
//            gsonBuilder.registerTypeAdapter(FunctionConfig.class, new FunctionConfigDeserializer());
//            Gson customGson = gsonBuilder.create();
//            functionConfig = customGson.fromJson(Utils.readFileContent(functionConfigDir), FunctionConfig.class);
//            functionNode.setFunctionConfig(functionConfig);
//            logger.debug("Loading the function config of " + functionNode.getAbsolutePath() + ": " + functionNode.getAbsolutePath());
//
//        } else {
//            logger.debug("Create new function config of " + functionNode.getAbsolutePath());
//            functionConfig = new WorkspaceConfig().fromJson().getDefaultFunctionConfig();
//            functionConfig.setFunctionNode(functionNode);
//            functionNode.setFunctionConfig(functionConfig);
//            functionConfig.createBoundOfArgument(functionConfig, functionNode);
//
//            //
//            GsonBuilder builder = new GsonBuilder();
//            builder.registerTypeAdapter(FunctionConfig.class, new FunctionConfigSerializer());
//            Gson gson = builder.setPrettyPrinting().create();
//            String json = gson.toJson(functionConfig, FunctionConfig.class);
//
//            String functionConfigFile = new WorkspaceConfig().fromJson().getFunctionConfigDirectory() + File.separator +
//                    functionConfig.getFunctionNode().getNameOfFunctionConfigJson() + ".json";
//            logger.debug("Export the config of function " + functionConfig.getFunctionNode().getAbsolutePath() + " to " + functionConfigFile);
//            Utils.writeContentToFile(json, functionConfigFile);
//        }
//
//        // GUI logic
//        item.setExpanded(true);
//
//        logger.debug("Create new thread to run the test data");
//        GenerateTestdataTask task = new GenerateTestdataTask(shouldViewReport);
//        task.setSelectedPrototype(selectedPrototype);
//        task.setFunction(functionNode);
//        task.setTreeNodeInTestcaseNavigator(item);
//        AkaThread thread = new AkaThread(task);
//        thread.setName(functionNode.getSimpleName());
//        AkaThreadManager.akaThreadList.add(thread);
//        AkaThreadManager.autoTestdataGenForSrcFileThreadPool.execute(thread);
//    }

    /**
     * @param functionNode      ICommonFunctionNode
     * @param shouldViewReport  true if we want to display test case execution tab, false if otherwise
     * @param item              TestCasesTreeItem
     * @param selectedPrototype a prototype of a template function (used to generate test data for a specific prototype of a template function)
     */
    private void generateTestcaseAutomaticallyForNormalFunction(ICommonFunctionNode functionNode,
                                                                boolean shouldViewReport, TestCasesTreeItem item,
                                                                TestPrototype selectedPrototype) {
        UILogger uiLogger = UILogger.getUiLogger();
        Platform.runLater(() -> BaseSceneController.getBaseSceneController().showMessagesPane());
        uiLogger.info("Start generate test cases for function: " + functionNode.getSingleSimpleName());

        if (functionNode.isTemplate() && selectedPrototype == null)
            return;
        if (functionNode instanceof MacroFunctionNode && selectedPrototype == null)
            return;
//        if (functionNode.hasVoidPointerArgument() && selectedPrototype == null)
//            return;

//        LoadingPopupController loadingPopup = LoadingPopupController.getInstance();
//        if (!loadingPopup.getStage().isShowing()) {
//            loadingPopup.getStage().setTitle("Automatic generate test case");
////            loadingPopup.setOwnerStage(UIController.getPrimaryStage());
//            loadingPopup.show();
//        }

        // refresh executions
        TCExecutionDetailLogger.clearExecutions(functionNode);

        MDIWindowController.getMDIWindowController().removeTestCasesExecutionTabByFunction(functionNode);
        TCExecutionDetailLogger.initFunctionExecutions(functionNode);

        uiLogger.info("Getting function configuration of function " + functionNode.getName() + " ...");
        FunctionConfig functionConfig = null;

        // search for the function config file of the current function node
        String functionConfigDir = new WorkspaceConfig().fromJson().getFunctionConfigDirectory() +
                File.separator + functionNode.getNameOfFunctionConfigJson() + ".json";
        if (new File(functionConfigDir).exists()) {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(FunctionConfig.class, new FunctionConfigDeserializer());
            Gson customGson = gsonBuilder.create();
            functionConfig = customGson.fromJson(Utils.readFileContent(functionConfigDir), FunctionConfig.class);
            functionNode.setFunctionConfig(functionConfig);
            logger.debug("Loading the function config of " + functionNode.getAbsolutePath() + ": " + functionNode.getAbsolutePath());

        } else {
            logger.debug("Create new function config of " + functionNode.getAbsolutePath());
            functionConfig = new WorkspaceConfig().fromJson().getDefaultFunctionConfig();
            functionConfig.setFunctionNode(functionNode);
            functionNode.setFunctionConfig(functionConfig);
            functionConfig.createBoundOfArgument(functionConfig, functionNode);

            //
            GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapter(FunctionConfig.class, new FunctionConfigSerializer());
            Gson gson = builder.setPrettyPrinting().create();
            String json = gson.toJson(functionConfig, FunctionConfig.class);

            String functionConfigFile = new WorkspaceConfig().fromJson().getFunctionConfigDirectory() + File.separator +
                    functionConfig.getFunctionNode().getNameOfFunctionConfigJson() + ".json";
            logger.debug("Export the config of function " + functionConfig.getFunctionNode().getAbsolutePath() + " to " + functionConfigFile);
            Utils.writeContentToFile(json, functionConfigFile);
        }

        // GUI logic
        item.setExpanded(true);

        logger.debug("Create new thread to run the test data");
        GenerateTestdataTask task = new GenerateTestdataTask(shouldViewReport);
        task.setSelectedPrototype(selectedPrototype);
        task.setFunction(functionNode);
        task.setTreeNodeInTestcaseNavigator(item);
        AkaThread thread = new AkaThread(task);
        thread.setName(functionNode.getSimpleName());
        AkaThreadManager.akaThreadList.add(thread);
        AkaThreadManager.autoTestdataGenForSrcFileThreadPool.execute(thread);

//        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
//            @Override
//            public void handle(WorkerStateEvent event) {
//                try {
//                    List<AutoGeneratedTestCaseExecTask> list = task.getTestCaseExecTask();
//
//                    EventHandler<WorkerStateEvent> handler = new EventHandler<WorkerStateEvent>() {
//                        private int remainingTasks = list.size();
//
//                        @Override
//                        public void handle(WorkerStateEvent event) {
//                            if ((--remainingTasks) == 0) {
//                                try {
//                                    UIController.viewCoverageOfMultipleTestcasesFromAnotherThread(functionNode.getName(), task.get());
//                                } catch (Exception ex) {
//                                    ex.printStackTrace();
//                                }
//                            }
//                        }
//                    };
//
//                    List<TestCase> testCases = list.stream()
//                            .map(AutoGeneratedTestCaseExecTask::getTestCase)
//                            .collect(Collectors.toList());
//
//                    UIController.viewCoverageOfMultipleTestcasesFromAnotherThread(functionNode.getName(), testCases);
//
//                    if (list.size() > 0) {
//                        ExecutorService executorService = Executors.newFixedThreadPool(list.size());
//                        list.forEach(t -> {
//                            t.setOnSucceeded(handler);
//                            executorService.submit(t);
//                        });
//                        executorService.shutdown();
//                    }
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                }
//            }
//        });
    }

    private TestCase getTestCaseByTestNewNode(TestNewNode testNewNode) {
        List<ITestcaseNode> names = TestcaseSearch.searchNode(testNewNode, new TestNameNode());
        if (names.size() == 1) {
            String name = ((TestNameNode) names.get(0)).getName();
            TestCase testCase = TestCaseManager.getBasicTestCaseByName(name);
            return testCase;
        }
        return null;
    }

//    private void addDebugAndGetLog(TestNewNode testNewNode) {
//        MenuItem mi = new MenuItem("Debug and get Log");
//        getContextMenu().getItems().add(mi);
//        mi.setOnAction(event -> {
//                /*
//                  Find the test case object
//                 */
//            List<ITestcaseNode> names = TestcaseSearch.searchNode(testNewNode, new TestNameNode());
//
//            if (names.size() == 1) {
//                String name = testNewNode.getName();
//                ITestcaseNode parent = testNewNode.getParent();
//                if (name != null && name.length() > 0) {
//                    ITestCase testCase = null;
//
//                    if (parent instanceof TestNormalSubprogramNode) {
//                        try {
//                            ICommonFunctionNode function = UIController.searchFunctionNodeByPath(((TestNormalSubprogramNode) parent).getName());
//                            logger.debug("The function corresponding to the clicked node: " + function.getAbsolutePath());
//                            /*
//                              Find the test case object by its name
//                             */
//                            testCase = TestCaseManager.getBasicTestCaseByName(name);
//
//                            if (testCase != null)
//                                ((TestCase) testCase).setFunctionNode(function);
//                        } catch (FunctionNodeNotFoundException fe) {
//                            functionNodeNotFoundExeptionHandle(fe);
//                        }
//
//                    } else if (parent instanceof TestCompoundSubprogramNode) {
//                        testCase = TestCaseManager.getCompoundTestCaseByName(name);
//                    }
//
//                    if (testCase != null) {
//                        UIController.debugAndGetLog(testCase);
//                    }
//                }
//            }
//        });
//        if (UIController.getMode().equals(UIController.MODE_EDIT))
//            mi.setDisable(true);
//        else if (UIController.getMode().equals(UIController.MODE_VIEW))
//            mi.setDisable(false);
//    }

    private void addExecuteTestCaseWithDebugMode(TestNewNode testNewNode) {
        MenuItem mi = new MenuItem("Execute With Debug Mode");
        getContextMenu().getItems().add(mi);
        mi.setOnAction(event -> {
                /*
                  Find the test case object
                 */
            List<ITestcaseNode> names = TestcaseSearch.searchNode(testNewNode, new TestNameNode());

            if (names.size() == 1) {
                String name = testNewNode.getName();
                ITestcaseNode parent = testNewNode.getParent();
                if (name != null && name.length() > 0) {
                    ITestCase testCase = null;

                    if (parent instanceof TestNormalSubprogramNode) {
                        try {
                            ICommonFunctionNode function = UIController.searchFunctionNodeByPath(((TestNormalSubprogramNode) parent).getName());
                            logger.debug("The function corresponding to the clicked node: " + function.getAbsolutePath());
                            /*
                              Find the test case object by its name
                             */
                            testCase = TestCaseManager.getBasicTestCaseByName(name);

                            if (testCase != null)
                                ((TestCase) testCase).setFunctionNode(function);
                        } catch (FunctionNodeNotFoundException fe) {
                            functionNodeNotFoundExeptionHandle(fe);
                        }

                    } else if (parent instanceof TestCompoundSubprogramNode) {
                        testCase = TestCaseManager.getCompoundTestCaseByName(name);
                    }

                    if (testCase != null) {
                        UIController.executeTestCaseWithDebugMode(testCase);
                    }
                }
            }
        });
        if (UIController.getMode().equals(UIController.MODE_EDIT))
            mi.setDisable(true);
        else if (UIController.getMode().equals(UIController.MODE_VIEW))
            mi.setDisable(false);
    }

    private void addGenerateTestCaseReport(TestNewNode testNewNode) {
        MenuItem mi = new MenuItem("Generate Test Case Data Report");
        getContextMenu().getItems().add(mi);
        mi.setOnAction(event -> {
            try {
                ITestCase testCase = TestCaseManager.getTestCaseByName(testNewNode.getName());

                if (testCase instanceof TestCase) {
                    ICommonFunctionNode function = UIController.searchFunctionNodeByPath(((TestNormalSubprogramNode) testNewNode.getParent()).getName());
                    ((TestCase) testCase).setFunctionNode(function);
                }

                // Generate test case data report
                IReport report = new FullReport(testCase, LocalDateTime.now());
                ReportManager.export(report);

                // display on MDIWindow
                MDIWindowController.getMDIWindowController().viewReport(report.getName(), report.toHtml());

                logger.debug("generate test report for test case " + testCase.getName() + " success");
            } catch (FunctionNodeNotFoundException fe) {
                functionNodeNotFoundExeptionHandle(fe);
            }
        });
    }

    private void genTestCaseToExcel(Map<String, TestCase> testCaseMap) {
        ExcelReport excelReport = new ExcelReport();
        excelReport.getTestReportDirectoryInput();
        if (excelReport.getTEST_REPORT_DIRECTORY_OUTPUT() == null) {
            return;
        }
        excelReport.showPopup();

        IProjectNode projectNode = Environment.getInstance().getProjectNode();
//                    File file = new File(projectNode.getAbsolutePath())
//                            .getParentFile();
        String projectName = projectNode.getName();
        excelReport.setProjectName("< " + projectName + " >");
        Map<String , TestCase> testcases = testCaseMap;

        excelReport.exportTestcases(testcases);
        System.out.println("Finish Export Excel report");
    }

    private void addGenerateNewTestcaseReportInExcel() {
        MenuItem mi = new MenuItem("Generate Test Case Data Report in Excel");
        getContextMenu().getItems().add(mi);
        mi.setOnAction(event -> {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Map<String , TestCase> testcases = TestCaseManager.getTestcases();
                    genTestCaseToExcel(testcases);
                }
            });
        });
    }

    private void addGenerateMultipleTestcaseReportInExcel(TestNewNode node) {
        MenuItem mi = new MenuItem("Generate selected test cases in Excel");
        getContextMenu().getItems().add(mi);
        mi.setOnAction(event -> {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    System.out.println("Hello");
                    List<TestNewNode> testNewNodes = new ArrayList<>();
                    for (TreeItem<ITestcaseNode> treeItem : getTreeTableView().getSelectionModel().getSelectedItems()) {
                        addTestCaseToList(testNewNodes, treeItem.getValue());
                    }
                    List<TestCaseExecutionThread> tasks = new ArrayList<>();
                    Map<String, TestCase> testCaseMap = new HashMap<>();
                    for (TestNewNode testNewNode : testNewNodes) {
                        List<ITestcaseNode> names = TestcaseSearch.searchNode(testNewNode, new TestNameNode());
                        if (names.size() == 1) {
                            String testcaseName = ((TestNameNode) names.get(0)).getName();
                            TestCase testCase = TestCaseManager.getBasicTestCaseByName(testcaseName);
                            testCaseMap.put(testcaseName, testCase);
                        }
                    }
                    genTestCaseToExcel(testCaseMap);
                }
            });
        });
    }

    private void addGenerateCSVReport(TestNormalSubprogramNode node) {
        MenuItem mi = new MenuItem("Export Test Data to CSV file");
        getContextMenu().getItems().add(mi);

        mi.setOnAction(event -> {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    // list test new node of the chosen function.
                    String path = UIController.chooseDirectoryOfNewFile(".csv");
                    if (path == null) {
                        return;
                    }
                    //Generate file below
                    LoadingPopupController loadPopup = CSVReport.getPopup();
                    loadPopup.show();
                    System.out.println("Start Exporting...");
                    try {
                        IFunctionNode functionNode = (IFunctionNode) UIController.searchFunctionNodeByPath(node.getName());
                        List<TestCase> list = TestCaseManager.getTestCasesByFunction(functionNode);
                        CSVReport report = new CSVReport(functionNode, list);
                        report.exportTo(path);
                        UIController.showSuccessDialog("Finish.", "CSV Report", "Generate Successfully.");
                    } catch (FunctionNodeNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        loadPopup.close();
                    }
                    System.out.println("Finish export!");
                }
            });


        });
    }

    private void openPrototype(TestNewNode testNewNode) {
        TestCasesNavigatorController.getInstance().openPrototype(testNewNode);
    }

    private void addOpenPrototype(TestNewNode testNewNode) {
        MenuItem miOpen = new MenuItem("Open prototype");
        getContextMenu().getItems().add(miOpen);

        miOpen.setOnAction(event -> openPrototype(testNewNode));
    }

    /**
     * For a prototype of a template function
     *
     * @param testNewNode
     * @param item
     */
    private void addGenerateTestdataAutomaticallyForPrototype(TestNewNode testNewNode, TestCasesTreeItem item) {
        MenuItem mi = new MenuItem("Generate test data automatically for this prototype");
        getContextMenu().getItems().add(mi);

        mi.setOnAction(event -> {
            Alert confirmAlert = UIController.showYesNoDialog(Alert.AlertType.CONFIRMATION, "View Coverage Report?", "Confirmation",
                    "Do you want to view coverage report after finish automatic test case generation?");
            Optional<ButtonType> option = confirmAlert.showAndWait();
            if (option.get() == ButtonType.YES) {
                generateTestCaseAutomaticForPrototype(testNewNode, item, true);
                confirmAlert.close();
            } else if (option.get() == ButtonType.NO) {
                generateTestCaseAutomaticForPrototype(testNewNode, item, false);
                confirmAlert.close();
            } else {
                confirmAlert.close();
            }
        });
        if (UIController.getMode().equals(UIController.MODE_EDIT))
            mi.setDisable(true);
        else if (UIController.getMode().equals(UIController.MODE_VIEW))
            mi.setDisable(false);
    }

    private void generateTestCaseAutomaticForPrototype(TestNewNode testNewNode, TestCasesTreeItem item, boolean showReport) {
        try {
            ITestcaseNode subprogramNode = testNewNode.getParent();
            if (!(subprogramNode instanceof TestNormalSubprogramNode))
                return;
            ICommonFunctionNode function = UIController.searchFunctionNodeByPath(((TestNormalSubprogramNode) subprogramNode).getName());
            logger.debug("The function corresponding to the clicked node: " + function.getAbsolutePath());
//                if (function instanceof AbstractFunctionNode)
            if (function.isTemplate() || function instanceof MacroFunctionNode
                    || function.hasVoidPointerArgument() || function.hasFunctionPointerArgument()) {
                TestPrototype selectedPrototype = TestCaseManager.getPrototypeByName(testNewNode.getName(),
                        new WorkspaceConfig().fromJson().getTestcaseDirectory(), true);

                List<IDataNode> arguments = Search2.findSubprogramUnderTest(selectedPrototype.getRootDataNode()).getChildren();
                if (arguments == null || arguments.size() == 0) {
                    UIController.showErrorDialog("The template arguments in prototype " + selectedPrototype.getName() + " are not set up completely",
                            "Autogen for a prototype", "Fail");
                } else
                    generateTestcaseAutomaticallyForNormalFunction(function, showReport, item, selectedPrototype);
            }
        } catch (FunctionNodeNotFoundException fe) {
            functionNodeNotFoundExeptionHandle(fe);
        }
    }

    private void openTestCase(TestNewNode testNewNode) {
        TestCasesNavigatorController.getInstance().openTestCase(testNewNode);
    }

    private void addOpenTestCase(TestNewNode testNewNode) {
        MenuItem miOpen = new MenuItem("Open Test Case");
        getContextMenu().getItems().add(miOpen);

        miOpen.setOnAction(event -> openTestCase(testNewNode));
    }

    private void viewTestcaseJson(TestNewNode testNewNode) {
        try {
            ITestcaseNode parent = testNewNode.getParent();
            String name = testNewNode.getName();

            ITestCase testCase = null;
            if (parent instanceof TestNormalSubprogramNode) {
                testCase = TestCaseManager.getBasicTestCaseByName(name);
            } else if (parent instanceof TestCompoundSubprogramNode) {
                testCase = TestCaseManager.getCompoundTestCaseByName(name);
            }

            try {
                Utils.openFolderorFileOnExplorer(testCase.getPath());
            } catch (OpenFileException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            UIController.showErrorDialog("Can not open json file", "Open Test case", "Fail");
        }
    }

    private void addViewTestCaseJson(TestNewNode testNewNode) {
        MenuItem miOpen = new MenuItem("Open Test Case in Json");
        getContextMenu().getItems().add(miOpen);

        miOpen.setOnAction(event -> viewTestcaseJson(testNewNode));
    }

    private void viewReport(TestNewNode testNewNode) {
        ITestcaseNode parent = testNewNode.getParent();
        List<ITestcaseNode> names = TestcaseSearch.searchNode(testNewNode, new TestNameNode());

        if (names.size() == 1) {
            String name = ((TestNameNode) names.get(0)).getName();
            if (parent instanceof TestNormalSubprogramNode) {
                TestCase testCase = TestCaseManager.getBasicTestCaseByName(name);
                if (testCase != null) {
                    MDIWindowController.getMDIWindowController().viewReport(testCase);
                }
            } else if (parent instanceof TestCompoundSubprogramNode) {
//                CompoundTestCase compoundTestCase = TestCaseManager.getCompoundTestCaseByName(name);
//                Environment.setCurrentTestcompound(compoundTestCase);
//                if (compoundTestCase != null) {
//                    UIController.viewTestCase(compoundTestCase);
//                }
            }
        } else {
            logger.debug("There are more than two similar names in a test case ");
        }
    }

    private void addViewReport(TestNewNode testNewNode) {
        MenuItem miViewReport = new MenuItem("View Report of this Test Case");
        miViewReport.setDisable(true); // i think we should delete this option
        getContextMenu().getItems().add(miViewReport);

        miViewReport.setOnAction(event -> viewReport(testNewNode));
    }

    private void addDuplicateTestCase(TestNewNode testNewNode, TestCasesTreeItem treeItem) {
        ITestcaseNode parent = testNewNode.getParent();
        MenuItem miDuplicate = new MenuItem("Duplicate Test Case");
        getContextMenu().getItems().add(miDuplicate);
        miDuplicate.setOnAction(event -> {
            getTreeTableView().getSelectionModel().clearSelection();
            List<ITestcaseNode> names = TestcaseSearch.searchNode(testNewNode, new TestNameNode());
            if (names.size() == 1) {
                String name = ((TestNameNode) names.get(0)).getName();
                if (parent instanceof TestNormalSubprogramNode) {
                    // duplicate on disk
                    TestCase testCase = TestCaseManager.duplicateBasicTestCase(name);
                    if (testCase != null) {
                        // add to children of parent
                        TestNewNode newTestCase = testCase.getTestNewNode();
                        parent.getChildren().add(newTestCase);
                        newTestCase.setParent(parent);
                        // display on the navigator
                        TestCasesTreeItem item = new TestCasesTreeItem(newTestCase);
                        treeItem.getParent().getChildren().add(item);
                        getTreeTableView().getSelectionModel().select(item);
                        // save the testcases scripts to file .tst
                        Environment.getInstance().saveTestcasesScriptToFile();
                        // display on MDIWindow
                        UIController.viewTestCase(testCase);
                    }
                } else if (parent instanceof TestCompoundSubprogramNode) {
                    // duplicate on disk
                    CompoundTestCase compoundTestCase = TestCaseManager.duplicateCompoundTestCase(name);
                    if (compoundTestCase != null) {
                        // add to children of parent
                        TestNewNode newTestCase = compoundTestCase.getTestNewNode();
                        parent.getChildren().add(newTestCase);
                        newTestCase.setParent(parent);
                        // display on the navigator
                        TestCasesTreeItem item = new TestCasesTreeItem(newTestCase);
                        treeItem.getParent().getChildren().add(item);
                        getTreeTableView().getSelectionModel().select(item);
                        // save the testcases scripts to file .tst
                        Environment.getInstance().saveTestcasesScriptToFile();
                        // display on MDIWindow
                        UIController.viewTestCase(compoundTestCase);
                    }
                }
            } else {
                logger.debug("There are more than two similar names in a test case ");
            }
        });
    }

    private void addDeletePrototype(TestNewNode testNewNode, TestCasesTreeItem item) {
        MenuItem miDelete = new MenuItem("Delete Prototype");
        getContextMenu().getItems().add(miDelete);

        miDelete.setOnAction(event -> {
            deleteTestCase(testNewNode, item);
        });
    }

    private void addDeleteTestCase(TestNewNode testNewNode, TestCasesTreeItem item) {
        MenuItem miDelete = new MenuItem("Delete Test Case");
        getContextMenu().getItems().add(miDelete);

        miDelete.setOnAction(event -> {
            deleteTestCase(testNewNode, item);
        });
    }

//        private void deleteTestCase(TestNewNode testNewNode, TestCasesTreeItem item) {
//            ITestcaseNode parent = testNewNode.getParent();
//            // remove the testcase
//            parent.getChildren().remove(testNewNode);
//            // save the testcases scripts to file .tst
//            Environment.saveTestcasesScriptToFile();
//            // remove from the test case navigator
////                TreeItem<ITestcaseNode> item = getTreeItem();
//            if (item.getParent().getChildren()!=null)
//                item.getParent().getChildren().remove(item);
//            getTreeTableView().getSelectionModel().clearSelection();
//
//            List<ITestcaseNode> names = TestcaseSearch.searchNode(testNewNode, new TestNameNode());
//            if (names.size() == 1) {
//                String name = ((TestNameNode) names.get(0)).getName();
//                if (parent instanceof TestNormalSubprogramNode) {
//                    // remove from disk
//                    TestCaseManager.removeBasicTestCase(name);
//                    // remove from MDI window
//                    MDIWindowController.getMDIWindowController().removeTestCaseTab(name);
//                } else if (parent instanceof TestCompoundSubprogramNode) {
//                    // remove from disk
//                    TestCaseManager.removeCompoundTestCase(name);
//                    // remove from MDI window
//                    MDIWindowController.getMDIWindowController().removeCompoundTestCaseTab(name);
//                }
//                // refresh compound testcase tree table views that were opened
//                UIController.refreshCompoundTestcaseViews();
//
//            } else {
//                logger.debug("There are more than two similar names in a test case ");
//            }
//        }

    private void addDeselectCoverageOption(ITestcaseNode node) {
        if (node instanceof TestNormalSubprogramNode || node instanceof TestCompoundSubprogramNode) {
            MenuItem miDeselectCoverage = new MenuItem(("Deselect Coverage"));
            getContextMenu().getItems().add(miDeselectCoverage);
            miDeselectCoverage.setDisable(true);

            miDeselectCoverage.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    // TODO: handle here
                }

            });
        }
    }

    private void addCollapseAllChildrenOption(ITestcaseNode node) {
        if (node instanceof TestNormalSubprogramNode || node instanceof TestCompoundSubprogramNode) {
            MenuItem miCollapseAllChildren = new MenuItem(("Collapse All Children"));
            getContextMenu().getItems().add(miCollapseAllChildren);
            miCollapseAllChildren.setDisable(true);

            miCollapseAllChildren.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    // TODO: handle here
                }

            });
        }
    }

    private void addExpandAllChildrenOption(ITestcaseNode node) {
        if (node instanceof TestNormalSubprogramNode || node instanceof TestCompoundSubprogramNode) {
            MenuItem miExpandAllChildren = new MenuItem(("Expand All Children"));
            getContextMenu().getItems().add(miExpandAllChildren);
            miExpandAllChildren.setDisable(true);

            miExpandAllChildren.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    // TODO: handle here
                }

            });
        }
    }

    private void addDeleteOption(ITestcaseNode node) {
        if (node instanceof TestNormalSubprogramNode || node instanceof TestCompoundSubprogramNode) {
            MenuItem miDelete = new MenuItem(("Delete"));
            getContextMenu().getItems().add(miDelete);
            miDelete.setDisable(true);

            miDelete.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    // TODO: handle here
                }

            });
        }
    }

    private void addOpenSourceInDebugMode(ITestcaseNode node) {
        if (node instanceof TestUnitNode) {
            MenuItem miOpenSourceInDebug = new MenuItem("Open source code in debug");
            miOpenSourceInDebug.setDisable(!MDIWindowController.getMDIWindowController().checkDebugOpen());
            getContextMenu().getItems().add(miOpenSourceInDebug);
            miOpenSourceInDebug.setOnAction(e -> {
                TestUnitNode testUnitNode = (TestUnitNode) node;
                String path = testUnitNode.getName();
                DebugController.getDebugController().openSource(path);
            });
        } else {
            // todo: handle if not unit node
        }
    }

    private void addViewCoverageOptionToAFile(ITestcaseNode node, TestCasesTreeItem treeItem) {
        MenuItem miViewCoverage = new MenuItem("View Coverage of this file");
        // enable/disable CoverageMode
        miViewCoverage.setDisable(!Environment.getInstance().isCoverageModeActive());

        getContextMenu().getItems().add(miViewCoverage);
        viewCoverage(miViewCoverage, node, treeItem);
    }

    private void addViewCoverageOptionToATestcase(ITestcaseNode node, TestCasesTreeItem treeItem) {
        MenuItem miViewCoverage = new MenuItem("View Coverage of this test case");
        // enable/disable CoverageMode
        miViewCoverage.setDisable(!Environment.getInstance().isCoverageModeActive());

        getContextMenu().getItems().add(miViewCoverage);
        viewCoverage(miViewCoverage, node, treeItem);
    }


    private void addInsertBasisPathTestCasesOption(ITestcaseNode node) {
        if (node instanceof TestNormalSubprogramNode) {
            MenuItem miInsertBasisPathTestCases = new MenuItem("Insert Basis Path Test Cases");
            getContextMenu().getItems().add(miInsertBasisPathTestCases);
            miInsertBasisPathTestCases.setDisable(true);

            miInsertBasisPathTestCases.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    // TODO: handle here
                }

            });
        } else {
            logger.debug("Can not insert test case for " + node.getClass());
        }
    }

    private void addSelectAllChildren(ITestcaseNode node) {
        MenuItem mi = new MenuItem("Select All");
        getContextMenu().getItems().add(mi);

        mi.setDisable(!Environment.getInstance().isCoverageModeActive());

        mi.setOnAction(event -> {
            SelectionUpdater.check(node);
            TestCasesNavigatorController.getInstance().refreshNavigatorTree();
        });
    }

    private void addDeselectAllChildren(ITestcaseNode node) {
        MenuItem mi = new MenuItem("Deselect All");
        getContextMenu().getItems().add(mi);

        mi.setDisable(!Environment.getInstance().isCoverageModeActive());

        mi.setOnAction(event -> {
            SelectionUpdater.uncheck(node);
            TestCasesNavigatorController.getInstance().refreshNavigatorTree();
        });
    }

    private void addStopAutomatedTestdataGenerationOption(ITestcaseNode node, TestCasesTreeItem item) {
        MenuItem mi = new MenuItem("Stop automated test data generation");
        getContextMenu().getItems().add(mi);

        mi.setOnAction(event -> {
            List<ITestcaseNode> nodes = TestcaseSearch.searchNode(node, new TestNormalSubprogramNode());
            if (node instanceof TestNormalSubprogramNode)
                nodes.add(node);
            for (ITestcaseNode child : nodes) {
                try {
                    ICommonFunctionNode functionNode = UIController.searchFunctionNodeByPath(((TestNormalSubprogramNode) child).getName());
                    if (functionNode instanceof IFunctionNode)
                        AkaThreadManager.stopAutomatedTestdataGenerationThreadForAFunction((IFunctionNode) functionNode);
                } catch (FunctionNodeNotFoundException fe) {
                    functionNodeNotFoundExeptionHandle(fe);
                }
            }
            TestCasesNavigatorController.getInstance().refreshNavigatorTree();
        });
    }

    /**
     * For an unit
     *
     * @param node
     * @param item
     */
    private void addGenerateTestdataAutomaticallyOptionForAnUnit(ITestcaseNode node, TestCasesTreeItem item) {
        MenuItem mi = new MenuItem("Generate test data automatically");
        getContextMenu().getItems().add(mi);

        mi.setOnAction(event -> {
            Alert confirmAlert = UIController.showYesNoDialog(Alert.AlertType.CONFIRMATION, "View Coverage Report?", "Confirmation",
                    "Do you want to view coverage report after finish automatic test case generation?");
            Optional<ButtonType> option = confirmAlert.showAndWait();
            if (option.get() == ButtonType.YES) {
                generateTestCaseAutomaticForUnit(item, true);
                confirmAlert.close();
            } else if (option.get() == ButtonType.NO) {
                generateTestCaseAutomaticForUnit(item, false);
                confirmAlert.close();
            } else {
                confirmAlert.close();
            }
        });
        if (UIController.getMode().equals(UIController.MODE_EDIT))
            mi.setDisable(true);
        else if (UIController.getMode().equals(UIController.MODE_VIEW))
            mi.setDisable(false);
    }

    private void generateTestCaseAutomaticForUnit(TestCasesTreeItem item, boolean showReport) {
        item.loadChildren(true);

        List<ITestcaseNode> selectedFunctions = null;
        if (Environment.getInstance().isCoverageModeActive())
            selectedFunctions = SelectionUpdater.getAllSelectedFunctions(item.getValue());
        else
            selectedFunctions = TestcaseSearch.searchNode(item.getValue(), new TestNormalSubprogramNode());

        if (selectedFunctions.size() == 0) {
            UIController.showErrorDialog("Please select at least one function", "Automated test data generation for a file", "No selected functions");
        } else {
            for (ITestcaseNode selectedFunction : selectedFunctions) {
                if (selectedFunction instanceof TestNormalSubprogramNode) {
                    generateTestdataAutomaticallyForFunction((TestNormalSubprogramNode) selectedFunction, item, showReport);
                }
            }
            MenuBarController.getMenuBarController().refresh();
        }
    }

    private void generateTestCaseAutomaticForSubprogram(TestNormalSubprogramNode subprogramNode, TestCasesTreeItem item, boolean showReport) {
        try {
            ICommonFunctionNode function = UIController.searchFunctionNodeByPath(subprogramNode.getName());
            logger.debug("The function corresponding to the clicked node: " + function.getAbsolutePath());
            if (
                //function instanceof ConstructorNode || function instanceof DestructorNode ||
                    function instanceof MacroFunctionNode) {
                UIController.showErrorDialog("Do not support to generate test case automatically for this kind of function",
                        "Automated test case generation failure", "Do not support");

            } else if (function instanceof IFunctionNode)
                generateTestdataAutomaticallyForFunction(subprogramNode, item, showReport);
        } catch (FunctionNodeNotFoundException fe) {
            UIController.showErrorDialog(
                    "Does not find the function " + fe.getFunctionPath(),
                    "Error", "Not found");
            logger.error("FunctionNodeNotFound: " + fe.getFunctionPath());
        }
    }

    private void addViewInstrumentedSourcecodeOption(ITestcaseNode node, TestCasesTreeItem item) {
        if (node instanceof TestUnitNode) {
            MenuItem miViewInstrumentedSourcecode = new MenuItem("View Instrumented Source Code");
            getContextMenu().getItems().add(miViewInstrumentedSourcecode);

            miViewInstrumentedSourcecode.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    ISourcecodeFileNode sourcecodeFileNode =
                            UIController.searchSourceCodeFileNodeByPath((TestUnitNode) node, Environment.getInstance().getProjectNode());
                    if (sourcecodeFileNode != null) {
                        logger.debug("The src file corresponding to the clicked node: " + sourcecodeFileNode.getAbsolutePath());
                        try {
                            Utils.openFolderorFileOnExplorer(ProjectClone.getClonedFilePath(sourcecodeFileNode.getAbsolutePath()));
                        } catch (OpenFileException e) {
                            e.printStackTrace();
                        }
                    } else {
                        logger.debug("The source code file not found");
                    }
                }

            });
        }
    }

//    private void addInsertMinMidMaxOption(ITestcaseNode node, TestCasesTreeItem item) {
//        if (node instanceof TestNormalSubprogramNode) {
//            Menu mMinMidMax = new Menu("Insert Min Mid Max");
//
//            // menu Item 1
//            MenuItem miMinMidMax = new MenuItem("Min Mid Max");
//            mMinMidMax.getItems().add(miMinMidMax);
//            miMinMidMax.setOnAction(new EventHandler<ActionEvent>() {
//                @Override
//                public void handle(ActionEvent event) {
//                    insertNormalTestCase("MIN", node, item);
//                    insertNormalTestCase("MID", node, item);
//                    insertNormalTestCase("MAX", node, item);
//                }
//
//            });
//
//            // menu item 2
//            MenuItem miMin = new MenuItem("Min");
//            mMinMidMax.getItems().add(miMin);
//            miMin.setOnAction(new EventHandler<ActionEvent>() {
//                @Override
//                public void handle(ActionEvent event) {
//                    insertNormalTestCase("MIN", node, item);
//                }
//            });
//
//            // menu item 3
//            MenuItem miMid = new MenuItem("Mid");
//            mMinMidMax.getItems().add(miMid);
//            miMid.setOnAction(new EventHandler<ActionEvent>() {
//                @Override
//                public void handle(ActionEvent event) {
//                    insertNormalTestCase("MID", node, item);
//                }
//
//            });
//
//            // menu item 4
//            MenuItem miMax = new MenuItem("Max");
//            mMinMidMax.getItems().add(miMax);
//            miMax.setOnAction(new EventHandler<ActionEvent>() {
//                @Override
//                public void handle(ActionEvent event) {
//                    insertNormalTestCase("MAX", node, item);
//                }
//
//            });
//
//            // merge all
//            getContextMenu().getItems().add(mMinMidMax);
//
//        } else {
//            logger.debug("Can not insert test case for " + node.getClass());
//        }
//    }
//
//    private void addExecuteOption(ITestcaseNode node) {
//        assert (node != null);
//        if (node instanceof TestNormalSubprogramNode || node instanceof TestNewNode
//                || node instanceof TestCompoundSubprogramNode) {
//            MenuItem miExecute = new MenuItem("Execute");
//            getContextMenu().getItems().add(miExecute);
//            miExecute.setDisable(true);
//
//            miExecute.setOnAction(new EventHandler<ActionEvent>() {
//                @Override
//                public void handle(ActionEvent event) {
//                    // TODO: handle here
//                }
//
//            });
//        }
//    }


    /**
     * Allow to insert prototype
     *
     * @param navigatorNode
     * @param item
     */
    private void addCreatePrototypeForTemplateAndMacroFunction(ITestcaseNode navigatorNode, TestCasesTreeItem item) {
        assert (navigatorNode != null);

        if (navigatorNode instanceof TestNormalSubprogramNode) {
            try {
                ICommonFunctionNode functionNode = UIController.searchFunctionNodeByPath(((TestNormalSubprogramNode) navigatorNode).getName());

                if (functionNode.isTemplate() || functionNode instanceof MacroFunctionNode ||
                        functionNode.hasVoidPointerArgument() || functionNode.hasFunctionPointerArgument()) {
                    MenuItem miInsertRealType = new MenuItem("Create new prototype");

                    miInsertRealType.setOnAction(event -> {
                        String prototypeName = TestCaseManager.generateContinuousNameOfTestcase(TestPrototype.PROTOTYPE_SIGNAL + functionNode.getSimpleName());
                        insertPrototypeOfFunction(navigatorNode, item, prototypeName);

                    });
                    getContextMenu().getItems().add(miInsertRealType);
                }
            } catch (FunctionNodeNotFoundException fe) {
                functionNodeNotFoundExeptionHandle(fe);
            }
        }
    }

    private void addInsertTestCase(ITestcaseNode navigatorNode, TestCasesTreeItem item) {
        assert (navigatorNode != null);

        MenuItem miInsertTestCase = new MenuItem("Insert Test Case");

        if (navigatorNode instanceof TestNormalSubprogramNode) {
            miInsertTestCase.setOnAction(event -> insertNormalTestCase(navigatorNode, item));

        } else if (navigatorNode instanceof TestCompoundSubprogramNode) {
            miInsertTestCase.setOnAction(event -> insertCompoundTestCase(navigatorNode, item));

        }

        getContextMenu().getItems().add(miInsertTestCase);
    }

    private void addViewDependency(ITestcaseNode navigatorNode) {
        assert (navigatorNode != null);

        if (navigatorNode instanceof TestUnitNode || navigatorNode instanceof TestNormalSubprogramNode) {

            MenuItem miViewDependency = new MenuItem("View dependency");
            getContextMenu().getItems().add(miViewDependency);

            miViewDependency.setOnAction(event -> {
                // find the node corresponding the clicked item
                INode node = null;
                if (navigatorNode instanceof TestUnitNode)
                    node = UIController.searchSourceCodeFileNodeByPath((TestUnitNode) navigatorNode, Environment.getInstance().getProjectNode());
                else { // navigatorNode instance of TestNormalSubprogramNode
                    try {
                        node = UIController.searchFunctionNodeByPath(((TestNormalSubprogramNode) navigatorNode).getName());
                    } catch (FunctionNodeNotFoundException fe) {
                        functionNodeNotFoundExeptionHandle(fe);
                    }
                }

                if (node != null) {
                    Utils.viewDependency(node);
                }
            });
        }
    }

    private void addOpenSourceOption(ITestcaseNode navigatorNode) {
        assert (navigatorNode != null);

        if (navigatorNode instanceof TestUnitNode || navigatorNode instanceof TestNormalSubprogramNode) {

            MenuItem miViewSourceCode = new MenuItem("Open Source");

            if (navigatorNode instanceof TestUnitNode) {
                miViewSourceCode.setOnAction(event -> {
                    ISourcecodeFileNode node = UIController.searchSourceCodeFileNodeByPath((TestUnitNode) navigatorNode, Environment.getInstance().getProjectNode());
                    try {
                        UIController.viewSourceCode(node);
                        UILogger.getUiLogger().logToBothUIAndTerminal("Opened source code of " + node.getName() + " [" + node.getClass().getSimpleName() + "] on this tool");
                    } catch (Exception e) {
                        UIController.showErrorDialog("Error code: " + e.getMessage(), "Open source code file",
                                "Can not open source code file");
                    }
                });
            } else { // navigatorNode instanceof TestNormalSubprogramNode
                try {
                    ICommonFunctionNode iFunctionNode = UIController.searchFunctionNodeByPath(((TestNormalSubprogramNode) navigatorNode).getName());
//                    if (iFunctionNode instanceof AbstractFunctionNode) {
//                        AbstractFunctionNode node = (AbstractFunctionNode) iFunctionNode;
                    miViewSourceCode.setOnAction(event -> {
                        try {
                            UIController.viewSourceCode(iFunctionNode);
                            UILogger.getUiLogger().logToBothUIAndTerminal("Opened source code of " + iFunctionNode.getName() + " [" + iFunctionNode.getClass().getSimpleName() + "] on this tool");
                        } catch (Exception e) {
                            e.printStackTrace();
                            UIController.showErrorDialog("Error code: " + e.getMessage(), "Open source code file",
                                    "Can not open source code file");
                        }
                    });
                } catch (FunctionNodeNotFoundException fe) {
                    functionNodeNotFoundExeptionHandle(fe);
                }
            }

            getContextMenu().getItems().add(miViewSourceCode);
        }
    }

    private void addCteOption(ITestcaseNode navigatorNode, TestCasesTreeItem item)
    {
        assert (navigatorNode != null);

        if ( navigatorNode instanceof TestNormalSubprogramNode) {

            MenuItem miViewCTE = new MenuItem("Open CTE");

                try {
                    ICommonFunctionNode iFunctionNode = UIController.searchFunctionNodeByPath(((TestNormalSubprogramNode) navigatorNode).getName());
                    miViewCTE.setOnAction(event -> {
                        LoadingPopupController popUp = CteCoefficent.getOpenLoading();
                        popUp.setText("Openning CTE...");
                        popUp.show();
                        try {
                            IFunctionNode Fnode = (IFunctionNode) iFunctionNode;
                            Task<Void> createCteTask = new Task<Void>() {
                                @Override
                                protected Void call() throws Exception {
                                    Platform.runLater(() -> {
                                        CteController cteController = new CteController(Fnode, (TestNormalSubprogramNode) navigatorNode);

                                        BooleanChangeListener listener = new BooleanChangeListener() {
                                            @Override
                                            public void stateChanged(BooleanChangeEvent event) {
                                                if (event.getDispatcher().getFlag() == true) {
                                                    final LoadingPopupController[] controller = new LoadingPopupController[1];
                                                    boolean needLoading = true;
                                                    Platform.runLater(() -> {

                                                            controller[0] = LoadingPopupController.newInstance("Exporting To Test Case");
                                                            controller[0].initOwnerStage(UIController.getPrimaryStage());
                                                            controller[0].show();

                                                    });

                                                    List<TestCase> testCases = cteController.getCombinedTestCases();
                                                    if (!testCases.isEmpty()) {
                                                        for (TestCase testCase : testCases) {
                                                            insertTestCaseFromCte(navigatorNode, item, testCase);
                                                        }
                                                    }
                                                    cteController.turnOffCombineTask();
                                                    Platform.runLater(() -> {
                                                        if(controller[0] != null)
                                                        {
                                                            controller[0].close();
                                                        }
                                                        UIController.showSuccessDialog("Exported All Selected Testcases From CTE", "Exporting Testcases", "Exporting Successful");
                                                    });

                                                }
                                            }
                                        };
                                        cteController.addingListener(listener);
                                        try {
                                            UIController.viewCte(Fnode, cteController);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    });
                                    return null;
                                }
                            };


                            createCteTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                                @Override
                                public void handle(WorkerStateEvent event) {
                                    popUp.close();
                                }
                            });


                            new Thread(createCteTask).start();

                           // UILogger.getUiLogger().logToBothUIAndTerminal("Opened source code of " + iFunctionNode.getName() + " [" + iFunctionNode.getClass().getSimpleName() + "] on this tool");
                        } catch (Exception e) {
                            e.printStackTrace();
                            popUp.close();
                            UIController.showErrorDialog("Error code: " + e.getMessage(), "Open CTE",
                                    "Can not open CTE");

                        }
                    });
                } catch (FunctionNodeNotFoundException fe) {
                }
            //}

            getContextMenu().getItems().add(miViewCTE);
        }
    }

    private void insertTestCaseFromCte(ITestcaseNode navigatorNode, TestCasesTreeItem item, TestCase testCase) {
        if (!(navigatorNode instanceof TestNormalSubprogramNode))
            return;

        //clear selection
        Platform.runLater(() -> {
            getTreeTableView().getSelectionModel().clearSelection();
        });


        // lambda checking
        try {
            ICommonFunctionNode functionNode = UIController.searchFunctionNodeByPath(((TestNormalSubprogramNode) navigatorNode).getName());
            if (functionNode instanceof LambdaFunctionNode) {
                List<String> list = new ArrayList<>();
                if (functionNode.getParent() instanceof SourcecodeFileNode) {
                    INode parentNode = functionNode.getDependencies().stream()
                            .filter(d -> d instanceof FunctionCallDependency && d.getEndArrow().equals(functionNode))
                            .map(Dependency::getStartArrow)
                            .findFirst().orElse(null);
                    if (parentNode instanceof ICommonFunctionNode)
                        list = ((ICommonFunctionNode) parentNode).getArguments().stream()
                                .map(Object::toString)
                                .collect(Collectors.toList());
                } else if (functionNode.getParent() instanceof StructureNode) {
                    list = ((StructureNode) functionNode.getParent())
                            .getPublicAttributes().stream()
                            .map(INode::getName)
                            .collect(Collectors.toList());
                    INode parentNode = functionNode.getDependencies().stream()
                            .filter(d -> d instanceof FunctionCallDependency && d.getEndArrow().equals(functionNode))
                            .map(Dependency::getStartArrow)
                            .findFirst()
                            .orElse(null);
                    if (parentNode instanceof ICommonFunctionNode)
                        list = ((ICommonFunctionNode) parentNode).getArguments().stream()
                                .map(Object::toString)
                                .collect(Collectors.toList());
                }

                list.add("this");
                List<String> captures = Arrays.stream(((LambdaFunctionNode) functionNode).getOriginalAST().getCaptures())
                        .map(IASTNode::getRawSignature)
                        .collect(Collectors.toList());

                for (String c : captures) {
                    boolean accepted = false;
                    for (String l : list) {
                        if (c.startsWith(l)) {
                            accepted = true;
                        }
                    }
                    if (!accepted) {
                        UIController.showErrorDialog("Not yet supported", "Error", "Error");
                        return;
                    }
                }
            }
        } catch (Exception e) {
            logger.debug(e);
        }




        InsertTestCaseFromCte insertFromCteTask = new InsertTestCaseFromCte((TestSubprogramNode) navigatorNode, testCase);

        insertFromCteTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                try {
                    TestCase testCase1 = insertFromCteTask.get();

                    TestNewNode testNewNode = testCase1.getTestNewNode();

                    // display testcase on testcase navigator tree
                    item.setExpanded(true);
                    TestCasesTreeItem newTestCaseTreeItem = new TestCasesTreeItem(testNewNode);
                    item.getChildren().add(newTestCaseTreeItem);
                    getTreeTableView().getSelectionModel().select(newTestCaseTreeItem);

                    // render testcase view in MDI window
                   // UIController.viewTestCase(testCase);
                   // if(loadingPopup[0] != null) loadingPopup[0].close();


                } catch (Exception ex) {
                    UIController.showErrorDialog("Something wrong with selected function", "Generate test case",
                            "Can not generate test case");
                }
            }
        });


        AkaThread tmp = new AkaThread(insertFromCteTask);
        tmp.start();
        try {
            tmp.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void viewCoverage(MenuItem miViewCoverage, ITestcaseNode node, TestCasesTreeItem treeItem) {
        if (node instanceof TestUnitNode) {
            miViewCoverage.setOnAction(event -> {
                viewCoverageOfSourcecodeFile(node, treeItem);
            });
        } else if (node instanceof TestNewNode && node.getParent() instanceof TestNormalSubprogramNode) {
            miViewCoverage.setOnAction(event -> {
                viewCoverageOfATestcase(node);
            });
        }
    }

    private void viewCoverageOfATestcase(ITestcaseNode node) {
        List<ITestcaseNode> names = TestcaseSearch.searchNode(node, new TestNameNode());

        if (names.size() == 1) {
            String name = ((TestNameNode) names.get(0)).getName();
            TestCase testCase = TestCaseManager.getBasicTestCaseByName(name);
            if (testCase != null) {
                UIController.viewCoverageOfATestcase(testCase);
            }
        }
    }

    private void viewCoverageOfSourcecodeFile(ITestcaseNode node, TestCasesTreeItem treeItem) {
        if (!(node instanceof TestUnitNode))
            return;

        TestUnitNode unitNode = (TestUnitNode) node;

        TestCasesNavigatorController.getInstance().refreshNavigatorTree();
        treeItem.loadChildren(true);

//        ProjectNode root = Environment.getInstance().getProjectNode();
//        ISourcecodeFileNode sourcecodeFileNode =
//                UIController.searchSourceCodeFileNodeByPath(unitNode, root);
//
//        if (sourcecodeFileNode != null)
//            logger.debug("The selected src file = " + sourcecodeFileNode.getAbsolutePath());
//        else
//            logger.debug(unitNode.getName() + " not found");
//
//        /**
//         * STEP: Get unsuccessful test cases
//         */
//        List<String> notSuccessTCNames = new ArrayList<>(); // contain name of unsuccessful test cases
//
//        for (TreeItem subprogramTreeItem : treeItem.getChildren())
//            for (Object testcaseItem : subprogramTreeItem.getChildren())
//                // only consider the test cases checked on test case navigator
//                if (testcaseItem instanceof CheckBoxTreeItem &&
//                        ((CheckBoxTreeItem) testcaseItem).getValue() != null &&
//                        ((ITestcaseNode) ((CheckBoxTreeItem) testcaseItem).getValue()).isSelectedInTestcaseNavigator()) {
//                    TestNewNode testNewNode = (TestNewNode) ((CheckBoxTreeItem) testcaseItem).getValue();
//                    if (testNewNode.isPrototypeTestcase()) {
//                        // ignore
//                    } else {
//                        String name = testNewNode.getName();
//                        TestCase testCase = TestCaseManager.getBasicTestCaseByName(name);
//                        if (testCase != null) {
//                            if (testCase.getStatus().equals(TestCase.STATUS_SUCCESS)
//                                || testCase.getStatus().equals(TestCase.STATUS_RUNTIME_ERR)) {
//                                testCases.add(testCase);
//                            } else {
//                                // add to display on popup
//                                notSuccessTCNames.add(testCase.getName());
//                            }
//                        }
//                    }
//                }
//
//        /**
//         * STEP: If there exist at least one successful test case, we need to notify users
//         *
//         */
//        if (notSuccessTCNames.size() > 0) { // need to pupup to notify user
//            SomeTestCasesAreNotSuccessPopupController popupController = SomeTestCasesAreNotSuccessPopupController.getInstance(notSuccessTCNames);
//            Stage popUpWindow = popupController.getPopUpWindow();
//            // block the environment window
//            assert popUpWindow != null;
//            popUpWindow.initModality(Modality.WINDOW_MODAL);
//            popUpWindow.initOwner(UIController.getPrimaryStage().getScene().getWindow());
//
//            popupController.getbContinue().setOnAction(event1 -> {
//                // users accept to compute coverage of test cases
//                UIController.viewCoverageOfMultipleTestcases(sourcecodeFileNode.getName(), testCases);
//                popUpWindow.close();
//            });
//
//            popUpWindow.show();
//        } else {
//            // all test cases are executed successfully before
//            UIController.viewCoverageOfMultipleTestcases(sourcecodeFileNode.getName(), testCases);
//        }

        LoadingPopupController loadingPopup = LoadingPopupController.newInstance("View Coverage");
        loadingPopup.initOwnerStage(UIController.getPrimaryStage());
        loadingPopup.show();

        ClassifyTestCaseTask task = new ClassifyTestCaseTask(unitNode, treeItem);
        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                ClassifyTestCaseTask.TestCaseClassification classification = task.get();

                if (classification.getFailures().isEmpty()) {
                    Path projectPath = Paths.get(Environment.getInstance().getProjectNode().getAbsolutePath());
                    Path unitPath = Paths.get(unitNode.getName());
                    String relativePath = projectPath.relativize(unitPath).toString();
                    UIController.viewCoverageOfMultipleTestcases(relativePath, classification.getSuccesses());
                } else {
                    List<String> notSuccessTCNames = classification
                            .getFailures().stream()
                            .map(AbstractTestCase::getName)
                            .collect(Collectors.toList());

                    SomeTestCasesAreNotSuccessPopupController popupController =
                            SomeTestCasesAreNotSuccessPopupController.getInstance(notSuccessTCNames);

                    assert popupController != null;
                    Stage popUpWindow = popupController.getPopUpWindow();

                    // block the environment window
                    assert popUpWindow != null;
                    popUpWindow.initModality(Modality.WINDOW_MODAL);
                    popUpWindow.initOwner(UIController.getPrimaryStage().getScene().getWindow());

                    popupController.getbContinue().setOnAction(event1 -> {
                        // users accept to compute coverage of test cases
                        UIController.viewCoverageOfMultipleTestcases(unitNode.getName(), classification.getSuccesses());
                        popUpWindow.close();
                    });

                    popUpWindow.show();
                }
            }
        });

        new AkaThread(task).start();
    }

    /**
     * Show prototype tab
     *
     * @param navigatorNode
     * @param item
     * @param nameTestcase
     */
    private void insertPrototypeOfFunction(ITestcaseNode navigatorNode, TestCasesTreeItem item, String nameTestcase) {
        if (!(navigatorNode instanceof TestSubprogramNode))
            return;

        //clear selection
        getTreeTableView().getSelectionModel().clearSelection();

        LoadingPopupController loadingPopup = LoadingPopupController.newInstance("Initializing prototype");
        loadingPopup.initOwnerStage(UIController.getPrimaryStage());
        loadingPopup.show();

        InsertPrototypeTask insertTask = new InsertPrototypeTask((TestSubprogramNode) navigatorNode);
        insertTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                try {
                    TestPrototype testCase = insertTask.get();
                    // display testcase on testcase navigator tree
                    TestNewNode testNewNode = testCase.getTestNewNode();

                    item.setExpanded(true);
                    TestCasesTreeItem newTestCaseTreeItem = new TestCasesTreeItem(testNewNode);
                    item.getChildren().add(newTestCaseTreeItem);
                    getTreeTableView().getSelectionModel().select(newTestCaseTreeItem);

                    // render testcase view in MDI window
                    UIController.viewPrototype(testCase);

                    loadingPopup.close();

                } catch (Exception fe) {
                    UIController.showErrorDialog("Something wrong with selected function", "Prototype generation",
                            "Can not generate a prototype");
                }
            }
        });

        new AkaThread(insertTask).start();
    }

    private void insertNormalTestCase(ITestcaseNode navigatorNode, TestCasesTreeItem item) {
        if (!(navigatorNode instanceof TestNormalSubprogramNode))
            return;

        //clear selection
        getTreeTableView().getSelectionModel().clearSelection();

        // lambda checking
        try {
            ICommonFunctionNode functionNode = UIController.searchFunctionNodeByPath(((TestNormalSubprogramNode) navigatorNode).getName());
            if (functionNode instanceof LambdaFunctionNode) {
                List<String> list = new ArrayList<>();
                if (functionNode.getParent() instanceof SourcecodeFileNode) {
                    INode parentNode = functionNode.getDependencies().stream()
                            .filter(d -> d instanceof FunctionCallDependency && d.getEndArrow().equals(functionNode))
                            .map(Dependency::getStartArrow)
                            .findFirst().orElse(null);
                    if (parentNode instanceof ICommonFunctionNode)
                        list = ((ICommonFunctionNode) parentNode).getArguments().stream()
                                .map(Object::toString)
                                .collect(Collectors.toList());
                } else if (functionNode.getParent() instanceof StructureNode) {
                    list = ((StructureNode) functionNode.getParent())
                            .getPublicAttributes().stream()
                            .map(INode::getName)
                            .collect(Collectors.toList());
                    INode parentNode = functionNode.getDependencies().stream()
                            .filter(d -> d instanceof FunctionCallDependency && d.getEndArrow().equals(functionNode))
                            .map(Dependency::getStartArrow)
                            .findFirst()
                            .orElse(null);
                    if (parentNode instanceof ICommonFunctionNode)
                        list = ((ICommonFunctionNode) parentNode).getArguments().stream()
                                .map(Object::toString)
                                .collect(Collectors.toList());
                }

                list.add("this");
                List<String> captures = Arrays.stream(((LambdaFunctionNode) functionNode).getOriginalAST().getCaptures())
                        .map(IASTNode::getRawSignature)
                        .collect(Collectors.toList());

                for (String c : captures) {
                    boolean accepted = false;
                    for (String l : list) {
                        if (c.startsWith(l)) {
                            accepted = true;
                        }
                    }
                    if (!accepted) {
                        UIController.showErrorDialog("Not yet supported", "Error", "Error");
                        return;
                    }
                }
            }
        } catch (Exception e) {
            logger.debug(e);
        }

        LoadingPopupController loadingPopup = LoadingPopupController.newInstance("Initializing test case");
        loadingPopup.initOwnerStage(UIController.getPrimaryStage());
        loadingPopup.show();

        InsertTestCaseTask insertTask = new InsertTestCaseTask((TestNormalSubprogramNode) navigatorNode);

        insertTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                try {
                    TestCase testCase = insertTask.get();

                    TestNewNode testNewNode = testCase.getTestNewNode();

                    // display testcase on testcase navigator tree
                    item.setExpanded(true);
                    TestCasesTreeItem newTestCaseTreeItem = new TestCasesTreeItem(testNewNode);
                    item.getChildren().add(newTestCaseTreeItem);
                    getTreeTableView().getSelectionModel().select(newTestCaseTreeItem);

                    // render testcase view in MDI window
                    UIController.viewTestCase(testCase);

                    loadingPopup.close();

                } catch (Exception ex) {
                    UIController.showErrorDialog("Something wrong with selected function", "Generate test case",
                            "Can not generate test case");
                }
            }
        });

        new AkaThread(insertTask).start();
    }

    private void insertCompoundTestCase(ITestcaseNode navigatorNode, TestCasesTreeItem item) {
        if (!(navigatorNode instanceof TestSubprogramNode))
            return;

        //clear selection
        getTreeTableView().getSelectionModel().clearSelection();

        LoadingPopupController loadingPopup = LoadingPopupController.newInstance("Initializing test case");
        loadingPopup.initOwnerStage(UIController.getPrimaryStage());
        loadingPopup.show();

        InsertCompoundTestCaseTask insertTask = new InsertCompoundTestCaseTask((TestSubprogramNode) navigatorNode);

        insertTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                try {
                    CompoundTestCase testCase = insertTask.get();

                    TestNewNode testNewNode = testCase.getTestNewNode();

                    // display testcase on testcase navigator tree
                    item.setExpanded(true);
                    TestCasesTreeItem newTestCaseTreeItem = new TestCasesTreeItem(testNewNode);
                    item.getChildren().add(newTestCaseTreeItem);
                    getTreeTableView().getSelectionModel().select(newTestCaseTreeItem);

                    // render testcase view in MDI window
                    UIController.viewTestCase(testCase);

                    loadingPopup.close();

                } catch (Exception ex) {
                    UIController.showErrorDialog("Something wrong with compound unit", "Generate test case",
                            "Can not generate test case");
                }
            }
        });

        new AkaThread(insertTask).start();
    }
}