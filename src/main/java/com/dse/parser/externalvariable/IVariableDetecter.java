package com.dse.parser.externalvariable;

import com.dse.parser.object.IFunctionNode;
import com.dse.parser.object.IVariableNode;
import com.dse.search.ISearch;

import java.util.List;

/**
 * Find all external variables of a function
 *
 * @author ducanhnguyen
 */
public interface IVariableDetecter extends ISearch {
    /**
     * Find external variables of a function
     *
     * @return
     */
    List<IVariableNode> findVariables();

    IFunctionNode getFunction();

    void setFunction(IFunctionNode function);
}