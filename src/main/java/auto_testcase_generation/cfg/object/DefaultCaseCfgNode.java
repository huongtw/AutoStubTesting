package auto_testcase_generation.cfg.object;

import org.eclipse.cdt.core.dom.ast.IASTNode;

public class DefaultCaseCfgNode extends CaseCfgNode{

    public DefaultCaseCfgNode(IASTNode node) {
        super(node);
    }

//    @Override
//    public boolean isVisited() {
//        if(getTrueNode() != null )
//            return getTrueNode().isVisited();
//        return true;
//    }
}
