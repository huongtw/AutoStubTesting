package auto_testcase_generation.cfg.object;

/**
 * Represent the end node of CFG
 *
 * @author ducanh
 */
public class EndFlagCfgNode extends FlagCfgNode {

	private BeginFlagCfgNode beginNode;

	public static final String END_FLAG = "End";

	public EndFlagCfgNode() {
		setContent(EndFlagCfgNode.END_FLAG);
	}

	public BeginFlagCfgNode getBeginNode() {
		return beginNode;
	}

	public void setBeginNode(BeginFlagCfgNode beginNode) {
		this.beginNode = beginNode;
	}
}
