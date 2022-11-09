package com.dse.coverage.gcov;

import com.dse.compiler.Compiler;
import com.dse.compiler.Terminal;
import com.dse.config.AkaConfig;
import com.dse.config.CommandConfig;
import com.dse.config.WorkspaceConfig;
import com.dse.environment.Environment;
import com.dse.guifx_v3.helps.UIController;
import com.dse.logger.AkaLogger;
import com.dse.parser.IProjectLoader;
import com.dse.parser.object.CppFileNode;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.INode;
import com.dse.testcase_execution.TestcaseExecution;
import com.dse.testcase_execution.testdriver.LcovTestDriverGenerationForC;
import com.dse.testcase_execution.testdriver.LcovTestDriverGenerationForCpp;
import com.dse.testcase_execution.testdriver.TestDriverGeneration;
import com.dse.testcase_execution.testdriver.TestDriverGenerationForC;
import com.dse.testcase_manager.*;
import com.dse.thread.AbstractAkaTask;
import com.dse.util.*;
import javafx.scene.control.Alert;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.dse.coverage.gcov.GcovInfo.changeReportContent;

public class LCOVTestReportGeneration extends AbstractAkaTask<GcovInfo> {

    private final static AkaLogger logger = AkaLogger.get(LCOVTestReportGeneration.class);
    private static final String LCOV_DIR = "lcov";
    private static final String LCOV_DATA_FILE = LCOV_DIR + File.separator + "lcovCommand.dat";
    private static final String CSS_FILE_NAME = "gcov.css";
    private static final String HTML_REPORT_NAME = "index.html";
    private static final String ERROR_TAG = " ERROR: ";
    private final ITestCase testCase;
    private GcovInfo gcovInfo;

    public LCOVTestReportGeneration(ITestCase _testCase) {
        this.testCase = _testCase;
    }

    @Override
    protected GcovInfo call() throws Exception {
        init();
        LCOVReportGenerate();
        return gcovInfo;
    }

    private void init() throws Exception {
        gcovInfo = new GcovInfo();

        TestcaseExecution execution = new TestcaseExecution();
        execution.initializeConfigurationOfTestcase(testCase);
        execution.initializeCommandConfigToRunTestCase(testCase);

        File containerFolder = new File(testCase.getSourceCodeFile()).getParentFile();
        String path = containerFolder.getAbsolutePath();


        String cmdDataPath = path.replace(WorkspaceConfig.TEST_DRIVER_FOLDER_NAME, LCOV_DATA_FILE);
        this.gcovInfo.setCmdDataPath(cmdDataPath);
        logger.debug("Command Data Path: " + cmdDataPath);

        String lcovTestCaseDir = LCOV_DIR + File.separator + testCase.getName();
        path = path.replace(WorkspaceConfig.TEST_DRIVER_FOLDER_NAME, lcovTestCaseDir);
        logger.info("Path: " + path);
        logger.info("Test Case Name: " + testCase.getName());

        this.gcovInfo.setDirectory(path);
        this.gcovInfo.setRootFilePath(path);

        String reportDirPath = path + File.separator + testCase.getName();
        this.gcovInfo.setReportDirPath(reportDirPath);

        String reportPath = reportDirPath + File.separator + HTML_REPORT_NAME;
        this.gcovInfo.setReportPath(reportPath);

        String cssPath = reportDirPath + File.separator + CSS_FILE_NAME;
        this.gcovInfo.setCssPath(cssPath);

        this.gcovInfo.setUpCommand();
        logger.debug("Command={" + "\n" +
                this.gcovInfo.getCompileGcdaFlag() + ",\n" +
                this.gcovInfo.getCompileGcnoFlag() + ",\n" +
                this.gcovInfo.getLinkingGcdaFlag() + ",\n" +
                this.gcovInfo.getLinkingGcnoFlag() + ",\n" +
                this.gcovInfo.getLcovCommand() + ",\n" +
                this.gcovInfo.getLcovBranch() + ",\n" +
                this.gcovInfo.getLcovInputDirFlag() + ",\n" +
                this.gcovInfo.getLcovOutputDirFlag() + ",\n" +
                this.gcovInfo.getGenerateCommand() + ",\n" +
                this.gcovInfo.getGenerateBranchFlag() + ",\n" +
                this.gcovInfo.getGenerateOutputDirFlag() + "}\n");

        TestDriverGeneration lcovTestDriver = generateTestDriver();
        lcovTestDriver.setTestCase(testCase);

        lcovTestDriver.generate();
        String testDriverContent = lcovTestDriver.getTestDriver();
        String filePath = path + File.separator + testCase.getName() + SpecialCharacter.DOT + LCOV_DIR;

        if (lcovTestDriver instanceof TestDriverGenerationForC) {
            filePath += IProjectLoader.C_FILE_SYMBOL;
        } else {
            filePath += IProjectLoader.CPP_FILE_SYMBOL;
        }

        logger.debug("Lcov path: " + filePath);
        gcovInfo.setLcovCPath(filePath);

        Utils.writeContentToFile(testDriverContent, filePath);
    }

