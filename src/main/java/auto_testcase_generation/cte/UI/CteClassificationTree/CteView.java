package auto_testcase_generation.cte.UI.CteClassificationTree;

import auto_testcase_generation.cte.UI.Controller.CteController;
import auto_testcase_generation.cte.UI.CteClassificationTree.CteCommand.*;
import auto_testcase_generation.cte.UI.CteClassificationTree.CteComponent.*;
import auto_testcase_generation.cte.UI.CteCoefficent;
import auto_testcase_generation.cte.UI.DataInsert.CteDataInsert;
import auto_testcase_generation.cte.UI.OptionTable.CteOptionTable;
import auto_testcase_generation.cte.booleanChangeListen.ListenableBoolean;
import auto_testcase_generation.cte.core.ClassificationTreeManager;
import auto_testcase_generation.cte.core.cteNode.CteClass;
import auto_testcase_generation.cte.core.cteNode.CteClassification;
import auto_testcase_generation.cte.core.cteNode.CteComposition;
import auto_testcase_generation.cte.core.cteNode.CteNode;
import com.dse.testcase_manager.TestCase;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;

public  class CteView extends Pane {

    private ListenableBoolean boolListener;
    private CteCompositionNode headNode;
    private ContextMenu ctMenu;
    private CteViewManager cteViewManager;
    private CteController cteController;
    private double currentY = 10;
    private ClassificationTreeManager treeManager;
    private List<CteAlignLine> alignLineList = new ArrayList<>();
    private CteOptionTable oTable;
    private boolean createNew;
    public int mode = 0;
    public CteUiNode renamingNode = null;
    private double mouseDownX ;  // for multiselect
    private double mouseDownY ;
    private Rectangle selectionRectangle;
    private boolean nodeSelecting = false;

    private double scaleValue = 1;
    private final double zoomIntensity = 0.02;

    public CteView(ClassificationTreeManager _tManager)
    {
        super();
        treeManager = _tManager;
        CteComposition node = treeManager.getTree().getRoot();
        createNew = treeManager.isCreateNew();
        cteViewManager = new CteViewManager();
        cteViewManager.setCteView(this);
        ctMenu = new ContextMenu();

        headNode = new CteCompositionNode(100, 50, _tManager.getTree().getRoot().getId());
        headNode.setTitle(node.getName());
        headNode.setCteMain(this);
        headNode.setUpTag();
        headNode.setHeadNode(true);
        this.AddNode(headNode);
        this.AddChildOfNode(node, headNode);
        this.cteViewManager.getFreeTree().add(headNode);
        this.reArrange();
//        setUpScrollBar();
        menuProcess();
        setUpRenameHandler();
        setUpKeyBoard();
        //this.addEventHandler(new MouseEvent());

        selectionRectangle = new Rectangle();
        selectionRectangle.setStroke(Color.BLACK);
        selectionRectangle.setFill(Color.TRANSPARENT);
        selectionRectangle.getStrokeDashArray().addAll(5.0, 5.0);
        setUpMultiSelect();
    }

