package com.dse.debugger.gdb;

import com.dse.compiler.Terminal;
import com.dse.config.AkaConfig;
import com.dse.config.WorkspaceConfig;
import com.dse.debugger.GDBProcess;
import com.dse.debugger.component.BreakPointFactory;
import com.dse.debugger.component.breakpoint.BreakPointManager;
import com.dse.debugger.component.variable.GDBVarChange;
import com.dse.debugger.controller.DebugController;
import com.dse.debugger.gdb.analyzer.GDBStatus;
import com.dse.debugger.gdb.analyzer.OutputGDB;
import com.dse.debugger.gdb.analyzer.OutputSyntax;
import com.dse.debugger.component.breakpoint.BreakPoint;
import com.dse.debugger.component.frame.GDBFrame;
import com.dse.debugger.component.variable.GDBVar;
import com.dse.debugger.component.watches.WatchPoint;
import com.dse.debugger.utils.DebugCommandConfig;
import com.dse.debugger.controller.BreakPointController;
import com.dse.environment.Environment;
import com.dse.guifx_v3.helps.UIController;
import com.dse.testcase_manager.ITestCase;
import com.dse.thread.AbstractAkaTask;
import com.dse.logger.AkaLogger;
import com.dse.util.CompilerUtils;
import com.dse.util.Utils;
import com.dse.util.VariableTypeUtils;
import com.google.gson.*;
import javafx.application.Platform;
import javafx.collections.ObservableMap;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class GDB extends AbstractAkaTask<Object> implements IGDBMI {
    private final AtomicBoolean isExecuting = new AtomicBoolean(false);
    private final static AkaLogger logger = AkaLogger.get(GDB.class);
    private final static String RUN_WITH_GDB_MI = "--interpreter=mi";
    private final static String COMMAND_FILE = "-x " + Utils.normalizePath(GDB.class.getResource("/gdbconfig/gdb.conf").getPath());
    private final static int END_STRING_LENGTH = 65;
    private ITestCase testCase;
    private String logPath;
    private ObservableMap<String, TreeSet<BreakPoint>> breakPointMap;

    private final ArrayList<String> listCommands = new ArrayList<>();
    private final ArrayList<GDBVarChange> listVarChange = new ArrayList<>();

    private GDBProcess currentProcess;

    public BreakPoint addNewBreakPoint(int value, String path) {
        String cmd = GDB_BR + "\"" + new File(path).getName() + ":" + value + "\"";
        String output = executeAndLog(cmd);
        //OutputGDB outputGDB = new OutputAnalyzer().analyzeOutput(output, GDB_BR);
        // todo: check null output
        return new OutputAnalyzer().analyzeBreakAdd(output);
    }

    public OutputGDB deleteBreakPoint(BreakPoint breakPoint) {
        String cmd = GDB_DEL_POINT + breakPoint.getNumber();
        String output = executeAndLog(cmd);
        return new OutputAnalyzer().analyzeOutput(output, GDB_CLEAR);
    }

    public OutputGDB disableBreakPoint(BreakPoint breakPoint) {
        String cmd = GDB_DISABLE + breakPoint.getNumber();
        String output = executeAndLog(cmd);
        return new OutputAnalyzer().analyzeOutput(output, GDB_DISABLE);
    }

    public OutputGDB enableBreakPoint(BreakPoint breakPoint) {
        String cmd = GDB_ENABLE + breakPoint.getNumber();
        String output = executeAndLog(cmd);
        return new OutputAnalyzer().analyzeOutput(output, GDB_ENABLE);
    }

    public OutputGDB addConditionOnBreakPoint(BreakPoint breakPoint, String conditionExpr) {
        String cmd = GDB_BREAK_CONDITION + breakPoint.getNumber() + " (" + conditionExpr + ")";
        String output = executeAndLog(cmd);
        OutputGDB outputGDB = new OutputAnalyzer().analyzeOutput(output, GDB_BREAK_CONDITION);
        if (outputGDB != null && !outputGDB.isError()) {
            breakPoint.setCond(conditionExpr);
        }
        return outputGDB;
    }

    public OutputGDB removeConditionOnBreakPoint(BreakPoint breakPoint) {
        String cmd = GDB_BREAK_CONDITION + breakPoint.getNumber();
        String output = executeAndLog(cmd);
        OutputGDB outputGDB = new OutputAnalyzer().analyzeOutput(output, GDB_BREAK_CONDITION);
        if (outputGDB != null && !outputGDB.isError()) {
            breakPoint.setCond("");
        }
        return outputGDB;
    }

    public OutputGDB evaluateExpression(String expression) {
        String cmd = GDB_DATA_EVALUATE_EXPRESSION + " \"" + expression + "\"";
        String output = executeAndLog(cmd);
        return new OutputAnalyzer().analyzeOutput(output, GDB_DATA_EVALUATE_EXPRESSION);
    }

    /**
     * Start debugging
     *
     * @param testCase test case
     * @return a process
     */
    private GDBProcess startDebugProcess(ITestCase testCase) {
        try {
            compileAndLink(new DebugCommandConfig().fromJson(testCase.getCommandDebugFile()));
            String exePath = testCase.getDebugExecutableFile();
            String exeFile = new File(exePath).getCanonicalPath();
            File exeDebugFile = new File(testCase.getDebugExecutableFile());
            if (!exeDebugFile.exists()) {
                UIController.showErrorDialog("Can not generate executable file", "Error", "Can not find executable file");
                return null;
            }

            // start gdb-mi process
            String gdbConfPath = new WorkspaceConfig().fromJson().getDebugDirectory() + File.separator + "gdb.conf";
            gdbConfPath = Utils.normalizePath(gdbConfPath);
            File gdbConfFile = new File(gdbConfPath);
            if (!gdbConfFile.exists()) {
                Utils.copyFileFromResource("/gdbconfig/gdb.conf", gdbConfFile);
            }
            return new GDBProcess(gdbConfPath, exePath);
//            String commandFile = String.format("-x \"%s\"", gdbConfFile);
//            String gdbCommand = String.format("%s %s \"%s\" %s", debugCommand, commandFile, exeFile, RUN_WITH_GDB_MI);
//            Process gdbProcess = Runtime.getRuntime().exec(gdbCommand);
//            logger.debug("[GDB thread] Executing " + gdbCommand);
//            // start a new thread managing gdb-mi process
//            currentProcess = new CurprocessThread();
//            currentProcess.setProcess(gdbProcess);
//            currentProcess.setGdb_mi_enabled(false);
//            logger.debug("[GDB thread] A new threading managing gdb-mi process is started!");
//            new AkaThread(currentProcess).start();
//
//            if (currentProcess != null) {
//                return currentProcess;
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void compileAndLink(DebugCommandConfig customCommandConfig) throws IOException, InterruptedException {
        // Create an executable file
        String workspace = new AkaConfig().fromJson().getOpeningWorkspaceDirectory();
        String directory = new File(workspace).getParentFile().getParentFile().getPath();

        for (String compilationCommand : customCommandConfig.getDebugCommands().values()) {
            logger.debug("[GDB thread] Executing " + compilationCommand);
            String[] script = CompilerUtils.prepareForTerminal(Environment.getInstance().getCompiler(), compilationCommand);
//            String script = compilationCommand.replace("\"", "");

            Terminal terminal = new Terminal(script, directory);
            Process p = terminal.getProcess();
//            Process p = Runtime.getRuntime().exec(script);
            logger.debug("[GDB thread] Wait for 5 seconds");
            p.waitFor(5, TimeUnit.SECONDS);
            logger.debug("Execute done");
        }
        {
            logger.debug("[GDB thread] Linking object file. Command: " + customCommandConfig.getLinkingCommand());
            String[] script = CompilerUtils.prepareForTerminal(Environment.getInstance().getCompiler(), customCommandConfig.getLinkingCommand());
//            String script = customCommandConfig.getLinkingCommand().replace("\"", "");
            Terminal terminal = new Terminal(script, directory);
            Process p = terminal.getProcess();

//            Process p = Runtime.getRuntime().exec(script);
            p.waitFor();
        }
    }

    private void setBreakpoints() {
        logger.debug("[GDB thread] Start executing breakpoint saving in gdb-mi");
        final int[] total = {0};
        breakPointMap = BreakPointManager.getBreakPointManager().getBreakPointMap();
        breakPointMap.keySet().forEach(key -> {
            TreeSet<BreakPoint> brSet = breakPointMap.get(key);
            TreeSet<BreakPoint> tempSet = (TreeSet<BreakPoint>) brSet.clone();
            for (BreakPoint br : tempSet) {
                BreakPoint newBr = addNewBreakPoint(br.getLine(), key);
                if (br.getEnabled().equals("n")) {
                    disableBreakPoint(newBr);
                    newBr.setEnabled("n");
                    newBr.setSelected(false);
                }
                if (br.getCond() != null && !br.getCond().equals("")) {
                    addConditionOnBreakPoint(newBr, br.getCond());
                }
                brSet.remove(br);
                brSet.add(newBr);
                total[0]++;
            }
        });
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                BreakPointController.getBreakPointController().addAll(BreakPointManager.getBreakPointManager().getAllBreaks());
                BreakPointManager.getBreakPointManager().getActivePoints().forEach(p -> BreakPointFactory.updateBreak(p));
                logger.debug("[GDB thread] Add " + total[0] + " breakpoints done");
                DebugController.getDebugController().updateBreakPointFile();
            }
        });
    }

    private String executeAndLog(String command) {
        return currentProcess.executeAndLog(command);
//        String logPath = new WorkspaceConfig().fromJson().getDebugCommandsDirectory() + "/" + testCase.getName() + ".log";
//        this.logPath = logPath;
//        runCommandInGdb(currentProcess, GDB_SET + "logging on");
//        runCommandInGdb(currentProcess, command);
//        runCommandInGdb(currentProcess, GDB_SET + "logging off");
//
//        String res = Utils.readFileContent(logPath);
//        while (timeSearch(res) == null) {
//            try {
//                Thread.sleep(10);
//                res = Utils.readFileContent(logPath);
//            } catch (InterruptedException e) {
//                UIController.showErrorDialog("Unable to get the result when executing the following command in gdb: " + command + "\n",
//                        "GDB unresponsiveness", "GDB unresponsiveness");
//                break;
//            }
//        }
//        listCommands.add(command);
//        logger.debug("Get the GDB output done");
//        return res;
    }

    private GDBTime timeSearch(String res) {
        if (!res.trim().endsWith(OutputSyntax.END_LOG.getSyntax().trim())) {
            return null;
        }
        else if (Utils.isWindows()) {
            final String TIME_TAG = "time=";
            int startIdx = res.lastIndexOf(TIME_TAG);
            int endIdx = res.indexOf("}", startIdx);
            String json = res.substring(startIdx + TIME_TAG.length(), endIdx + 1)
                    .replace("=", ":");
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            return gson.fromJson(json, GDBTime.class);
        } else {
            int idx = res.lastIndexOf("time=");
            int size = res.length() - 1;
            if (idx == -1 || idx + END_STRING_LENGTH != size) {
                if (res.contains(OutputSyntax.ERROR_RESULT.getSyntax())) {
                    return new GDBTime();
                }
                return null;
            }
            int end = res.lastIndexOf("\n" + OutputSyntax.END_LOG.getSyntax() + "\n");

            String json = res.substring(idx + 5, end).replace("=", ":");
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            return gson.fromJson(json, GDBTime.class);
        }
    }

