package com.dse.testdata;

import com.dse.parser.object.ICommonFunctionNode;
import com.dse.testdata.object.IDataNode;
import com.dse.testdata.object.RootDataNode;
import com.dse.testdata.object.ValueDataNode;
import com.dse.util.NodeType;

public interface IDataTree {
    /**
     * Get the corresponding function
     *
     * @return
     */
    ICommonFunctionNode getFunctionNode();

    /**
     * Get the root of the tree
     *
     * @return
     */
    RootDataNode getRoot();

    void setRoot(RootDataNode root);

    void setFunctionNode(ICommonFunctionNode fn);

    /**
     * Get the root of the subtree
     * Ex: GLOBAL, UUT, STUB
     *
     * @param level type of the root
     * @return
     */
    RootDataNode getSubTreeRoot(NodeType level);

    /**
     * Get the root (ex: GLOBAL, UUT, STUB,...) of the
     * @param node
     * @return
     */
    RootDataNode getSubTreeRoot(IDataNode node);

    /**
     * Expand the node to get its elements
     *
     * @param node
     * @throws Exception
     */
    void expand(ValueDataNode node) throws Exception;

    /**
     * Expand the union attribute node
     *
     * @param node
     * @param name of the attribute
     * @throws Exception
     */
    void expand(ValueDataNode node, String name) throws Exception;
}
