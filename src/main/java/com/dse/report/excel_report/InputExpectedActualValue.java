package com.dse.report.excel_report;

import com.dse.parser.object.FunctionNode;
import com.dse.testcase_execution.result_trace.IResultTrace;
import com.dse.testcase_execution.result_trace.ResultTrace;
import com.dse.testdata.object.*;
//import com.sun.istack.internal.NotNull;
//import com.sun.istack.internal.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InputExpectedActualValue {
    private TestCaseDataExcel testCaseDataExcel;
    private List<InputExpectedActualValue> children = new ArrayList<>();
    private ValueDataNode input;
    private ValueDataNode expectedOutput;
    private IResultTrace actualOutput;
    private String absolutePath;
    private String name;
    private String type;

    public TestCaseDataExcel getTestCaseDataExcel() {
        return testCaseDataExcel;
    }

    public void setTestCaseDataExcel(TestCaseDataExcel testCaseDataExcel) {
        this.testCaseDataExcel = testCaseDataExcel;
    }

    public List<InputExpectedActualValue> getChildren() {
        return children;
    }

    public void setChildren(List<InputExpectedActualValue> children) {
        this.children = children;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ValueDataNode getInput() {
        return input;
    }

    public void setInput(ValueDataNode input) {
        this.input = input;
    }

    public ValueDataNode getExpectedOutput() {
        return expectedOutput;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    public void setExpectedOutput(ValueDataNode expectedOutput) {
        this.expectedOutput = expectedOutput;
    }

    public IResultTrace getActualOutput() {
        return actualOutput;
    }

    public void setActualOutput(ResultTrace actualOutput) {
        this.actualOutput = actualOutput;
    }

    public String getName() {
        return name;
    }

    public String getNameInExcel() {
//        FunctionNode functionNode = (input == null) ? input.getCorrespondingVar().getSetterNode() : //todo here
        switch (type) {
            case "stub": {
                return "[STUB]["+ testCaseDataExcel.getTestCase().getFunctionNode().getSimpleName() + "]" + name;
            }
            default:
                return getName();
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public static InputExpectedActualValue generate(/*@Nullable*/ ValueDataNode input, /*@*/ ValueDataNode expectedOutput
            , /*@Nullable*/ ResultTrace actualOutput, /*@NotNull*/ String type) {
        InputExpectedActualValue obj = new InputExpectedActualValue();
        obj.setType(type);
        if (input != null) {
            obj.setInput(input);
            obj.setName(input.getVituralName());
            obj.setAbsolutePath(input.getCorrespondingVar().getAbsolutePath());
        }
//        if (input instanceof NormalDataNode) {
//            obj.setName((NormalDataNode)input);
//        }

        if (expectedOutput != null) {
            obj.setExpectedOutput(expectedOutput);
            String name = expectedOutput.getVituralName();
            if ((type.equals("return"))) {
                obj.setName(name.replace("AKA_EXPECTED_OUTPUT", "RETURN"));
            }
//            else {
//                obj.setName(expectedOutput.getVituralName());
//            }
            obj.setAbsolutePath(expectedOutput.getCorrespondingVar().getAbsolutePath());
        }
        if (actualOutput != null) {
            String name = actualOutput.getActualName();
            if (name.contains("AKA_ACTUAL_OUTPUT")) {
                name = name.replace("AKA_ACTUAL_OUTPUT", "RETURN");
            }
            obj.setName(name);
            obj.setActualOutput(actualOutput);
        }

        return obj;
    }

    private String getDataNodeToString(ValueDataNode valueDataNode) {
        if (valueDataNode == null) return null;
        if (valueDataNode instanceof NormalDataNode) {
            return ((NormalDataNode) valueDataNode).getValue();
        }
        else if (valueDataNode instanceof PointerDataNode) {
            return String.valueOf(((PointerDataNode) valueDataNode).getAllocatedSize());
        }
        else if (valueDataNode instanceof VoidPointerDataNode) {
            return ((VoidPointerDataNode) valueDataNode).getReferenceType();
        }
        return "nothing";
    }

    public String getInputToString() {
        return getDataNodeToString(input);
    }
    public String getExpectedOutputToString() {
        return getDataNodeToString(expectedOutput);
    }
    public String getActualOutputToString() {
        if (actualOutput == null) return ""; // "none";
        return actualOutput.getActual();
    }

//    public static String getDataNodeName(IDataNode dataNode) {
//        IDataNode parent = dataNode.getParent();
//        return (parent == null) ? dataNode.getName() : getDataNodeName(dataNode.getParent()) + "." + dataNode.getName();
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InputExpectedActualValue that = (InputExpectedActualValue) o;
        if (this.absolutePath != null && that.absolutePath != null) {
            return absolutePath.equals(that.absolutePath);
        }
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(absolutePath, name);
    }

    @Override
    public String toString() {
        return "{" +
                getName() + "=" + getInputToString() +
                ", absolutePath='" + absolutePath + '\'' +
                '}';
    }
}
