package auto_testcase_generation.testdatagen.se;

import auto_testcase_generation.cfg.testpath.ITestpathInCFG;
import auto_testcase_generation.testdatagen.se.memory.IVariableNodeTable;
import auto_testcase_generation.testdatagen.se.memory.VariableNodeTable;

import java.util.List;
import java.util.Set;

/**
 * Interface of symbolic execution for a test path
 *
 * @author ducanhnguyen
 */
public interface ISymbolicExecution {
	String NO_SOLUTION_CONSTRAINT = "1<0";

	String NO_SOLUTION_CONSTRAINT_SMTLIB = "< 1 0";

	String UNSAT_IN_Z3 = "unsat";

	String ALWAYS_TRUE_CONSTRAINT = "1>0";

	String TO_INT_Z3 = "to_int";
	/**
	 * Represent type of statements
	 */
	int UNSPECIFIED_STATEMENT = -1;

	/**
	 * Ex1: a+b <br/>
	 * Ex2: a = new int[2]
	 */

	int BINARY_ASSIGNMENT = 0;
	/**
	 * Ex: a++; a--
	 */
	int UNARY_ASSIGNMENT = 1;

	int CONDITION = 2;

	int DECLARATION = 3;

	int RETURN = 5;

	int THROW = 6;

	int IGNORE = 7;

	int NAMESPACE = 8;

	String NAMESPACE_SIGNAL = "using namespace ";

	IPathConstraints getConstraints();

	void setConstraints(PathConstraints constraints);

	Parameters getParameters();

	void setParameters(Parameters parameters);

	String getReturnValue();

	void setReturnValue(String returnValue);

	IVariableNodeTable getTableMapping();

	void setTableMapping(VariableNodeTable tableMapping);

	ITestpathInCFG getTestpath();

	void setTestpath(ITestpathInCFG testpath);

	Set<NewVariableInSe> getNewVariables();

	void setNewVariables(Set<NewVariableInSe> newVariables);

	PathConstraints getNormalizedPathConstraints();
}