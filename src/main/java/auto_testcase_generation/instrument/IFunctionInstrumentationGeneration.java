package auto_testcase_generation.instrument;

import com.dse.parser.object.IFunctionNode;

/**
 * Instrument function
 *
 * @author DucAnh
 */
public interface IFunctionInstrumentationGeneration {
	String IS_FULL_CONDITION =  "ifc";//"IS_FULL_CONDITION";
	String IS_SUB_CONDITION = "isc";//"IS_SUB_CONDITION";
	String IS_NORMAL_STATEMENT = "ins";//"IS_NORMAL_SATETEMENT";

	String FUNCTION_ADDRESS = "function";
	String LINE_NUMBER_IN_SOURCE_CODE_FILE = "lis";//"line-in-sourcecode";
	String LINE_NUMBER_IN_FUNCTION = "lif";//"line-in-function";
	String START_OFFSET_IN_SOURCE_CODE_FILE = "sois";//"start-offset-in-sourcecode";
	String START_OFFSET_IN_FUNCTION = "soif";//"start-offset-in-function";
	String END_OFFSET_IN_SOURCE_CODE_FILE = "eois";//"end-offset-in-sourcecode";
	String END_OFFSET_IN_FUNCTION = "eoif";//"end-offset-in-function";
	String STATEMENT = "statement";
	String DELIMITER_BETWEEN_PROPERTIES = "###";

	// can not use "=" (will be wrong if the path point to an overloading function having "=")
	String DELIMITER_BETWEEN_PROPERTY_AND_VALUE = "===";

	/**
	 * Generate instrumented source code of a function
	 *
	 * @return the instrumented function if the function could be instrumented,
	 * null otherwises
	 */
	String generateInstrumentedFunction();
}
