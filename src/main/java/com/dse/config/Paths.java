package com.dse.config;

/**
 * The list of paths (in samples), etc.
 *
 * @author duc-anh
 */
public class Paths{

	public static String AUTOGEN = "/Users/ducanhnguyen/Documents/akautauto/datatest/duc-anh/autogen/template";
	public static boolean START_FROM_COMMANDLINE = false;
	public static String STUDENT_MANAGEMENT = "datatest/duc-anh/StudentManagement/";
	public static String DATA_GEN_TEST = "datatest/duc-anh/DataGenTest/";
	public static String VARIABLE_NODE_TEST = "datatest/duc-anh/VariableNodeTest/";
	public static String CPP_AND_HEADER_PARSER_TEST = "datatest/duc-anh/CPPandHeaderParserTest/";
	public static String FUNCTION_NODE_NAME_TEST = "datatest/duc-anh/FunctionNodeNameTest/";
	public static String GET_NAME_OF_EXE_IN_DEVCPP_MAKEFILE = "datatest/duc-anh/GetNameOfExeInDevCppMakeFile/";
	public static String RANDOM_GENERATION_TEST2 = "datatest/duc-anh/RandomGenerationTest2";
	public static String TYPEDEF_NODE_NAME_TEST = "datatest/duc-anh/TypedefNodeNameTest";
	public static String TYPE_DEPENDENCY_GENERATION_TEST = "datatest/duc-anh/TypeDependencyGenerationTest/";
	public static String TEST_PATH_GENERATION_TEST = "datatest/duc-anh/TestpathGenerationTest/";
	public static String SYMBOLIC_EXECUTION_TEST = "datatest/duc-anh/SymbolicExecutionTest/";
	public static String COMBINED_STATIC_AND_DYNAMIC_GENERATION = "datatest/duc-anh/CombinedStaticAndDynamicGen/";
	public static String STATEMENT_COVERAGE_COMPUTATION_TEST = "datatest/duc-anh/StatementCoverageComputationTest/";
	public static String FUNCTION_TRANSFORMER_TEST = "datatest/duc-anh/FunctionTransformerTest/";
	public static String SEPARATE_FUNCTION_TEST = "datatest/duc-anh/SeparateFunctionTest";
	public static String INCLUDE_DEPENDENCY_GENERATION_TEST = "datatest/duc-anh/IncludeHeaderDependencyGeneration";
	public static String EXPECTED_OUTPUT_PANELV2_TEST = "datatest/duc-anh/ExpectedOutputPanelv2Test";
	public static String GTEST_LIB = "datatest/nartoan/lib/GoogleTest";
	public static String GTEST_FOR_CHECK = "datatest/nartoan/GTestForCheck/";
	public static String RUN_AND_EXPORT_RESULTS = "../cft4cpp-core/local";
	public static String TSDV_LOG4CPP = "datatest/duc-anh/TSDV_log4cpp";
	public static String TSDV_TMATH = "datatest/duc-anh/TSDV_ttmath-0.9.3";
	public static String TYPE_SIMPLE = "datatest/duc-anh/TypeDependencyGenerationTest/Eclipse_Type Simple";
	public static String BTL = "datatest/duc-anh/BTL/";
	public static String SIMBOLIC_EXECUTION_VS = "datatest/nartoan/VisualStudioSolution/";
	public static String SUB_CONDITION = "datatest/nartoan/SubConditionTest/";
	public static String CFG_GENERATION = "datatest/duc-anh/CFGGeneration/";
	public static String SHIP_GAME = "datatest/duc-anh/Ship Destroyer Game1";
	public static String MAKEFILE_SAMPLES = "datatest/duc-anh/makefile samples";
	public static String SAMPLE01 = "datatest/duc-anh/Sample1/";
	public static String SAMPLE02 = "datatest/duc-anh/Sample2/";
	public static String SAMPLE03 = "datatest/duc-anh/Sample3/";
	public static String SWT = "datatest/nartoan/SwitchTestCase/";
	public static String RANDOM_GENERATION = "datatest/duc-anh/RandomGenerationTest2";
	public static String TREE_EXPRESSION_GENERATION_TEST = "datatest/duc-anh/TreeExpressionGenerationTest/";
	public static String SAMPLE_CODE = "datatest/duc-anh/SampleCode/";
	public static String ASTYLE = "../cft4cpp-core/lib/AStyle.exe";
	public static String TSDV_R1 = "datatest/tsdv/Sample_for_R1/";
	public static String TSDV_R1_10 = "datatest/tsdv/Sample_for_R1_10file/";
	public static String TSDV_R1_2 = "datatest/tsdv/Sample_for_R1_2/";
	public static String TSDV_R1_3 = "datatest/tsdv/Sample_for_R1_3_Cpp11/";
	public static String TSDV_R1_4 = "datatest/tsdv/Sample_for_R1_4/";
	public static String CORE_UTILS = "datatest/duc-anh/coreutils-8.24";
	public static String RETURN_ENUM = "datatest/tsdv/SampleSource-2017A/return_enum/";
	public static String GLOBAL_VARIABLE = "datatest/tsdv/SampleSource-2017A/global_variable/";
	public static String JOURNAL_TEST = "datatest/duc-anh/Algorithm/";
	public static String JOURNAL_TEST_V2 = "datatest/duc-anh/Algorithmv2/";
	public static String IMSTRUMENT_TEST = "datatest/duc-anh/instrument/";
	public static String EXTERNAL_VARIABLE_DETECTER_TEST = "datatest/duc-anh/ExternalVariableDetecterTest";

