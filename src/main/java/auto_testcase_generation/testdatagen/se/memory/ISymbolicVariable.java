package auto_testcase_generation.testdatagen.se.memory;

import java.util.List;

import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.INode;

/**
 * This interface represents a node in the symbolic variables tree
 * 
 * @author ducanhnguyen
 *
 */
public interface ISymbolicVariable extends Comparable<ISymbolicVariable> {

	// Each default value of a symbolic variable should be started with a unique
	// string
	// For example, we have "int a", the default value of "a" is "tvwa", or
	// something else.
	String PREFIX_SYMBOLIC_VALUE = "";//""____aka____";// default

	// This is separator between structure name and its attributes.
	// For example, we have "a.age", its default value may be "tvwa_______age"
	String SEPARATOR_BETWEEN_STRUCTURE_NAME_AND_ITS_ATTRIBUTES = "___attr___";// default
	String SEPARATOR_BETWEEN_STRUCTURE_POINTER_NAME_AND_ITS_ATTRIBUTES = "____arrbegin____0____arrend_______attr___";// default

	String ARRAY_OPENING = "____arrbegin____";// default
	String ARRAY_CLOSING = "____arrend____";// default

	// Unspecified scope
	int UNSPECIFIED_SCOPE = -1;
	int GLOBAL_SCOPE = 0;

	/**
	 * Check whether the variable is basic type (not pointer, not array, not
	 * structure, not enum, etc.), only number or char.
	 *
	 * @return
	 */
	boolean isBasicType();

	/**
	 * Get the name of symbolic variable
	 * 
	 * @return
	 */
	String getName();

	/**
	 * Set the name for the symbolic variable
	 * 
	 * @param name
	 */
	void setName(String name);

	/**
	 * Get type of symbolic variable, e.g., int, int*, float
	 * 
	 * @return
	 */
	String getType();

	/**
	 * Set type for the symbolic variable
	 * 
	 * @param type
	 */
	void setType(String type);

//	INode getContext();
//
//	void setContext(INode context);

	/**
	 * Get scope level of the symbolic variable. If the symbolic variable scope is
	 * global, its value is equivalent to GLOBAL_SCOPE
	 * 
	 * @return
	 */
	int getScopeLevel();

	/**
	 * Set the scope level for the symbolic variable
	 * 
	 * @param scopeLevel
	 */
	void setScopeLevel(int scopeLevel);

	/**
	 * Get the corresponding node. For example, if the symbolic variable is "Student
	 * x", its node is the definition of "Student". That is: <b>class Student{ ...
	 * }</b>
	 * 
	 * @return
	 */
	INode getNode();

	/**
	 * Set the corresponding node
	 * 
	 * @param node
	 */
	void setNode(INode node);

	/**
	 * Get all physical cells stored by symbolic variable. For example, consider
	 * symbolic variable x <br/>
	 * If x is basic type, it has one physical cell. <br/>
	 * If x is one dimension array, it has a list of physical cells. Each physical
	 * cell represents an array item
	 * 
	 * @return
	 */
	List<PhysicalCell> getAllPhysicalCells();

	ICommonFunctionNode getFunction();

	void setFunction(ICommonFunctionNode function);

	boolean assign(ISymbolicVariable other);
}