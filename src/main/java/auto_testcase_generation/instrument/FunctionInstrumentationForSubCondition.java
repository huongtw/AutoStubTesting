package auto_testcase_generation.instrument;

import com.dse.parser.ProjectParser;
import com.dse.parser.object.MacroFunctionNode;
import com.dse.search.Search;
import com.dse.search.condition.MacroFunctionNodeCondition;
import com.dse.testcase_execution.DriverConstant;
import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;
import com.dse.logger.AkaLogger;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTryBlockStatement;

import java.io.File;

/**
 * Instrument function with sub-condition
 *
 * @author
 */
public class FunctionInstrumentationForSubCondition extends AbstractFunctionInstrumentation {
	final static AkaLogger logger = AkaLogger.get(FunctionInstrumentationForSubCondition.class);

	public FunctionInstrumentationForSubCondition(IASTFunctionDefinition astFunctionNode) {
		this.astFunctionNode = astFunctionNode;
	}

	public static void main(String[] args) {
		ProjectParser parser = new ProjectParser(new File("D:\\IdeaProjects\\akautauto_2\\datatest\\hoannv\\check_debug"));
		parser.setExpandTreeuptoMethodLevel_enabled(true);
		MacroFunctionNode function = (MacroFunctionNode) Search.searchNodes(parser.getRootTree(), new MacroFunctionNodeCondition(),
				"dbg.h\\check_debug(A,M,...)").get(0);
		System.out.println(new FunctionInstrumentationForSubCondition(function.convertMacroFunctionToRealFunction(function.getAST()))
				.generateInstrumentedFunction());
	}

	@Override
	public String generateInstrumentedFunction() {
		if (astFunctionNode != null) {
			IASTFunctionDefinition astFunctionNode = (IASTFunctionDefinition) this.astFunctionNode;
			StringBuilder tempStr = new StringBuilder();
			tempStr.append(getShortenContent(astFunctionNode.getDeclSpecifier()))
					.append(SpecialCharacter.SPACE)
					.append(getShortenContent(astFunctionNode.getDeclarator()))
					.append(parseCompoundStatement((IASTCompoundStatement) astFunctionNode.getBody(), null, ""));
			return tempStr.toString();
		} else {
			return "";
		}
	}

	private String parseCompoundStatement(IASTCompoundStatement block, String extra, String margin) {
		StringBuilder markedContent = new StringBuilder();
		markedContent.append(SpecialCharacter.OPEN_BRACE);
		markedContent.append(SpecialCharacter.LINE_BREAK);

		if (extra != null) {
			markedContent.append(margin + SpecialCharacter.TAB);
		}

		for (IASTStatement stm : block.getStatements())
			markedContent.append(margin + SpecialCharacter.TAB)
					.append(parseStatement(stm, margin + SpecialCharacter.TAB))
					.append(SpecialCharacter.LINE_BREAK)
					.append(SpecialCharacter.LINE_BREAK);

		markedContent.append(SpecialCharacter.CLOSE_BRACE);

		return markedContent.toString();
	}

	private String parseStatement(IASTStatement stm, String margin) {
		StringBuilder tempStr = new StringBuilder();
		if (stm instanceof IASTCompoundStatement) {
			tempStr.append(parseCompoundStatement((IASTCompoundStatement) stm, null, margin));

		} else if (stm instanceof IASTIfStatement) {
			IASTIfStatement astIf = (IASTIfStatement) stm;
			IASTStatement astElse = astIf.getElseClause();
			IASTExpression astCond = astIf.getConditionExpression();

			tempStr.append("if (").append(createMarkForSubCondition(astCond)).append(")");

			tempStr.append(addExtraCall(astIf.getThenClause(), "", margin));

			if (astElse != null) {
				tempStr.append(SpecialCharacter.LINE_BREAK).append(margin).append("else ");
				tempStr.append(addExtraCall(astElse, "", margin));
			}
		} else if (stm instanceof IASTForStatement) {
			IASTForStatement astFor = (IASTForStatement) stm;
			IASTStatement astInit = astFor.getInitializerStatement();
			IASTExpression astCond = (IASTExpression) Utils.shortenAstNode(astFor.getConditionExpression());
			IASTExpression astIter = astFor.getIterationExpression();

			if (!(astInit instanceof IASTNullStatement))
				//tempStr.append(mark(esc(ast(astInit)), true)).append(SpecialCharacter.LINE_BREAK).append(margin);
				tempStr.append(createMarkForNormalStatement(astInit, true)).append(SpecialCharacter.LINE_BREAK).append(margin);
			tempStr.append("for (").append(getShortenContent(astInit));

			if (astCond != null)
				tempStr.append(createMarkForSubCondition(astCond));
			tempStr.append("; ");

			if (astIter != null)
				tempStr.append(createMarkForNormalStatement(astIter, false)).append(',');
			tempStr.append(getShortenContent(astIter)).append(") ");

			// Block for does not have condition, e.g., for (int i=0;;)
			if (astCond == null)
				tempStr.append(parseStatement(astFor.getBody(), margin));
			else
				tempStr.append(addExtraCall(astFor.getBody(), "", margin));

		} else if (stm instanceof IASTWhileStatement) {
			IASTWhileStatement astWhile = (IASTWhileStatement) stm;
			IASTExpression astCond = (IASTExpression) Utils.shortenAstNode(astWhile.getCondition());

			tempStr.append("while (").append(createMarkForSubCondition(astCond)).append(")");

			tempStr.append(addExtraCall(astWhile.getBody(), "", margin));

		} else if (stm instanceof IASTDoStatement) {
			IASTDoStatement astDo = (IASTDoStatement) stm;
			IASTExpression astCond = (IASTExpression) Utils.shortenAstNode(astDo.getCondition());

			tempStr.append("do ").append(addExtraCall(astDo.getBody(), "", margin)).append(SpecialCharacter.LINE_BREAK)
					.append(margin).append("while (").append(createMarkForSubCondition(astCond)).append(");");

		} else if (stm instanceof ICPPASTTryBlockStatement) {
			ICPPASTTryBlockStatement astTry = (ICPPASTTryBlockStatement) stm;

			String extra = "start try";
			tempStr.append(createMarkForTryCatch(extra, true));

			tempStr.append(SpecialCharacter.LINE_BREAK).append(margin).append("try ");
			tempStr.append(addExtraCall(astTry.getTryBody(), null, margin));

			for (ICPPASTCatchHandler catcher : astTry.getCatchHandlers()) {
				tempStr.append(SpecialCharacter.LINE_BREAK).append(margin).append("catch (");

				String exception = catcher.isCatchAll() ? "..." : getShortenContent(catcher.getDeclaration());
				tempStr.append(exception).append(") ");

				extra = SpecialCharacter.EMPTY + exception + SpecialCharacter.EMPTY;
				tempStr.append(addExtraCall(catcher.getCatchBody(), extra, margin));
			}

			extra = "end catch";
			tempStr.append(SpecialCharacter.LINE_BREAK).append(margin)
					.append(createMarkForTryCatch(extra, true));

		} else if (stm instanceof IASTBreakStatement || stm instanceof IASTContinueStatement)
			tempStr.append(getShortenContent(stm));
		else {
			String raw = getShortenContent(stm);
			tempStr.append(createMarkForNormalStatement(stm, true)).append(SpecialCharacter.SPACE).append(raw);
		}

		return tempStr.toString();
	}

