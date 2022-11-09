package auto_testcase_generation.cte.UI.CteClassificationTree.CteComponent;

import auto_testcase_generation.cte.UI.CteClassificationTree.CteView;
import auto_testcase_generation.cte.UI.CteCoefficent;
import auto_testcase_generation.cte.UI.OptionTable.CteOTColumn;
import auto_testcase_generation.cte.core.cteNode.CteClass;
import auto_testcase_generation.cte.core.cteNode.CteClassification;
import auto_testcase_generation.cte.core.cteNode.CteComposition;
import auto_testcase_generation.cte.core.cteNode.CteNode;
import com.dse.guifx_v3.helps.UIController;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.WindowEvent;

import java.util.List;


public class CteClassNode extends CteUiNode{

    private CteAlignLine alignLine = null;

    public CteClassNode(double _x,double _y)
    {
        super(_x, _y);
        this.getStrokeDashArray().addAll(15.0, 3.0);
        this.createDecoratedDetail();
    }

    public CteClassNode(double _x,double _y, String _id)
    {
        super(_x, _y, _id);
        this.getStrokeDashArray().addAll(15.0, 3.0);
        this.createDecoratedDetail();
    }


    public CteClassificationNode addClassificationChild()
    {
        this.coefficentForX += 1;
        String ID = this.addClassificationChildInCore();
        CteClassificationNode child = new CteClassificationNode(this.getX() + coefficentForX* CteCoefficent.padX,
                                                                                    this.getY() + CteCoefficent.padY, ID);
        setUpChild(child);

        child.addToRoot();
        child.setIsChildOfClass(true);
        this.getAlignLine().turnOffMode();

        this.cteMain.getOTable().addGroups(child.getCoreId());
        this.hideChoices();
        return child;
    }

    public void addClassificationChild(String name, String _id)
    {
        this.coefficentForX += 1;
        CteClassificationNode child = new CteClassificationNode(this.getX() + coefficentForX*CteCoefficent.padX,
                this.getY() + CteCoefficent.padY, _id);
        setUpChild(child);
        child.setTitle(name);
        child.addToRoot();
        child.setIsChildOfClass(true);
        this.getAlignLine().turnOffMode();
    }

    public CteCompositionNode addCompositionChild()
    {
        this.coefficentForX += 1;
        String ID = this.addCompositionChildInCore();
        CteCompositionNode child = new CteCompositionNode(this.getX() + coefficentForX*CteCoefficent.padX,
                                                                                this.getY() + CteCoefficent.padY, ID);
        setUpChild(child);
        child.setIsChildOfClass(true);
        this.getAlignLine().turnOffMode();
        this.hideChoices();
        return child;
    }

    public void addCompositionChild(String name, String _id)
    {
        this.coefficentForX += 1;
        CteCompositionNode child = new CteCompositionNode(this.getX() + coefficentForX*CteCoefficent.padX,
                this.getY() + CteCoefficent.padY, _id);
        setUpChild(child);
        child.setTitle(name);
        child.setIsChildOfClass(true);
        this.getAlignLine().turnOffMode();
    }

    public CteUiNode addChild(CteNode node)
    {
        if(node instanceof CteComposition)
        {
            this.addCompositionChild();
        }
        else if(node instanceof CteClassification)
        {
            this.addClassificationChild();
        }

        if(!(node instanceof CteClass))
        {
            String name = node.getName();
            this.children.get(children.size()-1).setTitle(name);
            return this.children.get(children.size()-1);
        }
        else return null;
    }