//    public synchronized void runCommandInGdb(CurprocessThread process, String command) {
//        process.setCommand(command);
//        process.setGdb_mi_enabled(true);
//        // Force the gdb debugger thread to wait for current process thread
//        int MAX_WAITING = 100;
//        while (currentProcess.isGdb_mi_enabled() && MAX_WAITING >= 0) {
//            try {
//                Thread.sleep(10);
//                logger.debug("Waiting for [CurprocessThread] to execute the command " + command + " successfully");
//                MAX_WAITING--;
//            } catch (InterruptedException ex) {
//                ex.printStackTrace();
//                UIController.showErrorDialog("Unable to get the result when executing the following command in gdb: " + command + "\n",
//                        "GDB unresponsiveness", "GDB unresponsiveness");
//                break;
//            }
//        }
//        if (MAX_WAITING < 0)
//            UIController.showErrorDialog("Unable to get the result when executing the following command in gdb: " + command + "\n",
//                    "GDB unresponsiveness", "GDB unresponsiveness");
//    }

    public GDBStatus beginDebug() {
        DebugController.getDebugController().stop();
        DebugController.getDebugController().resetPathHit(testCase);
        logger.debug("[GDB Thread] Pressing start debug");
//        String gtestReport = " --gtest_output=" + String.format("xml:%s", testCase.getExecutionResultFile());
        String cmd = "run";// + gtestReport;
        String output = executeAndLog(cmd);
        return new OutputAnalyzer().analyze(output, GDB_RUN);
    }

    public GDBStatus nextBr() {
        logger.debug("[GDB thread] Pressing next breakpoint");
        String output = executeAndLog(GDB_NEXT);
        return new OutputAnalyzer().analyze(output, GDB_NEXT);
    }

    public GDBStatus nextLine() {
        logger.debug("[GDB thread] Pressing next line");
        String output = executeAndLog(GDB_NEXT_LINE);
        return new OutputAnalyzer().analyze(output, GDB_NEXT_LINE);
    }

    public GDBStatus stepIn() {
        logger.debug("[GDB Thread] Pressing step into function");
        String output = executeAndLog(GDB_STEP_IN);
        return new OutputAnalyzer().analyze(output, GDB_STEP_IN);
    }

    public GDBStatus stepOut() {
        logger.debug("[GDB Thread] Pressing step out function");
        String output = executeAndLog(GDB_STEP_OUT);
        return new OutputAnalyzer().analyze(output, GDB_STEP_OUT);
    }

    public GDBStatus kill() {
        logger.debug("[GDB Thread] Pressing stop debugging");
        String output = executeAndLog(GDB_KILL);
        return new OutputAnalyzer().analyze(output, GDB_KILL);
    }

    /**
     * This function add new watch point that will make program stop when value of expression changed
     *
     * @param exp expression
     * @return tree item with GDBVar
     */
    public TreeItem<GDBVar> addNewWatch(String exp) {
        logger.debug("[GDB Thread] Add new watch to GDB");
        String output = executeAndLog(GDB_ADD_WATCH + exp);
        OutputGDB outputGDB = new OutputAnalyzer().analyzeOutput(output, GDB_ADD_WATCH);
        if (outputGDB == null) {
            UIController.showErrorDialog("Can not add watch", "Error", "Try again");
            return null;
        }
        if (outputGDB.isError()) {
            String msg = JsonParser.parseString(outputGDB.getJson()).getAsJsonObject().get("msg").getAsString();
            UIController.showErrorDialog(msg, "Error", "Try again");
            return null;
        } else {
            WatchPoint watch = new OutputAnalyzer().analyzeWatchPoint(outputGDB);
            String out = executeAndLog(GDB_CREATE_VARIABLE + exp + GDB_END_STRING);
            GDBVar var = new OutputAnalyzer().analyzeInternalVariable(out);
            if (var != null) {
                var.setRealName(exp);
                var.setWatchPoint(watch);
                return createItem(var);
            } else return null;
        }
    }

    /**
     * This function add a normal watch that just show value of the expression. In this case, the debugging program
     * will not stop even when the value of the expression changed
     *
     * @param exp expression
     * @return a item which contains a variable
     */
    public TreeItem<GDBVar> addNormalWatch(String exp) {
        logger.debug("[GDB Thread] Add normal watch point");
        String out = executeAndLog(GDB_CREATE_VARIABLE + exp + GDB_END_STRING);
        GDBVar var = new OutputAnalyzer().analyzeInternalVariable(out);
        if (var != null) {
            var.setRealName(exp);
            return createItem(var);
        } else return null;
    }

    /**
     * This function delete an advanced watch point in debugging
     *
     * @param num id of the watch point
     * @return true if delete successfully
     */
    public boolean deletePoint(String num) {
        logger.debug("[GDB Thread] Delete an advanced watch point");
        String output = executeAndLog(GDB_DEL_POINT + num);
        OutputGDB outputGDB = new OutputAnalyzer().analyzeOutput(output, GDB_DEL_POINT);
        return outputGDB != null;
    }

    /**
     * This function delete a variable in debugging
     *
     * @param var variable is to be deleted
     * @return true if delete successfully
     */
    public boolean deleteVariable(GDBVar var) {
        logger.debug("[GDB Thread] Delete a variable in debugging");
        String output = executeAndLog(GDB_DELETE_VARIABLE + var.getName());
        OutputGDB outputGDB = new OutputAnalyzer().analyzeOutput(output, GDB_DELETE_VARIABLE);
        return outputGDB != null;
    }

    public TreeItem<GDBVar> showVar(String variableName) {
        String output = executeAndLog(GDB_CREATE_VARIABLE + variableName + GDB_END_STRING);
        GDBVar var = new OutputAnalyzer().getVarCreated(output);
        if (var != null) {
            var.setRealName(variableName);
            TreeItem<GDBVar> item = createItem(var);
            executeAndLog(GDB_DELETE_VARIABLE + var.getName());
            return item;
        }
        return null;
    }

    public TreeItem<GDBVar> getTreeItemVarWithExp(String exp) {
        String output = executeAndLog(GDB_CREATE_VARIABLE + exp + GDB_END_STRING);
        GDBVar var = new OutputAnalyzer().getVarCreated(output);
        if (var != null) {
            var.setRealName(exp);
            return createItem(var);
        }
        return null;
    }

    public void updateVariable(GDBVar item, TreeView<GDBVar> treeView) {
        TreeItem<GDBVar> newItem = createItem(item);
        TreeItem<GDBVar> varTreeItem = treeView.getSelectionModel().getSelectedItem();
        int index = varTreeItem.getParent().getChildren().indexOf(varTreeItem);
        varTreeItem.getParent().getChildren().add(index + 1, newItem);
        varTreeItem.getParent().getChildren().remove(index);

    }

    public void assignVariable(GDBVar item, String value, TreeView<GDBVar> treeView) {
        String name = item.getName();
        String realName = item.getRealName();
        String output;
        value = "\"" + value + "\"";
        if (name.equals(realName)) {
            String out = executeAndLog(GDB_CREATE_VARIABLE + name + GDB_END_STRING);
            GDBVar var = new OutputAnalyzer().analyzeInternalVariable(out);
            output = executeAndLog(GDB_ASSIGN_VARIABLE + var.getName() + " " + value);
            item = var;
            item.setRealName(realName);
        } else {
            output = executeAndLog(GDB_ASSIGN_VARIABLE + name + " " + value);
        }
        OutputGDB outputGDB = new OutputAnalyzer().analyzeOutput(output, GDB_ASSIGN_VARIABLE);
        if (outputGDB == null) {
            UIController.showErrorDialog("Can not assign value", "Error", "Try again");
        } else {
            logger.debug("OutputGDB:" + outputGDB.toString());
            if (outputGDB.isError()) {
                String msg = JsonParser.parseString(outputGDB.getJson()).getAsJsonObject().get("msg").getAsString();
                UIController.showErrorDialog(msg, "Error", "Try again");
            } else {
                String newValue = JsonParser.parseString(outputGDB.getJson()).getAsJsonObject().get("value").getAsString();
                item.setValue(newValue);
                TreeItem<GDBVar> newItem = createItem(item);
                TreeItem<GDBVar> varTreeItem = treeView.getSelectionModel().getSelectedItem();
                int index = varTreeItem.getParent().getChildren().indexOf(varTreeItem);
                varTreeItem.getParent().getChildren().add(index + 1, newItem);
                varTreeItem.getParent().getChildren().remove(index);
                checkUpdateVar(newItem.getValue());
            }
        }
    }

    private void checkUpdateVar(GDBVar newVar) {
        String output = executeAndLog(GDB_VARIABLE_UPDATE);
        String expr = newVar.getRealName();
        String name = newVar.getName();
        System.out.println(output);
        OutputGDB outputGDB = new OutputAnalyzer().analyzeOutput(output, GDB_VARIABLE_UPDATE);
        if (outputGDB.isError()) {
            logger.debug("Checking updated variables failed");
        } else {
            logger.debug("Checking updated variables");
            JsonArray jsonArray = JsonParser.parseString(outputGDB.getJson()).getAsJsonObject().get("changelist").getAsJsonArray();
            Gson gson = new Gson();
            for (int i = 0; i < jsonArray.size(); i++) {
                GDBVarChange change = gson.fromJson(jsonArray.get(i).getAsJsonObject(), GDBVarChange.class);
                if (change.getName().equals(name)) {
                    change.setRealName(expr);
                    this.listVarChange.add(change);
                }
            }
            logger.debug("Saved changed on variables");
        }

    }

    public OutputGDB selectFrame(GDBFrame frame) {
        logger.debug("[GDB thread] Change the current frame");
        String output = executeAndLog(GDB_SELECT_FRAME + " " + frame.getLevel());
        return new OutputAnalyzer().analyzeOutput(output, GDB_SELECT_FRAME);
    }


    public ArrayList<GDBFrame> getFrames() {
        logger.debug("[GDB thread] Checking frame at this breakpoint hit");
        String output = executeAndLog(GDB_FRAME_LIST);
        return new OutputAnalyzer().analyzeFrames(output, GDB_FRAME_LIST);
    }

    /**
     * Call GDB to get all variables in local function and build a variable tree
     * GDB_COMMAND = GDB_FRAME_VARIABLES
     *
     * @return A variable tree
     */
    public ArrayList<TreeItem<GDBVar>> buildTreeVars() {
        logger.debug("[GDB thread] Checking local variables at this breakpoint hit");
        String output = executeAndLog(GDB_FRAME_VARIABLES);
        ArrayList<GDBVar> vars = new OutputAnalyzer().analyzeVariables(output, GDB_FRAME_VARIABLES);
        ArrayList<TreeItem<GDBVar>> res = new ArrayList<>();
        vars.forEach(e -> {
            e.setRealName(e.getName());
            res.add(createItem(e));
        });
        return res;
    }

    private TreeItem<GDBVar> createItem(GDBVar var) {
        String type = var.getType();
        int sizeOfArray = var.getSize();
        TreeItem<GDBVar> res = new TreeItem<>(var);
        String realName = var.getRealName();
        if (type != null && (type.contains("*") || (type.contains("[") && var.getValue() != null) || var.getValue() == null)) {
            String castName = realName;
            if (type.contains("*")) {
                if (sizeOfArray > 0) {
                    String newType = type.replaceAll("\\*", "[" + sizeOfArray + "]").replaceAll("\\(", "").replaceAll("\\)", "");
                    long count = newType.chars().filter(x -> x == '[').count();
                    castName = "(" + newType + ") " + String.join("", Collections.nCopies((int) count, "*")) + castName;
                }
            }
            if (var.getValue() != null && type.contains("[")) {
                if (sizeOfArray > 0) {
                    int start = type.indexOf("[");
                    int end = type.indexOf("]");
                    long count = type.chars().filter(x -> x == '[').count();
                    castName = "(" + type.substring(0, start + 1) + sizeOfArray + type.substring(end) + ")" + String.join("", Collections.nCopies((int) count, "*")) + castName;
                }
            }
            String output = executeAndLog(GDB_CREATE_VARIABLE + castName + GDB_END_STRING);
            GDBVar newVar = new OutputAnalyzer().analyzeInternalVariable(output);
            newVar.setWatchPoint(var.getWatchPoint());
            newVar.setRealName(realName);
            newVar.setStartIdx(var.getStartIdx());
            newVar.setEndIdx(var.getEndIdx());
            res = createTreeItem(newVar);
        }
        return res;
    }
    private static final int CHILDREN_MAXIMUM_SIZE = 128;
    private TreeItem<GDBVar> createTreeItem(GDBVar var) {
        TreeItem<GDBVar> current = new TreeItem<>(var);
        if (var.getNumchild() > 0 || var.getDynamic() != null) {
            boolean isAnalyzeChildren = true;
            if(VariableTypeUtils.isOneDimension(var.getType()) || VariableTypeUtils.isMultipleDimension(var.getType())) {
                if (var.getNumchild() > CHILDREN_MAXIMUM_SIZE)
                    isAnalyzeChildren = false;
            }
            if (isAnalyzeChildren) {
                String childOutput = executeAndLog(GDB_GET_CHILD_VARIABLE + var.getName() + GDB_END_STRING);
                ArrayList<GDBVar> children = new OutputAnalyzer().analyzeChildInternalVariable(childOutput);
                children.forEach(child -> {
                    int idx = children.indexOf(child);
                    String name = child.getName();
                    int limitStack = (int) name.chars().filter(ch -> ch == '.').count();
                    if (limitStack < 10) {
                        if ((idx >= var.getStartIdx() && idx <= var.getEndIdx()) || (var.getEndIdx() == 0 && var.getStartIdx() == 0)) {
                            child.setRealName(name.substring(name.lastIndexOf(".") + 1));
                            TreeItem<GDBVar> childItem = createTreeItem(child);
                            current.getChildren().add(childItem);
                        }
                    }
                });
            }
        }
        return current;
    }

    @Override
    public GDBStatus call() {
        try {
            GDBProcess curProcess = this.startDebugProcess(this.testCase);
            if (curProcess == null) {
                DebugController.getDebugController().turnOff();
            } else {
//                curProcess.setGdb_mi_enabled(false);
                this.currentProcess = curProcess;
                this.skipAllFileCreatedByAka();
                this.setupLogging();
                this.setPrettyPrinting();
                this.setBreakpoints();
            }
        } catch (Exception e) {
            logger.debug("Can not call GDB");
            e.printStackTrace();
        }

        return null;
    }

    private void skipAllFileCreatedByAka() {
        // skip test driver
        String testDrivePath =  testCase.getSourceCodeFile();
        testDrivePath = Utils.normalizePath(testDrivePath);
        testDrivePath = new File(testDrivePath).getName();
        currentProcess.executeAndLog(GDB_SKIP_FILE + testDrivePath);
//        runCommandInGdb(currentProcess, GDB_SKIP_FILE + "\"" + testCase.getSourceCodeFile() + "\"");
//        runCommandInGdb(currentProcess,GDB_SKIP_FUNCTION + "\"" + testCase.getSourceCodeFile() + "\"");
        // skip stub codes
//        for (String stubCodePath : StubManager.getFileList().values()) {
//            executeAndLog(GDB_SKIP_FILE + stubCodePath);
//        }
    }

    private void setupLogging() {
        String logPath = new WorkspaceConfig().fromJson().getDebugCommandsDirectory() + "/" + testCase.getName() + ".log";
        this.logPath = logPath;
        currentProcess.executeAndLog(GDB_SET + "logging file \"" + logPath + "\"");
        currentProcess.executeAndLog(GDB_SET + "logging redirect on");
        currentProcess.executeAndLog(GDB_SET + "logging overwrite on");

//        runCommandInGdb(currentProcess, GDB_SET + "logging file \"" + logPath + "\"");
//        runCommandInGdb(currentProcess, GDB_SET + "logging redirect on");
//        runCommandInGdb(currentProcess, GDB_SET + "logging overwrite on");
    }

    private void setPrettyPrinting() {
        logger.debug("[GDB thread] Enable pretty printing");
        currentProcess.executeAndLog("-enable-pretty-printing");
//        runCommandInGdb(currentProcess, "-enable-pretty-printing");
        logger.debug("[GDB thread] Enable timings printing");
//        runCommandInGdb(currentProcess, "-enable-timings");
        currentProcess.executeAndLog("-enable-timings");
    }

    public ITestCase getTestcase() {
        return testCase;
    }

    public void setTestcase(ITestCase testcase) {
        this.testCase = testcase;
    }

    public void setBreakPointMap(ObservableMap<String, TreeSet<BreakPoint>> breakPointMap) {
        this.breakPointMap = breakPointMap;
    }

    public boolean isExecuting() {
        return this.isExecuting.get();
    }

    public void setExecuting(boolean isExecuting) {
        this.isExecuting.set(isExecuting);
    }

    public void logAllCommands() {
        StringBuilder builder = new StringBuilder();
        builder.append(String.join("\n", listCommands));
        String filePath = new WorkspaceConfig().fromJson().getDebugDirectory() + File.separator + "last_commands.log";
        Utils.writeContentToFile(builder.toString(), filePath);
    }

    public GDBProcess getCurrentProcess() {
        return currentProcess;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }
}