package auto_testcase_generation.cfg.object;

import com.dse.parser.object.IFunctionNode;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;

public class ConditionElementDoCfgNode extends ConditionDoCfgNode implements IConditionElementCfgNode {

	public ConditionElementDoCfgNode(IASTNode node) {
		super(node);
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		ConditionElementDoCfgNode cloneNode = (ConditionElementDoCfgNode) super.clone();
		return cloneNode;
	}
}
