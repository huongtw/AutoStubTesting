package auto_testcase_generation.testdatagen.strategy;

import auto_testcase_generation.testdatagen.se.IPathConstraints;

/**
 * Represent an output of path selection strategy
 * 
 * @author Duc Anh Nguyen
 *
 */
public class PathSelectionOutput {
	IPathConstraints negatedPathConstraints;
	boolean negateAllConditions = false;

	public PathSelectionOutput() {
	}

	public IPathConstraints getNegatedPathConstraints() {
		return negatedPathConstraints;
	}

	public void setNegatedPathConstraints(IPathConstraints negatedPathConstraints) {
		this.negatedPathConstraints = negatedPathConstraints;
	}

	public boolean isNegateAllConditions() {
		return negateAllConditions;
	}

	public void setNegateAllConditions(boolean negateAllConditions) {
		this.negateAllConditions = negateAllConditions;
	}
}
