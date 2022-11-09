package auto_testcase_generation.testdatagen.se;

import com.dse.parser.normalizer.MultipleNormalizers;
import auto_testcase_generation.testdatagen.se.expander.AbstractPathConstraintExpander;
import auto_testcase_generation.testdatagen.se.expander.ArrayIndexExpander;
import auto_testcase_generation.testdatagen.se.memory.VariableNodeTable;
import auto_testcase_generation.testdatagen.se.normalization.ArrayIndexNormalizer;
import auto_testcase_generation.testdatagen.se.normalization.CharToNumberNormalizer;
import auto_testcase_generation.testdatagen.se.normalization.PointerAccessNormalizer;
import auto_testcase_generation.testdatagen.se.normalization.UnnecessaryCharacterNormalizer;
import com.dse.parser.object.IFunctionNode;

import java.util.ArrayList;

public class PathConstraints extends ArrayList<PathConstraint> implements IPathConstraints {

	private boolean isAlwaysFalse = false;

	private IFunctionNode functionNode;

	private VariableNodeTable variableTableNode;

	public PathConstraints() {
	}

	/**
	 * Add new constraint to a list of path constraints. In this method, the
	 * constraint is normalized (e.g., 'a'---> 65).
	 */
	@Override
	public boolean add(PathConstraint newConstraint) {
		if (this.contains(newConstraint))
			return true;
		final String ALWAYS_TRUE_EXPRESSION_IN_JEVAL = "1";
		final String ALWAYS_FALSE_EXPRESSION_IN_JEVAL = "0";

		if (new CustomJeval().evaluate(newConstraint.getConstraint()).equals(ALWAYS_FALSE_EXPRESSION_IN_JEVAL)) {
			this.isAlwaysFalse = true;
			return false;

		} else if (new CustomJeval().evaluate(newConstraint.getConstraint()).equals(ALWAYS_TRUE_EXPRESSION_IN_JEVAL))
			return true;

		else {
//			newConstraint.setConstraint(normalizeConstraint(newConstraint.getConstraint()));
//			newConstraint.setConstraint(newConstraint.getConstraint());

			AbstractPathConstraintExpander indexExpander = new ArrayIndexExpander();
			indexExpander.setInputConstraint(newConstraint.getConstraint());
			indexExpander.generateNewConstraints();
			for (String newConstraintItem : indexExpander.getNewConstraints()) {
//				try {
//					newConstraintItem = ExpressionRewriterUtils.rewrite(variableTableNode, newConstraintItem);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
				super.add(new PathConstraint(newConstraintItem, null, PathConstraint.ADDITIONAL_TYPE));
			}

			return super.add(newConstraint);
		}
	}

	public boolean add(PathConstraint constraint, boolean normalize) {
		if (normalize)
			return this.add(constraint);
		else {
			return super.add(constraint);
		}
	}

	@Override
	public String normalizeConstraint(String constraint) {
		String normalizedSc;
		MultipleNormalizers mul = new MultipleNormalizers();
		mul.addNormalizer(new CharToNumberNormalizer());
		mul.addNormalizer(new ArrayIndexNormalizer());
		mul.addNormalizer(new UnnecessaryCharacterNormalizer());
		mul.addNormalizer(new PointerAccessNormalizer());
		mul.setOriginalSourcecode(constraint);
		mul.normalize();
		normalizedSc = mul.getNormalizedSourcecode();

		// Some small other normalizers here
		// Character '\0' (end of char*) ==> ASCII = 0
		normalizedSc = normalizedSc.replace("'\\0'", "0");
		normalizedSc = normalizedSc.replace("\\s*true\\s*", "1");
		normalizedSc = normalizedSc.replace("\\s*false\\s*", "0");
		return normalizedSc;
	}

	@Override
	public PathConstraints getNullorNotNullConstraints() {
		PathConstraints output = new PathConstraints();

		for (PathConstraint constraint : this)
			if (this.containNULLExpression(constraint))
				output.add(constraint, false);
		return output;
	}

