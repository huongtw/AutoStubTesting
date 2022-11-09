package com.dse.guifx_v3.objects.hint;

import com.dse.config.AkaConfig;

public interface HintContent {

    interface MenuBar {
        /**
         * Hint đi kèm hotkey của nó luôn, do custom menu item không hiển thị hotkey
         */

        String NEW_C_CPP_ENV_HINT = "Create a new testing environment for C/C++ project (Ctrl+N)";
        String OPEN_ENV_HINT = "Open an exist environment (Ctrl+O)";
        String SET_Z3_HINT = "Select Z3 solver for automated test cases generation";
        String QUIT_HINT = "Quit Akautauto tool (Ctrl+Q)";

        String INCREMENTAL_BUILDING_HINT = "Rebuild the environment if there are changes in source code (Ctrl+I)";
        String UPDATE_ENV_HINT = "Update environment's properties (Ctrl+U)";
        String ANALYSIS_DEPENDENCIES_HINT = "Reanalyze source code's dependencies (Ctrl+A)";
        String INSTRUMENT_HINT = "Re-instrument all source code files in the project (Ctrl+P)";
        String REGRESSION_HINT = "Manage regression scripts for regression testing";

        String CLEAN_FUNCTION_HINT = "Optimize the selected test cases at function level (Ctrl+B)";
        String CLEAN_FILE_HINT = "Optimize the selected test cases at source code file level (Ctrl+Shift+B)";
        String USER_CODE_HINT = "Manage user code";

        String RUN_WITHOUT_REPORT_HINT = "Execute the selected test cases without reports (Ctrl+R)";
        String RUN_HINT = "Execute the selected test cases and show reports (Ctrl+Shift+R)";
        String DEBUG_HINT = "Debug the selected test cases (Ctrl+Shift+D)";
        String STOP_AUTOGEN_HINT = "Stop all waiting automated test data generation threads (Ctrl+S)";

        String SET_AUTOCONFIG_HINT = "Configure the test data generation (Ctrl+Shift+A)";
        String SET_BOUND_HINT = "Configure bounds of variables with primitive types";

        String VIEW_ENV_HINT = "View the environment report (Ctrl+E)";
        String VIEW_TEST_DATA_HINT = "View Test Case Data Report of selected test cases (Ctrl+T)";
        String VIEW_FULL_HINT = "View Full Report of selected test cases (Ctrl+F)";
        String VIEW_TEST_MANAGE_HINT = "View Test Case Management Report of selected files (Ctrl+M)";

        String USER_MANUAL_HINT = "View User Manual (Ctrl+H)";
        String TOOL_VERSION_HINT = "Akautauto v" + AkaConfig.VERSION;
    }

    interface EnvBuilder {
        String STEP_CHOOSE_COMPILER = "Configure the compile for testing project";
        String STEP_SET_NAME = "Change the name of the current environment";
        String STEP_CHOOSE_METHOD = "Choose testing mode";
        String STEP_SET_BUILD_OPTION = "Configure the build options";
        String STEP_LOCATE_SRC = "Select the directories containing the source codes under test";
        String STEP_CHOOSE_UUT = "Classify the source code files in the testing project";
        String STEP_USER_CODE = "Add user code";
        String STEP_SUMMARY = "View summary report";

        interface Compiler {
            String NEW_DEFINED_VAR = "Add a new variable defined using define \"Define flag\" in \"Compile command\"";
            String PARSE_COMMAND = "Extract the defined variables and included directories in the given command";
            String TEST_SETTING = "Inspect the given preprocessor/compile commands";
            String PARSE_CMD = "Parse the above script";
            String TEST_RUN = "Run the given command";
            String TEST_SELECT_FILE = "Select a source code file to process";
        }

        interface UserCode {
            String COMPILE = "Compile the source code in selected tab";
            String ADD = "Add a new user code";
        }

        interface UUTChooser {
            String SELECT = "Classify the selected file as Unit Under Test";
            String DESELECT = "Classify the selected file as STUB";
        }

        interface SrcLocation {
            String ADD = "Add a directory";
            String ADD_RECURSIVE = "Add a directory and its sub-directories";
            String DELETE = "Remove the selected directory";
            String VIEW_RELATED = "View the selected source code files as relative/absolute paths";
        }

        interface SrcResolver {
            String OPEN_FILE = "Open the source file in text editor";
            String OPEN_HELP = "View User Manual";
            String ABORT = "Stop environment building process";
            String IGNORE_ALL = "Ignore errors in all source files and keep building";
            String IGNORE = "Ignore errors in current source file and keep building";
        }

        interface BuildOpt {
            String WHITEBOX = "Be able to test private and protected methods";
        }
    }
}
