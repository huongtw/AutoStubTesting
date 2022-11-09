package auto_testcase_generation.cte.UI.CteClassificationTree.CteCommand;

import auto_testcase_generation.cte.UI.CteClassificationTree.CteComponent.CteUiNode;
import auto_testcase_generation.cte.UI.CteClassificationTree.CteViewManager;
import auto_testcase_generation.cte.UI.CteCoefficent;

import java.util.ArrayList;
import java.util.List;

public class CteSetParentCommand extends CteUndoableCommand{
    CteUiNode target;
    CteUiNode childNode;
    //List<CteUiNode> listOfNode = new ArrayList<>();
    CteViewManager viewManager;

    public CteSetParentCommand(CteUiNode node, CteViewManager _manager)
    {
        childNode = node;
        childNode.setBgColorForNodes(CteCoefficent.NodeBgColor.setParModeCor);
        viewManager = _manager;
    }

    public void setTarget(CteUiNode _target)
    {
        target = _target;
    }

//    public void setListOfNode(List<CteUiNode> list)
//    {
//        for(int i = 0; i<list.size();i++)
//        {
//            listOfNode.add(list.get(i));
//        }
//    }

    public void destroy()
    {
        target = null;
        childNode = null;
        viewManager = null;
    }

    @Override
    public void execute() {
            target.setAsParent(childNode, null);
    }

    @Override
    public void undo() {
        childNode.unparent();
    }

    public CteUiNode getChildNode() {
        return childNode;
    }
}
