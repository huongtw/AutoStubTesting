package auto_testcase_generation.cte.UI.CteClassificationTree.CteComponent;

import auto_testcase_generation.cte.UI.CteCoefficent;
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
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.stage.WindowEvent;

import java.util.ArrayList;
import java.util.List;

public class CteCompositionNode extends CteUiNode{

//    protected List<CteClassificationNode> children ;
    private List<CteClassificationNode> classificationNodesList;
    private boolean childOfClass = false;
    private boolean isHeadNode = false;

    public CteCompositionNode(double _x, double _y)
    {
        super(_x, _y);
        this.setStrokeWidth(3.0);
        this.setStrokeLineJoin(StrokeLineJoin.ROUND);
        this.setStrokeLineCap(StrokeLineCap.ROUND);
        this.setStroke(Color.valueOf("#6b5323"));
        children = new ArrayList<>();
        classificationNodesList = new ArrayList<>();
        this.setRootNode();
        createDecoratedDetail();
    }

    public CteCompositionNode(double _x, double _y, String _id)
    {
        super(_x, _y, _id);
        this.setStrokeWidth(3.0);
        this.setStrokeLineJoin(StrokeLineJoin.ROUND);
        this.setStrokeLineCap(StrokeLineCap.ROUND);
        this.setStroke(Color.valueOf("#6b5323"));
        children = new ArrayList<>();
        classificationNodesList = new ArrayList<>();
        this.setRootNode();
        createDecoratedDetail();
        //this.DeleteAlignLine();
    }

    public CteClassificationNode addClassificationChild()
    {
        this.coefficentForX += 1;
        String ID = this.addClassificationChildInCore();
        CteClassificationNode child = new CteClassificationNode(this.getX() + coefficentForX* CteCoefficent.padX,
                this.getY() + CteCoefficent.padY, ID);
        setUpChild(child);

        this.cteMain.getOTable().addGroups(child.getCoreId());
        if(this.isChildOfClass())
        {
            CteUiNode parClassNode = this.getClassParent();
            if(parClassNode == null) System.out.println("Cannot Find Class Parent of this node");
            else
            {
                child.setIsChildOfClass(true);
                this.cteMain.getOTable().getColByID(parClassNode.getCoreId()).HideChoices();
            }
        }
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
        if(this.isChildOfClass())
        {
            child.setIsChildOfClass(true);
        }
    }

    public CteCompositionNode addCompositionChild()
    {
        this.coefficentForX += 1;
        String ID = addCompositionChildInCore();
        CteCompositionNode child = new CteCompositionNode(this.getX() + coefficentForX*CteCoefficent.padX,
                this.getY() + CteCoefficent.padY, ID);
        setUpChild(child);

        if(this.isChildOfClass())
        {
            child.setIsChildOfClass(true);
        }
        return child;
    }