	private String createMarkForSubCondition(IASTNode astCon) {
		StringBuilder tempStr = new StringBuilder();
		astCon = Utils.shortenAstNode(astCon);
		if (isCondition(astCon)) {
			if (astCon instanceof IASTBinaryExpression) {
				int operator = ((IASTBinaryExpression) astCon).getOperator();

				switch (operator) {
				case IASTBinaryExpression.op_greaterEqual:
				case IASTBinaryExpression.op_greaterThan:
				case IASTBinaryExpression.op_lessEqual:
				case IASTBinaryExpression.op_lessThan:
					tempStr.append("	(").append(astCon)
							.append("&&").append(Utils.shortenAstNode(astCon).getRawSignature()).append(")");
					break;

				case IASTBinaryExpression.op_logicalAnd:
				case IASTBinaryExpression.op_logicalOr:
					IASTExpression operand1 = ((IASTBinaryExpression) astCon).getOperand1();
					IASTExpression operand2 = ((IASTBinaryExpression) astCon).getOperand2();

					tempStr.append("(").append(createMarkForSubCondition(operand1)).append(")")
                            .append(operator == IASTBinaryExpression.op_logicalAnd ? "	&&" : "	||").append("(").append(createMarkForSubCondition(operand2)).append(")");
					break;
				}
			} else {
				// unary expression
				tempStr.append(DriverConstant.MARK + "(\"")
						.append(addContentOfMarkFunction(astCon, astFunctionNode, functionPath, false, true)).
						append("\")&&").
						append(astCon.getRawSignature());
			}
		} else {
			tempStr.append(DriverConstant.MARK + "(\"")
					.append(addContentOfMarkFunction(astCon, astFunctionNode, functionPath, false, true)).
					append("\")&&").
					append(astCon.getRawSignature());
		}

		return tempStr.toString();
	}

	private String addExtraCall(IASTStatement stm, String extra, String margin) {
		if (extra != null)
			extra = createMarkForTryCatch(extra, true);

		if (stm instanceof IASTCompoundStatement)
			return parseCompoundStatement((IASTCompoundStatement) stm, extra, margin);
		else {
			String inside = margin + SpecialCharacter.TAB;
			String b = SpecialCharacter.OPEN_BRACE +
					SpecialCharacter.LINE_BREAK +
					inside + inside + parseStatement(stm, inside) +
					SpecialCharacter.LINE_BREAK + margin +
					SpecialCharacter.CLOSE_BRACE;
			return b;
		}
	}

	private String createMarkForNormalStatement(IASTNode astCon, boolean end) {
		StringBuilder tempStr = new StringBuilder();
		tempStr.append(DriverConstant.MARK + "(\"").
				append(addContentOfMarkFunction(astCon, astFunctionNode, functionPath, false, false)).
				append("\")");

		if (end)
			tempStr.append(';');

		return tempStr.toString();
	}

	private String createMarkForTryCatch(String arg, boolean end) {
		String b = DriverConstant.MARK + "(\"" + arg + "\")";

		if (end)
			b += ';';

		return b;
	}
}