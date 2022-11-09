package com.dse.guifx_v3.controllers.main_view;

import com.dse.guifx_v3.about_us.ViewHelp;
import com.dse.guifx_v3.controllers.LicenseController;
import com.dse.guifx_v3.objects.hint.Hint;
import com.dse.guifx_v3.objects.hint.HintContent;
import com.dse.testcase_manager.minimize.*;
import com.dse.guifx_v3.controllers.FunctionConfigurationController;
import com.dse.thread.AkaThread;
import com.dse.project_init.LcovProjectClone;
import com.dse.thread.task.*;
import com.dse.thread.task.testcase_selection.RetrieveTestCaseTask;
import com.dse.thread.task.testcase_selection.RetrieveUnitNodeTask;
import com.dse.thread.task.view_report.ViewManagementReportTask;
import com.dse.guifx_v3.controllers.object.LoadingPopupController;
import com.dse.testcase_execution.ITestcaseExecution;
import com.dse.boundary.ExistedBoundaryController;
import com.dse.config.AkaConfig;
import com.dse.config.WorkspaceConfig;
import com.dse.environment.EnvironmentAnalyzer;
import com.dse.environment.EnvironmentSearch;
import com.dse.environment.WorkspaceCreation;
import com.dse.environment.object.*;
import com.dse.guifx_v3.controllers.TestCasesNavigatorController;
import com.dse.guifx_v3.controllers.build_environment.AbstractCustomController;
import com.dse.guifx_v3.controllers.build_environment.BaseController;
import com.dse.environment.Environment;
import com.dse.guifx_v3.helps.Factory;
import com.dse.guifx_v3.helps.UIController;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.INode;
import com.dse.parser.object.IProjectNode;
import com.dse.parser.object.ProjectNode;
import com.dse.parser.systemlibrary.SystemHeaderParser;
import com.dse.probe_point_manager.ProbePointManager;
import com.dse.project_init.ProjectClone;
import com.dse.regression.controllers.AvailableRegressionScriptsController;
import com.dse.report.*;
import com.dse.search.Search;
import com.dse.search.condition.SourcecodeFileNodeCondition;
import com.dse.testcase_execution.TestCaseExecutionThread;
import com.dse.testcase_manager.CompoundTestCase;
import com.dse.testcase_manager.ITestCase;
import com.dse.testcase_manager.TestCase;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.testcasescript.SelectionUpdater;
import com.dse.testcasescript.object.*;
import com.dse.thread.AbstractAkaTask;
import com.dse.thread.AkaThreadManager;
import com.dse.thread.task.view_report.ViewReleaseNotesTask;
import com.dse.user_code.controllers.UserCodeManagerController;
import com.dse.logger.AkaLogger;
import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class MenuBarController implements Initializable {
    final static AkaLogger logger = AkaLogger.get(MenuBarController.class);

    @FXML
    private CustomMenuItem cmiSetZ3Solver;
    @FXML
    private CustomMenuItem cminewCCPPEnvironment;
    @FXML
    private Menu miRecentEnvironments;
    @FXML
    private CustomMenuItem cmiExit;
    private BackgroundTaskObjectController rebuildEnvironmentBGController;
    @FXML
    private CustomMenuItem cmiOpenEnvironment;
    @FXML
    private Menu mNew;
    @FXML
    private Menu mFile;

    /**
     * Singleton pattern like
     */
    private static MenuBar menuBar = null;
    private static MenuBarController menuBarController = null;

    private static void prepare() {
        FXMLLoader loader = new FXMLLoader(Object.class.getResource("/FXML/MenuBar.fxml"));
        try {
            Parent parent = loader.load();
            menuBar = (MenuBar) parent;
            menuBarController = loader.getController();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static MenuBar getMenuBar() {
        if (menuBar == null) {
            prepare();
        }
        return menuBar;
    }

    public static MenuBarController getMenuBarController() {
        if (menuBarController == null) {
            prepare();
        }
        return menuBarController;
    }

    private void getLicense() {
        LicenseController licenseController = LicenseController.getInstance();
        licenseController.getLicense();
        if (licenseController.isValidLicense()
                && !licenseController.isExpiredLicense()) {
        } else {
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // set icon
        mNew.setGraphic(new ImageView(new Image(Factory.class.getResourceAsStream("/icons/file/newEnvironment.png"))));
        miRecentEnvironments.setGraphic(new ImageView(new Image(Factory.class.getResourceAsStream("/icons/file/recentEnvironments.png"))));

        // load existing environments
        mFile.setOnShowing(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                refreshRecentEnvironments();
            }
        });

        //Wednesday, April 14th, 2021
        Hint.tooltipNode(cminewCCPPEnvironment.getContent(), HintContent.MenuBar.NEW_C_CPP_ENV_HINT);
        Hint.tooltipNode(cmiOpenEnvironment.getContent(), HintContent.MenuBar.OPEN_ENV_HINT);
        Hint.tooltipNode(cmiSetZ3Solver.getContent(), HintContent.MenuBar.SET_Z3_HINT);
        Hint.tooltipNode(cmiExit.getContent(), HintContent.MenuBar.QUIT_HINT);
    }

    private void refreshRecentEnvironments() {
        miRecentEnvironments.getItems().removeAll(miRecentEnvironments.getItems());

        List<String> recentEnvironment = new AkaConfig().fromJson().getRecentEnvironments();
        for (String recentEnv : recentEnvironment)
            if (new File(recentEnv).exists()) {
                MenuItem miRecentEnv = new MenuItem();
                miRecentEnv.setMnemonicParsing(false);
                miRecentEnv.setText(recentEnv);

                // check exist
                boolean exist = false;
                for (MenuItem menuItem : miRecentEnvironments.getItems()) {
                    if (menuItem.getText().equals(recentEnv))
                        exist = true;
                }
                if (!exist) {
                    miRecentEnvironments.getItems().add(0, miRecentEnv);
                    // event when opening a recent environment
                    miRecentEnv.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            File file = new File(recentEnv);
                            if (isOpeningWorkspace(file)) {
                                if (allowOpeningWorkspace(file)) {
                                    openEnvironmentFromEnvFile(new File(recentEnv));
                                }
                            } else {
                                openEnvironmentFromEnvFile(new File(recentEnv));
                            }
                        }
                    });
                }
            }
    }

    @FXML
    public void openUserManual(ActionEvent event) {
        try {
            ViewHelp viewHelp = new ViewHelp();
            if (Utils.isWindows()) {
                viewHelp.showUserManualInWindowOS();
            } else {
                viewHelp.showUserManualDirectoryInUnixOS();
            }
            logger.debug("Open User Manual Successfully");
        } catch (Exception e) {
            e.printStackTrace();
            String err = "Error when open User Manual: " + e.getMessage();
            logger.error(err, e);
            UIController.showErrorDialog(err, "User Manual", "Something wrong");
        }
    }

    @FXML
    public void exit(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.NONE, "Do you want to close Akautauto Tool?", ButtonType.YES, ButtonType.NO);
        alert.initOwner(UIController.getPrimaryStage());
        alert.showAndWait();

        if (alert.getResult() == ButtonType.YES) {
            UIController.shutdown();
        }
    }

    @FXML
    public void createNewEnvironment() {
        if (Environment.getInstance().getProjectNode() == null)
            AbstractCustomController.ENVIRONMENT_STATUS = AbstractCustomController.STAGE.CREATING_NEW_ENV_FROM_BLANK_GUI;
        else
            AbstractCustomController.ENVIRONMENT_STATUS = AbstractCustomController.STAGE.CREATING_NEW_ENV_FROM_OPENING_GUI;
        UIController.newCCPPEnvironment();
    }

    @FXML
    public void importTestCases() {
        if (Environment.getInstance().getName().equals("")) {
            String msg = "Must to have an environment to import testcases.";
            UIController.showErrorDialog(msg, "Environment not found", "Fail");
        } else {
            Stage primaryStage = UIController.getPrimaryStage();
            DirectoryChooser workingDirectoryChooser = new DirectoryChooser();
            workingDirectoryChooser.setTitle("Choose test cases directory");
            File file = workingDirectoryChooser.showDialog(primaryStage);

            if (file != null) {
                logger.debug("Import testcases from directory: " + file.getAbsolutePath());
                TestCaseManager.importTestCasesFromDirectory(file);
            }
        }
    }

    @FXML
    public void openEnvironment() {
        // backup current Environment
        Environment.backupEnvironment();

        FileChooser fileChooser = new FileChooser();
        addExtensionFilterWhenOpeningEnvironment(fileChooser);
        File environmentFile = fileChooser.showOpenDialog(UIController.getPrimaryStage());
        if (environmentFile != null && environmentFile.exists()) {
            if (isOpeningWorkspace(environmentFile)) {
                if (allowOpeningWorkspace(environmentFile)) {
                    openEnvironmentFromEnvFile(environmentFile);
                }
            } else {
                openEnvironmentFromEnvFile(environmentFile);
            }
        } else
            Environment.restoreEnvironment();
    }

    private void openEnvironmentFromEnvFile(File environmentFile) {
        setUpWorkingDirectoryAgain(environmentFile);
        // STEP: parse the environment file to construct environment tree
        boolean isAnalyzedSuccessfully = analyzeEnvironmentScript(environmentFile);
        if (!isAnalyzedSuccessfully) {
            UIController.showDetailDialogInMainThread(Alert.AlertType.ERROR, "Error",
                    "Environment file analysis error",
                    "Could not parse environment file " + environmentFile.getAbsolutePath());
            return;
        }

        LoadingPopupController loadingPopup = LoadingPopupController.newInstance("Loading Environment");
        loadingPopup.initOwnerStage(UIController.getPrimaryStage());
        loadingPopup.show();

        EnvironmentLoaderTask task = new EnvironmentLoaderTask(environmentFile);
        // add task to background tasks monitor
        BackgroundTaskObjectController controller = BackgroundTaskObjectController.getNewInstance();
        if (controller != null) {
            rebuildEnvironmentBGController = controller;
            controller.setlTitle("Rebuild Environment");
            controller.setCancelTitle("Stopping Rebuild");
            BackgroundTasksMonitorController.getController().addBackgroundTask(controller);
            // set task to controller to cancel as need when processing
            controller.setTask(task);
            controller.getProgressIndicator().progressProperty().bind(task.progressProperty());
            controller.getProgressBar().progressProperty().bind(task.progressProperty());

            new AkaThread(task).start();
            task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    // remove task if done
                    Platform.runLater(() -> BackgroundTasksMonitorController.getController().removeBackgroundTask(controller));
                    rebuildEnvironmentBGController = null;
                    MenuBarController.getMenuBarController().refresh();

                    if (Environment.PREPROCESSOR_HEADER)
                        Platform.runLater(new SystemHeaderParser.ReloadThread());

                    loadingPopup.close();
                }
            });
            task.setOnCancelled(event -> {
                Environment.restoreEnvironment();
                rebuildEnvironmentBGController = null;
                MenuBarController.getMenuBarController().refresh();
            });
        }
    }

    public BackgroundTaskObjectController getRebuildEnvironmentBGController() {
        return rebuildEnvironmentBGController;
    }

    public void setRebuildEnvironmentBGController(BackgroundTaskObjectController rebuildEnvironmentBGController) {
        this.rebuildEnvironmentBGController = rebuildEnvironmentBGController;
    }

    //    /**
