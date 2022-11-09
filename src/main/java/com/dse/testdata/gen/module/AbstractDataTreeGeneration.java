package com.dse.testdata.gen.module;

import com.dse.parser.object.*;
import com.dse.testdata.object.DataNode;
import com.dse.testdata.object.IDataNode;
import com.dse.testdata.object.RootDataNode;
import com.dse.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractDataTreeGeneration implements IDataTreeGeneration {
    protected ICommonFunctionNode functionNode;
    protected RootDataNode root = new RootDataNode();
    protected Map<String, String> values = new HashMap<>();

    @Override
    public String getFunctionCall(ICommonFunctionNode functionNode) throws Exception {
        return Utils.getFullFunctionCall(functionNode);
    }

    public int START_VIRTUAL_VARIABLE = 97; //'a'
    public int END_VIRTUAL_VARIABLE = 122; //'z'
    public int currentVirturalIndex = START_VIRTUAL_VARIABLE;
    public List<String> avoidingNames = new ArrayList<>();
    /**
     * Set virtual for nodes in the tree.
     *
     * @param n data node
     */
    public void setVituralName(IDataNode n) {
        if (n == null)
            return;
        else
            ((DataNode) n).setVirtualName();

        if (n.getChildren() != null)
            for (IDataNode child : n.getChildren())
                this.setVituralName(child);
    }
//
//    private String chooseNewVirtualName(int currentVirturalIndex, List<String> avoidingNames) {
//        String str = (char) currentVirturalIndex + "";
//        while (avoidingNames.contains(str)) {
//            currentVirturalIndex += 1;
//            str = (char) currentVirturalIndex + "";
//        }
//        str = "aka_" + str;
//        return str;
//    }

    @Override
    public String getInputformFile() {
        try {
            String input = root.generareSourcecodetoReadInputFromFile();
            return input;
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public String getInputSavedInFile() {
        try {
            String input = root.generateInputToSavedInFile();
            return input;
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public String getInputforDisplay() {
        try {
            String input = root.getInputForDisplay();
            return input;
        } catch (Exception e) {

            return "";
        }
    }

    @Override
    public String getInputforGoogleTest() {
        try {
            return getRoot().getInputForGoogleTest(false);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public ICommonFunctionNode getFunctionNode() {
        return functionNode;
    }

    @Override
    public void setFunctionNode(ICommonFunctionNode functionNode) {
        this.functionNode = functionNode;
    }

    @Override
    public void setRoot(RootDataNode root) {
        this.root = root;
    }

    @Override
    public RootDataNode getRoot() {
        return root;
    }

    public Map<String, String> getValues() {
        return values;
    }

    public void setValues(Map<String, String> values) {
        this.values = values;
    }
}
