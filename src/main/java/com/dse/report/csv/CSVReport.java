package com.dse.report.csv;

import com.dse.guifx_v3.controllers.object.LoadingPopupController;
import com.dse.guifx_v3.helps.UIController;
import com.dse.parser.object.*;
import com.dse.search.Search2;
import com.dse.testcase_manager.TestCase;
import com.dse.testdata.Iterator;
import com.dse.testdata.object.*;
import com.dse.util.Utils;
import com.dse.util.UtilsCsv;
import com.dse.util.VariableTypeUtils;
import org.eclipse.cdt.core.dom.ast.IASTNode;

import java.io.File;
import java.util.*;

public class CSVReport {

    private static final String _COMMA = ",";
    private static final String _AMSTB = "AMSTB_";
    private static final String _STUBCNT = "STUBCNT_";
    private static final String _STUBRET = "STUBRET_";
    private static final String _STUBARG = "STUBARG_";
    private static final String _ARG = "ARG";

    private String modLine;
    private String stubLine;
    private String functionLanguage;
    private List<String> stub_func = new ArrayList<>();
    private String comment = "#COMMENT" + _COMMA;
    //each String = 1 test data of a test case
    private List<String> listValue = new ArrayList<>();
    private int input_num = 0;
    private int output_num = 0;

    private IFunctionNode functionNode;
    private List<TestCase> listTestcase = new ArrayList<>();
    private List<CsvParameter> listParameter = new ArrayList<>();
    private List<CsvParameter> listOutputParameter = new ArrayList<>();
    private List<CsvParameter> listInputGlobal = new ArrayList<>();
    private List<CsvParameter> listDummyParamToStubC = new ArrayList<>();

    private boolean isGlobalInput = false;
    private boolean isArgInput = false;
    private boolean isReturnNode = false;
    private boolean isStatics = false;

    private Queue<CsvParameter> pointerParam = new LinkedList<>();
    private List<CsvParameter> allParameter = new ArrayList<>();
    private  List<CsvParameter> inputParameter = new ArrayList<>();
    private List<CsvParameter> outputParameter = new ArrayList<>();

    private List<CsvParameter> listSelectedStub = new ArrayList<>();

    public List<CsvParameter> getListInputGlobal() {
        return listInputGlobal;
    }

    public void setListInputGlobal(List<CsvParameter> listInputGlobal) {
        this.listInputGlobal = listInputGlobal;
    }

//    private TestCase currentTestcase;

    public CSVReport(IFunctionNode functionNode, List<TestCase> testCases) {
        setFunctionNode(functionNode);
        setListTestcase(testCases);
        stubLine = "";
    }

    public void setFunctionNode(IFunctionNode functionNode) {
        this.functionNode = functionNode;
        if (functionNode.getParent() instanceof CppFileNode) {
            this.functionLanguage = "CPP";
        } else this.functionLanguage = "C";
    }

    public void setListTestcase(List<TestCase> listTestcase) {
        this.listTestcase = listTestcase;
    }

    public IFunctionNode getFunctionNode() {
        return functionNode;
    }

    public List<TestCase> getListTestcase() {
        return listTestcase;
    }

    public void exportTo(String path) {
        parse();
        String content = generateContent(); // CSV content
        File csvFile = new File(path);
        Utils.writeContentToFile(content, path);
        String stubCContent = generateStubCContentFile();
        Utils.writeContentToFile(stubCContent, csvFile.getParent() + "\\STUB_" + csvFile.getName().replaceAll(".csv", ".c"));
//        Utils.writeContentToFile();
    }

