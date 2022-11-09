package auto_testcase_generation.cfg.testpath;

import java.util.ArrayList;
import java.util.List;

import auto_testcase_generation.cfg.ICFG;
import auto_testcase_generation.cfg.object.ConditionCfgNode;
import auto_testcase_generation.cfg.object.ICfgNode;

/**
 * Represent full test path from the beginning node to the end node
 *
 * @author ducanhnguyen
 */
public class FullTestpath extends AbstractTestpath implements IFullTestpath {

	/**
	 *
	 */
	private static final long serialVersionUID = 3205932220413141035L;

	@Override
	public IPartialTestpath getPartialTestpathAt(int endConditionId, boolean finalConditionType) {
		IPartialTestpath tp = new PartialTestpath();
		tp.setFunctionNode(getFunctionNode());

		if (endConditionId < getNumConditionsIncludingNegativeConditon()) {
			int numVisitedCondition = 0;

			for (ICfgNode node : this) {
				tp.cast().add(node);
				if (node instanceof ConditionCfgNode) {
					numVisitedCondition++;
					if (numVisitedCondition >= endConditionId + 1)
						break;
				}
			}
			tp.setFinalConditionType(finalConditionType);
			return tp;
		} else
			return tp;
	}

	@Override
	public int getNumUnvisitedStatements(ICFG cfg) {
		int numUnvisitedStatements = 0;

		List<ICfgNode> unvisitedNodes = cfg.getUnvisitedStatements();

		List<Integer> unvisitedIds = new ArrayList<>();
		for (ICfgNode unvisitedNode : unvisitedNodes)
			unvisitedIds.add(unvisitedNode.getId());

		for (ICfgNode cfgNode : getAllCfgNodes())
			if (cfgNode.isNormalNode())
				if (unvisitedIds.contains(cfgNode.getId()))
					numUnvisitedStatements++;
		return numUnvisitedStatements;
	}

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
		output.append(get(size() - 1));
		return output.toString();
	}

	@Override
	public FullTestpath cast() {
		return this;
	}

}
