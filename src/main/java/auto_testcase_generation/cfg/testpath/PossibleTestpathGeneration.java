package auto_testcase_generation.cfg.testpath;

import auto_testcase_generation.cfg.CFGGenerationforSubConditionCoverage;
import auto_testcase_generation.cfg.ICFG;
import auto_testcase_generation.cfg.object.AbstractConditionLoopCfgNode;
import auto_testcase_generation.cfg.object.ConditionCfgNode;
import auto_testcase_generation.cfg.object.EndFlagCfgNode;
import auto_testcase_generation.cfg.object.ICfgNode;
import auto_testcase_generation.constraints.checker.RelatedConstraintsChecker;
import auto_testcase_generation.testdatagen.se.ISymbolicExecution;
import auto_testcase_generation.testdatagen.se.Parameters;
import auto_testcase_generation.testdatagen.se.PathConstraint;
import auto_testcase_generation.testdatagen.se.SymbolicExecution;
import auto_testcase_generation.testdatagen.se.solver.ISmtLibGeneration;
import auto_testcase_generation.testdatagen.se.solver.RunZ3OnCMD;
import auto_testcase_generation.testdatagen.se.solver.SmtLibGeneration;
import auto_testcase_generation.testdatagen.se.solver.solutionparser.Z3SolutionParser;
import com.dse.config.Paths;
import com.dse.parser.ProjectParser;
import com.dse.parser.object.IFunctionNode;
import com.dse.search.Search;
import com.dse.search.condition.FunctionNodeCondition;
import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;
import com.dse.logger.AkaLogger;

import java.io.File;

/**
 * Generate all possible test paths
 *
 * @author DucAnh
 */
public class PossibleTestpathGeneration implements ITestpathGeneration {
	public static final String CONSTRAINTS_FILE = "";//Settingv2.getValue(ISettingv2.SMT_LIB_FILE_PATH);
	public static final String Z3 = "";//Settingv2.getValue(ISettingv2.SOLVER_Z3_PATH);
	final static AkaLogger logger = AkaLogger.get(PossibleTestpathGeneration.class);
	/**
	 * Represent control flow graph
	 */
	private ICFG cfg;
	private long maxIterationsforEachLoop = ITestpathGeneration.DEFAULT_MAX_ITERATIONS_FOR_EACH_LOOP;
	private FullTestpaths possibleTestpaths = new FullTestpaths();

	public PossibleTestpathGeneration(ICFG cfg) {
		this.cfg = cfg;
	}

	public PossibleTestpathGeneration(ICFG cfg, long maxloop) {
		maxIterationsforEachLoop = maxloop;
		this.cfg = cfg;

		this.cfg.resetVisitedStateOfNodes();
		this.cfg.setIdforAllNodes();
	}

	/**
	 * @param cfg
	 * @param maxloop
	 * @param isResetVisitedState
	 *            true if the visit stated is marked unvisited
	 */
	public PossibleTestpathGeneration(ICFG cfg, int maxloop, boolean isResetVisitedState) {
		maxIterationsforEachLoop = maxloop;
		this.cfg = cfg;

		if (isResetVisitedState) {
			this.cfg.resetVisitedStateOfNodes();
			this.cfg.setIdforAllNodes();
		}
	}

	public static void main(String[] args) throws Exception {
		ProjectParser parser = new ProjectParser(new File(Paths.JOURNAL_TEST));

		IFunctionNode function = (IFunctionNode) Search
				.searchNodes(parser.getRootTree(), new FunctionNodeCondition(), "mergeSort(int[],int,int)").get(0);
		System.out.println(function.getAST().getRawSignature());

		CFGGenerationforSubConditionCoverage cfgGen = new CFGGenerationforSubConditionCoverage(function);
		ICFG cfg = cfgGen.generateCFG();
		cfg.setFunctionNode(function);
		cfg.setIdforAllNodes();
		cfg.resetVisitedStateOfNodes();

		int maxIterations = 1;
		PossibleTestpathGeneration tpGen = new PossibleTestpathGeneration(cfg, maxIterations);
		tpGen.generateTestpaths();

		System.out.println("Num of test paths: " + tpGen.getPossibleTestpaths().size());
	}

