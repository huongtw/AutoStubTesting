package com.dse.guifx_v3.controllers.build_environment;

import com.dse.boundary.BoundaryManager;
import com.dse.compiler.message.ICompileMessage;
import com.dse.config.AkaConfig;
import com.dse.config.WorkspaceConfig;
import com.dse.environment.WorkspaceCreation;
import com.dse.guifx_v3.controllers.main_view.BaseSceneController;
import com.dse.guifx_v3.controllers.main_view.MenuBarController;
import com.dse.guifx_v3.controllers.object.LoadingPopupController;
import com.dse.guifx_v3.controllers.object.build_environment.Step;
import com.dse.environment.Environment;
import com.dse.guifx_v3.helps.UIController;
import com.dse.guifx_v3.objects.hint.Hint;
import com.dse.guifx_v3.objects.hint.HintContent;
import com.dse.thread.AkaThread;
import com.dse.thread.task.BuildEnvironmentResult;
import com.dse.thread.task.BuildNewEnvironmentTask;
import com.dse.parser.object.IProjectNode;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.thread.task.PreUpdateEnvironmentTask;
import com.dse.user_code.UserCodeManager;
import com.dse.logger.AkaLogger;
import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static com.dse.guifx_v3.helps.UIController.getPrimaryStage;

public class BaseController implements Initializable {

    final static AkaLogger logger = AkaLogger.get(BaseController.class);
    /**
     * Singleton pattern
     */
    private static BaseController baseController = null;
    private static Scene baseScene = null;
    private int currentStep = CHOOSE_COMPILER_WINDOW_INDEX;
    private static final Map<Integer, Step> map = new HashMap<>();

