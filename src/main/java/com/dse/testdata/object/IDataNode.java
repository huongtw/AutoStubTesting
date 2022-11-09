package com.dse.testdata.object;

import com.dse.parser.object.IVariableNode;
import com.dse.parser.object.VariableNode;

import java.util.List;

public interface IDataNode {
	/**
	 * The access operator to access element in class/struct
	 */
	String DOT_ACCESS = ".";
	String GETTER_METHOD = IDataNode.DOT_ACCESS + "get";
	String NULL_POINTER_IN_CPP = "nullptr";
	String NULL_POINTER_IN_C = "NULL";
	String ONE_LEVEL_POINTER_OPERATOR = "*";
	String REFERENCE_OPERATOR = "&";
	String SETTER_METHOD = IDataNode.DOT_ACCESS + "set";

	void addChild(IDataNode newChild);

	List<IDataNode> getChildren();

	void setChildren(List<IDataNode> children);

	/**
	 * Get the string used to display in GUI
	 *
	 * @return
	 * @throws Exception
	 */
	String getInputForDisplay() throws Exception;

	/**
	 * Get the string used to put in google test file
	 *
	 * @return
	 * @throws Exception
	 */
	String getInputForGoogleTest(boolean isDeclared) throws Exception;

	String generareSourcecodetoReadInputFromFile() throws Exception;

	String generateInputToSavedInFile() throws Exception;

	String getName();

	String getDisplayNameInParameterTree();

	void setName(String name);

	/**
	 * Get all node from the root node to the current node
	 *
	 * @param n
	 *            the current node
	 * @return a list of nodes from the root node
	 */
	List<IDataNode> getNodesChainFromRoot(IDataNode n);

	IDataNode getParent();

	void setParent(IDataNode parent);

	String getVituralName();

	void setVituralName(String vituralName);

	void setVirtualName();

	String getPathFromRoot();

	@Override
	String toString();

	IDataNode getRoot();

	IDataNode getUnit();
}