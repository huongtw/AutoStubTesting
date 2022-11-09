package com.dse.testcasescript.object;

import com.dse.parser.object.IFunctionNode;

public class TestNormalSubprogramNode extends TestSubprogramNode {
    private IFunctionNode functionNode;

    public IFunctionNode getFunctionNode() {
        return functionNode;
    }

    public void setFunctionNode(IFunctionNode functionNode) {
        this.functionNode = functionNode;
    }

}