    private static void prepare() {
        FXMLLoader loader = new FXMLLoader(Object.class.getResource("/FXML/envbuilding/Base.fxml"));
        try {
            baseScene = new Scene(loader.load());
            baseController = loader.getController();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Scene getBaseScene() {
        if (baseScene == null)
            prepare();
        return baseScene;
    }

    public static BaseController getBaseController() {
        if (baseController == null) prepare();
        return baseController;
    }

    @FXML
    private Button bBack;
    @FXML
    private Button bNext;
    @FXML
    private Button bBuild;
    @FXML
    private Label labelChooseCompiler;
    @FXML
    private Label labelNameEnvironment;
    @FXML
    private Label labelTestingMethod;
    @FXML
    private Label labelBuildOptions;
    @FXML
    private Label labelLocateSourceFiles;
    @FXML
    private Label labelChooseUUT;
    @FXML
    private Label labelUserCode;
    @FXML
    private Label labelSummary;
    @FXML
    private Label title;
    @FXML
    private SplitPane inputWindow;

    public void initialize(URL location, ResourceBundle resources) {
        map.put(CHOOSE_COMPILER_WINDOW_INDEX, new Step(labelChooseCompiler, Step.CHOOSE_COMPILER));
        map.put(NAME_ENVIRONMENT_WINDOW_INDEX, new Step(labelNameEnvironment, Step.NAME_ENVIRONMENT));
        map.put(TESTING_METHOD_WINDOW_INDEX, new Step(labelTestingMethod, Step.TESTING_METHOD));
        map.put(BUILDING_OPTIONS_WINDOW_INDEX, new Step(labelBuildOptions, Step.BUILD_OPTIONS));
        map.put(LOCATE_SOURCE_CODE_FILE_WINDOW_INDEX, new Step(labelLocateSourceFiles, Step.LOCATE_SOURCE_FILES));
        map.put(CHOOSE_UUTS_AND_STUB_WINDOW_INDEX, new Step(labelChooseUUT, Step.CHOOSE_UUT));
        map.put(USER_CODE_WINDOW_INDEX, new Step(labelUserCode, Step.USER_CODE));
        map.put(SUMMARY_WINDOW_INDEX, new Step(labelSummary, Step.SUMARY));

        setCurrentStep(CHOOSE_COMPILER_WINDOW_INDEX);

        setHint();
    }

    public void setHint() {
        Hint.tooltipNode(labelChooseCompiler, HintContent.EnvBuilder.STEP_CHOOSE_COMPILER);
        Hint.tooltipNode(labelNameEnvironment, HintContent.EnvBuilder.STEP_SET_NAME);
        Hint.tooltipNode(labelTestingMethod, HintContent.EnvBuilder.STEP_CHOOSE_METHOD);
        Hint.tooltipNode(labelBuildOptions, HintContent.EnvBuilder.STEP_SET_BUILD_OPTION);
        Hint.tooltipNode(labelLocateSourceFiles, HintContent.EnvBuilder.STEP_LOCATE_SRC);
        Hint.tooltipNode(labelChooseUUT, HintContent.EnvBuilder.STEP_CHOOSE_UUT);
        Hint.tooltipNode(labelUserCode, HintContent.EnvBuilder.STEP_USER_CODE);
        Hint.tooltipNode(labelSummary, HintContent.EnvBuilder.STEP_SUMMARY);
    }

    public static void resetStatesInAllWindows() {
        logger.debug("Reset all states in all windows in environment builder");
        prepare();

        Environment.WindowState.isSearchListNodeUpdated(false);

        getBaseController().clearUserCodes();
    }

    private void setCurrentStep(int currentStep) {
        this.currentStep = currentStep;

        if (this.currentStep == CHOOSE_COMPILER_WINDOW_INDEX) bBack.setDisable(true);
        else bBack.setDisable(false);

        if (this.currentStep == SUMMARY_WINDOW_INDEX) {
            bNext.setDisable(true);
            bBuild.setDisable(false);
        } else {
            bNext.setDisable(false);
            bBuild.setDisable(true);
        }

        selectStep();
    }

    public static void loadEnvironment() {
        for (Step step : map.values()) {
            step.getController().loadFromEnvironment();
        }
    }

    public void save() {
        Step step = map.get(currentStep);
        if (step != null) {
            step.getController().save();
        }
    }

    public void next() {
        if (currentStep == map.size()) return;
        save();
        Step step = map.get(currentStep);
        if (step.getController().isValid()) {
            deSelectStep();
            setCurrentStep(++currentStep);
            selectStep();
        }
    }

    public void back() {
        if (currentStep == CHOOSE_COMPILER_WINDOW_INDEX) return;
        save();
//        Step step = map.get(currentStep);
//        if (step.getController().isValid()) {
        deSelectStep();
        setCurrentStep(--currentStep);
        selectStep();
//        }
    }

    private void selectStep() {
        switch (currentStep) {
            case USER_CODE_WINDOW_INDEX:
                updateUserCode();
                break;

            case CHOOSE_UUTS_AND_STUB_WINDOW_INDEX:
                updateChooseUUT();
                break;

            case SUMMARY_WINDOW_INDEX:
                updateSummary();
                break;
        }

        Step step = map.get(currentStep);

        if (step != null) {
            title.setText(step.getLabel().getText());
            step.getLabel().setStyle("-fx-border-color: #000000; -fx-border-width: 2; -fx-background-color: white");

            inputWindow.getItems().clear();
            AnchorPane anchorPane = step.getAnchorPane();
            inputWindow.getItems().add(anchorPane);
        }
    }

    private void deSelectStep() {
        Step step = map.get(currentStep);
        if (step != null) {
//            step.getController().save();
            step.getLabel().setStyle("-fx-border-color: Grey; -fx-border-width: 1; -fx-background-color: white");
        }
    }

    private void savePrevStep() {
        deSelectStep();
        save();
    }

    public void cancel() {
        resetStatesInAllWindows();
        Environment.removeTempEnvironment();
        Environment.restoreEnvironment();
        UIController.getEnvironmentBuilderStage().close();
    }

    public void updateChooseUUT() {
        Step stepChooseUUT = map.get(CHOOSE_UUTS_AND_STUB_WINDOW_INDEX);
        ((ChooseUUTController) stepChooseUUT.getController()).update();
    }

    public boolean isSourcesLocated() {
        Step stepLocateSources = map.get(LOCATE_SOURCE_CODE_FILE_WINDOW_INDEX);
        return stepLocateSources.getController().isValid();
    }

    public void updateUserCode() {
        Step stepUserCode = map.get(USER_CODE_WINDOW_INDEX);
        ((UserCodeController) stepUserCode.getController()).initializeView();
    }

    public <T extends AbstractCustomController> T getStepController(int index) {
        Step step = map.get(index);
        return (T) step.getController();
    }

    private void clearUserCodes() {
        Step stepUserCode = map.get(USER_CODE_WINDOW_INDEX);
        ((UserCodeController) stepUserCode.getController()).clear();
    }

    public void updateSummary() {
        Step stepSummary = map.get(SUMMARY_WINDOW_INDEX);
        ((SummaryController) stepSummary.getController()).update();
    }

    @FXML
    public void buildEnvironment(ActionEvent actionEvent) {
        String dir = new AkaConfig().fromJson().getWorkingDirectory();
        String name = Environment.getInstance().getName();
        if (AbstractCustomController.ENVIRONMENT_STATUS != AbstractCustomController.STAGE.UPDATING_ENV_FROM_OPENING_ENV) {
            String envFileName = dir + File.separator + name + WorkspaceConfig.ENV_EXTENSION;
            if (new File(envFileName).exists()) {
                logger.error("The environment file " + new WorkspaceConfig().fromJson().getEnvironmentFile() + " exists! Check it again.");
                UIController.showErrorDialog("Could not build the environment because the name of the new environment exists", "Build environment", "Fail");
                return;
            }
        }
        String loggerFileName = dir + File.separator + name + File.separator + "aka.log";
        logger.setAppender(loggerFileName);
        logger.debug("Build the environment");

        LoadingPopupController loadingPopup = LoadingPopupController.newInstance("Build Environment");
        loadingPopup.initOwnerStage(UIController.getEnvironmentBuilderStage());
        loadingPopup.show();

        if (AbstractCustomController.ENVIRONMENT_STATUS == AbstractCustomController.STAGE.UPDATING_ENV_FROM_OPENING_ENV) {

            PreUpdateEnvironmentTask task = new PreUpdateEnvironmentTask();
            new AkaThread(task).start();

        } else {
            buildNewEnvironment(actionEvent, loadingPopup, true);
        }

        MenuBarController.getMenuBarController().refresh();
    }

    private void buildNewEnvironment(ActionEvent actionEvent, LoadingPopupController loadingPopup, boolean shouldCompile) {
        BuildNewEnvironmentTask task = new BuildNewEnvironmentTask(shouldCompile);
        new AkaThread(task).start();

        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                loadingPopup.close();

                int success = BUILD_NEW_ENVIRONMENT.FAILURE.OTHER;

                BuildEnvironmentResult result = null;
                try {
                    result = task.get();
                    success = result.getExitCode();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }

                if (success != BUILD_NEW_ENVIRONMENT.SUCCESS.CREATE_ENVIRONMENT) {
                    switch (success) {
                        case BUILD_NEW_ENVIRONMENT.FAILURE.LINKAGE: {
                            showLinkError(actionEvent, result.getLinkMessage());
                            break;
                        }

                        case BUILD_NEW_ENVIRONMENT.FAILURE.COMPILATION: {
                            showResolver(result);
                            break;
                        }

                        case BUILD_NEW_ENVIRONMENT.FAILURE.DUPLICATE_ENV_FILE:
                        case BUILD_NEW_ENVIRONMENT.FAILURE.DUPLICATE_TST_FILE: {
                            UIController.showErrorDialog("Could not build the environment because the name of the new environment exists", "Build environment", "Fail");
                            break;
                        }
                        case BUILD_NEW_ENVIRONMENT.FAILURE.OTHER: {
                            UIController.showErrorDialog("Could not build the environment due to unexpected error", "Build environment", "Fail");
                            break;
                        }
                    }
                } else { // if successful
                    //
                    closeTheEnvironmentBuilderWindow(actionEvent);

                    // clear every thing (MDIWindows, messange windows)
                    UIController.clear();
                    resetStatesInAllWindows();

                    File testcasesScriptFile = new File(new WorkspaceConfig().fromJson().getTestscriptFile());
                    loadTestCasesNavigator(testcasesScriptFile);

                    TestCaseManager.clearMaps();
                    TestCaseManager.initializeMaps();

                    // update information displaying on bottom of screen
                    BaseSceneController.getBaseSceneController().updateInformation();

                    loadProjectStructureTree();
                    // load boundary of variable types
                    BoundaryManager.getInstance().clear();
                    BoundaryManager.getInstance().loadExistedBoundaries();
                    // load user codes
                    UserCodeManager.getInstance().clear();
                    UserCodeManager.getInstance().loadExistedUserCodes();

                    File envFile = new File(new WorkspaceConfig().fromJson().getEnvironmentFile());
                    MenuBarController.addEnvironmentToHistory(envFile);

                    UIController.showSuccessDialog("Build the environment successfully", "Build environment", "Success");
                }
            }
        });
    }

    private void showLinkError(ActionEvent actionEvent, ICompileMessage linkMessage) {
        ButtonType ignore = new ButtonType("Ignore", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert alert = new Alert(Alert.AlertType.WARNING, SpecialCharacter.EMPTY, ignore, cancel);
        alert.setTitle("Build Environment Error");
        alert.setHeaderText("Failed to link this project");

        TextArea textArea = new TextArea();
        textArea.setWrapText(true);
        textArea.setEditable(false);
        textArea.setMinHeight(350);
        textArea.setText(linkMessage.getMessage());

        alert.getDialogPane().setContent(textArea);
        alert.initOwner(getPrimaryStage());

        Optional<ButtonType> result = alert.showAndWait();

        if (result.orElse(ignore) == cancel) {
            alert.close();
        } else {
            LoadingPopupController loadingPopup = LoadingPopupController.newInstance("Build Environment");
            loadingPopup.initOwnerStage(UIController.getEnvironmentBuilderStage());
            loadingPopup.show();

            buildNewEnvironment(actionEvent, loadingPopup, false);
        }
    }

    public void onSelectChooseCompiler() {
        savePrevStep();
        setCurrentStep(CHOOSE_COMPILER_WINDOW_INDEX);
        selectStep();
    }

    public void onSelectSummary() {
        savePrevStep();
        setCurrentStep(SUMMARY_WINDOW_INDEX);
        selectStep();
    }

    public void onSelectUserCode() {
        savePrevStep();
        setCurrentStep(USER_CODE_WINDOW_INDEX);
        selectStep();
    }

    public void onSelectChooseUUT() {
        savePrevStep();
        setCurrentStep(CHOOSE_UUTS_AND_STUB_WINDOW_INDEX);
        selectStep();
    }

    public void onSelectLocateSourceFiles() {
        savePrevStep();
        setCurrentStep(LOCATE_SOURCE_CODE_FILE_WINDOW_INDEX);
        selectStep();
    }

    public void onSelectBuildOptions() {
        savePrevStep();
        setCurrentStep(BUILDING_OPTIONS_WINDOW_INDEX);
        selectStep();
    }

    public void onSelectTestingMethod() {
        savePrevStep();
        setCurrentStep(TESTING_METHOD_WINDOW_INDEX);
        selectStep();

    }

    public void onSelectNameEnvironment() {
        savePrevStep();
        setCurrentStep(NAME_ENVIRONMENT_WINDOW_INDEX);
        selectStep();
    }

    public static class BUILD_NEW_ENVIRONMENT {
        public static class SUCCESS {
            public final static int EXPORT_ENV_FILE = 0;
            public final static int EXPORT_TST_FILE = 1;
            public final static int CREATE_ENVIRONMENT = 2;
            public final static int COMPILATION = 3;
        }

        public static class FAILURE {
            public final static int OTHER = 12;
            public final static int DUPLICATE_ENV_FILE = 10;
            public final static int DUPLICATE_TST_FILE = 11;
            public final static int COMPILATION = 13;
            public final static int LINKAGE = 14;
        }
    }

    public static void addAnalysisInformationToWorkspace(String newWorkspace, String dependencyFolder,
                                                         String physicalFile, String elementFolder) {
        // create workspace
        WorkspaceCreation wk = new WorkspaceCreation();
        wk.setWorkspace(newWorkspace);
        wk.setDependenciesFolder(dependencyFolder);
        wk.setElementFolder(elementFolder);
        wk.setPhysicalTreePath(physicalFile);
        wk.setRoot(Environment.getInstance().getProjectNode());
        wk.create(wk.getRoot(), wk.getElementFolder(), wk.getDependenciesFolder(), wk.getPhysicalTreePath());
    }

    private void closeTheEnvironmentBuilderWindow(ActionEvent actionEvent) {
        // if there is no error, move to the main window
        final Node source = (Node) actionEvent.getSource();
        final Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    private void loadTestCasesNavigator(File testCasesScript) {
        UIController.loadTestCasesNavigator(testCasesScript);
    }

    private void loadProjectStructureTree() {
        IProjectNode root = Environment.getInstance().getProjectNode();
        root.setName(Environment.getInstance().getName());
        UIController.loadProjectStructureTree(root);
    }

    public static Stage showResolver(BuildEnvironmentResult result) {
        Stage sourceCodeResolverStage = new Stage();
        sourceCodeResolverStage.setTitle("Source code resolver");
        sourceCodeResolverStage.setResizable(false);

        // block the environment building window
        sourceCodeResolverStage.initModality(Modality.WINDOW_MODAL);
        sourceCodeResolverStage.initOwner(UIController.getEnvironmentBuilderStage().getScene().getWindow());

        try {
            FXMLLoader loader = new FXMLLoader(BaseController.class.getResource("/FXML/envbuilding/SourceCodeResolver.fxml"));
            Parent root = loader.load();

            SrcResolverController controller = loader.getController();

            // set the directory where the linkage command is executed
            controller.setDirectory(Environment.getInstance().getProjectNode().getAbsolutePath());

            sourceCodeResolverStage.setScene(new Scene(root));
            controller.setStage(sourceCodeResolverStage);

            controller.setCompilationCommands(result.getCompilationCommands());
            controller.setCompiler(Environment.getInstance().getCompiler());
            controller.setStartIdx(result.getFileIndex());
            controller.setSourceFiles(result.getSourceCodes());

            String title = result.getSourceCodes().get(result.getFileIndex().get()).getAbsolutePath();
            controller.displayProblemOnScreen(title, result.getCompileMessage());

            sourceCodeResolverStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return sourceCodeResolverStage;
    }

    private static final int CHOOSE_COMPILER_WINDOW_INDEX = 1;
    private static final int NAME_ENVIRONMENT_WINDOW_INDEX = 2;
    private static final int TESTING_METHOD_WINDOW_INDEX = 3;
    private static final int BUILDING_OPTIONS_WINDOW_INDEX = 4;
    private static final int LOCATE_SOURCE_CODE_FILE_WINDOW_INDEX = 5;
    private static final int CHOOSE_UUTS_AND_STUB_WINDOW_INDEX = 6;
    public static final int USER_CODE_WINDOW_INDEX = 7;
    private static final int SUMMARY_WINDOW_INDEX = 8;
}
