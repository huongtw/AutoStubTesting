package auto_testcase_generation.cfg;

import com.dse.parser.object.IFunctionNode;

/**
 * This interface is used to generate CFG
 *
 * @author ducanh
 */
public interface ICFGGeneration {
	int IF_FLAG = 0;

	int DO_FLAG = 1;

	int WHILE_FLAG = 2;

	int FOR_FLAG = 3;

	/**
	 * Generate the control flow graph corresponding to the given function
	 *
	 * @return
	 */
	ICFG generateCFG() throws Exception;

	IFunctionNode getFunctionNode();

	void setFunctionNode(IFunctionNode functionNode);

}
