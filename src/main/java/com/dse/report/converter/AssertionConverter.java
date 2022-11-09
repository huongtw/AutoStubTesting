package com.dse.report.converter;

import com.dse.parser.object.IFunctionPointerTypeNode;
import com.dse.testcase_execution.result_trace.AssertionResult;
import com.dse.testcase_execution.result_trace.IResultTrace;
import com.dse.parser.object.EnumNode;
import com.dse.parser.object.INode;
import com.dse.report.element.IElement;
import com.dse.report.element.Table;
import com.dse.testdata.comparable.AssertMethod;
import com.dse.testdata.object.*;
import com.dse.testdata.object.RootDataNode;
import com.dse.testdata.object.stl.ListBaseDataNode;
import com.dse.util.SourceConstant;
import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;

import java.util.Arrays;
import java.util.List;

public abstract class AssertionConverter extends Converter {
    /**
     * List of failures from gtest report
     */
    protected List<IResultTrace> failures;

    /**
     * Function call tag: index of function call expresstion
     */
    protected int fcalls;

    /**
     * Result PASS/ALL
     */
    protected AssertionResult results = new AssertionResult();

    public AssertionConverter(List<IResultTrace> failures, int fcalls) {
        this.failures = failures;
        this.fcalls = fcalls;
    }

    public Table execute(SubprogramNode root) {
        Table table = generateHeaderRows(root);

        for (IDataNode child : root.getChildren())
            table = recursiveConvert(table, child, 2);

        return table;
    }

    protected Table generateHeaderRows(SubprogramNode root) {
        Table table = new Table(false);

        table.getRows().add(new Table.Row("Parameter", "Type", "Expected Value", "Actual Value"));

        INode sourceNode = Utils.getSourcecodeFile(root.getFunctionNode());
        String unitName = sourceNode.getName();
        Table.Row uutRow = new Table.Row("Calling UUT: " + unitName, "", "", "");
        table.getRows().add(uutRow);
        Table.Row sutRow = new Table.Row(generateTab(1) + "Subprogram: " + root.getName(), "", "", "");
        table.getRows().add(sutRow);

        return table;
    }

    @Override
    public boolean isShowInReport(IDataNode node) {
        if (node instanceof ValueDataNode) {
            ValueDataNode valueNode = (ValueDataNode) node;
            IDataNode parent = valueNode.getParent();

            if (parent instanceof SubprogramNode)
                return true;

            if (parent instanceof UnionDataNode) {
                String field = ((UnionDataNode) parent).getSelectedField();
                return node.getName().equals(field);
            }

            if (parent instanceof ArrayDataNode || parent instanceof PointerDataNode) {
                if (!((ValueDataNode) parent).haveValue()) {
                    return false;
                }

                if (!valueNode.haveValue()) {
                    return false;
                }
            }

            if (isShowInReport(node.getParent()))
                return true;

            return valueNode.haveValue();
        }

        return false;
    }

    @Override
    public Table.Row convertSingleNode(IDataNode node, int level) {
        String key = generateTab(level) + node.getDisplayNameInParameterTree();

        String type = "";
        if (node instanceof ValueDataNode && !(node instanceof SubprogramNode))
            type = ((ValueDataNode) node).getRawType();

        Table.Row row = new Table.Row(key, type);

        Table.Cell[] valueCells = new Table.Cell[] {new Table.Cell<>(""), new Table.Cell<>("")};
        if (node instanceof ValueDataNode && isValuable(node)) {
            ValueDataNode dataNode = (ValueDataNode) node;

            if (isHaveExpected(dataNode)) {
                String expectedName = node.getVituralName();
                if (dataNode instanceof FunctionPointerDataNode && node.getName().isEmpty()) {
                    IFunctionPointerTypeNode typeNode = ((FunctionPointerDataNode) dataNode).getCorrespondingType();
                    String functionName = typeNode.getFunctionName();
                    expectedName = node.getVituralName() + functionName;
                }
                valueCells = findValuesExpectCase(dataNode, expectedName);
            } else
                valueCells = findValuesActualCase(dataNode);
        }

        row.getCells().addAll(Arrays.asList(valueCells));

        return row;
    }

    protected boolean isHaveExpected(ValueDataNode dataNode) {
        if (dataNode.getAssertMethod() == null)
            return false;

        if (notNeedValue(dataNode))
            return true;

        if (!dataNode.isExpected())
            return false;

        if (!dataNode.haveValue())
            return dataNode instanceof NormalDataNode
                    || dataNode instanceof EnumDataNode;

        return true;
    }

    protected boolean notNeedValue(ValueDataNode dataNode) {
        String assertMethod = dataNode.getAssertMethod();
        return AssertMethod.ASSERT_NULL.equals(assertMethod)
                || AssertMethod.ASSERT_NOT_NULL.equals(assertMethod)
                || AssertMethod.ASSERT_TRUE.equals(assertMethod)
                || AssertMethod.ASSERT_FALSE.equals(assertMethod);
    }

