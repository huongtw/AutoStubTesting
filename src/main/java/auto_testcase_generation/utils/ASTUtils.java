package auto_testcase_generation.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;
import com.dse.util.UtilsVu;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.DefaultLogService;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.dom.parser.cpp.*;

public class ASTUtils {

	public static IASTTranslationUnit getIASTTranslationUnit(char[] source, String filePath,
			Map<String, String> macroList, ILanguage lang) {
		FileContent reader = FileContent.create(filePath, source);
		String[] includeSearchPaths = new String[0];
		IScannerInfo scanInfo = new ScannerInfo(macroList, includeSearchPaths);
		IncludeFileContentProvider fileCreator = IncludeFileContentProvider.getSavedFilesProvider();
		int options = ILanguage.OPTION_IS_SOURCE_UNIT;
		IParserLogService log = new DefaultLogService();

		try {
			return lang.getASTTranslationUnit(reader, scanInfo, fileCreator, null, options, log);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Generate declaration statement
	 * @param elements [0] return type
	 *                 [1] name
	 *                 [3] default value
	 * @return IASTDeclarationStatement
	 */
	public static IASTDeclarationStatement generateDeclarationStatement(String... elements) {
		IASTDeclarationStatement normalize = null;

		if (elements.length > 0) {
			String type = elements[0];

			String name = "__name__";
			if (elements.length >= 2)
				name = elements[1];

			String value = SpecialCharacter.EMPTY;
			if (elements.length >= 3)
				value = elements[2];

			String tempDeclare = type + SpecialCharacter.SPACE + name;
			if (!value.isEmpty()) {
				tempDeclare += " = " + value;
			}
			IASTNode tempAST = Utils.convertToIAST(tempDeclare);
			if (tempAST instanceof IASTDeclarationStatement)
				normalize = ((IASTDeclarationStatement) tempAST);
		}

		return normalize;
	}

	public static IASTBinaryExpression generateBinaryExpression(String operator, String operand1, String operand2) {
		IASTBinaryExpression expr = null;

		String stm = operand1 + operator + operand2;

		IASTNode temp = Utils.convertToIAST(stm);
		if (temp instanceof IASTBinaryExpression)
			expr = (IASTBinaryExpression) temp;

		return expr;
	}


	/**
	 * In cÃ¢y cáº¥u trÃºc ra mÃ n hÃ¬nh
	 */
	public static void printTree(IASTNode n, String s) {
		String content = n.getRawSignature().replaceAll("[\r\n]", "");
		IASTNode[] child = n.getChildren();
		System.out.println(s + content + ": " + n.getClass().getSimpleName());
		for (IASTNode c : child)
			ASTUtils.printTree(c, s + "   ");

	}

	public static void printTree(String path) {
		try {
			File file = new File(path);
			String content = UtilsVu.getContentFile(file);
			ILanguage lang = file.getName().toLowerCase().endsWith(".c") ? GCCLanguage.getDefault()
					: GPPLanguage.getDefault();
			IASTTranslationUnit u = ASTUtils.getIASTTranslationUnit(content.toCharArray(), path, null, lang);

			ASTUtils.printTree(u, " | ");
		} catch (Exception e) {

		}
	}

	
	/**
	 * Print an ASTNode out the screen <br/>
	 * Example: From the ASTNode of this function: <br/>
	 * 
	 * <pre>
	 * int class_test1(SinhVien sv) {
	 * 	if ((1.0 + 2) > x)
	 * 		return true;
	 * 	if (1 + 2 > x + 1 + 3)
	 * 		return true;
	 * }
	 * </pre>
	 * 
	 * We have the content of ASTNode:
	 * 
	 * <pre>
	 | int class_test1(SinhVien sv){	if ((1.0+2)>x)		return true;	if (1+2>x+1+3)		return true;}: CPPASTTranslationUnit
	 |    int class_test1(SinhVien sv){	if ((1.0+2)>x)		return true;	if (1+2>x+1+3)		return true;}: CPPASTFunctionDefinition
	 |       int: CPPASTSimpleDeclSpecifier
	 |       class_test1(SinhVien sv): CPPASTFunctionDeclarator
	 |          class_test1: CPPASTName
	 |          SinhVien sv: CPPASTParameterDeclaration
	 |             SinhVien: CPPASTNamedTypeSpecifier
	 |                SinhVien: CPPASTName
	 |             sv: CPPASTDeclarator
	 |                sv: CPPASTName
	 |       {	if ((1.0+2)>x)		return true;	if (1+2>x+1+3)		return true;}: CPPASTCompoundStatement
	 |          if ((1.0+2)>x)		return true;: CPPASTIfStatement
	 |             (1.0+2)>x: CPPASTBinaryExpression
	 |                (1.0+2): CPPASTUnaryExpression
	 |                   1.0+2: CPPASTBinaryExpression
	 |                      1.0: CPPASTLiteralExpression
	 |                      2: CPPASTLiteralExpression
	 |                x: CPPASTIdExpression
	 |                   x: CPPASTName
	 |             return true;: CPPASTReturnStatement
	 |                true: CPPASTLiteralExpression
	 |          if (1+2>x+1+3)		return true;: CPPASTIfStatement
	 |             1+2>x+1+3: CPPASTBinaryExpression
	 |                1+2: CPPASTBinaryExpression
	 |                   1: CPPASTLiteralExpression
	 |                   2: CPPASTLiteralExpression
	 |                x+1+3: CPPASTBinaryExpression
	 |                   x+1: CPPASTBinaryExpression
	 |                      x: CPPASTIdExpression
	 |                         x: CPPASTName
	 |                      1: CPPASTLiteralExpression
	 |                   3: CPPASTLiteralExpression
	 |             return true;: CPPASTReturnStatement
	 |                true: CPPASTLiteralExpression
	 * 
	 * </pre>
	 */
	public static void printTreeFromAstNode(IASTNode n, String tab) {
		String content = n.getRawSignature().replaceAll("[\r\n]", "");
		IASTNode[] child = n.getChildren();
		System.out.println(tab + content + ": " + n.getClass().getSimpleName());
		for (IASTNode c : child)
			ASTUtils.printTreeFromAstNode(c, tab + "   ");

	}

	/**
	 * Print content of abstract tree from source code <br/>
	 * Example: From the ASTNode of this function: <br/>
	 * 
	 * <pre>
	 * int class_test1(SinhVien sv) {
	 * 	if ((1.0 + 2) > x)
	 * 		return true;
	 * 	if (1 + 2 > x + 1 + 3)
	 * 		return true;
	 * }
	 * </pre>
	 * 
	 * We have the content of ASTNode:
	 * 
	 * <pre>
	 | int class_test1(SinhVien sv){	if ((1.0+2)>x)		return true;	if (1+2>x+1+3)		return true;}: CPPASTTranslationUnit
	 |    int class_test1(SinhVien sv){	if ((1.0+2)>x)		return true;	if (1+2>x+1+3)		return true;}: CPPASTFunctionDefinition
	 |       int: CPPASTSimpleDeclSpecifier
	 |       class_test1(SinhVien sv): CPPASTFunctionDeclarator
	 |          class_test1: CPPASTName
	 |          SinhVien sv: CPPASTParameterDeclaration
	 |             SinhVien: CPPASTNamedTypeSpecifier
	 |                SinhVien: CPPASTName
	 |             sv: CPPASTDeclarator
	 |                sv: CPPASTName
	 |       {	if ((1.0+2)>x)		return true;	if (1+2>x+1+3)		return true;}: CPPASTCompoundStatement
	 |          if ((1.0+2)>x)		return true;: CPPASTIfStatement
	 |             (1.0+2)>x: CPPASTBinaryExpression
	 |                (1.0+2): CPPASTUnaryExpression
	 |                   1.0+2: CPPASTBinaryExpression
	 |                      1.0: CPPASTLiteralExpression
	 |                      2: CPPASTLiteralExpression
	 |                x: CPPASTIdExpression
	 |                   x: CPPASTName
	 |             return true;: CPPASTReturnStatement
	 |                true: CPPASTLiteralExpression
	 |          if (1+2>x+1+3)		return true;: CPPASTIfStatement
	 |             1+2>x+1+3: CPPASTBinaryExpression
	 |                1+2: CPPASTBinaryExpression
	 |                   1: CPPASTLiteralExpression
	 |                   2: CPPASTLiteralExpression
	 |                x+1+3: CPPASTBinaryExpression
	 |                   x+1: CPPASTBinaryExpression
	 |                      x: CPPASTIdExpression
	 |                         x: CPPASTName
	 |                      1: CPPASTLiteralExpression
	 |                   3: CPPASTLiteralExpression
	 |             return true;: CPPASTReturnStatement
	 |                true: CPPASTLiteralExpression
	 * 
	 * </pre>
	 */
	public static void printTreeFromSourcecodeFile(String path) {
		try {
			File file = new File(path);

			if (file.exists()) {
				String content = UtilsVu.getContentFile(file);
				ILanguage lang = file.getName().toLowerCase().endsWith(".c") ? GCCLanguage.getDefault()
						: GPPLanguage.getDefault();
				IASTTranslationUnit u = ASTUtils.getIASTTranslationUnit(content.toCharArray(), path, null, lang);

				ASTUtils.printTreeFromAstNode(u, " | ");
			} else
				throw new Exception("File " + path + " does not exist");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get all functions in the given source code
	 *
	 * @param sourcecode
	 *            Source code contains several functions
	 * @return
	 */
	public static List<ICPPASTFunctionDefinition> getFunctionsinAST(char[] sourcecode) {
		List<ICPPASTFunctionDefinition> output = new ArrayList<>();

		try {
			IASTTranslationUnit unit = ASTUtils.getIASTTranslationUnitforCpp(sourcecode);

			if (unit.getChildren()[0] instanceof CPPASTProblemDeclaration)
				unit = ASTUtils.getIASTTranslationUnitforC(sourcecode);

			ASTVisitor visitor = new ASTVisitor() {
				@Override
				public int visit(IASTDeclaration declaration) {
					if (declaration instanceof ICPPASTFunctionDefinition) {
						output.add((ICPPASTFunctionDefinition) declaration);
						return ASTVisitor.PROCESS_SKIP;
					}
					return ASTVisitor.PROCESS_CONTINUE;
				}
			};

			visitor.shouldVisitDeclarations = true;

			unit.accept(visitor);
		} catch (Exception e) {

		}
		return output;
	}

	public static IASTTranslationUnit getIASTTranslationUnitforC(char[] code) throws Exception {
		File filePath = new File("");
		FileContent fc = FileContent.create(filePath.getAbsolutePath(), code);
		Map<String, String> macroDefinitions = new HashMap<>();
		String[] includeSearchPaths = new String[0];
		IScannerInfo si = new ScannerInfo(macroDefinitions, includeSearchPaths);
		IncludeFileContentProvider ifcp = IncludeFileContentProvider.getEmptyFilesProvider();
		IIndex idx = null;
		int options = ILanguage.OPTION_IS_SOURCE_UNIT;
		IParserLogService log = new DefaultLogService();
		return GCCLanguage.getDefault().getASTTranslationUnit(fc, si, ifcp, idx, options, log);
	}

	public static IASTTranslationUnit getIASTTranslationUnitforCpp(char[] code) throws Exception {
		File filePath = new File("");
		FileContent fc = FileContent.create(filePath.getAbsolutePath(), code);
		Map<String, String> macroDefinitions = new HashMap<>();
		String[] includeSearchPaths = new String[0];
		IScannerInfo si = new ScannerInfo(macroDefinitions, includeSearchPaths);
		IncludeFileContentProvider ifcp = IncludeFileContentProvider.getEmptyFilesProvider();
		IIndex idx = null;
		int options = ILanguage.OPTION_IS_SOURCE_UNIT;
		IParserLogService log = new DefaultLogService();
		return GPPLanguage.getDefault().getASTTranslationUnit(fc, si, ifcp, idx, options, log);
	}



	/**
	 * Get all unary expression
	 * <p>
	 * Ex: "x=(a++) +1+ (--b)" -------> unary expression: {"a++", "--b}
	 *
	 * @param ast
	 * @return
	 */
	public static List<ICPPASTUnaryExpression> getUnaryExpressions(IASTNode ast) {
		List<ICPPASTUnaryExpression> unaryExpressions = new ArrayList<>();

		ASTVisitor visitor = new ASTVisitor() {

			@Override
			public int visit(IASTExpression name) {
				if (name instanceof ICPPASTUnaryExpression) {
					unaryExpressions.add((ICPPASTUnaryExpression) name);
				}
				return ASTVisitor.PROCESS_CONTINUE;
			}
		};

		visitor.shouldVisitExpressions = true;

		ast.accept(visitor);
		return unaryExpressions;
	}

	/**
	 * Shorten ast node. <br/>
	 * Ex:"(a)" -----> "a" <br/>
	 * Ex: "(!a)" --------> "!a"
	 *
	 * @param ast
	 * @return
	 */
	public static IASTNode shortenAstNode(IASTNode ast) {
		IASTNode tmp = ast;
		/*
		 * Ex:"(a)" -----> "a"
		 *
		 * Ex: "(!a)" --------> !a
		 */
		while ((tmp instanceof CPPASTExpressionStatement || tmp instanceof ICPPASTUnaryExpression
				&& tmp.getRawSignature().startsWith("(") && tmp.getRawSignature().endsWith(")"))
				&& tmp.getChildren().length == 1 && !tmp.getRawSignature().startsWith("!"))
			tmp = tmp.getChildren()[0];

		return tmp;
	}

	/**
	 * Get all expression in the assignment. Ex: "x=y=z+1"---->{x, y, z+1} in order
	 * of left side to right side
	 *
	 * @param binaryAST
	 */
	public static List<String> getAllExpressionsInBinaryExpression(IASTBinaryExpression binaryAST) {
		List<String> expression = new ArrayList<>();
		IASTNode tmpAST = binaryAST;

		while (tmpAST instanceof IASTBinaryExpression) {
			IASTNode firstChild = tmpAST.getChildren()[0];
			expression.add(firstChild.getRawSignature());

			IASTNode secondChild = tmpAST.getChildren()[1];
			tmpAST = secondChild;
		}
		expression.add(tmpAST.getRawSignature());

		return expression;
	}

	/**
	 * Find ast by the content of condition
	 *
	 * @param name
	 *            the content of condition
	 * @param ast
	 *            the ast of source code containing the condition
	 * @return
	 */
	public static IASTNode findFirstConditionByName(String name, IASTNode ast) {
		ASTVisitor visitor = new ASTVisitor() {
			@Override
			public int visit(IASTStatement statement) {
				if (statement instanceof IASTIfStatement) {
					IASTNode con = ((IASTIfStatement) statement).getConditionExpression();

					if (con.getRawSignature().equals(name)) {
						Utils.output = con;
						return ASTVisitor.PROCESS_ABORT;
					}
				} else if (statement instanceof IASTWhileStatement) {
					IASTNode con = ((IASTWhileStatement) statement).getCondition();
					if (con.getRawSignature().equals(name)) {
						Utils.output = con;
						return ASTVisitor.PROCESS_ABORT;
					}
				} else if (statement instanceof IASTDoStatement) {
					IASTNode con = ((IASTDoStatement) statement).getCondition();
					if (con.getRawSignature().equals(name)) {
						Utils.output = con;
						return ASTVisitor.PROCESS_ABORT;
					}
				} else if (statement instanceof IASTSwitchStatement) {
					// TODO: xu ly
				}
				return ASTVisitor.PROCESS_CONTINUE;
			}
		};
		visitor.shouldVisitStatements = true;
		visitor.shouldVisitExpressions = true;
		ast.accept(visitor);

		return Utils.output;
	}

	public static List<IASTUnaryExpression> getStarExpressions(IASTNode ast) {
		List<IASTUnaryExpression> astList = new ArrayList<>();

		ASTVisitor visitor = new ASTVisitor() {

			@Override
			public int visit(IASTExpression name) {
				if (name instanceof IASTUnaryExpression
						&& ((IASTUnaryExpression) name).getOperator() == IASTUnaryExpression.op_star){
					astList.add((IASTUnaryExpression) name);
				}
				return ASTVisitor.PROCESS_CONTINUE;
			}
		};

		visitor.shouldVisitExpressions = true;

		ast.accept(visitor);
		return astList;
	}

	public static List<IASTFieldReference> getFieldReferences(IASTNode ast) {
		List<IASTFieldReference> astList = new ArrayList<>();

		ASTVisitor visitor = new ASTVisitor() {

			@Override
			public int visit(IASTExpression name) {
				if (name instanceof IASTFieldReference)
					astList.add((IASTFieldReference) name);
//				if (name instanceof IASTUnaryExpression
//						&& ((IASTUnaryExpression) name).getOperator() == IASTUnaryExpression.op_star){
//					astList.add(name);
//				}
				return ASTVisitor.PROCESS_CONTINUE;
			}
		};

		visitor.shouldVisitExpressions = true;

		ast.accept(visitor);
		return astList;
	}

	/**
	 * Get all binary expressions
	 *
	 * @param ast
	 * @return
	 */
	public static List<ICPPASTBinaryExpression> getBinaryExpressions(IASTNode ast) {
		List<ICPPASTBinaryExpression> binaryASTs = new ArrayList<>();

		ASTVisitor visitor = new ASTVisitor() {

			@Override
			public int visit(IASTExpression name) {
				if (name instanceof ICPPASTBinaryExpression)
					binaryASTs.add((ICPPASTBinaryExpression) name);
				return ASTVisitor.PROCESS_CONTINUE;
			}
		};

		visitor.shouldVisitExpressions = true;

		ast.accept(visitor);
		return binaryASTs;
	}

	/**
	 * Get all declarations in the given ast
	 *
	 * @param ast
	 * @return
	 */
	public static List<IASTSimpleDeclaration> getSimpleDeclarations(IASTNode ast) {
		List<IASTSimpleDeclaration> declarationASTs = new ArrayList<>();

		ASTVisitor visitor = new ASTVisitor() {

			@Override
			public int visit(IASTDeclaration name) {
				if (name instanceof IASTSimpleDeclaration)
					declarationASTs.add((IASTSimpleDeclaration) name);
				return ASTVisitor.PROCESS_CONTINUE;
			}
		};

		visitor.shouldVisitDeclarations = true;

		ast.accept(visitor);
		return declarationASTs;
	}

	public static List<ICPPASTLiteralExpression> getLiteralExpressions(IASTNode ast) {
		List<ICPPASTLiteralExpression> literalASTs = new ArrayList<>();

		ASTVisitor visitor = new ASTVisitor() {

			@Override
			public int visit(IASTExpression name) {
				if (name instanceof ICPPASTLiteralExpression)
					literalASTs.add((ICPPASTLiteralExpression) name);
				return ASTVisitor.PROCESS_CONTINUE;
			}
		};

		visitor.shouldVisitExpressions = true;

		ast.accept(visitor);
		return literalASTs;
	}

	/**
	 * Láº¥y danh sÃ¡ch id trong má»™t node AST
	 *
	 * @param ast
	 * @return
	 */
	public static List<CPPASTIdExpression> getIds(IASTNode ast) {
		List<CPPASTIdExpression> ids = new ArrayList<>();

		ASTVisitor visitor = new ASTVisitor() {
			@Override
			public int visit(IASTExpression expression) {
				if (expression instanceof CPPASTIdExpression)
					ids.add((CPPASTIdExpression) expression);
				return ASTVisitor.PROCESS_CONTINUE;
			}
		};

		visitor.shouldVisitExpressions = true;

		ast.accept(visitor);
		return ids;
	}

	public static <T extends IASTArraySubscriptExpression> List<T> getArraySubscriptExpression(IASTNode ast) {
		List<T> ids = new ArrayList<>();

		ASTVisitor visitor = new ASTVisitor() {
			@Override
			public int visit(IASTExpression expression) {
				if (expression instanceof ICPPASTArraySubscriptExpression) {
					ids.add((T) expression);

					if (expression.getChildren()[0] instanceof ICPPASTArraySubscriptExpression)
						return ASTVisitor.PROCESS_SKIP;
					else
						return ASTVisitor.PROCESS_CONTINUE;
				} else
					return ASTVisitor.PROCESS_CONTINUE;
			}
		};

		visitor.shouldVisitExpressions = true;

		ast.accept(visitor);
		return ids;
	}

	/**
	 * Ex: (a>0 && a==1)
	 *
	 * @param ast
	 * @return
	 */
	public static boolean isMultipleCodition(IASTNode ast) {
		if (ast instanceof ICPPASTUnaryExpression)
			return true;
		else {
			int operator = ((CPPASTBinaryExpression) ast).getOperator();
			switch (operator) {
				case IASTBinaryExpression.op_logicalAnd:
				case IASTBinaryExpression.op_logicalOr:
				case IASTBinaryExpression.op_binaryAnd:
					return true;
				default:
					return false;
			}
		}
	}

	/**
	 * Check whether the statement is assignment or not
	 *
	 * @param binaryExpression
	 * @return
	 */
	public static boolean isBinaryAssignment(IASTBinaryExpression binaryExpression) {
		switch (binaryExpression.getOperator()) {
			case IASTBinaryExpression.op_assign:
			case IASTBinaryExpression.op_multiplyAssign:
			case IASTBinaryExpression.op_divideAssign:
			case IASTBinaryExpression.op_moduloAssign:
			case IASTBinaryExpression.op_plusAssign:
			case IASTBinaryExpression.op_minusAssign:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Ex: a==0
	 */
	public static boolean isSingleCodition(IASTNode ast) {
		String content = ast.getRawSignature();
		String[] SINGLE_CONDITIONS = new String[] { "&&", "||" };
		for (String singleCondition : SINGLE_CONDITIONS)
			if (content.contains(singleCondition))
				return false;

		return true;
	}

	/**
	 * Check whether the statement is condition or not
	 *
	 * @param ast
	 * @return
	 */
	public static boolean isCondition(ICPPASTBinaryExpression ast) {
		switch (ast.getOperator()) {
			case IASTBinaryExpression.op_greaterEqual:
			case IASTBinaryExpression.op_greaterThan:
			case IASTBinaryExpression.op_lessEqual:
			case IASTBinaryExpression.op_lessThan:
			case IASTBinaryExpression.op_equals:
			case IASTBinaryExpression.op_notequals:

			case IASTBinaryExpression.op_logicalAnd:
			case IASTBinaryExpression.op_logicalOr:
				return true;
			default:
				return false;
		}

	}
}
