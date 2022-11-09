package auto_testcase_generation.cfg.object;

import com.dse.parser.object.IFunctionNode;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;

public class ConditionElementWhileCfgNode extends ConditionWhileCfgNode implements IConditionElementCfgNode {

	public ConditionElementWhileCfgNode(IASTNode node) {
		super(node);
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		ConditionElementWhileCfgNode cloneNode = (ConditionElementWhileCfgNode) super.clone();
		return cloneNode;
	}

}
