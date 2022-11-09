package auto_testcase_generation.cfg.object;

import com.dse.config.FunctionConfig;
import com.dse.parser.object.IFunctionNode;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

public class SimpleCfgNode extends NormalCfgNode {

	public SimpleCfgNode(IASTNode node) {
		super(node);
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		SimpleCfgNode cloneNode = (SimpleCfgNode) super.clone();
		return cloneNode;
	}
}
