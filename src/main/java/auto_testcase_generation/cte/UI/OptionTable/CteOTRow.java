package auto_testcase_generation.cte.UI.OptionTable;

import auto_testcase_generation.cte.UI.CteClassificationTree.CteComponent.CteCompositionNode;
import auto_testcase_generation.cte.UI.TestcaseTable.CteTestcaseElement;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class CteOTRow {
   // private List<ToggleGroup> groupList;
    private List<CteOTChoice> butOfRowList;
    private List<CteOTGroup> groupList;
    private List<CteOTGroup> bin;
    private int rowNum;
    private String testCaseID = new String();
    private Rectangle highLight;
    private Line choicesLine;


    public CteOTRow(String id)
    {
        groupList = new ArrayList<>();
        butOfRowList = new ArrayList<>();
        testCaseID = id;
        highLight = new Rectangle();
        choicesLine = new Line();
        bin = new ArrayList<>();
        setUpHighlight();
        setUpChoicesLine();

    }

//    public void createRadioButton(ToggleGroup gr, CteUiNode cNode, int numOfRound){
//        CteOTChoice but = new CteOTChoice();
//        but.setLayoutX(cNode.getX() + cNode.getWidth()/2 - 7);
//        but.setLayoutY(40 + (numOfRound-1)*30);
//        but.setToggleGroup(gr);
//        this.butOfRowList.add(but);
//       // this.getChildren().add(but);
//        //   System.out.println("add button");
//    }

//    public void createRadioButton(ToggleGroup gr, CteUiNode cNode, int numOfRound, String _id){
//        CteOTChoice but = new CteOTChoice();
//        but.setNodeId(_id);
//        but.setLayoutX(cNode.getX() + cNode.getWidth()/2 - 7 );
//        but.setLayoutY(40 + (numOfRound-1)*30);
//        but.setToggleGroup(gr);
//        but.layoutXProperty().bind(cNode.getAlignLine().startXProperty().subtract(7));
//        this.butOfRowList.add(but);
//        // this.getChildren().add(but);
//        //   System.out.println("add button");
//    }

    public void createRadioButton(CteOTColumn col){
        if(col == null) System.out.println("Col is null!!");
        CteOTChoice but = new CteOTChoice();
        but.setNodeId(col.getNodeID());
        but.setLayoutX(col.getNode().getX() + col.getNode().getWidth()/2 - 7 );
        but.setLayoutY(40 + (rowNum-1)*30);
        but.setToggleGroup(getGroupByID(col.getGroupID()));
        if (col.getNode().getAlignLine() != null) {
            but.layoutXProperty().bind(col.getNode().getAlignLine().startXProperty().subtract(7));
        }
        if(col.getParentID() != null)
        {
            CteOTChoice tmp = this.getChoicebyId(col.getParentID());
            but.setParentChoice(tmp);
        }

        this.butOfRowList.add(but);
        col.getChoicesInCol().add(but);
        if(col.isHiding())
        {
            col.HideChoices();
        }
    }

    public void createRadioButton(CteOTColumn col, String parentID){
        if(col == null) System.out.println("Col is null!!");
        CteOTChoice but = new CteOTChoice();
        but.setNodeId(col.getNodeID());
        but.setLayoutX(col.getNode().getX() + col.getNode().getWidth()/2 - 7 );
        but.setLayoutY(40 + (rowNum-1)*30);
        but.setToggleGroup(getGroupByID(col.getGroupID()));
        but.layoutXProperty().bind(col.getNode().getAlignLine().startXProperty().subtract(7));
        but.setParentChoice(this.getChoicebyId(parentID));
        this.butOfRowList.add(but);
        col.getChoicesInCol().add(but);
        //this.getChildren().add(but);
        //   System.out.println("add button");
    }



    public void createGroups(CteCompositionNode rootNode){
        if(rootNode.getClassificationNodesList().size() == 0)
        {
        }
        else
        {
            for(int i = 0; i< rootNode.getClassificationNodesList().size();i++)
            {
                CteOTGroup a = new CteOTGroup(rootNode.getClassificationNodesList().get(i).getCoreId());
                groupList.add(a);
            }
        }
    }

    public void addGroup(String id)
    {
        CteOTGroup a = new CteOTGroup(id);
        groupList.add(a);
    }

//    public void createRadioButtons( CteCompositionNode rootNode, int numOfRound )
//    {
//        System.out.println("Num Of Group:  " + rootNode.getClassificationNodesList().size() );
//        for(int i = 0; i < rootNode.getClassificationNodesList().size(); i++)
//        {
//            System.out.println("In group " + i + " , num of Child is    " + rootNode.getClassificationNodesList().get(i).getChildren().size());
//            if(rootNode.getClassificationNodesList().get(i).getChildren().size() == 0)
//            {
//                String id = rootNode.getClassificationNodesList().get(i).getCoreId();
//                createRadioButton(groupList.get(i), rootNode.getClassificationNodesList().get(i), numOfRound, id );
//                System.out.println("Create Button for group  " + i);
//            }
//            for(int j =0; j < rootNode.getClassificationNodesList().get(i).getChildren().size(); j++)
//            {
//                String id2 = rootNode.getClassificationNodesList().get(i).getChildren().get(j).getCoreId();
//                createRadioButton(groupList.get(i), rootNode.getClassificationNodesList().get(i).getChildren().get(j), numOfRound, id2 );
//                System.out.println("Create Button for child " + j + " of group  " + i);
//            }
//
//
//        }
//    }

    public void createRadioButtons( List<CteOTColumn> colList ){
        if(colList.size() != 0)
        {
            for(int i = 0; i < colList.size() ; i++)
            {
                createRadioButton(colList.get(i));
            }
        }
    }

    public List<CteOTChoice> getButOfRowList() {
        return butOfRowList;
    }

    public CteOTChoice getChoicebyId(String _id)
    {
       // CteOTChoice result = null;
        for(int i = 0; i < butOfRowList.size(); i ++)
        {
            if(butOfRowList.get(i).getNodeId().equals(_id))
            {
                return butOfRowList.get(i);
            }
        }
        return null;
    }

    public CteOTGroup getGroupByID(String Id)
    {
        for(CteOTGroup gr : groupList)
        {
            if (gr.getGroupID().equals(Id)) return gr;
        }
        return null;
    }

    public void deleteGroupByID(String Id)
    {
        CteOTGroup tmp = getGroupByID(Id);
        if(tmp != null)
        {
            groupList.remove(tmp);
            bin.add(tmp);
        }
    }

    public void restoreGroupByID(String Id)
    {
        for(int i = 0; i < bin.size();i++)
        {
            if(bin.get(i).getGroupID().equals(Id))
            {
                groupList.add(bin.get(i));
                bin.remove(i);
                break;
            }
        }
    }

    public int getRowNum() {
        return rowNum;
    }

    public void setRowNum(int rowNum) {
        this.rowNum = rowNum;
    }

    public String getTestCaseID() {
        return testCaseID;
    }

    public void selfDestruct()
    {
        butOfRowList.clear();
        groupList.clear();
        testCaseID = null;
        choicesLine = null;
    }

    public void setPosOfChoices(CteTestcaseElement element)
    {
        if(element!= null)
        {
            for (CteOTChoice cteOTChoice : butOfRowList) {
                cteOTChoice.layoutYProperty().bind(element.getTcLabel().yProperty().add(1));
            }

        }
    }

    public void setPosOfChoice(CteTestcaseElement element, String choiceID)
    {
        CteOTChoice tmp = getChoicebyId(choiceID);
        if(tmp != null)
        {
            tmp.layoutYProperty().bind(element.getTcLabel().yProperty().add(1));
        }
    }

    public void setUpHighlight()
    {
        highLight.setX(0);
        highLight.setFill(Color.TRANSPARENT);

        highLight.setStroke(Color.TRANSPARENT);
    }

    public Rectangle getHighLight() {
        return highLight;
    }

    public void activateHighlight(){
        highLight.setFill(Color.valueOf("A2BC89"));
    }

    public void deactivateHighlight()
    {
        highLight.setFill(Color.TRANSPARENT);
    }
     public void deleteHighlight()
     {
         highLight = null;
     }

     public void setUpChoicesLine()
     {
         choicesLine.setStartX(0);
         choicesLine.setStrokeWidth(1.2);
         choicesLine.setStroke(Color.GRAY);

         choicesLine.endYProperty().bind(choicesLine.startYProperty());
     }

     public void setUpChoicesLineYPos()
     {
         choicesLine.startYProperty().bind(highLight.yProperty().add(highLight.getHeight()/2 + 2));
     }


    public Line getChoicesLine() {
        return choicesLine;
    }
}
