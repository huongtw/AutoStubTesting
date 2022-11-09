package auto_testcase_generation.cfg.testpath;

import auto_testcase_generation.cfg.ICFG;

/**
 * Generate test paths of a CFG
 *
 * @author ducanh
 */
public interface ITestpathGeneration {

    int DEFAULT_MAX_ITERATIONS_FOR_EACH_LOOP = 1;

    /**
     * Generate test paths satisfying criterion of the given CFG
     *
     * @return
     */
    void generateTestpaths() throws Exception;

    /**
     * Get the CFG
     *
     * @return
     */
    ICFG getCfg();

    /**
     * Set the CFG
     *
     * @param cfg
     */
    void setCfg(ICFG cfg);

    /**
     * Get the maximum iterations for each loop
     *
     * @return
     */
    long getMaxIterationsforEachLoop();

    /**
     * Set the maximum iterations for each loop
     *
     * @param maxIterationsforEachLoop
     */
    void setMaxIterationsforEachLoop(long maxIterationsforEachLoop);

    FullTestpaths getPossibleTestpaths();

}
