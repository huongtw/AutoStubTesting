package auto_testcase_generation.cfg.testpath;

import com.dse.parser.object.IFunctionNode;

/**
 * Generate static solution of a test path
 *
 * @author ducanhnguyen
 */
public interface IStaticSolutionGeneration {
    String NO_SOLUTION = "";
    String EVERY_SOLUTION = " ";

    /**
     * Generate static solution
     *
     * @return
     * @throws Exception
     */
    void generateStaticSolution() throws Exception;

    /**
     * Get test path
     *
     * @return
     */
    ITestpathInCFG getTestpath();

    /**
     * Set test path
     *
     * @param testpath
     */
    void setTestpath(AbstractTestpath testpath);

    /**
     * Get function node
     *
     * @return
     */
    IFunctionNode getFunctionNode();

    /**
     * Set function node
     *
     * @param functionNode
     */
    void setFunctionNode(IFunctionNode functionNode);

    /**
     * Get static solution
     *
     * @return
     */
    String getStaticSolution();


    String getConstraintsFile();

    void setConstraintsFile(String constraintsFile);

    String getZ3SolverPath();

    void setZ3SolverPath(String z3SolverPath);

    void setStaticSolution(String staticSolution);

}