    private void parse() {
        //   List<String> listDummyName = UtilsCsv.getPointerArgCheckNullDirect(functionNode.getAST());
        //  System.out.println(listDummyName);
        for (TestCase testCase : listTestcase) {
            parseStubTestData(testCase);
            parseInputTestData(testCase);
            parseOutputTestData(testCase);
        }

        listOutputParameter.sort(new Comparator<CsvParameter>() {
            @Override
            public int compare(CsvParameter o1, CsvParameter o2) {
                return o1.compareTo(o2);
            }
        });

        listInputGlobal.sort(new Comparator<CsvParameter>() {
            @Override
            public int compare(CsvParameter o1, CsvParameter o2) {
                return o1.compareTo(o2);
            }
        });

        listParameter.sort(new Comparator<CsvParameter>() {
            @Override
            public int compare(CsvParameter o1, CsvParameter o2) {
                return o1.compareTo(o2);
            }
        });

        listSelectedStub.sort(new Comparator<CsvParameter>() {
            @Override
            public int compare(CsvParameter o1, CsvParameter o2) {
                return o1.compareTo(o2);
            }
        });

        //set #COMMENT
        //set count INPUT AND OUTPUT
        input_num = 0;
        output_num = 0;

        // moveReturnNodeToFirst(); // move returnNode in stub subprogram to the top of list

        for (CsvParameter parameter : listSelectedStub) {
            SubprogramNode subProgram = (SubprogramNode) parameter.getDataNode();
            IFunctionNode subProgramFunctionNode = (IFunctionNode) subProgram.getFunctionNode();
            String name = subProgramFunctionNode.getSimpleName();
            stubLine += "%" + _COMMA + "AMSTB_" + name + _COMMA + name + _COMMA + "\n";
            comment += parameter.getNameFormat() + _COMMA;
            input_num++;
            allParameter.add(parameter);
            getCmtForStub(parameter);
        }

//        for (CsvParameter parameter1 : UtilsCsv.getChildInPointerParameter(pointerParam)) {
//            if (parameter1.getType() == CsvParameter._INPUT) input_num++;
//            else output_num++;
//            allParameter.add(parameter1);
//            comment += parameter1.getNameFormat() + _COMMA;
//        }

        for (CsvParameter parameter : listParameter) {
            comment += getCmt(parameter);
        }
//        for (CsvParameter parameter1 : UtilsCsv.getChildInPointerParameter(pointerParam)) {
//            if (parameter1.getType() == CsvParameter._INPUT) input_num++;
//            else output_num++;
//            allParameter.add(parameter1);
//            comment += parameter1.getNameFormat() + _COMMA;
//        }

        for (CsvParameter parameter : listInputGlobal) {
            comment += getCmt(parameter);
        }
//        for (CsvParameter parameter1 : UtilsCsv.getChildInPointerParameter(pointerParam)) {
//            if (parameter1.getType() == CsvParameter._INPUT) input_num++;
//            else output_num++;
//            comment += parameter1.getNameFormat() + _COMMA;
//            allParameter.add(parameter1);
//        }

        for (CsvParameter parameter : listOutputParameter) {
            comment += getCmt(parameter);
        }

//        for (CsvParameter parameter1 : UtilsCsv.getChildInPointerParameter(pointerParam)) {
//            if (parameter1.getDataNode() instanceof PointerDataNode) continue;
//            if (parameter1.getType() == CsvParameter._INPUT) input_num++;
//            else output_num++;
//            comment += parameter1.getNameFormat() + _COMMA;
//            allParameter.add(parameter1);
//        }
       // scaleOutputElement(inputParameter,outputParameter);
        resetModLine();

        // set input
        for (int i = 0; i < listTestcase.size(); i++) {
            String input = _COMMA;
            for (CsvParameter parameter : allParameter) {
                input += getValue_(parameter, i);
            }

           /* for (CsvParameter parameter : listParameter) {
                input += getValue(parameter, i);
            }
            for (CsvParameter parameter : listInputGlobal) {
                input += getValue(parameter, i);
            }
            for (CsvParameter parameter : listOutputParameter) {
                input += getValue(parameter, i);
            }*/

            if (!input.equals(_COMMA)) {
                listValue.add(input);
            }
        }

    }

    private void  scaleOutputElement(List<CsvParameter> listInput_, List<CsvParameter> listOutput_,List<CsvParameter> allParameter_) {
        List<String> listInputName = new ArrayList<>();
        List<CsvParameter> listIndex = new ArrayList<>();

        for(CsvParameter parameter:listInput_) {
            listInputName.add(parameter.getNameFormat());
        }

        for (CsvParameter csvParameter : listOutput_) {
            if (csvParameter.isReturn()) {
                if(csvParameter.getDataNode() instanceof PointerDataNode) {
                    if(csvParameter.getNameFormat().contains("$")) listIndex.add(csvParameter);
                    else if(csvParameter.getNameFormat().contains("@")
                            && !csvParameter.getNameFormat().contains("@@")) listIndex.add(csvParameter);
                }
                continue;
            }
            else if(csvParameter.getDataNode() instanceof  PointerDataNode) {
                if(csvParameter.getNameFormat().contains("$")||csvParameter.getNameFormat().contains("@")) listIndex.add(csvParameter);
            }
            else if (!listInputName.contains(csvParameter.getNameFormat())) listIndex.add(csvParameter);
        }

        for (CsvParameter parameter: listIndex) {
            if (!parameter.isStatics()) {
                allParameter_.remove(parameter);
                allParameter.remove(parameter);
                output_num--;
            }
        }
    }

