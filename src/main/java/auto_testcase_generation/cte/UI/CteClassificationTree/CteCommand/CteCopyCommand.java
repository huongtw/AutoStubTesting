package auto_testcase_generation.cte.UI.CteClassificationTree.CteCommand;

import auto_testcase_generation.cte.UI.CteClassificationTree.CteComponent.CteClassNode;
import auto_testcase_generation.cte.UI.CteClassificationTree.CteComponent.CteClassificationNode;
import auto_testcase_generation.cte.UI.CteClassificationTree.CteComponent.CteUiNode;
import auto_testcase_generation.cte.UI.CteClassificationTree.CteViewManager;
import auto_testcase_generation.cte.core.cteNode.CteClass;
import auto_testcase_generation.cte.core.cteNode.CteClassification;
import auto_testcase_generation.cte.core.cteNode.CteComposition;
import auto_testcase_generation.cte.core.cteNode.CteNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CteCopyCommand implements ICteCommand{
    List<CteUiNode> listOfNodes = new ArrayList<>();
    HashMap<CteNode, CteUiNode> rootMapping = new HashMap<>();
    List<CteUiNode> rootUiList = new ArrayList<>();
    List<CteNode> rootList = new ArrayList<>();
    CteViewManager viewManager;
    public CteCopyCommand(List<CteUiNode> list, CteViewManager _viewManager)
    {
        viewManager = _viewManager;
        for(CteUiNode node: list)
        {
            listOfNodes.add(node);
            rootUiList.add(node);
        }
    }

    private void filterRootOnly()
    {
        List<CteUiNode> tmpList = new ArrayList<>();
        for(int i = 0; i < rootUiList.size(); i++)
        {
            List<CteUiNode> childList = rootUiList.get(i).getChildren();
            for (CteUiNode node : childList) {
                if (rootUiList.contains(node)) {
                    tmpList.add(node);
                }
            }
        }
        for (CteUiNode node : tmpList) {
            rootUiList.remove(node);
        }
    }

    private void CopyRoot()
    {
            for(CteUiNode node : rootUiList)
            {
                CteNode newNode = createNodeFromUiNode(node);
                rootList.add(newNode);
                rootMapping.put(newNode, node);
            }
    }

    private void CopyChildForRoot()
    {
        for(int i = 0; i < rootUiList.size(); i++)
        {
            copyTree(rootList.get(i), rootMapping.get(rootList.get(i)));
        }
    }

    private void copyTree(CteNode Node, CteUiNode UiNode)
    {
        for(int i = 0; i < UiNode.getChildren().size(); i++)
        {
            if(listOfNodes.contains(UiNode.getChildren().get(i))) {
                Node.addChild(UiNode.getChildren().get(i));
                copyTree(Node.getChildren().get(Node.getChildren().size()-1), UiNode.getChildren().get(i));
            }
        }
    }

    @Override
    public void execute() {
        filterRootOnly();
        CopyRoot();
        CopyChildForRoot();

       viewManager.addToWaitingForPaste(rootList);
    }

    public List<CteNode> getRootList() {
        return rootList;
    }

    public List<CteUiNode> getListOfNodes() {
        return listOfNodes;
    }

    private CteNode createNodeFromUiNode(CteUiNode UiNode)
    {
        if(UiNode instanceof CteClassNode)
        {
            return new CteClass(UiNode);
        }
        else if(UiNode instanceof CteClassificationNode)
        {
            return  new CteClassification(UiNode);
        }
        else
        {
            return new CteComposition(UiNode);
        }
    }
}
