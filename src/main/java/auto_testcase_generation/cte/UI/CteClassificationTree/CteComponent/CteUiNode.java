package auto_testcase_generation.cte.UI.CteClassificationTree.CteComponent;

import auto_testcase_generation.cte.UI.Controller.ColorChangeController;
import auto_testcase_generation.cte.UI.CteClassificationTree.CteView;
import auto_testcase_generation.cte.UI.CteCoefficent;
import auto_testcase_generation.cte.UI.OptionTable.CteOTColumn;
import auto_testcase_generation.cte.core.cteNode.CteNode;
import com.dse.boundary.DataSizeModel;
import com.dse.boundary.PrimitiveBound;
import com.dse.environment.Environment;
import com.dse.guifx_v3.helps.UIController;
import com.dse.testcase_manager.TestCase;
import com.dse.testdata.gen.module.TreeExpander;
import com.dse.testdata.object.*;
import com.dse.util.Utils;
import com.dse.util.VariableTypeUtils;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import java.util.*;

public class CteUiNode extends DraggableNode {


    protected CteUiNode parent;
    protected Edge edge;
    public List<CteUiNode> children ;
    protected ContextMenu ctMenu;
    protected int  coefficentForX = -1;
    protected double childWidth;
    protected int deepAdding = 0;
    protected  double yForChild;
  //  protected  double initialPosX;
    protected  double startChild;
    protected CteView cteMain = null;
    protected CteCompositionNode rootNode;
    protected boolean freeNode = false;
    protected String id = new String();
    protected TestCase testCase = null;
    protected String Tag = new String();
    protected List<String> childNames = new ArrayList<>();
    private boolean isChoosing = false;


    public CteView getCteMain() {
        return cteMain;
    }

    public void setCteMain(CteView cte) {

        this.cteMain = cte;
        if(this instanceof CteClassNode) {
            createTestCaseForNode();
        }
        setUpDragMultiNode();
    }


    public CteUiNode(double x, double y)
    {
        super(x, y);
        initialize();
    }

    public CteUiNode(double x, double y, String _id)
    {
        super(x, y);
        this.id = _id;
        initialize();

    }
    private void initialize() {
        this.yForChild = this.getY() + CteCoefficent.padY;

        parent = null;
        children = new ArrayList<>();
        ctMenu = new ContextMenu();
        setUpMenu();
        menuProcess();
        childWidth = 10;
        setUpMouse();

    }

    public void setParent(CteUiNode _p)
    {
        this.parent = _p;
    }

    public CteUiNode addChild(CteNode node)
    {return null;}

    protected void setUpChild(CteUiNode child) {
        child.setParent(this);
        child.setRootNode();
        Edge edge1 = new Edge(this, child);
        child.setEdges(edge1);
        child.setUpInitialPos();
        child.setCteMain(this.getCteMain());
        child.setUpTag();
        this.children.add(child);
        this.getCteMain().addNodeToScreen(child);
    }

    public void menuProcess()
    {
       this.setOnContextMenuRequested(event -> ctMenu.
               show((Node) event.getSource(), event.getScreenX(), event.getScreenY()));
    }

    protected void setUpMenu()
    {

    }

    protected List<MenuItem> initializeMenuItem() {
        List<MenuItem> tmp = new ArrayList<>();
        ctMenu.getItems().clear();
        ImageView renameIcon = new ImageView("icons/cte/renameIc.png");
        renameIcon.setFitWidth(15);
        renameIcon.setFitHeight(15);
        MenuItem reNameItem = new MenuItem("Rename", renameIcon);
        reNameItem.setOnAction((t) ->{
            FixName();
        });
        tmp.add(reNameItem);

        ImageView unparIcon = new ImageView("icons/cte/unParIc.png");
        unparIcon.setFitWidth(15);
        unparIcon.setFitHeight(15);
        MenuItem Unparent = new MenuItem("Unset parent", unparIcon );
        Unparent.setOnAction((t) -> {
            System.out.println("Unparent");
            cteMain.makeUnparentCommand(this);
        });
        tmp.add(Unparent);

        ImageView deleteIc = new ImageView("icons/cte/deleteIc.png");
        deleteIc.setFitWidth(15);
        deleteIc.setFitHeight(15);
        MenuItem Delete = new MenuItem("Delete", deleteIc);
        Delete.setOnAction((t) ->{
            System.out.println("Delete Node");
            cteMain.makeDeleteCommand();
        });
        tmp.add(Delete);

        ImageView subtreeIcon = new ImageView("icons/cte/subtreeIc.png");
        subtreeIcon.setFitWidth(15);
        subtreeIcon.setFitHeight(15);
        MenuItem chooseSubTree = new MenuItem("Choose SubTree", subtreeIcon);
        chooseSubTree.setOnAction((t) -> {
            System.out.println("Choose SubTree");
            cteMain.getCteViewManager().chooseSubTree(this);
        });
        tmp.add(chooseSubTree);

        ImageView changeCorlIcon = new ImageView("icons/cte/colorPicker.png");
        changeCorlIcon.setFitWidth(15);
        changeCorlIcon.setFitHeight(15);
        MenuItem changeColor = new MenuItem("Change Color", changeCorlIcon);
        changeColor.setOnAction((t) -> {
            ColorChangeController colorChange = ColorChangeController.newInstance(cteMain.getCteViewManager(), getDecorColor());
            colorChange.initOwnerStage(UIController.getPrimaryStage());
            colorChange.show();
        });
        tmp.add(changeColor);

        ImageView setParIcon = new ImageView("icons/cte/setParIc.png");
        setParIcon.setFitWidth(15);
        setParIcon.setFitHeight(15);
        MenuItem setParItem = new MenuItem("Set Parent", setParIcon);
        setParItem.setOnAction((t) -> {
            if (this != this.rootNode) {
                cteMain.makeSetParentCommand(this);
            }
        });
        tmp.add(setParItem);

        ImageView cutIcon = new ImageView("icons/cte/cutIc.png");
        cutIcon.setFitWidth(15);
        cutIcon.setFitHeight(15);
        MenuItem Cut = new MenuItem("Cut", cutIcon);
        Cut.setOnAction((t) -> {
            System.out.println("Cut");
            cteMain.makeCutCommand();
        });
        tmp.add(Cut);

        ImageView copyIcon = new ImageView("icons/cte/copyIc.png");
        copyIcon.setFitWidth(15);
        copyIcon.setFitHeight(15);
        MenuItem Copy = new MenuItem("Copy", copyIcon);
        Copy.setOnAction((t) -> {
            System.out.println("Copy");
            cteMain.makeCopyCommand();
        });
        tmp.add(Copy);

        ImageView pasteIcon = new ImageView("icons/cte/pasteIc.png");
        pasteIcon.setFitWidth(15);
        pasteIcon.setFitHeight(15);
        MenuItem Paste = new MenuItem("Paste", pasteIcon);
        Paste.setOnAction((t) -> {
            System.out.println("Paste");
            cteMain.makePasteCommand(this);
        });
        tmp.add(Paste);


        return tmp;
    }

    protected void setUpDragMultiNode() {
        draggableNodes = cteMain.getCteViewManager().getChosenNodes();
    }

