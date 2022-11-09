package auto_testcase_generation.cte.core.cteNode;

import auto_testcase_generation.cte.UI.CteClassificationTree.CteComponent.CteClassNode;
import auto_testcase_generation.cte.UI.CteClassificationTree.CteComponent.CteClassificationNode;
import auto_testcase_generation.cte.UI.CteClassificationTree.CteComponent.CteCompositionNode;
import auto_testcase_generation.cte.UI.CteClassificationTree.CteComponent.CteUiNode;
import auto_testcase_generation.cte.core.ClassificationTree;
import auto_testcase_generation.cte.core.cteTable.CteTable;
import auto_testcase_generation.pairwise.*;
import com.dse.parser.object.IFunctionNode;
import com.dse.testdata.object.RootDataNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CteClass extends CteNode {

//    private RootDataNode rootDataNode;

    public CteClass() {}

    public CteClass(CteUiNode UiNode){
        super(UiNode);
    }

    public CteClass(String name, CteNode parent) {
        super(name, parent);
    }

    public CteClass(String name) {
        super(name);
    }

    public CteClass(String name, IFunctionNode functionNode) {
            super(name, functionNode);
        }


    @Override
    protected boolean checkChildren(List<CteNode> children) {
        for (CteNode node : children) {
            if (!checkChildren(node)) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected boolean checkChildren(CteNode child) {
        if (child == null) return false;
        if (child.getClass() == CteClass.class) {
            System.out.println("ERROR: Class cannot be Class's children");
            return false;
        }
        return true;
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

    @Override
    protected void printNode() {
        System.out.print(name);
    }

    @Override
    public String toString() {
        return ("CteClass " + name);
    }
//    public void setRootDataNode(ICommonFunctionNode functionNode, int i){
//
//
//        FunctionDetailTree functionDetailTree = new FunctionDetailTree(functionNode);
//        DataTree dataTree = new DataTree(functionDetailTree);
//        rootDataNode = dataTree.getRoot();
//        NormalNumberDataNode argumentNode = (NormalNumberDataNode) rootDataNode.getChildren().
//                get(0).getChildren().get(1).getChildren().get(i + 1);
//        argumentNode.setValue(name);
//
//    }

    public List<Testcase> genPairwise() {
        if(children.size() == 0) {
            return null;
        }

        List<Param> listParameter = new ArrayList<>();

        for(CteNode node: CteTable.getBelowClassification(this)) {
            List<Value> values = new ArrayList<>();
            Param param = new Param(node.getName(), values);
            for (CteNode child : node.getChildren()) {
                List<Testcase> temp = ((CteClass) child).genPairwise();
                if (temp != null) {
                    for (Testcase t : temp) {
                        Value value = new Value(param, t.getListTestData());
                        values.add(value);
                    }
                } else {
                    Value value = new Value(param, child);
                    values.add(value);
                }
            }
            listParameter.add(param);
        }

        PairWiser pws = new PairWiser(listParameter);
        pws.inOrderParameter();

        return pws.getTestSetT();
    }

}
