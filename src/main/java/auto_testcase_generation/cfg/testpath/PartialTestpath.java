package auto_testcase_generation.cfg.testpath;

import auto_testcase_generation.cfg.object.ConditionCfgNode;
import auto_testcase_generation.cfg.object.ICfgNode;

/**
 * Represent partial test path
 *
 * @author ducanhnguyen
 */
public class PartialTestpath extends AbstractTestpath implements IPartialTestpath {
	/**
	 *
	 */
	private static final long serialVersionUID = 2276531353820115816L;
	private boolean finalConditionType;

	@Override
	public String getFullPath() {
		StringBuilder output = new StringBuilder();
		for (int i = 0; i < size() - 1; i++) {
			ICfgNode n = get(i);
			if (n instanceof ConditionCfgNode)
				if (nextIsTrueBranch(n, i))
					output.append("(").append(n.getContent()).append(") ").append(ITestpathInCFG.SEPARATE_BETWEEN_NODES).append(" ");
				else
					output.append("!(").append(n.getContent()).append(") ").append(ITestpathInCFG.SEPARATE_BETWEEN_NODES).append(" ");
			else
				output.append(n.getContent()).append(ITestpathInCFG.SEPARATE_BETWEEN_NODES).append(" ");
		}
		if (finalConditionType)
			output.append(get(size() - 1));
		else
			output.append("!(").append(get(size() - 1)).append(")");
		return output.toString();
	}

	@Override
	public boolean getFinalConditionType() {
		return finalConditionType;
	}

	@Override
	public void setFinalConditionType(boolean finalConditionType) {
		this.finalConditionType = finalConditionType;
	}

	@Override
	public PartialTestpath cast() {
		return this;
	}
}
