package auto_testcase_generation.cte.UI.OptionTable;

import auto_testcase_generation.cte.UI.CteClassificationTree.*;
import auto_testcase_generation.cte.UI.CteClassificationTree.CteComponent.CteClassNode;
import auto_testcase_generation.cte.UI.CteClassificationTree.CteComponent.CteClassificationNode;
import auto_testcase_generation.cte.UI.CteClassificationTree.CteComponent.CteCompositionNode;
import auto_testcase_generation.cte.UI.CteClassificationTree.CteComponent.CteUiNode;
import auto_testcase_generation.cte.UI.CteCoefficent;
import auto_testcase_generation.cte.UI.TestcaseTable.CteTestcaseTable;
import auto_testcase_generation.cte.core.cteTable.CteTable;
import javafx.scene.Group;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CteOptionTable extends Pane {

    private List<CteOTRow> rowList;
    private List<CteOTColumn> colList;
    private CteCompositionNode rootNode;
    private CteTestcaseTable testcaseTable;
    private int numOfRound = 0;
    private CteTable cTableManager;
    private CteView cteMain;

    private boolean empty = true;



    public CteOptionTable(CteView cteView) {
        super();
 //       borderProperty().setValue(new Border(new BorderStroke(Color.BLACK,
  //              BorderStrokeStyle.DASHED, CornerRadii.EMPTY, new BorderWidths(1))));

        this.setPosition(CteCoefficent.otStartX, CteCoefficent.otStartY);
//        this.setMinSize(CteCoefficent.otMinWidth, CteCoefficent.otMinHeight);
        rowList = new ArrayList<>();
        colList = new ArrayList<>();


        cteMain = cteView;
        cteMain.setOTable(this);
        cTableManager = cteMain.getTreeManager().getTable();
        rootNode = cteMain.getHeadNode();
        createColumns();

        this.setOnMouseEntered((t) ->
        {
            this.cteMain.ShowAlignLines();
        });

        this.setOnMouseExited((t) ->
        {
            this.cteMain.HideAlignLines();
        });

    }

    public void setPosition(double _x, double _y) {
        this.setLayoutX(_x);
        this.setLayoutY(_y);
    }

    public CteOTRow addRow(String id) {
        CteOTRow row = new CteOTRow(id);
        row.createGroups(this.rootNode);
        row.setRowNum(numOfRound);
        getChildren().add(row.getHighLight());
        getChildren().add(row.getChoicesLine());
        row.createRadioButtons(colList);
        increaseRow();
        for (int i = 0; i < row.getButOfRowList().size(); i++) {
            getChildren().addAll(row.getButOfRowList().get(i));
        }
        row.getHighLight().widthProperty().bind(this.widthProperty().subtract(5));
        row.getChoicesLine().endXProperty().bind(this.widthProperty().subtract(5));
        rowList.add(row);
        if (isEmpty()) setEmpty(false);
        return row;
    }

    public void deleteRow(String id){
        CteOTRow row = getRowById(id);
        if(row != null)
        {

            if(getChildren().contains(row.getHighLight()))
            {
                getChildren().remove(row.getHighLight());
                getChildren().remove(row.getChoicesLine());
                row.deactivateHighlight();
                row.deleteHighlight();
            }

            for(int i = 0; i< row.getButOfRowList().size(); i++)
            {
                this.getChildren().removeAll(row.getButOfRowList().get(i));
            }
            for (CteOTColumn tmpCol : colList) {
                for (int j = 0; j < row.getButOfRowList().size(); j++) {
                    CteOTChoice tmpChoice = row.getButOfRowList().get(j);
                    if (tmpCol.getChoicesInCol().contains(tmpChoice)) {
                        tmpCol.getChoicesInCol().remove(tmpChoice);
                        row.getButOfRowList().remove(j);
                        break;
                    }
                }
            }
            rowList.remove(row);
            numOfRound --;
            row.selfDestruct();
        }
        else System.out.println("Cannot find row with ID : " + id);
    }

    public void createColumn(CteClassNode node, String grId) {
        CteOTColumn col = new CteOTColumn();
        col.setNode(node);
        col.setGroupID(grId);
        this.colList.add(col);
    }

    public void createColumn(CteClassNode node, String grID, String parentID)
    {
        CteOTColumn col = new CteOTColumn();
        col.setNode(node);
        col.setGroupID(grID);
        col.setParentID(parentID);
        this.colList.add(col);
    }

    public void createColumns() {
        List<CteClassificationNode> classificationList = rootNode.getClassificationNodesList();
        System.out.println("Num Of Group:  " + classificationList.size());
        for (int i = 0; i < classificationList.size(); i++) {
            System.out.println("In group " + i + " , num of Child is    " + classificationList.get(i).getChildren().size());
//            if (classificationList.get(i).getChildren().size() == 0) {
//                CteClassificationNode tmpClassification = classificationList.get(i);
//                if(tmpClassification.isChildOfClass())
//                {
//                    CteUiNode tmpParent = tmpClassification.getParentNode();
//                    if(tmpParent instanceof CteClassNode)
//                    {
//                        createColumn(tmpClassification, tmpClassification.getCoreId(), tmpParent.getCoreId());
//                    }
//                    else
//                    {
//                        CteCompositionNode tmpCompoParent = (CteCompositionNode) tmpParent;
//                        createColumn(tmpClassification, tmpClassification.getCoreId(), tmpCompoParent.getClassParent().getCoreId());
//                    }
//                }
//                else{
//                    createColumn(classificationList.get(i), classificationList.get(i).getCoreId());
//                }
//
//                System.out.println("Create column for" + " group  " + i);
//            }
            for (int j = 0; j < classificationList.get(i).getChildren().size(); j++) {
                CteUiNode tmpClass = classificationList.get(i).getChildren().get(j);
                //Tan add this line
                if (tmpClass.getChildren().size() == 0) {

                    CteClassificationNode tmpParent = (CteClassificationNode) tmpClass.getParentNode();
                    //
                    if(tmpParent.isChildOfClass())
                    {
                        if(tmpParent.getParentNode() instanceof CteClassNode) {
                            createColumn((CteClassNode)tmpClass, tmpParent.getCoreId(), tmpParent.getParentNode().getCoreId());
                        }
                        else
                        {
                            CteCompositionNode parTmpParent = (CteCompositionNode) tmpParent.getParentNode();
                            createColumn((CteClassNode)tmpClass, tmpParent.getCoreId(),parTmpParent.getClassParent().getCoreId());
                        }
                    }
                    else {
                        System.out.println(tmpClass.getChildren().size());
                        createColumn((CteClassNode)tmpClass, classificationList.get(i).getCoreId());
                        System.out.println("Create column for child " + j + " of group  " + i);
                    }
                }
                else
                {
                    createColumn((CteClassNode)tmpClass, classificationList.get(i).getCoreId());
                    getColByID(tmpClass.getCoreId()).setHiding(true);
                }
            }
        }
    }

    public void scale(double scaleRate) {

    }
    public void addColOfChoices(CteClassNode node, String grId,  CteUiNode parentForParID)
    {

            if (parentForParID == null) {
                createColumn(node, grId);
                for (int i = 0; i < rowList.size(); i++) {
                    CteOTColumn tmp = getColByID(node.getCoreId());
                    rowList.get(i).createRadioButton(tmp);
                    rowList.get(i).setPosOfChoice(this.testcaseTable.getTestcaseElementByID(rowList.get(i).getTestCaseID()), node.getCoreId());
                    this.getChildren().add(rowList.get(i).getChoicebyId(node.getCoreId()));
                }
            } else {
                if (parentForParID instanceof CteClassificationNode) {
                    String parentSetID = parentForParID.getChildren().get(0).getCoreId();
                    createColumn(node, grId);
                    CteOTColumn tmp = getColByID(node.getCoreId());
                    if(rowList.isEmpty()) {}
                    else  {
                        for (int i = 0; i < rowList.size(); i++) {
                            rowList.get(i).createRadioButton(tmp);
                            rowList.get(i).setPosOfChoice(this.testcaseTable.getTestcaseElementByID(rowList.get(i).getTestCaseID()), node.getCoreId());
                            this.getChildren().add(rowList.get(i).getChoicebyId(node.getCoreId()));
                        }
                    }

                } else if (parentForParID instanceof CteClassNode) {
                    String parentID = parentForParID.getCoreId();
                    createColumn(node, grId, parentID);
                    CteOTColumn tmp = getColByID(node.getCoreId());
                    for (int i = 0; i < rowList.size(); i++) {

                        rowList.get(i).createRadioButton(tmp, parentID);
                        rowList.get(i).setPosOfChoice(this.testcaseTable.getTestcaseElementByID(rowList.get(i).getTestCaseID()), node.getCoreId());
                        this.getChildren().add(rowList.get(i).getChoicebyId(node.getCoreId()));
                    }
                }
            }

    }

    public void changeColID(String oldID, CteClassNode newNode)
    {
     CteOTColumn tmp  = getColByID(oldID);
     if(tmp!= null) {
         tmp.setNode(newNode);
         System.out.println(tmp.getNodeID());
     }
    }

    public CteOTColumn getColByID(String id) {
        if(id != null) {
            for (CteOTColumn col : colList) {
                if (col.getNodeID().equals(id)) return col;
            }
        }
        return null;
    }

    public void deleteColByID(String id) {
        CteOTColumn temp = getColByID(id);
        if (temp != null) {
            System.out.println("found col!!");
            for (int i = 0; i < temp.getChoicesInCol().size(); i++) {
                if (this.getChildren().contains(temp.getChoicesInCol().get(i))) {
                    this.getChildren().remove(temp.getChoicesInCol().get(i));
                    System.out.println("RemoveChoices!");
                }
                this.rowList.get(i).getButOfRowList().remove(temp.getChoicesInCol().get(i));
            }
            temp.Destruct();
            colList.remove(temp);
        }
    }

    public void hideColByID(String id)
    {
        CteOTColumn temp = getColByID(id);
        if (temp != null) {
            temp.HideChoices();
            temp.unchosenChoices();
            temp.setHiding(true);
        }
    }

    public void addGroups(String id)
    {
        for (CteOTRow cteOTRow : rowList) {
            cteOTRow.addGroup(id);
        }
    }

    public void changeGroupForCol(String colID, String newGroupID)
    {

        for(int i  = 0; i < rowList.size(); i++)
        {
            CteOTGroup tmpGroup = rowList.get(i).getGroupByID(newGroupID);
            rowList.get(i).getChoicebyId(colID).setToggleGroup(tmpGroup);
        }
        getColByID(colID).setGroupID(newGroupID);
        getColByID(colID).ShowChoices();
    }

    public CteOTRow getRowById(String Id)
    {
        for (int i = 0; i < rowList.size(); i++)
        {
            if( rowList.get(i).getTestCaseID().equals(Id))
            {
                return rowList.get(i);
            }
        }
        return null;
    }

    public void changeParentForColNChoices(String colID, String newParColID)
    {
        CteOTColumn targetCol = getColByID(colID);
        if(targetCol != null)
        {
            CteOTColumn newParCol = getColByID(newParColID);

            if (newParCol != null){
                 targetCol.setParentID(newParColID);
                for (int i = 0; i < rowList.size(); i++) {
                    CteOTRow tmp = rowList.get(i);
                    tmp.getChoicebyId(colID).setParentChoice(tmp.getChoicebyId(newParColID));
                }
            }
            else
            {
                targetCol.setParentID(null);
                for (int i = 0; i < rowList.size(); i++) {
                    CteOTRow tmp = rowList.get(i);
                    tmp.getChoicebyId(colID).setParentChoice(null);
                }
            }


        }
    }



    public CteCompositionNode getRootNode() {
        return rootNode;
    }

    public void setRootNode(CteCompositionNode rootNode) {
        this.rootNode = rootNode;
    }

    public void increaseRow()
    {
        numOfRound+=1;
    }



    public CteTable getcTableManager() {
        return cTableManager;
    }

    public List<CteOTRow> getRowList() {
        return rowList;
    }

    public CteView getCteMain() {
        return cteMain;
    }

    public void setCteMain(CteView cteMain) {
        this.cteMain = cteMain;
    }

    public boolean isEmpty() {
        return empty;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }

    public int getNumOfRound() {
        return numOfRound;
    }

    public CteTestcaseTable getTestcaseTable() {
        return testcaseTable;
    }

    public void setTestcaseTable(CteTestcaseTable testcaseTable) {
        this.testcaseTable = testcaseTable;
    }

    public List<CteOTColumn> getColList() {
        return colList;
    }
}
