package com.dse.testdata.object;

import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.IFunctionNode;
import com.dse.testdata.object.DataNode;
import com.dse.testdata.object.IDataNode;
import com.dse.testdata.object.ValueDataNode;
import com.dse.util.NodeType;

import java.util.HashMap;
import java.util.Map;

import static com.dse.util.NodeType.ROOT;

/**
 * Represent the root of the variable tree
 *
 * @author lamnt
 */
public class RootDataNode extends DataNode {

	protected ICommonFunctionNode functionNode;
	protected NodeType level;
//	private String testcaseName;

//	// map input to expected output of global variables
//    // only used for GLOBAL level
//	private Map<ValueDataNode, ValueDataNode> globalInputExpOutputMap = null;

	public RootDataNode() {
		level = ROOT;
	}

	public RootDataNode(NodeType type) {
		this.level = type;
//		if (type.equals(NodeType.GLOBAL)) {
//			globalInputExpOutputMap = new HashMap<>();
//		}
	}

	public ICommonFunctionNode getFunctionNode() {
		return this.functionNode;
	}

	public void setFunctionNode(ICommonFunctionNode functionNode) {
		this.functionNode = functionNode;
	}

	@Override
	public String getName() {
		return level.toString();
//		return UtilsVu.getTypeRoot(level);
	}

	public NodeType getLevel() {
		return level;
	}

	public void setLevel(NodeType level) {
		this.level = level;
	}

//	public String getTestcaseName() {
//		return testcaseName;
//	}
//
//	public void setTestcaseName(String testcaseName) {
//		this.testcaseName = testcaseName;
//	}

	public String getDisplayNameInParameterTree() {
		return String.format("<<%s>>", level.toString());
	}

//	public Map<ValueDataNode, ValueDataNode> getGlobalInputExpOutputMap() {
//		return globalInputExpOutputMap;
//	}
//
//	public void setGlobalInputExpOutputMap(Map<ValueDataNode, ValueDataNode> globalInputExpOutputMap) {
//		this.globalInputExpOutputMap = globalInputExpOutputMap;
//	}
//
//	public boolean putGlobalExpectedOutput(ValueDataNode expectedOuput) {
//		ValueDataNode input = null;
//		for (IDataNode child : getChildren()) {
//			if (((ValueDataNode) child).getCorrespondingVar().getAbsolutePath().equals(expectedOuput.getCorrespondingVar().getAbsolutePath())) {
//				input = (ValueDataNode) child;
//				break;
//			}
//		}
//
//		if (input != null) {
//			if (globalInputExpOutputMap.containsKey(input)) {
//				globalInputExpOutputMap.remove(input);
//			}
//			globalInputExpOutputMap.put(input, expectedOuput);
//			return true;
//		}
//
//		return false;
//	}
}
