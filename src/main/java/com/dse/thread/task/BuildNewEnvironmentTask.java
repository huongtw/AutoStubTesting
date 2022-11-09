package com.dse.thread.task;

import com.dse.config.AkaConfig;
import com.dse.config.WorkspaceConfig;
import com.dse.coverage.InstructionComputator;
import com.dse.environment.Environment;
import com.dse.coverage.gcov.LcovWorkspaceConfig;
import com.dse.environment.EnvironmentSearch;
import com.dse.environment.WorkspaceCreation;
import com.dse.environment.object.EnviroSBFNode;
import com.dse.environment.object.EnviroUUTNode;
import com.dse.environment.object.EnvironmentRootNode;
import com.dse.environment.object.IEnvironmentNode;
import com.dse.guifx_v3.controllers.build_environment.AbstractCustomController;
import com.dse.guifx_v3.controllers.build_environment.BaseController;
import com.dse.guifx_v3.controllers.build_environment.UserCodeController;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.INode;
import com.dse.parser.object.ISourcecodeFileNode;
import com.dse.parser.object.ParseToLambdaFunctionNode;
import com.dse.parser.systemlibrary.SystemHeaderParser;
import com.dse.project_init.ProjectClone;
import com.dse.search.LambdaFunctionNodeCondition;
import com.dse.search.Search;
import com.dse.search.condition.AbstractFunctionNodeCondition;
import com.dse.search.condition.MacroFunctionNodeCondition;
import com.dse.search.condition.SourcecodeFileNodeCondition;
import com.dse.testcasescript.object.*;
import com.dse.thread.AbstractAkaTask;
import com.dse.user_code.UserCodeManager;
import com.dse.user_code.envir.EnvironmentUserCode;
import com.dse.logger.AkaLogger;
import com.dse.util.PathUtils;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.dse.guifx_v3.controllers.build_environment.BaseController.*;

public class BuildNewEnvironmentTask extends AbstractAkaTask<BuildEnvironmentResult> {

    private static final AkaLogger logger = AkaLogger.get(BuildNewEnvironmentTask.class);

    private BuildEnvironmentResult result = new BuildEnvironmentResult();

    private final boolean shouldCompile;

    public BuildNewEnvironmentTask() {
        shouldCompile = true;
    }

    public BuildNewEnvironmentTask(boolean shouldCompile) {
        this.shouldCompile = shouldCompile;
    }

    @Override
    protected BuildEnvironmentResult call() {
        if (Environment.getInstance().getProjectNode() == null) {
            result.setExitCode(BUILD_NEW_ENVIRONMENT.FAILURE.OTHER);
            return result;
        }

        /*
         * Point the current workspace to the creating workspace.
         *
         * new WorkspaceConfig().fromJson() will return the configuration of creating workspace
         */
        saveCreatingWorkspaceToAkaConfig();

        // initialize workspace (just create necessary folder) FIRSTLY (mandatory)
        String workspace = ".." + File.separator + WorkspaceConfig.WORKING_SPACE_NAME
                + File.separator + Environment.getInstance().getName();
        initializeRelativeWorkspace(workspace);

        exportUserCodes();

        if (shouldCompile) {
            // compile all
            int isCompiledSuccessfully = compileTheTestedProject(Environment.getInstance().getProjectNode());
            if (isCompiledSuccessfully != BaseController.BUILD_NEW_ENVIRONMENT.SUCCESS.COMPILATION) {
                result.setExitCode(isCompiledSuccessfully);
                return result;
            }

            logger.debug("The project " + Environment.getInstance().getProjectNode().getAbsolutePath() + " is compiled successfully");
        }

        // export physical_tree.json, dependencies, elements, etc. to initialized working space
        WorkspaceConfig wkConfig = new WorkspaceConfig().fromJson();
        BaseController.addAnalysisInformationToWorkspace(workspace,
                wkConfig.getDependencyDirectory(),
                wkConfig.getPhysicalJsonFile(),
                wkConfig.getElementDirectory());

        File envFile = new File(new WorkspaceConfig().fromJson().getEnvironmentFile());
        EnvironmentRootNode envRoot = Environment.getInstance().getEnvironmentRootNode();
        int exportedEnvironmentDone = exportEnvironmentToFile(envFile, envRoot);
        if (exportedEnvironmentDone != BaseController.BUILD_NEW_ENVIRONMENT.SUCCESS.EXPORT_ENV_FILE) {
            result.setExitCode(exportedEnvironmentDone);
            return result;
        }
        envRoot.setEnvironmentScriptPath(envFile.getAbsolutePath());
        File tstFile = new File(new WorkspaceConfig().fromJson().getTestscriptFile());
        int exportTestscriptDone = exportTestscriptToFile(tstFile, envRoot);
        if (exportTestscriptDone == BaseController.BUILD_NEW_ENVIRONMENT.SUCCESS.EXPORT_TST_FILE) {
            updateRemaining();
            try {
                InstructionComputator.compute();
            } catch (Exception e) {
                logger.error("Cant compute", e);
            }
            result.setExitCode(BaseController.BUILD_NEW_ENVIRONMENT.SUCCESS.CREATE_ENVIRONMENT);
        } else {
            result.setExitCode(exportTestscriptDone);
        }

        return result;
    }