    public void setXPos(double newX)
    {
        this.setPosX(newX);
    }

    public void setYPos(double newY)
    {
        this.setPosY(newY);
    }



    public List<CteUiNode> getChildren() {
        return children;
    }

    public void setChildren(List<CteUiNode> children) {
        this.children = children;
    }

    public Edge getEdges() {
        return edge;
    }

    public void setEdges(Edge _edge) {
        this.edge = _edge;
    }

    public ContextMenu getCtMenu() {
        return ctMenu;
    }

    public double getStartChild() {
        return startChild;
    }

    public void setStartChild(double startChild) {
        this.startChild = startChild;
    }

    //    public double getInitialPosX() {
//        return initialPosX;
//    }


    public double getyForChild() {
        return yForChild;
    }

    public void setUpInitialPos()
    {
        startChild = parent.getStartChild() + CteCoefficent.padY;
    }

    public double getChildWidth() {
        return childWidth;
    }

    public void setChildWidth(double _childWidth) {
        this.childWidth = _childWidth;
        if(this.parent != null)
        {
            this.parent.setChildWidth(_childWidth);
        }
//        for(int i =0; i < children.size(); i++)
//        {
//            children.get(i).setChildWidth(_childWidth);
//        }
    }

    public CteUiNode getLeafNode()
    {
        CteUiNode result;
        if(this.getChildren().size()!=0)
        {
            result = this.getLeafNode();
        }
        else
            result = this;
        return result;
    }



    public int getDeepAdding() {
        return deepAdding;
    }

    public void raiseDeepAdding()
    {
        deepAdding+=1;
    }

    public void resetDeepAdding()
    {
        deepAdding=0;
        if(this.children.size() != 0)
        {
            for(int i =0; i<children.size(); i++)
            {
                this.children.get(i).resetDeepAdding();
            }
        }
    }

    public void setFreeNode(boolean _freeNode) {

        this.freeNode = _freeNode;
        this.applyBgColor();
        for(CteUiNode child: this.getChildren())
        {
            child.setFreeNode(_freeNode);
        }
    }

    public void deleteNode()
    {
        this.TurnOffDataInsert();
        if (this.parent != null) {
            if(this.parent instanceof CteClassNode)
            {

                    if(parent.getChildren().size() == 1 && parent instanceof CteClassNode) {
                        ((CteClassNode) parent).getAlignLine().turnOnMode();
                        CteOTColumn tmpCol = this.cteMain.getOTable().getColByID(this.parent.getCoreId());
                        if (tmpCol != null) {
                            tmpCol.ShowChoices();
                        }
                    }

            }

            this.parent.getChildren().remove(this);
            this.parent.removeChildName(this.getTitle());
        }
        if( this instanceof CteClassNode && this.parent != null) {
                cteMain.getOTable().hideColByID(this.getCoreId()); // nên lưu lại id de sau khi khong the undo delete thi delete

        }

        cteMain.clearNodeFromScreen(this);

        System.out.println("Remove Node");
        this.edge = null;


        this.cteMain.getTreeManager().getTree().searchNodeById(this.getCoreId()).delete();
        for(CteUiNode child: this.getChildren())
        {
            cteMain.getChildren().remove(child.getEdges());
            child.setEdges(null);
            child.setParent(null);
            child.setFreeNode(true);
            child.hideColForNode();
            cteMain.getCteViewManager().getFreeTree().add(child);
        }

        if(this instanceof CteClassificationNode)
        {
            for(int i = 0; i < cteMain.getOTable().getRowList().size(); i++)
            {
                cteMain.getOTable().getRowList().get(i).deleteGroupByID(this.getCoreId()); // xem để làm undelete
            }
            rootNode.getClassificationNodesList().remove(this);
        }
        cteMain.removeNodeFrommTreeList(this);
        this.rootNode = null;
        this.getChildren().clear();
    }

    public void hideColForNode()
    {
        for (CteUiNode child: this.getChildren()) {
            child.hideColForNode();
        }

    }

    public void unparent()
    {
        this.clearValueOfTestcase();
        if(parent != null)
        {
            parent.removeChildName(this.getTitle());
            this.parent.getChildren().remove(this);
            this.cteMain.getTreeManager().getTree().searchNodeById(parent.getCoreId()).removeChild(this.getCoreId());
            if(this.parent instanceof CteClassNode)
            {
                this.parent.showLineNChoiceForAllLeaf();
            }
        }

        setBgColorForNodes(CteCoefficent.NodeBgColor.freeNodeCor);
        cteMain.clearEdgeOfNode(this);
        this.edge = null;
        this.setParent(null);
        this.setFreeNode(true);
        this.hideColForNode();
        cteMain.getCteViewManager().getFreeTree().add(this);
    }

    private void clearValueOfTestcase()
    {
        if(this instanceof CteClassNode)
        {
            this.setValueForTestcase("");
        }
        else
        {
            if (this.children.size() > 0)
            {
                for(int i = 0; i < children.size(); i++)
                {
                    children.get(i).clearValueOfTestcase();
                }
            }
        }
    }

    public void setAsParent(CteUiNode childNode, Integer index){}

    public void checkSetParentCondition(CteUiNode parentNode)
    {

    }

//    public void hideColForNode()
//    {
//        if(this.getChildren().size() == 0 && this.alignLine != null)
//        {
//            this.alignLine.setFreeNode(true);
//            this.getCteMain().getOTable().hideColByID(this.getCoreId());
//        }
//        else {
//            for (int i = 0; i < this.getChildren().size(); i ++) {
//                this.getChildren().get(i).hideColForNode();
//            }
//        }
//    }


    public CteUiNode getParentNode() {
        return parent;
    }

    public CteCompositionNode getRootNode() {
        return rootNode;
    }

    public void setRootNode(CteCompositionNode rootNode) {
        this.rootNode = rootNode;
    }

    public void setRootNode()
    {
        this.setRootNode(this.getParentNode().getRootNode());
    }

//    public void setOnSettingParent()
//    {
//        this.cteMain.getCteViewManager().setAssignNode(this);
//        this.cteMain.getCteViewManager().setOnMode(true);
//    }

    public boolean isFreeNode() {
        return freeNode;
    }

    protected void setUpMouse()
    {
        CteUiNode tmpNode = this;
        setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                if(cteMain.getCteViewManager().hasSetParentCommand())
                {
                    if(t.getButton().equals(MouseButton.PRIMARY)) {
                        if (t.getClickCount() == 1) {
                            System.out.println("Primary Press");
                            checkSetParentCondition(tmpNode);

                        }
                    }
                }
                else //ViewDataInsert
                {
                    if (t.getButton().equals(MouseButton.PRIMARY)) {
                        if (t.getClickCount() == 2){
                            FixName();
                            System.out.println("FixName");
                        }
                        else if (t.getClickCount() == 1) {
                            cteMain.getCteViewManager().addChosenNode((CteUiNode)t.getSource(), t.isControlDown(), !t.isDragDetect());
                        }
                    }
                    else if (t.getButton().equals(MouseButton.SECONDARY))
                    {
                        if(!isChoosing) cteMain.getCteViewManager().addChosenNode((CteUiNode)t.getSource(), t.isControlDown(), !t.isDragDetect());
                    }
                }
            }
        });

    }

    public String getCoreId() {
        return id;
    }

    public void setCoreId(String id) {
        this.id = id;
    }