//     * @return true if we can load GUI successfully
//     */
//    private boolean loadGUIWhenOpeningEnv() {
//        UIController.clear();
//
//        loadProjectStructureTree();
//        loadTestCaseNavigator();
//
//        // after load testcase, load probe points
//        loadProbePoints();
//        loadUnitTestableState();
//
//        ProjectClone.cloneEnvironment();
//
//        // update coverage type displaying on bottom,...
//        BaseSceneController.getBaseSceneController().updateInformation();
//
//        UIController.showSuccessDialog("Loading environment is successfully", "Success",
//                "The environment has been loaded");
//        return true;
//    }

    private void loadProbePoints() {
        ProbePointManager.getInstance().clear();
        ProbePointManager.getInstance().loadProbePoints();
        MDIWindowController.getMDIWindowController().updateLVProbePoints();
    }

    /**
     * When we build an environment successfully, or when we open an existing environment,
     * we will add the path of these environments to history.
     *
     * @param environmentFile the environment added to history
     * @return true if the environment file does not exist in the history,
     * false if otherwise
     */
    public static boolean addEnvironmentToHistory(File environmentFile) {
        AkaConfig akaConfig = new AkaConfig().fromJson();
        List<String> recentEnvironments = akaConfig.getRecentEnvironments();
        if (!(recentEnvironments.contains(environmentFile.getAbsolutePath()))) {
            recentEnvironments.add(environmentFile.getAbsolutePath());
            akaConfig.exportToJson();
            return true;
        } else
            return false;
    }

    public void loadUnitTestableState() {
        List<INode> sources = Search.searchNodes(Environment.getInstance().getProjectNode(), new SourcecodeFileNodeCondition());
        ProjectTreeExpandTask.loadUnitTestableState(sources);
//        List<IEnvironmentNode> uuts = EnvironmentSearch.searchNode(Environment.getInstance().getEnvironmentRootNode(), new EnviroUUTNode());
//        for (IEnvironmentNode uut : uuts) {
//            for (INode source : sources) {
//                if (((EnviroUUTNode) uut).getName().equals(source.getAbsolutePath())) {
//                    ((EnviroUUTNode) uut).setUnit(source);
//                    break;
//                }
//            }
//        }
//
//        List<IEnvironmentNode> sbfs = EnvironmentSearch.searchNode(Environment.getInstance().getEnvironmentRootNode(), new EnviroSBFNode());
//        for (IEnvironmentNode sbf : sbfs) {
//            for (INode source : sources) {
//                if (((EnviroSBFNode) sbf).getName().equals(source.getAbsolutePath())) {
//                    ((EnviroSBFNode) sbf).setUnit(source);
//                    break;
//                }
//            }
//        }
//
//        List<IEnvironmentNode> dontStubs = EnvironmentSearch.searchNode(Environment.getInstance().getEnvironmentRootNode(), new EnviroDontStubNode());
//        for (IEnvironmentNode dontStub : dontStubs) {
//            for (INode source : sources) {
//                if (((EnviroDontStubNode) dontStub).getName().equals(source.getAbsolutePath())) {
//                    ((EnviroDontStubNode) dontStub).setUnit(source);
//                    break;
//                }
//            }
//        }
//
//        List<IEnvironmentNode> ignores = EnvironmentSearch.searchNode(Environment.getInstance().getEnvironmentRootNode(), new EnviroIgnoreNode());
//        for (IEnvironmentNode ignore : ignores) {
//            for (INode source : sources) {
//                if (((EnviroDontStubNode) ignore).getName().equals(source.getAbsolutePath())) {
//                    ((EnviroDontStubNode) ignore).setUnit(source);
//                    break;
//                }
//            }
//        }
    }

    private boolean isOpeningWorkspace(File environmentFile) {
        String environmentFilePath = environmentFile.getAbsolutePath();
        String workspace = environmentFilePath.replace(WorkspaceConfig.ENV_EXTENSION, SpecialCharacter.EMPTY);
        Set<String> openingWorkspaces = new AkaConfig().fromJson().getOpeningWorkspaces();
        if (openingWorkspaces.contains(workspace)) {
            return true;
        }
        return false;
    }

    private boolean allowOpeningWorkspace(File environmentFile) {

        String environmentFilePath = environmentFile.getAbsolutePath();
        String workspace = environmentFilePath.replace(WorkspaceConfig.ENV_EXTENSION, SpecialCharacter.EMPTY);
        Set<String> openingWorkspaces = new AkaConfig().fromJson().updateOpeningWorkspaces().getOpeningWorkspaces();
        if (openingWorkspaces.contains(workspace)) {
            Alert confirmAlert = null;
            confirmAlert = UIController.showYesNoDialog(Alert.AlertType.WARNING, "Aka automation tool", "Do you want to continue?", "It looks like another Aka Automation instance is running with this project open.");
//            if (Utils.isUnix()) {
//                UIController.showErrorDialog("It looks like another AkaUTAutomation instance is \nrunning with this project open.", "Aka automation tool", "Unable to open project.");
//            } else {
//                UIController.showErrorDialog("It looks like another AkaUTAutomation instance is running with this project open.", "Aka automation tool", "Unable to open project.");
//            }
            Optional<ButtonType> option = confirmAlert.showAndWait();
            if (option.get() == ButtonType.YES) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    // change the working directory to the directory contains the environment file
    private void setUpWorkingDirectoryAgain(File environmentFile) {
        AkaConfig akaConfig = new AkaConfig().fromJson();

        String environmentFilePath = environmentFile.getAbsolutePath();

        String newWorkingDirectory = environmentFile.getParentFile().getAbsolutePath();
        akaConfig.setWorkingDirectory(newWorkingDirectory);
        logger.debug("Setup the working directory at " + newWorkingDirectory);

        String workspace = environmentFilePath.replace(WorkspaceConfig.ENV_EXTENSION, SpecialCharacter.EMPTY);
        logger.setAppender(workspace+ File.separator+ "aka.log");
        akaConfig.setOpeningWorkspaceDirectory(workspace);
        akaConfig.addOpeningWorkspaces(workspace);
        logger.debug("Setup the workspace directory at " + workspace);

        String workspaceConfig = workspace + File.separator + WorkspaceConfig.WORKSPACE_CONFIG_NAME;
        akaConfig.setOpenWorkspaceConfig(workspaceConfig);
        logger.debug("Setup the workspace config at " + workspaceConfig);

        akaConfig.exportToJson();

        BaseSceneController.getBaseSceneController().updateInformation();
    }

//    /**
//     * Load the project rather than parsing it
//     * @return null if we find compilation errors
//     */
//    private INode loadProject() {
//        ChangesBetweenSourcecodeFiles.reset();
//
//        WorkspaceLoader loader = new WorkspaceLoader();
//
//        String workspacePath = new AkaConfig().fromJson().getOpeningWorkspaceDirectory();
//        loader.setWorkspace(new File(workspacePath));
//
//        String physicalTreePath = new WorkspaceConfig().fromJson().getPhysicalJsonFile();
//        loader.setPhysicalTreePath(new File(physicalTreePath));
//        loader.load(loader.getPhysicalTreePath());
//        INode root = loader.getRoot();
//        return root;
//    }

    public static Alert showDialogWhenComparingSourcecode(Alert.AlertType type, String title, String headText, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(headText);
        alert.setContentText(content);
        ButtonType okButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);
        ButtonType viewChangesButton = new ButtonType("Changes", ButtonBar.ButtonData.HELP);
        ButtonType viewDependenciesButton = new ButtonType("Problems", ButtonBar.ButtonData.HELP);

        alert.getButtonTypes().setAll(okButton, noButton, viewChangesButton, viewDependenciesButton);

        // show changes
        Button tmp = (Button) alert.getDialogPane().lookupButton(viewChangesButton);
        tmp.addEventFilter(
                ActionEvent.ACTION,
                event -> {
                    UIController.showDetailDialog(Alert.AlertType.INFORMATION, "Changes detection",
                            "Changes",
                            Utils.readFileContent(new WorkspaceConfig().fromJson().getFileContainingChangesWhenComparingSourcecode()));
                    event.consume();
                }
        );

        // show unresolved dependencies
        Button tmp2 = (Button) alert.getDialogPane().lookupButton(viewDependenciesButton);
        tmp2.addEventFilter(
                ActionEvent.ACTION,
                event -> {
                    String unresolvedDependenciesContent = Utils.readFileContent(new WorkspaceConfig().fromJson().getFileContainingUnresolvedDependenciesWhenComparingSourcecode());
                    UIController.showDetailDialog(Alert.AlertType.INFORMATION, "Unresolved dependencies detection",
                            "Unresolved dependencies",
                            unresolvedDependenciesContent);
                    event.consume();
                }
        );


        TextArea textArea = new TextArea();
        textArea.setWrapText(true);
        textArea.setEditable(false);
        textArea.setMinHeight(350);
        textArea.setText(content);

        alert.getDialogPane().setContent(textArea);

        return alert;
    }

//    public MenuItem getcmiViewTestCaseData() {
//        return cmiViewTestCaseData;
//    }
//
//    public MenuItem getcmiViewFull() {
//        return cmiViewFull;
//    }
//
//    public MenuItem getcmiViewCoverage() {
//        return cmiViewCoverage;
//    }
//
//    public MenuItem getcmiViewTestCaseManage() {
//        return cmiViewTestCaseManage;
//    }

    @Deprecated
    private void addMenuItemDisableProperty(MenuItem menuItem, Class<?>... activeNodes) {
        if (menuItem == null)
            return;
        try {
            menuItem.setDisable(true);
        } catch (Exception e) {

        }
        TreeTableView<ITestcaseNode> testCasesNavigator = TestCasesNavigatorController
                .getInstance().getTestCasesNavigator();

        testCasesNavigator.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
            menuItem.disableProperty().bind(new BooleanBinding() {
                @Override
                protected boolean computeValue() {
                    try {
                        ObservableList<TreeItem<ITestcaseNode>> list = testCasesNavigator
                                .getSelectionModel().getSelectedItems();

                        for (TreeItem<ITestcaseNode> selectedItem : list) {
                            boolean disable = true;

                            if (activeNodes != null)
                                for (Class<?> node : activeNodes)
                                    if (selectedItem != null && selectedItem.getValue() != null) {
                                        if (node.isInstance(selectedItem.getValue())) {
                                            disable = false;
                                            break;
                                        }
                                    }

                            if (disable)
                                return true;
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return false;
                }
            });
        });
    }

    // trung lap voi file NameEnvironmentController
    public void loadProjectStructureTree() {
        if (Environment.getInstance().getProjectNode() != null) {
            IProjectNode root = Environment.getInstance().getProjectNode();

            EnvironmentRootNode environmentRootNode = Environment.getInstance().getEnvironmentRootNode();
            List<IEnvironmentNode> nameNodes = EnvironmentSearch.searchNode(environmentRootNode, new EnviroNameNode());
            if (nameNodes.size() == 1) {
                root.setName(((EnviroNameNode) nameNodes.get(0)).getName());
                UIController.loadProjectStructureTree(root);
            } else {
                logger.error("There are more than one name in the enviroment script. Can not export!");
            }
        }
    }

    /**
     * Only accept .env files when opening a dialog for choosing environment file
     *
     * @param fileChooser
     */
    private void addExtensionFilterWhenOpeningEnvironment(FileChooser fileChooser) {
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("ENV files (*.env)", "*.env");
        fileChooser.getExtensionFilters().add(extFilter);
    }

    /**
     * @param environmentFile
     * @return false if parsing environment file .env causses errors
     */
    private boolean analyzeEnvironmentScript(File environmentFile) {
        logger.debug("Analyzing the environment script " + environmentFile.getName());

        EnvironmentAnalyzer analyzer = new EnvironmentAnalyzer();
        analyzer.analyze(environmentFile);
        IEnvironmentNode root = analyzer.getRoot();

        if (root instanceof EnvironmentRootNode) {
            Environment.getInstance().setEnvironmentRootNode((EnvironmentRootNode) root);
            return true;
        }

        return false;
    }

//    @FXML
//    public void setWorkingDirectory(ActionEvent actionEvent) {
//        Stage primaryStage = UIController.getPrimaryStage();
//        DirectoryChooser workingDirectoryChooser = new DirectoryChooser();
//        workingDirectoryChooser.setTitle("Choose working directory");
//        File file = workingDirectoryChooser.showDialog(primaryStage);
//
//        if (file != null) {
//            if (file.isDirectory()) {
//                new AkaConfig().fromJson().setWorkingDirectory(file.getAbsolutePath()).exportToJson();
//                BaseSceneController.getBaseSceneController().setupWorkingDirectory();
//                logger.debug("The current working directory: " + file.getAbsolutePath());
//
//                showValidWorkingDirectoryStatus();
//                getcminewCCPPEnvironment().setDisable(false);
//            } else {
//                // never happen
//            }
//        }
//    }

//    private void showValidWorkingDirectoryStatus() {
//        Alert alert = new Alert(Alert.AlertType.INFORMATION);
//        alert.setTitle("Set the working directory");
//        alert.setHeaderText("Success");
//        alert.setContentText("The working directory has been set up");
//        alert.showAndWait();
//    }

    private void showValidZ3SolverStatus() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Set the z3 solver");
        alert.setHeaderText("Success");
        alert.setContentText("The z3 solver has been set up");
        alert.showAndWait();
    }

    private List<ITestCase> getAllSelectedTestCases(boolean containData) {
        List<ITestCase> testCases = new ArrayList<>();

        TestcaseRootNode rootNode = Environment.getInstance().getTestcaseScriptRootNode();

        if (Environment.getInstance().isCoverageModeActive()) {
            List<ITestcaseNode> selectedTestcases = SelectionUpdater.getAllSelectedTestcases(rootNode);

            for (ITestcaseNode node : selectedTestcases) {
                if (node instanceof TestNameNode) {
                    try {
                        String testCaseName = ((TestNameNode) node).getName();
                        ITestCase testCase;
                        if (containData)
                            testCase = TestCaseManager.getTestCaseByName(testCaseName);
                        else
                            testCase = TestCaseManager.getTestCaseByNameWithoutData(testCaseName);
                        if (testCase != null)
                            testCases.add(testCase);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }

        return testCases;
    }

    @FXML
    public void viewTestCaseData(ActionEvent actionEvent) {
        if (Environment.getInstance().getProjectNode() == null) {
            UIController.showErrorDialog("You must open an environment firstly", "Akautauto Error", "There is no opening environment");
            return;
        }

        LoadingPopupController loadingPopup = LoadingPopupController.newInstance("Retrieve selected test cases");
        loadingPopup.initOwnerStage(UIController.getPrimaryStage());
        loadingPopup.show();

        RetrieveTestCaseTask retrieveTestCaseTask = new RetrieveTestCaseTask();
        retrieveTestCaseTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                loadingPopup.close();
                List<ITestCase> testCases = retrieveTestCaseTask.get();

                if (!testCases.isEmpty()) {
                    // Generate test case data report
                    IReport report = new TestCaseDataReport(testCases, LocalDateTime.now());
                    ReportManager.export(report);

                    // display on MDIWindow
                    MDIWindowController.getMDIWindowController().viewReport(report.getName(), report.toHtml());
                } else {
                    UIController.showErrorDialog("You need to select at least one test case to execute", "Test Case Report", "There is no selected test cases");
                }
            }
        });
        new AkaThread(retrieveTestCaseTask).start();
    }

    @FXML
    public void viewFullReport(ActionEvent actionEvent) {
        if (Environment.getInstance().getProjectNode() == null) {
            UIController.showErrorDialog("You must open an environment firstly", "Akautauto Error", "There is no opening environment");
            return;
        }

        LoadingPopupController loadingPopup = LoadingPopupController.newInstance("Retrieve selected test cases");
        loadingPopup.initOwnerStage(UIController.getPrimaryStage());
        loadingPopup.show();

        RetrieveTestCaseTask retrieveTestCaseTask = new RetrieveTestCaseTask();
        retrieveTestCaseTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                loadingPopup.close();
                List<ITestCase> testCases = retrieveTestCaseTask.get();

                if (testCases.isEmpty()) {
                    UIController.showErrorDialog("You need to select at least one test case to execute", "Test Case Report", "There is no selected test cases");
                } else {
                    for (ITestCase testCase : testCases) {
                        Platform.runLater(() -> {
                            // Generate test case data report
                            IReport report = new FullReport(testCase, LocalDateTime.now());
                            ReportManager.export(report);

                            // display on MDIWindow
                            MDIWindowController.getMDIWindowController().viewReport(report.getName(), report.toHtml());
                        });
                    }
                }
            }
        });
        new AkaThread(retrieveTestCaseTask).start();
    }

    @FXML
    public void viewTestCaseManage(ActionEvent actionEvent) {
        if (Environment.getInstance().getProjectNode() == null) {
            UIController.showErrorDialog("You must open an environment firstly", "Akautauto Error", "There is no opening environment");
            return;
        }

        RetrieveUnitNodeTask retrieveUnitsTask = new RetrieveUnitNodeTask();
        retrieveUnitsTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                List<ITestcaseNode> nodes = retrieveUnitsTask.get();
                if (!nodes.isEmpty()) {
                    ViewManagementReportTask viewTask = new ViewManagementReportTask(nodes);
                    new AkaThread(viewTask).start();
                }
            }
        });

        retrieveUnitsTask.start();
    }

    @FXML
    @Deprecated
    public void viewCoverage(ActionEvent actionEvent) {
        if (Environment.getInstance().getProjectNode() == null) {
            UIController.showErrorDialog("You must open an environment firstly", "Akautauto Error", "There is no opening environment");
            return;
        }

        LoadingPopupController loadingPopup = LoadingPopupController.newInstance("Retrieve selected test cases");
        loadingPopup.initOwnerStage(UIController.getPrimaryStage());
        loadingPopup.show();

        RetrieveTestCaseTask retrieveTestCaseTask = new RetrieveTestCaseTask();
        retrieveTestCaseTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                loadingPopup.close();
                List<ITestCase> testCases = retrieveTestCaseTask.get();

                testCases.removeIf(tc -> tc instanceof CompoundTestCase
                        || !tc.getStatus().equals(TestCase.STATUS_SUCCESS));

                if (!testCases.isEmpty()) {
                    if (testCases.size() == 1)
                        UIController.viewCoverageOfATestcase((TestCase) testCases.get(0));
                    else {
                        Map<INode, List<TestCase>> map = new HashMap<>();

                        for (ITestCase testCase : testCases) {
                            ICommonFunctionNode function = ((TestCase) testCase).getFunctionNode();
                            INode unit = Utils.getSourcecodeFile(function);
                            List<TestCase> testCasesOfUnit = map.get(unit);

                            if (testCasesOfUnit == null)
                                testCasesOfUnit = new ArrayList<>();

                            if (!testCasesOfUnit.contains(testCase))
                                testCasesOfUnit.add((TestCase) testCase);

                            map.put(function, testCasesOfUnit);
                        }

                        for (INode unit : map.keySet())
                            UIController.viewCoverageOfMultipleTestcases(unit.getName(), map.get(unit));
                    }
                }
            }
        });

        new AkaThread(retrieveTestCaseTask).start();
    }