    public void addCompositionChild(String name, String _id)
    {
        this.coefficentForX += 1;
        CteCompositionNode child = new CteCompositionNode(this.getX() + coefficentForX*
                CteCoefficent.padX, this.getY() + CteCoefficent.padY, _id);


        setUpChild(child);

        child.setTitle(name);

        if(this.isChildOfClass())
        {
            child.setIsChildOfClass(true);
        }
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
        MenuItem reNameItem = temp.get(0);
        MenuItem Unparent = temp.get(1);
        MenuItem Delete = temp.get(2);
        MenuItem setParItem = temp.get(5);
        MenuItem Cut = temp.get(6);
        MenuItem Copy = temp.get(7);
        MenuItem Paste = temp.get(8);

        ImageView compositionIcon = new ImageView("icons/cte/compoIc.png");
        compositionIcon.setFitWidth(15);
        compositionIcon.setFitHeight(15);
        MenuItem CompoNodeItem = new MenuItem("Create Composition Node", compositionIcon);
        CompoNodeItem.setOnAction((t) ->{
            System.out.println("Create Composition Node");
            cteMain.makeCreateNodeCommand(this, addCompositionChild());
        });

        ImageView classificationIcon = new ImageView("icons/cte/classifiIc.png");
        classificationIcon.setFitWidth(15);
        classificationIcon.setFitHeight(15);
        MenuItem ClassifiNodeItem = new MenuItem("Create Classification Node", classificationIcon);
        ClassifiNodeItem.setOnAction((t) -> {
            System.out.println("Create Classification Node");
            cteMain.makeCreateNodeCommand(this, addClassificationChild());
        });
        temp.subList(5, 9).clear();

        temp.add(1, CompoNodeItem);
        temp.add(2, ClassifiNodeItem);


        ctMenu.getItems().addAll(temp);
        CteCompositionNode tmp = this;
        ctMenu.setOnShowing(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                ctMenu.getItems().clear();
                ctMenu.getItems().addAll(temp);

                if(tmp.isHeadNode)
                {
                    ctMenu.getItems().removeAll(Delete, reNameItem, Unparent);
                    if(cteMain.getCteViewManager().getWaitingForPaste().size() > 0
                            || cteMain.getCteViewManager().getLastCopyList().size() >0 ) ctMenu.getItems().add(Paste);
                }
                else
                {
                    if(tmp.isFreeNode())
                    {
                        ctMenu.getItems().removeAll(CompoNodeItem, ClassifiNodeItem);
                        if(cteMain.getCteViewManager().ValidSetParFunction(tmp) && tmp.getParentNode() == null)
                        {
                            ctMenu.getItems().add(setParItem);
                            ctMenu.getItems().removeAll(Unparent);
                        }
                    }
                    else{
                        if(cteMain.getCteViewManager().getWaitingForPaste().size() > 0
                        || cteMain.getCteViewManager().getLastCopyList().size() >0 ) ctMenu.getItems().add(Paste);
                    }

                    if(tmp.isChoosing())
                    {
                        ctMenu.getItems().add(Cut);
                        ctMenu.getItems().add(Copy);
                    }
                }
            }
        });
    }

    public void setMenu()
    {

    }

    @Override
    public void setRootNode() {
        if (this.getParentNode() != null) {
            super.setRootNode();
        }
        else this.setRootNode(this);
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
                if (childNode.isFreeNode() && childNode!=this) {
                    Edge newEdge = new Edge(this, childNode);
                    childNode.setEdges(newEdge);
                    childNode.setParent(this);
                    childNode.setFreeNode(false);
                    childNode.setUpTag();
                    if(index != null){
                        this.children.add(index.intValue(), childNode);
                    }
                    else this.children.add(childNode);
                    this.cteMain.getChildren().add(childNode.getEdges());
                    this.setChildForNodeInCore(childNode.getCoreId());
                    this.addChildName(childNode.getTitle());
                    if(this.isChildOfClass())
                    {
                        CteClassNode classPar = (CteClassNode) this.getClassParent();
                        if(childNode instanceof CteClassificationNode)
                        {
                            CteClassificationNode tmpClassifi = (CteClassificationNode) childNode;
                            tmpClassifi.setIsChildOfClass(true);


                                for(int i = 0; i < tmpClassifi.getChildren().size(); i++)
                                {
                                    CteClassNode tmpChild = (CteClassNode) tmpClassifi.getChildren().get(i);
                                    cteMain.getOTable().changeParentForColNChoices(tmpChild.getCoreId(), classPar.getCoreId());
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
                        else
                        {
                            CteCompositionNode tmpCompo = (CteCompositionNode)  childNode;
                            if(!tmpCompo.isChildOfClass())
                            {
                                tmpCompo.setIsChildOfClass(true);
                            }

                            classPar.SetUpCompositionChildToClass(tmpCompo);
                        }
                    }
                    else
                    {
                        if(childNode instanceof CteClassificationNode)
                        {
                            CteClassificationNode tmpClassifi = (CteClassificationNode) childNode;
                            if(tmpClassifi.isChildOfClass())
                            {
                                tmpClassifi.setIsChildOfClass(false);

                                for(int i = 0; i < tmpClassifi.getChildren().size(); i++)
                                {
                                    CteClassNode tmpChild = (CteClassNode) tmpClassifi.getChildren().get(i);
                                    cteMain.getOTable().changeParentForColNChoices(tmpChild.getCoreId(),null);
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

                            else
                            {

                                for(int i = 0; i < tmpClassifi.getChildren().size(); i++)
                                {
                                    CteClassNode tmpChild = (CteClassNode) tmpClassifi.getChildren().get(i);
                                    if(tmpChild.getChildren().size() == 0)
                                    {
                                        tmpChild.getAlignLine().setFreeNode(false);
                                        tmpChild.getAlignLine().turnOnMode();
                                        cteMain.getOTable().getColByID(tmpChild.getCoreId()).ShowChoices();
                                    }
                                    else
                                    {
                                        tmpChild.showLineNChoiceForAllLeaf();
                                    }
                                }
                            }

                        }
                        else
                        {
                            CteCompositionNode tmpCompo = (CteCompositionNode) childNode;
                            if(tmpCompo.isChildOfClass())
                            {
                                tmpCompo.setIsChildOfClass(false);
                            }
                            tmpCompo.showLineNChoiceForAllLeaf();
                        }
                    }
                    this.cteMain.removeTree(childNode);
                }
    }

    public List<CteClassificationNode> getClassificationNodesList() {
        return classificationNodesList;
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

    public boolean isChildOfClass() {
        return childOfClass;
    }

    public void setIsChildOfClass(boolean childOfClass) {
        this.childOfClass = childOfClass;
        if(this.getChildren().size() > 0)
        {
            for(int i = 0; i < this.getChildren().size(); i++)
            {
                if(this.getChildren().get(i) instanceof CteCompositionNode)
                {
                    CteCompositionNode tmpCompoNode = (CteCompositionNode) this.getChildren().get(i);
                    tmpCompoNode.setIsChildOfClass(childOfClass);
                }
                else
                {
                    CteClassificationNode tmpClassifiNode = (CteClassificationNode) this.getChildren().get(i);
                    tmpClassifiNode.setIsChildOfClass(childOfClass);
                }
            }
        }
    }



    public boolean isHeadNode() {
        return isHeadNode;
    }


    public void setHeadNode(boolean headNode) {
        isHeadNode = headNode;
//        if(headNode == true)
//        {
//            setUpMenu();
//        }
    }

    @Override
    public void createDecoratedDetail() {
        Rectangle decoRec = new Rectangle();
        decoratedDetail.add(decoRec);

        decoRec.xProperty().bind(this.xProperty().add(this.strokeWidthProperty().divide(2)));
        decoRec.yProperty().bind(this.yProperty().add(this.strokeWidthProperty().divide(2)));
        decoRec.widthProperty().bind(this.widthProperty().subtract(this.strokeWidthProperty()));
        decoRec.heightProperty().bind(this.heightProperty().subtract(this.strokeWidthProperty()));

        decoRec.setStrokeWidth(2.0);
        decoRec.setStroke(getDecorColor());
        decoRec.setFill(Color.TRANSPARENT);
    }

    public void changeDecoratedDetailColor(Color newColor) {
        for(int i = 0; i<decoratedDetail.size(); i++ )
        {
            decoratedDetail.get(i).setStroke(newColor);
            setDecorColor(newColor);
        }
    }
}
