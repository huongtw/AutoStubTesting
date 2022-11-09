package auto_testcase_generation.testdatagen.se.normalstatementparser;

import auto_testcase_generation.cfg.FunctionCallVisitor;
import auto_testcase_generation.testdatagen.se.ExpressionRewriterUtils;
import auto_testcase_generation.testdatagen.se.memory.*;
import auto_testcase_generation.testdatagen.se.memory.array.ArraySymbolicVariable;
import auto_testcase_generation.testdatagen.se.memory.array.one_dim.OneDimensionSymbolicVariable;
import auto_testcase_generation.testdatagen.se.memory.basic.BasicSymbolicVariable;
import auto_testcase_generation.testdatagen.se.memory.pointer.PointerSymbolicVariable;
import auto_testcase_generation.testdatagen.se.memory.structure.SimpleStructureSymbolicVariable;
import com.dse.parser.object.IFunctionNode;
import com.dse.util.Utils;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.*;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTBinaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCastExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTLiteralExpression;

/**
 * Assign a variable that its name is normal (e.g., x, y, z) to an expression.
 *
 * @author ducanhnguyen
 */
public class NormalVariableToExpressionParser extends NormalBinaryAssignmentParser {

	@Override
	public void parse(IASTNode ast, VariableNodeTable table, FunctionCallTable callTable) throws Exception {
		ast = Utils.shortenAstNode(ast);

		if (ast instanceof ICPPASTBinaryExpression) {
			IASTNode leftAST = ((ICPPASTBinaryExpression) ast).getOperand1();
			IASTNode rightAST = ((ICPPASTBinaryExpression) ast).getOperand2();
			ISymbolicVariable left = table.findorCreateVariableByName(leftAST.getRawSignature());
			ISymbolicVariable right = table.findorCreateVariableByName(rightAST.getRawSignature());

			if (left instanceof BasicSymbolicVariable) {
				/*
				 * In case right is type casting, e.g., (char) x
				 */
				if (rightAST instanceof CPPASTCastExpression)
					new NormalVariableToTypeCastingParser().parse(ast, table, callTable);
				else
				/*
				 * The right expression is a condition. Ex: x = a>b
				 */
				if (rightAST instanceof CPPASTBinaryExpression && Utils.isCondition(rightAST.getRawSignature()))
					throw new Exception("Don't support " + ast.getRawSignature());
				else if (rightAST instanceof IASTFunctionCallExpression) {
					String varName = callTable.get(rightAST);
					if (varName != null) {
						BasicSymbolicVariable leftVar = (BasicSymbolicVariable) left;
						leftVar.setValue(varName);
						callTable.remove(rightAST);
					}
				} else {
					rightAST = Utils.shortenAstNode(rightAST);

					BasicSymbolicVariable leftVar = (BasicSymbolicVariable) left;
					String reducedRight = ExpressionRewriterUtils.rewrite(table, rightAST.getRawSignature());

//					if (rightAST.getRawSignature().equals(leftAST.getRawSignature())) {
//						for (ISymbolicVariable var : table.findVariablesByName(rightAST.getRawSignature())) {
//							if (!var.equals(leftVar) && var instanceof BasicSymbolicVariable) {
//								reducedRight = ((BasicSymbolicVariable) var).getSymbolicValue();
//								break;
//							}
//						}
//					}

					leftVar.setValue(reducedRight);
				}
			} else if (left instanceof PointerSymbolicVariable) {
				rightAST = Utils.shortenAstNode(rightAST);
				// TODO: MULTI LEVEL
//				if (((PointerSymbolicVariable) left).getLevel() == 1)
				assignOneLevelSymbolicVariableToExpression(leftAST, rightAST, (PointerSymbolicVariable) left, table, callTable);
 			} else if (left instanceof SimpleStructureSymbolicVariable) {
				SimpleStructureSymbolicVariable leftVar = (SimpleStructureSymbolicVariable) left;
				if (right == null) {
					leftVar.createNewAttributes(rightAST.getRawSignature(), null);
				} else {
					SimpleStructureSymbolicVariable rightVar = (SimpleStructureSymbolicVariable) right;
					leftVar.copyAttributes(rightVar);
//					leftVar.assign(rightVar);
				}
			} else {
				throw new Exception("Don't support " + ast.getRawSignature());
			}
		} else {
			throw new Exception("Don't support " + ast.getRawSignature());
		}
	}

