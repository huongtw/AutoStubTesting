package auto_testcase_generation.cfg.object;

import com.dse.parser.object.IFunctionNode;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;

public class ConditionElementForCfgNode extends ConditionForCfgNode implements IConditionElementCfgNode {

	public ConditionElementForCfgNode(IASTNode node) {
		super(node);
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		ConditionElementForCfgNode cloneNode = (ConditionElementForCfgNode) super.clone();
		return cloneNode;
	}
}
