package auto_testcase_generation.cte.UI.CteClassificationTree.CteCommand;

import auto_testcase_generation.cte.UI.CteClassificationTree.CteComponent.CteUiNode;

public class CteUnparentCommand extends CteUndoableCommand{
    private CteUiNode target;
    private CteUiNode parentNode;
    private Integer index;

    public CteUnparentCommand(CteUiNode node)
    {
        target = node;
        parentNode = node.getParentNode();
        index = parentNode.getChildren().indexOf(target);
    }

    @Override
    public void execute() {
        target.unparent();
    }

    @Override
    public void undo() {
        parentNode.setAsParent(target, index);
    }

    public void setTarget(CteUiNode target) {
        this.target = target;
    }
}
