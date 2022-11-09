package auto_testcase_generation.cte.UI.CteClassificationTree.CteComponent;

import javafx.scene.shape.Line;

public class Edge extends Line {

    private DraggableNode parentNode;
    private DraggableNode childNode;

    public Edge()
    {
        new Line();
    }

    public Edge(DraggableNode par, DraggableNode chi)
    {
        new Line();
        parentNode = par;
        childNode = chi;
        this.startXProperty().bind(par.xProperty().add(par.getWidth()/2));
        this.startYProperty().bind(par.yProperty().add(par.getHeight()));

        this.endXProperty().bind(chi.xProperty().add(childNode.getWidth()/2));
        this.endYProperty().bind(chi.yProperty());

        this.setStrokeWidth(1);
    }

    public void reBindEdge(){
        this.startXProperty().bind(parentNode.xProperty().add(parentNode.getWidth()/2));
        this.startYProperty().bind(parentNode.yProperty().add(parentNode.getHeight()));
        if(childNode!=null) {
            this.endXProperty().bind(childNode.xProperty().add(childNode.getWidth()/2));
            this.endYProperty().bind(childNode.yProperty());
        }
    }
}