    //CSV content
    private String generateContent() {
        String content;
        content = modLine + "\n" + (stubLine.equals("") ? "" : stubLine) + comment + "\n";
        System.out.println(modLine);
        System.out.println(comment);
        for (String val : listValue) {
            System.out.println(val);
            content += val + "\n";
        }
        return content;
    }

    @Deprecated
    private String getValue(CsvParameter parameter, int id) {
        Integer idx = id;
        String res = "";
        if ((parameter.getDataNode() instanceof NormalDataNode) || (parameter.getDataNode() instanceof EnumDataNode)
                || parameter.getDataNode() instanceof PointerDataNode || parameter.isHaveValue()
        ) {
            if (parameter.isHaveValue() || parameter.getDataNode() instanceof PointerDataNode) {
                CsvValueNode valueNode = parameter.getValueMap().get(idx);
                if (valueNode == null) {
                    res += _COMMA;
                } else {
                    res += valueNode.getValue() + _COMMA;
                }
            }
        }
        if (parameter.getChildren() != null) {
            for (CsvParameter param : parameter.getChildren()) {
                res += getValue(param, id);
            }
        }
        return res;
    }

    @Deprecated
    private String getComment(CsvParameter parameter) {
        String res = "";
        if (parameter.isHaveValue()) {
            if ((parameter.getDataNode() instanceof NormalDataNode)
                    || (parameter.getDataNode() instanceof EnumDataNode)
                    || (parameter.getDataNode() instanceof UnionDataNode)
                    || (parameter.getDataNode() instanceof PointerDataNode && (parameter.getType() == CsvParameter._INPUT || parameter.isReturn() == true))) {
                res += parameter.getNameFormat() + _COMMA;
                allParameter.add(parameter);
                if (parameter.getType() == CsvParameter._INPUT) {
                    input_num++;
                } else output_num++;
            }
        }
        if (parameter.getChildren() != null) {
            for (CsvParameter param : parameter.getChildren()) {
                res += getComment(param);
            }
        }
        return res;
    }

    private String getCmt(CsvParameter parameter) {
        String result = "";
        Stack<CsvParameter> stack = new Stack<>();
        List<CsvParameter> list = new ArrayList<>();
        stack.push(parameter);
        boolean isOutput = listOutputParameter.contains(parameter);
        while (!stack.isEmpty()) {
            CsvParameter current = stack.pop();
            if (((current.isHaveValue() && (isDisplayedParameter(current)))
                    ||((current.isHaveValue() && current.isStub())))) {
                if (isOutput) {
                    if (current.getDataNode() instanceof NormalDataNode || current.getDataNode() instanceof EnumDataNode
                            || (current.getDataNode() instanceof PointerDataNode &&
                            (current.getType() == CsvParameter._INPUT
                                    || (current.isReturn() && current.getParent() == null)
                                    || current.getChildren().size() > 0))) {
                      //  result += current.getNameFormat() + _COMMA;
                        allParameter.add(current);
                        outputParameter.add(current);
                        list.add(current);
                        if (current.getType() == CsvParameter._INPUT) input_num++;
                        else output_num++;
                    }
                } else {
                   // result += current.getNameFormat() + _COMMA;
                    allParameter.add(current);
                    inputParameter.add(current);
                    list.add(current);
                    if (current.getType() == CsvParameter._INPUT) input_num++;
                    else output_num++;
                }

               /* if (current.getDataNode() instanceof PointerDataNode) {
                    pointerParam.add(current);
                    continue;
                }*/
            }
            for (int i = current.getChildren().size() - 1; i >= 0; i--) {
                stack.push(current.getChildren().get(i));
            }
        }

        if(isOutput) {
          scaleOutputElement(inputParameter,outputParameter,list);
          outputParameter.clear();
        }
        for(CsvParameter parameter_: list) {
            String formatName = parameter_.getNameFormat();
            result+=formatName+_COMMA;
        }

        return result;
    }