	@Override
	public void generateTestpaths() {
		// Date startTime = Calendar.getInstance().getTime();
		FullTestpaths testpaths_ = new FullTestpaths();

		ICfgNode beginNode = cfg.getBeginNode();
		FullTestpath initialTestpath = new FullTestpath();
		initialTestpath.setFunctionNode(cfg.getFunctionNode());
		try {
			traverseCFG(beginNode, initialTestpath, testpaths_);
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (ITestpathInCFG tp : testpaths_)
			tp.setFunctionNode(cfg.getFunctionNode());

		possibleTestpaths = testpaths_;

		// Calculate the running time
		// Date end = Calendar.getInstance().getTime();
		// totalRunningTime = end.getTime() - startTime.getTime();
		// logger.debug("Total running time: " + totalRunningTime + " ms");
		// logger.debug("Solving time: " + solvingTime + " ms");
		// logger.debug("Number of solving calls: " + numberOfSolvingCalls + "
		// ms");
		// logger.debug(
		// "Number of solving calls that does not have solution: " +
		// numberOfSolvingCallsThatNoSolution + " ms");
	}

	private void traverseCFG(ICfgNode stm, FullTestpath tp, FullTestpaths testpaths) throws Exception {
		tp.add(stm);
		if (stm instanceof EndFlagCfgNode) {
			testpaths.add((FullTestpath) tp.clone());
        } else {
			ICfgNode trueNode = stm.getTrueNode();
			ICfgNode falseNode = stm.getFalseNode();

			if (stm instanceof ConditionCfgNode)

				if (stm instanceof AbstractConditionLoopCfgNode) {

					int currentIterations = tp.count(trueNode);
					if (currentIterations < maxIterationsforEachLoop) {

						traverseCFG(falseNode, tp, testpaths);
						traverseCFG(trueNode, tp, testpaths);
					} else
						traverseCFG(falseNode, tp, testpaths);
				} else {
					traverseCFG(falseNode, tp, testpaths);
					traverseCFG(trueNode, tp, testpaths);
				}
			else
				traverseCFG(trueNode, tp, testpaths);

        }
        tp.remove(tp.size() - 1);
    }

	protected boolean haveSolution(FullTestpath tp, boolean finalConditionType) throws Exception {
		IPartialTestpath tp1 = createPartialTestpath(tp, finalConditionType);

		String solution = solveTestpath(cfg.getFunctionNode(), tp1);

		return !solution.equals(IStaticSolutionGeneration.NO_SOLUTION);
	}

	protected IPartialTestpath createPartialTestpath(FullTestpath fullTp, boolean finalConditionType) {
		IPartialTestpath partialTp = new PartialTestpath();
		for (ICfgNode node : fullTp.getAllCfgNodes())
			partialTp.getAllCfgNodes().add(node);

		partialTp.setFinalConditionType(finalConditionType);
		return partialTp;
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

		// fast checking
		RelatedConstraintsChecker relatedConstraintsChecker = new RelatedConstraintsChecker(
				se.getConstraints().getNormalConstraints(), function);
		boolean isRelated = relatedConstraintsChecker.check();
		//
		if (isRelated) {
			if (se.getConstraints().getNormalConstraints().size()
					+ se.getConstraints().getNullorNotNullConstraints().size() > 0) {
				/*
				 * Solve the path constraints
				 */
				ISmtLibGeneration smtLibGen = new SmtLibGeneration(function.getPassingVariables(),
						se.getConstraints().getNormalConstraints(), function, se.getNewVariables());
				smtLibGen.generate();

				Utils.writeContentToFile(smtLibGen.getSmtLibContent(), CONSTRAINTS_FILE);

				RunZ3OnCMD z3 = new RunZ3OnCMD(Z3, CONSTRAINTS_FILE);
				z3.execute();
				logger.debug("solving done");
				StringBuilder staticSolution = new StringBuilder(new Z3SolutionParser().getSolution(z3.getSolution()));

				if (staticSolution.toString().equals(IStaticSolutionGeneration.NO_SOLUTION)) {
					return IStaticSolutionGeneration.NO_SOLUTION;
				} else {
					if (se.getConstraints().getNullorNotNullConstraints().size() > 0)
						for (PathConstraint nullConstraint : se.getConstraints().getNullorNotNullConstraints())
							staticSolution.append(nullConstraint).append(SpecialCharacter.END_OF_STATEMENT);

					if (se.getConstraints().getNullorNotNullConstraints().size() > 0)
						return staticSolution + "; " + se.getConstraints().getNullorNotNullConstraints();
					else
						return staticSolution + ";";
				}
			} else
				return IStaticSolutionGeneration.NO_SOLUTION;
		} else
			return IStaticSolutionGeneration.EVERY_SOLUTION;
	}

	@Override
	public ICFG getCfg() {
		return cfg;
	}

	@Override
	public void setCfg(ICFG cfg) {
		this.cfg = cfg;
	}

	@Override
	public long getMaxIterationsforEachLoop() {
		return maxIterationsforEachLoop;
	}

	@Override
	public void setMaxIterationsforEachLoop(long maxIterationsforEachLoop) {
		this.maxIterationsforEachLoop = maxIterationsforEachLoop;
	}

	@Override
	public FullTestpaths getPossibleTestpaths() {
		return possibleTestpaths;
	}
}