//    public void CreateAlignLine()
//    {
//        alignLine = new CteAlignLine(this.getX() + this.getWidth()/2, this.getY() + this.getHeight());
//        alignLine.startXProperty().bind(this.xProperty().add(this.getWidth()/2));
//        alignLine.startYProperty().bind(this.yProperty().add(this.getHeight()));
//        cteMain.getChildren().add(alignLine);
//        cteMain.getAlignLineList().add(alignLine);
//    }
//
//    public void DeleteAlignLine()
//    {
//        cteMain.getChildren().remove(alignLine);
//        cteMain.getAlignLineList().remove(alignLine);
//        alignLine = null;
//    }

//    public void ChangeALignLinePos()
//    {
//        if(alignLine!= null) {
//            this.alignLine.setXPos(this.getX() + this.getWidth() / 2);
//            this.alignLine.setYPos(this.getY() + this.getHeight());
//        }
//    }

//    public CteAlignLine getAlignLine() {
//        return alignLine;
//    }

    public void setChildForNodeInCore(String childID)
    {
        if( cteMain.getTreeManager().getTree().searchNodeById(this.getCoreId()) == null) System.out.println("Can't not find parent with ID " + this.getCoreId());
        else if( cteMain.getTreeManager().getTree().searchNodeById(childID) == null )   System.out.println("Can't not find child with ID " + childID);
        else
        cteMain.getTreeManager().getTree().searchNodeById(this.getCoreId()).addExistedChild( cteMain.getTreeManager().getTree().searchNodeById(childID));
    }

    public void createTestCaseForNode()
    {
        //Tan: chuyen Testcase sang core
        TestCase temp = cteMain.getTreeManager().getTree().searchNodeById(id).getTestcase();
        if (temp != null && temp.getRootDataNode() != null) {
            testCase = temp;
        }

    }

    public TestCase getTestCase() {
        return testCase;
    }

    public boolean isLeaf()
    {
        return this.getChildren().size() == 0;
    }

    protected void TurnOffDataInsert()
    {
        if(!this.cteMain.getDInsert().RemoveTestCaseByNodeID(this.getCoreId()))
        {
            if(!this.isLeaf())
            {
                for(int i = 0; i < this.getChildren().size(); i++)
                {
                    boolean a = cteMain.getDInsert().RemoveTestCaseByNodeID(this.getChildren().get(i).getCoreId());
                    if(a) break;
                }
            }
        }
    }

    public void FixName()
    {
        cteMain.setMode(CteCoefficent.CteViewMode.ENTERING_RENAME);
        setUpTextField();
        label.setVisible(false);
        tfield.setVisible(true);
        tfield.toFront();

        tfield.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {  //hinh nhu ko co tac dung
                if(cteMain.getMode() == CteCoefficent.CteViewMode.ENTERING_RENAME); cteMain.setMode(CteCoefficent.CteViewMode.RENAMING);
            }   //
        });

        this.cteMain.setRenamingNode(this);
    }

    @Override
    public void setTitle(String _title)
    {
        if(!title.equals(_title)) {
            String tmp = _title.trim();
            if (tmp.isEmpty()) {
                UIController.showErrorDialog("Node's name cannot be blank", "Invalid Name For Node", "Blank Name");
            } else {
                if (parent != null && parent.childNameExisted(_title)) {
                    UIController.showErrorDialog("There's a node with the same content in the group", "Invalid Name For Node", "Duplicated Name");
                } else {
                    if (parent != null) {
                        parent.removeChildName(title);
                        parent.addChildName(_title);
                    }

                    title = _title;
                    label.setText(title);
                    if (cteMain != null) {
                        this.cteMain.getTreeManager().getTree().searchNodeById(this.getCoreId()).setName(_title);
                        this.cteMain.setMode(CteCoefficent.CteViewMode.RENAMED);
                    }
                    resize();
                    setUpTag();
                    if (edge != null) {
                        edge.reBindEdge();
                    }
                    for (CteUiNode child : children) {
                        Edge tmpE = child.getEdges();
                        if (tmpE != null) tmpE.reBindEdge();
                    }
                    if (this instanceof CteClassNode) {
                        CteAlignLine alignLine = ((CteClassNode) this).getAlignLine();
                        if (alignLine != null) alignLine.startXProperty().bind(this.xProperty().add(this.getWidth() / 2));
                    }
                }
            }
        }
    }



    @Override
    public void changeName() {
        String name = this.getNewName();
        cteMain.makeChangeNameCommand(this, name);
    }

    public String getTag() {
        return Tag;
    }

    private void setTag(String tag) {
        Tag = tag;
    }

    public void setUpTag()
    {
        if(this.getParentNode() != null)
        {
            if(this.getTitle().toLowerCase().equals("input"))
            {
                String tmpTag = "input \n end.";
                this.setTag(tmpTag);
            }
            else if(this.getTitle().toLowerCase().equals("output"))
            {
                String tmpTag = "output \n end.";
                this.setTag(tmpTag);
            }
            else if(this.getTitle().toLowerCase().equals("global"))
            {
                String parTag = removeEndInTag(this.getParentNode().getTag());
                String tmpTag = parTag + "global \n end.";
                this.setTag(tmpTag);
            }
            else if(this.getTitle().toLowerCase().equals("argument"))
            {
                String parTag = removeEndInTag(this.getParentNode().getTag());
                String tmpTag =  parTag + "argument \n end.";
                this.setTag(tmpTag);
            }
            else if(this.getTitle().toLowerCase().equals("return"))
            {
                String tmpTag ="return \n end.";
                this.setTag(tmpTag);
            }
            else{
                String parTag = this.getParentNode().getTag();
                if(parTag.equals("Others"))
                {
                    this.setTag("Others");
                }
                else if(this.isChildOfClass())
                {
                    this.setTag(this.getTopClassParent().getTag());
                }
                else if(this instanceof CteClassNode)
                {
                    this.setTag(parTag);
                    //setValueForTestcase();
                } else {
                    if(parTag.contains("global \n end.") || parTag.contains("argument \n end.") ||
                            parTag.contains("struct: " + getParentNode().getTitle() + " \n end.") || parTag.contains("type_array \n end."))
                    {
                        if(this instanceof CteCompositionNode)
                        {
                            String tmpParTag = removeEndInTag(parTag);
                            String tmpTag = tmpParTag + "struct: " + this.getTitle() + " \n end.";
                            this.setTag(tmpTag);
                        }
                        else
                        {
                            if(this.getTitle().contains("["))
                            {
                                if(!parTag.contains("type_array \n end."))
                                {
                                    int index = parTag.lastIndexOf("struct: ");
                                    String tmpTag = parTag.substring(0, index - 1);
                                    String newTag = tmpTag + " type_array \n end.";
                                    getParentNode().setTag(newTag);
                                    parTag = this.getParentNode().getTag();
                                }

                                String tmpTag = removeEndInTag(parTag);
                                int indexOfOpen = this.getTitle().indexOf("[");
                                String name = this.getTitle().substring(0, indexOfOpen);
                                String pos = getPosOfElement(this.getTitle());
                                String Ntag = tmpTag + "name: " + name + " \n" + " pos: " + pos + " \n end.";

                                this.setTag(Ntag);
                            }
                            else
                            {
                                String tmpTag = removeEndInTag(parTag);
                                String Ntag = tmpTag + "name: " + this.getTitle() + " \n end.";
                                this.setTag(Ntag);
                            }
                        }
                    }
                    else
                    {
                        this.setTag("Others");
                    }
                }


            }
        }
        if(this instanceof CteClassNode && this.getParentNode() != null)
        {
            if(! title.contains("_copy"))  this.setValueForTestcase(this.title);
        }
        this.setUpTagForChildren();
    }

    private String getPosOfElement(String name)
    {
        String posOnly = name.substring(name.indexOf("["), name.lastIndexOf("]"));
        List<String> posList = Arrays.asList(posOnly.split("]"));
        List<String> result = new ArrayList<>();

        for(int i = 0; i < posList.size(); i++)
        {
            result.add(posList.get(i).substring(1));
        }

        String res  = "";
        for(int i = 0; i < result.size(); i++)
        {
            res = res + result.get(i) + "x";
        }

        res = res.substring(0, res.lastIndexOf("x"));

        return res;
    }

    private String getStringAfter(String tag, String kindOfField)
    {
        String res;
        if(tag.indexOf(kindOfField) < 0) return null;
        String temp = tag.substring(tag.indexOf(kindOfField) + kindOfField.length());
        res = temp.substring(0, temp.indexOf(" \n"));
        return res;
    }

    private String removeEndInTag(String tag)
    {
        int index = tag.indexOf("end.");
        if(index > -1) return tag.substring(0, index);
        else return null;
    }

    private int calculateSizeOfArray(String tag)
    {
        String pos = getStringAfter(tag, "pos: ");
        if(pos != null) {
            if (pos.contains("x") == false) {
                return Integer.parseInt(pos) + 1;
            } else {
                List<String> posList = Arrays.asList(pos.split("x"));
                int result = 1;
                for (int i = 0; i < posList.size(); i++) {
                    result *= (Integer.parseInt(posList.get(i)) + 1);
                }
                return result;
            }
        }
        return -1;
    }

    private int[] calculateSizeOfMulDiArray(String tag)
    {
        String pos = getStringAfter(tag, "pos: ");
        if(pos != null && pos.contains("x")) {
            List<String> posList = Arrays.asList(pos.split("x"));
            int[] result = new int[posList.size()];
            for (int i = 0; i < posList.size(); i++) {
                result[i] = (Integer.parseInt(posList.get(i)) + 1);
            }
            return result;

        }
        return null;
    }

