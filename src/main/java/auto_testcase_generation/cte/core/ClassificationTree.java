package auto_testcase_generation.cte.core;

import auto_testcase_generation.cte.core.cteNode.CteClass;
import auto_testcase_generation.cte.core.cteNode.CteClassification;
import auto_testcase_generation.cte.core.cteNode.CteComposition;
import auto_testcase_generation.cte.core.cteNode.CteNode;
import com.dse.parser.object.FunctionNode;
import com.dse.parser.object.IFunctionNode;

import java.util.ArrayList;
import java.util.List;

public class ClassificationTree implements IClassificationTree{
    private String name;
    private CteComposition root;
    private FunctionNode fNode;
    private int nodeID = 0;
    private final List<CteNode> freeNode = new ArrayList<>();

    public ClassificationTree(String name, CteComposition root, IFunctionNode fNode) {
        this.name = name;
        this.fNode = (FunctionNode) fNode;
        this.root = root;
        root.setTree(this);
    }

    public ClassificationTree(String name, IFunctionNode fNode) {
        this.name = name;
        this.fNode = (FunctionNode) fNode;
    }

    public String getName() {
        return name;
    }

    public CteComposition getRoot() {
        return root;
    }

    public FunctionNode getfNode() {
        return fNode;
    }

    public void setRoot(CteComposition root) {
        this.root = root;
        setTreeRecursion(root);
    }

    public void initializeTreeTestcase(CteNode node) {
        for (CteNode child : node.getChildren()) {
            if (child.getClass() == CteClass.class) {
                child.setTestcase(fNode);
            }
            initializeTreeTestcase(child);
        }
    }

    public void SysPrint() {
        root.SysPrint(0);
    }

    public CteNode searchNodeById(String nodeId) {
        for (CteNode node : freeNode) {
            if (node.searchNodeById(nodeId) != null) {
                return node.searchNodeById(nodeId);
            }
        }
        return root.searchNodeById(nodeId);
    }

    public CteClass searchClassById(String classId) {
        CteNode temp = root.searchNodeById(classId);
        if (temp != null) {
            if (temp.getClass() == CteClass.class)
                return (CteClass) temp;
        }
        return null;
    }

    public void increaseNodeId() {
        nodeID++;
    }

    public int getNodeID() {
        return nodeID;
    }

    public void setIdForNodes() {
        for (int i = 1; i <= root.height(); i++) {
            setIdForCurrentLevel(root, i);
        }
    }

    private void setIdForCurrentLevel(CteNode node, int level) {
        if (node == null) return;
        if (level == 1) {
            node.setId();
        } else if (level > 1) {
            for (CteNode child : node.getChildren()) {
                setIdForCurrentLevel(child, level - 1);
            }
        }
    }

    private void setTreeRecursion(CteNode node){
        node.setTree(this);

        for (CteNode child : node.getChildren()) {
            setTreeRecursion(child);
        }
    }

    public List<CteNode> getFreeNode() {
        return freeNode;
    }

    public void addFreeNode(CteNode node) {
        if (node != null) {
            if (node.getTree() == this) {
                freeNode.add(node);
            }
        }
    }

    public void setNodeId(int numberOfNode) {
        nodeID = numberOfNode;
    }


    public int getNumberOfNodesInTree()
    {;
        return countChildNodesfromNode(root);
    }

    private int countChildNodesfromNode(CteNode node)
    {
        int result = 1;
        if(node.getChildren().size() == 0) return  result;
        else
        {
            for (int i = 0; i < node.getChildren().size(); i++)
            {
                result += countChildNodesfromNode(node.getChildren().get(i));
            }
            return result;
        }
    }
}
