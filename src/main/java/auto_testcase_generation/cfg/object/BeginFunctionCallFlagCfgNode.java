package auto_testcase_generation.cfg.object;

import com.dse.parser.object.IFunctionNode;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;

/**
 * Represent the function call flag node of CFG
 *
 * @author lamnt
 */
public class BeginFunctionCallFlagCfgNode extends FunctionCallFlagCfgNode {

	public BeginFunctionCallFlagCfgNode() {
		setContent(BeginFlagCfgNode.BEGIN_FLAG);
	}
}
