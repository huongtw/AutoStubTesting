package auto_testcase_generation.cfg.testpath;

import auto_testcase_generation.testdatagen.se.*;
import com.dse.parser.object.IFunctionNode;
import com.dse.parser.object.INode;
import com.dse.parser.object.IVariableNode;
import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;
import com.dse.logger.AkaLogger;

import auto_testcase_generation.testdatagen.se.memory.ISymbolicVariable;
import auto_testcase_generation.testdatagen.se.solver.ISmtLibGeneration;
import auto_testcase_generation.testdatagen.se.solver.solutionparser.IZ3SolutionParser;
import auto_testcase_generation.testdatagen.se.solver.RunZ3OnCMD;
import auto_testcase_generation.testdatagen.se.solver.SmtLibGeneration;
import auto_testcase_generation.testdatagen.se.solver.solutionparser.Z3SolutionParser;

import java.util.Set;

public class StaticSolutionGeneration implements IStaticSolutionGeneration {
	final static AkaLogger logger = AkaLogger.get(StaticSolutionGeneration.class);

	private ITestpathInCFG testpath;

	private IFunctionNode functionNode;

	private String staticSolution;

	private String constraintsFile;

	private String z3SolverPath;

	public static void main(String[] args) {
	}

	@Override
	public void generateStaticSolution() throws Exception {
		if (this.testpath != null && this.functionNode != null) {
			Parameters variables = this.removeExternVariables(this.getPassingVariables(this.functionNode));
			ISymbolicExecution se = new SymbolicExecution(this.testpath, variables, this.functionNode);

			this.staticSolution = solve(se.getConstraints(), functionNode, se.getNewVariables());
		} else
			this.staticSolution = IStaticSolutionGeneration.NO_SOLUTION;
	}

	public String solve(IPathConstraints pathConstraints, IFunctionNode functionNode, Set<NewVariableInSe> newVars) throws Exception {
		this.staticSolution = IZ3SolutionParser.NO_SOLUTION;

		if (pathConstraints.size() >= 1) {

			// Step 1. Find solution of not-null path constraints
			if (pathConstraints.getNormalConstraints().size() >= 1) {
				ISmtLibGeneration smtLibGeneration = null;
//				switch (functionNode.getFunctionConfig().getTestdataExecStrategy()+"") {
//				case IAutomatedTestdataGeneration.TESTDATA_GENERATION_STRATEGIES.MARS2 + "":
					smtLibGeneration = new SmtLibGeneration(functionNode.getPassingVariables(),
							pathConstraints.getNormalConstraints(), functionNode, newVars);
//					break;
//				case IAutomatedTestdataGeneration.TESTDATA_GENERATION_STRATEGIES.FAST_MARS + "":
//					smtLibGeneration = new SmtLibGeneration2(pathConstraints.getVariablesTableNode(),
//							pathConstraints.getNormalConstraints());
//					break;
//				}

				smtLibGeneration.generate();
				String smtLibContent = smtLibGeneration.getSmtLibContent();
//				 logger.debug("SMT-Lib content:\n" + smtLibContent);

				if (smtLibContent.equals(ISmtLibGeneration.EMPTY_SMT_LIB_FILE)) {
					// In this case, the content of the SMT-Lib file is empty.
					// We dont need to use
					// SMT-Solver because the path constraints does not have any
					// solution
					// definitely.
					this.staticSolution = NO_SOLUTION;
				} else {
					Utils.writeContentToFile(smtLibContent, constraintsFile);
					logger.debug("Append smt constraints to " + constraintsFile);

					logger.debug("Calling z3");
					logger.debug("z3 path = " + z3SolverPath);
					RunZ3OnCMD z3Runner = new RunZ3OnCMD(
							//AbstractSetting.getValue(ISettingv2.SOLVER_Z3_PATH),
							z3SolverPath,
							constraintsFile);
					z3Runner.execute();

					logger.debug("Original solution:\n" + z3Runner.getSolution());
					this.staticSolution = new Z3SolutionParser().getSolution(z3Runner.getSolution());
				}
			} else
				this.staticSolution = EVERY_SOLUTION;

			// Step 2. Add null path constraints (e.g., x!=NULL, x==NULL)
			if (this.staticSolution.equals(NO_SOLUTION)) {
				// nothing to do
			} else {
				// e.g., "x==NULL" ---------> add "sizeof(x)=0"
				for (PathConstraint nullConstraint : pathConstraints.getNullorNotNullConstraints())
					this.staticSolution += nullConstraint.getConstraint().replace("==", "=")
							.replace(ISymbolicVariable.PREFIX_SYMBOLIC_VALUE, "")
							.replace(ISymbolicVariable.SEPARATOR_BETWEEN_STRUCTURE_NAME_AND_ITS_ATTRIBUTES, ".")
							.replace(ISymbolicVariable.ARRAY_CLOSING, "]").replace(ISymbolicVariable.ARRAY_OPENING, "[")
							+ SpecialCharacter.END_OF_STATEMENT;
			}
		} else
			// In this case, the size of the path constraints is equivalent to
			// 0.
			this.staticSolution = IStaticSolutionGeneration.EVERY_SOLUTION;
		return this.staticSolution;
	}

	/**
	 * Get all passing variables of a function
	 *
	 * @param fn
	 * @return
	 */
	private Parameters getPassingVariables(IFunctionNode fn) {
		Parameters vars = new Parameters();
        vars.addAll(fn.getPassingVariables());
		return vars;
	}

	/**
	 * Remove all extern variables from the passing variable.Ex: remove variable
	 * "extern int x = 1"
	 *
	 * @param paramaters
	 * @return
	 */
	private Parameters removeExternVariables(Parameters paramaters) {
		for (int i = paramaters.size() - 1; i >= 0; i--) {
			INode n = paramaters.get(i);

			if (n instanceof IVariableNode)
				if (((IVariableNode) n).isExtern())
					paramaters.remove(n);

		}
		return paramaters;
	}

	@Override
	public ITestpathInCFG getTestpath() {
		return this.testpath;
	}

	@Override
	public void setTestpath(AbstractTestpath testpath) {
		this.testpath = testpath;
	}

	@Override
	public IFunctionNode getFunctionNode() {
		return this.functionNode;
	}

	@Override
	public void setFunctionNode(IFunctionNode functionNode) {
		this.functionNode = functionNode;
	}

	@Override
	public String getStaticSolution() {
		return this.staticSolution;
	}

	public String getConstraintsFile() {
		return constraintsFile;
	}

	public void setConstraintsFile(String constraintsFile) {
		this.constraintsFile = constraintsFile;
	}

	public String getZ3SolverPath() {
		return z3SolverPath;
	}

	public void setStaticSolution(String staticSolution) {
		this.staticSolution = staticSolution;
	}

	public void setZ3SolverPath(String z3SolverPath) {
		this.z3SolverPath = z3SolverPath;
	}

}
