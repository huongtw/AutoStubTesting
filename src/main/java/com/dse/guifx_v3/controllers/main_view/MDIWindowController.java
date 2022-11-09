package com.dse.guifx_v3.controllers.main_view;

import auto_testcase_generation.cte.UI.Controller.CteController;
import com.dse.compiler.Compiler;
import com.dse.compiler.message.ICompileMessage;
import com.dse.coverage.CoverageDataObject;
import com.dse.debugger.controller.DebugController;
import com.dse.environment.Environment;
import com.dse.guifx_v3.controllers.CompoundTestCaseTreeTableViewController;
import com.dse.guifx_v3.controllers.FunctionConfigurationController;
import com.dse.guifx_v3.controllers.TestCasesExecutionTabController;
import com.dse.guifx_v3.controllers.TestCasesNavigatorController;
import com.dse.guifx_v3.controllers.object.LoadingPopupController;
import com.dse.guifx_v3.helps.Factory;
import com.dse.guifx_v3.helps.TCExecutionDetailLogger;
import com.dse.guifx_v3.helps.UIController;
import com.dse.guifx_v3.objects.FXFileView;
import com.dse.guifx_v3.objects.SourceCodeViewTab;
import com.dse.logger.AkaLogger;
import com.dse.parser.dependency.Dependency;
import com.dse.parser.dependency.IncludeHeaderDependency;
import com.dse.parser.object.*;
import com.dse.probe_point_manager.ProbePointManager;
import com.dse.probe_point_manager.ProbePointUtils;
import com.dse.probe_point_manager.controllers.AddEditProbePointController;
import com.dse.probe_point_manager.objects.ProbePoint;
import com.dse.probe_point_manager.objects.ProbePointSourceCodeViewTab;
import com.dse.project_init.ProjectClone;
import com.dse.report.ReleaseNotes;
import com.dse.search.Search;
import com.dse.search.condition.SourcecodeFileNodeCondition;
import com.dse.testcase_manager.ITestCase;
import com.dse.testcase_manager.TestCase;
import com.dse.thread.AkaThread;
import com.dse.thread.task.IncrementalBuildEnvironmentLoaderTask;
import com.dse.thread.task.view_report.ViewCoverageTask;
import com.dse.thread.task.view_report.ViewReleaseNotesTask;
import com.dse.util.Utils;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.Workbook;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLAnchorElement;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.*;

import static com.dse.environment.Environment.backupEnvironment;

public class MDIWindowController implements Initializable {

    private final static AkaLogger logger = AkaLogger.get(MDIWindowController.class);

    /**
     * Singleton pattern like
     */
    private static AnchorPane mdiWindow = null; // view
    private static MDIWindowController mdiWindowController = null;

