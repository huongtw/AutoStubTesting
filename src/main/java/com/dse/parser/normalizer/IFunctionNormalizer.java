package com.dse.parser.normalizer;

import com.dse.parser.object.IFunctionNode;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;

/**
 * Normalize function
 *
 * @author ducanhnguyen
 */
public interface IFunctionNormalizer extends ISourceCodeNormalizer {
    /**
     * Get the normalized AST
     *
     * @return
     */
    IASTFunctionDefinition getNormalizedAST();

    /**
     * Get the function node
     *
     * @return
     */
    IFunctionNode getFunctionNode();

    /**
     * Set the function node need to be normalized
     *
     * @param functionNode
     */
    void setFunctionNode(IFunctionNode functionNode);
}