    /**
     * Keyboard Interaction
     */
    public void setUpKeyBoard()
    {
        this.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode() == KeyCode.DELETE)
                {
                    makeDeleteCommand();
                }
                else if(event.getCode() == KeyCode.X)
                {
                    if(event.isControlDown()) makeCutCommand();
                }
                else if(event.getCode() == KeyCode.C)
                {
                    if(event.isControlDown()) makeCopyCommand();
                }
                else if(event.getCode() == KeyCode.V)
                {
                    if(event.isControlDown())
                    {
                        if(cteViewManager.getChosenNodes().size() == 1) makePasteCommand(cteViewManager.getChosenNodes().get(0));
                    }
                }
                else if(event.getCode() == KeyCode.Z)
                {
                    if(event.isControlDown() && !event.isShiftDown())
                    {
                        cteViewManager.undoCommand();
                    }
                }
                else if(event.getCode() == KeyCode.Y)
                {
                    if(event.isControlDown() && !event.isShiftDown())
                    {
                        cteViewManager.redoCommand();
                    }
                }
                else if (event.getCode() == KeyCode.M)
                {
                    if(event.isControlDown())
                    {
                        cteController.resetCTE();
                    }
                }
            }
        });
    }

    /**
     * Multi Select
     *
     */

    public void setUpMultiSelect()
    {
        this.getChildren().add(selectionRectangle);

        this.setOnMousePressed(e -> {
            if(nodeSelecting == false) {
                mouseDownX = e.getX();
                mouseDownY = e.getY();

                selectionRectangle.setX(mouseDownX);
                selectionRectangle.setY(mouseDownY);
                selectionRectangle.setWidth(0);
                selectionRectangle.setHeight(0);
            }
        });

        this.setOnMouseDragged(e -> {
            if(nodeSelecting == false) {
                double mouseX = Math.max(e.getX(), 0);
                double mouseY = Math.max(e.getY(), 0);
                selectionRectangle.setX(Math.min(mouseX, mouseDownX));
                selectionRectangle.setWidth(Math.abs(mouseX - mouseDownX));
                selectionRectangle.setY(Math.min(mouseY, mouseDownY));
                selectionRectangle.setHeight(Math.abs(mouseY - mouseDownY));
            }
        });

        this.setOnMouseReleased(e -> {
            if(nodeSelecting)
            {
                nodeSelecting = false;
            }
            else {
                double width = selectionRectangle.getWidth();
                double height = selectionRectangle.getHeight();
                selectionRectangle.setWidth(0);
                selectionRectangle.setHeight(0);

                if (!e.isControlDown()) {
                    cteViewManager.removeAllChosenNodes();
                }
                selectMultiNodeInRectangle(selectionRectangle.getX(), selectionRectangle.getY(), width, height);
            }

        });
    }

    private void selectMultiNodeInRectangle(double X, double Y, double W, double H)
    {
        double endX = X + W;
        double endY = Y + H;
        checkNodeInRange(headNode, X-5, Y-5, endX+5, endY+5);
        for(int i = 1; i < cteViewManager.getFreeTree().size(); i++) // xet tu 1 vi free  tree dau tien la cai cay goc
        {

            checkNodeInRange(cteViewManager.getFreeTree().get(i), X-5, Y-5, endX+5, endY+5 );
        }

    }

    private void checkNodeInRange(CteUiNode node, double X, double Y, double endX, double endY)
    {
        if(node.getX() >= X && node.getX()+node.getWidth() <= endX && node.getY() >= Y && node.getY() + node.getHeight() <= endY)
        {
            if(node != headNode) cteViewManager.addChosenNode(node, true, false);
        }

        for(int i = 0; i < node.getChildren().size(); i++)
        {
            checkNodeInRange((node.getChildren().get(i)), X, Y, endX, endY);
        }

    }

    /**
     * Scroll to Zoom
     *
     */
    public void setUpScroll() {

        ScrollPane pane = cteController.getCteWindowController().getCTPane();
        VBox vBox = (VBox) pane.getContent();
        Node zoomNode = vBox.getChildren().get(0);

        vBox.setOnScroll(event -> {
            if (event.isControlDown()) {
                event.consume();

                double maxZoomRate = 2.0;
                double deltaY = event.getDeltaY();
                if (!(scaleValue > maxZoomRate && deltaY > 0) && !(scaleValue < minZoomRate() && deltaY < 0)) {
                    double zoomFactor = Math.exp(event.getTextDeltaY() * zoomIntensity);

                    Bounds innerBounds = zoomNode.getLayoutBounds();
                    Bounds viewportBounds = pane.getViewportBounds();

                    // calculate pixel offsets from [0, 1] range
                    double valX = pane.getHvalue() * (innerBounds.getWidth() - viewportBounds.getWidth());
                    double valY = pane.getVvalue() * (innerBounds.getHeight() - viewportBounds.getHeight());

                    scaleValue = scaleValue * zoomFactor;
                    updateScale();
                    pane.layout(); // refresh ScrollPane scroll positions & target bounds

                    // convert target coordinates to zoomTarget coordinates
                    Point2D posInZoomTarget = parentToLocal(zoomNode.parentToLocal(new Point2D(event.getX(), event.getY())));

                    // calculate adjustment of scroll position (pixels)
                    Point2D adjustment = getLocalToParentTransform().deltaTransform(posInZoomTarget.multiply(zoomFactor - 1));

                    // convert back to [0, 1] range
                    // (too large/small values are automatically corrected by ScrollPane)
                    Bounds updatedInnerBounds = zoomNode.getBoundsInLocal();
                    pane.setHvalue((valX + adjustment.getX()) / (updatedInnerBounds.getWidth() - viewportBounds.getWidth()));
                    pane.setVvalue((valY + adjustment.getY()) / (updatedInnerBounds.getHeight() - viewportBounds.getHeight()));

                    double paneHeight = getCteController().getCteWindowController().getCTPane().getHeight();
                    if (scaleValue < 1) {
                        setPrefHeight(paneHeight/scaleValue);
                    }
                }

            }
        });



    }

    private double minZoomRate() {
        double minZoomRate = 0.5;
        double paneWidth = getCteController().getCteWindowController().getCTPane().getWidth();
        return Math.min(paneWidth / getWidth(), minZoomRate);
    }

    private void updateScale() {
        setScaleX(scaleValue);
        setScaleY(scaleValue);
        oTable.setScaleX(scaleValue);
    }

    public void setNodeSelecting(boolean nodeSelecting) {
        this.nodeSelecting = nodeSelecting;
    }

    //    public  ListenableBoolean getNewNodeAvailable() {
