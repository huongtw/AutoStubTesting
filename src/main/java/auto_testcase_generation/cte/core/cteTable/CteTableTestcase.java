package auto_testcase_generation.cte.core.cteTable;

import auto_testcase_generation.cte.core.cteNode.CteClass;
import auto_testcase_generation.cte.core.cteNode.CteClassification;
import auto_testcase_generation.cte.core.cteNode.CteNode;

import java.util.*;

public class CteTableTestcase {
    private String name;
    private List<CteNode> chosenNodes = new ArrayList<>();
    private CteTable table;
    private int id;

    CteTableTestcase(String name, CteTable table, int id) {
        this.name = name;
        this.table = table;
        this.id = id;
    }

    CteTableTestcase(String name, List<CteNode> NodeList, CteTable table, int id) {
        this.name = name;
        this.table = table;
        this.chosenNodes = NodeList;
        this.id = id;
        removeWrongNodeFromNodeList();
    }

    CteTableTestcase(CteTableTestcase line) {
        this.name = line.name;
        this.chosenNodes = line.chosenNodes;
        this.table = line.table;
        this.id = line.id;
    }

     void removeWrongNodeFromNodeList() {
        Iterator<CteNode> iterator = chosenNodes.listIterator();
        while (iterator.hasNext()) {
            CteNode node = iterator.next();
            if (node == null || !node.isLeave()) {
                iterator.remove();
            } else if (node.getTree() != table.getTree() || node.isFreeNode()) {
                System.out.println("Node " + node.getId() + " is not in tree");
                iterator.remove();
            }
        }
    }

    void removeNodeFromSameColumn() {
        List<CteNode> removeList = new ArrayList<>();
        for (int i = 0; i < chosenNodes.size(); i++) {
            CteNode tempNodeI = chosenNodes.get(i);
            if (removeList.contains(tempNodeI)) continue;

            for (int j = i + 1; j < chosenNodes.size(); j++) {
                CteNode tempNodeJ = chosenNodes.get(j);
                if (check2NodeCollide(tempNodeI, tempNodeJ)) {
                    System.out.println(name + ": Deleted Node " + tempNodeI.getId()
                            + " and " + tempNodeJ.getId());
                    removeList.add(tempNodeI);
                    removeList.add(tempNodeJ);
                }
            }
        }
        removeList.forEach(chosenNodes::remove);
    }

    boolean check2NodeCollide(CteNode node1, CteNode node2) {
        CteNode node = node1.getParent();
        while (node != null) {
            if (node2.isDescendantOf(node)) {
                return node.getClass() == CteClassification.class;
            }
            node = node.getParent();
        }
        return false;
    }

    public void chooseNode(CteNode node) {
        if (node == null || !node.isLeave()) return;
        chosenNodes.remove(node);
        chosenNodes.add(node);
    }

    public void chooseNodes(List<CteNode> nodeList) {
        for (CteNode node : nodeList) {
            chooseNode(node);
        }
    }

    public void unchooseNode(CteNode node) {
        chosenNodes.remove(node);
    }

    public void clearChosenNode() {
        chosenNodes.clear();
    }

    public void SysPrint() {
        System.out.print(name + ": ");
        chosenNodes.forEach(node1 -> System.out.print(node1.getId() + ", "));
        System.out.println();
    }

    public String getName() {
        return name;
    }

    public List<CteNode> getChosenNodes() {
        return chosenNodes;
    }

    public int getId() {
        return id;
    }

}