    private AkaConfig saveCreatingWorkspaceToAkaConfig() {
        AkaConfig akaConfig = new AkaConfig().fromJson();
        String workspace = akaConfig.getWorkingDirectory() + File.separator + Environment.getInstance().getName();
        akaConfig.setOpeningWorkspaceDirectory(workspace);
        akaConfig.addOpeningWorkspaces(workspace);
        String workspaceConfig = workspace + File.separator + WorkspaceConfig.WORKSPACE_CONFIG_NAME;
        akaConfig.setOpenWorkspaceConfig(workspaceConfig);
        akaConfig.exportToJson();
        return akaConfig;
    }

    private WorkspaceConfig initializeRelativeWorkspace(String workspace) {
        String testingProject = "../../" + Environment.getInstance().getProjectNode().getName();

        LcovWorkspaceConfig wkConfig = new LcovWorkspaceConfig().fromJson();

        // save .tst and .env out of workspace
        wkConfig.setEnvironmentFile(new File(workspace).getParent() + File.separator + Environment.getInstance().getName() + WorkspaceConfig.ENV_EXTENSION);
        wkConfig.setTestscriptFile(new File(workspace).getParent() + File.separator + Environment.getInstance().getName() + ".tst");

        // initialize folders in workspace
        wkConfig.setPhysicalJsonFile(workspace + File.separator + WorkspaceConfig.PHYSICAL_JSON_NAME);
        wkConfig.setProbePointDirectory(workspace + File.separator + WorkspaceConfig.PROBE_POINT_FOLDER_NAME);
        wkConfig.setRegressionScriptDirectory(workspace + File.separator + WorkspaceConfig.REGRESSION_SCRIPT_FOLDER_NAME);
        wkConfig.setCompoundTestcaseDirectory(workspace + File.separator + WorkspaceConfig.COMPOUND_FOLDER_NAME);
        wkConfig.setDependencyDirectory(workspace + File.separator + WorkspaceConfig.DEPENDENCY_FOLDER_NAME);
        wkConfig.setTestcaseDirectory(workspace + File.separator + WorkspaceConfig.TESTCASE_FOLDER_NAME);
        wkConfig.setTestingProject(testingProject);
        wkConfig.setCurrentEnvironmentName(Environment.getInstance().getName());
        wkConfig.setInstrumentDirectory(workspace + File.separator + WorkspaceConfig.INSTRUMENT_SOURCE_FOLDER_NAME);
        wkConfig.setCommandFile(workspace + File.separator + WorkspaceConfig.COMPILATION_COMMAND_FILE_NAME);
        wkConfig.setTestcaseCommandsDirectory(workspace + File.separator + WorkspaceConfig.TEST_CASE_COMMAND_NAME);
        wkConfig.setDebugDirectory(workspace + File.separator + WorkspaceConfig.DEBUG_NAME);
        wkConfig.setDebugCommandsDirectory(workspace + File.separator + WorkspaceConfig.DEBUG_COMMAND_NAME);
        wkConfig.setDebugLogDirectory(workspace + File.separator + WorkspaceConfig.DEBUG_LOG_NAME);
        wkConfig.setBreakpointDirectory(workspace + File.separator + WorkspaceConfig.BREAKPOINT_FOLDER_NAME);
        wkConfig.setTestpathDirectory(workspace + File.separator + WorkspaceConfig.TEST_PATH_FOLDER_NAME);
        wkConfig.setCoverageDirectory(workspace + File.separator + WorkspaceConfig.COVERAGE_FOLDER_NAME);
        wkConfig.setExecutableFolderDirectory(workspace + File.separator + WorkspaceConfig.EXECUTABLE_FOLDER_NAME);
        wkConfig.setFunctionConfigDirectory(workspace + File.separator + WorkspaceConfig.FUNCTION_CONFIG_FOLDER_NAME);
        wkConfig.setReportDirectory(workspace + File.separator + WorkspaceConfig.REPORT_FOLDER_NAME);
        wkConfig.setFullReportDirectory(workspace + File.separator + WorkspaceConfig.FULL_REPORT_FOLDER_NAME);
        wkConfig.setExecutionReportDirectory(workspace + File.separator + WorkspaceConfig.EXECUTION_REPORT_FOLDER_NAME);
        wkConfig.setTestDriverDirectory(workspace + File.separator + WorkspaceConfig.TEST_DRIVER_FOLDER_NAME);
        wkConfig.setExecutionResultDirectory(workspace + File.separator + WorkspaceConfig.EXECUTION_RESULT_FOLDER_NAME);
        wkConfig.setTestDataReportDirectory(workspace + File.separator + WorkspaceConfig.TEST_DATA_REPORT_FOLDER_NAME);
        wkConfig.setStubCodeDirectory(workspace + File.separator + WorkspaceConfig.STUB_CODE_FOLDER_NAME);
        wkConfig.setElementDirectory(workspace + File.separator + WorkspaceConfig.ELEMENT_FOLDER_NAME);
        wkConfig.setVersionComparisonDirectory(workspace + File.separator + WorkspaceConfig.VERSION_COMPARISON_FOLDER_NAME);
        wkConfig.setTemplateFunctionDirectory(workspace + File.separator + WorkspaceConfig.TEMPLATE_FUNCTION_FOLDER_NAME);
        wkConfig.setBoundOfDataTypeFolder(workspace + File.separator + WorkspaceConfig.BOUND_OF_DATA_TYPES_FOLDER);
        String userCodeDir = workspace + File.separator + WorkspaceConfig.USER_CODE_FOLDER;
        wkConfig.setUserCodeDirectory(userCodeDir);
        wkConfig.setParamUserCodeDirectory(userCodeDir + File.separator + UserCodeManager.PARAM_FOLDER_NAME);
        wkConfig.setTestCaseUserCodeDirectory(userCodeDir +
                File.separator + UserCodeManager.TEST_CASE_FOLDER_NAME);

        wkConfig.setnStatementPath(workspace + File.separator + WorkspaceConfig.N_STATEMENTS_FILE_NAME);
        wkConfig.setnBranchPath(workspace + File.separator + WorkspaceConfig.N_BRANCHS_FILE_NAME);
        wkConfig.setnMcdcPath(workspace + File.separator + WorkspaceConfig.N_MCDC_FILE_NAME);
        wkConfig.setnBasicPath(workspace + File.separator + WorkspaceConfig.N_BASIC_PATH_FILE_NAME);
        wkConfig.setHeaderPreprocessorDirectory(workspace + File.separator + WorkspaceConfig.HEADER_PREPROCESSOR_FOLDER_NAME);
        wkConfig.setConstraintFolder(workspace + File.separator + WorkspaceConfig.CONSTRAINTS_FOLDER_NAME);
        wkConfig.setCteFolder(workspace + File.separator + WorkspaceConfig.CTE_FOLDER_NAME);


        String envUserCodeDir = workspace + File.separator + WorkspaceConfig.USER_CODE_FOLDER + File.separator + EnvironmentUserCode.ENVIR_FOLDER_NAME;
        wkConfig.setLcovPath(workspace + File.separator + "lcov");
        wkConfig.setEnvirUserCodeDirectory(envUserCodeDir);

        File project = new File(Environment.getInstance().getProjectNode().getAbsolutePath());
        workspace = project.getParent() + File.separator + WorkspaceConfig.WORKING_SPACE_NAME
                + File.separator + Environment.getInstance().getName();
        String workspaceConfig = workspace
                + File.separator + WorkspaceConfig.WORKSPACE_CONFIG_NAME;

        AkaConfig akaConfig = new AkaConfig().fromJson();
        akaConfig.setOpenWorkspaceConfig(workspaceConfig);
        akaConfig.setOpeningWorkspaceDirectory(workspace);
        akaConfig.addOpeningWorkspaces(workspace);
        akaConfig.exportToJson();

        wkConfig.exportToJson(workspaceConfig);
        return wkConfig;
    }