    private String getValue_(CsvParameter parameter, int testCase) {
        String result = "";
        if (!parameter.getStubCountMap().isEmpty()) {
            result += parameter.getStubCountMap().get(testCase) == null ?
                    "" : parameter.getStubCountMap().get(testCase);
            if (result.equals("")) result = "0";
            result += _COMMA;
            return result;
        }

        CsvValueNode valueNode = parameter.getValueMap().get(testCase);
        if (valueNode != null) {
            result += valueNode.getValue() /*+ _COMMA*/;
        }
        if (result.equals("")) {
//            int n = parameter.getValueMap().size();
            for (CsvValueNode node : parameter.getValueMap().values()) {
                String s = node.getValue();
                if (!s.equals("")) {
                    result += s + _COMMA;
                    break;
                }
            }
        } else {
            result += _COMMA;
        }

        return result;
    }

    private void parseStubTestData(TestCase testCase) {
        List<SubprogramNode> allPossibleStub = Search2.searchStubableSubprograms2(testCase.getRootDataNode());
        List<SubprogramNode> selectedStub = Search2.searchStubSubprograms2(testCase.getRootDataNode());
        parseStubData(testCase, allPossibleStub, selectedStub);
    }

    private void parseStubData(TestCase testCase, List<SubprogramNode> allStub, List<SubprogramNode> selectedStub) {

        for (SubprogramNode subprogram : selectedStub) {
            if (subprogram instanceof IterationSubprogramNode) continue;
            CsvParameter parameter = new CsvParameter();
            parameter.setDataNode(subprogram);
            parameter.setId(allStub.indexOf(subprogram));
            parameter.setName(subprogram.getName());
            parameter.setStub(true);
            CsvParameter tmp = parameter;

            if (listSelectedStub.contains(parameter)) {
                tmp = listSelectedStub.get(listSelectedStub.indexOf(parameter));
            } else {
                listSelectedStub.add(parameter);
            }
            // tmp.addStubCount(listTestcase.indexOf(testCase), UtilsCsv.getStubCount(subprogram));
            List<IDataNode> numberOfCalls = parameter.getDataNode().getChildren().get(0).getChildren();

            tmp.addStubCount(listTestcase.indexOf(testCase), numberOfCalls.size());
            SubprogramNode subProgram = (SubprogramNode) parameter.getDataNode();

            tmp.setNameFormatStub((IFunctionNode) subProgram.getFunctionNode()); // idx don't work in this case
            for (int i = 0; i < numberOfCalls.size(); i++) {

                // addIteratorStub(testCase, (IFunctionNode) subProgram.getFunctionNode(), tmp, numberOfCalls.get(i), i);
                addCallStub(testCase, (IFunctionNode) subProgram.getFunctionNode(), tmp, numberOfCalls.get(i), i);

            }
        }
    }

    private void addCallStub(TestCase testCase, IFunctionNode functionNode, CsvParameter parent, IDataNode dataNode, int currentIdx) {
        CsvValueNode valueNode = new CsvValueNode(listTestcase.indexOf(testCase), (IValueDataNode) dataNode);
        CsvParameter parameter = new CsvParameter();
        parameter.setStub(true);
        parameter.addValue(valueNode);
        parameter.setParent(parent);
        parameter.setDataNode((IValueDataNode) dataNode);
        parameter.setId(currentIdx);
        parameter.setCurrentLevel(0);
        parameter.setType(CsvParameter._INPUT);
        parameter.setNameFormatStub(functionNode);

        if (!parent.getChildren().contains(parameter)) {
            parent.getChildren().add(parameter);
        } else {
            int index = parent.getChildren().indexOf(parameter);
            parameter = parent.getChildren().get(index);
            parameter.addValue(valueNode);
        }
        // if (!valueNode.getValue().equals("")) parameter.setHaveValue(true);
        for (int i = 0; i < dataNode.getChildren().size(); i++) {
            addParameterChild(testCase, functionNode, parameter, dataNode.getChildren().get(i), i);
        }
    }

