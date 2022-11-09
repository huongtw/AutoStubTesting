package com.dse.testdata.gen.module;

import com.dse.parser.object.ICommonFunctionNode;
import com.dse.testdata.object.IDataNode;
import com.dse.testdata.object.RootDataNode;

import java.util.Map;

/**
 * A tree represent value of variables
 *
 * @author DucAnh
 */
public interface IDataTreeGeneration extends IGeneration {

	/**
	 * Generate tree of variables
	 *
	 * @throws Exception
	 */
	void generateTree() throws Exception;

	/**
	 * Get corresponding function call
	 *
	 * @return
	 * @throws Exception
	 */
	String getFunctionCall(ICommonFunctionNode functionNode) throws Exception;

	/**
	 * Get the corresponding function
	 *
	 * @return
	 */
	ICommonFunctionNode getFunctionNode();

	/**
	 * Set function node
	 *
	 * @param functionNode
	 */
	void setFunctionNode(ICommonFunctionNode functionNode);

	/**
	 * Get input for display
	 *
	 * @return
	 */
	String getInputforDisplay();

	/**
	 * Get input for google test
	 *
	 * @return
	 */
	String getInputforGoogleTest();

	/**
	 * Get input from file
	 *
	 * @return
	 */
	String getInputformFile();

	String getInputSavedInFile();

	/**
	 * Get static solution
	 *
	 * @return
	 */
	Map<String, String> getValues();

	void setValues(Map<String, String> values);

	void setRoot(RootDataNode root);

	RootDataNode getRoot();

	void setVituralName(IDataNode n);

//	DataNode getExpectedOutputNode();
}