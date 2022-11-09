package auto_testcase_generation.cte.UI.CteClassificationTree.CteComponent;

import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

public class CteAlignLine extends Line {

    private boolean freeNode = false;
    private boolean offMode = false;

    public CteAlignLine(double posX, double sPosY)
    {
        super();
        this.setStartX(posX);
        this.setStartY(sPosY);
        this.setEndX(posX);
        this.endXProperty().bind(this.startXProperty());
        this.setStrokeWidth(0.7);
        this.setStroke(Color.TRANSPARENT);
    }

    public void Hide()
    {
        this.setStroke(Color.TRANSPARENT);
    }

    public void Show()
    {
        if(!freeNode && !offMode) {
            this.setStroke(Color.GRAY);
        }
    }

    public void turnOffMode()
    {
        offMode = true;
        Hide();
    }

    public void turnOnMode()
    {
        freeNode = false;
        offMode = false;
    }

    public boolean isFreeNode() {
        return freeNode;
    }

    public void setFreeNode(boolean freeNode) {
        this.freeNode = freeNode;
    }
}
