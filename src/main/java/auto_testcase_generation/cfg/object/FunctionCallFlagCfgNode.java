package auto_testcase_generation.cfg.object;

import com.dse.parser.object.IFunctionNode;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;

/**
 * Represent the function call flag node of CFG
 *
 * @author lamnt
 */
public abstract class FunctionCallFlagCfgNode extends FlagCfgNode {

	protected IASTFunctionCallExpression expr;
	protected IFunctionNode function;

	public IASTFunctionCallExpression getExpr() {
		return expr;
	}

	public void setExpr(IASTFunctionCallExpression expr) {
		this.expr = expr;
	}

	public IFunctionNode getFunction() {
		return function;
	}

	public void setFunction(IFunctionNode function) {
		this.function = function;
	}
}
