package com.dse.config;

import com.dse.logger.AkaLogger;
import com.dse.util.PathUtils;
import com.dse.util.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import java.io.File;

public class WorkspaceConfig {
    final static AkaLogger logger = AkaLogger.get(WorkspaceConfig.class);
    public static final String AKA_EXTENSION = ".aka";

    public static final String WORKING_SPACE_NAME = "aka-working-space";
    public static final String FUNCTION_CONFIG_FOLDER_NAME = "function-configs";
    public static final String COMPILATION_COMMAND_FILE_NAME = "compilation" + AKA_EXTENSION;
    public static final String WORKSPACE_CONFIG_NAME = "workspace" + AKA_EXTENSION;
    public static final String COMPOUND_FOLDER_NAME = "test-cases" + File.separator + "compounds";
    public static final String TESTCASE_FOLDER_NAME = "test-cases";
    public static final String COVERAGE_FOLDER_NAME = "coverages";
    public static final String PROBE_POINT_FOLDER_NAME = "probepoints";
    public static final String REGRESSION_SCRIPT_FOLDER_NAME = "regression-scripts";
    public static final String PHYSICAL_JSON_NAME = "physical_tree.json";
    public static final String DEPENDENCY_FOLDER_NAME = "dependencies";
    public static final String ELEMENT_FOLDER_NAME = "element";// the folder containing elements in the testing project
    public static final String TEST_CASE_COMMAND_NAME = "test-case-commands";
    public static final String TEST_PATH_FOLDER_NAME = "test-paths";
    public static final String EXECUTABLE_FOLDER_NAME = "exe";
    public static final String DEBUG_NAME = "debugger";
    public static final String DEBUG_COMMAND_NAME = "debugger" + File.separator + "commands";
    public static final String DEBUG_LOG_NAME = "debugger" + File.separator + "log";
    public static final String BREAKPOINT_FOLDER_NAME = "debugger" + File.separator +"breakpoints";
    public static final String REPORT_FOLDER_NAME = "reports";
    public static final String FULL_REPORT_FOLDER_NAME = "reports" + File.separator + "full";
    public static final String TEST_DATA_REPORT_FOLDER_NAME = "reports" + File.separator + "test-data";
    public static final String EXECUTION_REPORT_FOLDER_NAME = "reports" + File.separator + "execution";
    public static final String TEST_DRIVER_FOLDER_NAME = "test-drivers";
    public static final String EXECUTION_RESULT_FOLDER_NAME = "execution-results";
    public static final String STUB_CODE_FOLDER_NAME = "stubs";
    public static final String VERSION_COMPARISON_FOLDER_NAME = "difference";
    public static final String TEMPLATE_FUNCTION_FOLDER_NAME = "template_functions"; // contain all real type of function templates
    public static final String HEADER_PREPROCESSOR_FOLDER_NAME = "header-preprocessor";
    public static final String N_STATEMENTS_FILE_NAME = "nStatements.json";
    public static final String N_BRANCHS_FILE_NAME = "nBranches.json";
    public static final String N_MCDC_FILE_NAME = "nMcdcs.json";
    public static final String N_BASIC_PATH_FILE_NAME = "nBasicPaths.json";
    public static final String BOUND_OF_DATA_TYPES_FOLDER = "datatypebound";
    public static final String USING_BOUND_OF_DATA_TYPES = "using_data_type_bound";
    public static final String USER_CODE_FOLDER = "user_code";
    public static final String CONSTRAINTS_FOLDER_NAME = "constraints";
    public static final String INSTRUMENT_SOURCE_FOLDER_NAME = "instruments";
    public static final String CTE_FOLDER_NAME = "cte";

    public static final String ENV_EXTENSION = ".env";

    @Expose
    private String envirUserCodeDirectory = "";

    @Expose
    private String instrumentDirectory = "";

//    @Expose
//    private String headerUserCodePath = "";
//
//    @Expose
//    private String dataUserCodeDirectory = "";