    private void addIteratorStub(TestCase testCase, IFunctionNode functionNode, CsvParameter parent, IDataNode dataNode, int currentIdx) {
        CsvValueNode valueNode = new CsvValueNode(listTestcase.indexOf(testCase), (IValueDataNode) dataNode);
        CsvParameter parameter = new CsvParameter();
        parameter.addValue(valueNode);
        parameter.setParent(parent);
        parameter.setDataNode((IValueDataNode) dataNode);
        parameter.setId(currentIdx);
        if (!parameter.getDataNode().getName().contains("RETURN")) parameter.setType(CsvParameter._OUTPUT);
        parameter.setCurrentLevel(0);
        parameter.setStub(true);
        parameter.setNameFormatStub(functionNode);

        if (!parent.getChildren().contains(parameter)) {
            parent.getChildren().add(parameter);
        } else {
            int index = parent.getChildren().indexOf(parameter);
            parameter = parent.getChildren().get(index);
            parameter.addValue(valueNode);
        }
        if (!valueNode.getValue().equals("")) parameter.setHaveValue(true);

        ValueDataNode valueDataNode = (ValueDataNode) dataNode;
        List<Iterator> iterators = valueDataNode.getIterators();
        for (Iterator iterator : iterators) {
            ValueDataNode iDataNode = iterator.getDataNode();
            int startIdx = iterator.getStartIdx();
            int repeat = iterator.getRepeat();
            for (int i = repeat < 0 ? -2 : 0; i < repeat; i++) {
                CsvValueNode node = new CsvValueNode(listTestcase.indexOf(testCase), (IValueDataNode) iDataNode);
                CsvParameter param = new CsvParameter();
                param.setDataNode(iDataNode);
                param.setParent(parent);
                if (!param.getDataNode().getName().contains("RETURN")) param.setType(CsvParameter._OUTPUT);
                param.addValue(node);
                param.setCurrentLevel(0);
                param.setId(currentIdx);
                param.setStub(true);
                if (repeat == -1) {
                    if (startIdx - 1 < parameter.getIterator().size()) {
                        param = parameter.getIterator().get(startIdx - 1);
                        param.addValue(node);
                        param.setIteratorIndex(startIdx - 1);

                    } else {
                        parameter.getIterator().add(param);
                        param.setIteratorIndex(parameter.getIterator().size() - 1);
                    }
                } else {
                    if (startIdx + i <= parameter.getIterator().size()) {
                        param = parameter.getIterator().get(startIdx + i - 1);
                        param.addValue(node);
                        param.setIteratorIndex(startIdx + i - 1);
                    } else {
                        parameter.getIterator().add(param);
                        param.setIteratorIndex(parameter.getIterator().size() - 1);
                    }
                }
                if (!node.getValue().equals("")) param.setHaveValue(true);
                param.setNameFormatStub(functionNode);
                for (int j = 0; j < iDataNode.getChildren().size(); j++) {
                    addParameterChild(testCase, functionNode, param, iDataNode.getChildren().get(j), j);
                }
            }
        }
    }

    private void getCmtForStub(CsvParameter parameters) { // subprograms
      /*  for (int i = 0; i < parameters.getMaxIterator(); i++) {
            for (CsvParameter parameter : parameters.getChildren()) {
                if (parameter.getIterator().size() > i) {
                    comment += getCmt(parameter.getIterator().get(i));
                }
            }
        }*/
        for (CsvParameter callStub : parameters.getChildren()) {
            List<CsvParameter> listChild = callStub.getChildren();
            CsvParameter lastNode = listChild.get(listChild.size() - 1);
            if (lastNode.getName().equals("RETURN")) {
                listChild.remove(lastNode);
                listChild.add(0, lastNode);
            }
            for (CsvParameter child : listChild) {
                comment += getCmt(child);
            }
        }
    }

    private void parseInputTestData(TestCase testCase) {
        RootDataNode root = testCase.getRootDataNode();
        GlobalRootDataNode globalRoot = Search2.findGlobalRoot(root);
        parseGlobalInput(testCase, globalRoot);
        parseSutInput(testCase, root, null);
    }

    private void parseGlobalInput(TestCase currentTestcase, GlobalRootDataNode globalRootDataNode) {
        if (globalRootDataNode.getChildren() != null) {
            // get all used variable in sut
            List<IVariableNode> usedVars = globalRootDataNode.getRelatedVariables();

            for (int i = 0; i < globalRootDataNode.getChildren().size(); i++) {
                IDataNode child = globalRootDataNode.getChildren().get(i);
                // get corresponding variable node of current data node
                IVariableNode correspondingNode = child instanceof ValueDataNode
                        ? ((ValueDataNode) child).getCorrespondingVar() : null;

                if (correspondingNode != null && usedVars.contains(correspondingNode)) {
                    // van hien nhung bien Global input ko co gia tri
                    if (child instanceof ClassDataNode) continue;
                    isGlobalInput = true;
                    //   preOrderTravelInputGlobalVariable(currentTestcase, child, i);
                    int idParam = UtilsCsv.getGlobalParameter(currentTestcase).indexOf(child.getName());
                    preOrderVariable(currentTestcase, child, idParam, CsvParameter._INPUT);
                    isGlobalInput = false;
                }
            }
        }
    }

