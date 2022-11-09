package com.dse.debugger.controller;

import com.dse.testcase_execution.CompoundTestcaseExecution;
import com.dse.testcase_execution.ITestcaseExecution;
import com.dse.testcase_execution.TestcaseExecution;
import com.dse.coverage.CoverageManager;
import com.dse.debugger.TestMode;
import com.dse.debugger.component.breakpoint.BreakPoint;
import com.dse.debugger.component.breakpoint.BreakPointManager;
import com.dse.debugger.gdb.GDB;
import com.dse.debugger.DebugTab;
import com.dse.debugger.gdb.analyzer.GDBStatus;
import com.dse.environment.Environment;
import com.dse.guifx_v3.helps.UIController;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.ISourcecodeFileNode;
import com.dse.project_init.ProjectClone;
import com.dse.report.ExecutionResultReport;
import com.dse.report.ReportManager;
import com.dse.testcase_manager.CompoundTestCase;
import com.dse.testcase_manager.ITestCase;
import com.dse.testcase_manager.TestCase;
import com.dse.thread.AkaThread;
import com.dse.logger.AkaLogger;
import com.dse.util.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.TreeSet;

public class DebugController implements Initializable {
    public static boolean isExit =false;
    private final static AkaLogger logger = AkaLogger.get(DebugController.class);

    public static DebugController debugController;
    protected static AnchorPane debugPane;

    protected static void prepare() {
        FXMLLoader loader = new FXMLLoader((DebugController.class.getResource("/FXML/debugger/Debug.fxml")));
        try {
            debugPane = loader.load();
            debugController = loader.getController();
        } catch (Exception e) {
            logger.debug("Can not load Debugger UI");
            e.printStackTrace();
        }
    }

    public static AnchorPane getDebugPane() {
        if (debugPane == null) prepare();
        return debugPane;
    }

    public static DebugController getDebugController() {
        if (debugController == null) prepare();
        return debugController;
    }

    @FXML
    TabPane codeViewer;

    @FXML
    Pane debugViewer;

    @FXML
    Pane varPane;

    @FXML
    Pane framePane;

    @FXML
    Pane watchesPane;

    @FXML
    Button continueBtn;

    @FXML
    Button nextBtn;

    @FXML
    Button stepInBtn;

    @FXML
    Button stepOutBtn;

    @FXML
    Button stopBtn;

    @FXML
    Button runBtn;

    @FXML
    Tab breakTab;

    protected ITestCase testCase = null;
    protected GDB gdb = null;
    protected TestMode mode = null;

    protected int currentLineHit = 0;