    @Expose
    private String paramUserCodeDirectory = "";

    @Expose
    private String testCaseUserCodeDirectory = "";

    @Expose
    private String nStatementPath = "";

    @Expose
    private String nBranchPath = "";

    @Expose
    private String nMcdcPath = "";

    @Expose
    private String nBasicPath = "";

    @Expose
    private String testDriverDirectory = "";

    @Expose
    private String headerPreprocessorDirectory = "";

    @Expose
    private String stubCodeDirectory = "";

    @Expose
    private String fullReportDirectory = "";

    @Expose
    private String executionReportDirectory = "";

    @Expose
    private String testDataReportDirectory = "";

    @Expose
    private String reportDirectory = "";

    @Expose
    private String functionConfigDirectory = "";

    @Expose
    private String executableFolderDirectory = "";

    @Expose
    private String commandFile = "";

    @Expose
    private String testingProject = "";

    @Expose
    private String currentEnvironmentName = "";

    @Expose
    private String environmentFile = "";

    @Expose
    private String testscriptFile = "";

    @Expose
    private String testcaseDirectory = "";

    @Expose
    private String dependencyDirectory = "";

    @Expose
    private String physicalJsonFile = "";

    @Expose
    private String probePointDirectory = "";

    @Expose
    private String regressionScriptDirectory = "";

    @Expose
    private String compoundTestcaseDirectory = "";

    @Expose
    private String testcaseCommandsDirectory = "";

    @Expose
    private String testpathDirectory = "";

    @Expose
    private String coverageDirectory = "";

    @Expose
    private String debugDirectory = "";

    @Expose
    private String breakpointDirectory = "";

    @Expose
    private String debugLogDirectory = "";

    @Expose
    private String debugCommandsDirectory = "";

    @Expose
    private String executionResultDirectory = "";

    @Expose
    private String elementDirectory = ""; // where we save md5 of all elements in project

    @Expose
    private String versionComparisonDirectory =  "";

    @Expose
    private FunctionConfig defaultFunctionConfig = new FunctionConfig();

    @Expose
    private String templateFunctionDirectory = "";

    @Expose
    private String boundOfDataTypeDirectory = "";

    @Expose
    private String userCodeDirectory = "";

    @Expose
    private String constraintFolder = "";

    @Expose
    private String cteFolder = "";

    public WorkspaceConfig() {
    }

    private String getOpenWorkspaceConfig() {
//        ProjectNode root = Environment.getInstance().getProjectNode();
        String path;
//        if (root != null) {
//            path = new File(root.getAbsolutePath()).getParent()
//                    + File.separator + "aka-working-space"
//                    + File.separator + Environment.getInstance().getName()
//                    + File.separator + WorkspaceConfig.WORKSPACE_CONFIG_NAME;
//        } else {
            path = new AkaConfig().fromJson().getOpenWorkspaceConfig();
//        }
        return path;
    }

    public WorkspaceConfig fromJson() {
        return load(getOpenWorkspaceConfig());
//        return load();
    }

    public WorkspaceConfig load(String path) {
        File currentWorkingSpaceConfig = new File(path);
        if (currentWorkingSpaceConfig.exists()) {
            GsonBuilder builder = new GsonBuilder();
            builder.excludeFieldsWithoutExposeAnnotation();
            Gson gson = builder.setPrettyPrinting().create();
            WorkspaceConfig setting = gson.fromJson(Utils.readFileContent(currentWorkingSpaceConfig), WorkspaceConfig.class);
            return setting;
        } else {
            logger.error("The " + currentWorkingSpaceConfig.getAbsolutePath() + " does not exist!");
            return new WorkspaceConfig();
        }
    }

