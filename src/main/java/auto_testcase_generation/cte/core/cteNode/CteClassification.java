package auto_testcase_generation.cte.core.cteNode;

import auto_testcase_generation.cte.UI.CteClassificationTree.CteComponent.CteClassNode;
import auto_testcase_generation.cte.UI.CteClassificationTree.CteComponent.CteClassificationNode;
import auto_testcase_generation.cte.UI.CteClassificationTree.CteComponent.CteCompositionNode;
import auto_testcase_generation.cte.UI.CteClassificationTree.CteComponent.CteUiNode;

import java.util.List;

public class CteClassification extends CteNode {

    public CteClassification() {}

    public CteClassification(String name) {
        super(name);
    }

    public CteClassification(String name, CteNode parent) {
        super(name, parent);
    }

    public CteClassification(CteUiNode UiNode)
    {
        super(UiNode);
    }

    public void setChildren(List<CteNode> children) {
        this.children = children;
    }

    @Override
    protected void printNode() {
        System.out.print("--" + name);
    }

    public int numberOfLeaves() {
        int count = 0;
        for (CteNode child : children) {
            if (child.getClass() == CteClass.class) {
                if (child.isLeave()) {
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    protected boolean checkChildren(List<CteNode> children) {
        for (CteNode child : children) {
            if (child.getClass() == CteClassification.class || child.getClass() == CteComposition.class) {
                System.out.println("ERROR: Classification and Composition cannot be Classification's children");
                return false;
            }
        }
        return true;
    }

    @Override
    protected boolean checkChildren(CteNode child) {
        if (child.getClass() == CteClassification.class || child.getClass() == CteComposition.class) {
            System.out.println("ERROR: Classification and Composition cannot be Classification's children");
            return false;
        }
        return true;
    }

    public void addClassChild()
    {
        CteClass newChild = new CteClass("Blank");
        this.addChild(newChild);
    }

    public void addChild(CteUiNode node)
    {
        if(node instanceof CteClassNode)
        {
            CteClass newChild = new CteClass(node);
            addChild(newChild);
        }
    }

    @Override
    public String toString() {
        return ("CteClassification " + name);
    }
}
