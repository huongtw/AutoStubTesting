package com.dse.testcase_manager;

import com.dse.config.CommandConfig;
import com.dse.debugger.utils.DebugCommandConfig;
import com.dse.testcase_execution.result_trace.AssertionResult;
import com.dse.user_code.objects.TestCaseUserCode;

import java.time.LocalDateTime;

public interface ITestCase extends ITestItem {
    String POSTFIX_TESTCASE_BY_USER = ".manual";
    String POSTFIX_TESTCASE_BY_RANDOM = ".random";
    String POSTFIX_TESTCASE_BY_BOUNDARY = ".boundary";
    String POSTFIX_TESTCASE_BY_DATA_TYPE_BOUNDARY = ".mmm";
    String POSTFIX_TESTCASE_BY_DIRECTED_METHOD = ".directed";
    String POSTFIX_TESTCASE_BY_NORMAL_BOUNDARY = ".normbound";
    String POSTFIX_TESTCASE_BY_BVA = ".bva";
    String POSTFIX_TESTCASE_BY_BOUNDARY_CONDITION = ".boundcond";
    String POSTFIX_TESTCASE_BY_ROBUSTNESS = ".robustness";
    String POSTFIX_TESTCASE_BY_BASIS_METHOD = ".basis";

    String COMPOUND_SIGNAL = "COMPOUND";
    String AKA_SIGNAL = ".";

    String STATUS_NA = "N/A";
    String STATUS_EXECUTING = "executing";
    String STATUS_SUCCESS = "success";
    String STATUS_FAILED = "failed";
    String STATUS_RUNTIME_ERR = "runtime error";
    String STATUS_EMPTY = "no status";

    String generateDefinitionCompileCmd();

    void setExecutionDateTime(LocalDateTime executionDateTime);

    LocalDateTime getExecutionDateTime();

    String getExecutionDate();

    String getExecutionTime();

    String getStatus();

    void setStatus(String status);

    AssertionResult getExecutionResult();

    void setExecutionResult(AssertionResult result);

    void appendExecutionResult(AssertionResult result);

    String getSourceCodeFile();

    void setSourceCodeFile(String sourcecodeFile);

    String getTestPathFile();

    void setTestPathFileDefault();

    String getExecutionResultTrace();

    void setTestPathFile(String testPathFile);

    String getExecutableFile();

    void setExecutableFileDefault();

    void setExecutableFile(String executableFile);

    String getCommandConfigFile();

    void setCommandConfigFileDefault();

    void setCommandConfigFile(String commandConfigFile);

    String getCommandDebugFile();

    void setCommandDebugFileDefault();

    void setCommandDebugFile(String commandDebugFile);

    String getBreakpointPath();

    void setBreakpointPathDefault();

    void setBreakpointPath(String breakpointPath);

    void setDebugExecutableFile(String debugExecutableFile);

    void setDebugExecutableFileDefault();

    @Deprecated
    String getExecutionResultFile();

    void setExecutionResultFile(String executionResultFile);

    void setExecutionResultFileDefault();

    String getDebugExecutableFile();

    void setCurrentCoverageDefault();

    void setCurrentProgressDefault();

    /**
     * Given a test case, we try to generate compilation commands + linking command.
     * <p>
     * The original project has its own commands, all the thing we need to do right now is
     * modify these commands.
     *
     * @return compile command, linker command and execute file path
     */
    CommandConfig generateCommands(String commandFile, String executableFilePath);

    DebugCommandConfig generateDebugCommands();

    void deleteOldData(); // delete all files related to the current test case

    void deleteOldDataExceptValue(); // delete all files related to the current test case except the file storing the value of test cas

//    void generateDebugCommands();

    String getAdditionalHeaders();

    void setAdditionalHeaders(String additionalHeaders);

    void setSourcecodeFileDefault();

    void setTestCaseUserCode(TestCaseUserCode testCaseUserCode);

    TestCaseUserCode getTestCaseUserCode();

    String getExecuteLog();

    void setExecuteLog(String executeLog);

    double getExecutedTime();

    void setExecutedTime(double executedTime);

    String STATEMENT_COVERAGE_FILE_EXTENSION = ".stm.cov";
    String BRANCH_COVERAGE_FILE_EXTENSION = ".branch.cov";
    String BASIS_PATH_COVERAGE_FILE_EXTENSION = ".basispath.cov";
    String MCDC_COVERAGE_FILE_EXTENSION = ".mcdc.cov";

    String STATEMENT_PROGRESS_FILE_EXTENSION = ".stm.pro";
    String BRANCH_PROGRESS_FILE_EXTENSION = ".branch.pro";
    String BASIS_PATH_PROGRESS_FILE_EXTENSION = ".basispath.pro";
    String MCDC_PROGRESS_FILE_EXTENSION = ".mcdc.pro";
}