    @Override
    protected void setUpMenu() {

        List<MenuItem> temp = initializeMenuItem();
        MenuItem Unparent = temp.get(1);
        MenuItem setParItem = temp.get(5);
        MenuItem Cut = temp.get(6);
        MenuItem Copy = temp.get(7);
        MenuItem Paste = temp.get(8);

        ImageView compositionIcon = new ImageView("icons/cte/compoIc.png");
        compositionIcon.setFitWidth(15);
        compositionIcon.setFitHeight(15);
        MenuItem CompoNode = new MenuItem("Create Composition Node", compositionIcon);
        CompoNode.setOnAction((t) ->{
            System.out.println("Create Composition Node");
            cteMain.makeCreateNodeCommand(this, addCompositionChild());
        });

        ImageView classificationIcon = new ImageView("icons/cte/classifiIc.png");
        classificationIcon.setFitWidth(15);
        classificationIcon.setFitHeight(15);
        MenuItem ClassifiNode = new MenuItem("Create Classification Node", classificationIcon);
        ClassifiNode.setOnAction((t) -> {
            System.out.println("Create Classification Node");
            cteMain.makeCreateNodeCommand(this, addClassificationChild());
        });
        temp.subList(5, 9).clear();

        temp.add(1, CompoNode);
        temp.add(2, ClassifiNode);


        ctMenu.getItems().addAll(temp);
        CteClassNode tmp = this;
        ctMenu.setOnShowing(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                ctMenu.getItems().clear();
                ctMenu.getItems().addAll(temp);


                if(tmp.isFreeNode() )
                {
                    ctMenu.getItems().removeAll(CompoNode, ClassifiNode);
                    if(cteMain.getCteViewManager().ValidSetParFunction(tmp) && tmp.getParentNode() == null)
                    {
                        ctMenu.getItems().add(setParItem);
                        ctMenu.getItems().removeAll(Unparent);
                    }

                }
                else{
                    if(cteMain.getCteViewManager().getWaitingForPaste().size() > 0
                                || cteMain.getCteViewManager().getLastCopyList().size() > 0) {
                        ctMenu.getItems().add(Paste);
                    }
                }

                if(tmp.isChoosing())
                {
                    ctMenu.getItems().add(Cut);
                    ctMenu.getItems().add(Copy);
                }

            }
        });
    }



    public void checkSetParentCondition(CteUiNode parentNode)
    {
        if(cteMain.getCteViewManager().getCteSetParentCommand() != null)
        {
            CteUiNode tmpChild = cteMain.getCteViewManager().getCteSetParentCommand().getChildNode();
            if(tmpChild instanceof CteClassNode)
            {
                UIController.showErrorDialog("The parent node is not suitable with child node !", "Cannot Set Parent", "Inappropriate parent node");
                this.cteMain.OffParentMode(tmpChild);
            }
            else if (this.childNameExisted(tmpChild.getTitle()))
            {
                UIController.showErrorDialog("There's a child node with the same content", "Cannot Set Parent", "Duplicated Node's name");
                this.cteMain.OffParentMode(tmpChild);
            }
            else
            {
                cteMain.getCteViewManager().getCteSetParentCommand().setTarget(this);
                cteMain.getCteViewManager().executeCommand( cteMain.getCteViewManager().getCteSetParentCommand());
            }
        }
    }


    @Override
    public void setAsParent(CteUiNode childNode, Integer index) {
                System.out.println("found CLassification or Composition");
                if (childNode.isFreeNode()) {
                    this.hideColForNode();
                    this.getAlignLine().turnOffMode();
                    this.alignLine.setFreeNode(false);
                    Edge newEdge = new Edge(this, childNode);
                    childNode.setEdges(newEdge);
                    childNode.setParent(this);
                    if(index != null){
                        this.children.add(index, childNode);
                    }
                    else this.children.add(childNode);
                    this.cteMain.getChildren().add(childNode.getEdges());
                    this.setChildForNodeInCore(childNode.getCoreId());
                    childNode.setFreeNode(false);
                    this.addChildName(childNode.getTitle());


                    if( childNode instanceof CteCompositionNode)
                    {
                        CteCompositionNode tmp = (CteCompositionNode)  childNode;
                        if(!tmp.isChildOfClass())
                        {
                            tmp.setIsChildOfClass(true);
                        }
                        SetUpCompositionChildToClass(tmp);

                    }
                    else {
                        CteClassificationNode tmp = (CteClassificationNode)  childNode;
                        tmp.setIsChildOfClass(true);
                        for(int i = 0; i < tmp.getChildren().size(); i++)
                        {
                            CteClassNode tmpChild = (CteClassNode) tmp.getChildren().get(i);
                            cteMain.getOTable().changeParentForColNChoices(tmpChild.getCoreId(), this.getCoreId());
                            if(tmpChild.getChildren().size() == 0)
                            {
                                tmpChild.getAlignLine().turnOnMode();
                                cteMain.getOTable().getColByID(tmpChild.getCoreId()).ShowChoices();
                            }
                            else
                            {
                                tmpChild.showLineNChoiceForAllLeaf();
                            }
                        }

                    }
                    this.cteMain.removeTree(childNode);
                    childNode.setUpTag();
                }
    }

    public void SetUpCompositionChildToClass(CteCompositionNode tmp)
    {
        if(tmp.getChildren().size() > 0)
        {
            for(int i = 0; i < tmp.getChildren().size(); i++)
            {
                if(tmp.getChildren().get(i) instanceof CteClassificationNode)
                {
                    CteClassificationNode tmpChild = (CteClassificationNode) tmp.getChildren().get(i);
                    if(tmpChild.getChildren().size() > 0)
                    {
                        for(CteUiNode tmpChildChild: tmpChild.getChildren())
                        {
                            cteMain.getOTable().changeParentForColNChoices(tmpChildChild.getCoreId(), this.getCoreId());
                            if(tmpChildChild.getChildren().size() == 0)
                            {
                                ((CteClassNode)tmpChildChild).getAlignLine().turnOnMode();
                                ((CteClassNode)tmpChildChild).getAlignLine().setFreeNode(false);
                                this.cteMain.getOTable().getColByID(tmpChildChild.getCoreId()).ShowChoices();
                            }
                        }
                    }
                    else {
                        this.cteMain.getOTable().changeParentForColNChoices(tmpChild.getCoreId(), this.getCoreId());
//                        tmpChild.getAlignLine().turnOnMode();        // classification khong con align Line nua
//                        tmpChild.getAlignLine().setFreeNode(false);
                        //this.cteMain.getOTable().getColByID(tmpChild.getCoreId()).ShowChoices();
                    }
                }
                else {
                    SetUpCompositionChildToClass((CteCompositionNode) tmp.getChildren().get(i));
                }
            }

        }
    }

    @Override
    public void setCteMain(CteView cte) {
        super.setCteMain(cte);
        if (alignLine == null) {
            CreateAlignLine();
            alignLine.endYProperty().bind(cteMain.heightProperty().subtract(3));
        }

    }
    @Override
    public void setFreeNode(boolean _freeNode) {
        super.setFreeNode(_freeNode);
        if(!_freeNode) {
            enableAlignLineForNode();
        }
    }

    public void enableAlignLineForNode()
    {
        if (this.alignLine != null)
        {
            this.alignLine.setFreeNode(false);
        }
            for(CteUiNode child : getChildren())
            {
                if (child instanceof CteClassNode) {
                    ((CteClassNode)child).enableAlignLineForNode();
                }
            }
    }

    @Override
    public void hideColForNode()
    {
        if(this.getChildren().size() == 0)
        {
            this.alignLine.setFreeNode(true);
            this.getCteMain().getOTable().hideColByID(this.getCoreId());
        }
    }

    public void CreateAlignLine()
    {
        alignLine = new CteAlignLine(this.getX() + this.getWidth()/2, this.getY() + this.getHeight());
        alignLine.startXProperty().bind(this.xProperty().add(this.getWidth()/2));
        alignLine.startYProperty().bind(this.yProperty().add(this.getHeight()));
        cteMain.getChildren().add(alignLine);
        cteMain.getAlignLineList().add(alignLine);
    }

    public void deleteAlignLine()
    {
        cteMain.getChildren().remove(alignLine);
        cteMain.getAlignLineList().remove(alignLine);
        alignLine = null;
    }

    public CteAlignLine getAlignLine() {
        return alignLine;
    }

    @Override
    public void deleteNode() {
        super.deleteNode();
        deleteAlignLine();
    }
    public void setRootNode()
    {
        this.setRootNode(this.getParentNode().getRootNode());
    }

    public String addClassificationChildInCore()
    {
        this.cteMain.getTreeManager().getTree().searchNodeById(this.getCoreId()).addClassificationChild();
        return  "c" + this.cteMain.getTreeManager().getTree().getNodeID();
    }

    public String addCompositionChildInCore()
    {
        this.cteMain.getTreeManager().getTree().searchNodeById(this.getCoreId()).addCompositionChild();
        return  "c" + this.cteMain.getTreeManager().getTree().getNodeID();
    }

    private  void hideChoices()
    {
        CteOTColumn tmp = this.cteMain.getOTable().getColByID(this.getCoreId());
        if(tmp != null) {
            tmp.HideChoices();
            tmp.unchosenChoices();
        }
    }

    public void createDecoratedDetail()
    {
        Rectangle decoRec = new Rectangle();
        decoratedDetail.add(decoRec);
        decoRec.setHeight(4);
        decoRec.widthProperty().bind(this.widthProperty());
        decoRec.yProperty().bind(this.yProperty().add(this.heightProperty()).subtract(decoRec.getHeight() + 1.5));
        decoRec.xProperty().bind(this.xProperty());
        decoRec.setFill(getDecorColor());


    }

    @Override
    public void changeDecoratedDetailColor(Color newColor) {
        for(int i = 0; i<decoratedDetail.size(); i++ )
        {
            decoratedDetail.get(i).setFill(newColor);
            setDecorColor(newColor);
        }
    }
    @Override
    public void showLineNChoiceForAllLeaf() {
        if (isLeaf()) {
            this.getAlignLine().setFreeNode(false);
            this.getAlignLine().turnOnMode();
            cteMain.getOTable().getColByID(this.getCoreId()).ShowChoices();
        } else {
            super.showLineNChoiceForAllLeaf();
        }
    }

}
