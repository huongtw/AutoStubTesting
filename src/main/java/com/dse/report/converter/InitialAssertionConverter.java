package com.dse.report.converter;

import com.dse.parser.object.INode;
import com.dse.search.Search2;
import com.dse.testcase_execution.result_trace.IResultTrace;
import com.dse.report.element.Table;
import com.dse.testdata.object.*;
import com.dse.testdata.object.RootDataNode;
import com.dse.util.NodeType;
import com.dse.util.Utils;

import java.util.List;

public class InitialAssertionConverter extends AssertionConverter {

    public InitialAssertionConverter(List<IResultTrace> failures, int fcalls) {
        super(failures, fcalls);
    }

    @Override
    public Table recursiveConvert(Table table, IDataNode node, int level) {
        if (node instanceof RootDataNode) {
            if (((RootDataNode) node).getLevel() != NodeType.ROOT) {
                if (!node.getChildren().isEmpty()) {
                    table.getRows().add(convertSingleNode(node, level++));
                }
            }
        } else {
            table.getRows().add(convertSingleNode(node, level++));
        }

        if (!(node instanceof ValueDataNode && ((ValueDataNode) node).isExpected())) {
            for (IDataNode child : node.getChildren())
                if (isShowInReport(child))
                    table = recursiveConvert(table, child, level);

        }

        return table;
    }

    @Override
    public Table execute(SubprogramNode root) {
        Table table = new Table(false);

        table.getRows().add(new Table.Row("Parameter", "Type", "Expected Value", "Actual Value"));

        INode sourceNode = Utils.getSourcecodeFile(root.getFunctionNode());
        String unitName = sourceNode.getName();
        Table.Row uutRow = new Table.Row("Return from UUT: " + unitName, "", "", "");
        table.getRows().add(uutRow);

        GlobalRootDataNode globalRoot = Search2.findGlobalRoot(root.getTestCaseRoot());
        if (globalRoot != null) {
            for (IDataNode child : globalRoot.getChildren()) {
//                String nodeValue = getNodeValue(child);
//                if ((nodeValue != null && !nodeValue.equals("<<null>>")))
                if (isShowInReport(child))
                    table = recursiveConvert(table, child, 1);
            }
        }
        Table.Row sutRow = new Table.Row(generateTab(1) + "Subprogram: " + root.getName(), "", "", "");
        table.getRows().add(sutRow);

        for (IDataNode child : root.getChildren())
            table = recursiveConvert(table, child, 2);

        return table;
    }

    @Override
    public Table.Row convertSingleNode(IDataNode node, int level) {
        String key = generateTab(level) + node.getDisplayNameInParameterTree();
        String type = null;
        String actualValue = null;

        if (node instanceof ValueDataNode && !(node instanceof SubprogramNode)) {
            type = ((ValueDataNode) node).getRawType();

            if (((ValueDataNode) node).isExpected()) {
//                expectedValue = getNodeValue(node);
            } else {
                actualValue = getNodeValue(node);
            }
        }

        return new Table.Row(key, type, null, actualValue);
    }
}