    public void exportToJson(String workspaceConfig) {
        GsonBuilder builder = new GsonBuilder();
        builder.excludeFieldsWithoutExposeAnnotation();
        Gson gson = builder.setPrettyPrinting().create();
        String json = gson.toJson(this);

        if (workspaceConfig != null && workspaceConfig.length() > 0) {
            Utils.writeContentToFile(json, workspaceConfig);
        } else{
            logger.error("workspace " + workspaceConfig + " is not set up");
        }
    }

    public void exportToJson() {
        GsonBuilder builder = new GsonBuilder();
        builder.excludeFieldsWithoutExposeAnnotation();
        Gson gson = builder.setPrettyPrinting().create();
        String json = gson.toJson(this);

        String openingWorkspaceConfig = new AkaConfig().fromJson().getOpenWorkspaceConfig();
        if (openingWorkspaceConfig != null && openingWorkspaceConfig.length() > 0) {
            Utils.writeContentToFile(json, openingWorkspaceConfig);
        } else{
            logger.error("openingWorkspaceConfig is not set up");
        }
    }

    private String toAbsolutePath(String relativePath) {
        return PathUtils.toAbsolute(relativePath);
    }

    private String initAndGetPath(String relativePath) {
        String absolutePath = toAbsolutePath(relativePath);

        if (!(new File(absolutePath).exists()))
            new File(absolutePath).mkdirs();

        return absolutePath;
    }

    private void init(String relativePath) {
        String absolutePath = toAbsolutePath(relativePath);
        new File(absolutePath).mkdirs();
    }

    public String getTestDriverDirectory() {
        return initAndGetPath(testDriverDirectory);
    }

    public WorkspaceConfig setTestDriverDirectory(String testDriverDirectory) {
        this.testDriverDirectory = testDriverDirectory;
        init((testDriverDirectory));
        return this;
    }

    public String getHeaderPreprocessorDirectory() {
        return initAndGetPath(headerPreprocessorDirectory);
    }

    public WorkspaceConfig setHeaderPreprocessorDirectory(String directory) {
        this.headerPreprocessorDirectory = directory;
        init((headerPreprocessorDirectory));
        return this;
    }

    public String getStubCodeDirectory() {
        return initAndGetPath(stubCodeDirectory);
    }

    public WorkspaceConfig setStubCodeDirectory(String testDriverDirectory) {
        this.stubCodeDirectory = testDriverDirectory;
        init((testDriverDirectory));
        return this;
    }

    public String getReportDirectory() {
        return initAndGetPath(reportDirectory);
    }

    public WorkspaceConfig setReportDirectory(String reportDirectory) {
        this.reportDirectory = reportDirectory;
        init((reportDirectory));
        return this;
    }

    public String getExecutionResultDirectory() {
        return initAndGetPath(executionResultDirectory);
    }

    public WorkspaceConfig setExecutionResultDirectory(String executionResultDirectory) {
        this.executionResultDirectory = executionResultDirectory;
        init((executionResultDirectory));
        return this;
    }

    public String getTestDataReportDirectory() {
        return initAndGetPath(testDataReportDirectory);
    }

    public WorkspaceConfig setTestDataReportDirectory(String testDataReportDirectory) {
        this.testDataReportDirectory = testDataReportDirectory;
        init((testDataReportDirectory));
        return this;
    }

    public String getFullReportDirectory() {
        return initAndGetPath(fullReportDirectory);
    }

    public WorkspaceConfig setFullReportDirectory(String testCaseDataReportDirectory) {
        this.fullReportDirectory = testCaseDataReportDirectory;
        init((testCaseDataReportDirectory));
        return this;
    }

    public String getExecutionReportDirectory() {
        return initAndGetPath(executionReportDirectory);
    }

    public WorkspaceConfig setExecutionReportDirectory(String executionReportDirectory) {
        this.executionReportDirectory = executionReportDirectory;
        init(executionReportDirectory);
        return this;
    }

    public String getTestcaseDirectory() {
        return initAndGetPath(testcaseDirectory);
    }

