package auto_testcase_generation.testdatagen.se.solver;

import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;
import com.dse.util.VariableTypeUtils;
import org.eclipse.cdt.core.dom.ast.*;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author anhanh
 */
public class SmtLibv2Normalizer2 extends SmtLibv2Normalizer {

	public SmtLibv2Normalizer2(String expression) {
		originalSourcecode = expression;
	}

	@Override
	public void normalize() {
		IASTNode ast = Utils.convertToIAST(originalSourcecode);
		if (!(ast instanceof IASTExpression)) {
			ast = Utils.convertToIAST(String.format("(%s)", originalSourcecode));
		}
		normalizeSourcecode = createSmtLib(ast);

		// Ex: "(1.2)" ----------->"1.2"
		normalizeSourcecode = normalizeSourcecode.replaceAll("\\(([a-zA-Z0-9_\\.]+)\\)", "$1");

//		normalizeSourcecode = convertToCompare(normalizeSourcecode);
	}

	public static void main(String[] args) {
		SmtLibv2Normalizer2 normalizer = new SmtLibv2Normalizer2("!!((mTEMPDSPA)!=0)");
		normalizer.normalize();
		System.out.println(normalizer.getOriginalSourcecode());
		System.out.println(normalizer.getNormalizedSourcecode());
	}


//	/**
//	 * Convert if (cond) --> if (cond == 1)
//	 * 		   if (!cond) --> if (cond == 0)
//	 * @param expr condition expression
//	 * @return normalized condition
//	 */
//	private String convertToCompare(String expr) {
//		boolean isNegative = expr.contains("(not ");
//
//		String core = expr.replaceFirst("\\bnot\\b", SpecialCharacter.EMPTY)
//				.trim()
//				.replaceAll("\\(\\s*([a-zA-Z0-9_\\.]+)\\s*\\)", "$1")
//				.trim();
//
//		IASTNode ast = Utils.convertToIAST(core);
//		if (ast instanceof IASTIdExpression || ast instanceof IASTFieldReference) {
//			if (isNegative) {
//				return String.format(" (= 0 %s) ", core);
//			} else {
//				return String.format(" (= 1 %s) ", core);
//			}
//		}
//
//		return expr;
//	}

