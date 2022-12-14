package auto_testcase_generation.cfg.object;

import com.dse.parser.object.IFunctionNode;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;

public class ConditionDoCfgNode extends AbstractConditionLoopCfgNode {

	public ConditionDoCfgNode(IASTNode node) {
		super(node);
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		ConditionDoCfgNode cloneNode = (ConditionDoCfgNode) super.clone();
		cloneNode.setVisitedFalseBranch(isVisitedFalseBranch());
		cloneNode.setVisitedTrueBranch(isVisitedTrueBranch());
		return cloneNode;
	}
}
