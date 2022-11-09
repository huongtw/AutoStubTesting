package com.dse.testdata.object;

import com.dse.environment.Environment;
import com.dse.parser.object.*;
import com.dse.parser.object.INode;
import com.dse.search.Search2;
import com.dse.testcase_execution.DriverConstant;
import com.dse.testcase_manager.TestPrototype;
import com.dse.testdata.gen.module.subtree.InitialArgTreeGen;
import com.dse.util.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class SubprogramNode extends ValueDataNode {

    protected INode functionNode;
//    private int called = 0;

    Map<ValueDataNode, ValueDataNode> inputToExpectedOutputMap = new HashMap<>();

    public SubprogramNode() {

    }

    public Map<ValueDataNode, ValueDataNode> getInputToExpectedOutputMap() {
        return inputToExpectedOutputMap;
    }

//    public void setCalled(int called) {
//        this.called = called;
//        return;
//    }
//
//    public void addCalled(int calls) {
//        this.called += calls;
//        return;
//    }
//
//    public int getCalled() {
//        return this.called;
//    }

    public SubprogramNode(INode fn) {
        setFunctionNode(fn);
    }

    public void initInputToExpectedOutputMap() {
        inputToExpectedOutputMap.clear();

        /*
          create initial tree
         */
        try {
            ICommonFunctionNode castFunctionNode = (ICommonFunctionNode) functionNode;
            RootDataNode root = new RootDataNode();
            root.setFunctionNode(castFunctionNode);
            InitialArgTreeGen dataTreeGen = new InitialArgTreeGen();
            List<ValueDataNode> actualNodes = Search2.searchParameterNodes(this);
            for (INode child : castFunctionNode.getChildren()) {
                if (child instanceof VariableNode) {
                    ValueDataNode expected = dataTreeGen.genInitialTree((VariableNode) child, root);
                    for (ValueDataNode actual : actualNodes) {
                        if (actual.getCorrespondingVar() == expected.getCorrespondingVar()) {
                            inputToExpectedOutputMap.put(actual, expected);
                            expected.setParent(actual.getParent());
                            expected.setExternel(false);
                            actualNodes.remove(actual);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initInputToExpectedOutputMapWithProtoType(String testCaseJsonFile) {
        inputToExpectedOutputMap.clear();
        try {
            if (new File(testCaseJsonFile).exists()) {
                String json = Utils.readFileContent(testCaseJsonFile);
                JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
                JsonObject firstChildren = jsonObject.getAsJsonObject("rootDataNode").getAsJsonArray("children").get(0).getAsJsonObject();
                String templateInf = firstChildren.getAsJsonArray("children").get(1).getAsJsonObject().getAsJsonArray("template_type").getAsString();
                String newType = templateInf.substring(3);
                ICommonFunctionNode castFunctionNode = (ICommonFunctionNode) functionNode;
                RootDataNode root = new RootDataNode();
                root.setFunctionNode(castFunctionNode);
                InitialArgTreeGen dataTreeGen = new InitialArgTreeGen();
                List<ValueDataNode> actualNodes = Search2.searchParameterNodes(this);
                for (INode child : castFunctionNode.getChildren()) {
                    if (child instanceof VariableNode) {
                        VariableNode schild = (VariableNode) ((VariableNode) child).clone();
                        schild.setCoreType( schild.getCoreType().replace("T", newType));
                        schild.setRawType(schild.getRawType().replace("T", newType));
                        schild.setReducedRawType(schild.getReducedRawType().replace("T", newType));
                        ValueDataNode expected = dataTreeGen.genInitialTree(schild, root);

                        for (ValueDataNode actual : actualNodes) {
                            if (actual.getName().equals(expected.getName())) {
                                expected.setRawType(expected.getRawType().replace("T", newType));
                                expected.setRealType(expected.getRealType().replace("T", newType));
//                                expected.getCorrespondingVar().setCoreType( expected.getCorrespondingVar().getCoreType().replace("T", newType));
//                                expected.getCorrespondingVar().setRawType(expected.getCorrespondingVar().getRawType().replace("T", newType));
//                                expected.getCorrespondingVar().setReducedRawType(expected.getCorrespondingVar().getReducedRawType().replace("T", newType));
                                inputToExpectedOutputMap.put(actual, expected);
                                expected.setParent(actual.getParent());
                                actualNodes.remove(actual);
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Collection<ValueDataNode> getParamExpectedOuputs() {
        return inputToExpectedOutputMap.values();
    }

    public boolean putParamExpectedOutputs(ValueDataNode expectedOuput) {
        if (expectedOuput.getName().equals("RETURN"))
            return false;

        List<ValueDataNode> parameterNodes = Search2.searchParameterNodes(this);
        ValueDataNode input = parameterNodes.stream()
                .filter(child -> child.getCorrespondingVar() == expectedOuput.getCorrespondingVar())
                .findFirst()
                .orElse(null);

        if (input != null) {
            inputToExpectedOutputMap.remove(input);
            inputToExpectedOutputMap.put(input, expectedOuput);
            return true;
        }

        return false;
    }

    public boolean checkIsValidParamExpectedOuputs() {
        for (IDataNode input : getChildren()) {
            if (! input.getName().equals("RETURN")) {
                ValueDataNode eo = getExpectedOuput((ValueDataNode) input);
                if (eo == null) return false;
            }
        }

        return true;
    }

    public INode getFunctionNode() {
        return functionNode;
    }

    public void setFunctionNode(INode functionNode) {
        this.functionNode = functionNode;

        if (functionNode == null)
            return;

        setName(functionNode.getName());

        String type = ((ICommonFunctionNode) functionNode).getReturnType();
        String realType = type;
        VariableNode returnVarNode = Search2.getReturnVarNode((ICommonFunctionNode) functionNode);

        if (returnVarNode != null) {
            setCorrespondingVar(returnVarNode);
            type = returnVarNode.getRawType();
            realType = returnVarNode.getRealType();
        }

        type = VariableTypeUtils.deleteStorageClassesExceptConst(type);
        type = VariableTypeUtils.deleteVirtualAndInlineKeyword(type);

        realType = VariableTypeUtils.deleteStorageClassesExceptConst(realType);
        realType = VariableTypeUtils.deleteVirtualAndInlineKeyword(realType);

        setRawType(type);
        setRealType(realType);
    }

    public ValueDataNode getExpectedOuput(ValueDataNode inputValue) {
        ValueDataNode eo = inputToExpectedOutputMap.get(inputValue);
        return eo;
    }

    @Override
    public String getSetterInStr(String nameVar) {
        return "(no setter)";
    }

    @Override
    public String getGetterInStr() {
        return "(no getter)";
    }

    public String getDisplayNameInParameterTree() {
        String prefixPath = "";

        INode originalNode = getFunctionNode();

        if (originalNode instanceof ICommonFunctionNode) {
            prefixPath = ((ICommonFunctionNode) originalNode).getSingleSimpleName();

            if (isLibrary())
                return prefixPath.replace(SourceConstant.STUB_PREFIX, SpecialCharacter.EMPTY);

            INode currentNode = originalNode.getParent();

            if (originalNode instanceof AbstractFunctionNode) {
                INode realParent = ((AbstractFunctionNode) originalNode).getRealParent();
                if (realParent != null)
                    currentNode = realParent;
            }

            while ((currentNode instanceof StructureNode || currentNode instanceof NamespaceNode)) {
                prefixPath = currentNode.getNewType() + SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS + prefixPath;
                currentNode = currentNode.getParent();
            }
        }

        return prefixPath;
    }

    private boolean isLibrary() {
        if (functionNode == null)
            return false;

        if (Environment.getInstance().getSystemLibraryRoot() == null)
            return false;

        INode root = functionNode.getParent();

        if (root == null)
            return false;

        root = root.getParent();

        return Environment.getInstance().getSystemLibraryRoot().equals(root);
    }

    @Override
    public String getDotGetterInStr() {
        return "(no getter)";
    }

    @Override
    public String generateInputToSavedInFile() throws Exception {
        return super.generateInputToSavedInFile();
    }

    public boolean isStubable() {
        if (this instanceof ConstructorDataNode)
            return false;

        if (getRoot() instanceof RootDataNode) {
            NodeType type = ((RootDataNode) getRoot()).getLevel();

            if (type == NodeType.STUB || type == NodeType.SBF)
                return true;
        }

        if (getParent() instanceof UnitNode) {
            UnitNode unit = getUnit();

            return unit instanceof StubUnitNode;
        }

        return false;
    }

    public boolean isStub() {
        return !getChildren().isEmpty();
    }
}