    private TestDriverGeneration generateTestDriver() {
        ICommonFunctionNode fn = null;

        if (testCase instanceof TestCase)
            fn = ((TestCase) testCase).getFunctionNode();
        else {
            List<TestCaseSlot> slots = ((CompoundTestCase) testCase).getSlots();
            if (!slots.isEmpty()) {
                String name = slots.get(0).getTestcaseName();
                TestCase testCase = TestCaseManager.getBasicTestCaseByName(name);
                fn = testCase.getFunctionNode();
            }
        }

        if (fn != null) {
            INode source = Utils.getSourcecodeFile(fn);
            if (source instanceof CppFileNode) {
                return new LcovTestDriverGenerationForCpp();
            }
        }

        return new LcovTestDriverGenerationForC();
    }

    public void LCOVReportGenerate() throws Exception {
        AddFlag();
        GenerateReport();
        String content = Utils.readFileContent(gcovInfo.getReportPath());
        content = changeReportContent(content, gcovInfo.getReportDirPath());
        this.gcovInfo.setReportContent(content);
    }

    private void AddFlag() throws Exception {
        CommandConfig conf = new CommandConfig().fromJson(testCase.getCommandConfigFile());
        Compiler compiler = Environment.getInstance().getCompiler();
        List<String> compileCommands = new ArrayList<>(conf.getCompilationCommands().values());

        String newName = testCase.getName() + SpecialCharacter.DOT + LCOV_DIR;
        String lcovTestCaseDir = LCOV_DIR + File.separator + testCase.getName();

        String compileCommand1 = compileCommands.get(0)
                .replace(testCase.getName(), newName)
                .replace(WorkspaceConfig.TEST_DRIVER_FOLDER_NAME, lcovTestCaseDir);
        String compileCommand = compileCommand1 + SpecialCharacter.SPACE
                + gcovInfo.getCompileGcdaFlag() + SpecialCharacter.SPACE
                + gcovInfo.getCompileGcnoFlag();

        String workspace = new AkaConfig().fromJson().getOpeningWorkspaceDirectory();
        String directory = new File(workspace).getParentFile().getParentFile().getPath();
        logger.debug("Directory: " + directory);

        String linkingCommand1 = conf.getLinkingCommand()
                .replace(testCase.getName(), newName)
                .replace(WorkspaceConfig.TEST_DRIVER_FOLDER_NAME, lcovTestCaseDir);
        String LinkingCommand = linkingCommand1 + SpecialCharacter.SPACE
                + gcovInfo.getLinkingGcdaFlag() + SpecialCharacter.SPACE
                + gcovInfo.getLinkingGcnoFlag();

        String exePath = conf.getExecutablePath().replace(testCase.getName(), newName);

        String[] compileScript = CompilerUtils.prepareForTerminal(compiler, compileCommand);
        Terminal terCompile = new Terminal(compileScript, directory);
        logger.debug("Compile: " + compileCommand);
        logger.debug(terCompile.get());

        String[] linkScript = CompilerUtils.prepareForTerminal(compiler, LinkingCommand);
        Terminal terLinkage = new Terminal(linkScript, directory);
        logger.debug("Linking: " + LinkingCommand);
        logger.debug(terLinkage.get());

        new Terminal(exePath, directory);
        logger.debug("Run exe file path: " + exePath);
        logger.debug("In directory " + directory);
    }