    protected String currentPathHit = null;
    protected BreakPointManager breakPointManager = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        breakPointManager = BreakPointManager.getBreakPointManager();
        this.framePane.getChildren().add(FrameController.getTitledPane());
        this.watchesPane.getChildren().add(WatchController.getTitledPane());
        this.varPane.getChildren().add(VariableController.getTitledPane());
        this.breakTab.setContent(BreakPointController.getPane());
    }

    public boolean loadAndExecuteTestCase(ITestCase testCase) {
        // 1. Load test case
        this.testCase = testCase;
        this.testCase.setPathDefault();
        this.testCase.setBreakpointPathDefault();

        // 2. Execute test case first to generate test driver
        if (this.testCase instanceof TestCase) {
            TestcaseExecution execution = new TestcaseExecution();
            execution.setMode(ITestcaseExecution.IN_DEBUG_MODE);
            execution.setTestCase(this.testCase);
            mode = TestMode.SINGLE;
            try {
                execution.execute();
            } catch (Exception e) {
                logger.debug("Execute single test case failed");
                e.printStackTrace();
            }
        } else if (this.testCase instanceof CompoundTestCase) {
            CompoundTestcaseExecution execution = new CompoundTestcaseExecution();
            execution.setMode(ITestcaseExecution.IN_DEBUG_MODE);
            execution.setTestCase(this.testCase);
            mode = TestMode.COMPOUND;
            try {
                execution.execute();
            } catch (Exception e) {
                logger.debug("Execute compound test case failed");
                e.printStackTrace();
            }
        }
//      clear breakpoint from previous testcase
        BreakPointController.getBreakPointController().clearAll();

        // 3. Load the saved breakpoint
        String breakPath = this.testCase.getBreakpointPath();
        breakPointManager.setup(breakPath);
        return true;
    }
    public void resetPathHit(ITestCase testCase) {
        this.currentLineHit = 0;
        this.currentPathHit = testCase.getSourceCodeFile();
    }

    public void startGDB(ITestCase testCase) {
        setDisableDebugButtons(true);
        this.currentLineHit = 0;
        this.currentPathHit = testCase.getSourceCodeFile();
        GDB gdb = new GDB();
        gdb.setTestcase(testCase);
        gdb.setBreakPointMap(breakPointManager.getBreakPointMap());
        new AkaThread(gdb).start();
        this.gdb = gdb;
        loadFirstTab();
    }

    private void loadFirstTab() {
        String path = "";
        int line = 0;
        if (mode == TestMode.COMPOUND) {
            CompoundTestCase temp = (CompoundTestCase) this.testCase;
            path = temp.getSourceCodeFile();
            path = Utils.normalizePath(path);
            // todo: open source code of first slot
            // just open test driver on compound case
        }
        if (mode == TestMode.SINGLE) {
            TestCase temp = (TestCase) this.testCase;
            ICommonFunctionNode functionNode = temp.getFunctionNode();
            line = ProjectClone.getStartLineNumber(functionNode);
            ISourcecodeFileNode cloneFile = Utils.getSourcecodeFile(temp.getFunctionNode());
            path = ProjectClone.getClonedFilePath(cloneFile.getAbsolutePath());
            path = Utils.normalizePath(path);
            ObservableList<Tab> tabList = this.codeViewer.getTabs();
            for (Tab tab : tabList) {
                DebugTab debugTab = (DebugTab) tab;
                if (debugTab.getPath().equals(path)) {
                    this.codeViewer.getSelectionModel().select(debugTab);
                    return;
                }
            }
        }
        TreeSet<BreakPoint> listPoint = breakPointManager.searchBreaksFromPath(path);
        if (listPoint == null) {
            listPoint = new TreeSet<>();
            breakPointManager.getBreakPointMap().put(path, listPoint);
        }
        DebugTab newTab = new DebugTab(path,listPoint) ;
        if (mode == TestMode.SINGLE) {
            newTab.showLineInViewport(line, false);
        }
        this.codeViewer.getTabs().add(newTab);
        this.codeViewer.getSelectionModel().select(newTab);

    }

    private void setDisableDebugButtons(boolean isDisable) {
        this.stopBtn.setDisable(isDisable);
        this.nextBtn.setDisable(isDisable);
        this.continueBtn.setDisable(isDisable);
        this.stepOutBtn.setDisable(isDisable);
        this.stepInBtn.setDisable(isDisable);
        WatchController.getWatchController().showButton(isDisable);
    }

    public ITestCase getCurrentTestCase() {
        return this.testCase;
    }

    public void turnOff() {
        stop();
        if (gdb != null && testCase != null) {
            gdb.logAllCommands();
            gdb = null;
            testCase = null;
            breakPointManager = null;
            BreakPointController.getBreakPointController().clearAll();
        }
        debugPane = null;
        debugController = null;
    }

    public GDB getGdb() {
        return this.gdb;
    }

    @FXML
    public void run() {
        if (gdb.isExecuting()) {
            Alert confirmAlert = UIController.showYesNoDialog(Alert.AlertType.CONFIRMATION, "Confirmation",
                    "Rerun debugging",
                    "Stop this session and start new session of debugging");
            Optional<ButtonType> option = confirmAlert.showAndWait();
            if (option.get() == ButtonType.YES) {
                gdb.kill();
                gdb.setExecuting(false);
                run();
                isExit = true;
            } else {
                confirmAlert.close();
            }
        } else {
            GDBStatus status = gdb.beginDebug();
            handleStatus(status);
            gdb.setExecuting(true);
            isExit = false;
        }
    }

    @FXML
    public void continueUntilNextBreakPoint() {
        GDBStatus status = gdb.nextBr();
        handleStatus(status);
    }

    @FXML
    public void nextLine() {
        GDBStatus status = gdb.nextLine();
        handleStatus(status);
    }

    @FXML
    public void stepInFunction() {
        GDBStatus status = gdb.stepIn();
        handleStatus(status);
    }

    @FXML
    public void stepOutFunction() {
        GDBStatus status = gdb.stepOut();
        handleStatus(status);
    }

    @FXML
    public void stopExecutingDebug() {
        isExit = true;
        GDBStatus status = gdb.kill();
        // todo: figure how to handle this case
        gdb.setExecuting(false);
        handleStatus(status);
    }

    public void handleStatus(GDBStatus status) {
        if (status == GDBStatus.EXIT) {
            isExit = true;
            stop();
            gdb.kill();
            // todo: figure how to handle this case
            gdb.setExecuting(false);
//            if (shouldShowReport) {
//            if (testCase instanceof TestCase) {
//                CoverageManager.exportCoveragesOfTestCaseToFile((TestCase) testCase,
//                        Environment.getInstance().getTypeofCoverage());
//            }
//            ExecutionResultReport report = new ExecutionResultReport(testCase, LocalDateTime.now());
//            ReportManager.export(report);
//            MDIWindowController.getMDIWindowController().viewReport(testCase.getName(), report.toHtml());
//            }
        }
        if (status == GDBStatus.CONTINUABLE) {
            setDisableDebugButtons(false);
            VariableController.getVariableController().updateVariables();
            FrameController.getFrameController().updateFrames();
            WatchController.getWatchController().updateWatches();
        }
        if (status == GDBStatus.ERROR) {
            stop();
            gdb.kill();
            // todo: figure how to handle this case
            gdb.setExecuting(false);
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Gdb has stopped unexpectedly", ButtonType.OK);
                alert.setTitle("Error");
                alert.setHeaderText("Gdb error");
                alert.showAndWait();
            });
        }
    }

    private DebugTab findTabByPath(String path) {
        if(path != null) {
            path = Utils.normalizePath(path);
            path = path.toUpperCase();
        }
        logger.debug("Finding tab by path: " + path);
        for (Tab tab : this.codeViewer.getTabs()) {
            DebugTab debugTab = (DebugTab) tab;
            if (debugTab.getPath().toUpperCase().equals(path)) {
                return debugTab;
            }
        }
        return null;
    }

    public void openCurrentHitLine(int line, String filePath, boolean isHit) {
        filePath = Utils.normalizePath(filePath);
        DebugTab oldTab = findTabByPath(Utils.normalizePath(this.currentPathHit));
        if (oldTab != null && isHit) {
            oldTab.removeStyleAtLine(this.currentLineHit);
        }
        DebugTab curTab = findTabByPath(filePath);
        if (curTab == null) {
            DebugTab newTab = new DebugTab(filePath, BreakPointManager.getBreakPointManager().getBreakPointMap().get(filePath));
            Platform.runLater(() -> {
                this.codeViewer.getTabs().add(newTab);
                this.codeViewer.getSelectionModel().select(newTab);
                newTab.showLineInViewport(line, isHit);
            });
        } else {
            this.codeViewer.getSelectionModel().select(curTab);
            Platform.runLater(() -> curTab.removeStyleAtLine(line - 1));
            Platform.runLater(() -> curTab.showLineInViewport(line, isHit));
        }
        this.currentPathHit = filePath;
        this.currentLineHit = line;

    }

    public void stop() {
        DebugTab oldTab = findTabByPath(this.currentPathHit);
        if (oldTab != null) {
            oldTab.removeStyleAtLine(this.currentLineHit);
        }
        this.currentLineHit = 0;
        this.currentPathHit = null;
        VariableController.getVariableController().clearAll();
        FrameController.getFrameController().clearAll();
        WatchController.getWatchController().clearAll();
        setDisableDebugButtons(true);
    }

    public void updateBreakPointFile() {
        logger.debug("Update new break point to file");
        GsonBuilder builder = new GsonBuilder().excludeFieldsWithoutExposeAnnotation();
        Gson gson = builder.setPrettyPrinting().create();
        File file = new File(testCase.getBreakpointPath());
        try {
            Type type = new TypeToken<HashMap<String, TreeSet<BreakPoint>>>() {
            }.getType();
            String json = gson.toJson(breakPointManager.getBreakPointMap(), type);
            FileWriter writer = new FileWriter(file.getPath());
            writer.write(json);
            writer.close();
        } catch (IOException e) {
            logger.debug("Can not save break point to file");
            e.printStackTrace();
        }
    }

    public void openSource(String nodeName) {
        String envName = Environment.getInstance().getName();
        int start = nodeName.lastIndexOf(File.separator) + 1;
        int end = nodeName.lastIndexOf(".");
        String prefix = nodeName.substring(0, start);
        String name = nodeName.substring(start, end);
        String tail = nodeName.substring(end);
        String newName = prefix + envName + "." + name + ".akaignore" + tail;

        DebugTab newTab = findTabByPath(newName);
        if (newTab == null) {
            breakPointManager.getBreakPointMap().computeIfAbsent(newName, k -> new TreeSet<>());
            newTab = new DebugTab(newName, breakPointManager.getBreakPointMap().get(newName));
            this.codeViewer.getTabs().add(newTab);
        }
        this.codeViewer.getSelectionModel().select(newTab);
    }

    public void clear() {
        codeViewer.getTabs().clear();
    }
}