	/**
	 * Consider expression below (p1 is a pointer): <br/>
	 * Ex1: p1 = &numbers[2]<br/>
	 * Ex2: p1 = &a<br/>
	 * Ex3: p1 = NULL<br/>
	 *
	 * @param astLeft
	 * @param astRight
	 * @param leftVar
	 * @param table
	 * @throws Exception
	 */
	private void assignOneLevelSymbolicVariableToExpression(IASTNode astLeft, IASTNode astRight, PointerSymbolicVariable leftVar,
															IVariableNodeTable table, FunctionCallTable callTable) throws Exception {
		String reducedRight = astRight.getRawSignature();
		/*
		 * Ex1: p1 = &numbers[2]
		 *
		 * Ex2: p1 = &a
		 *
		 * Ex3: p1 = NULL
		 */
		if (astRight instanceof IASTUnaryExpression && reducedRight.startsWith(AssignmentParser.ADDRESS_OPERATOR)) {
			IASTNode shortenRight = Utils.shortenAstNode(astRight.getChildren()[0]);

			/*
			 * Ex: numbers[2]
			 */
			if (shortenRight instanceof ICPPASTArraySubscriptExpression) {
				CPPASTIdExpression nameVar = (CPPASTIdExpression) shortenRight.getChildren()[0];
				CPPASTLiteralExpression index = (CPPASTLiteralExpression) shortenRight.getChildren()[1];

				String newRight = nameVar + "+" + index;
				assignPointerToPointer(Utils.convertToIAST(newRight), leftVar, table, callTable);

			} else
			/*
			 * Ex: a
			 */
			if (shortenRight instanceof CPPASTIdExpression) {
				String shortenRightInStr = shortenRight.getRawSignature();

				ISymbolicVariable ref = table.findorCreateVariableByName(shortenRightInStr);

				if (ref instanceof BasicSymbolicVariable) {
					LogicBlock vituralLeftBlock = new LogicBlock(
							ISymbolicVariable.PREFIX_SYMBOLIC_VALUE + ref.getName());
					vituralLeftBlock.addLogicalCell(((BasicSymbolicVariable) ref).getCell(), LogicBlock.FIRST_CELL);
					leftVar.setReference(new Reference(vituralLeftBlock, Reference.FIRST_START_INDEX));

				} else
					throw new Exception(
							"Don't support " + astLeft.getRawSignature() + " = " + astRight.getRawSignature());
			}
			/*
			 * Ex: NULL
			 */
			else if (reducedRight.equals("NULL"))
				assignPointerToNull(leftVar);
			else
				throw new Exception("Don't support " + astLeft.getRawSignature() + " = " + astRight.getRawSignature());
		} else
			/*
			 * In this case, the right expression may be an expression of pointer. Ex1:
			 * p2+1. Ex2: p2-1
			 */
			assignPointerToPointer(astRight, leftVar, table, callTable);
	}

	private void assignPointerToNull(Object left) {
		((Reference) left).setBlock(null);
		((Reference) left).setStartIndex(Reference.UNDEFINED_INDEX);
	}

	/**
	 * Parse statement that change the reference of a pointer <br/>
	 * Ex1: p1 = p2<br/>
	 * Ex2: p1 = p2 + 1<br/>
	 * Ex3: p1 = p2 - 1<br/>
	 *
	 * @param right right-hand side
	 * @param left left-hand side
	 * @param table table of variables
	 * @throws Exception
	 */
	private void assignPointerToPointer(IASTNode right, PointerSymbolicVariable left,
										IVariableNodeTable table, FunctionCallTable callTable) throws Exception {

		String index = Reference.FIRST_START_INDEX;
		String nameRightPointer;

		String newRight = right.getRawSignature();
		FunctionCallVisitor visitor = new FunctionCallVisitor((IFunctionNode) table.getFunctionNode());
		right.accept(visitor);
		for (IASTFunctionCallExpression expr : visitor.getCallMap().keySet()) {
			String varName = callTable.get(expr);
			if (varName != null) {
				String regex = "\\Q" + expr.getRawSignature() + "\\E";
				newRight = newRight.replaceFirst(regex, varName);
				callTable.remove(expr);
			}
		}

		if (!newRight.equals(right.getRawSignature())) {
			assignPointerToPointer(Utils.convertToIAST(newRight), left, table, callTable);
			return;
		}

		// Assign pointer to pointer directly
		// Ex1: p1=student.age
		// Ex2: p1 = student[0]
		// Ex3: p1 = p2
		if (right instanceof ICPPASTFieldReference // Ex1
				|| right instanceof ICPPASTArraySubscriptExpression // Ex2
				|| right instanceof CPPASTIdExpression// Ex3
		) {
			String rightStr = right.getRawSignature();
			ISymbolicVariable rightVar = table.findorCreateVariableByName(rightStr);

			if (rightVar != null)
				if (rightVar instanceof PointerSymbolicVariable) {
					/*
					 * p = numbers+3; (p: pointer, numbers: one-dimension array)
					 */
					Reference r = ((PointerSymbolicVariable) rightVar).getReference();

					if (r != null) {
						left.getReference().setBlock(r.getBlock());
						left.getReference().setStartIndex(r.getStartIndex());
					}

				} else if (rightVar instanceof ArraySymbolicVariable) {
					LogicBlock b = ((ArraySymbolicVariable) rightVar).getBlock();

					if (b != null) {
						left.getReference().setBlock(b);
						left.getReference().setStartIndex(index);
					}
				} else
					throw new Exception("Don't support " + left + " = " + right);

		} else if (right instanceof IASTExpression) {
			String reducedRightExpression = right.getRawSignature();

			int location = -1;
			if (right.getRawSignature().contains("+"))
				location = reducedRightExpression.indexOf("+");
			else if (right.getRawSignature().contains("-"))
				location = reducedRightExpression.indexOf("-");

			if (location >= 0) {
				index = reducedRightExpression.substring(location + 1);
				nameRightPointer = reducedRightExpression.substring(0, location);

				/*
				 * If name of pointer put in pair of brackets
				 */
				nameRightPointer = nameRightPointer.replace("(", "").replace(")", "");
				ISymbolicVariable rightVar = table.findorCreateVariableByName(nameRightPointer);

				if (rightVar != null)
					if (rightVar instanceof PointerSymbolicVariable) {
						/*
						 * p = numbers+3; (p: pointer, numbers: one-dimension array)
						 */
						Reference r = ((PointerSymbolicVariable) rightVar).getReference();

						if (r != null) {
							left.getReference().setBlock(r.getBlock());
							left.getReference().setStartIndex(r.getStartIndex() + "+ (" + index + ")");
						}

					} else if (rightVar instanceof OneDimensionSymbolicVariable) {
						LogicBlock b = ((OneDimensionSymbolicVariable) rightVar).getBlock();

						if (b != null) {
							left.getReference().setBlock(b);
							left.getReference().setStartIndex(index);
						}
					} else
						throw new Exception("Don't support " + left + " = " + right);
			}
		}
	}
}
