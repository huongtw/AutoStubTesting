package auto_testcase_generation.testdatagen.strategy;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import com.dse.logger.AkaLogger;

import auto_testcase_generation.cfg.object.ConditionCfgNode;
import auto_testcase_generation.testdatagen.se.IPathConstraints;
import auto_testcase_generation.testdatagen.se.PathConstraint;
import auto_testcase_generation.testdatagen.se.PathConstraints;

/**
 * Select a random branch vs an unvisited branch randomly
 * 
 * @author Duc Anh Nguyen
 *
 */
public class UnvisitedBranchesvsRandomBranchesSelection extends AbstractPathSelectionStrategy {
	final static AkaLogger logger = AkaLogger.get(UnvisitedBranchesvsRandomBranchesSelection.class);

	@Override
	public PathSelectionOutput negateTheOriginalPathConstraints() {
		PathSelectionOutput output = new PathSelectionOutput();

		IPathConstraints negatedConstraints = null;
		boolean foundNegatedCondition = false;
		Set<Integer> negatedIndexs = new HashSet<>();
		ConditionCfgNode negatedCfgNode;

		do {
			int negatedConstraintsIndexCandidate = new Random().nextInt(getOriginalConstraints().size());

			switch (((PathConstraints) getOriginalConstraints()).get(negatedConstraintsIndexCandidate).getType()) {
			case PathConstraint.ADDITIONAL_TYPE:
				negatedIndexs.add(negatedConstraintsIndexCandidate);
				continue;
			case PathConstraint.CREATE_FROM_DECISION:
				if (!negatedIndexs.contains(negatedConstraintsIndexCandidate)) {
					negatedIndexs.add(negatedConstraintsIndexCandidate);

					// If the negated condition is
					// not visited in all of two
					// branches
					negatedConstraints = getOriginalConstraints().negateConditionAt(negatedConstraintsIndexCandidate);

					// logger.debug("[Optimization]: negatedConstraints: " + negatedConstraints);
					// logger.debug("[Optimization]: getSolvedPathConstraints: " +
					// getSolvedPathConstraints());

					// If the negated path constraints are not solved before
					if (!getSolvedPathConstraints().contains(negatedConstraints)) {
						logger.debug("[Optimization] Add path constraints");
						getSolvedPathConstraints().add(negatedConstraints);

						negatedCfgNode = (ConditionCfgNode) ((PathConstraints) negatedConstraints)
								.get(negatedConstraintsIndexCandidate).getCfgNode();

						switch (new Random().nextInt(2)) {
						case 0:
							// choose random branches
							if (negatedCfgNode != null)
								if ((negatedCfgNode
										.getFalseNode() != null /* && !negatedCfgNode.isVisitedFalseBranch() */)
										|| (negatedCfgNode
												.getTrueNode() != null/*
																		 * && !negatedCfgNode.isVisitedTrueBranch()
																		 */)) {
									foundNegatedCondition = true;
								}
							break;
						case 1:
							// choose unvisited branches
							if (negatedCfgNode != null)
								if ((negatedCfgNode.getFalseNode() != null && !negatedCfgNode.isVisitedFalseBranch())
										|| (negatedCfgNode.getTrueNode() != null
												&& !negatedCfgNode.isVisitedTrueBranch())) {
									foundNegatedCondition = true;
								}
							break;
						}

					} else
						logger.debug("[Optimization] Duplicate path constraints");
				}
				break;
			}
		} while (!foundNegatedCondition && negatedIndexs.size() < getOriginalConstraints().size());

		if (foundNegatedCondition)
			output.setNegatedPathConstraints(negatedConstraints);
		else
			output.setNegatedPathConstraints(null);

		if (negatedIndexs.size() == getOriginalConstraints().size())
			output.setNegateAllConditions(true);
		else
			output.setNegateAllConditions(false);
		return output;

	}
}
