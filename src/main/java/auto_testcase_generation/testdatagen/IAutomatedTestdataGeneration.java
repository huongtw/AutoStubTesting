package auto_testcase_generation.testdatagen;

import com.dse.parser.object.ICommonFunctionNode;

/**
 * Generate test data for a function automatically
 *
 * @author ducanhnguyen
 */
public interface
IAutomatedTestdataGeneration {

	interface AUTOGEN_STATUS {
		int OTHER_ERRORS = -1;
		int NOT_ABLE_TO_GENERATE_CFG = 0;
		int NO_SHORTEST_PATH = 1;

		int FOUND_DUPLICATED_TESTPATH = -23;

		interface SOLVING_STATUS {
			int FOUND_DUPLICATED_TESTCASE = 2;
		}

		interface EXECUTION {
			int COULD_NOT_CONSTRUCT_TREE_FROM_TESTCASE = 3;
			int COUND_NOT_EXECUTE_TESTCASE = 4;
			int BE_ABLE_TO_EXECUTE_TESTCASE = 5;
		}
	}

	/**
	 * Generate test data satisfying criterion
	 *
	 * @throws Exception
	 */
	void generateTestdata(ICommonFunctionNode fn) throws Exception;
}