    private void parseSutInput(TestCase currentTestcase, IDataNode root, CsvParameter parent) {

        List<IDataNode> list = new ArrayList<>();
        IDataNode staticNode = Search2.findStaticRoot((RootDataNode) root);
        List<IDataNode> listArgNodes = Search2.findArgumentNodes((RootDataNode) root);
        list.addAll(listArgNodes);
        if (!staticNode.getChildren().isEmpty()) list.addAll(staticNode.getChildren());

        for (int i = 0; i < listArgNodes.size(); i++) {
            IDataNode child = listArgNodes.get(i);
            if (child instanceof ClassDataNode) continue;
            isArgInput = true;
            int idParam = UtilsCsv.getSubProgramParameters(currentTestcase, functionNode).indexOf(child.getName());
            preOrderVariable(currentTestcase, child, idParam, CsvParameter._INPUT);
        }

        isArgInput = false;

        for (int i = 0; i < staticNode.getChildren().size(); i++) {
            IDataNode child = staticNode.getChildren().get(i);
            if (child instanceof ClassDataNode) continue;
            int idParam = UtilsCsv.getSubProgramParameters(currentTestcase, functionNode).indexOf(child.getName());
            isStatics = true;
            preOrderVariable(currentTestcase, child, idParam, CsvParameter._INPUT);
        }
        isStatics = false;
    }


    public void addParameterChild(TestCase testCase, IFunctionNode functionNode, CsvParameter parent, IDataNode dataNode, int currentLevel) {
        if (parent == null) {
            return;
        }
        CsvParameter parameter = null;
        if (dataNode instanceof IValueDataNode) {
            int idValue = listTestcase.indexOf(testCase);
            CsvValueNode node = new CsvValueNode(idValue, (IValueDataNode) dataNode);

            parameter = new CsvParameter();
            parameter.setParent(parent);
            parameter.setDataNode((IValueDataNode) dataNode);
            parameter.setName(dataNode.getName());
            parameter.setId(parent.getId());
            parameter.setCurrentLevel(currentLevel);
            parameter.setType(parent.getType());
            parameter.addValue(node);
            parameter.setStub(parent.isStub());
            if (isArgInput) parameter.setArgInput(true);
            if (isReturnNode) {
                parameter.setReturn(true);
                if (dataNode instanceof PointerDataNode) {
                    parameter.setPointerChekNull(true);
                }
            }
            if (isStatics == true) {
                parameter.setStatics(true);
            }
            parameter.setPointerChekNull(parent.isPointerChekNull());
            if (UtilsCsv.getPointerArgCheckNullDirect(functionNode.getAST()).contains(dataNode.getName())) {
                parameter.setPointerChekNull(true);
                parameter.setDummyIndex(parent.getRootParent().getDummyIndex());
            }

            parameter.setNameFormat(functionNode);

            if (parameter.isStub()) {
                parameter.setIteratorIndex(parent.getIteratorIndex());
                parameter.setNameFormatStub(functionNode);
            }
       /*     if (parameter.isStub()) {
                if (parameter.getNameFormat().contains("@STUBRET")||parameter.getNameFormat().contains("Dummy_st_"+functionNode.getSimpleName()+"_return")) parameter.setType(CsvParameter._INPUT);
                else parameter.setType(CsvParameter._OUTPUT);
            }*/

            List<CsvParameter> childrenParameter = parent.getChildren();

            if (!childrenParameter.contains(parameter)) {
                childrenParameter.add(parameter);
                if (parameter.isPointerChekNull()) {
                    parameter.getRootParent().increaseDummyIndex();
                }
            } else {
                int index = childrenParameter.indexOf(parameter);
                parameter = childrenParameter.get(index);
                childrenParameter.get(index).addValue(node);
            }
            if (!node.getValue().equals("")) parameter.setHaveValue(true);
        }

        for (int i = 0; i < dataNode.getChildren().size(); i++) {
            if (parameter != null)
                addParameterChild(testCase, functionNode, parameter, dataNode.getChildren().get(i), i);
            else addParameterChild(testCase, functionNode, parent, dataNode.getChildren().get(i), i);
        }
    }