//    public MenuItem getcminewCCPPEnvironment() {
//        return cminewCCPPEnvironment;
//    }
//
//    public void rebuildEnvironment() {
//        // STEP: load project
//        boolean findCompilationError = loadProject() == null;
//        if (findCompilationError) {
//            String compilationError = Utils.readFileContent(new WorkspaceConfig().fromJson().getCompilationMessageWhenComplingProject());
//            UIController.showDetailDialogInMainThread(Alert.AlertType.ERROR, "Compilation error",
//                    "Found compilation error. The environment does not change!",
//                    compilationError);
//            return;
//        }
//
//        // STEP: check whether we need to
//        if (ChangesBetweenSourcecodeFiles.modifiedSourcecodeFiles.size() == 0) {
//            Platform.runLater(new Runnable() {
//                @Override
//                public void run() {
//                    loadGUI();
//                }
//            });
////            loadGUI();
//            return;
//        }
//
//        // STEP: show a dialog to inform changes
//        Alert alert = showDialogWhenComparingSourcecode(Alert.AlertType.WARNING, "Warning",
//                "Found changes in the testing project!\n" +
//                        "Do you still want to load the environment?",
//                ChangesBetweenSourcecodeFiles.getModifiedSourcecodeFilesInString());
//        alert.showAndWait().ifPresent(type -> {
//            if (type.getText().toLowerCase().equals("no")) {
//                UIController.showInformationDialog("You stopped loading the environment file due to changes in source code files"
//                        , "Information", "Stop loading environment");
//
//            } else if (type.getText().toLowerCase().equals("yes")) {
//                new WorkspaceUpdater().update();
//                Platform.runLater(this::loadGUI);
////            loadGUI();
//            }
//        });
//    }

    @FXML
    public void updateEnvironment() {
        if (Environment.getInstance().getProjectNode() == null) {
            UIController.showErrorDialog("You must open an environment firstly", "Akautauto Error", "There is no opening environment");
            return;
        }
        AbstractCustomController.ENVIRONMENT_STATUS = AbstractCustomController.STAGE.UPDATING_ENV_FROM_OPENING_ENV;
        UIController.updateCCPPEnvironment();
        BaseController.loadEnvironment();
    }