    public WorkspaceConfig setTestcaseDirectory(String testcaseDirectory) {
        this.testcaseDirectory = testcaseDirectory;
        init(testcaseDirectory);
        return this;
    }

    public String getEnvirUserCodeDirectory() {
        return initAndGetPath(envirUserCodeDirectory);
    }

    public WorkspaceConfig setEnvirUserCodeDirectory(String envirUserCodeDirectory) {
        this.envirUserCodeDirectory = envirUserCodeDirectory;
        init(envirUserCodeDirectory);
        return this;
    }

    public String getInstrumentDirectory() {
        return initAndGetPath(instrumentDirectory);
    }

    public WorkspaceConfig setInstrumentDirectory(String instrumentDirectory) {
        this.instrumentDirectory = instrumentDirectory;
        init(instrumentDirectory);
        return this;
    }

    public String getParamUserCodeDirectory() {
        return paramUserCodeDirectory;
    }

    public String getTestCaseUserCodeDirectory() {
        return testCaseUserCodeDirectory;
    }

    public void setParamUserCodeDirectory(String paramUserCodeDirectory) {
        this.paramUserCodeDirectory = paramUserCodeDirectory;
        init(paramUserCodeDirectory);
    }

    public void setTestCaseUserCodeDirectory(String testCaseUserCodeDirectory) {
        this.testCaseUserCodeDirectory = testCaseUserCodeDirectory;
        init(testCaseUserCodeDirectory);
    }

//    public String getDataUserCodeDirectory() {
//        return initAndGetPath(dataUserCodeDirectory);
//    }
//
//    public WorkspaceConfig setDataUserCodeDirectory(String dataUserCodeDirectory) {
//        this.dataUserCodeDirectory = dataUserCodeDirectory;
//        init(dataUserCodeDirectory);
//        return this;
//    }

//    public String getHeaderUserCodePath() {
//        return toAbsolutePath(headerUserCodePath);
//    }
//
//    public WorkspaceConfig setHeaderUserCodePath(String headerUserCodePath) {
//        this.headerUserCodePath = headerUserCodePath;
//        new File(toAbsolutePath(headerUserCodePath)).getParentFile().mkdirs();
//        return this;
//    }

    public String getDependencyDirectory() {
        return initAndGetPath(dependencyDirectory);
    }

    public WorkspaceConfig setDependencyDirectory(String dependencyDirectory) {
        this.dependencyDirectory = dependencyDirectory;
        init((dependencyDirectory));
        return this;
    }

    public String getProbePointDirectory() {
        return initAndGetPath(probePointDirectory);
    }

    public String getRegressionScriptDirectory() {
        return initAndGetPath(regressionScriptDirectory);
    }

    public WorkspaceConfig setProbePointDirectory(String probePointDirectory) {
        this.probePointDirectory = probePointDirectory;
        init((probePointDirectory));
        return this;
    }

    public WorkspaceConfig setRegressionScriptDirectory(String regressionScriptDirectory) {
        this.regressionScriptDirectory = regressionScriptDirectory;
        init((regressionScriptDirectory));
        return this;
    }

    public String getCurrentEnvironmentName() {
        return currentEnvironmentName;
    }

    public WorkspaceConfig setCurrentEnvironmentName(String currentEnvironmentName) {
        this.currentEnvironmentName = currentEnvironmentName;
        return this;
    }

    public String getEnvironmentFile() {
        return toAbsolutePath(environmentFile);
    }

    public WorkspaceConfig setEnvironmentFile(String environmentFile) {
        this.environmentFile = environmentFile;
        new File(toAbsolutePath(environmentFile)).getParentFile().mkdirs();
        return this;
    }

    public WorkspaceConfig setTestscriptFile(String testscriptFile) {
        this.testscriptFile = testscriptFile;
        new File(toAbsolutePath(testscriptFile)).getParentFile().mkdirs();
        return this;
    }

    public String getTestscriptFile() {
        return toAbsolutePath(testscriptFile);
    }

