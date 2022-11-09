package com.dse.report.converter;

import com.dse.parser.object.NumberOfCallNode;
import com.dse.testcase_execution.result_trace.IResultTrace;
import com.dse.parser.object.INode;
import com.dse.report.element.Table;
import com.dse.search.Search2;
import com.dse.testdata.comparable.AssertMethod;
import com.dse.testdata.object.*;
import com.dse.testdata.object.GlobalRootDataNode;
import com.dse.testdata.object.stl.ListBaseDataNode;
import com.dse.util.Utils;

import java.util.List;
import java.util.Map;

public class LastAssertionConverter extends AssertionConverter {

    private SubprogramNode sut;

    private Map<ValueDataNode, ValueDataNode> expectedGlobalMap;

    private final Map<SubprogramNode, Integer> numberOfCalls;

    public LastAssertionConverter(List<IResultTrace> failures, int fcalls, Map<SubprogramNode, Integer> numberOfCalls) {
        super(failures, fcalls);
        this.numberOfCalls = numberOfCalls;
    }

    @Override
    public Table execute(SubprogramNode root) {
        this.sut = root;

        Table table = new Table(false);

        table.getRows().add(new Table.Row("Parameter", "Type", "Expected Value", "Actual Value"));

        // Add number of stub function calls to Test Report
        appendNumberOfCalls(root, table);

        INode sourceNode = Utils.getSourcecodeFile(root.getFunctionNode());
        String unitName = sourceNode.getName();
        Table.Row uutRow = new Table.Row("Return from UUT: " + unitName, "", "", "");
        table.getRows().add(uutRow);

        GlobalRootDataNode globalRoot = Search2.findGlobalRoot(root.getTestCaseRoot());
        if (globalRoot != null) {
            expectedGlobalMap = globalRoot.getGlobalInputExpOutputMap();

            for (IDataNode child : expectedGlobalMap.keySet()) {
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

    private void appendNumberOfCalls(SubprogramNode sut, Table table) {
        RootDataNode parent = sut.getTestCaseRoot();

        List<SubprogramNode> subprograms = Search2.searchStubSubprograms(parent);

        if (!subprograms.isEmpty()) {
            table.getRows().add(new Table.Row("Number of calls", "", "", ""));
        }

        for (SubprogramNode subprogram: subprograms) {
            if (!subprogram.getChildren().isEmpty()) {
                if (subprogram.getChildren().get(0) instanceof NumberOfCallNode) {
                    NumberOfCallNode numberOfCallNode = (NumberOfCallNode) subprogram.getChildren().get(0);
                    String expectedCall = numberOfCallNode.getValue();
                    int actualCallValue = numberOfCalls.get(subprogram);
                    String actualCall = String.valueOf(actualCallValue);
                    String colorCall = Table.COLOR.RED;
                    if (expectedCall.equals(actualCall)){
                        colorCall = Table.COLOR.GREEN;
                    }
                    Table.Row newRow = new Table.Row(
                            new Table.Cell(generateTab(1) + subprogram.getName()),
                            new Table.Cell(""),
                            new Table.Cell(expectedCall, colorCall),
                            new Table.Cell(actualCall, colorCall)
                    );
                    table.getRows().add(newRow);
                }
            }
        }
    }

    @Override
    public Table recursiveConvert(Table table, IDataNode node, int level) {
        table = super.recursiveConvert(table, node, level);

        if (node instanceof PointerDataNode || node instanceof ArrayDataNode || node instanceof ListBaseDataNode) {
            ValueDataNode expectedNode = Search2.getExpectedValue((ValueDataNode) node);

            if (expectedNode != null) {
                for (IDataNode expectedChild : expectedNode.getChildren()) {
                    String expectedName = expectedChild.getName();
                    String expectedValue = getNodeValue(expectedChild);

                    if (expectedValue == null || expectedValue.equals("<<null>>"))
                        continue;

                    boolean found = false;

                    for (IDataNode actualChild : node.getChildren()) {
                        if (actualChild.getName().equals(expectedName)) {
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        table = super.recursiveConvert(table, expectedChild, level + 1);
                    }
                }
            }
        }

        return table;
    }

    @Override
    protected boolean isValuable(IDataNode node) {
        if (node instanceof ArrayDataNode || node instanceof PointerDataNode
                || node instanceof VoidPointerDataNode || node instanceof FunctionPointerDataNode) {
            String assertMethod = ((ValueDataNode) node).getAssertMethod();
            if (AssertMethod.ASSERT_NULL.equals(assertMethod)
                    || AssertMethod.ASSERT_NOT_NULL.equals(assertMethod))
                return true;
        }

        return super.isValuable(node);
    }

    @Override
    protected boolean isHaveExpected(ValueDataNode dataNode) {
        if (dataNode.getAssertMethod() == null)
            return false;

        if (notNeedValue(dataNode))
            return true;

        ValueDataNode expectedNode = Search2.getExpectedValue(dataNode);
        if (expectedNode != null) {
            return expectedNode.haveValue();
        }

        if (expectedGlobalMap.containsKey(dataNode)) {
            return expectedGlobalMap.get(dataNode).haveValue();
        }

        return super.isHaveExpected(dataNode);
    }

    @Override
    public boolean isShowInReport(IDataNode node) {
        if (node instanceof ValueDataNode) {
            ValueDataNode valueNode = (ValueDataNode) node;
            DataNode expected = Search2.getExpectedValue(valueNode);
            if (expected != null && super.isShowInReport(expected)) {
                return true;
            }
        }

        return super.isShowInReport(node);
    }
}