    private void parseOutputTestData(TestCase testCase) {
        // expected global value
        for (IValueDataNode child : testCase.getGlobalInputExpOutputMap().values()) {
            if (child.getName().contains("AKA_INSTANCE")) continue;
            if (child instanceof ClassDataNode) continue;
            int idParam = UtilsCsv.getPossibleOutput(testCase, functionNode).indexOf(child.getName());
            preOrderVariable(testCase, child, idParam, CsvParameter._OUTPUT);
        }

        // expected sut value
        SubprogramNode subprogramNode = Search2.findSubprogramUnderTest(testCase.getRootDataNode());
        if (subprogramNode != null) {
//            isArgInput = true;
            for (IDataNode child : subprogramNode.getInputToExpectedOutputMap().values()) {
                if (child instanceof ValueDataNode) {
                    ValueDataNode valueDataNode = (ValueDataNode) child;
                    if (valueDataNode.getCorrespondingVar() instanceof StaticVariableNode) {
                        isStatics = true;
                    }
                    if (valueDataNode.getCorrespondingVar() instanceof InternalVariableNode) {
                        isArgInput = true;
                    }
                }
                if (child instanceof ClassDataNode) continue;
                int idParam = UtilsCsv.getPossibleOutput(testCase, functionNode).indexOf(child.getName());
                preOrderVariable(testCase, child, idParam, CsvParameter._OUTPUT);
                isStatics = false;
                isArgInput = false;
            }

            // return var
            if (!VariableTypeUtils.isVoid(functionNode.getReturnType())) {
                int length = subprogramNode.getChildren().size();
                IDataNode returnNode = subprogramNode.getChildren().get(length - 1);
                if (!(returnNode instanceof ClassDataNode)) {
                    isReturnNode = true;
                    preOrderVariable(testCase, returnNode, 0, CsvParameter._OUTPUT);
                    isReturnNode = false;
                }
            }
        }
    }


    public void resetModLine() {
//        "mod,func,func,input_num,output_num,,,,type";
        modLine = "mod" + _COMMA + functionNode.getSimpleName() + _COMMA + functionNode.getSimpleName()
                + _COMMA + input_num + _COMMA + output_num + _COMMA
                + _COMMA + _COMMA + _COMMA + functionLanguage + _COMMA;
    }

    private CsvParameter addParameterNode(TestCase currentTestcase, CsvParameter parameter, CsvValueNode node, IDataNode dataNode) {
        if (parameter.getType() == CsvParameter._OUTPUT) {
            List<String> argumentsExpected = UtilsCsv.getPossibleOutput(currentTestcase, functionNode);
            String nameNode = dataNode.getName();
            if (isReturnNode) nameNode = functionNode.getSimpleName() + "@@";
            if (argumentsExpected.contains(nameNode)) {
                int id = argumentsExpected.indexOf(nameNode);
                if (!listOutputParameter.contains(parameter)) {
                    parameter.setId(id);
                    listOutputParameter.add(parameter);
                    if (!node.getValue().equals("")) parameter.setHaveValue(true);
                    return parameter;
                } else {
                    int index = listOutputParameter.indexOf(parameter);
                    if (dataNode.getChildren().isEmpty() || (dataNode instanceof PointerDataNode)) {
                        parameter = listOutputParameter.get(index);
                        parameter.addValue(node);
                        if (!node.getValue().equals("")) parameter.setHaveValue(true);
                    }
                    return listOutputParameter.get(index);
                }
            }
        } else {
            if (isGlobalInput) {
                if (!listInputGlobal.contains(parameter)) {
                    listInputGlobal.add(parameter);
                    if (!node.getValue().equals("")) parameter.setHaveValue(true);
                    return parameter;
                } else {
                    int index = listInputGlobal.indexOf(parameter);
                    if (dataNode.getChildren().isEmpty() || (dataNode instanceof PointerDataNode)) {
                        parameter = listInputGlobal.get(index);
                        parameter.addValue(node);
                        if (!node.getValue().equals("")) parameter.setHaveValue(true);
                    }
                    return listInputGlobal.get(index);
                }
            } else {
                if (!listParameter.contains(parameter)) {
                    listParameter.add(parameter);
                    if (!node.getValue().equals("")) parameter.setHaveValue(true);
                    return parameter;
                } else {
                    int index = listParameter.indexOf(parameter);
                    if (dataNode.getChildren().isEmpty() || (dataNode instanceof PointerDataNode)) {
                        parameter = listParameter.get(index);
                        parameter.addValue(node);
                        if (!node.getValue().equals("")) parameter.setHaveValue(true);
                    }
                    return listParameter.get(index);
                }
            }
        }

        return null;
    }

