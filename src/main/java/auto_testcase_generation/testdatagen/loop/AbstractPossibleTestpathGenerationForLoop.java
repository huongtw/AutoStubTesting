package auto_testcase_generation.testdatagen.loop;

import java.util.List;

import com.dse.parser.object.IFunctionNode;
import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;
import com.dse.logger.AkaLogger;

import auto_testcase_generation.cfg.ICFG;
import auto_testcase_generation.cfg.object.AbstractConditionLoopCfgNode;
import auto_testcase_generation.cfg.object.ICfgNode;
import auto_testcase_generation.cfg.object.IConditionLoopCfgNode;
import auto_testcase_generation.cfg.testpath.IStaticSolutionGeneration;
import auto_testcase_generation.cfg.testpath.ITestpathInCFG;
import auto_testcase_generation.cfg.testpath.PartialTestpath;
import auto_testcase_generation.cfg.testpath.PartialTestpaths;
import auto_testcase_generation.testdatagen.se.ISymbolicExecution;
import auto_testcase_generation.testdatagen.se.Parameters;
import auto_testcase_generation.testdatagen.se.PathConstraint;
import auto_testcase_generation.testdatagen.se.SymbolicExecution;
import auto_testcase_generation.testdatagen.se.memory.basic.BasicSymbolicVariable;
import auto_testcase_generation.testdatagen.se.memory.ISymbolicVariable;
import auto_testcase_generation.testdatagen.se.solver.ISmtLibGeneration;
import auto_testcase_generation.testdatagen.se.solver.RunZ3OnCMD;
import auto_testcase_generation.testdatagen.se.solver.SmtLibGeneration;
import auto_testcase_generation.testdatagen.se.solver.solutionparser.Z3SolutionParser;

/**
 * Created by ducanhnguyen on 5/8/2017.
 */
public abstract class AbstractPossibleTestpathGenerationForLoop {
    public static final String CONSTRAINTS_FILE = "";//Settingv2.getValue(ISettingv2.SMT_LIB_FILE_PATH);
    public static final String Z3 = "";//Settingv2.getValue(ISettingv2.SOLVER_Z3_PATH);
    final static AkaLogger logger = AkaLogger.get(AbstractPossibleTestpathGenerationForLoop.class);
    /**
     * Represent control flow graph
     */
    protected ICFG cfg;
    protected PartialTestpaths possibleTestpaths = new PartialTestpaths();
    /**
     * The maximum iteration for the testing loop
     */
    protected int maximumIterationForTestingLoop = -1;
    /**
     * Maximum iterations for all loops not being tested
     */
    protected int maximumIterationsForOtherLoops = -1;
    /**
     * The number of iteration in the unbound loop
     */
    protected int iterationForUnboundedTestingLoop = -1;
    protected boolean addTheEndTestingCondition = false;
    protected boolean isUnboundedTestingLoop = true;
    protected int delta_ = 0;
    protected int realMaximumIterationForTestingLoop;
    AbstractConditionLoopCfgNode loopCondition = null;

    protected abstract void traverseCFG(ICfgNode stm, PartialTestpath tp, PartialTestpaths testpaths,
                                        boolean isJustOverTheTestingLoop) throws Exception;

