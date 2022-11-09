package com.dse.testdata.gen.module.subtree;

import com.dse.parser.funcdetail.IFunctionDetailTree;
import com.dse.parser.object.VariableNode;
import com.dse.testdata.object.DataNode;
import com.dse.testdata.object.RootDataNode;
import com.dse.testdata.object.ValueDataNode;

/**
 * Xay dung nhanh (sub tree).
 * Ex: GLOBAL, UUT or STUB.
 *
 * @author TungLam
 */
public interface IInitialSubTreeGen {

    /**
     * Generate a complete data tree given a function
     * @param root
     * @param functionTree
     * @throws Exception
     */
    void generateCompleteTree(RootDataNode root, IFunctionDetailTree functionTree) throws Exception;

    /**
     * Generate a partial data tree given a variable
     * @param vCurrentChild
     * @param nCurrentParent
     * @throws Exception
     */
    ValueDataNode genInitialTree(VariableNode vCurrentChild, DataNode nCurrentParent) throws Exception;

//    /**
//     * Set the vitural name of node
//     *
//     * @param n
//     */
//    void setVituralName(IDataNode n);
}
