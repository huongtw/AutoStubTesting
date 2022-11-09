package auto_testcase_generation.cte.UI.CteClassificationTree.CteCommand;

import auto_testcase_generation.cte.UI.CteClassificationTree.CteComponent.CteUiNode;

import java.util.ArrayList;
import java.util.List;

public class CteCreateNodeCommand extends CteUndoableCommand {
    private boolean execute;
    private CteDeleteCommand deleteCommand;

    public CteCreateNodeCommand(CteUiNode parent, CteUiNode child) {
        execute = false;
        List <CteUiNode> tmp = new ArrayList<>();
        tmp.add(child);
        deleteCommand = new CteDeleteCommand(tmp, parent.getRootNode());
    }

    @Override
    public void execute() {
        if (!execute) execute = true;
        else deleteCommand.undo();
    }

    public void undo() {
        deleteCommand.execute();
    }
}