    private static void prepare() {
        FXMLLoader loader = new FXMLLoader(Object.class.getResource("/FXML/MDIWindow.fxml"));
        try {
            Parent parent = loader.load();
            mdiWindow = (AnchorPane) parent;
            mdiWindowController = loader.getController();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static AnchorPane getMDIWindow() {
        if (mdiWindow == null) {
            prepare();
        }
        return mdiWindow;
    }

    public static MDIWindowController getMDIWindowController() {
        if (mdiWindowController == null) {
            prepare();
        }
        return mdiWindowController;
    }

    // mdiTabPane > mdiTabTestCases, mdiTabSourceCode > testCaseViews, sourceCodeView > testCaseView, sourceCodeView
    @FXML
    private TabPane mdiTabPane;
    @FXML
    private Tab mdiTabTestCases;
    @FXML
    private Tab mdiTabSourceCode;
    @FXML
    private Tab mdiTabCompounds;
    @FXML
    private Tab mdiTabDebug;
    @FXML
    private Tab mdiTabFunctionConfiguration;
    @FXML
    private Tab mdiTabTestCasesExecution;
    @FXML
    private Tab mdiTabCoverage;
    @FXML
    private Tab mdiTabReports;
    @FXML
    private Tab mdiTabProbePoint;
    @FXML
    private Tab mdiTabPrototypes;
    @FXML
    private Tab mdiTabAboutUs;
    @FXML
    private Tab mdiTabCte;
    @FXML
    private TabPane testCaseViews;
    @FXML
    private TabPane prototypeViews;
    @FXML
    private TabPane sourceCodeViews;
    @FXML
    private TabPane testCasesExecutions;
    @FXML
    private TabPane coverageViews;
    @FXML
    private TabPane functionConfigurations;
    @FXML
    private TabPane compounds; // mdiTabCompounds contains compounds tab pane
    @FXML
    private TabPane reports;
    @FXML
    private TabPane probePointSourceCodeViews;
    @FXML
    private TabPane tpAboutUs;
    @FXML
    private TabPane cteView;
    @FXML
    private AnchorPane paneCTE;
    @FXML
    private ScrollPane paneCT;
    @FXML
    private ScrollPane paneOT;
    @FXML
    private ScrollPane paneTT;
    @FXML
    private ScrollPane paneDI;
    @FXML
    private ScrollBar horBarCT;
//    @FXML
//    private ScrollBar horBarOT;
    @FXML
    private Button bCompileOpeningSourcecodeTab;
    @FXML
    private Button bCompileAllSourcecodeTabs;
    @FXML
    private Button bRefreshAllOpeningSourceCodeFileTabs;
    @FXML
    private Button bSaveOpeningSourcecodeTab;
    @FXML
    private Button bSaveAllOpeningSourcecodeTabs;
    @FXML
    private Button bOpenProbePointMode;
    @FXML
    private Button bChangeMode;
    @FXML
    private ListView<ProbePoint> lvProbePoints;

    private final List<String> compoundTestcaseTabNames = new ArrayList<>();
    private final List<String> testCasesTabNames = new ArrayList<>();
    private final List<String> prototypeTabNames = new ArrayList<>();
    private final List<String> sourceCodeTabNames = new ArrayList<>();
    private final List<String> functionConfigTabNames = new ArrayList<>();
    private final List<String> testCasesExecutionsTabNames = new ArrayList<>();
    private final List<String> coverageViewsTabNames = new ArrayList<>();
    private final List<String> reportsTabNames = new ArrayList<>();
    private final List<String> probePointSourceCodeTabNames = new ArrayList<>();
    private final Map<String, CompoundTestCaseTreeTableViewController> compoundTestCaseControllerMap = new HashMap<>();
    private final List<String> cteTabNames = new ArrayList<>();

    @FXML
    public void mdiTabPrototypeClose(Event event) {
        prototypeViews.getTabs().clear();
        prototypeTabNames.clear();
    }

    @FXML
    void mdiTabTestCasesClose() {
        testCaseViews.getTabs().clear();
        testCasesTabNames.clear();
    }

    @FXML
    void mdiTabReportsClose() {
        reports.getTabs().clear();
        reportsTabNames.clear();
    }

    @FXML
    void mdiTabCoverageClose() {
        coverageViews.getTabs().clear();
        coverageViewsTabNames.clear();
    }

    @FXML
    void mdiTabTestCasesExecutionClose() {
        testCasesExecutions.getTabs().clear();
        testCasesExecutionsTabNames.clear();
    }

    @FXML
    void mdiTabFunctionConfigClose() {
        functionConfigurations.getTabs().clear();
        functionConfigTabNames.clear();
    }

    @FXML
    void mdiTabCompoundsClose() {
        compounds.getTabs().clear();
        compoundTestcaseTabNames.clear();
        compoundTestCaseControllerMap.clear();
        Environment.getInstance().setCurrentTestcompoundController(null);
    }

    @FXML
    void mdiTabProbePointClose() {
        probePointSourceCodeViews.getTabs().clear();
        probePointSourceCodeTabNames.clear();
    }

    @FXML
    public void mdiTabSourceCodeClose() {
        sourceCodeViews.getTabs().clear();
        sourceCodeTabNames.clear();
    }
    @FXML
    public void mdiTabCteClose()
    {
        cteView.getTabs().clear();
        cteTabNames.clear();
    }

    public void removeViewsAfterChangeCoverageType() {
        mdiTabPane.getTabs().removeAll(mdiTabReports, mdiTabTestCasesExecution, mdiTabCoverage);
        mdiTabReportsClose();
        mdiTabTestCasesExecutionClose();
        mdiTabCoverageClose();
    }

    public void initialize(URL location, ResourceBundle resources) {
        mdiTabPane.getTabs().clear();

        testCaseViews.getTabs().clear();
        sourceCodeViews.getTabs().clear();
        compounds.getTabs().clear();
        functionConfigurations.getTabs().clear();
        testCasesExecutions.getTabs().clear();
        coverageViews.getTabs().clear();
        reports.getTabs().clear();
        probePointSourceCodeViews.getTabs().clear();
        cteView.getTabs().clear();

        mdiTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        testCaseViews.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        sourceCodeViews.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        prototypeViews.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        compounds.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        functionConfigurations.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        testCasesExecutions.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        coverageViews.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        reports.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        cteView.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);

        Image compileIcon = new Image(Factory.class.getResourceAsStream("/icons/open_source/compile.png"));
        bCompileOpeningSourcecodeTab.setGraphic(new ImageView(compileIcon));
        addCompileASourcecodeFileOption();

        Image compileAllIcon = new Image(Factory.class.getResourceAsStream("/icons/open_source/compileAll.png"));
        bCompileAllSourcecodeTabs.setGraphic(new ImageView(compileAllIcon));
        addCompileAllSourcecodeFilesOption();

        Image refreshIcon = new Image(Factory.class.getResourceAsStream("/icons/open_source/refresh.png"));
        bRefreshAllOpeningSourceCodeFileTabs.setGraphic(new ImageView(refreshIcon));
        addRefreshAllSourcecodeFileTabsOption();

        Image probpointIcon = new Image(Factory.class.getResourceAsStream("/icons/open_source/probe_point_16px.png"));
        bOpenProbePointMode.setGraphic(new ImageView(probpointIcon));
        addOpenProbePointMode();

        Image saveIcon = new Image(Factory.class.getResourceAsStream("/icons/open_source/save.png"));
        bSaveOpeningSourcecodeTab.setGraphic(new ImageView(saveIcon));
        addSaveActiveSourcecodeTabOption();

        Image saveAllIcon = new Image(Factory.class.getResourceAsStream("/icons/open_source/saveAll.png"));
        bSaveAllOpeningSourcecodeTabs.setGraphic(new ImageView(saveAllIcon));
        addSaveAllActiveSourcecodeTabOption();

        Image editIcon = new Image(Factory.class.getResourceAsStream("/icons/open_source/edit.png"));
        bChangeMode.setGraphic(new ImageView(editIcon));
        addChangeMode();

        disableButtons(true);// default mode is View Mode, so we disable some buttons

        lvProbePoints.setCellFactory(param -> new ListCell<ProbePoint>() {
            @Override
            protected void updateItem(ProbePoint item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setGraphic(null);
                    setText(null);
                } else if (item.getName() != null) {
                    setText(item.getName());
                    ContextMenu contextMenu = new ContextMenu();
                    setContextMenu(contextMenu);

                    addEditProbePoint(item);
                    addDeleteProbePoint(item);

                    if (!item.isValid()) {
                        setStyle("-fx-text-fill: red");
                    } else
                        setStyle("");
                }
            }

            private void addEditProbePoint(ProbePoint item) {
                MenuItem mi = new MenuItem("Edit");
                mi.setOnAction(event -> {
                    if (item != null) {
                        Stage window = AddEditProbePointController.getWindow(AddEditProbePointController.TYPE_EDIT, item, lvProbePoints);
                        if (window != null) {
                            window.setResizable(false);
                            window.initModality(Modality.WINDOW_MODAL);
                            window.initOwner(UIController.getPrimaryStage().getScene().getWindow());
                            window.show();
                        }
                    }
                });
                getContextMenu().getItems().add(mi);
            }

            private void addDeleteProbePoint(ProbePoint item) {
                MenuItem mi = new MenuItem("Delete");
                mi.setOnAction(event -> {
//                    if (ProbePointUtils.deleteProbePointInFile(item)) {
//                        lvProbePoints.getItems().remove(item);
//                        ProbePointManager.getInstance().remove(item);
//                        lvProbePoints.refresh();
//                    }

                    ProbePointUtils.deleteProbePointInFile(item);
                    lvProbePoints.getItems().remove(item);
                    ProbePointManager.getInstance().remove(item);
                    lvProbePoints.refresh();
                });
                getContextMenu().getItems().add(mi);
            }
        });
    }