//        return newNodeAvailable;
//    }


    private void AddNode(CteUiNode newCteNode)
    {
        Task<Void> addNodeTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Platform.runLater(() -> CteView.this.getChildren().addAll(newCteNode, newCteNode.getLabel(),
                                                newCteNode.getTfield()));
                return null;
            }
        };

        new Thread(addNodeTask).start();


    }

    private void AddNode(CteUiNode newCteNode, Edge newEdge)
    {
        Task<Void> addNodeTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Platform.runLater(() -> CteView.this.getChildren().addAll(newCteNode, newCteNode.getLabel(),
                        newCteNode.getTfield(), newEdge));
                return null;
            }
        };

        new Thread(addNodeTask).start();

    }

    private void AddChildOfNode(CteNode node, CteUiNode par)
    {
        if (node.getChildren().size() != 0)
        {
            for (CteNode coreChild : node.getChildren()) {
                if (coreChild instanceof CteComposition) {
                    addCompositionNode((CteComposition) coreChild, par);
                }
                else if (coreChild instanceof CteClassification) {
                    addClassificationNode((CteClassification) coreChild, par);
                }
                else {
                    ((CteClassificationNode) par).addChild(coreChild.getName(), coreChild.getId());
                }
            }

            for(int i = 0; i < par.getChildren().size(); i++)
            {
                AddChildOfNode(node.getChildren().get(i), par.getChildren().get(i));
            }
        }
    }

    private void addCompositionNode(CteComposition node, CteUiNode par) {
        if (par instanceof CteCompositionNode)
            ((CteCompositionNode) par).addCompositionChild(node.getName(), node.getId());
        else if (par instanceof CteClassNode)
            ((CteClassNode) par).addCompositionChild(node.getName(), node.getId());
    }

    private void addClassificationNode(CteClassification node, CteUiNode par) {
        if (par instanceof CteCompositionNode)
            ((CteCompositionNode) par).addClassificationChild(node.getName(), node.getId());
        else if (par instanceof CteClassNode)
            ((CteClassNode) par).addClassificationChild(node.getName(), node.getId());
    }

    public void menuProcess()
    {
        this.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
            @Override
            public void handle(ContextMenuEvent event) {
                ctMenu.show((Node) event.getSource(), event.getScreenX(), event.getScreenY());
            }
        });
    }


    /**
     * Arrange Function
     */

    public void reArrange()
    {
       // headNode.setChildWidth(10);
        currentY = 10;
        double stX = 10;

        for(int i = 0; i < cteViewManager.getFreeTree().size(); i++) {
            if(i > 0 && currentY > 2000)
            {
                stX += cteViewManager.getFreeTree().get(i).getChildWidth();
                currentY = 10;
            }
            arrangeChildNode(cteViewManager.getFreeTree().get(i));
            cteViewManager.getFreeTree().get(i).setChildWidth(10);
            arrangeNode(cteViewManager.getFreeTree().get(i), stX, currentY + 50);

        }

    }

    private void arrangeChildNode(CteUiNode node)
    {
        node.sortChildByPos();
        for(int i = 0; i < node.getChildren().size(); i++)
        {
            arrangeChildNode(node.getChildren().get(i));
        }
    }

    private void arrangeNode(CteUiNode node, double startX, double startY)
    {
        double y = startY;
        if(node.getChildren().size() == 0)
        {
                node.setXPos(node.getTopNode().getChildWidth() + CteCoefficent.nodeSpace + startX);
                if(node.getParentNode()!= null) {
                    if(node.getParentNode().getWidth() > node.getWidth())
                    {
                        double newPos = node.getX() + (node.getParentNode().getWidth() - node.getWidth())/2;
                        node.setXPos(newPos);
                    }
                    node.getParentNode().setChildWidth(node.getX() + node.getWidth());
                }
                node.setYPos(y);
                if(y > currentY)  currentY = y + node.getHeight();

        }
        else
        {
            double padY = node.getHeight() + 30;
            y+= padY;
            for(int i = 0; i < node.children.size(); i++)
            {
                arrangeNode(node.children.get(i), startX, y);
                if (i == 0){
                    node.setStartChild(node.children.get(i).getX());
                }
            }
            y-=padY;
//            if(node.getY() != node.getInitialPosY())
//            {
//               // node.setYPos((int) node.getInitialPosY());
//            }
            node.setYPos(y);
            double endChild = node.getChildren().get(node.getChildren().size() - 1).getX() +
                                                    node.getChildren().get(node.getChildren().size() - 1).getWidth();
            if(node.getParentNode() != null)
            {

                double newX = node.getStartChild() + (endChild - node.getStartChild())/2 - node.getWidth()/2;
                node.setXPos(newX);
                if(node.getX() + node.getWidth() > headNode.getChildWidth())
                {
                     node.getParentNode().setChildWidth(newX + node.getWidth());
                }

            }
            else
            {
                double newX = node.getStartChild() + (endChild - node.getStartChild())/2 - node.getWidth()/2;
                node.setXPos(newX);
            }
        }

//        updateScrollBar();
    }

    public CteCompositionNode getHeadNode() {
        return headNode;
    }

    public void clearNodeFromScreen(CteUiNode node)
    {
        if(node.getEdges() != null) {
            this.getChildren().remove(node.getEdges());
        }
        this.getChildren().removeAll(node, node.getTfield(), node.getLabel());
        this.getChildren().removeAll(node.getDecoratedDetail());

        if(cteViewManager.getFreeTree().contains(node))
        {
            cteViewManager.getFreeTree().remove(node);
        }
    }

    public void addNodeToScreen(CteUiNode node)
    {
        if(!this.getChildren().contains(node))
        {
            this.getChildren().add(node);
            this.getChildren().add(node.getTfield());
            this.getChildren().add(node.getLabel());
            this.getChildren().addAll(node.getDecoratedDetail());
            if(node.getEdges()!= null && !this.getChildren().contains(node.getEdges()))  this.getChildren().add(node.getEdges());

        }

        if(node.isFreeNode() && node.getParentNode() == null)
        {
            cteViewManager.getFreeTree().add(node);
        }
    }

    public void clearEdgeOfNode(CteUiNode node)
    {
        this.getChildren().remove(node.getEdges());
    }

    public void addEdgeOfNode(CteUiNode node)
    {
        if(node.getEdges() != null)
        {
            this.getChildren().add(node.getEdges());
        }
        else System.out.println("the Edge of " + node.getTitle() +  " is Null");
    }





    public CteViewManager getCteViewManager() {
        return cteViewManager;
    }

    public void OffParentMode(CteUiNode childNode)
    {
        childNode.setBgColorForNodes(CteCoefficent.NodeBgColor.normalCor);
        cteViewManager.setCteSetParentCommand(null);

    }

    public void OnParentMode(CteSetParentCommand setParentCommand){
        cteViewManager.setCteSetParentCommand(setParentCommand);
    }


    public void removeNodeFrommTreeList(CteUiNode node)
    {
        if(cteViewManager.getFreeTree().contains(node)) cteViewManager.getFreeTree().remove(node);
        else System.out.println("Cannot find node in tree list.");
    }

    public void removeTree(CteUiNode removeNode)
    {
        cteViewManager.removeTreeAfterSetParent(removeNode);
        this.OffParentMode(removeNode);
    }

    public ClassificationTreeManager getTreeManager() {
        return treeManager;
    }

    public void setTreeManager(ClassificationTreeManager treeManager) {
        this.treeManager = treeManager;
    }

    public List<CteAlignLine> getAlignLineList() {
        return alignLineList;
    }

    public void HideAlignLines()
    {
        for (CteAlignLine cteAlignLine : alignLineList) {
            cteAlignLine.Hide();
        }
    }

    public void ShowAlignLines(){
        for (CteAlignLine cteAlignLine : alignLineList) {
            cteAlignLine.Show();

        }
    }

    public CteOptionTable getOTable() {
        return oTable;
    }

    public void setOTable(CteOptionTable oTable) {
        this.oTable = oTable;
    }

    public CteDataInsert getDInsert() {
        return cteViewManager.getDataInsert();
    }

    public TestCase getTestCaseByID(String ID)
    {

        return DFSForTestCase(headNode, ID);
    }

    private TestCase DFSForTestCase(CteUiNode node, String ID)
    {
        if (node.getChildren().size()!=0)
        {
            for(int i = 0; i<node.getChildren().size(); i++)
            {
                CteUiNode a = node.getChildren().get(i);
                if(a.getCoreId().equals(ID))
                {
                    return a.getTestCase();
                }
                else
                {
                    TestCase tmp = DFSForTestCase(a, ID);
                    if(tmp == null) continue;
                    else return tmp;
                }
            }
        }
        return null;
    }

    public boolean isCreateNew() {
        return createNew;
    }

    public void setCreateNew(boolean createNew) {
        this.createNew = createNew;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int value) {
        this.mode = value;
    }

    public void setRenamingNode(CteUiNode _renamingNode) {
        this.renamingNode = _renamingNode;
    }

    private void setUpRenameHandler()
    {
        EventHandler<MouseEvent> renameHandler = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(mode == CteCoefficent.CteViewMode.RENAMING)
                {
                    if(renamingNode != null)
                    {
                        if(event.getX() < renamingNode.getX() || event.getX() > renamingNode.getX() + renamingNode.getWidth()
                                || event.getY() < renamingNode.getY() || event.getY() > renamingNode.getY()+renamingNode.getHeight() )
                        {
                            renamingNode.changeName();
                            renamingNode = null;
                        }
                    }
                }
                else if(mode == CteCoefficent.CteViewMode.ENTERING_RENAME)
                {
                    mode = CteCoefficent.CteViewMode.RENAMING;
                }
                else
                {

                }
            }
        };

        this.addEventFilter(MouseEvent.MOUSE_CLICKED, renameHandler);

    }

    /**
     * Command on Node
     */
    public void makeChangeNameCommand(CteUiNode node, String newName)
    {
        CteChangeNameCommand command = new CteChangeNameCommand(node, newName);
        cteViewManager.executeCommand(command);
    }

    public void makeCutCommand()
    {
        CteCutCommand command = new CteCutCommand(cteViewManager.getChosenNodes(), cteViewManager);
        cteViewManager.executeCommand(command);
    }

    public void makePasteCommand(CteUiNode node)
    {
        CtePasteCommand command = new CtePasteCommand(node, cteViewManager);
        cteViewManager.executeCommand(command);
    }

    public void makeCopyCommand()
    {
        CteCopyCommand command = new CteCopyCommand(cteViewManager.getChosenNodes(),cteViewManager );
        cteViewManager.executeCommand(command);
    }

    public void makeUnparentCommand(CteUiNode node)
    {
        CteUnparentCommand command = new CteUnparentCommand(node);
        cteViewManager.executeCommand(command);
    }

    public void makeDeleteCommand()
    {
        CteDeleteCommand command = new CteDeleteCommand(cteViewManager.getChosenNodes(), headNode);
        cteViewManager.executeCommand(command);
    }

    public void makeSetParentCommand(CteUiNode childNode)
    {
        CteSetParentCommand command = new CteSetParentCommand(childNode, cteViewManager);
        this.OnParentMode(command);
//        cteViewManager.executeCommand(command);
    }

    public void makeCreateNodeCommand(CteUiNode parent, CteUiNode child) {
        CteCreateNodeCommand command = new CteCreateNodeCommand(parent, child);
        cteViewManager.executeCommand(command);
    }

    public boolean hasFreeNode()
    {
        if(cteViewManager.getFreeTree().size() > 1) return true;
        return false;
    }

    public CteController getCteController() {
        return cteController;
    }

    public void setCteController(CteController cteController) {
        this.cteController = cteController;
    }
}