    public String getPhysicalJsonFile() {
        return toAbsolutePath(physicalJsonFile);
    }

    public WorkspaceConfig setPhysicalJsonFile(String physicalJsonFile) {
        this.physicalJsonFile = physicalJsonFile;
        return this;
    }

    public String getCompoundTestcaseDirectory() {
        return initAndGetPath(compoundTestcaseDirectory);
    }

    public WorkspaceConfig setCompoundTestcaseDirectory(String compoundTestcaseDirectory) {
        this.compoundTestcaseDirectory = compoundTestcaseDirectory;
        init((compoundTestcaseDirectory));
        return this;
    }

    public String getTestingProject() {
        return testingProject;
    }

    public WorkspaceConfig setTestingProject(String testingProject) {
        this.testingProject = testingProject;
        return this;
    }

    public String getCommandFile() {
        return toAbsolutePath(commandFile);
    }

    public WorkspaceConfig setCommandFile(String commandFile) {
        this.commandFile = commandFile;
        return this;
    }

    public String getTestcaseCommandsDirectory() {
        return initAndGetPath(testcaseCommandsDirectory);
    }

    public WorkspaceConfig setTestcaseCommandsDirectory(String testcaseCommandsDirectory) {
        this.testcaseCommandsDirectory = testcaseCommandsDirectory;
        init((testcaseCommandsDirectory));
        return this;
    }

    public String getDebugLogDirectory() {
        return initAndGetPath(debugLogDirectory);
    }

    public WorkspaceConfig setDebugLogDirectory(String debugLogDirectory) {
        this.debugLogDirectory = debugLogDirectory;
        init((debugLogDirectory));
        return this;
    }

    public String getDebugDirectory() {
        return initAndGetPath(debugDirectory);
    }

    public WorkspaceConfig setDebugDirectory(String debugDirectory) {
        this.debugDirectory = debugDirectory;
        init((debugDirectory));
        return this;
    }

    public String getBreakpointDirectory() {
        return initAndGetPath(breakpointDirectory);
    }

    public WorkspaceConfig setBreakpointDirectory(String breakpointDirectory) {
        this.breakpointDirectory = breakpointDirectory;
        init((breakpointDirectory));
        return this;
    }

    public String getDebugCommandsDirectory() {
        return initAndGetPath(debugCommandsDirectory);
    }

    public WorkspaceConfig setDebugCommandsDirectory(String debugCommandsDirectory) {
        this.debugCommandsDirectory = debugCommandsDirectory;
        init((debugCommandsDirectory));
        return this;
    }

    public String getCoverageDirectory() {
        return initAndGetPath(coverageDirectory);

    }

    public WorkspaceConfig setCoverageDirectory(String coverageDirectory) {
        this.coverageDirectory = coverageDirectory;
        init((coverageDirectory));
        return this;
    }

    public String getTestpathDirectory() {
        return initAndGetPath(testpathDirectory);

    }

    public WorkspaceConfig setTestpathDirectory(String testpathDirectory) {
        this.testpathDirectory = testpathDirectory;
        init((testpathDirectory));
        return this;
    }

    public String getExecutableFolderDirectory() {
        return initAndGetPath(executableFolderDirectory);
    }

    public WorkspaceConfig setExecutableFolderDirectory(String executableFolderDirectory) {
        this.executableFolderDirectory = executableFolderDirectory;
        init((executableFolderDirectory));
        return this;
    }

    public String getFunctionConfigDirectory() {
        return initAndGetPath(functionConfigDirectory);
    }

    public WorkspaceConfig setFunctionConfigDirectory(String functionConfigDirectory) {
        this.functionConfigDirectory = functionConfigDirectory;
        init((functionConfigDirectory));
        return this;
    }

    public WorkspaceConfig setElementDirectory(String elementDirectory) {
        this.elementDirectory = elementDirectory;
        init((elementDirectory));
        return this;
    }

