package auto_testcase_generation.cte.core.cteTable;

import auto_testcase_generation.cte.core.ClassificationTree;
import auto_testcase_generation.cte.core.cteNode.CteClass;
import auto_testcase_generation.cte.core.cteNode.CteClassification;
import auto_testcase_generation.cte.core.cteNode.CteNode;
import auto_testcase_generation.pairwise.*;

import java.util.*;

public class CteTable {
    private final ClassificationTree tree;
    private final List<CteTableTestcase> lines = new ArrayList<>();
    private int lineId = 0;


    public CteTable(ClassificationTree tree) {
        this.tree = tree;
    }

    public void loadTable() {
        if (tree != null) {
            if (tree.getRoot() != null) {
                lines.forEach((line) -> {
                    //line.removeNodeFromSameColumn();
                    line.removeWrongNodeFromNodeList();
                });
            }
        }
    }

    public static List<CteClassification> getBelowClassification(CteNode node) {
        if (node == null) return null;
        List<CteClassification> columns = new ArrayList<>();
        for (CteNode child : node.getChildren()) {
            if (child.getClass() == CteClassification.class && !child.getChildren().isEmpty()) {
                CteClassification temp = (CteClassification) child;
                columns.add(temp);
                continue;
            }
            columns.addAll(getBelowClassification(child));
        }
        return columns;
    }

    public CteClassification getUpperClassification(CteNode node) {
        if (node == null || node.getParent() == null) return null;

        CteNode parent = node.getParent();
        if (parent.getClass() == CteClassification.class) {
            return (CteClassification) parent;
        } else {
            return getUpperClassification(parent);
        }
    }

    public CteTableTestcase addLines() {
        lineId++;
        CteTableTestcase newLines = new CteTableTestcase("Testcase " + lineId, this, lineId);
        lines.add(newLines);
        return newLines;
    }

    public CteTableTestcase addLines(List<CteNode> NodeList) {
        lineId++;
        CteTableTestcase newLines = new CteTableTestcase("Testcase " + lineId, this, lineId);
        lines.add(newLines);
        return newLines;
    }

    public CteTableTestcase addLines(String name, List<CteNode> NodeList) {
        CteTableTestcase newLines = new CteTableTestcase(name, NodeList, this, ++lineId);
        lines.add(newLines);
        return newLines;
    }

    public CteTableTestcase getLine(int lineId) {
        for (CteTableTestcase line : lines) {
            if (line.getId() == lineId) {
                return line;
            }
        }
        return null;
    }

    public void chooseNodeForLine(CteTableTestcase line, CteNode Node1) {
        if (line != null && Node1 != null) {
            line.chooseNode(Node1);
        }
    }

    public void chooseNodeForLine(int lineId, CteNode Node1) {
        chooseNodeForLine(getLine(lineId), Node1);
    }

    public void chooseNodeForLine(int lineId, String NodeId) {
        CteNode node = tree.searchNodeById(NodeId);
        chooseNodeForLine(lineId, node);
    }

    public void unchooseNodeForLine(CteTableTestcase line, CteNode Node1) {
        if (line != null && Node1 != null) {
            line.unchooseNode(Node1);
        }
    }

    public void unchooseNodeForLine(int lineId, CteNode Node1) {
        unchooseNodeForLine(getLine(lineId), Node1);
    }

    public void unchooseNodeForLine(int lineId, String NodeId) {
        CteNode node = tree.searchNodeById(NodeId);
        unchooseNodeForLine(lineId, node);
    }

    public List<CteTableTestcase> genPairwise() {

        List<Param> listParameter = new ArrayList<>();

        for(CteNode node: getBelowClassification(tree.getRoot())) {
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
        System.out.println("Successfully generated " + pws.getTestSetT().size() + " testcases!");
        return addLinesFromPairWiser(pws);
    }

    private List<CteTableTestcase> addLinesFromPairWiser(PairWiser pws) {
        List<CteTableTestcase> tcList = new ArrayList<>();
        for (int i = 0; i < pws.getTestSetT().size(); i++) {
            List<CteNode> classList = new ArrayList<>();
            for (Value value : pws.getTestSetT().get(i).getListTestData()) {
                classList.addAll(getClassListFromValue(value));
            }
            CteTableTestcase blankCoreLine = addLines();
            CteTableTestcase UILine = new CteTableTestcase(blankCoreLine);
            UILine.chooseNodes(classList);
            tcList.add(UILine);
        }
        return tcList;
    }

    private List<CteClass> getClassListFromValue(Value value) {
        if (value == null) return null;
        List<CteClass> classList = new ArrayList<>();
        Object val = value.getVal();
        if (val.getClass() == CteClass.class) {
            classList.add((CteClass) val);
        } else if (val instanceof List) {
            for (Object obj : (List)val) {
                classList.addAll(getClassListFromValue((Value) obj));
            }
        }
        return classList;
    }

    public void deleteLine(int id) {
        lines.remove(getLine(id));
    }

    public void clearLines() {
        lines.clear();
    }

    public void SysPrint() {
        System.out.println("\n---- Table: ");
        lines.forEach(CteTableTestcase::SysPrint);
    }

    public void resetLineID()
    {
        lineId = 0;
    }



    public ClassificationTree getTree() {
        return tree;
    }

    public List<CteTableTestcase> getLines() {
        return lines;
    }
}
