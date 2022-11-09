package auto_testcase_generation.cfg.object;

/**
 * Represent the function call flag node of CFG
 *
 * @author lamnt
 */
public class EndFunctionCallFlagCfgNode extends FunctionCallFlagCfgNode {

	private BeginFunctionCallFlagCfgNode beginNode;

	public EndFunctionCallFlagCfgNode() {
		setContent(EndFlagCfgNode.END_FLAG);
	}

	public BeginFunctionCallFlagCfgNode getBeginNode() {
		return beginNode;
	}

	public void setBeginNode(BeginFunctionCallFlagCfgNode beginNode) {
		this.beginNode = beginNode;
	}
}