//    public void setcminewCCPPEnvironment(CustomMenuItem cminewCCPPEnvironment) {
//        this.cminewCCPPEnvironment = cminewCCPPEnvironment;
//    }
//
//    public void setcmiExit(CustomMenuItem cmiExit) {
//        this.cmiExit = cmiExit;
//    }
//
//    public MenuItem getcmiExit() {
//        return cmiExit;
//    }
//
//    public void setcmiOpenProject(CustomMenuItem cmiOpenProject) {
//        this.cmiOpenProject = cmiOpenProject;
//    }
//
//    public MenuItem getcmiOpenProject() {
//        return cmiOpenProject;
//    }
//
//    public Menu getMiRecentProject() {
//        return miRecentEnvironments;
//    }
//
//    public void setMiRecentProject(Menu miRecentProject) {
//        this.miRecentEnvironments = miRecentProject;
//    }

    @FXML
    public void miCleanAtFunctionLevel(ActionEvent actionEvent) {
        if (Environment.getInstance().getProjectNode() == null) {
            UIController.showErrorDialog("You must open an environment firstly", "Akautauto Error", "There is no opening environment");
            return;
        }

        LoadingPopupController loadingPopup = LoadingPopupController.newInstance("Retrieve selected test cases");
        loadingPopup.initOwnerStage(UIController.getPrimaryStage());
        loadingPopup.show();

        RetrieveTestCaseTask retrieveTestCaseTask = new RetrieveTestCaseTask();
        retrieveTestCaseTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                loadingPopup.close();

                List<ITestCase> testCases = retrieveTestCaseTask.get();

                ITestcaseNode root = Environment.getInstance().getTestcaseScriptRootNode();
                int numSelectedFunction = SelectionUpdater.getAllSelectedFunctions(root).size();

                Alert confirmAlert = UIController.showYesNoDialog(Alert.AlertType.CONFIRMATION, "Clean Test Cases",
                        "Remove all unnecessary test cases",
                        "All test cases which don't increase accumulated coverage will be removed. Are you sure want to continue?\n\n" +
                                "Number of selected test cases: " + testCases.size() + "\nNumber of selected functions: " + numSelectedFunction);

                Optional<ButtonType> option = confirmAlert.showAndWait();

                if (option.get() == ButtonType.YES) {
                    LoadingPopupController loadingPopup = LoadingPopupController.newInstance("Optimizing test cases");
                    loadingPopup.initOwnerStage(UIController.getPrimaryStage());
                    loadingPopup.show();

                    AbstractAkaTask<Void> cleanTask = new AbstractAkaTask<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            List<TestCase> testCaseList = testCases.stream()
                                    .filter(tc -> tc instanceof TestCase)
                                    .map(tc -> (TestCase) tc)
                                    .collect(Collectors.toList());
                            ITestCaseMinimizer minimizer = new GreedyMinimizer();
                            minimizer.clean(testCaseList, Scope.FUNCTION);
                            return null;
                        }
                    };

                    cleanTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                        @Override
                        public void handle(WorkerStateEvent event) {
                            loadingPopup.close();
                            UIController.showSuccessDialog("Remove redundant test cases at source code file level successfully", "Optimize number of test cases", "Success");
                        }
                    });

                    new AkaThread(cleanTask).start();

                } else
                    confirmAlert.close();

            }
        });
        new AkaThread(retrieveTestCaseTask).start();
    }

    public void cleanTestCasesAtSourcecodeFileLevel(ActionEvent actionEvent) {
        if (Environment.getInstance().getProjectNode() == null) {
            UIController.showErrorDialog("You must open an environment firstly", "Akautauto Error", "There is no opening environment");
            return;
        }

        LoadingPopupController loadingPopup = LoadingPopupController.newInstance("Retrieve selected test cases");
        loadingPopup.initOwnerStage(UIController.getPrimaryStage());
        loadingPopup.show();

        RetrieveTestCaseTask retrieveTestCaseTask = new RetrieveTestCaseTask();
        retrieveTestCaseTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                loadingPopup.close();

                List<ITestCase> testCases = retrieveTestCaseTask.get();

                ITestcaseNode root = Environment.getInstance().getTestcaseScriptRootNode();
                int numSelectedFunction = SelectionUpdater.getAllSelectedFunctions(root).size();

                Alert confirmAlert = UIController.showYesNoDialog(Alert.AlertType.CONFIRMATION, "Clean Test Cases",
                        "Remove all unnecessary test cases",
                        "All test cases which don't increase accumulated coverage will be removed. Are you sure want to continue?\n\n" +
                                "Number of selected test cases: " + testCases.size() + "\nNumber of selected functions: " + numSelectedFunction);

                Optional<ButtonType> option = confirmAlert.showAndWait();

                if (option.get() == ButtonType.YES) {
                    LoadingPopupController loadingPopup = LoadingPopupController.newInstance("Optimizing test cases");
                    loadingPopup.initOwnerStage(UIController.getPrimaryStage());
                    loadingPopup.show();

                    AbstractAkaTask<Void> cleanTask = new AbstractAkaTask<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            List<TestCase> testCaseList = testCases.stream()
                                    .filter(tc -> tc instanceof TestCase)
                                    .map(tc -> (TestCase) tc)
                                    .collect(Collectors.toList());
                            ITestCaseMinimizer minimizer = new GreedyMinimizer();
                            minimizer.clean(testCaseList, Scope.SOURCE);
                            return null;
                        }
                    };

                    cleanTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                        @Override
                        public void handle(WorkerStateEvent event) {
                            loadingPopup.close();
                            UIController.showSuccessDialog("Remove redundant test cases at source code file level successfully", "Optimize number of test cases", "Success");
                        }
                    });

                    new AkaThread(cleanTask).start();

                } else
                    confirmAlert.close();

            }
        });
        new AkaThread(retrieveTestCaseTask).start();
    }

    @FXML
    public void runAllSelectedTestCasesWithReport(ActionEvent actionEvent) {
        if (Environment.getInstance().getProjectNode() == null) {
            UIController.showErrorDialog("You must open an environment firstly", "Akautauto Error", "There is no opening environment");
            return;
        }

        runAllSelectedTestCases(true);
    }

    @FXML
    public void runAllSelectedTestCasesWithoutReport(ActionEvent actionEvent) {
        if (Environment.getInstance().getProjectNode() == null) {
            UIController.showErrorDialog("You must open an environment firstly", "Akautauto Error", "There is no opening environment");
            return;
        }

        runAllSelectedTestCases(false);
    }

    public void runAllSelectedTestCases(boolean showReport) {
        if (Environment.getInstance().getProjectNode() == null) {
            UIController.showErrorDialog("You must open an environment firstly", "Akautauto Error", "There is no opening environment");
            return;
        }
        // get all test cases
        List<ITestCase> testCases = getAllSelectedTestCases(true);

        if (testCases.isEmpty()) {
            UIController.showErrorDialog("You need to select at least one test case to execute", "Executing Test Case", "There is no selected test cases");
        }

        logger.debug("You are requesting to execute " + testCases.size() + " test cases");

        // put test cases in different threads
        List<TestCaseExecutionThread> tasks = new ArrayList<>();
        for (ITestCase testCase : testCases) {
            testCase.deleteOldDataExceptValue();
            TestCaseExecutionThread executionThread = new TestCaseExecutionThread(testCase);
            executionThread.setExecutionMode(ITestcaseExecution.IN_EXECUTION_WITH_FRAMEWORK_TESTING_MODE);
            executionThread.setShouldShowReport(showReport);
            tasks.add(executionThread);
        }
        logger.debug("Create " + tasks.size() + " threads to execute " + tasks.size() + " test cases");

        // add these threads to executors
        // at the same time, we do not execute all of the requested test cases.
        int MAX_EXECUTING_TESTCASE = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(MAX_EXECUTING_TESTCASE);
        for (TestCaseExecutionThread task : tasks)
            executorService.execute(task);
        executorService.shutdown();
    }

    @FXML
    public void debugTestCase(ActionEvent actionEvent) {
        if (Environment.getInstance().getProjectNode() == null) {
            UIController.showErrorDialog("You must open an environment firstly", "Akautauto Error", "There is no opening environment");
            return;
        }

        // get all test cases
        List<ITestCase> testCases = getAllSelectedTestCases(true);
        testCases.removeIf(tc -> tc instanceof CompoundTestCase);

        if (testCases.isEmpty()) {
            UIController.showErrorDialog("You need to select at least one test case to execute", "Executing Test Case", "There is no selected test cases");
        }

        logger.debug("You are requesting to execute " + testCases.size() + " test cases");

        // put test cases in different threads
        List<Task<ITestCase>> tasks = new ArrayList<>();
        for (ITestCase testCase : testCases) {
            testCase.deleteOldDataExceptValue();
            Task<ITestCase> task = new Task<ITestCase>() {
                @Override
                protected ITestCase call() {
                    Platform.runLater(() -> UIController.executeTestCaseWithDebugMode((TestCase) testCase));
                    return testCase;
                }
            };
            tasks.add(task);
        }
        logger.debug("Create " + tasks.size() + " threads to execute " + tasks.size() + " test cases");

        // add these threads to executors
        // at the same time, we do not execute all of the requested test cases.
        int MAX_EXECUTING_TESTCASE = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(MAX_EXECUTING_TESTCASE);
        for (Task<ITestCase> task : tasks)
            executorService.execute(task);
        executorService.shutdown();
    }

    public void viewEnvironment(ActionEvent actionEvent) {
        if (Environment.getInstance().getProjectNode() == null) {
            UIController.showErrorDialog("You must open an environment firstly", "Akautauto Error", "There is no opening environment");
            return;
        }

        AbstractAkaTask<IReport> task = new AbstractAkaTask<IReport>() {
            @Override
            protected IReport call() throws Exception {
                IReport report = new EnvironmentReport(Environment.getInstance(), LocalDateTime.now());
                ReportManager.export(report);
                return report;
            }
        };

        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                try {
                    IReport report = task.get();
                    // display on MDIWindow
                    MDIWindowController.getMDIWindowController().viewReport(report.getName(), report.toHtml());
                } catch (InterruptedException | ExecutionException e) {
                    UIController.showErrorDialog(e.getMessage(),
                            "Something wrong", "Fail");
                }
            }
        });

        new AkaThread(task).start();
    }

    @FXML
    public void openAutomatedTestdataGenerationConfig(ActionEvent actionEvent) {
        if (Environment.getInstance().getProjectNode() == null) {
            UIController.showErrorDialog("You must open an environment firstly", "Akautauto Error", "There is no opening environment");
            return;
        }

        // will show a dialog to config the default function configuration in automated test data generation module
        // save config in workspace.aka
        FunctionConfigurationController controller = FunctionConfigurationController
                .getInstanceForDefaultConfiguration();
        if (controller != null) {
            controller.showStage();
        }

//        Stage stage = Factory.generateDefaultFunctionConfigStage();
//        if (stage != null) {
//            stage.initOwner(UIController.getPrimaryStage());
//            stage.show();
//        }
    }

    @FXML
    public void openTexteditorConfig(ActionEvent actionEvent) {
        // will show a dialog to config the default function configuration in automated test data generation module
        // save config in workspace.aka
        // config: length of tab, v.v.
    }

    /**
     * Analyze dependency again.
     * <p>
     * This function will overwrite existing dependency files in the folder {working-space}/{env-name}/dependencies
     *
     * @param actionEvent
     */
    public void analyzeDependencies(ActionEvent actionEvent) {
        if (Environment.getInstance().getProjectNode() == null) {
            UIController.showErrorDialog("You must open an environment firstly", "Akautauto Error", "There is no opening environment");
            return;
        }

        AbstractAkaTask<Boolean> task = new AbstractAkaTask<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                try {
                    ProjectNode projectRootNode = Environment.getInstance().getProjectNode();
                    new WorkspaceCreation().exportSourcecodeFileNodeToWorkingDirectory(projectRootNode,
                            new WorkspaceConfig().fromJson().getElementDirectory(),
                            new WorkspaceConfig().fromJson().getDependencyDirectory());
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        };

        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                try {
                    if (task.get()) {
                        UIController.showSuccessDialog("Dependency analyzer successes",
                                "Dependency analyzer", "Success");
                    } else
                        UIController.showErrorDialog("Dependency analyzer caught an unexpected error",
                                "Dependency analyzer", "Fail");
                } catch (InterruptedException | ExecutionException e) {
                    UIController.showErrorDialog("Dependency analyzer caught an unexpected error",
                            "Dependency analyzer", "Fail");
                }
            }
        });

        new AkaThread(task).start();
    }


    @FXML
    public void incrementalBuild() {
        // todo: put to running processes
        if (Environment.getInstance().getProjectNode() == null) {
            UIController.showErrorDialog("You must open an environment firstly", "Akautauto Error", "There is no opening environment");
            return;
        }

        // backup Environment before build
        backupEnvironment();

        IncrementalBuildEnvironmentLoaderTask task = new IncrementalBuildEnvironmentLoaderTask();

        new AkaThread(task).start();

        //TODO: merge task with dev-hoan
//        // STEP: compile all source code files
//        // todo: move to Task to run in another thread
//        String error = compileAllSourcecodeFiles();
//        if (error != null && error.length() > 0) {
//            UIController.showDetailDialogInMainThread(Alert.AlertType.ERROR, "Incremental build",
//                    "Found compilation error. The environment does not change!",
//                    error);
//            // restore Environment if find out any compile error
//            Environment.restoreEnvironment();
//            return false;
//        }
//
//        // STEP: load project
//        ChangesBetweenSourcecodeFiles.reset();
//        WorkspaceLoader loader = new WorkspaceLoader();
//        String physicalTreePath = new WorkspaceConfig().fromJson().getPhysicalJsonFile();
//        loader.setPhysicalTreePath(new File(physicalTreePath));
//        loader.setShouldCompileAgain(false);
//        loader.setElementFolderOfOldVersion(new WorkspaceConfig().fromJson().getElementDirectory());
//
//        // the load method below might call one thread, so use while loop to wait for the thread done
//        loader.load(loader.getPhysicalTreePath());
//        while (!loader.isLoaded()) {
//            try {
//                Thread.sleep(100);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//        if (ChangesBetweenSourcecodeFiles.modifiedSourcecodeFiles.size() == 0) {
//            UIController.showSuccessDialog("There is no change in source code files when rebuilding incrementally",
//                    "Incremental build", "No change");
//            // restore Environment if find out any compile error
//            Environment.restoreEnvironment();
//            return true;
//        } else {
//            logger.debug("Deleted Nodes: ");
//            for (String str : ChangesBetweenSourcecodeFiles.deletedPaths) {
//                logger.debug(str);
//            }
//            logger.debug("End Deleted Node.");
//            logger.debug("Modified Nodes:");
//            for (INode node : ChangesBetweenSourcecodeFiles.modifiedNodes) {
//                logger.debug(node.getAbsolutePath());
//            }
//            logger.debug("End Modified Nodes.");
//            logger.debug("Added Nodes:");
//            for (INode node : ChangesBetweenSourcecodeFiles.addedNodes) {
//                logger.debug(node.getAbsolutePath());
//            }
//            logger.debug("End Added Nodes.");
////            UIController.showDetailDialog(Alert.AlertType.INFORMATION, "Incremental build", "Changes",
////                    ChangesBetweenSourcecodeFiles.getReportOfDifferences());
//            IncrementalBuildingConfirmWindowController controller =
//                    IncrementalBuildingConfirmWindowController.getInstance();
//            if (controller != null) {
//                Stage window = controller.getStage();
//                if (window != null) {
//                    window.setResizable(false);
//                    window.initModality(Modality.WINDOW_MODAL);
//                    window.initOwner(UIController.getPrimaryStage().getScene().getWindow());
//                    window.setOnCloseRequest(event -> Environment.restoreEnvironment());
//                    window.showAndWait();
//                }
//                if (controller.isConfirm()) {
//                    // update source code file node on active source code tabs
//                    updateActiveSourceCodeTabs();
//                    return true;
//                } else return false;
//            } else {
//                return false;
//            }
//        }
    }
