package com.dse.parser.normalizer;

import com.dse.parser.object.IFunctionNode;

/**
 * Remove break line in function
 *
 * @author ducanhnguyen
 */
public class BreakLineNormalizer extends AbstractFunctionNormalizer implements IFunctionNormalizer {

    public BreakLineNormalizer() {
    }

    public BreakLineNormalizer(IFunctionNode functionNode) {
        this.functionNode = functionNode;
    }

    @Override
    public void normalize() {
        normalizeSourcecode = functionNode.getAST().getRawSignature().replace("\n", "").replace("\r", "");
    }

}