	protected String createSmtLib(IASTNode ast) {
		StringBuilder normalizeSc = new StringBuilder();

		if (ast.getRawSignature().equals("NULL")) {
			normalizeSc = new StringBuilder("0");

		} else if (ast.getRawSignature().equals(NEGATIVE_ONE)) {
			normalizeSc = new StringBuilder("- 1");

		} else if (ast instanceof IASTName || ast instanceof IASTIdExpression
				|| ast instanceof IASTLiteralExpression || ast instanceof IASTFieldReference) {

			boolean isBool = false;

			IASTNode cur = ast.getParent();
			while (cur != null) {
				if (cur instanceof IASTUnaryExpression) {
					int op = ((IASTUnaryExpression) cur).getOperator();
					if (op == IASTUnaryExpression.op_bracketedPrimary) {
						cur = cur.getParent();
					} else if (op == IASTUnaryExpression.op_not) {
						isBool = true;
						break;
					} else {
						break;
					}
				} else if (cur instanceof IASTBinaryExpression) {
					int op = ((IASTBinaryExpression) cur).getOperator();
					if (op == IASTBinaryExpression.op_logicalAnd || op == IASTBinaryExpression.op_logicalOr) {
						isBool = true;
						break;
					} else {
						break;
					}
				} else {
					break;
				}
			}

			if (isBool)
				normalizeSc = new StringBuilder("not (= " + ast.getRawSignature() + " 0)");
			else
				normalizeSc = new StringBuilder(ast.getRawSignature());

		}
		// cast nhưng parse sai ra function call
		else if (ast instanceof IASTFunctionCallExpression
				&& ((IASTFunctionCallExpression) ast).getArguments().length == 1) {
			IASTExpression funcNameAST = ((IASTFunctionCallExpression) ast).getFunctionNameExpression();
			if (funcNameAST instanceof IASTUnaryExpression) {
				if (((IASTUnaryExpression) funcNameAST).getOperator() == IASTUnaryExpression.op_bracketedPrimary) {
					funcNameAST = ((IASTUnaryExpression) funcNameAST).getOperand();
				}
			}
			String funcName = funcNameAST.getRawSignature();
			if (VariableTypeUtils.isNumBasic(funcName)) {
				return createSmtLib(((IASTFunctionCallExpression) ast).getArguments()[0]);
			}
		} else {
			// STEP 1. Shorten expression
			boolean isNegate = false;
			boolean isUnaryExpression = ast instanceof IASTUnaryExpression;

			int count = 0;
			while (ast instanceof IASTUnaryExpression) {
				if (++count > 10)
					break; // to avoid infinite loop
				IASTUnaryExpression astUnary = (IASTUnaryExpression) ast;
				IASTExpression astUnaryOperand = astUnary.getOperand();

				switch (astUnary.getOperator()) {
					case IASTUnaryExpression.op_plus:
						case IASTUnaryExpression.op_bracketedPrimary:
							ast = astUnaryOperand;
						break;
					case IASTUnaryExpression.op_minus:
						ast = Utils.convertToIAST(NEGATIVE_ONE + "*(" + astUnaryOperand.getRawSignature() + ")");
						break;
					case IASTUnaryExpression.op_prefixIncr:
						ast = Utils.convertToIAST("1+ " + astUnaryOperand.getRawSignature());
						break;
					case IASTUnaryExpression.op_prefixDecr:
						ast = Utils.convertToIAST(astUnaryOperand.getRawSignature() + "-1");
						break;
						case IASTUnaryExpression.op_not:
						isNegate = !isNegate;
						ast = astUnaryOperand;
						break;
					case IASTUnaryExpression.op_star: {
						ast = Utils.convertToIAST(astUnaryOperand.getRawSignature() + "[0]");
						break;
					}
					default: {
						break;
					}
				}
			}

			// STEP 2. Get operator
			String operator = "";
			if (isUnaryExpression) {
				if (isNegate) {
					operator = "not";
					normalizeSc = new StringBuilder(String.format("%s %s", operator, createSmtLib(ast)));
				} else {
					normalizeSc = new StringBuilder(String.format("%s", createSmtLib(ast)));
				}

			} else if (ast instanceof IASTBinaryExpression) {
				IASTBinaryExpression astBinary = (IASTBinaryExpression) ast;
				switch (astBinary.getOperator()) {
				case IASTBinaryExpression.op_divide:
					operator = "div"; // integer division
					break;
				case IASTBinaryExpression.op_minus:
					operator = "-";
					break;
				case IASTBinaryExpression.op_plus:
					operator = "+";
					break;
				case IASTBinaryExpression.op_multiply:
					operator = "*";
					break;
				case IASTBinaryExpression.op_modulo:
					operator = "mod";
					break;

				case IASTBinaryExpression.op_greaterEqual:
					operator = ">=";
					break;
				case IASTBinaryExpression.op_greaterThan:
					operator = ">";
					break;
				case IASTBinaryExpression.op_lessEqual:
					operator = "<=";
					break;
				case IASTBinaryExpression.op_lessThan:
					operator = "<";
					break;
				case IASTBinaryExpression.op_equals:
					operator = "=";
					break;
				case IASTBinaryExpression.op_notequals:
					operator = "!=";
					break;

				case IASTBinaryExpression.op_logicalAnd:
					operator = "and";
					break;
				case IASTBinaryExpression.op_logicalOr:
					operator = "or";
					break;
				}

				if (operator.length() > 0)
					if (operator.equals("!="))
						if ((astBinary.getOperand1().getRawSignature().equals("NULL"))) {
							normalizeSc = new StringBuilder(String.format("(> %s 0)",
									createSmtLib(astBinary.getOperand2())));

						} else if (astBinary.getOperand2().getRawSignature().equals("NULL")) {
							normalizeSc = new StringBuilder(String.format("(> %s 0)",
									createSmtLib(astBinary.getOperand1())));

						} else
							normalizeSc = new StringBuilder(String.format("not (= %s %s)",
									createSmtLib(astBinary.getOperand1()),
									createSmtLib(astBinary.getOperand2())));
					else
						normalizeSc = new StringBuilder(String.format("%s %s %s", operator,
								createSmtLib(astBinary.getOperand1()),
								createSmtLib(astBinary.getOperand2())));

			} else if (ast instanceof IASTArraySubscriptExpression) {
				// Get all elements in array item
				List<IASTNode> elements = new ArrayList<>();

				int countWhile = 0;
				while (ast.getChildren().length > 1) {
					if (countWhile++ > 10)
						break;// to avoid infinite loop
					elements.add(0, ast.getChildren()[1]);
					ast = ast.getChildren()[0];
				}
				elements.add(ast);

				IASTNode astName = elements.get(elements.size() - 1);
				normalizeSc = new StringBuilder(astName.getRawSignature());

				for (int i = elements.size() - 2; i >= 0; i--)
					normalizeSc.append(createSmtLib(elements.get(i)));

			} else if (ast instanceof IASTCastExpression) {
				ast = ((IASTCastExpression) ast).getOperand();
				normalizeSc.append(createSmtLib(ast));
			}
		}

		normalizeSc = new StringBuilder(checkInBracket(normalizeSc.toString()) ? normalizeSc.toString() : " (" + normalizeSc + ") ");
		return normalizeSc.toString();
	}

	private boolean checkInBracket(String stm) {
		stm = stm.trim();
		if (stm.startsWith("(")) {
			int count = 0;
			for (Character c : stm.toCharArray())
				if (c == '(')
					count++;
				else if (c == ')')
					count--;
			return count == 0;
		} else
			return false;

	}

	private static final String NEGATIVE_ONE = "(-1)";
}
