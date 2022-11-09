package auto_testcase_generation.cte.UI.CteClassificationTree.CteComponent;

import javafx.beans.property.DoubleProperty;
import javafx.scene.Cursor;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.List;

public class DraggableNode extends Rectangle {

    protected String title;
    protected Text label;
    protected TextField tfield;
    protected HBox vb;
    double orgSceneX;
    double orgSceneY;
    protected DoubleProperty midDown;
    protected double paddingX = 7.5, paddingY = 7;
    protected double alphaY = 10;
    protected boolean onClick = false;
    protected List<? extends DraggableNode> draggableNodes;
    protected List<Rectangle> decoratedDetail = new ArrayList<>();
    private Color decorColor = Color.TRANSPARENT;
    protected Color bgColor = Color.TRANSPARENT;


    public DraggableNode(double x, double y) {
        this.setX(x);
        this.setY(y);
        this.setFill(bgColor);
        this.setStroke(Color.BLACK);
        title = "Blank";
        label = new Text(title);
        label.setX(x + paddingX);
        label.setY(y + paddingY + alphaY);
        label.setTextAlignment(TextAlignment.LEFT);
        tfield = new TextField();
        tfield.setVisible(false);

        this.setWidth(label.getLayoutBounds().getWidth() + 2 * paddingX); //length()*alphaX
        this.setHeight(label.getLayoutBounds().getHeight() + 1 * paddingY);

        List<DraggableNode> temp = new ArrayList<>();
        temp.add(this);
        draggableNodes = temp;
        ProcesssingNode();

    }

    public void setUpTextField() {
        tfield.setLayoutX(this.getX());
        tfield.setLayoutY(this.getY());
        tfield.setPrefWidth(this.getWidth());
        tfield.setPrefHeight(this.getHeight());
        tfield.setText(label.getText());

    }

    protected void ProcesssingNode() {
        setUpTextField();
        this.setCursor(Cursor.DEFAULT);
        this.setOnMousePressed((t) -> {
            orgSceneX = t.getX();
            orgSceneY = t.getY();

            DraggableNode node = (DraggableNode) (t.getSource());
            node.toFront();
            turnOnNodeSelect();
        });


        this.setOnMouseDragged((t) -> {
            double offsetX = t.getX() - orgSceneX + Math.min(t.getX(), 0);
            double offsetY = t.getY() - orgSceneY + Math.min(t.getY(), 0);

            DraggableNode thisNode = (DraggableNode) (t.getSource());
            if (t.isControlDown()) {
                for (DraggableNode chosenNode : draggableNodes) {
                    dragNode(chosenNode, offsetX, offsetY);
                }

                if (!draggableNodes.contains(thisNode)) {
                    dragNode(thisNode, offsetX, offsetY);
                }
            } else {
                dragNode(thisNode, offsetX, offsetY);
            }


            orgSceneX = t.getX();
            orgSceneY = t.getY();

        });

//        this.setOnMouseReleased();


        this.setOnMouseExited((t) -> label.toFront());

        label.setOnMouseEntered((t) -> this.toFront());


        FixNameEnable();


    }

    private void dragNode(DraggableNode chooseNode, double offsetX, double offsetY) {
        chooseNode.setPosX(Math.max(chooseNode.getX() + offsetX, 0));
        chooseNode.setPosY(Math.max(chooseNode.getY() + offsetY, 0));
    }

    protected void turnOnNodeSelect() {

    }

    protected void FixNameEnable() {

        tfield.setOnKeyPressed(k -> {
            if (k.getCode().equals(KeyCode.ENTER)) {
                changeName();
            }
        });
    }

    public void changeName() {
        setTitle(tfield.getText());
        tfield.setVisible(false);
        label.setVisible(true);
        label.toFront();
    }

    public String getNewName() {
        String result = tfield.getText();
        tfield.setVisible(false);
        label.setVisible(true);
        label.toFront();
        return result;
    }


    public void setTitle(String _title) {
        title = _title;
        label.setText(title);
        resize();
    }

    public String getTitle() {
        return title;
    }

    public void resize() {
        this.setWidth(label.getLayoutBounds().getWidth() + 2 * paddingX);
        this.setHeight(label.getLayoutBounds().getHeight() + 1 * paddingY);
    }


    public Text getLabel() {
        return label;
    }

    public TextField getTfield() {
        return tfield;
    }

    public void setPosX(double newX) {
        this.setX(newX);
        this.label.setX(this.getX() + paddingX);
        this.setUpTextField();

    }

    public void setPosY(double newY) {
        this.setY(newY);
        this.label.setY(this.getY() + paddingY + alphaY);
        this.setUpTextField();
        // this.ChangeALignLinePos();
    }

    public void createDecoratedDetail() {}

    public void changeDecoratedDetailColor(Color newColor){}

    public List<Rectangle> getDecoratedDetail() {
        return decoratedDetail;
    }

    public Color getDecorColor()
    {
        return decorColor;
    }

    public void setDecorColor(Color currentColor) {
        this.decorColor = currentColor;
    }

    public Color getBgColor() {
        return bgColor;
    }

    public void setBgColor(Color bgColor) {
        this.bgColor = bgColor;
        applyBgColor();
    }

    public void applyBgColor()
    {
        this.setFill(bgColor);
        toFronLabel();
    }

    public void toFronLabel()
    {
        label.toFront();
    }
}