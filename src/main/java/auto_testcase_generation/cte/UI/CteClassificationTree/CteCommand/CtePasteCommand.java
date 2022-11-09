package auto_testcase_generation.cte.UI.CteClassificationTree.CteCommand;

import auto_testcase_generation.cte.UI.CteClassificationTree.CteComponent.CteClassNode;
import auto_testcase_generation.cte.UI.CteClassificationTree.CteComponent.CteCompositionNode;
import auto_testcase_generation.cte.UI.CteClassificationTree.CteComponent.CteUiNode;
import auto_testcase_generation.cte.UI.CteClassificationTree.CteViewManager;
import auto_testcase_generation.cte.core.cteNode.CteClass;
import auto_testcase_generation.cte.core.cteNode.CteClassification;
import auto_testcase_generation.cte.core.cteNode.CteComposition;
import auto_testcase_generation.cte.core.cteNode.CteNode;

import java.util.*;

public class CtePasteCommand extends CteUndoableCommand{
    CteUiNode target;
    CteViewManager viewManager;
    List<CteNode> listOfCopyNodes = new ArrayList<>();
    List<CteUiNode> listOfNewNodes = new ArrayList<>();
    Map<CteNode, String> mapNodeName = new HashMap<>();


    public CtePasteCommand(CteUiNode node, CteViewManager _viewManager)
    {
        target = node;
        viewManager = _viewManager;

        if(viewManager.getWaitingForPaste().size() == 0)
        {
            viewManager.addToWaitingForPaste(viewManager.getLastCopyList());
        }

        for(int i = 0; i < viewManager.getWaitingForPaste().size(); i++)
        {
            listOfCopyNodes.add(viewManager.getWaitingForPaste().get(i));
        }


        filterCopyNodes();
    }

    @Override
    public void execute() {
        listOfNewNodes.clear();
        if(listOfCopyNodes.size() != 0) {
            pasteRootAndNodes();
            resetName();
            if(listOfNewNodes.size() > 0) viewManager.clearWaitingForPaste();
            else
            {
                viewManager.deleteLastestInUndo(this);
            }
        }
        else viewManager.deleteLastestInUndo(this);

    }

    @Override
    public void undo() {
        for(int i = 0; i < listOfNewNodes.size(); i++)
        {
            listOfNewNodes.get(i).deleteNode();
        }
    }


    public void filterCopyNodes()
    {
        List<CteNode> tmp = new ArrayList<>();
        if(target instanceof CteCompositionNode || target instanceof  CteClassNode)
        {
            for(int i = 0; i < listOfCopyNodes.size(); i++)
            {
                if(listOfCopyNodes.get(i) instanceof CteClass) tmp.add(listOfCopyNodes.get(i));
            }
        }
        else {
            for(int i = 0; i < listOfCopyNodes.size(); i++)
            {
                if(listOfCopyNodes.get(i) instanceof CteComposition ||
                                                listOfCopyNodes.get(i) instanceof CteClassification)
                {
                    tmp.add(listOfCopyNodes.get(i));
                }
            }
        }

        for(int i = 0; i < tmp.size(); i++)
        {
            listOfCopyNodes.remove(tmp.get(i));
        }

        filterDuplicateName();
    }



    private void filterDuplicateName()
    {
        List<String> name = new ArrayList<>();
        List<CteNode> tmp = new ArrayList<>();
        for(int i =0; i < listOfCopyNodes.size(); i++)
        {
            tmp.add(listOfCopyNodes.get(i));
        }

        if(!tmp.isEmpty()) {
            for(int i = 0; i < target.getChildren().size(); i++)
            {
                name.add(target.getChildren().get(i).getTitle());
            }
            for (int i = 0; i < tmp.size(); i++) {
                if (isExistNamedInList(name, tmp.get(i).getName())) {
                    String NewName = newNameForNode(name,tmp.get(i).getName(), 0 );
                    mapNodeName.put(tmp.get(i), tmp.get(i).getName());
                    tmp.get(i).setName(NewName);
                }

                name.add(tmp.get(i).getName());
            }

        }
    }

    private boolean isExistNamedInList(List<String> list, String name){
        for(int i = 0; i < list.size(); i++ )
        {
            if(list.get(i).equals(name)) return true;
        }
        return false;
    }

    private void pasteRootAndNodes()
    {
        Map<CteNode, CteUiNode> nodeMap = new HashMap<>();
        for(int i = 0; i < listOfCopyNodes.size(); i++)
        {
            CteUiNode node = target.addChild(listOfCopyNodes.get(i));
            listOfNewNodes.add(node);
            nodeMap.put(listOfCopyNodes.get(i), node);
        }

        for(int i = 0; i < listOfCopyNodes.size(); i++)
        {
            pasteTree(listOfCopyNodes.get(i), nodeMap.get(listOfCopyNodes.get(i)));
        }
    }

    private void pasteTree(CteNode node, CteUiNode uiNode)
    {
        for(int i = 0; i < node.getChildren().size(); i++)
        {
           CteUiNode tmpNode = uiNode.addChild(node.getChildren().get(i));
           listOfNewNodes.add(tmpNode);
        }

        for(int i = 0; i < node.getChildren().size(); i++)
        {
            pasteTree(node.getChildren().get(i), uiNode.getChildren().get(i));
        }

    }

    private String newNameForNode(List<String> nameList, String name, int num)
    {
        int NewNum = num + 1;
        StringBuilder newName = new StringBuilder(name);
        newName.append( " (" );
        newName.append(NewNum);
        newName.append(")");
        if(isExistNamedInList(nameList, newName.toString())) return newNameForNode(nameList, name, NewNum);
        else return newName.toString();
    }

    private void resetName()
    {
        for(int i = 0; i < listOfCopyNodes.size(); i++)
        {
            String oldName = mapNodeName.get(listOfCopyNodes.get(i));
            if(oldName != null)
            {
                listOfCopyNodes.get(i).setName(oldName);
            }
        }
    }

    public List<CteNode> getListOfCopyNodes() {
        return listOfCopyNodes;
    }
}