	@Override
	public PathConstraints getNormalConstraints() {
		PathConstraints output = new PathConstraints();

		for (PathConstraint constraint : this)
			if (!this.containNULLExpression(constraint))
				output.add(constraint);
		return output;
	}

	@Override
	public String toString() {
		StringBuilder output = new StringBuilder();
		for (PathConstraint constraint : this)
			// output += "\t" + constraint + ";\n";
			output.append(constraint).append("\n");
		output.append("\n\n");
		return output.toString();
	}

	/**
	 * Return true if the expression is assignment to NULL or equivalently.
	 */
	private boolean containNULLExpression(PathConstraint constraint) {
		return constraint.getConstraint().contains("=NULL") || constraint.getConstraint().contains("!=NULL");
	}

	public IFunctionNode getFunctionNode() {
		return this.functionNode;
	}

	public void setFunctionNode(IFunctionNode functionNode) {
		this.functionNode = functionNode;
	}

	@Override
	public boolean isAlwaysFalse() {
		return this.isAlwaysFalse;
	}

	@Override
	public int getNumofConditions() {
		return size();
	}

	@Override
	public String getElementAt(int pos) {
		return get(pos).getConstraint();
	}

	@Override
	public IPathConstraints negateTheLastCondition() {
		PathConstraints pc = (PathConstraints) this.clone();
		PathConstraint lastConstraint = pc.get(pc.size() - 1);
		lastConstraint.setConstraint("!(" + lastConstraint.getConstraint() + ")");
		return pc;
	}

	@Override
	public IPathConstraints negateConditionAt(int endIndex) {
		if (endIndex < 0 || endIndex >= size())
			return null;
		else {
			PathConstraints pc = new PathConstraints();

			for (int i = 0; i <= endIndex - 1; i++)
				try {
					pc.add((PathConstraint) get(i).clone());
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
			try {
				PathConstraint negatedConstraint = (PathConstraint) get(endIndex).clone();
				// "x!=NULL" ----negated-------> "x==NULL"
				if (negatedConstraint.getConstraint().contains("!=NULL")) {
					negatedConstraint.setConstraint(negatedConstraint.getConstraint().split("!=")[0] + "==NULL");
				} else
				// "x==NULL" ----negated-------> "x!=NULL"
				if (negatedConstraint.getConstraint().contains("==NULL")) {
					negatedConstraint.setConstraint(negatedConstraint.getConstraint().split("==")[0] + "!=NULL");
				} else
					negatedConstraint.setConstraint("!(" + negatedConstraint.getConstraint() + ")");
				pc.add(negatedConstraint);
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			pc.setVariablesTableNode(getVariablesTableNode());

			return pc;
		}
	}

	@Override
	public void setVariablesTableNode(VariableNodeTable tables) {
		this.variableTableNode = tables;
	}

	@Override
	public VariableNodeTable getVariablesTableNode() {
		return variableTableNode;
	}

	@Override
	public int size() {
		return super.size();
	}

//	public void addNotNullConstraintsForPointer(ISymbolicVariable symbolicVariable) {
//		if (VariableTypes.isOneLevel(symbolicVariable.getType())) {
//			OneLevelSymbolicVariable cast = (OneLevelSymbolicVariable) symbolicVariable;
//			String newConstraint = cast.getReference().getBlock().getName();
//
//			final String NOT_NULL_VALUE = "1";
//			this.add(new PathConstraint(String.format("%s==%s", newConstraint, NOT_NULL_VALUE), null, PathConstraint.ADDITIONAL_TYPE));
//		} else {
//			// nothing to do
//		}
//	}

//	public void addNotNullConstraintsForPointer(List<ISymbolicVariable> symbolicVariables) {
//		for (ISymbolicVariable symbolicVariable : symbolicVariables)
//			addNotNullConstraintsForPointer(symbolicVariable);
//	}

	@Override
	public int getNumOfPCcreatedFromDecision() {
		int num = 0;
		for (PathConstraint constraint : this)
			if (constraint.getType() == PathConstraint.CREATE_FROM_DECISION)
				num++;
		return num;
	}

}