    private void exportUserCodes() {
        UserCodeController controller = BaseController.getBaseController()
                .getStepController(USER_CODE_WINDOW_INDEX);

        controller.export();
    }

    private int compileTheTestedProject(INode root) {
        CompileProjectTask task = new CompileProjectTask(root);

        try {
            result = task.call();
            return result.getExitCode();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return BUILD_NEW_ENVIRONMENT.FAILURE.OTHER;
    }

    private int exportTestscriptToFile(File testscriptFile, EnvironmentRootNode envRoot) {
        logger.debug("Exporting test script to file");

        // check the existence of the test script file
        if (AbstractCustomController.ENVIRONMENT_STATUS == AbstractCustomController.STAGE.CREATING_NEW_ENV_FROM_OPENING_GUI
                || AbstractCustomController.ENVIRONMENT_STATUS == AbstractCustomController.STAGE.CREATING_NEW_ENV_FROM_BLANK_GUI)
            if (testscriptFile.exists()) {
                logger.error("The test script file " + testscriptFile.getAbsolutePath() + " exists! Check it again.");
                return BaseController.BUILD_NEW_ENVIRONMENT.FAILURE.DUPLICATE_TST_FILE;
            }

        /**
         * Create a test case script
         */
        String relativePath = PathUtils.toRelative(testscriptFile.getAbsolutePath());
        new WorkspaceConfig().fromJson().setTestscriptFile(relativePath).exportToJson();
        TestcaseRootNode testCaseRoot = new TestcaseRootNode();

        // <<COMPOUND>>
        TestCompoundSubprogramNode compoundNode = new TestCompoundSubprogramNode();
        testCaseRoot.addChild(compoundNode);

        // <<INIT>>
        TestInitSubprogramNode initNode = new TestInitSubprogramNode();
        testCaseRoot.addChild(initNode);

        // <<SBF>> and <<UUT>>
        List<IEnvironmentNode> uutNodes = EnvironmentSearch.searchNode(envRoot, new EnviroUUTNode());
        List<IEnvironmentNode> allSourceCodeNodes = new ArrayList<>(uutNodes);
        List<IEnvironmentNode> sbfNodes = EnvironmentSearch.searchNode(envRoot, new EnviroSBFNode());
        allSourceCodeNodes.addAll(sbfNodes);

        for (IEnvironmentNode scNode : allSourceCodeNodes) {
            TestUnitNode unitNode = new TestUnitNode();

            if (scNode instanceof EnviroUUTNode) {
                unitNode.setName(((EnviroUUTNode) scNode).getName());
            } else if (scNode instanceof EnviroSBFNode) {
                unitNode.setName(((EnviroSBFNode) scNode).getName());
            }

            testCaseRoot.addChild(unitNode);

            // find the source code file node corresponding to the absolute path
            INode matchedSourcecodeNode = null;
            List<INode> sourcecodeNodes = Search.searchNodes(Environment.getInstance().getProjectNode(), new SourcecodeFileNodeCondition());
            for (INode sourcecodeNode : sourcecodeNodes)
                if (sourcecodeNode instanceof ISourcecodeFileNode)
                    if (sourcecodeNode.getAbsolutePath().equals(unitNode.getName())) {
                        matchedSourcecodeNode = sourcecodeNode;
                        break;
                    }

            // add subprograms
            if (matchedSourcecodeNode != null) {
                ParseToLambdaFunctionNode newParser = new ParseToLambdaFunctionNode();
                newParser.parse(matchedSourcecodeNode);
                
                List<INode> children = Search.searchNodes(matchedSourcecodeNode, new AbstractFunctionNodeCondition());
                children.addAll(Search.searchNodes(matchedSourcecodeNode, new MacroFunctionNodeCondition()));
                children.addAll(Search.searchNodes(matchedSourcecodeNode, new LambdaFunctionNodeCondition()));
                children = children.stream().distinct().collect(Collectors.toList());

                for (INode function : children) {
                    if (function instanceof ICommonFunctionNode) {
                        TestNormalSubprogramNode newSubprogram = new TestNormalSubprogramNode();
                        newSubprogram.setName(function.getAbsolutePath());
                        unitNode.addChild(newSubprogram);
                    }
                }
            }

        }
        String content = testCaseRoot.exportToFile();

        // export test script to file
        try {
            FileWriter writer = new FileWriter(testscriptFile);
            writer.write(content);
            writer.close();
            Environment.getInstance().getTestcaseScriptRootNode();
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.debug("The test script has been exported. Path = " + testscriptFile.getAbsolutePath());
        return BaseController.BUILD_NEW_ENVIRONMENT.SUCCESS.EXPORT_TST_FILE; // success
    }

    protected void updateRemaining() {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.submit(new ProjectClone.CloneThread());

        if (Environment.PREPROCESSOR_HEADER)
            executorService.submit(new SystemHeaderParser.InitThread());

        executorService.shutdown();

        try {
            executorService.awaitTermination(10, TimeUnit.MINUTES);

            // create workspace
            WorkspaceCreation wk = new WorkspaceCreation();
            wk.setWorkspace(new AkaConfig().fromJson().getOpeningWorkspaceDirectory());
            wk.setDependenciesFolder(new WorkspaceConfig().fromJson().getDependencyDirectory());
            wk.setElementFolder(new WorkspaceConfig().fromJson().getElementDirectory());
            wk.setPhysicalTreePath(new WorkspaceConfig().fromJson().getPhysicalJsonFile());
            wk.setRoot(Environment.getInstance().getProjectNode());
            wk.create(wk.getRoot(), wk.getElementFolder(), wk.getDependenciesFolder(), wk.getPhysicalTreePath());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Export a environment tree to file
     *
     * @return true if success, false if failed
     */
    private int exportEnvironmentToFile(File environmentFile, EnvironmentRootNode root) {
        logger.debug("Exporting the environment script to file.");

        // if we are creating new environment, but the environment exists before
        if (AbstractCustomController.ENVIRONMENT_STATUS == AbstractCustomController.STAGE.CREATING_NEW_ENV_FROM_BLANK_GUI
                || AbstractCustomController.ENVIRONMENT_STATUS == AbstractCustomController.STAGE.CREATING_NEW_ENV_FROM_OPENING_GUI)
            if (environmentFile.exists()) {
                logger.error("The environment file " + environmentFile.getAbsolutePath() + " exists! Check it again.");
                return BaseController.BUILD_NEW_ENVIRONMENT.FAILURE.DUPLICATE_ENV_FILE;
            }

        // export the environment script to file in the working directory
        String envFileRelative = PathUtils.toRelative(environmentFile.getAbsolutePath());
        new WorkspaceConfig().fromJson().setEnvironmentFile(envFileRelative).exportToJson();
        try {
            FileWriter writer = new FileWriter(environmentFile);
            writer.write(root.exportToFile());
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.debug("The environment has been exported. Path = " + environmentFile.getAbsolutePath());
        return BaseController.BUILD_NEW_ENVIRONMENT.SUCCESS.EXPORT_ENV_FILE;
    }
}
