package com.dse.regression;

import com.dse.cli.command.ICommand;
import com.dse.cli.command.Run;
import com.dse.config.WorkspaceConfig;
import com.dse.guifx_v3.controllers.main_view.BaseSceneController;
import com.dse.regression.objects.RegressionScript;
import com.dse.testcase_manager.CompoundTestCase;
import com.dse.testcase_manager.ITestCase;
import com.dse.testcase_manager.TestCase;
import com.dse.testcasescript.object.TestSubprogramNode;
import com.dse.testdata.object.IDataNode;
import com.dse.testdata.object.RootDataNode;
import com.dse.testdata.object.UnitUnderTestNode;
import com.dse.thread.AkaThread;
import com.dse.thread.RunRegressionScriptTask;
import com.dse.logger.AkaLogger;
import com.dse.util.Utils;

import java.io.*;
import java.util.*;

public class RegressionScriptManager {
    private final static AkaLogger logger = AkaLogger.get(RegressionScriptManager.class);

    private static String REGRESSION_SCRIPT_FOLDER = new WorkspaceConfig().fromJson().getRegressionScriptDirectory();

    /**
     * Singleton partern
     */
    private static RegressionScriptManager instance = null;

    private RegressionScriptManager() {
    }

    public static RegressionScriptManager getInstance() {
        if (instance == null) {
            instance = new RegressionScriptManager();
        }
        return instance;
    }

    private Map<String, RegressionScript> nameToRegressionScript = new HashMap<>();

    public void clear() {
        nameToRegressionScript.clear();
    }

    public String getRegressionScriptFilePath(RegressionScript regressionScript) {
        return REGRESSION_SCRIPT_FOLDER + File.separator + regressionScript.getName() + ".rs";
    }

    public void deleteRegressionScriptFile(RegressionScript regressionScript) {
        Utils.deleteFileOrFolder(new File(regressionScript.getScriptFilePath()));
    }

    public RegressionScript getRegressionScriptByName(String name) {
        return nameToRegressionScript.getOrDefault(name, null);
    }

    public void runRegressionScript(RegressionScript regressionScript) {
        BaseSceneController.getBaseSceneController().viewMessagesTab(regressionScript);
        RunRegressionScriptTask task = new RunRegressionScriptTask(regressionScript);
        new AkaThread(task).start();
    }

    public void exportRegressionScript(RegressionScript regressionScript) {
        String path = REGRESSION_SCRIPT_FOLDER + File.separator + regressionScript.getName() + ".rs";
        String content = exportRegressionScriptToString(regressionScript.getTestCases());
        Utils.writeContentToFile(content, path);
    }

    public String exportRegressionScriptToString(List<ITestCase> testCases) {
        if (testCases.size() == 0) return " ";
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < testCases.size(); i++) {
            ITestCase iTestCase = testCases.get(i);
            if (iTestCase instanceof TestCase) {
                TestCase testCase = (TestCase) iTestCase;
                RootDataNode rootDataNode = testCase.getRootDataNode();
                UnitUnderTestNode unitUnderTestNode = null;
                for (IDataNode dataNode : rootDataNode.getChildren()) {
                    if (dataNode instanceof UnitUnderTestNode) {
                        unitUnderTestNode = (UnitUnderTestNode) dataNode;
                    }
                }
                if (unitUnderTestNode != null) {
                    String unitName = unitUnderTestNode.getName();
                    String subprogramName = testCase.getFunctionNode().getSimpleName();
                    builder.append("run -u=").append(unitName).append(" ");
                    builder.append("-s=").append(subprogramName).append(" ");
                    builder.append("-t=").append(testCase.getName());
                } else {
                    logger.debug("The RootDataNode has no UnitUnderTestNode.");
                }
            } else if (iTestCase instanceof CompoundTestCase) {
                String unitName = TestSubprogramNode.COMPOUND_SIGNAL;
                String subprogramName = TestSubprogramNode.COMPOUND_SIGNAL;
                builder.append("run -u=").append(unitName).append(" ");
                builder.append("-s=").append(subprogramName).append(" ");
                builder.append("-t=").append(iTestCase.getName());
            }

            if (i < testCases.size() - 1) {
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    // called when open existed Environment or after creating an Environment
    public void loadRegressionScripts() {
        File regressionScriptDirectory = new File(REGRESSION_SCRIPT_FOLDER);
        FilenameFilter rsFilter = (f, name) -> name.endsWith("rs");
        for (File file : Objects.requireNonNull(regressionScriptDirectory.listFiles(rsFilter))) {
            RegressionScript regressionScript = importRegressionScript(file);
            add(regressionScript);
        }
    }
    public RegressionScript importRegressionScript(File scriptPath) {
        RegressionScript regressionScript = new RegressionScript();
        regressionScript.setName(scriptPath.getName().replace(".rs", ""));
        List<ITestCase> iTestCases = parseScriptToGetTestCases(scriptPath);
        for (ITestCase iTestCase : iTestCases) {
            regressionScript.getTestCases().add(iTestCase);
        }
        return regressionScript;
    }

    private List<ITestCase> parseScriptToGetTestCases(File scriptPath) {
        List<ITestCase> testCases = new ArrayList<>();
        List<String> commands = readData(scriptPath.getAbsolutePath());
        for (String cmd : commands) {
            logger.debug("Loading script: " + cmd);
            try {
                ICommand<?> command = ICommand.parse(cmd);
                if (command instanceof Run) {
                    testCases.addAll(((Run) command).collectTestCases());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                logger.error(ex.getMessage());
            }
        }
        return testCases;
    }

    /**
     * Read data from file path
     *
     * @param path path to file
     * @return data in string
     */
    private static List<String> readData(String path) {
        List<String> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {

            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                data.add(sCurrentLine);
            }

        } catch (IOException e) {
            // // e.printStackTrace();
        }
        return data;
    }
    public boolean add(RegressionScript regressionScript) {
        if (!nameToRegressionScript.containsKey(regressionScript.getName())) {
            nameToRegressionScript.put(regressionScript.getName(), regressionScript);
            return true;
        } else {
            logger.debug("The name of the regression script is existed. " + regressionScript.getName());
            return false;
        }
    }

    public void remove(RegressionScript regressionScript) {
        nameToRegressionScript.remove(regressionScript.getName());
    }

    public List<RegressionScript> getAllRegressionScripts() {
        return new ArrayList<>(nameToRegressionScript.values());
    }
}
