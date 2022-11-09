package auto_testcase_generation.cte.UI.CteClassificationTree.CteCommand;

import auto_testcase_generation.cte.UI.CteClassificationTree.CteComponent.CteUiNode;
import auto_testcase_generation.cte.UI.CteClassificationTree.CteViewManager;

import java.util.*;

public class CteCutCommand extends CteUndoableCommand{
    List<CteUiNode> listOfNode = new ArrayList<>();
    List<CteUiNode> listOfRoot = new ArrayList<>();
    List<CteUnparentCommand> unparentCommands = new ArrayList<>();
    CteViewManager manager;

    public CteCutCommand(List<CteUiNode> list, CteViewManager _manager)
    {
        for(CteUiNode node:list)
        {
            listOfNode.add(node);
        }

        manager = _manager;

    }

    @Override
    public void execute() {
        manager.getLastCopyList().clear();
        makeListOfRoot();
        unparentCommands.clear();
        CteCopyCommand copy = new CteCopyCommand(listOfNode, manager);
        copy.execute();
        removeChild();
      //  int startPosOfRoot = manager.getFreeTree().size();
        for(int i = 0; i < listOfRoot.size(); i++)
        {
            if(listOfRoot.get(i).isFreeNode() == false)
            {
            CteUnparentCommand tmp = new CteUnparentCommand(listOfRoot.get(i));
            tmp.execute();
            unparentCommands.add(tmp);
          //  listOfRoot.get(i).setFreeNode(false);
            }
           // manager.getFreeTree().remove(listOfRoot.get(i));
            manager.getCteView().clearNodeFromScreen(listOfRoot.get(i));

        }

     //   manager.moveToWaitingForPaste(startPosOfRoot);
        manager.removeAllChosenNodes();


    }

    @Override
    public void undo() {
        for(int i = 0; i < unparentCommands.size(); i++)
        {
            unparentCommands.get(i).undo();
        }

        for(int i = 0; i < listOfNode.size(); i++)
        {
            manager.getCteView().addNodeToScreen(listOfNode.get(i));
        }
    }

    private void removeChild()
    {
        List<CteUiNode> tmpList = new ArrayList<>();
        for(int i = 0; i < listOfRoot.size(); i++)
        {
            List<CteUiNode> childList = listOfRoot.get(i).getChildren();
            for(int j = 0; j< childList.size(); j++)
            {
                if(listOfRoot.contains(childList.get(j))) {
                    tmpList.add(childList.get(j));
                    manager.getCteView().clearNodeFromScreen(childList.get(j));
                }
                else
                {
                    CteUnparentCommand tmp = new CteUnparentCommand(childList.get(j));
                    unparentCommands.add(tmp);
                }
            }
        }

        for(int i = 0; i < unparentCommands.size(); i++)
        {
            unparentCommands.get(i).execute();
        }

        for(int i = 0; i < tmpList.size();i++)
        {
            listOfRoot.remove(tmpList.get(i));
        }
    }

    private void makeListOfRoot()
    {
        listOfRoot.clear();
        for(int i = 0; i< listOfNode.size(); i++)
        {
            listOfRoot.add(listOfNode.get(i));
        }
    }

    public void destroy()
    {
        listOfRoot.clear();
        unparentCommands.clear();
        for(int i = 0; i < listOfNode.size(); i++)
        {
            listOfNode.get(i).deleteNode();
        }
        listOfNode.clear();
        manager = null;
    }

}