    public String getElementDirectory() {
        return initAndGetPath(elementDirectory);
    }

    public String getVersionComparisonDirectory() {
        return initAndGetPath(versionComparisonDirectory);
    }

    public WorkspaceConfig setVersionComparisonDirectory(String versionComparisonDirectory) {
        this.versionComparisonDirectory = versionComparisonDirectory;
        init((versionComparisonDirectory));
        return this;
    }

    public FunctionConfig getDefaultFunctionConfig() {
        return defaultFunctionConfig;
    }

    public WorkspaceConfig setDefaultFunctionConfig(FunctionConfig defaultFunctionConfig) {
        this.defaultFunctionConfig = defaultFunctionConfig;
        return this;
    }

    public String getFileContainingChangesWhenComparingSourcecode(){
        return getVersionComparisonDirectory() + File.separator + "changes_detail.log";
    }

    public String getFileContainingChangedSourcecodeFileWhenComparingSourcecode(){
        return getVersionComparisonDirectory()+File.separator + "changes_general.log";
    }

    public String getFileContainingUnresolvedDependenciesWhenComparingSourcecode(){
        return getVersionComparisonDirectory() + File.separator + "unresolved_dependencies.log";
    }

    public String getCompilationMessageWhenComplingProject(){
        return getVersionComparisonDirectory() + File.separator + "compilation.log";
    }

    public String getDeletedTestcaseWhenUpdatingEnv(){
        return getVersionComparisonDirectory() + File.separator + "deleted_testcases_when_updating_env.log";
    }

    public String getTemplateFunctionDirectory() {
        return toAbsolutePath(templateFunctionDirectory);
    }

    public void setTemplateFunctionDirectory(String templateFunctionDirectory) {
        this.templateFunctionDirectory = templateFunctionDirectory;
        init((templateFunctionDirectory));
    }

    public String getBoundOfDataTypeDirectory() {
        return toAbsolutePath(boundOfDataTypeDirectory);
    }

    public void setBoundOfDataTypeFolder(String boundOfDataTypeFolder) {
        this.boundOfDataTypeDirectory = boundOfDataTypeFolder;
        init(boundOfDataTypeFolder);
    }

    public String getUserCodeDirectory() {
        return userCodeDirectory;
    }

    public void setUserCodeDirectory(String userCodeDirectory) {
        this.userCodeDirectory = userCodeDirectory;
        init(userCodeDirectory);
    }

    public String getConstraintFolder() {
        return initAndGetPath(constraintFolder);
    }

    public void setConstraintFolder(String constraintFolder) {
        this.constraintFolder = constraintFolder;
        new File(toAbsolutePath(constraintFolder)).getParentFile().mkdirs();
    }

    public String getCteFolder() {
        return initAndGetPath(cteFolder);
    }

    public void setCteFolder(String cteFolder) {
        this.cteFolder = cteFolder;
        new File(toAbsolutePath(cteFolder)).getParentFile().mkdirs();
    }

    public String getnStatementPath() {
        return toAbsolutePath(nStatementPath);
    }

    public WorkspaceConfig setnStatementPath(String nStatementPath) {
        this.nStatementPath = nStatementPath;
        return this;
    }

    public WorkspaceConfig setnBranchPath(String nBranchPath) {
        this.nBranchPath = nBranchPath;
        return this;
    }

    public String getnBranchPath() {
        return toAbsolutePath(nBranchPath);
    }

    public WorkspaceConfig setnMcdcPath(String nMcdcPath) {
        this.nMcdcPath = nMcdcPath;
        return this;
    }

    public String getnMcdcPath() {
        return toAbsolutePath(nMcdcPath);
    }

    public WorkspaceConfig setnBasicPath(String nBasicPath) {
        this.nBasicPath = nBasicPath;
        return this;
    }

    public String getnBasicPath() {
        return toAbsolutePath(nBasicPath);
    }
}


