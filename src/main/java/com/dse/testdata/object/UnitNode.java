package com.dse.testdata.object;

import com.dse.parser.object.INode;

public abstract class UnitNode extends DataNode {
    private INode sourceNode;

//    private boolean stubChildren = true;

    public UnitNode() {

    }

    public UnitNode(INode source) {
        setSourceNode(source);
    }

    public void setSourceNode(INode sourceNode) {
        if (sourceNode == null)
            return;

        this.sourceNode = sourceNode;

        String fileName = sourceNode.getName();
        setName(fileName);
    }

    public INode getSourceNode() {
        return sourceNode;
    }

    @Override
    public String getDisplayNameInParameterTree() {
        return getName();
    }

    @Override
    public String generateInputToSavedInFile() throws Exception {
        return super.generateInputToSavedInFile();
    }

//    public void setStubChildren(boolean stubChildren) {
//        this.stubChildren = stubChildren;
//    }
//
//    public boolean isStubChildren() {
//        return stubChildren;
//    }
}
