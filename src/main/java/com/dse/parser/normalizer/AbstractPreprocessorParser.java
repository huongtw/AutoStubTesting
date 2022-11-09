package com.dse.parser.normalizer;

import com.dse.parser.object.IFunctionNode;

/**
 * Abstract class for parsing preprocessor
 *
 * @author ducanhnguyen
 */
public abstract class AbstractPreprocessorParser extends AbstractParser {
    protected IFunctionNode functionNode;

    public IFunctionNode getFunctionNode() {
        return functionNode;
    }

    public void setFunctionNode(IFunctionNode functionNode) {
        this.functionNode = functionNode;
    }
}
