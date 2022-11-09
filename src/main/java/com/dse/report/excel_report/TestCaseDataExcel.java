package com.dse.report.excel_report;

import com.dse.parser.object.FunctionNode;
import com.dse.search.Search2;
import com.dse.testcase_execution.result_trace.IResultTrace;
import com.dse.testcase_execution.result_trace.ResultTrace;
import com.dse.testcase_manager.TestCase;
import com.dse.testdata.object.*;
import com.dse.util.VariableTypeUtils;
import org.apache.commons.math3.exception.NullArgumentException;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingFormatArgumentException;

public class TestCaseDataExcel {
    private TestCase testCase;
    private Integer id;
    private String unitName;
    private TestCaseResult result;
    private String testLog;
    private List<InputExpectedActualValue> parameterList = new ArrayList<>();

    public TestCaseDataExcel(TestCase testCase, Integer id) {
        this.testCase = testCase;
        this.id = id;
        init();
    }

    public String getUnitName() {
        return unitName;
    }

    public TestCase getTestCase() {
        return testCase;
    }

    public void setTestCase(TestCase testCase) {
        this.testCase = testCase;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public TestCaseResult getResult() {
        return result;
    }

    public void setResult(TestCaseResult result) {
        this.result = result;
    }

    public void setResult(String result) {
        String res = result.toUpperCase();
        switch (res) {
            case "N/A": {
                setResult(TestCaseResult.N_A);
                break;
            }
            case "FAILED": {
                setResult(TestCaseResult.Failed);
                break;
            }
            case "SUCCESS": {
                setResult(TestCaseResult.Success);
                break;
            }
            case "RUNTIME ERROR": {
                setResult(TestCaseResult.Runtime_Error);
                break;
            }
            default: throw new MissingFormatArgumentException("Wrong format type for Test case result");
        }
    }

    public List<InputExpectedActualValue> getParameterList() {
        return parameterList;
    }

    public void setParameterList(List<InputExpectedActualValue> parameterList) {
        this.parameterList = parameterList;
    }

    public void init() {
        if (testCase == null) {
            throw new NullArgumentException();
        }
        setUnitName(((FunctionNode) testCase.getFunctionNode()).getSimpleName());
        setResult(testCase.getStatus());
        generateInputExpectedActualValue();
    }

    public void generateInputExpectedActualValue() {
        //reset list
        if (getParameterList() != null) {
            setParameterList(new ArrayList<>());
        }
        TestCase testCase = getTestCase();

        parseSTUB(testCase);
        parseGlobal(testCase);
        parseInput(testCase);
//        parseExpected(testCase);
        parseActual(testCase);
        parseReturn(testCase);
    }

    public void parseSTUB(TestCase testCase) {
        List<SubprogramNode> allPossibleStub = Search2.searchStubableSubprograms(testCase.getRootDataNode());
        List<SubprogramNode> selectedStub = Search2.searchStubSubprograms(testCase.getRootDataNode());
        List<InputExpectedActualValue> listStub = new ArrayList<>();

        for (IDataNode node : selectedStub) {
            preOderRootDataNode(node, "stub parameter");
        }
        System.out.println(listStub.size());
    }

    public void parseGlobal(TestCase testCase) {
        GlobalRootDataNode globalRoot = Search2.findGlobalRoot(testCase.getRootDataNode());
        preOderRootDataNode(globalRoot, "global input");

        // parse Global expected
        for (IValueDataNode child : testCase.getGlobalInputExpOutputMap().values()) {
            preOderRootDataNode(child, "global output");
        }


    }

    public void parseInput(TestCase testCase) {
        List<IDataNode> args = Search2.findArgumentNodes(testCase.getRootDataNode());
        for (IDataNode node : args) {
            preOderRootDataNode(node, "input");
//            parameterList.addAll(preOderRootDataNode(node, "input"));
        }
//        parameterList.addAll(preOderRootDataNode(testCase.getRootDataNode()));

        parseExpected(testCase);
    }

    public void parseExpected(TestCase testCase) {
        SubprogramNode subprogramNode = Search2.findSubprogramUnderTest(testCase.getRootDataNode());
        for (IDataNode child : subprogramNode.getInputToExpectedOutputMap().values()) {
            preOderRootDataNode(child, "expected");
        }

    }

    public void parseActual(TestCase testCase) {
        if (result != TestCaseResult.N_A) {
            List<IResultTrace> traces = ResultTrace.load(testCase);
            List<InputExpectedActualValue> list = getParameterList();
            if (traces != null) {
                for (IResultTrace trace : traces) {
                    String actual = trace.getActualName();

                    InputExpectedActualValue input = InputExpectedActualValue.generate(null, null,
                            (ResultTrace) trace, "actual");
                    if (list.contains(input)) {
                        int i = list.indexOf(input);
                        InputExpectedActualValue realInput = list.get(i);
                        realInput.setActualOutput((ResultTrace) trace);
                    }
                    else {
                        list.add(input);
                        input.setTestCaseDataExcel(this);
                    }
                }
            }
        }
    }

    public void parseReturn(TestCase testCase) {
        if (!VariableTypeUtils.isVoid(testCase.getFunctionNode().getReturnType())) {
            SubprogramNode subprogramNode = Search2.findSubprogramUnderTest(testCase.getRootDataNode());
            int length = subprogramNode.getChildren().size();
            IDataNode returnNode = subprogramNode.getChildren().get(length - 1);
            preOderRootDataNode(returnNode, "return");
        }

    }

    public List<InputExpectedActualValue> preOderRootDataNode(IDataNode rootDataNode, String type) {
        List<InputExpectedActualValue> list = getParameterList();
        if (rootDataNode == null) throw new NullPointerException("parseDataNode method in TestCaseDataExcel: rootDataNode cannot be null");
        if (rootDataNode instanceof IValueDataNode) {
            if (isGenerated(rootDataNode)) {
                switch(type) {
                    case "stub return":
                    case "global input":
                    case "input": {
                        InputExpectedActualValue input = InputExpectedActualValue.generate((ValueDataNode) rootDataNode, null, null, type);
                        if (list.contains(input)) {
                            int i = list.indexOf(input);
                            InputExpectedActualValue realInput = list.get(i);
                        }
                        else {
                            list.add(input);
                            input.setTestCaseDataExcel(this);
                        }
                        break;
                    }
                    case "stub parameter":
                    case "global output":
                    case "expected":
                    case "return":{
                        InputExpectedActualValue input = InputExpectedActualValue.generate(null, (ValueDataNode) rootDataNode, null, type);
                        if (list.contains(input)) {
                            int i = list.indexOf(input);
                            InputExpectedActualValue realInput = list.get(i);
                            realInput.setExpectedOutput((ValueDataNode) rootDataNode);
                        }
                        else {
                            list.add(input);
                            input.setTestCaseDataExcel(this);
                        }
                        break;
                    }

                    case "actual": {
                        break;
                    }

//                    case "expected": {
//
//                        break;
//                    }
//                    case "actual": {
//
//                    }
                    default:
                }
            }
//            else if (rootDataNode instanceof PointerDataNode) {
//
//            }
        }

        for (IDataNode children : rootDataNode.getChildren()) {
            preOderRootDataNode(children, type);
        }
        return list;
    }

    public static boolean isGenerated(IDataNode dataNode) {
        if (dataNode instanceof NormalDataNode) {
            return ((NormalDataNode) dataNode).getValue() != null;
        }
        else if (dataNode instanceof VoidPointerDataNode) {
            return ((VoidPointerDataNode) dataNode).getReferenceType() != null;
        }
        else if (dataNode instanceof EnumDataNode) {
            return ((EnumDataNode) dataNode).getValue() != null;
        }
        else if (dataNode instanceof UnionDataNode) {
            return ((UnionDataNode) dataNode).getSelectedField() == null;
        }
        return (dataNode instanceof PointerDataNode);
    }

}
