package auto_testcase_generation.cfg.object;

import com.dse.parser.object.IFunctionNode;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;

public class ConditionWhileCfgNode extends AbstractConditionLoopCfgNode {

	public ConditionWhileCfgNode(IASTNode node) {
		super(node);
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		ConditionWhileCfgNode cloneNode = (ConditionWhileCfgNode) super.clone();
		cloneNode.setVisitedFalseBranch(isVisitedFalseBranch());
		cloneNode.setVisitedTrueBranch(isVisitedTrueBranch());
		return cloneNode;
	}

}
