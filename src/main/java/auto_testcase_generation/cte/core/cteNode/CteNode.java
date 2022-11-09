package auto_testcase_generation.cte.core.cteNode;

import auto_testcase_generation.cte.UI.CteClassificationTree.CteComponent.CteUiNode;
import auto_testcase_generation.cte.core.ClassificationTree;
import com.dse.parser.object.IFunctionNode;
import com.dse.testcase_manager.TestCase;
import java.util.ArrayList;
import java.util.List;

public abstract class CteNode {
    protected String name = "";
    protected List<CteNode> children = new ArrayList<>();
    protected CteNode parent;
    protected String id;
    protected ClassificationTree tree;
    protected TestCase testcase;

    protected CteNode() {

    }

    public CteNode (String name, CteNode parent) {
        this.name = name;
        this.parent = parent;
        setId();
    }

    public CteNode (String name) {
        this.name = name;
        setId();
    }

    public CteNode(CteUiNode UiNode)
    {
        this.name = UiNode.getTitle();
    }


    public CteNode(String name, IFunctionNode functionNode) {
        this.name = name;
        setTestcase(functionNode);
    }

    public void SysPrint(int level){
        for (int i = 0; i < level; i++) {
            System.out.print("\t");
        }
        printNode();
        System.out.print(" (id " + id + ")");
        if (children.size() != 0) {
            System.out.println(":");
            for (CteNode i: children) {
                i.SysPrint(level + 1);
            }
        } else {
            System.out.println();
        }
    }

    public CteNode searchNodeById(String nodeId) {
        if (id.equals(nodeId)) return this;
        return searchDescendantById(nodeId, this);
    }

    public CteNode searchDescendantById(String nodeId, CteNode node) {
        CteNode temp = null;
        for (CteNode child : node.children) {
            if (temp != null) return temp;
            if (child.getId().equals(nodeId)) {
                return child;
            }
            temp = searchDescendantById(nodeId, child);
        }
        return temp;
    }

    public boolean isDescendantOf(CteNode ancestor) {
        for (CteNode child : ancestor.getChildren()) {
            if (child == this) {
                return true;
            }
            if (isDescendantOf(child)) return true;
        }
        return false;
    }

    public void setId() {
        if (tree != null) {
            tree.increaseNodeId();
            id = "c" + tree.getNodeID();
            if (testcase != null) {
                testcase.setName(id);
            }
        }
    }

    public void setId(String id) {
        this.id = id;
        if (testcase != null) {
            testcase.setName(id);
        }
    }

    public void setTestcase(IFunctionNode functionNode) {
        testcase = new TestCase(functionNode, "temp");
        testcase.initParameterExpectedOutputs();
        testcase.initGlobalInputExpOutputMap();
    }

    public void addChild(CteNode node) {
        if (checkChildren(node)) {
            children.add(node);
            node.setParent(this);

            if (tree != null) {
                node.setTree(tree);
                if (node.getClass() == CteClass.class) {
                    node.setTestcase(tree.getfNode());
                }
                if (node.isFreeNode()) {
                    tree.getFreeNode().remove(node);
                } else if (node.id == null) {
                    node.setId();
                }
            }
        }
    }

    public void addExistedChild(CteNode node)
    {
        if (checkChildren(node)) {
            children.add(node);
            node.setParent(this);
        }
    }


    public void addClassChild() {}
    public void addClassificationChild() {}
    public void addCompositionChild() {}

    public void addChild(CteUiNode node)
    {

    }

    public void removeChild(CteNode node) {
        children.remove(node);
        node.parent = null;
    }

    public void removeChild(String nodeId)
    {
        CteNode result = null;
        for(CteNode child : children)
        {
            if(child.getId().equals(nodeId))
            {
                result = child;
                break;
            }
        }
        if(result != null)
        {
            tree.addFreeNode(result);
            removeChild(result);
        }
    }

    public void delete() {
        if(!this.isFreeNode()) {
            parent.removeChild(this);
        }
        for (CteNode child : children) {
            child.setParent(null);
            tree.addFreeNode(child);
        }
    }


    public int height() {
        return heightRecursion(this);
    }

    private int heightRecursion(CteNode root) {
        int h = 0;
        if (root == null) return 0;
        for (CteNode child : root.getChildren()) {
            h = Math.max(heightRecursion(child), h);
        }
        return h + 1;
    }

    public boolean isFreeNode() {
        return tree.getFreeNode().contains(this);
    }

    protected abstract boolean checkChildren(List<CteNode> children);

    protected abstract boolean checkChildren(CteNode child);

    protected abstract void printNode();

    public String getName() {
        return name;
    }

    public CteNode getParent() {
        return parent;
    }

    public List<CteNode> getChildren() {
        return children;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParent(CteNode parent) {

        this.parent = parent;
        if (tree != null && this.isFreeNode()) tree.getFreeNode().remove(this);
    }

    public String getId() {
        return id;
    }

    public void setTree(ClassificationTree tree) {
        this.tree = tree;
    }

    public ClassificationTree getTree() {
        return tree;
    }

    public String toString() {
        return ("CteNode " + name);
    }

    public void setTestcase(TestCase testcase) {
        this.testcase = testcase;
    }

    public TestCase getTestcase() {
        return testcase;
    }

    public boolean isLeave() {
        return children.isEmpty();
    }

}