    private void addSaveActiveSourcecodeTabOption() {
        bSaveOpeningSourcecodeTab.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                saveActiveSourcecodeTab();
            }
        });
    }

    private void addChangeMode() {
        bChangeMode.setOnMouseClicked(event -> {
            if (UIController.getMode().equals(UIController.MODE_VIEW))
                setMode(UIController.MODE_EDIT);
            else if (UIController.getMode().equals(UIController.MODE_EDIT))
                setMode(UIController.MODE_VIEW);
        });
    }

    private void setMode(String mode) {
        if (mode.equals(UIController.MODE_VIEW)) {
            if (askToChangeToViewMode()) {// ask for changing to view mode
                // popup loading
                LoadingPopupController loadingPopup = LoadingPopupController.newInstance("Incremental building");
                loadingPopup.initOwnerStage(UIController.getPrimaryStage());
                loadingPopup.show();

                // save all source code file
                saveAllActiveSourcecodeTab();
                // incremental build
                // backup Environment before build
                backupEnvironment();
                IncrementalBuildEnvironmentLoaderTask task = new IncrementalBuildEnvironmentLoaderTask();

                task.setOnAllow(new IncrementalBuildEnvironmentLoaderTask.AllowEditModeListener() {
                    @Override
                    public void onAllow() {
                        for (Tab scTab : sourceCodeViews.getTabs()) {
                            ((SourceCodeViewTab) scTab).getCodeArea().setEditable(false);
                        }
                        Image saveIcon = new Image(Factory.class.getResourceAsStream("/icons/open_source/edit.png"));
                        bChangeMode.setGraphic(new ImageView(saveIcon));
                        bChangeMode.getTooltip().setText("Open Edit Mode");
                        bOpenProbePointMode.setDisable(false);
                        disableButtons(true);

                        UIController.setMode(mode);
                        // refresh menu item
                        TestCasesNavigatorController.getInstance().refreshNavigatorTree();
                    }
                });

                task.setOnSucceeded(event -> loadingPopup.close());

                new AkaThread(task).start();
            }
        } else if (mode.equals(UIController.MODE_EDIT)) {
            if (askToChangeToEditMode()) {// ask for changing to edit mode
                // close probe point tab
                mdiTabPane.getTabs().remove(mdiTabProbePoint);
                for (Tab scTab : sourceCodeViews.getTabs()) {
                    ((SourceCodeViewTab) scTab).getCodeArea().setEditable(true);
                }
                Image saveIcon = new Image(Factory.class.getResourceAsStream("/icons/open_source/eye_view.png"));
                bChangeMode.setGraphic(new ImageView(saveIcon));
                bChangeMode.getTooltip().setText("Open View Mode");
                bOpenProbePointMode.setDisable(true);
                disableButtons(false);

                UIController.setMode(mode);
                // refresh menu item
                TestCasesNavigatorController.getInstance().refreshNavigatorTree();
            }
        }
    }

    /**
     * disable/enable buttons:
     * Save and compile all opening source code Tabs
     * Save and compile Opening source code Tabs
     * Compile all opening source code Tabs
     * Compile all opening source code Tabs
     * Refresh All Opening Source code File tabs
     *
     * @param disable disable or not
     */
    private void disableButtons(boolean disable) {
        bSaveAllOpeningSourcecodeTabs.setDisable(disable);
        bSaveOpeningSourcecodeTab.setDisable(disable);
        bCompileAllSourcecodeTabs.setDisable(disable);
        bCompileOpeningSourcecodeTab.setDisable(disable);
        bRefreshAllOpeningSourceCodeFileTabs.setDisable(disable);
    }

    private boolean askToChangeToEditMode() {
        Alert confirmAlert = UIController.showYesNoDialog(Alert.AlertType.CONFIRMATION,
                "Change mode confirmation",
                "Do you want to change to edit mode?",
                "If YES, Test cases execution features and Test data automatic " +
                        "generation features will be disable, " +
                        "the probe point tab will be close.");

        Optional<ButtonType> option = confirmAlert.showAndWait();
        if (option.get() == ButtonType.YES) {
            return true;
        }
        return false;
    }

    private boolean askToChangeToViewMode() {
        Alert confirmAlert = UIController.showYesNoDialog(Alert.AlertType.CONFIRMATION,
                "Change mode confirmation",
                "Do you want to change to view mode?",
                "If YES, all modified source file will be saved, " +
                        "the current environment will be incremental build.");

        Optional<ButtonType> option = confirmAlert.showAndWait();
        if (option.get() == ButtonType.YES) {
            return true;
        }
        return false;
    }

    private void addOpenProbePointMode() {
        bOpenProbePointMode.setOnMouseClicked(event -> {
            openProbePointManager();
        });
    }

    private void saveActiveSourcecodeTab() {
        Tab currentTab = sourceCodeViews.getSelectionModel().getSelectedItem();
        saveSourceCodeInTab(currentTab);

        // if content of the source code file is modified then block Probe Point function
    }

    private String getContentOfSourceCodeFileTab(Tab tab) {
        // get text inside a tab
        String content = "";
        AnchorPane anchorPane = (AnchorPane) tab.getContent();
        Node firstChild = anchorPane.getChildren().get(0);
        if (firstChild instanceof VirtualizedScrollPane) {
            Object codeArea = ((VirtualizedScrollPane) firstChild).getContent();
            if (codeArea instanceof CodeArea) {
                content = ((CodeArea) codeArea).getText();
            }
        }
        return content;
    }

    private void addSaveAllActiveSourcecodeTabOption() {
        bSaveAllOpeningSourcecodeTabs.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                saveAllActiveSourcecodeTab();
            }
        });
    }

    private void saveAllActiveSourcecodeTab() {
        for (Tab currentTab : Environment.getInstance().getActiveSourcecodeTabs().keySet()) {
            saveSourceCodeInTab(currentTab);
        }
        // todo: ask to save when close an active source code tab
    }

    private void saveSourceCodeInTab(Tab currentTab) {
        // save in origin source code file, do not update to ignore file
        ISourcecodeFileNode activeSrcNode = (ISourcecodeFileNode) Environment.getInstance()
                .getActiveSourcecodeTabs().get(currentTab);
        String newContent = getContentOfSourceCodeFileTab(currentTab);
        Utils.writeContentToFile(newContent, activeSrcNode);
    }

    /**
     * Used after open an environment, incremental build or update environment.
     * Enable button open probe point.
     */
    private void addRefreshAllSourcecodeFileTabsOption() {
        bRefreshAllOpeningSourceCodeFileTabs.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                for (Tab currentTab : Environment.getInstance().getActiveSourcecodeTabs().keySet()) {
                    INode activeSrcNode = Environment.getInstance().getActiveSourcecodeTabs().get(currentTab);
                    if (activeSrcNode instanceof ISourcecodeFileNode) {
                        FXFileView fileView = new FXFileView(activeSrcNode);
                        AnchorPane acp = fileView.getAnchorPane(true);
                        currentTab.setContent(acp);
                    }
                }
            }
        });
    }

    private List<INode> findCompilableSourceCodes() {
        // search for source code file nodes in source code file lists and compile them
        INode root = Environment.getInstance().getProjectNode();
        List<INode> sourceFiles = Search.searchNodes(root, new SourcecodeFileNodeCondition());

        List<INode> ignores = Environment.getInstance().getIgnores();
        List<String> libraries = ProjectClone.getLibraries();

        // remove all ignore files and libraries
        sourceFiles.removeIf(f -> ignores.contains(f) || libraries.contains(f.getAbsolutePath()));

        // remove included file
        sourceFiles.removeIf(f -> {
            for (Dependency d : f.getDependencies()) {
                if (d instanceof IncludeHeaderDependency && d.getEndArrow() == f)
                    return true;
            }

            return false;
        });

        return sourceFiles;
    }

    private void addCompileAllSourcecodeFilesOption() {
        bCompileAllSourcecodeTabs.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                saveAllActiveSourcecodeTab();

                Compiler c = Environment.getInstance().getCompiler();
                List<INode> sourceCodes = findCompilableSourceCodes();

                for (INode currentSrcFile : sourceCodes) {
                    ICompileMessage message = c.compile(currentSrcFile);

                    if (message.getType() == ICompileMessage.MessageType.ERROR) {
                        String error = "Source code file: " + currentSrcFile.getAbsolutePath()
                                + "\nMESSSAGE:\n" + message.getMessage() + "\n----------------\n";
                        UIController.showDetailDialog(Alert.AlertType.ERROR, "Compilation message", "Compile message", error);
                        return;
                    }
                }
                UIController.showSuccessDialog("Compile all source code files successfully"
                        , "Compilation message", "Compile message");
            }
        });
    }

    private void addCompileASourcecodeFileOption() {
        bCompileOpeningSourcecodeTab.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                saveActiveSourcecodeTab();

                Compiler c = Environment.getInstance().getCompiler();
                Tab currentTab = sourceCodeViews.getSelectionModel().getSelectedItem();
                INode currentSrcFile = Environment.getInstance().getActiveSourcecodeTabs().get(currentTab);
//                UILogger.getUiLogger().log("Compiling " + currentSrcFile.getAbsolutePath());

                // compile
                ICompileMessage message = c.compile(currentSrcFile);
                if (message.getType() == ICompileMessage.MessageType.ERROR) {
                    String error = "Source code file: " + currentSrcFile.getAbsolutePath()
                            + "\nMESSSAGE:\n" + message.getMessage() + "\n----------------\n";
                    UIController.showDetailDialog(Alert.AlertType.ERROR, "Compilation message", "Compile message", error);
                } else {
                    UIController.showSuccessDialog("Compile the opening source code file " + currentSrcFile.getAbsolutePath() + " successfully"
                            , "Compilation message", "Compile message");
                }
            }
        });
    }

    private Tab getCompoundTestcaseTabByName(String name) {
        for (Tab tab : compounds.getTabs()) {
            if (tab.getText().equals(name)) return tab;
        }
        return null;
    }

    private Tab getCoverageViewTabByName(String name) {
        for (Tab tab : coverageViews.getTabs()) {
            if (tab.getText().equals(name)) return tab;
        }
        return null;
    }

    private Tab getProbePointSourceCodeViewTabByName(String name) {
        for (Tab tab : probePointSourceCodeViews.getTabs()) {
            if (tab.getText().equals(name)) return tab;
        }
        return null;
    }

    private Tab getPrototypeTabByName(String name) {
        for (Tab tab : prototypeViews.getTabs()) {
            if (tab.getText().equals(name)) return tab;
        }
        return null;
    }

    private Tab getTestCasesTabByName(String name) {
        for (Tab tab : testCaseViews.getTabs()) {
            if (tab.getText().equals(name)) return tab;
        }
        return null;
    }

    private Tab getTestCasesExecutionTabByName(String name) {
        for (Tab tab : testCasesExecutions.getTabs()) {
            if (tab.getText().equals(name)) return tab;
        }
        return null;
    }

    private Tab getSourceCodeTabByName(String name) {
        for (Tab tab : sourceCodeViews.getTabs()) {
            if (tab.getText().equals(name)) return tab;
        }
        return null;
    }

    private Tab getCteTabByName(String name) {
        for (Tab tab : cteView.getTabs()) {
            if (tab.getText().equals(name)) return tab;
        }
        return null;
    }

    private Tab getFunctionConfigTabByName(String name) {
        for (Tab tab : functionConfigurations.getTabs()) {
            if (tab.getText().equals(name)) return tab;
        }
        return null;
    }

    private Tab getReportsTabByName(String name) {
        for (Tab tab : reports.getTabs()) {
            if (tab.getText().equals(name)) return tab;
        }
        return null;
    }

    public Map<String, CompoundTestCaseTreeTableViewController> getCompoundTestCaseControllerMap() {
        return compoundTestCaseControllerMap;
    }

    public void viewCompoundTestCase(Tab tab, String name) {
        // Add a tab which is used to contain Compounds tabs if it does not exist
        if (!mdiTabPane.getTabs().contains(mdiTabCompounds)) {
            mdiTabPane.getTabs().add(mdiTabCompounds);
        }

        if (compoundTestcaseTabNames.contains(name)) {
            Tab ctcTab = getCompoundTestcaseTabByName(name);
            if (ctcTab != null) {
                compounds.getSelectionModel().select(ctcTab);
                mdiTabPane.getSelectionModel().select(mdiTabCompounds);
            }
        } else {
            // Add a new compound test case tab

            tab.setOnClosed(event -> {
                compoundTestcaseTabNames.remove(name);
                compoundTestCaseControllerMap.remove(name);
                // when there are no compound testcase views open
                if (compoundTestCaseControllerMap.size() == 0) {
                    Environment.getInstance().setCurrentTestcompoundController(null);
                }
                logger.debug("Size of compound testcase controller map: " + compoundTestCaseControllerMap.size());
            });

            compounds.getTabs().add(tab);
            compoundTestcaseTabNames.add(name);
            compounds.getSelectionModel().select(tab);
            mdiTabPane.getSelectionModel().select(mdiTabCompounds);
        }
    }

    public void viewTestCase(AnchorPane testCaseView, String name) {
        // Add a tab which is used to contain test case tabs if it does not exist
        if (!mdiTabPane.getTabs().contains(mdiTabTestCases)) {
            mdiTabPane.getTabs().add(mdiTabTestCases);
        }

        if (testCasesTabNames.contains(name)) { // the test case is opened
            Tab tab = getTestCasesTabByName(name);
            if (tab != null) {
                testCaseViews.getSelectionModel().select(tab);
                mdiTabPane.getSelectionModel().select(mdiTabTestCases);
            }
        } else {
            // Add a new test case tab
            Tab tcTab = new Tab(name);
            tcTab.setContent(testCaseView);
            testCaseViews.getTabs().add(tcTab);
            testCaseViews.getSelectionModel().select(tcTab);
            mdiTabPane.getSelectionModel().select(mdiTabTestCases);
            testCasesTabNames.add(name);

            // event when closing the test case tab
            tcTab.setOnClosed(event -> testCasesTabNames.remove(name));
            tcTab.setOnSelectionChanged(arg0 -> Environment.getInstance().setCurrentTestcaseTab(tcTab.getText()));
        }
    }

    public void viewPrototype(AnchorPane prototypeView, String name) {
        // Add a tab which is used to contain test case tabs if it does not exist
        if (!mdiTabPane.getTabs().contains(mdiTabPrototypes)) {
            mdiTabPane.getTabs().add(mdiTabPrototypes);
        }

        if (prototypeTabNames.contains(name)) { // the test case is opened
            Tab tab = getPrototypeTabByName(name);
            if (tab != null) {
                prototypeViews.getSelectionModel().select(tab);
                mdiTabPane.getSelectionModel().select(mdiTabPrototypes);
            }
        } else {
            // Add a new test case tab
            Tab tcTab = new Tab(name);
            tcTab.setContent(prototypeView);
            prototypeViews.getTabs().add(tcTab);
            prototypeViews.getSelectionModel().select(tcTab);
            mdiTabPane.getSelectionModel().select(mdiTabPrototypes);
            prototypeTabNames.add(name);

            // event when closing the test case tab
            tcTab.setOnClosed(event -> prototypeTabNames.remove(name));
//            tcTab.setOnSelectionChanged(arg0 -> Environment.getInstance().setCurrentPrototypeTab(tcTab.getText()));
        }
    }

    public void viewTestCasesExecution(ICommonFunctionNode functionNode) {
        String name = functionNode.getSingleSimpleName();
        if (!mdiTabPane.getTabs().contains(mdiTabTestCasesExecution)) {
            mdiTabPane.getTabs().add(mdiTabTestCasesExecution);
        }
        mdiTabPane.getSelectionModel().select(mdiTabTestCasesExecution);

        if (testCasesExecutionsTabNames.contains(name)) {
            Tab tab = getTestCasesExecutionTabByName(name);
            if (tab != null) {
                testCasesExecutions.getSelectionModel().select(tab);
            }
        } else {
            TestCasesExecutionTabController controller = TCExecutionDetailLogger.getTCExecTabControllerByFunction(functionNode);
            if (controller == null) {
                // init
                TCExecutionDetailLogger.initFunctionExecutions(functionNode);
            }
            controller = TCExecutionDetailLogger.getTCExecTabControllerByFunction(functionNode);
            if (controller != null) {
                Tab tab = controller.getTab();
                testCasesExecutions.getTabs().add(tab);
                testCasesExecutions.getSelectionModel().select(tab);
                testCasesExecutionsTabNames.add(name);

                // event when closing the function configuration tab
                tab.setOnClosed(event -> testCasesExecutionsTabNames.remove(name));
            }
        }
    }

    public void removeTestCasesExecutionTabByFunction(ICommonFunctionNode functionNode) {
        String name = functionNode.getSingleSimpleName();
        Tab tab = getTestCasesExecutionTabByName(name);
        testCasesExecutions.getTabs().remove(tab);
        testCasesExecutionsTabNames.remove(name);
    }

    public void viewFunctionConfiguration(ICommonFunctionNode functionNode) {
        FunctionConfigurationController controller = FunctionConfigurationController.getInstance(functionNode);
        if (controller != null) {
            controller.showStage();
        }
    }

    public void openProbePointManager() {
        Tab currentSourceCodeViewTab = sourceCodeViews.getSelectionModel().getSelectedItem();
        INode activeSrcNode = Environment.getInstance().getActiveSourcecodeTabs().get(currentSourceCodeViewTab);
//        if (modifiedSourceNode.contains(activeSrcNode)) {
//            return; // block open probe point function
//        }

        if (!mdiTabPane.getTabs().contains(mdiTabProbePoint)) {
            mdiTabPane.getTabs().add(mdiTabProbePoint);
        }
        mdiTabPane.getSelectionModel().select(mdiTabProbePoint);

        if (currentSourceCodeViewTab != null) {
//            SourcecodeFileNode sourcecodeFileNode = (SourcecodeFileNode) ((SourceCodeViewTab) currentSourceCodeViewTab).getSourceCodeFileNode();
            ISourcecodeFileNode sourcecodeFileNode = (ISourcecodeFileNode) activeSrcNode;
            String name = sourcecodeFileNode.getName();

            // index to display tab source code of probe points
            int index = probePointSourceCodeViews.getTabs().size();
            if (probePointSourceCodeTabNames.contains(name)) {
                Tab tab = getProbePointSourceCodeViewTabByName(name);
                if (tab != null) {
                    index = probePointSourceCodeViews.getTabs().indexOf(tab);
                    probePointSourceCodeViews.getTabs().remove(index);
                    probePointSourceCodeTabNames.remove(name);
                }
            }

            ProbePointSourceCodeViewTab scTab = new ProbePointSourceCodeViewTab(sourcecodeFileNode,
                    ProbePointManager.getInstance().getListOfProbePointLines(sourcecodeFileNode));
            probePointSourceCodeViews.getTabs().add(index, scTab);
            probePointSourceCodeTabNames.add(name);
            probePointSourceCodeViews.getSelectionModel().select(scTab);
            scTab.setOnClosed(event -> probePointSourceCodeTabNames.remove(name));
        }
    }

    // hoan_tmp, should be deleted
    public void viewSourceCode(ISourcecodeFileNode sourcecodeFileNode) {
        if (!mdiTabPane.getTabs().contains(mdiTabProbePoint)) {
            mdiTabPane.getTabs().add(mdiTabProbePoint);
        }
        mdiTabPane.getSelectionModel().select(mdiTabProbePoint);

        ProbePointSourceCodeViewTab scTab = new ProbePointSourceCodeViewTab(sourcecodeFileNode, new TreeSet<>());
        probePointSourceCodeViews.getTabs().add(scTab);
        probePointSourceCodeViews.getSelectionModel().select(scTab);
    }

    public void updateLVProbePoints() {
        lvProbePoints.getItems().clear();
        List<ProbePoint> probePoints = ProbePointManager.getInstance().getAllProbePoint();
        lvProbePoints.getItems().addAll(probePoints);
    }

    public void viewSourceCode(INode sourcecodeFileNode, FXFileView fileView) {
        AnchorPane sourceCodeView = fileView.getAnchorPane(true);
        if (!(sourcecodeFileNode instanceof ISourcecodeFileNode))
            return;
        String name = sourcecodeFileNode.getAbsolutePath().replace(Environment.getInstance().getProjectNode().getAbsolutePath(), "");
        if (!mdiTabPane.getTabs().contains(mdiTabSourceCode)) {
            mdiTabPane.getTabs().add(mdiTabSourceCode);
        }

        mdiTabPane.getSelectionModel().select(mdiTabSourceCode);

        if (sourceCodeTabNames.contains(name)) {
            Tab tab = getSourceCodeTabByName(name);
            sourceCodeViews.getTabs().remove(tab);
        }

        mdiTabPane.getSelectionModel().select(mdiTabSourceCode);

        SourceCodeViewTab scTab = new SourceCodeViewTab(sourcecodeFileNode);
        scTab.setCodeArea(fileView.getCodeArea());
        scTab.getCodeArea().setEditable(UIController.getMode().equals(UIController.MODE_EDIT));
        scTab.setText(name);
        sourceCodeViews.getTabs().add(scTab);
        scTab.setContent(sourceCodeView);
        sourceCodeViews.getSelectionModel().select(scTab);
        sourceCodeTabNames.add(name);

//        scTab.setOnSelectionChanged(event ->
//                bOpenProbePointMode.setDisable(modifiedSourceNode.contains(sourcecodeFileNode)));
//        bOpenProbePointMode.setDisable(modifiedSourceNode.contains(sourcecodeFileNode));

        Environment.getInstance().getActiveSourcecodeTabs().remove(scTab);
        Environment.getInstance().getActiveSourcecodeTabs().put(scTab, sourcecodeFileNode);

        scTab.setOnClosed(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                sourceCodeTabNames.remove(name);
                Environment.getInstance().getActiveSourcecodeTabs().remove(event.getSource());
            }
        });
    }

    public void viewClassificationTree(IFunctionNode FFileNode, CteController cteController){

        AnchorPane cteWindow = cteController.getCteWindow();

        if (FFileNode == null)
            return;
      //  String name = FFileNode.getAbsolutePath().replace(Environment.getInstance().getProjectNode().getAbsolutePath(), "");
        String name = FFileNode.getSimpleName();
        if (!mdiTabPane.getTabs().contains(mdiTabCte)) {
            mdiTabPane.getTabs().add(mdiTabCte);
        }

        mdiTabPane.getSelectionModel().select(mdiTabCte);

        cteController.getCteWindowController().getDInTR().minWidthProperty().bind(cteView.widthProperty());

        if (cteTabNames.contains(name)) {
            Tab tab = getCteTabByName(name);
            cteView.getTabs().remove(tab);
        }

        mdiTabPane.getSelectionModel().select(mdiTabCte);


        Tab cteTab = new Tab(name);
        //cteTab.setText(name);
        cteView.getTabs().add(cteTab);
        cteTab.setContent(cteWindow);
        cteView.getSelectionModel().select(cteTab);
        cteTabNames.add(name);

//        scTab.setOnSelectionChanged(event ->
//                bOpenProbePointMode.setDisable(modifiedSourceNode.contains(sourcecodeFileNode)));
//        bOpenProbePointMode.setDisable(modifiedSourceNode.contains(sourcecodeFileNode));

//        Environment.getInstance().getActiveSourcecodeTabs().put(scTab, sourcecodeFileNode);

        cteTab.setOnClosed(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                cteTabNames.remove(name);
                Environment.getInstance().getActiveSourcecodeTabs().remove(event.getSource());
            }
        });

        cteController.setUpPos(cteView);
    }

    public void removeAndCreateNewCoverageTab(String tabName, Tab tab) {
        // add coverage tabpane and open it automatically
        if (!mdiTabPane.getTabs().contains(mdiTabCoverage)) {
            mdiTabPane.getTabs().add(mdiTabCoverage);
        }
        mdiTabPane.getSelectionModel().select(mdiTabCoverage);

        // remove the current coverage tab opening this test case
        int index = coverageViews.getTabs().size();
        if (coverageViewsTabNames.contains(tabName)) {
            Tab tmpTab = getCoverageViewTabByName(tabName);
            if (tmpTab != null) {
                index = coverageViews.getTabs().indexOf(tmpTab);
                coverageViews.getTabs().remove(index);
                coverageViewsTabNames.remove(tabName);
            }
        }

        if (tab != null) {
            // add new coverage tab of this test case
            coverageViews.getTabs().add(index, tab);
            coverageViews.getSelectionModel().select(tab);
            coverageViewsTabNames.add(tab.getText());
            // event when closing the function coverages tab
            tab.setOnClosed(event -> coverageViewsTabNames.remove(tab.getText()));
        }
    }

    public void viewCoverageOfMultipleTestcase(String tabName, List<TestCase> testCases) {
        if (testCases.size() > 0) {
            ViewCoverageTask task = new ViewCoverageTask(testCases);
            task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    LoadingPopupController.getInstance().close();

                    List<CoverageDataObject> srcCovDatas = new ArrayList<>();
                    List<CoverageDataObject> funcCovDatas = new ArrayList<>();
                    if (task.getValue().size() == 2) {
                        srcCovDatas.add(task.getValue().get(0));
                        funcCovDatas.add(task.getValue().get(1));
                    } else if (task.getValue().size() == 4) {
                        srcCovDatas.add(task.getValue().get(0));
                        srcCovDatas.add(task.getValue().get(1));
                        funcCovDatas.add(task.getValue().get(2));
                        funcCovDatas.add(task.getValue().get(3));
                    }

                    ICommonFunctionNode sut =  testCases.get(0).getFunctionNode();
                    int line = -1;
                    if (sut instanceof AbstractFunctionNode)
                        line = ((AbstractFunctionNode) sut).getAST().getFileLocation().getStartingLineNumber();
                    Tab tab = Factory.generateCoverageTabView(tabName, line, srcCovDatas, funcCovDatas);

                    if (tab != null)
                        removeAndCreateNewCoverageTab(tabName, tab);
                    else {
                        UIController.showErrorDialog("Can not open coverage of multiple test cases", "Coverage tab", "Can not open");
                    }
                }
            });
            new AkaThread(task).start();