    private static final String INFO_FILE_EXT = ".lcov_main_coverage.info";

    private void GenerateReport() {
        String workspace = new AkaConfig().fromJson().getOpeningWorkspaceDirectory();
        String directory = new File(workspace).getParentFile().getParentFile().getPath();

        String res = SpecialCharacter.EMPTY;

        // STEP 1: GCOV
        String gcovCommand = gcovInfo.getGcovCommand() + SpecialCharacter.SPACE + SpecialCharacter.DOUBLE_QUOTES + gcovInfo.getLcovCPath() + SpecialCharacter.DOUBLE_QUOTES;
        logger.debug("Gcov Request: " + gcovCommand);
        try {
            Terminal terGcov = new Terminal(gcovCommand, directory);
            logger.debug("Gcov Response: " + terGcov.get());
            res += terGcov.get() + SpecialCharacter.LINE_BREAK;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        // STEP 2: LCOV
        String infoFilePath = gcovInfo.getDirectory() + File.separator + testCase.getName() + INFO_FILE_EXT;

        //"lcov --rc lcov_branch_coverage=1 --capture --directory " + " --output-file"
        String lcovCommand = gcovInfo.getLcovCommand() + SpecialCharacter.SPACE
                + gcovInfo.getLcovBranch() + SpecialCharacter.SPACE
                + gcovInfo.getLcovInputDirFlag() + SpecialCharacter.SPACE
                + SpecialCharacter.DOUBLE_QUOTES + gcovInfo.getDirectory() + SpecialCharacter.DOUBLE_QUOTES + SpecialCharacter.SPACE
                + gcovInfo.getLcovOutputDirFlag() + SpecialCharacter.SPACE
                + SpecialCharacter.DOUBLE_QUOTES + infoFilePath + SpecialCharacter.DOUBLE_QUOTES;
        logger.debug("lcov Request: " + lcovCommand);
        try {
            Terminal terLcov = new Terminal(lcovCommand);
            logger.debug("lcov Response: " + terLcov.get());
            res += terLcov.get() + SpecialCharacter.LINE_BREAK;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        // STEP 3: Gen html report
        //"genhtml --branch-coverage "  " --output-directory "
        String genhtmlCommand = gcovInfo.getGenerateCommand() + SpecialCharacter.SPACE
                + gcovInfo.getGenerateBranchFlag() + SpecialCharacter.SPACE
                + SpecialCharacter.DOUBLE_QUOTES + infoFilePath + SpecialCharacter.DOUBLE_QUOTES + SpecialCharacter.SPACE
                + gcovInfo.getGenerateOutputDirFlag() + SpecialCharacter.SPACE
                + SpecialCharacter.DOUBLE_QUOTES + gcovInfo.getDirectory() + File.separator + testCase.getName() + SpecialCharacter.DOUBLE_QUOTES;
        logger.debug("GenHtml Request: " + genhtmlCommand);
        try {
            Terminal terGenHtml = new Terminal(genhtmlCommand);
            logger.debug("GenHtml Response: " + terGenHtml.get());
            res += terGenHtml.get();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        if (res.contains(ERROR_TAG)) {
            String title = "Cant generate LCOV report";
            String headText = "Gcov & Lcov log below";
            UIController.showDetailDialog(Alert.AlertType.ERROR, title, headText, res);
        }
    }

    public GcovInfo getInfo() {
        return gcovInfo;
    }
}
