package auto_testcase_generation.cte.UI.CteClassificationTree.CteCommand;

import auto_testcase_generation.cte.UI.CteClassificationTree.CteComponent.CteUiNode;
import auto_testcase_generation.cte.core.cteNode.CteNode;

import java.util.ArrayList;
import java.util.List;

public class CteDeleteCommand extends CteUndoableCommand{
    CteUiNode rootNode;
    List<CteUiNode> listOfNode = new ArrayList<>();
    List<CteUnparentCommand> unparentCommands = new ArrayList<>();
    List<CteUiNode> rootLists = new ArrayList<>();
    CteCutCommand cutCommand;
    public CteDeleteCommand(List<CteUiNode> list, CteUiNode root)
    {
        rootNode = root;
        for(int i = 0; i < list.size(); i++)
        {
            if(list.get(i) != rootNode) {
                listOfNode.add(list.get(i));
                rootLists.add(list.get(i));
            }
        }
        //removeChild();
    }

    private void removeChild()
    {
        List<CteUiNode> tmpList = new ArrayList<>();
        for(int i = 0; i < rootLists.size(); i++)
        {
            List<CteUiNode> childList = rootLists.get(i).getChildren();
            for(int j = 0; j< childList.size(); j++)
            {
                if(rootLists.contains(childList.get(j))) tmpList.add(childList.get(j));
            }
        }

        for(int i = 0; i < tmpList.size();i++)
        {
            rootLists.remove(tmpList.get(i));
        }
    }

    @Override
    public void execute() {
        cutCommand = new CteCutCommand(listOfNode, rootNode.getCteMain().getCteViewManager());
        cutCommand.execute();
        rootNode.getCteMain().getCteViewManager().clearWaitingForPaste();
    }

    @Override
    public void undo() {
        cutCommand.undo();
    }

    @Override
    public void destroy() {
        cutCommand.destroy();
    }
}
