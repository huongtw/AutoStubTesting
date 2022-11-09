package com.dse.report.converter;

import com.dse.report.element.Table;
import com.dse.testdata.Iterator;
import com.dse.testdata.object.*;
import com.dse.testdata.object.RootDataNode;

import java.util.List;

public class DataTreeConverter extends Converter {
    public static Table execute(IDataNode root) {
        Table table = new DataTreeConverter().recursiveConvert(new Table(false), root, 0);
        table.getRows().add(0, new Table.HeaderRow("", "Expected Value", "Actual Value"));
        return table;
    }

    public boolean isShowInReport(IDataNode node) {
        if (node instanceof RootDataNode) {
            switch (((RootDataNode) node).getLevel()) {
                case GLOBAL:
                    return !node.getChildren().isEmpty();
                case STUB:
                case SBF:
                    boolean result = false;

                    for (IDataNode child : node.getChildren())
                        result = result || isShowInReport(child);

                    return result;
                default:
                    return true;
            }
        } else if (node instanceof SubprogramNode && !(node instanceof ConstructorDataNode))
            return !node.getChildren().isEmpty();
        else if (node instanceof UnitNode) {
            if (node instanceof StubUnitNode) {
                boolean result = false;

                for (IDataNode child : node.getChildren())
                    result = result || isShowInReport(child);

                return result;
            } else
                return true;
        } else
            return true;
    }

    @Override
    public Table recursiveConvert(Table table, IDataNode node, int level) {
        table = super.recursiveConvert(table, node, level);

        if (node instanceof ValueDataNode) {
            List<Iterator> iterators = ((ValueDataNode) node).getIterators();
            boolean isFirstNode = iterators.get(0).getDataNode() == node;

            for (int i = 1; i < iterators.size() && isFirstNode; i++) {
                ValueDataNode dataNode = iterators.get(i).getDataNode();
                table = recursiveConvert(table, dataNode, level);
            }
        }

        return table;
    }

    public Table.Row convertSingleNode(IDataNode node, int level) {
        String key = generateTab(level);

        if (node instanceof UnitNode)
            key += "IN UNIT => ";
        else if (node instanceof SubprogramNode && !(node instanceof ConstructorDataNode))
            key += "SUBPROGRAM => ";

        key += node.getDisplayNameInParameterTree();

        String expectedValue = "", actualValue = "";

        if (node instanceof ValueDataNode && !(node instanceof SubprogramNode)) {
            if (((ValueDataNode) node).isExpected())
                expectedValue = getNodeValue(node);
            else
                actualValue = getNodeValue(node);
        }

        return new Table.Row(key, expectedValue, actualValue);
    }
}
