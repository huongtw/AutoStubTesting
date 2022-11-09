package auto_testcase_generation.cte.UI.CteClassificationTree.CteComponent;

import auto_testcase_generation.cte.UI.CteCoefficent;
import auto_testcase_generation.cte.core.cteNode.CteClass;
import auto_testcase_generation.cte.core.cteNode.CteNode;
import com.dse.guifx_v3.helps.UIController;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.WindowEvent;

import java.util.List;

public class CteClassificationNode extends CteUiNode {
    private boolean childOfClass = false;

    public CteClassificationNode (double _x, double _y)
    {
        super(_x, _y);
        createDecoratedDetail();
    }

    public CteClassificationNode (double _x, double _y, String _id)
    {
        super(_x, _y, _id);
        createDecoratedDetail();
    }

    public CteUiNode addChild() {
        this.coefficentForX += 1;
        String ID = this.addChildInCore();
        CteClassNode child = new CteClassNode(this.getX() + coefficentForX* CteCoefficent.padX,
                                                                        this.getY() + CteCoefficent.padY, ID);
        setUpChild(child);

        child.setValueForTestcase(this.title);
        if(this.getChildren().size() > 1)
        {
            this.cteMain.getOTable().addColOfChoices(child, this.getCoreId(), this.getClassParent());
        }
        else {
            this.cteMain.getOTable().addColOfChoices(child, this.getCoreId(), this.getClassParent());
            }
        return child;
    }

    public void addChild(String name, String _id)
    {
        this.coefficentForX += 1;
        CteClassNode child = new CteClassNode(this.getX() + coefficentForX*CteCoefficent.padX,
                this.getY() + CteCoefficent.padY, _id);
        setUpChild(child);
        child.setTitle(name);

//        if(cteMain.isCreateNew()) {
            child.setValueForTestcase(this.title);
//        }

    }

    public CteUiNode addChild(CteNode node)
    {
        if(node instanceof CteClass)
        {
            this.addChild();
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

        ImageView classIcon = new ImageView("icons/cte/classIc.png");
        classIcon.setFitWidth(15);
        classIcon.setFitHeight(15);
        MenuItem ClassNode = new MenuItem("Create Class Node", classIcon);
        ClassNode.setOnAction((t) -> {
            System.out.println("Create Class Node");
            cteMain.makeCreateNodeCommand(this, addChild());
        });
        temp.subList(5, 9).clear();

        temp.add(1, ClassNode);

        ctMenu.getItems().addAll(temp);
        CteClassificationNode tmp = this;
        ctMenu.setOnShowing(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                ctMenu.getItems().clear();
                ctMenu.getItems().addAll(temp);
                if(tmp.isFreeNode())
                {
                    ctMenu.getItems().removeAll(ClassNode);
                    if (cteMain.getCteViewManager().ValidSetParFunction(tmp) && tmp.getParentNode() == null) {
                        ctMenu.getItems().add(setParItem);
                        ctMenu.getItems().removeAll(Unparent);
                    }
                }
                else{
                    if(cteMain.getCteViewManager().getWaitingForPaste().size() > 0
                            || cteMain.getCteViewManager().getLastCopyList().size() >0) {
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
            if(!(tmpChild instanceof CteClassNode))
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
                System.out.println("found CLass with ID" + childNode.getCoreId()) ;
                if (childNode.isFreeNode()) {
                    Edge newEdge = new Edge(this, childNode);
                    CteClassNode tmp = (CteClassNode)  childNode;
                    tmp.setEdges(newEdge);
                    tmp.setParent(this);
                    if(index != null){
                        this.children.add(index, tmp);
                    }
                    else this.children.add(tmp);
                    this.cteMain.getChildren().add(tmp.getEdges());
                    tmp.getAlignLine().setFreeNode(false);
                    tmp.setFreeNode(false);
//                                this.alignLine.turnOffMode();
                    tmp.setUpTag();
                    tmp.setValueForTestcase(this.title);
                    this.setChildForNodeInCore(tmp.getCoreId());
                    this.addChildName(tmp.getTitle());
                    this.cteMain.getOTable().changeGroupForCol(tmp.getCoreId(), this.getCoreId());
//                                if(this.getChildren().size() == 1)
//                                {
//                                    cteMain.getOTable().deleteColByID(tmp.getCoreId());
//                                    this.cteMain.getOTable().changeColID(this.getCoreId(), tmp);
//                                }
//                                else if(this.getChildren().size() > 1)
//                                {
                    if(this.isChildOfClass())
                    {
                        String parID = this.getClassParent().getCoreId();
                        cteMain.getOTable().changeParentForColNChoices(tmp.getCoreId(), parID);
                    }
//                                }
                    else
                    {
                        cteMain.getOTable().changeParentForColNChoices(tmp.getCoreId(), null);
                    }

                    if(tmp.getChildren().size() == 0)
                    {
                        tmp.getAlignLine().turnOnMode();
                        cteMain.getOTable().getColByID(tmp.getCoreId()).ShowChoices();
                    }
                    else
                    {
                        cteMain.getOTable().getColByID(tmp.getCoreId()).HideChoices();
                        cteMain.getOTable().getColByID(tmp.getCoreId()).unchosenChoices();
                        tmp.showLineNChoiceForAllLeaf();
                    }
                    this.cteMain.removeTree(childNode);
                }
                else{
                    this.cteMain.OffParentMode(childNode);
                }
    }

    public void addToRoot()
    {
        this.getRootNode().getClassificationNodesList().add(this);
    }

    @Override
    public boolean isChildOfClass() {
        return childOfClass;
    }

    public void setIsChildOfClass(boolean childOfClass) {
        this.childOfClass = childOfClass;
    }

    public String addChildInCore()
    {
        this.cteMain.getTreeManager().getTree().searchNodeById(this.getCoreId()).addClassChild();
        return  "c" + this.cteMain.getTreeManager().getTree().getNodeID();
    }

    @Override
    public void createDecoratedDetail() {
        Rectangle decoRec1 = new Rectangle();
        Rectangle decoRec2 = new Rectangle();
        decoratedDetail.add(decoRec1);
        decoratedDetail.add(decoRec2);

        decoRec1.setWidth(4);
        decoRec2.setWidth(4);

        decoRec1.heightProperty().bind(this.heightProperty());
        decoRec2.heightProperty().bind(this.heightProperty());

        decoRec1.yProperty().bind(this.yProperty());
        decoRec2.yProperty().bind(this.yProperty());

        decoRec1.xProperty().bind(this.xProperty().add(1.5));
        decoRec2.xProperty().bind(this.xProperty().add(this.widthProperty()).subtract(decoRec2.getWidth() + 1.5));

        decoRec1.setFill(getDecorColor());
        decoRec2.setFill(getDecorColor());
    }

    @Override
    public void changeDecoratedDetailColor(Color newColor) {
        for(int i = 0; i<decoratedDetail.size(); i++ )
        {
            decoratedDetail.get(i).setFill(newColor);
            setDecorColor(newColor);
        }
    }
}
