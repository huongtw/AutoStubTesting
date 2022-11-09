package auto_testcase_generation.cte.core.cteNode;

import auto_testcase_generation.cte.UI.CteClassificationTree.CteComponent.CteClassificationNode;
import auto_testcase_generation.cte.UI.CteClassificationTree.CteComponent.CteCompositionNode;
import auto_testcase_generation.cte.UI.CteClassificationTree.CteComponent.CteUiNode;

import java.util.List;

public class CteComposition extends CteNode {

    public CteComposition() {}

    public CteComposition(String name){
        super(name);
    }

    public CteComposition(CteUiNode UiNode)
    {
        super(UiNode);
    }

    public void addClassificationChild()
    {
        CteClassification newChild = new CteClassification("Blank");
        addChild(newChild);
    }

    public void addCompositionChild()
    {
        CteComposition newChild = new CteComposition("Blank");
        addChild(newChild);
    }

    public void addChild(CteUiNode node)
    {
        if(node instanceof CteClassificationNode)
        {
            CteClassification newChild = new CteClassification(node);
            addChild(newChild);
        }
        else if(node instanceof CteCompositionNode)
        {
            CteComposition newChild = new CteComposition(node);
            addChild(newChild);
        }
    }

    protected boolean checkChildren(List<CteNode> children) {
        for (CteNode node : children) {
            if (node.getClass() == CteClass.class) {
                System.out.println("ERROR: Class cannot be Composition's children");
                return false;
            }
        }
        return true;
    }

    protected boolean checkChildren(CteNode child) {
        if (child.getClass() == CteClass.class) {
            System.out.println("ERROR: Class cannot be Composition's children");
            return false;
        }
        return true;
    }

    public boolean isRoot() {
        return parent == null;
    }

    @Override
    protected void printNode() {
        System.out.print("==" + name);
    }

    @Override
    public String toString() {
        return ("CteComposition " + name);
    }


}