//    public void sUTag() {
//        if (this.getParentNode() == null) {
//
//            this.setTag(CteCoefficent.CteNodeTag.Root);
//        } else {
//            if (this.parent.getTag().equals(CteCoefficent.CteNodeTag.Root)) {
//                if (this.getLabel().getText().equals("input")) this.setTag(CteCoefficent.CteNodeTag.InputZ);
//                else if (this.getLabel().getText().equals("output")) this.setTag(CteCoefficent.CteNodeTag.OutputZ);
//                else this.setTag(CteCoefficent.CteNodeTag.Undefined);
//            } else if (this.parent.getTag().equals(CteCoefficent.CteNodeTag.InputZ)) {
//                if (this.getLabel().getText().equals("global")) this.setTag(CteCoefficent.CteNodeTag.InputGlobal);
//                else if (this.getLabel().getText().equals("argument")) this.setTag(CteCoefficent.CteNodeTag.InputArg);
//                else this.setTag(CteCoefficent.CteNodeTag.Undefined);
//            } else if (this.parent.getTag().equals(CteCoefficent.CteNodeTag.OutputZ)) {
//                if (this.getLabel().getText().equals("global")) this.setTag(CteCoefficent.CteNodeTag.OutputGlobal);
//                else if (this.getLabel().getText().equals("argument"))
//                    this.setTag(CteCoefficent.CteNodeTag.OutputArg);
//                else if (this.getLabel().getText().equals("return")) this.setTag(CteCoefficent.CteNodeTag.OutputReturn);
//                else this.setTag(CteCoefficent.CteNodeTag.Undefined);
//            } else if (this.parent.getTag().equals(CteCoefficent.CteNodeTag.InputArg)) {
//                if(this instanceof CteCompositionNode)
//                {
//                    this.setTag(CteCoefficent.CteNodeTag.InputArgUS);
//                }
//                else this.setTag(CteCoefficent.CteNodeTag.InputArgC);
//            } else if (this.parent.getTag().equals(CteCoefficent.CteNodeTag.OutputArg)) {
//                if(this instanceof CteCompositionNode)
//                {
//                    this.setTag(CteCoefficent.CteNodeTag.OutputArgUS);
//                }
//                else this.setTag(CteCoefficent.CteNodeTag.OutputArgC);
//            } else if (this.parent.getTag().equals(CteCoefficent.CteNodeTag.InputGlobal)) {
//                if(this instanceof CteCompositionNode)
//                {
//                    this.setTag(CteCoefficent.CteNodeTag.InputGlobalArgUS);
//                }
//                else this.setTag(CteCoefficent.CteNodeTag.InputGlobArg);
//            } else if (this.parent.getTag().equals(CteCoefficent.CteNodeTag.InputGlobArg)) {
//                this.setTag(CteCoefficent.CteNodeTag.InputGlobVal);
//            } else if (this.parent.getTag().equals(CteCoefficent.CteNodeTag.InputGlobalArgUS)) {
//                this.setTag(CteCoefficent.CteNodeTag.InputGlobalArgUSField);
//            } else if (this.parent.getTag().equals(CteCoefficent.CteNodeTag.InputGlobalArgUSField)) {
//                this.setTag(CteCoefficent.CteNodeTag.InputGlobalArgUSFieldVal);
//            } else if (this.parent.getTag().equals(CteCoefficent.CteNodeTag.OutputGlobal)) {
//                if(this instanceof CteCompositionNode)
//                {
//                    this.setTag(CteCoefficent.CteNodeTag.OutputGlobalArgUS);
//                }
//                else this.setTag(CteCoefficent.CteNodeTag.OutputGlobArg);
//            }
//            else if (this.parent.getTag().equals(CteCoefficent.CteNodeTag.OutputGlobArg)) {
//                this.setTag(CteCoefficent.CteNodeTag.OutputGlobVal);
//            }
//            else if (this.parent.getTag().equals(CteCoefficent.CteNodeTag.OutputGlobalArgUS)) {
//                this.setTag(CteCoefficent.CteNodeTag.OutputGlobalArgUSField);
//            }
//            else if (this.parent.getTag().equals(CteCoefficent.CteNodeTag.OutputGlobalArgUSField)) {
//                this.setTag(CteCoefficent.CteNodeTag.OutputGlobalArgUSFieldVal);
//            }
//            else if (this.parent.getTag().equals(CteCoefficent.CteNodeTag.InputArgC))
//            {
//                this.setTag(CteCoefficent.CteNodeTag.InputArgVal);
//            }
//            else if(this.parent.getTag().equals(CteCoefficent.CteNodeTag.InputArgUS))
//            {
//                this.setTag(CteCoefficent.CteNodeTag.InputArgUSField);
//            }
//            else if(this.parent.getTag().equals(CteCoefficent.CteNodeTag.InputArgUSField))
//            {
//                this.setTag(CteCoefficent.CteNodeTag.InputArgUSFieldVal);
//            }
//            else if (this.parent.getTag().equals(CteCoefficent.CteNodeTag.OutputArgC))
//            {
//                this.setTag(CteCoefficent.CteNodeTag.OutputArgVal);
//            } else if (this.parent.getTag().equals(CteCoefficent.CteNodeTag.OutputArgUS))
//            {
//                this.setTag(CteCoefficent.CteNodeTag.OutputArgUSField);
//            }
//            else if (this.parent.getTag().equals(CteCoefficent.CteNodeTag.OutputArgUSField))
//            {
//                this.setTag(CteCoefficent.CteNodeTag.OutputArgUSFieldVal);
//            }else if (this.parent.getTag().equals(CteCoefficent.CteNodeTag.InputArgVal) ||
//                            this.parent.getTag().equals(CteCoefficent.CteNodeTag.InputArgUSFieldVal) ||
//                            this.parent.getTag().equals(CteCoefficent.CteNodeTag.OutputArgVal) ||
//                            this.parent.getTag().equals(CteCoefficent.CteNodeTag.OutputArgUSFieldVal) ||
//                            this.parent.getTag().equals(CteCoefficent.CteNodeTag.ArgVChild) ||
//                            this.parent.getTag().equals(CteCoefficent.CteNodeTag.ReturnVal)  ) {
//                this.setTag(CteCoefficent.CteNodeTag.ChildOfClass);
//            }
//            else if (this.parent.getTag().equals(CteCoefficent.CteNodeTag.OutputReturn))
//            {
//                this.setTag(CteCoefficent.CteNodeTag.ReturnVal);
//            } else if (this.parent.getTag().equals(CteCoefficent.CteNodeTag.Undefined)) {
//                this.setTag(CteCoefficent.CteNodeTag.Undefined);
//            }
//            else if(this.parent.getTag().equals(CteCoefficent.CteNodeTag.ChildOfClass)){
//                if(this instanceof CteClassificationNode || this instanceof CteCompositionNode) {
//                    this.setTag(CteCoefficent.CteNodeTag.ChildOfClass);
//                }
//                else {
//                    this.setTag(this.getParentNode().getTopClassParent().getTag());
//                }
//            }
//            else {
//                this.setTag(CteCoefficent.CteNodeTag.ChildOfClass);
//            }
//        }
//        this.setUpTagForChildren();
//        if(this instanceof CteClassNode && this.getParentNode() != null ) this.setValueForTestcase();
//    }

    private void setUpTagForChildren()
    {
        if(this.getChildren().size() != 0)
        {
            for(int i = 0; i < this.getChildren().size(); i++)
            {
                this.getChildren().get(i).setUpTag();
            }
        }
    }

    public void setValueForTestcase(String Val)
    {
        if(testCase != null && !this.freeNode && this instanceof CteClassNode)
        {
            String parentTitle; // parent Node
            String parentTitle2; // parent of parent

            if(parent.isChildOfClass())
            {
                parentTitle2 = parent.getTopClassParent().getParentNode().getParentNode().getTitle(); //parent of parent
                parentTitle = parent.getTopClassParent().getParentNode().getTitle(); //  parent
            }
            else  {
               if(this.parent != null && this.parent.getParentNode() != null) {
                   parentTitle2 = this.parent.getParentNode().getTitle();
                   parentTitle = this.parent.getTitle();
               }
               else
               {
                    parentTitle = "";
                    parentTitle2= "";
               }

            }

            if (this.getTag().contains("input"))
            {
                if(this.getTag().contains("struct"))
                {
                    if(this.getTag().contains("type_array"))
                    {
                        String structTitle, arrayTitle, arrayElementTitle;
                        structTitle = getStringAfter(this.getTag(), "struct: ");
                        arrayTitle = getStringAfter(this.getTag(), "name: ");
                        parentTitle2 = structTitle;
                        parentTitle = arrayTitle;
                    }
                    ValueDataNode node = this.getDataNodeForInput(this.testCase.getRootDataNode(), parentTitle2);

                    if(node instanceof UnionDataNode)
                    {
                        UnionDataNode auxNode = (UnionDataNode) node;
                        auxNode.setField(parentTitle);
                        try {
                            new TreeExpander().expandStructureNodeOnDataTree(auxNode, parentTitle);
                        } catch (Exception e) {
                        }
                        if(!auxNode.getChildren().isEmpty())  setDataNodeValue((ValueDataNode) auxNode.getChildren().get(0), Val);
                    }
                    else if(node instanceof StructDataNode)
                    {
                        if(this.getTag().contains("type_array"))
                        {
                            String arrayElementTitle = this.getParentNode().getTitle();
                            ValueDataNode arrayNode = this.getAttributeNodeFromStructNode((StructDataNode) node, parentTitle);
                            if(arrayNode instanceof ArrayDataNode)
                            {
                                ArrayDataNode tmpArrayNode = (ArrayDataNode) arrayNode;
                                setSizeForArrayNode(tmpArrayNode);
                            }
                            ValueDataNode mainNode = this.getDataNodeForInput(arrayNode, arrayElementTitle);
                            setDataNodeValue(mainNode, Val);

                        }
                        else {
                            ValueDataNode structNode = this.getDataNodeForInput(this.testCase.getRootDataNode(), parentTitle2);
                            ValueDataNode mainNode = this.getAttributeNodeFromStructNode((StructDataNode) structNode, parentTitle);
                            setDataNodeValue(mainNode, Val);
                        }
                    }
                }
                else
                {
                    if(this.getTag().contains("global"))
                    {
                        ValueDataNode node = this.getDataNodeForInput(this.testCase.getRootDataNode(), parentTitle);
                        setDataNodeValue(node, Val);
                    }
                    else {
                        if(this.getTag().contains("type_array"))
                        {
                            ValueDataNode node = this.getDataNodeForInput(this.testCase.getRootDataNode(), parentTitle2);
                            if(node instanceof  ArrayDataNode)  { setSizeForArrayNode((ArrayDataNode) node); }
                            ValueDataNode mainNode = this.getDataNodeForInput(node, parentTitle);
                            setDataNodeValue(mainNode, Val);

                        }
                        else
                        {
                            ValueDataNode node = this.getDataNodeForInput(this.testCase.getRootDataNode(), parentTitle);
                            setDataNodeValue(node, Val);
                        }

                    }
                }
            }
            else if(this.getTag().contains("return"))
            {
                ValueDataNode node = this.getDataNodeForInput(this.getTestCase().getRootDataNode(), "RETURN");
                setDataNodeValue(node, Val);
            }
            else if(this.getTag().equals("Others"))
            {

            }
            else if(this.getTag().contains("output"))
            {
                if(this.getTag().contains("struct"))
                {
                    if(this.getTag().contains("type_array"))
                    {
                        String structTitle, arrayTitle, arrayElementTitle;
                        structTitle = getStringAfter(this.getTag(), "struct: ");
                        arrayTitle = getStringAfter(this.getTag(), "name: ");
                        parentTitle2 = structTitle;
                        parentTitle = arrayTitle;
                    }
                    ValueDataNode node = this.getDataNodeForInput(this.testCase.getRootDataNode(), parentTitle2);
                    ValueDataNode mainNode;
                    if(this.getTag().contains("global"))
                    {
                        mainNode = getGlobExpectedDataNodeForInput(this.testCase.getRootDataNode(), node);
                    }
                    else mainNode = getExpectedDataNodeForInput(this.testCase.getRootDataNode(), node);
                    //else mainNode = getGlobExpectedDataNodeForInput(this.testCase.getRootDataNode(), node);
                    String auxField = new String(parentTitle);
                    if(mainNode instanceof UnionDataNode)
                    {
                        UnionDataNode auxNode = (UnionDataNode) mainNode;
                        auxNode.setField(auxField);
                        try {
                            new TreeExpander().expandStructureNodeOnDataTree(auxNode, auxField);
                        } catch (Exception e) {
                        }
                        if(!auxNode.getChildren().isEmpty()) {
                            ValueDataNode expectedNode = (ValueDataNode) auxNode.getChildren().get(0);
                            setDataNodeValue(expectedNode, Val);
                        }

                    }
                    else if(mainNode instanceof StructDataNode)
                    {
                        if(this.getTag().contains("type_array"))
                        {
                            String arrayElementTitle = this.getParentNode().getTitle();
                            ValueDataNode arrayNode = this.getAttributeNodeFromStructNode((StructDataNode) mainNode, parentTitle);
                            if(arrayNode instanceof ArrayDataNode)
                            {
                                ArrayDataNode tmpArrayNode = (ArrayDataNode) arrayNode;
                                setSizeForArrayNode(tmpArrayNode);
                            }
                            ValueDataNode mainAttNode = this.getDataNodeForInput(arrayNode, arrayElementTitle);
                            setDataNodeValue(mainAttNode, Val);

                        }
                        else {
                            ValueDataNode keyNode = this.getDataNodeForInput(this.testCase.getRootDataNode(), parentTitle2);
                            ValueDataNode expectedNode = getExpectedDataNodeForInput(this.testCase.getRootDataNode(), keyNode);
                            ValueDataNode mainAttNode = getAttributeNodeFromStructNode((StructDataNode) expectedNode, parentTitle);
                            setDataNodeValue(mainAttNode, Val);
                        }
                    }
                }
                else {
                    if(this.getTag().contains("global"))
                    {
                        ValueDataNode keyNode = this.getDataNodeForInput(this.testCase.getRootDataNode(),parentTitle);
                        ValueDataNode expectedNode = getGlobExpectedDataNodeForInput(this.testCase.getRootDataNode(), keyNode);
                        setDataNodeValue(expectedNode, Val);
                    }
                    else {
                        if(this.getTag().contains("type_array"))
                        {
                            ValueDataNode keyNode = this.getDataNodeForInput(this.testCase.getRootDataNode(), parentTitle2);
                            ValueDataNode expectNode = getGlobExpectedDataNodeForInput(this.testCase.getRootDataNode(), keyNode);
                            if(expectNode instanceof  ArrayDataNode)  { setSizeForArrayNode((ArrayDataNode) expectNode); }
                            ValueDataNode mainNode = this.getDataNodeForInput(expectNode, parentTitle);
                            setDataNodeValue(mainNode, Val);

                        }
                        else {
                            ValueDataNode keyNode = this.getDataNodeForInput(this.testCase.getRootDataNode(), parentTitle);
                            ValueDataNode expectedNode = getExpectedDataNodeForInput(this.testCase.getRootDataNode(), keyNode);
                            setDataNodeValue(expectedNode, Val);
                        }
                    }
                }
            }
        }
    }

    private void setSizeForArrayNode(ArrayDataNode node)
    {
        if(node instanceof OneDimensionDataNode) {
            OneDimensionDataNode tmpNode = (OneDimensionDataNode) node;
            int size = calculateSizeOfArray(this.getTag());
            if (size > 0) {
                if(tmpNode.getSize() < size) {
                    tmpNode.setSize(size);
                    tmpNode.setSizeIsSet(true);
                    if (size <= 20) {
                        TreeExpander expander = new TreeExpander();
                        try {
                            expander.expandTree(tmpNode);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        else if(node instanceof MultipleDimensionDataNode)
        {
            MultipleDimensionDataNode tmpNode = (MultipleDimensionDataNode) node;
            int[] size = calculateSizeOfMulDiArray(this.getTag());
            if (size != null) {
                tmpNode.setSizes(size);
                tmpNode.setSizeIsSet(true);
                int totalsize = 1;
                for(int i = 0; i < tmpNode.getDimensions(); i++)
                {
                    totalsize *= tmpNode.getSizeOfDimension(i);
                }
                if (totalsize <= 20) {
                    TreeExpander expander = new TreeExpander();
                    try {
                        expander.expandTree(tmpNode);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private ValueDataNode getDataNodeForInput(IDataNode node, String name)
    {
       for(IDataNode cNode : node.getChildren())
       {
           if(cNode instanceof ValueDataNode && (cNode.getName().equals(name)))
           {
               return (ValueDataNode) cNode;
           }
           else if(cNode.getChildren().size() != 0)
           {
               ValueDataNode tmp = this.getDataNodeForInput(cNode, name);
               if(tmp !=  null) return tmp;
               else continue;
           }
       }

        return null;
    }


    private ValueDataNode getExpectedDataNodeForInput(IDataNode rootNode, ValueDataNode keyNode)
    {
        SubprogramNode subprogramNode = findSubProgramNode(rootNode);
        if(subprogramNode != null)
        {
            Map ExpectedMap = subprogramNode.getInputToExpectedOutputMap();
            return (ValueDataNode) ExpectedMap.get(keyNode);
        }
        return null;
    }

    private ValueDataNode getAttributeNodeFromStructNode(StructDataNode structNode, String name)
    {
        if(!structNode.getChildren().isEmpty())
            for(int i = 0; i < structNode.getChildren().size();i++)
            {
                if(structNode.getChildren().get(i).getName().equals(name) && structNode.getChildren().get(i) instanceof ValueDataNode)
                    return (ValueDataNode) structNode.getChildren().get(i);
            }
        return null;
    }

    private SubprogramNode findSubProgramNode(IDataNode node)
    {
        if(node.getChildren().size() != 0)
        {
            for(int i = 0; i < node.getChildren().size(); i++)
            {
                IDataNode tmpNode =  node.getChildren().get(i);
                if(tmpNode instanceof SubprogramNode)
                {
                    return (SubprogramNode) tmpNode;
                }
                else
                {
                    SubprogramNode tmpSubNode = findSubProgramNode(tmpNode);
                    if(tmpSubNode == null) continue;
                    else return tmpSubNode;
                }
            }
        }
        return null;

    }

    private ValueDataNode getGlobExpectedDataNodeForInput(IDataNode rootNode, ValueDataNode keyNode)
    {
        GlobalRootDataNode globalRootDataNode = findRootGlobalNode(rootNode);
        if(globalRootDataNode != null)
        {
            Map ExpectedMap = globalRootDataNode.getGlobalInputExpOutputMap();
            ValueDataNode expectedNode = (ValueDataNode) ExpectedMap.get(keyNode);
            if(expectedNode != null)
            {
                return expectedNode;
            }
        }
        return null;
    }

    private GlobalRootDataNode findRootGlobalNode(IDataNode node)
    {
        if(node.getChildren().size() != 0)
        {
            for(int i = 0; i < node.getChildren().size(); i++)
            {
                IDataNode tmpNode =  node.getChildren().get(i);
                if(tmpNode instanceof GlobalRootDataNode)
                {
                    return (GlobalRootDataNode) tmpNode;
                }
                else
                {
                    GlobalRootDataNode tmpSubNode = findRootGlobalNode(tmpNode);
                    if(tmpSubNode == null) continue;
                    else return tmpSubNode;
                }
            }
        }
        return null;
    }

    private void setDataNodeValue(ValueDataNode dataNode, String nvalue) {
        if (dataNode == null) {
            System.out.println("Cannot Find DataNode");
        }
        else if(nvalue.isEmpty())
        {
            if (dataNode instanceof NormalDataNode) {
                NormalDataNode normalNode = (NormalDataNode) dataNode;
                normalNode.setValue(null);
            } else if (dataNode instanceof EnumDataNode) {
                EnumDataNode enumNode = (EnumDataNode) dataNode;
                enumNode.setValue(null);
                enumNode.setValueIsSet(false);
            }

            cteMain.getCteViewManager().reloadDataInsert(this);
        }
        else {
            boolean ValChanged = true;
            String newValue = nvalue;
            if (dataNode instanceof NormalNumberDataNode) {
                NormalNumberDataNode tmp = (NormalNumberDataNode) dataNode;
                String type = dataNode.getRealType();
                type = VariableTypeUtils.removeRedundantKeyword(type);
                String normalizedValue = Utils.preprocessorLiteral(newValue);
                if (type.equals("unsigned long long int") || type.equals("unsigned long long")
                        || type.equals("uint64_t") || type.equals("uint_least64_t") || type.equals("uint_fast64_t")) {
                    ((NormalNumberDataNode) dataNode).setValue(newValue);
                } else if (VariableTypeUtils.isNumFloat(type)) {
                    try {
                        double value = Double.parseDouble(normalizedValue);
                        PrimitiveBound bound = Environment.getBoundOfDataTypes().getBounds().get(type);
                        if (bound != null) {
                            if (value >= Double.parseDouble(bound.getLower())
                                    && value <= Double.parseDouble(bound.getUpper())) {
                                if (type.equals("bool")) {
                                    if (newValue.equalsIgnoreCase("true") || newValue.equalsIgnoreCase("false"))
                                        ((NormalNumberDataNode) dataNode).setValue(newValue);
                                    else if (newValue.equals("1"))
                                        ((NormalNumberDataNode) dataNode).setValue("true");
                                    else if (newValue.equals("0"))
                                        ((NormalNumberDataNode) dataNode).setValue("false");
                                    else
                                        UIController.showErrorDialog("Invalid value of bool", "Test data entering", "Invalid value");
                                } else
                                    ((NormalNumberDataNode) dataNode).setValue(newValue);
                            } else {
                                // nothing to do
//                                UIController.showErrorDialog("Value " + value + " out of scope " + bound,
//                                        "Test data entering", "Invalid value");
                                ValChanged = false;
                            }
                        }
                    } catch (Exception e) {
//                        e.printStackTrace();
//                        System.out.println("Do not handle when committing " + dataNode.getClass());
                       // UIController.showErrorDialog("Wrong input for " + dataNode.getName() + "; unit = " + dataNode.getUnit().getName(), "Test data entering", "Invalid value");
                        ValChanged = false;
                    }
                } else {
                    try {
                        long value = Long.parseLong(normalizedValue);
                        if (VariableTypeUtils.isTimet(type) || VariableTypeUtils.isSizet(type)) {
                            ((NormalNumberDataNode) dataNode).setValue(newValue);
                        } else {

                            PrimitiveBound bound = Environment.getBoundOfDataTypes().getBounds().get(type);
                            if (bound != null) {
                                if (value >= Long.parseLong(bound.getLower())
                                        && value <= Long.parseLong(bound.getUpper())) {
                                    if (type.equals(VariableTypeUtils.BASIC.BOOLEAN.BOOL)) {
                                        if (newValue.equalsIgnoreCase("true") || newValue.equalsIgnoreCase("false"))
                                            ((NormalNumberDataNode) dataNode).setValue(newValue);
                                        else if (newValue.equals("1"))
                                            ((NormalNumberDataNode) dataNode).setValue("true");
                                        else if (newValue.equals("0"))
                                            ((NormalNumberDataNode) dataNode).setValue("false");
                                        else
                                            UIController.showErrorDialog("Invalid value of bool", "Test data entering", "Invalid value");
                                    } else
                                        ((NormalNumberDataNode) dataNode).setValue(newValue);
                                } else {
                                    // nothing to do
//                                    UIController.showErrorDialog("Value " + value + " out of scope " + bound,
//                                            "Test data entering", "Invalid value");
                                    ValChanged = false;
                                }
                            }
                        }
                    } catch (Exception e) {
                        ValChanged = false;
                      //  e.printStackTrace();
                       // UIController.showErrorDialog("Wrong input for " + dataNode.getName() + "; unit = " + dataNode.getUnit().getName(), "Test data entering", "Invalid value");
                    }
                }
            } else if (dataNode instanceof NormalCharacterDataNode) {
                // CASE: Type character
                if(newValue.length() == 1)
                {
                    newValue = NormalCharacterDataNode.VISIBLE_CHARACTER_PREFIX + newValue + NormalCharacterDataNode.VISIBLE_CHARACTER_PREFIX;
                }
                if (newValue.startsWith(NormalCharacterDataNode.VISIBLE_CHARACTER_PREFIX)
                        && newValue.endsWith(NormalCharacterDataNode.VISIBLE_CHARACTER_PREFIX)) {
                    String character = newValue.substring(1, newValue.length() - 1);
                    String ascii = NormalCharacterDataNode.getCharacterToACSIIMapping().get(character);
                    if (ascii != null)
                        ((NormalCharacterDataNode) dataNode).setValue(ascii + "");
                    else {
//                        UIController.showErrorDialog("You type wrong character for the type " + dataNode.getRawType()
//                                        + " in src " + dataNode.getUnit().getName() +
//                                        NormalCharacterDataNode.RULE
//                                , "Wrong input of character", "Fail");
//                        System.out.println("Do not handle when the length of text > 1 for character parameter");
                        ValChanged = false;
                    }
                } else {
                    try {
                        // CASE: Type ascii
                        String normalizedValue = Utils.preprocessorLiteral(newValue);
                        long value = Long.parseLong(normalizedValue);
                        DataSizeModel dataSizeModel = Environment.getBoundOfDataTypes().getBounds();
                        String type = dataNode.getRealType();
                        PrimitiveBound bound = dataSizeModel.get(type);
                        if (bound == null)
                            bound = dataSizeModel.get(type.replace("std::", "").trim());
                        if (bound == null) {
                            UIController.showErrorDialog("You type wrong character for the type " + dataNode.getRawType()
                                            + " in src " + dataNode.getUnit().getName() +
                                            NormalCharacterDataNode.RULE
                                    , "Wrong input of character", "Fail");

                        } else if (value <= bound.getUpperAsDouble() && value >= bound.getLowerAsDouble()) {
                            ((NormalCharacterDataNode) dataNode).setValue(newValue);

                        } else {
//                            UIController.showErrorDialog("Value " + newValue + " is out of bound " + dataNode.getRawType()
//                                            + "[" + bound.getUpperAsDouble() + "," + bound.getLowerAsDouble() + "]"
//                                            + " in src " + dataNode.getUnit().getName() +
//                                            NormalCharacterDataNode.RULE
//                                    , "Wrong input of character", "Fail");
                            ValChanged = false;
                        }
                    } catch (Exception e) {
                        //e.printStackTrace();
//                        UIController.showErrorDialog("You type wrong character for the type " + dataNode.getRawType()
//                                + " in src " + dataNode.getUnit().getName() +
//                                NormalCharacterDataNode.RULE, "Wrong input of character", "Fail");
                        System.out.println("Do not handle when the length of text > 1 for character parameter");
                        ValChanged = false;
                    }
                }
//        } else if (dataNode instanceof NormalStringDataNode) {
//            try {
//                long lengthOfString = Long.parseLong(newValue);
//
//                if (lengthOfString < 0)
//                    throw new Exception();
//
//                ((NormalStringDataNode) dataNode).setAllocatedSize(lengthOfString);
//                TreeExpander expander = new TreeExpander();
//                expander.setRealTypeMapping(this.realTypeMapping);
//                expander.expandTree(dataNode);
//            } catch (Exception e) {
//                e.printStackTrace();
//                UIController.showErrorDialog("Length of a string must be >=0 and is an integer", "Wrong length of string", "Invalid length");
//            }
//
//        }
//        else if (dataNode instanceof OtherUnresolvedDataNode) {
//            System.out.println("OtherUnresolvedDataNode");
//            ((OtherUnresolvedDataNode) dataNode).setUserCode(newValue);


            }
            else if(dataNode instanceof EnumDataNode)
            {
                EnumDataNode tmp = (EnumDataNode) dataNode;
                boolean isValue = false;
                for(int i = 0; i < tmp.getAllNameEnumItems().size(); i++)
                {
                    if(tmp.getAllNameEnumItems().get(i).equals(newValue))
                    {
                        isValue = true;
                        break;
                    }
                }

                if(isValue)
                {
                    tmp.setValue(newValue);
                    tmp.setValueIsSet(true);
                }
                else ValChanged = false;
            }
            else if(dataNode instanceof PointerDataNode)
            {
                PointerDataNode tmp = (PointerDataNode) dataNode;
                if (newValue.toLowerCase().equals("null"))
                {
                    tmp.setAllocatedSize(-1);
                    tmp.setSizeIsSet(true);
                }
                else ValChanged = false;
            }
            else if(dataNode instanceof OneDimensionCharacterDataNode)
            {
                char[] newValArray = newValue.toCharArray();
                if(newValArray.length <= dataNode.getChildren().size())
                {
                    for(int i = 0; i < newValArray.length; i++)
                    {
                        setDataNodeValue((ValueDataNode) dataNode.getChildren().get(i), Character.toString(newValArray[i]));
                    }
                }
            }
            else
            {
                System.out.println("Do not support to enter data for " + dataNode.getClass());
                ValChanged = false;
            }

            if(ValChanged) cteMain.getCteViewManager().reloadDataInsert(this);
        }
    }


    public void chosenModeActivate()
    {
        this.setFill(Color.valueOf("b7d3ec"));
        isChoosing = true;
        toFronLabel();

    }

    public void  chosenModeDeactivate(){
        this.setFill(this.bgColor);
        isChoosing = false;
        toFronLabel();
    }

    public boolean isChildOfClass() {
        return false;
    }

    public void addingIdInName()
    {

    }

    public void showLineNChoiceForAllLeaf()
    {
        for(CteUiNode child: this.getChildren())
        {
            child.showLineNChoiceForAllLeaf();
        }
    }

    public List<String> getChildNames() {
        return childNames;
    }

    public void addChildName(String newName)
    {
        if (!newName.equalsIgnoreCase("blank")) childNames.add(newName);
    }

    public boolean childNameExisted(String name)
    {
        if(name.equalsIgnoreCase("blank")) return false;

        for(String child : childNames)
        {
            if(child.equals(name)) return true;
        }
        return false;
    }

    public void removeChildName(String name)
    {
        String tmp = null;
        for(String child : childNames)
        {
            if(child.equals(name)) tmp = child;
        }
        if(tmp != null) childNames.remove(tmp);
    }
    public CteUiNode getClassParent()
    {
        if(this instanceof CteCompositionNode || this instanceof CteClassificationNode)
        {
            if( this.isChildOfClass() )
            {
                if(parent == null) return null;
                else if(parent instanceof CteClassNode) return parent;
                else if(parent instanceof CteCompositionNode || parent instanceof CteClassificationNode)
                {
                return parent.getClassParent();
                }
            }
        }
        return null;
    }

    public CteUiNode getTopClassParent()
    {
        CteUiNode tmp = this.getClassParent();
        if(tmp != null)
        {
            if(tmp.getParentNode().isChildOfClass()) return tmp.getParentNode().getTopClassParent();
            else return tmp;
        }
        return null;
    }

    public void sortChildByPos()
    {
        this.children.sort(new Comparator<CteUiNode>() {
            @Override
            public int compare(CteUiNode o1, CteUiNode o2) {
                if(o1.getX() > o2.getX()) return 1;
                else if(o1.getX() < o2.getX()) return -1;
                else return 0;
            }
        });
    }

    public CteUiNode getTopNode()
    {
        if(this.getParentNode() == null) return this;
        else return  this.getParentNode().getTopNode();
    }

    public void turnOnNodeSelect()
    {
        cteMain.setNodeSelecting(true);
    }

    public int getCoefficentForX() {
        return coefficentForX;
    }

    public boolean isChoosing() {
        return isChoosing;
    }

//    public void changeBgColor()
//    {
//        this.applyBgColor();
//        for(CteUiNode child : children)
//        {
//            child.changeBgColor();
//        }
//    }

    public void setBgColorForNodes(Color colr)
    {
        this.setBgColor(colr);
        for(CteUiNode child : children)
        {
            child.setBgColorForNodes(colr);
        }
    }

}

