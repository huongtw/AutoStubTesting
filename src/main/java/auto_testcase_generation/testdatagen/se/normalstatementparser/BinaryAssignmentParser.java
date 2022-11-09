package auto_testcase_generation.testdatagen.se.normalstatementparser;

import auto_testcase_generation.testdatagen.se.memory.*;
import auto_testcase_generation.testdatagen.se.memory.pointer.PointerSymbolicVariable;
import com.dse.util.IRegex;
import com.dse.util.Utils;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTBinaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCastExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNewExpression;

import auto_testcase_generation.testdatagen.se.ExpressionRewriterUtils;
import auto_testcase_generation.testdatagen.se.memory.array.one_dim.OneDimensionSymbolicVariable;

/**
 * Parse binary assignment statement. <br/>
 * Ex1: p1 = p2 +1<br/>
 * Ex2: p = &a <br/>
 * Ex3: p = &numbers[2]<br/>
 * Ex4: x = a + 1<br/>
 * Ex5: Right expression contains malloc, calloc, called function, new, etc. (x
 * = checkPrime(4))
 *
 * @author ducanhnguyen
 */
public class BinaryAssignmentParser extends AssignmentParser {

	@Override
	public void parse(IASTNode ast, VariableNodeTable table, FunctionCallTable callTable) throws Exception {
		ast = Utils.shortenAstNode(ast);

		if (ast instanceof IASTBinaryExpression) {
			IASTBinaryExpression binaryAST = (IASTBinaryExpression) ast;
			int operator = binaryAST.getOperator();

			switch (operator) {
				case IASTBinaryExpression.op_assign: {
					int type = getTypeOfBinaryAssignment(binaryAST);

					switch (type) {
						case NEW:
							new NewBinaryAssignmentParser().parse(binaryAST, table, callTable);
							break;

						// Ex: "s = (int*)malloc(sizeof(int)*n);"
						case MALLOC:
							String nameVar = ((IASTBinaryExpression) ast).getOperand1().getRawSignature();
							ISymbolicVariable var = table.findorCreateVariableByName(nameVar);

							int mallocIndex = binaryAST.getRawSignature().indexOf("malloc(");
							// Ex: size ="sizeof(int)*n"
							String sizeInStr = binaryAST.getRawSignature().substring(
									mallocIndex + "malloc(".length(), binaryAST.getRawSignature().length() - 1);
							sizeInStr = ExpressionRewriterUtils.rewrite(table, sizeInStr);

							if (sizeInStr.matches(IRegex.POSITIVE_INTEGER_REGEX)) {
								LogicBlock block;
								if (var instanceof PointerSymbolicVariable)
									block = ((PointerSymbolicVariable) var).getReference().getBlock();

								else if (var instanceof OneDimensionSymbolicVariable)
									block = ((OneDimensionSymbolicVariable) var).getBlock();
								else
									throw new Exception("Dont support " + ast.getRawSignature());

								if (block != null)
									for (int i = 0; i < Utils.toInt(sizeInStr); i++) {
										PhysicalCell newCell = new PhysicalCell(PhysicalCell.DEFAULT_VALUE);
										String index = i + "";
										block.addLogicalCell(newCell, index);
									}
							} else {
								if (var instanceof PointerSymbolicVariable)
									((PointerSymbolicVariable) var).setSize(sizeInStr);
								else
									throw new Exception("Dont support " + ast.getRawSignature());
							}

							break;

						case CALLOC:
							throw new Exception("Dont support calloc in: " + binaryAST.getRawSignature());

						case NORMAL_ASSIGNMENT:
							new NormalBinaryAssignmentParser().parse(binaryAST, table, callTable);
							break;

						// Ex: a=b=c=d=e
						case MULTIPLE_ASSIGNMENTS:
							new MultipleAssignmentParser().parse(binaryAST, table, callTable);
							break;

						default:
							throw new Exception("Dont support " + ast.getRawSignature());
						}

					break;
				}
				case IASTBinaryExpression.op_multiplyAssign:
				case IASTBinaryExpression.op_divideAssign:
				case IASTBinaryExpression.op_moduloAssign:
				case IASTBinaryExpression.op_plusAssign:
				case IASTBinaryExpression.op_minusAssign:
					new ShortAssignmentParser().parse(binaryAST, table, callTable);
					break;

				default:
					throw new Exception("Dont support assignment" + binaryAST.getRawSignature());
			}
		}
	}

	/**
	 * Get type of binary assignment
	 *
	 * @param ast
	 * @return
	 */
	public int getTypeOfBinaryAssignment(IASTBinaryExpression ast) {
		IASTExpression right = ast.getOperand2();

		if (right != null) {
			/*
			 * Ex: a = new int[10]
			 */
			final String MALLOC_SIGNAL = "malloc";
			final String CALLOC_SIGNAL = "calloc";

			// Ex: s = (int*)malloc(sizeof(int)*n);
			if (right instanceof IASTCastExpression) {
				// Get "malloc(sizeof(int)*n)"
				IASTNode tmp = right.getChildren()[1];
				if (tmp.getRawSignature().startsWith(MALLOC_SIGNAL))
					return MALLOC;
				else if (tmp.getRawSignature().startsWith(CALLOC_SIGNAL))
					return CALLOC;
				else
					return AssignmentParser.UNSPECIFIED_ASSIGNMENT;
			} else if (right instanceof ICPPASTNewExpression)
				return AssignmentParser.NEW;
			else if (right.getRawSignature().startsWith(MALLOC_SIGNAL))
				return AssignmentParser.MALLOC;
			else if (right.getRawSignature().startsWith(CALLOC_SIGNAL))
				return AssignmentParser.CALLOC;
			else {
				/*
				 * Ex: a=b=c+1
				 */
				String rightSide = ast.getOperand2().getRawSignature();
				if (rightSide.contains("="))
					return AssignmentParser.MULTIPLE_ASSIGNMENTS;
				else
					return AssignmentParser.NORMAL_ASSIGNMENT;
			}
		} else {
			/*
			 * Ex: double fx_vec[N_MAX] = { -0.5772156649015329E+00,
			 * -0.4237549404110768E+00, -0.2890398965921883E+00, -0.1691908888667997E+00,
			 * -0.6138454458511615E-01, 0.3648997397857652E-01, 0.1260474527734763E+00,
			 * 0.2085478748734940E+00, 0.2849914332938615E+00, 0.3561841611640597E+00,
			 * 0.4227843350984671E+00 };
			 */
			IASTInitializerClause initialization = ast.getInitOperand2();
			if (initialization != null)
				return AssignmentParser.NORMAL_ASSIGNMENT;
		}
		return AssignmentParser.UNSPECIFIED_ASSIGNMENT;
	}
}