    public void generateTestpaths() throws Exception {
        if (loopCondition != null && maximumIterationsForOtherLoops != -1 && iterationForUnboundedTestingLoop != -1) {
            ICfgNode beginNode = cfg.getBeginNode();
            PartialTestpath initialTestpath = new PartialTestpath();
            initialTestpath.setFunctionNode(cfg.getFunctionNode());
            try {
                traverseCFG(beginNode, initialTestpath, possibleTestpaths, false);
                realMaximumIterationForTestingLoop = maximumIterationForTestingLoop;

                if (!isUnboundedTestingLoop) {
                    // maxLoop-1
                    logger.debug("test (maxLoop-1) = " + (realMaximumIterationForTestingLoop - 1) + " times");
                    cfg.resetVisitedStateOfNodes();
                    delta_ = -1;
                    traverseCFG(beginNode, initialTestpath, possibleTestpaths, false);

                    // maxLoop+1
                    logger.debug("test (maxLoop+1) = " + (realMaximumIterationForTestingLoop + 1) + " times");
                    cfg.resetVisitedStateOfNodes();
                    delta_ = +1;
                    traverseCFG(beginNode, initialTestpath, possibleTestpaths, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else
            throw new Exception("Dont configure full");
    }

    public int getMaximumIterationsInTargetCondition(ICfgNode stm, PartialTestpath tp) throws Exception {
        int iteration = iterationForUnboundedTestingLoop;
        isUnboundedTestingLoop = true;

        if (stm instanceof AbstractConditionLoopCfgNode) {
            if (stm.equals(loopCondition)) {
                AbstractConditionLoopCfgNode stmCast = (AbstractConditionLoopCfgNode) stm;

                String iterationVariable = stmCast.getIterationVariable();
                if (iterationVariable != null) {
                    // Create table of variables to the current condition
                    Parameters paramaters = new Parameters();
                    paramaters.addAll(cfg.getFunctionNode().getArguments());
                    paramaters.addAll(cfg.getFunctionNode().getReducedExternalVariables());

                    ISymbolicExecution se = new SymbolicExecution((ITestpathInCFG) tp, paramaters, cfg.getFunctionNode());
                    List<ISymbolicVariable> vars = se.getTableMapping().getVariables();

                    for (ISymbolicVariable var : vars)
                        if (var.getName().equals(iterationVariable)
                                && var instanceof BasicSymbolicVariable) {

                            double initialIteration = Utils.toDouble(var.getAllPhysicalCells().get(0).getValue());
                            if (initialIteration != Utils.UNDEFINED_TO_DOUBLE) {
                                // Compute delta
                                double bound = -1;
                                if (stmCast.getLowerBound() != IConditionLoopCfgNode.CANNOT_DETECT_BOUND) {
                                    bound = stmCast.getLowerBound();
                                } else if (stmCast.getUpperBound() != IConditionLoopCfgNode.CANNOT_DETECT_BOUND) {
                                    bound = stmCast.getUpperBound();
                                }

                                double delta = -1;
                                if (bound >= 0 && initialIteration >= 0)
                                    delta = Math.abs(bound - initialIteration) + 1;
                                else if (bound >= 0 && initialIteration <= 0)
                                    delta = bound + Math.abs(initialIteration) + 1;
                                else if (bound <= 0 && initialIteration >= 0)
                                    delta = Math.abs(bound) + initialIteration + 1;
                                else if (bound <= 0 && initialIteration <= 0)
                                    delta = Math.abs(Math.abs(bound) - Math.abs(initialIteration)) + 1;

                                iteration = Utils.toInt(Math.round(delta) + "") + delta_;
                                isUnboundedTestingLoop = false;
                            } else
                                iteration = iterationForUnboundedTestingLoop;
                            break;
                        }
                } else {
                    iteration = iterationForUnboundedTestingLoop;
                }
            }
        }
        return iteration;
    }

    protected String solveTestpath(IFunctionNode function, ITestpathInCFG testpath) throws Exception {
        /*
		 * Get the passing variables of the given function
		 */
        Parameters paramaters = new Parameters();
        paramaters.addAll(function.getArguments());
        paramaters.addAll(function.getReducedExternalVariables());

		/*
		 * Get the corresponding path constraints of the test path
		 */
        ISymbolicExecution se = new SymbolicExecution(testpath, paramaters, function);
        if (se.getConstraints().getNormalConstraints().size() + se.getConstraints().getNullorNotNullConstraints().size() > 0) {
			/*
			 * Solve the path constraints
			 */
            ISmtLibGeneration smtLibGen = new SmtLibGeneration(function.getPassingVariables(),
                    se.getConstraints().getNormalConstraints(),
                    function, se.getNewVariables());
            smtLibGen.generate();

            Utils.writeContentToFile(smtLibGen.getSmtLibContent(), CONSTRAINTS_FILE);

            RunZ3OnCMD z3 = new RunZ3OnCMD(Z3, CONSTRAINTS_FILE);
            z3.execute();
            StringBuilder staticSolution = new StringBuilder(new Z3SolutionParser().getSolution(z3.getSolution()));

            if (staticSolution.toString().equals(IStaticSolutionGeneration.NO_SOLUTION)) {
                return IStaticSolutionGeneration.NO_SOLUTION;
            } else {
                if (se.getConstraints().getNullorNotNullConstraints().size() > 0)
                    for (PathConstraint nullConstraint : se.getConstraints().getNullorNotNullConstraints())
                        staticSolution.append(nullConstraint.getConstraint()).append(SpecialCharacter.END_OF_STATEMENT);

                if (se.getConstraints().getNullorNotNullConstraints().size() > 0)
                    return staticSolution + "; " + se.getConstraints().getNullorNotNullConstraints();
                else
                    return staticSolution + ";";
            }
        } else
            return IStaticSolutionGeneration.NO_SOLUTION;
    }

    public PartialTestpaths getPossibleTestpaths() {
        return possibleTestpaths;
    }

    public int getMaximumIterationForTestingLoop() {
        return maximumIterationForTestingLoop;
    }

    public int getMaximumIterationsForOtherLoops() {
        return maximumIterationsForOtherLoops;
    }

    public void setMaximumIterationsForOtherLoops(int maximumIterationsForOtherLoops) {
        this.maximumIterationsForOtherLoops = maximumIterationsForOtherLoops;
    }

    public int getIterationForUnboundedTestingLoop() {
        return iterationForUnboundedTestingLoop;
    }

    public void setIterationForUnboundedTestingLoop(int iterationForUnboundedTestingLoop) {
        this.iterationForUnboundedTestingLoop = iterationForUnboundedTestingLoop;
    }

    public int getRealMaximumIterationForTestingLoop() {
        return realMaximumIterationForTestingLoop;
    }

    public void setAddTheEndTestingCondition(boolean addTheEndTestingCondition) {
        this.addTheEndTestingCondition = addTheEndTestingCondition;
    }

}
