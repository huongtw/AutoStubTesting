package auto_testcase_generation.cte.UI.OptionTable;

import auto_testcase_generation.cte.UI.CteClassificationTree.CteComponent.CteAlignLine;
import auto_testcase_generation.cte.UI.CteClassificationTree.CteComponent.CteClassNode;
import auto_testcase_generation.cte.UI.CteClassificationTree.CteComponent.CteUiNode;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.RadioButton;

import java.util.ArrayList;
import java.util.List;

public class CteOTChoice extends RadioButton {

    private String tcName;
    private String nodeId;
    private int rowNum;
    private CteOTChoice parentChoice = null;
    private List<CteOTChoice> childChoices = new ArrayList<>();

    public CteOTChoice()
    {
        super();
        tcName = new String();
        nodeId = new String();
        EnableCheckingParent();
    }

    public String getTcName() {
        return tcName;
    }

    public void setTcName(String tcName) {
        this.tcName = tcName;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public int getRowNum() {
        return rowNum;
    }

    public void setRowNum(int rowNum) {
        this.rowNum = rowNum;
    }

    public void setBind(CteClassNode node)
    {
        this.layoutXProperty().bind(node.getAlignLine().startXProperty().subtract(7));
    }

    public void scale(double scaleFactor, CteAlignLine alignLine) {
        layoutXProperty().bind(alignLine.startXProperty().subtract(7).multiply(scaleFactor));
    }

    public CteOTChoice getParentChoice() {
        return parentChoice;
    }

    public void EnableCheckingParent()
    {
//        this.setOnAction(e -> {
//            CteOTChoice intermediary = (CteOTChoice) e.getSource();
//            if(intermediary.getParentChoice() != null && intermediary.isSelected()) {
//                System.out.println("Choice with Id " + intermediary.getNodeId());
//                intermediary.getParentChoice().setSelected(true);
//            }
//            if(!intermediary.isSelected())
//            {
//                System.out.println("Unselected choice " + intermediary.getNodeId());
//            }
//
//        } );

        CteOTChoice intermediary = this;

        this.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(intermediary.getParentChoice() != null && newValue)
                {
                    intermediary.getParentChoice().setSelected(true);
                }
                if(intermediary.getChildChoices().size() != 0 && !newValue)
                {
                    for(int i = 0; i < intermediary.getChildChoices().size(); i++)
                    {
                        intermediary.getChildChoices().get(i).setSelected(false);
                    }
                }
            }
        });


    }

    public void setParentChoice(CteOTChoice parentChoice) {
        if(this.parentChoice != null)
        {
            this.parentChoice.getChildChoices().remove(this);
        }
        this.parentChoice = parentChoice;
        if(parentChoice != null) {
            parentChoice.getChildChoices().add(this);}
    }

    public void setParentSelected()
    {
        if(parentChoice != null)
        {
            this.getParentChoice().setSelected(true);
        }
    }

    public List<CteOTChoice> getChildChoices() {
        return childChoices;
    }
}