    public static LoadingPopupController getPopup() {
        LoadingPopupController loadPopup = LoadingPopupController.newInstance("Generating Test Report in CSV");
        loadPopup.setText("Generating...");
        loadPopup.initOwnerStage(UIController.getPrimaryStage());
        return loadPopup;
    }

    private void preOrderVariable(TestCase currentTestcase, IDataNode dataNode, int idParam, int type) {

        int id = listTestcase.indexOf(currentTestcase);
        boolean isDummy = false;
        CsvValueNode node = new CsvValueNode(id, (IValueDataNode) dataNode);
        CsvParameter parameter = new CsvParameter();

        parameter.setDataNode((IValueDataNode) dataNode);
        if (isReturnNode) {
            parameter.setName(functionNode.getSimpleName() + "@@");
            parameter.setReturn(true);
            if (dataNode instanceof PointerDataNode) {
                parameter.setPointerChekNull(true);
            }
        } else parameter.setName(dataNode.getName());
        parameter.setId(idParam);
        parameter.addValue(node);
        if (UtilsCsv.getPointerArgCheckNullDirect(functionNode.getAST()).contains(dataNode.getName())) {
            parameter.setPointerChekNull(true);
            isDummy = true;
            parameter.setDummyIndex(1);
        }
        if (type == CsvParameter._OUTPUT) parameter.setType(CsvParameter._OUTPUT);
        if (isArgInput) parameter.setArgInput(true);
        if (isStatics == true) {
            parameter.setStatics(true);
        }
        parameter.setNameFormat(functionNode);
        parameter = addParameterNode(currentTestcase, parameter, node, dataNode);

        for (int i = 0; i < dataNode.getChildren().size(); i++) {
            addParameterChild(currentTestcase, functionNode, parameter, dataNode.getChildren().get(i), i);
        }
    }

    private void reformatReport(List<CsvParameter> listParameter) {
        for (CsvParameter parameter : listParameter) {
//        CsvValueNode longestSizeValueNode = null;
            for (CsvValueNode valueNode : parameter.getValueMap().values()) {
                if (valueNode.getValue().equals("")) {

                }
//                IValueDataNode data = valueNode.getData();
//                IValueDataNode dataNode = valueNode.getData();
//                if (dataNode != null) {
//                    if (dataNode instanceof NormalDataNode ) return ((NormalDataNode) dataNode).getValue();
//                    else if (dataNode instanceof EnumDataNode) return ((EnumDataNode) dataNode).getValue();
//                else if (dataNode instanceof PointerDataNode
//                        && dataNode.getChildren().size() > data.getChildren().size()) {
//                    data = dataNode;
//                }
//                }
            }
        }
    }

    public boolean isDisplayedParameter(CsvParameter parameter) {
        List<IASTNode> listASTUsedParameter = UtilsCsv.getExpressionNode(functionNode.getAST());
        String paramName = parameter.getName();
        if (parameter.getParent() == null) {
            return true;
        }
        if (parameter.isReturn()) return true;
        if (parameter.isStatics()) return true;
        if (parameter.isPointerParameter()) {
            if (parameter.isFirstChildOfPointer()) {
                return true;
            }
            if (parameter.getRootPointerNode() != null) {
                return false;
            }
        }
        for (IASTNode child : listASTUsedParameter) {
            if (child.getRawSignature().equals(paramName)) {
                return true;
            }
        }
        return false;
    }

    public String generateStubCContentFile() {
        String content = "";
        for (CsvParameter parameter : allParameter) {
            if (parameter.isPointerParameter()) {
                listDummyParamToStubC.add(parameter);
                content += parameter.getContentFormatIntoStubCFile() + "\n";
            }
        }
        return content;
    }

}