    protected Table.Cell[] findValuesActualCase(ValueDataNode node) {
        String actualValue = getNodeValue(node);
        String expectedValue = SpecialCharacter.EMPTY;

        return new Table.Cell[] {
                new Table.Cell<>(expectedValue),
                new Table.Cell<>(actualValue)
        };
    }

    protected Table.Cell[] findValuesExpectCase(ValueDataNode node, String expectedName) {
        String expectedValue = getNodeValue(node);
        expectedValue = AssertMethod.findExpectedFromFailure(node.getAssertMethod(), expectedValue);
        String actualValue = "<<Unknown>>";

        if (node instanceof ListBaseDataNode)
            expectedName += ".size()";

        String expectedOutputRegex = "\\Q" + SourceConstant.EXPECTED_OUTPUT + "\\E";
        expectedName = expectedName.replaceFirst(expectedOutputRegex, SourceConstant.ACTUAL_OUTPUT);

        boolean match = false;

        if (failures != null) {
            boolean found = false;

            for (IResultTrace failure : failures) {
                String fcallTag = "Aka function calls: " + fcalls;

                if ((failure.getExpectedName().equals(expectedName)
                        || failure.getActualName().equals(expectedName))
                        && failure.getMessage().contains(fcallTag)) {

                    found = true;

                    String[] values = findValuesFromFailure(node, failure);
                    actualValue = values[1];
                    expectedValue = values[0];

                    match = isMatch(node, failure);

                    break;
                }
            }

            if (!found) {
                return new Table.Cell[]{
                        new Table.Cell<>(SpecialCharacter.EMPTY),
                        new Table.Cell<>(SpecialCharacter.EMPTY)
                };
            }
        }

        String bgColor = IElement.COLOR.RED;
        if (AssertMethod.USER_CODE.equals(node.getAssertMethod())) {
            bgColor = IElement.COLOR.LIGHT;
        } else {
            if (match) {
                bgColor = IElement.COLOR.GREEN;
//                results[0]++;
                results.increasePass();
            }

//            results[1]++;
            results.increaseTotal();
        }

        return new Table.Cell[] {
                new Table.Cell<>(expectedValue, bgColor),
                new Table.Cell<>(actualValue, bgColor)
        };
    }

    protected boolean isValuable(IDataNode node) {
        if (node instanceof RootDataNode || node instanceof SubprogramNode)
            return false;

        else if (((ValueDataNode) node).isUseUserCode())
            return true;

        else if (((ValueDataNode) node).isExpected()) {
            ValueDataNode valueNode = (ValueDataNode) node;
            return valueNode.haveValue() || notNeedValue(valueNode);

        } else if (node instanceof StructDataNode || node instanceof ClassDataNode
                || node instanceof UnionDataNode)
            return false;

        else if (node instanceof ArrayDataNode || node instanceof PointerDataNode) {
            String value = getNodeValue(node);
            return value != null && !value.isEmpty();

        } else if (node instanceof NormalDataNode || node instanceof EnumDataNode)
            return true;

        else if (node instanceof VoidPointerDataNode) {
            return ((VoidPointerDataNode) node).getInputMethod() == VoidPointerDataNode.InputMethod.USER_CODE;

        } else
            return node instanceof ListBaseDataNode || node instanceof UnresolvedDataNode;
    }

    protected boolean isMatch(ValueDataNode node, IResultTrace failure) {
        String actual = failure.getActual();
        String expected = failure.getExpected();

        if (node instanceof EnumDataNode) {
            INode typeNode = node.getCorrespondingType();
            if (typeNode instanceof EnumNode) {
                EnumNode enumNode = (EnumNode) typeNode;
                for (String name : enumNode.getAllNameEnumItems()) {
                    String val = enumNode.getValueOfEnumItem(name);
                    if (val.equals(actual)) {
                        actual = name;
                    }
                    if (val.equals(expected)) {
                        expected = name;
                    }
                }
            }
        }

        return AssertMethod.isMatch(node.getAssertMethod(), actual, expected);
    }

    protected String[] findValuesFromFailure(ValueDataNode node, IResultTrace failure) {
        String[] values = new String[2];
        values[1] = failure.getActual();

//        if (node instanceof EnumDataNode) {
//            INode typeNode = node.getCorrespondingType();
//            if (typeNode instanceof EnumNode) {
//                EnumNode enumNode = (EnumNode) typeNode;
//                for (String name : enumNode.getAllNameEnumItems()) {
//                    String val = enumNode.getValueOfEnumItem(name);
//                    if (val.equals(values[1])) {
//                        values[1] = name;
//                    }
//                }
//            }
//        }

        values[0] = AssertMethod.findExpectedFromFailure(node.getAssertMethod(), failure.getExpected());

        return values;
    }

    public static final String FALSE_VALUE = "0";
    public static final String NULL_VALUE = "0";

    public AssertionResult getResults() {
        return results;
    }

    protected static final int OFFSET = 10;

    protected static final String PROBLEM_VALUE = "Can't find variable value";

    protected static final String MATCH = "MATCH";
}