//            Tab tab = Factory.generateCoverageTab(tabName, testCases);
//            if (tab != null)
//                removeAndCreateNewCoverageTab(tabName, tab);
//            else {
//                UIController.showErrorDialog("Can not open coverage of multiple test cases", "Coverage tab", "Can not open");
//            }
        }
    }

    public void viewReport(ITestCase testCase) {
        String name = testCase.getName();
        if (!mdiTabPane.getTabs().contains(mdiTabReports)) {
            mdiTabPane.getTabs().add(mdiTabReports);
        }

        mdiTabPane.getSelectionModel().select(mdiTabReports);
        // index to display tab of the function
        int index = reports.getTabs().size();
        if (reportsTabNames.contains(name)) {
            Tab tab = getReportsTabByName(name);
            if (tab != null) {
                index = reports.getTabs().indexOf(tab);
                reports.getTabs().remove(index);
                reportsTabNames.remove(name);
            }
        }

        Tab tab = Factory.generateReportTab(name, testCase);
        if (tab != null) {
            reports.getTabs().add(index, tab);
            reports.getSelectionModel().select(tab);
            reportsTabNames.add(tab.getText());

            // event when closing the function coverages tab
            tab.setOnClosed(event -> reportsTabNames.remove(tab.getText()));
        }
    }

    public void viewReport(String name, String content) {
        if (!mdiTabPane.getTabs().contains(mdiTabReports)) {
            mdiTabPane.getTabs().add(mdiTabReports);
        }

        mdiTabPane.getSelectionModel().select(mdiTabReports);
        // index to display tab of the function
        int index = reports.getTabs().size();
        if (reportsTabNames.contains(name)) {
            Tab tab = getReportsTabByName(name);
            if (tab != null) {
                index = reports.getTabs().indexOf(tab);
                reports.getTabs().remove(index);
                reportsTabNames.remove(name);
            }
        }

        // Add a new test case report tab
        Tab tab = Factory.generateReportTab(name, content);
        reports.getTabs().add(index, tab);
        reports.getSelectionModel().select(tab);
        reportsTabNames.add(tab.getText());

        // event when closing the function coverages tab
        tab.setOnClosed(event -> reportsTabNames.remove(tab.getText()));
    }

    public void viewReport(String name, String content, String cssLink) {
        if (!mdiTabPane.getTabs().contains(mdiTabReports)) {
            mdiTabPane.getTabs().add(mdiTabReports);
        }

        mdiTabPane.getSelectionModel().select(mdiTabReports);
        // index to display tab of the function
        int index = reports.getTabs().size();
        if (reportsTabNames.contains(name)) {
            Tab tab = getReportsTabByName(name);
            if (tab != null) {
                index = reports.getTabs().indexOf(tab);
                reports.getTabs().remove(index);
                reportsTabNames.remove(name);
            }
        }

        // Add a new test case report tab
        Tab tab = Factory.generateReportTab(name, content, cssLink);
        reports.getTabs().add(index, tab);
        reports.getSelectionModel().select(tab);
        reportsTabNames.add(tab.getText());

        // event when closing the function coverages tab
        tab.setOnClosed(event -> reportsTabNames.remove(tab.getText()));
    }

    public boolean checkDebugOpen() {
        return mdiTabPane.getTabs().contains(mdiTabDebug);
    }

    public void viewDebug(ITestCase testCase) {
        if (!mdiTabPane.getTabs().contains(mdiTabDebug)) {
            mdiTabDebug.setContent(DebugController.getDebugPane());
            boolean isExecutable = DebugController.getDebugController().loadAndExecuteTestCase(testCase);
            if (isExecutable) {
                DebugController.getDebugController().startGDB(testCase);
                mdiTabPane.getTabs().add(mdiTabDebug);
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setGraphic(null);
            alert.setTitle("Notification Dialog");
            alert.setContentText("Execute testcase " + testCase.getName() + " in debug mode?");

            ButtonType confirmBtn = new ButtonType("Yes");
            ButtonType buttonTypeCancel = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(confirmBtn, buttonTypeCancel);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == confirmBtn) {
                DebugController.getDebugController().turnOff();
                mdiTabDebug.setContent(DebugController.getDebugPane());
                boolean isExecutable = DebugController.getDebugController().loadAndExecuteTestCase(testCase);
                if (isExecutable) {
                    DebugController.getDebugController().startGDB(testCase);
                    if (DebugController.getDebugController().getCurrentTestCase() != testCase) {
                        mdiTabPane.getTabs().add(mdiTabDebug);
                    }
                }
            }
        }

        mdiTabDebug.setOnClosed(e -> DebugController.getDebugController().turnOff());
        mdiTabPane.getSelectionModel().select(mdiTabDebug);
    }


    public void removeTestCaseTab(String name) {
        Tab tab = getTestCasesTabByName(name);

        if (tab != null)
            testCaseViews.getTabs().remove(tab);

        if (name != null)
            testCasesTabNames.remove(name);
    }

    public void removePrototypeTab(String name) {
        Tab tab = getPrototypeTabByName(name);
        prototypeViews.getTabs().remove(tab);
        prototypeTabNames.remove(name);
    }

    public void removeCompoundTestCaseTab(String name) {
        Tab tab = getCompoundTestcaseTabByName(name);
        compounds.getTabs().remove(tab);
        compoundTestcaseTabNames.remove(name);
        compoundTestCaseControllerMap.remove(name);
    }

    public void clear() {
        mdiTabPane.getTabs().clear();

        testCaseViews.getTabs().clear();
        sourceCodeViews.getTabs().clear();
        compounds.getTabs().clear();
        coverageViews.getTabs().clear();
        reports.getTabs().clear();
        prototypeViews.getTabs().clear();
        probePointSourceCodeViews.getTabs().clear();
        cteView.getTabs().clear();

        compoundTestcaseTabNames.clear();
        testCasesTabNames.clear();
        sourceCodeTabNames.clear();
        coverageViewsTabNames.clear();
        reportsTabNames.clear();
        prototypeTabNames.clear();
        probePointSourceCodeTabNames.clear();
        cteTabNames.clear();

        DebugController.getDebugController().clear();
    }

    public void viewReleaseNotes(ReleaseNotes releaseNotes) {
        if (!mdiTabPane.getTabs().contains(mdiTabAboutUs)) {
            mdiTabPane.getTabs().add(mdiTabAboutUs);
        }

        mdiTabPane.getSelectionModel().select(mdiTabAboutUs);

        final WebView reportView = new WebView();
        final WebEngine webEngine = reportView.getEngine();
        webEngine.loadContent(releaseNotes.toHtml());
        webEngine.setUserStyleSheetLocation(Object.class.getResource("/css/report_style.css").toString());
        webEngine.documentProperty().addListener(new ChangeListener<Document>() {
            @Override
            public void changed(ObservableValue<? extends Document> observable, Document oldValue, Document newValue) {
                if (newValue != null) {
                    NodeList nodeList = newValue.getElementsByTagName("a");
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        org.w3c.dom.Node node = nodeList.item(i);
                        EventTarget eventTarget = (EventTarget) node;
                        eventTarget.addEventListener("click", new EventListener() {
                            @Override
                            public void handleEvent(org.w3c.dom.events.Event evt) {
                                EventTarget target = evt.getCurrentTarget();
                                HTMLAnchorElement anchorElement = (HTMLAnchorElement) target;
                                String href = anchorElement.getHref();
                                if (href.startsWith("http")) {
                                    logger.debug("Open " + href);
                                    Desktop d = Desktop.getDesktop();
                                    try {
                                        URI address = new URI(href);
                                        d.browse(address);
                                    } catch (URISyntaxException | IOException e) {
                                        logger.error("Cant open " + href + ": " + e.getMessage());
                                    }
                                } else if (href.equals("#newer")) {
                                    logger.debug("Newer release notes");
                                    Workbook workbook = releaseNotes.getWorkbook();
                                    int index = releaseNotes.getIndex();
                                    new AkaThread(new ViewReleaseNotesTask(workbook, index - 1)).start();
                                } else if (href.equals("#prev")) {
                                    logger.debug("Prev release notes");
                                    Workbook workbook = releaseNotes.getWorkbook();
                                    int index = releaseNotes.getIndex();
                                    new AkaThread(new ViewReleaseNotesTask(workbook, index + 1)).start();
                                }

                                evt.preventDefault();
                            }
                        }, false);
                    }
                }
            }
        });

        mdiTabAboutUs.setContent(reportView);
    }
}
