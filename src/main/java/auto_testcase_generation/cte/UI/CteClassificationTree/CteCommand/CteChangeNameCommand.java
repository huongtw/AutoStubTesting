package auto_testcase_generation.cte.UI.CteClassificationTree.CteCommand;

import auto_testcase_generation.cte.UI.CteClassificationTree.CteComponent.CteUiNode;

public class CteChangeNameCommand extends CteUndoableCommand{

    private CteUiNode targetNode;
    private String oldName;
    private String newName;

    public CteChangeNameCommand(CteUiNode _target, String _newName)
    {
        targetNode = _target;
        oldName = _target.getTitle();
        newName = _newName;
    }

    @Override
    public void execute() {
        targetNode.setTitle(newName);
    }

    @Override
    public void undo() {
        targetNode.setTitle(oldName);
    }
}