//
//    private void updateActiveSourceCodeTabs() {
//        Environment.getInstance().getActiveSourcecodeTabs().clear();
//        Map<Tab, INode> activeSourcecodeTabs = Environment.getBackupEnvironment().getActiveSourcecodeTabs();
//        for (Tab tab : activeSourcecodeTabs.keySet()) {
//            SourcecodeFileNode fileNode = UIController.searchSourceCodeFileNodeByPath(
//                    activeSourcecodeTabs.get(tab).getAbsolutePath());
//            ((SourceCodeViewTab) tab).setSourceCodeFileNode(fileNode);
//            Environment.getInstance().getActiveSourcecodeTabs().put(tab, fileNode);
//        }
//    }

    private void backupEnvironment() {
        // use update Environment to backup and create a clone version of currently Environment
        Environment.backupEnvironment();
//        Environment.getInstance().setActiveSourcecodeTabs(Environment.getBackupEnvironment().getActiveSourcecodeTabs());
    }

    public void loadTestCaseNavigator() {
        if (Environment.getInstance().getEnvironmentRootNode() != null) {
            EnvironmentRootNode root = Environment.getInstance().getEnvironmentRootNode();
            if (root != null) {
                List<IEnvironmentNode> nodes = EnvironmentSearch.searchNode(root, new EnviroNameNode());
                if (nodes.size() == 1) {
                    String testcaseScriptPath = new WorkspaceConfig().fromJson().getTestscriptFile();
                    File testcaseScriptFile = new File(testcaseScriptPath);
                    if (testcaseScriptFile.exists()) {
                        UIController.loadTestCasesNavigator(testcaseScriptFile);
                        logger.debug("Load test case navigator from " + testcaseScriptPath);

                        TestCaseManager.clearMaps();
                        TestCaseManager.initializeMaps();

//                        addMenuItemDisableProperty(miViewTestCaseData,
//                                TestNewNode.class,
//                                TestNormalSubprogramNode.class,
//                                TestCompoundSubprogramNode.class,
//                                TestInitSubprogramNode.class,
//                                TestUnitNode.class,
//                                TestcaseRootNode.class
//                        );
//                        addMenuItemDisableProperty(miViewFull,
//                                TestNewNode.class,
//                                TestNormalSubprogramNode.class,
//                                TestCompoundSubprogramNode.class,
//                                TestInitSubprogramNode.class,
//                                TestUnitNode.class,
//                                TestcaseRootNode.class
//                        );
//                        addMenuItemDisableProperty(miViewTestCaseManage,
//                                TestCompoundSubprogramNode.class,
//                                TestInitSubprogramNode.class,
//                                TestUnitNode.class,
//                                TestcaseRootNode.class
//                        );
//                        addMenuItemDisableProperty(miViewCoverage,
//                                TestNewNode.class,
//                                TestNormalSubprogramNode.class,
//                                TestUnitNode.class
//                        );

                    } else {
                        logger.error("Can not load the test case navigator script file");
                        logger.debug("The test script file path: " + testcaseScriptPath);
                    }
                } else {
                    logger.error("There is more than one name in the environment file");
                }
            }
        }
    }

    public void stopAllAutomatedTestdataGenerationThread(ActionEvent actionEvent) {
        if (Environment.getInstance().getProjectNode() == null) {
            UIController.showErrorDialog("You must open an environment firstly", "Akautauto Error", "There is no opening environment");
            return;
        }

        logger.debug("Shut down all automated test data generation threads");
        AbstractAkaTask<Void> task = new AbstractAkaTask<Void>() {
            @Override
            protected Void call() throws Exception {
                INode root = Environment.getInstance().getProjectNode();
                AkaThreadManager.stopAutomatedTestdataGenerationForAll(root);
                return null;
            }
        };

        new AkaThread(task).start();

        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                UIController.showSuccessDialog("Stop automated test data generation successfully",
                        "Automated test data generation", "Terminate successfully");
                MenuBarController.getMenuBarController().refresh();
            }
        });
    }

    public synchronized void refresh() {
        logger.debug("Refresh menu bar");

        boolean openedProject = Environment.getInstance().getProjectNode() == null;
        openedProject = false;
        boolean isActiveMultiSelection = Environment.getInstance().isCoverageModeActive();

        logger.debug("is active multi selection = " + isActiveMultiSelection);

        // FILE
        miRecentEnvironments.setDisable(new AkaConfig().fromJson().getRecentEnvironments().size() == 0);;
    }

    @FXML
    public void showAvailableRegressionScripts() {
        if (Environment.getInstance().getProjectNode() == null) {
            UIController.showErrorDialog("You must open an environment firstly", "Akautauto Error", "There is no opening environment");
            return;
        }

        AvailableRegressionScriptsController controller = AvailableRegressionScriptsController.getInstance();
        if (controller != null) {
            Stage stage = controller.getStage();
            if (stage != null) {
                stage.setResizable(false);
                stage.initModality(Modality.WINDOW_MODAL);
                stage.initOwner(UIController.getPrimaryStage().getScene().getWindow());
                stage.show();
            }
        }
    }

    public void instrumentProject(ActionEvent actionEvent) {
        if (Environment.getInstance().getProjectNode() == null) {
            UIController.showErrorDialog("You must open an environment firstly", "Akautauto Error", "There is no opening environment");
            return;
        }

        AbstractAkaTask<Void> task = new AbstractAkaTask<Void>() {
            @Override
            protected Void call() throws Exception {
                ProjectClone.cloneEnvironment();
                LcovProjectClone.cloneLcovEnvironment();
                return null;
            }
        };

        new AkaThread(task).start();

        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                UIController.showSuccessDialog("Project is instrumented successfully",
                        "Project instrumentation", "Success");
            }
        });
    }

    public void setupBoundOfVariableType(ActionEvent actionEvent) {
        if (Environment.getInstance().getProjectNode() == null) {
            UIController.showErrorDialog("You must open an environment firstly", "Akautauto Error", "There is no opening environment");
            return;
        }

        ExistedBoundaryController controller = ExistedBoundaryController.getInstance();
        if (controller != null) {
            Stage stage = controller.getStage();
            if (stage != null) {
                stage.setResizable(false);
                stage.initModality(Modality.WINDOW_MODAL);
                stage.initOwner(UIController.getPrimaryStage().getScene().getWindow());
                stage.show();
            }
        }
    }

    public void setZ3Solver(ActionEvent actionEvent) {
        while (true) {
            Stage primaryStage = UIController.getPrimaryStage();
            DirectoryChooser workingDirectoryChooser = new DirectoryChooser();
            workingDirectoryChooser.setTitle("Select Z3 Solver");
            File selectedDir = workingDirectoryChooser.showDialog(primaryStage);

            if (selectedDir != null) {
                String z3Path = null;
                if (selectedDir.isDirectory()) {
                    List<String> allFiles = Utils.getAllFiles(selectedDir.getAbsolutePath());
                    for (String f : allFiles) {
                        String path = new File(f).getAbsolutePath();
                        if (path.endsWith("bin" + File.separator + "z3")
                                || path.endsWith("bin" + File.separator + "z3.exe")) {
                            z3Path = new File(f).getAbsolutePath();
                            break;
                        }
                    }
                }

                if (z3Path != null) {
                    new AkaConfig().fromJson().setZ3Path(z3Path).exportToJson();
                    logger.debug("The current z3 solver file: " + z3Path);
                    showValidZ3SolverStatus();
                    return;
                } else {
                    if (selectedDir.isDirectory())
                        UIController.showErrorDialog("Do not find z3 in folder " + selectedDir.getAbsolutePath()
                                , "Set up z3 solver", "Wrong configuration");
                }
            } else
                return;
        }
    }

    @FXML
    public void manageUserCode() {
        if (Environment.getInstance().getProjectNode() == null) {
            UIController.showErrorDialog("You must open an environment firstly", "Akautauto Error", "There is no opening environment");
            return;
        }

        // show User code manage window
        UserCodeManagerController controller = UserCodeManagerController.getInstance();
        if (controller != null) {
            controller.showStage(UIController.getPrimaryStage());
        }
    }

    @FXML
    public void viewReleaseNotes(ActionEvent actionEvent) {
        new AkaThread(new ViewReleaseNotesTask()).start();
    }

    @FXML
    public void viewLicense(ActionEvent actionEvent) {
        LicenseController controller = LicenseController.getInstance();
        controller.setLicenseView();
        if (controller != null) {
            Stage stage = controller.getStage();
            if (stage != null) {
                stage.setResizable(false);
                stage.initModality(Modality.WINDOW_MODAL);
                stage.initOwner(UIController.getPrimaryStage().getScene().getWindow());
                stage.show();
            }
        }

    }

    public void testZ3Solver(ActionEvent actionEvent) {
        final String z3Path = new AkaConfig().fromJson().getZ3Path();
        if (z3Path == null || z3Path.isEmpty()) {
            UIController.showErrorDialog("Z3 path hasn't set yet!", "Z3 Solver", "Z3 Solver Error");
        } else if (!new File(z3Path).exists()) {
            UIController.showErrorDialog("Can't find Z3 Solver at the selected path", "Z3 Solver", "Z3 Solver Error");
        } else {
            TestZ3Task testZ3Task = new TestZ3Task(z3Path);
            new AkaThread(testZ3Task).start();
        }
    }
}
