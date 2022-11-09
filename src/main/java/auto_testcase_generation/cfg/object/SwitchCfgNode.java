package auto_testcase_generation.cfg.object;

import org.eclipse.cdt.core.dom.ast.IASTNode;

import java.util.List;

public class SwitchCfgNode extends NormalCfgNode{

    public SwitchCfgNode(IASTNode node) {
        super(node);
    }

    public List<ICfgNode> getCases() {
        return getListTarget();
    }
}
