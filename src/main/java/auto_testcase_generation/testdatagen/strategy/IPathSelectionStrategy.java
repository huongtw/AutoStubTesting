package auto_testcase_generation.testdatagen.strategy;

import java.util.List;

import auto_testcase_generation.testdatagen.se.IPathConstraints;

/**
 * Interface for path selection strategy
 * 
 * @author Duc Anh Nguyen
 *
 */
public interface IPathSelectionStrategy {

	/**
	 * Negate path constraints
	 *
	 * @return
	 */
	PathSelectionOutput negateTheOriginalPathConstraints();

	List<IPathConstraints> getSolvedPathConstraints();

	void setSolvedPathConstraints(List<IPathConstraints> solvedPathConstraints);

	List<String> getGeneratedTestdata();

	void setGeneratedTestdata(List<String> generatedTestdata);

	IPathConstraints getOriginalConstraints();

	void setOriginalConstraints(IPathConstraints originalConstraints);
}
