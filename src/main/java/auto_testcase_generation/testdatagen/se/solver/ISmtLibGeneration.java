package auto_testcase_generation.testdatagen.se.solver;

public interface ISmtLibGeneration {
	String EMPTY_SMT_LIB_FILE = "";
	String OPTION_TIMEOUT = "(set-option :timeout 5000)";
	String SOLVE_COMMAND = "(check-sat)\n(get-model)";

	String getSmtLibContent();

	void generate() throws Exception;
}