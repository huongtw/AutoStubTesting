package auto_testcase_generation.cte.UI.OptionTable;

import auto_testcase_generation.cte.UI.CteClassificationTree.CteComponent.CteClassNode;
import auto_testcase_generation.cte.UI.CteClassificationTree.CteComponent.CteUiNode;

import java.util.ArrayList;
import java.util.List;

public class CteOTColumn {

   private String nodeID;
   private String groupID;
   private String parentID;
   private List<CteOTChoice>  choicesInCol;
   private CteClassNode node;
   private boolean hiding = false;

    public CteOTColumn()
    {
        nodeID = "";
        choicesInCol = new ArrayList<>();
    }

    public void AddChoice(CteOTChoice _choice)
    {
        choicesInCol.add(_choice);
    }

    public void RemoveChoice(CteOTChoice _choice)
    {
        choicesInCol.remove(_choice);
    }

    public void HideChoices()
    {
        for (CteOTChoice cteOTChoice : choicesInCol) {
            cteOTChoice.setVisible(false);
        }
        hiding = true;
    }

    public void unchosenChoices()
    {
        for (CteOTChoice cteOTChoice : choicesInCol) {
            cteOTChoice.setSelected(false);
        }
    }

    public void ShowChoices()
    {
        for (CteOTChoice cteOTChoice : choicesInCol) {
            cteOTChoice.setVisible(true);
        }
        hiding = false;
    }

    public String getNodeID() {
        return nodeID;
    }

    public void setNodeID(String nodeID) {
        this.nodeID = nodeID;
        for (CteOTChoice cteOTChoice : choicesInCol) {
            cteOTChoice.setNodeId(nodeID);
        }
    }

    public List<CteOTChoice> getChoicesInCol() {
        return choicesInCol;
    }

    public CteClassNode getNode() {
        return node;
    }

    public void setNode(CteClassNode node) {
        this.node = node;
        for (CteOTChoice cteOTChoice : choicesInCol) {
            cteOTChoice.setBind(node);
        }
        this.setNodeID(node.getCoreId());
    }

    public void scale(double scale) {
        for (CteOTChoice cteOTChoice : choicesInCol) {
            cteOTChoice.scale(scale, node.getAlignLine());
        }
    }

    public String getGroupID() {
        return groupID;
    }

    public String getParentID() {
        return parentID;
    }

    public void setParentID(String parentID) {
        this.parentID = parentID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public void Destruct()
    {
        groupID = null;
        nodeID = null;
        node = null;
        parentID = null;
        choicesInCol.clear();
    }

    public boolean isHiding() {
        return hiding;
    }

    public void setHiding(boolean hiding) {
        this.hiding = hiding;
    }
}
