package auto_testcase_generation.testdatagen.fastcompilation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import auto_testcase_generation.testdatagen.se.NewVariableInSe;
import auto_testcase_generation.testdatagen.se.memory.VariableNodeTable;
import com.dse.util.SpecialCharacter;

import auto_testcase_generation.testdatagen.se.ISymbolicExecution;
import auto_testcase_generation.testdatagen.se.PathConstraint;
import auto_testcase_generation.testdatagen.se.memory.ISymbolicVariable;
import auto_testcase_generation.testdatagen.se.solver.ISmtLibGeneration;
import auto_testcase_generation.testdatagen.se.solver.SmtLibv2Normalizer2;
import auto_testcase_generation.testdatagen.testdatainit.VariableTypes;

/**
 * Generate SMT-Lib file
 *
 * @author anhanh
 */
@Deprecated
public class SmtLibGeneration2 implements ISmtLibGeneration {

	// List of test cases
	private List<ISymbolicVariable> testcases;
	// List of path constraints
	private List<PathConstraint> constraints;
	// SMT-Lib content
	private String smtLib = "";

	private Set<NewVariableInSe> newVariableInSes = new HashSet<>();

	public SmtLibGeneration2(List<ISymbolicVariable> testcases, List<PathConstraint> constraints) {
		this.testcases = testcases;
		this.constraints = constraints;
	}

	public static void main(String[] args) throws Exception {
	}

	@Override
	public void generate() throws Exception {
		smtLib = ISmtLibGeneration.OPTION_TIMEOUT + SpecialCharacter.LINE_BREAK + getDeclarationFun(testcases);

		// Generate body of the smt-lib file
		if (constraints.size() == 0)
			smtLib = EMPTY_SMT_LIB_FILE;
		else {
			for (PathConstraint constraint : constraints)
				switch (constraint.getConstraint()) {
				case ISymbolicExecution.NO_SOLUTION_CONSTRAINT:
					smtLib = EMPTY_SMT_LIB_FILE;
					return;
				case ISymbolicExecution.ALWAYS_TRUE_CONSTRAINT:
					// nothing to do
					break;
				default:
					SmtLibv2Normalizer2 normalizer = new SmtLibv2Normalizer2(constraint.getConstraint());
					normalizer.normalize();

					if (normalizer.getNormalizedSourcecode() != null
							&& normalizer.getNormalizedSourcecode().length() > 0) {
						smtLib += "(assert" + normalizer.getNormalizedSourcecode() + ")" + SpecialCharacter.LINE_BREAK;
					} else {
						// If we can not normalize the constraint, we ignore it :)
					}
					break;
				}

			smtLib += ISmtLibGeneration.SOLVE_COMMAND;
		}
		//		logger.debug(smtLib);
	}

	/**
	 * Generate "(declare-fun...)"
	 *
	 * @return
	 * @throws Exception
	 */
	private String getDeclarationFun(List<ISymbolicVariable> variables) throws Exception {
		StringBuilder output = new StringBuilder();
		for (ISymbolicVariable var : variables) {
			String type = var.getType();
			if (VariableTypes.isBasic(type))
				switch (VariableTypes.getType(type)) {
				/*
				  float type
				 */
				case VariableTypes.BASIC.NUMBER.FLOAT.FLOAT:
				case VariableTypes.BASIC.NUMBER.FLOAT.DOUBLE:

				case VariableTypes.BASIC.NUMBER.FLOAT.FLOAT + VariableTypes.REFERENCE:
				case VariableTypes.BASIC.NUMBER.FLOAT.DOUBLE + VariableTypes.REFERENCE:
					output.append("(declare-fun ").append(convertToVariableInSmt(var.getName())).append(" () Real)").append(SpecialCharacter.LINE_BREAK);
					break;
				default:
					/*
					  integer type
					 */
					output.append("(declare-fun ").append(convertToVariableInSmt(var.getName())).append(" () Int)").append(SpecialCharacter.LINE_BREAK);
					break;
				}
			else if (VariableTypes.isOneDimension(type) || VariableTypes.isOneLevel(type))
				/*
				  float type
				 */
				switch (VariableTypes.getType(type)) {

				case VariableTypes.BASIC.NUMBER.FLOAT.FLOAT + VariableTypes.ONE_DIMENSION:
				case VariableTypes.BASIC.NUMBER.FLOAT.DOUBLE + VariableTypes.ONE_DIMENSION:

				case VariableTypes.BASIC.NUMBER.FLOAT.FLOAT + VariableTypes.ONE_LEVEL:
				case VariableTypes.BASIC.NUMBER.FLOAT.DOUBLE + VariableTypes.ONE_LEVEL:

					output.append("(declare-fun ").append(convertToVariableInSmt(var.getName())).append(" (Int) Real)").append(SpecialCharacter.LINE_BREAK);
					break;

				default:
					/*
					  integer type
					 */
					output.append("(declare-fun ").append(convertToVariableInSmt(var.getName())).append(" (Int) Int)").append(SpecialCharacter.LINE_BREAK);
					break;
				}
			else if (VariableTypes.isTwoDimension(type) || VariableTypes.isTwoLevel(type))
				/*
				  float type
				 */
				switch (VariableTypes.getType(type)) {

				case VariableTypes.BASIC.NUMBER.FLOAT.FLOAT + VariableTypes.TWO_DIMENSION:
				case VariableTypes.BASIC.NUMBER.FLOAT.DOUBLE + VariableTypes.TWO_DIMENSION:
				case VariableTypes.BASIC.NUMBER.FLOAT.FLOAT + VariableTypes.TWO_LEVEL:
				case VariableTypes.BASIC.NUMBER.FLOAT.DOUBLE + VariableTypes.TWO_LEVEL:

					output.append("(declare-fun ").append(convertToVariableInSmt(var.getName())).append(" (Int Int) Real)").append(SpecialCharacter.LINE_BREAK);
					break;

				default:
					/*
					  integer type
					 */
					output.append("(declare-fun ").append(convertToVariableInSmt(var.getName())).append(" (Int Int) Int)").append(SpecialCharacter.LINE_BREAK);
					break;
				}
			else
				output.append("; dont support ").append(var.getType()).append(SpecialCharacter.LINE_BREAK);
		}
		return output.toString();

	}

	@Override
	public String getSmtLibContent() {
		return smtLib;
	}

	protected String convertToVariableInSmt(String name) {
		return ISymbolicVariable.PREFIX_SYMBOLIC_VALUE
				+ VariableNodeTable.normalizeNameOfVariable(name);
	}
}