	public static final String MULTIPLE_TYPEDEF_DECLARE = "data-test/samvu/Simple";
	public static class CURRENT_FUNCTION_CONFIGURATION {

		public static int SIZE_OF_ARRAY = 10; // default
		public static int MAX_ITERATION_FOR_EACH_LOOP = 1; // default
		public static int LENGTH_OF_LINKED_LIST = 1; // default

		public static class BOUND_PARAMETERS {

			public static class CHARACTER_TYPE {

				public static int LOWER = 0; // default
				public static int UPPER = 126; // default
			}

			public static class INTEGER_TYPE {

				public static int LOWER = -100; // default
				public static int UPPER = 100; // default
			}
		}
	}

	public static class CURRENT_PROJECT {

		/**
		 * The path of selected project
		 */
		public static String ORIGINAL_PROJECT_PATH = "";

		/**
		 * The path of cloned project
		 */
		public static String CLONE_PROJECT_NAME = "cloneProject";
		public static String CLONE_PROJECT_PATH = "";

		/**
		 * Type of project
		 */
		public static int TYPE_OF_PROJECT;

		/**
		 * The path of makefile
		 */
		public static String MAKEFILE_PATH = "";

		/**
		 * The path of .exe after the project is compiled
		 */
		public static String EXE_PATH = "";

		/**
		 * The name of file that contains test path after the project is
		 * executed
		 */
		public static String TESTDRIVER_EXECUTION_NAME_POSTFIX = "testdriver_execution.txt";

		public static String CURRENT_TESTDRIVER_EXECUTION_NAME = "";

		public static String TESTDATA_INPUT_FILE_NAME = "input.txt";
		public static String TESTDATA_INPUT_FILE_PATH = "";

		public static String CURRENT_TESTDRIVER_EXECUTION_PATH = "";

		public static String LOCAL_FOLDER = "./local";

		public static void reset() {
			Paths.CURRENT_PROJECT.TESTDRIVER_EXECUTION_NAME_POSTFIX = Paths.CURRENT_PROJECT.CLONE_PROJECT_PATH = Paths.CURRENT_PROJECT.MAKEFILE_PATH = Paths.CURRENT_PROJECT.EXE_PATH = "";
			TESTDATA_INPUT_FILE_NAME = TESTDATA_INPUT_FILE_PATH = CURRENT_TESTDRIVER_EXECUTION_PATH = "";
		}
	}
}
