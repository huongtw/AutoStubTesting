package auto_testcase_generation.testdatagen.se;

import auto_testcase_generation.testdatagen.se.memory.*;
import com.dse.environment.Environment;
import com.dse.parser.dependency.finder.Level;
import com.dse.parser.dependency.finder.VariableSearchingSpace;
import com.dse.parser.normalizer.ClassvsStructNormalizer;
import auto_testcase_generation.testdatagen.se.memory.array.multiple_dims.MultipleDimensionSymbolicVariable;
import auto_testcase_generation.testdatagen.se.memory.array.one_dim.OneDimensionSymbolicVariable;
import auto_testcase_generation.testdatagen.se.memory.basic.BasicSymbolicVariable;
import auto_testcase_generation.testdatagen.se.memory.pointer.PointerSymbolicVariable;
import auto_testcase_generation.testdatagen.se.memory.structure.SimpleStructureSymbolicVariable;
import auto_testcase_generation.testdatagen.se.memory.structure.UnionSymbolicVariable;
import auto_testcase_generation.testdatagen.se.normalization.PointerAccessNormalizer;
import auto_testcase_generation.testdatagen.testdatainit.VariableTypes;
import auto_testcase_generation.testdatagen.testdatainit.VariablesSize;
import auto_testcase_generation.testdatagen.testdatainit.VariablesSize.BASIC;
import auto_testcase_generation.utils.ASTUtils;
import auto_testcase_generation.utils.RegexUtils;
import com.dse.parser.object.*;
import com.dse.search.Search;
import com.dse.search.condition.EnumNodeCondition;
import com.dse.search.condition.MacroDefinitionNodeCondition;
import com.dse.search.condition.MacroFunctionNodeCondition;
import com.dse.util.IRegex;
import com.dse.util.Utils;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTBinaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTLiteralExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTUnaryExpression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utils
 * 
 * @author ducanhnguyen
 *
 */
public class ExpressionRewriterUtils {
    /**
     * Shorten expressions <br/>
     * Ex0: "1+1" ---shorten---> "2" <br/>
     * Ex1: "(1+2)+x>0" ---shorten---> "3+x>0" <br/>
     * Ex2: "a[1+2]+x>0" ---shorten---> "a[3]+x>0"
     *
     * @param expression
     * @return
     * @see{ExpressionRewriterUtilsTest.class}
     */
    public static String shortenExpressionByCalculatingValue(String expression) {
        String normalizedExpression = expression;
        final int MAX_NUMBER_OF_REDUCTION = 3;

        for (int i = 0; i < MAX_NUMBER_OF_REDUCTION; i++) {
            // STEP 1. Get all expressions which could be calculated
            IASTNode astExpression = Utils.convertToIAST(normalizedExpression);

            List<IASTNode> astExpressions = new ArrayList<>();
            ASTVisitor visitor = new ASTVisitor() {
                @Override
                public int visit(IASTExpression statement) {
                    if (statement instanceof IASTBinaryExpression || statement instanceof IASTLiteralExpression
                            || statement instanceof IASTIdExpression)
                        astExpressions.add(statement);
                    return PROCESS_CONTINUE;
                }
            };
            visitor.shouldVisitExpressions = true;
            astExpression.accept(visitor);

            // STEP 2. Calculate the value of each expression
            for (IASTNode astExpressionItem : astExpressions) {
                String value;
                value = new CustomJeval().evaluate(astExpressionItem.getRawSignature());

                if (!value.equals(astExpressionItem.getRawSignature())) {
                    normalizedExpression = normalizedExpression.replace(astExpressionItem.getRawSignature(), value);
                    normalizedExpression = normalizedExpression.replace("(" + astExpressionItem.getRawSignature() + ")",
                            value);
                    normalizedExpression = normalizedExpression.replace("[" + astExpressionItem.getRawSignature() + "]",
                            "[" + value + "]");
                }
            }
        }
        return normalizedExpression;
    }

	/**
	 * Convert one-level/two level pointer access to array access
	 *
	 * @see{ExpressionRewriterUtilsTest.class}
	 *
	 * @param expression
	 * @return
	 */
	public static String convertPointerItemToArrayItem(String expression) {
		String normalizedExpression = expression;

        final int MAX_NUMBER_OF_REDUCTION = 3;

        for (int i = 0; i < MAX_NUMBER_OF_REDUCTION; i++) {
            List<ICPPASTUnaryExpression> unaryExpressions = ASTUtils
                    .getUnaryExpressions(Utils.convertToIAST(normalizedExpression));

            for (ICPPASTUnaryExpression unaryExpression : unaryExpressions)
                switch (unaryExpression.getOperator()) {
                    case IASTUnaryExpression.op_star:
                        String oldExp = unaryExpression.getRawSignature();

                        IASTNode firstChild = ASTUtils.shortenAstNode(unaryExpression.getChildren()[0]);
                        if (firstChild instanceof CPPASTIdExpression
                                || firstChild instanceof IASTArraySubscriptExpression) {
                            // Ex: *p1
                            String pointerName = firstChild.getRawSignature();
                            String newExp = pointerName + "[0]";
                            normalizedExpression = normalizedExpression.replace(oldExp, newExp);

                        } else if (firstChild instanceof CPPASTBinaryExpression) {
                            // Ex: *(p2+1 + (1+1))
                            IASTNode pointerAst = ASTUtils.getIds(firstChild).get(0);
                            String pointerName = pointerAst.getRawSignature();

                            String index = firstChild.getRawSignature().replaceFirst(RegexUtils.toRegex(pointerName), "");

                            String newExp = pointerName + "[0" + index + "]";
                            normalizedExpression = normalizedExpression.replace(oldExp, newExp);
                        }
                }
        }
        normalizedExpression = shortenExpressionByCalculatingValue(normalizedExpression);
        return normalizedExpression;

    }

    /**
     * Convert character to its ASCII <br/>
     * Ex: 'a' -----convert---> 97
     *
     * @param expression
     * @return
     */
    public static String convertCharToNumber(String expression) {
        String normalizedExpression = expression;

        final String CHARACTER_BEGINNING = "'";

        // STEP 1. Get all expressions which could be calculated
        IASTNode astExpression = Utils.convertToIAST(normalizedExpression);

        List<CPPASTLiteralExpression> literalExpressions = new ArrayList<>();
        ASTVisitor visitor = new ASTVisitor() {
            @Override
            public int visit(IASTExpression expression) {
                // Ex: 'a'
                if (expression instanceof CPPASTLiteralExpression)
                    if (expression.getRawSignature().startsWith(CHARACTER_BEGINNING)
                            && expression.getRawSignature().endsWith(CHARACTER_BEGINNING))
                        literalExpressions.add((CPPASTLiteralExpression) expression);
                return PROCESS_CONTINUE;
            }
        };
        visitor.shouldVisitExpressions = true;
        astExpression.accept(visitor);

        // STEP 2. Calculate the value of each expression
        for (CPPASTLiteralExpression astExpressionItem : literalExpressions) {
            // Ex: 'a' ------------> 97
            normalizedExpression = normalizedExpression.replace(astExpressionItem.getRawSignature(),
                    Utils.getASCII(
                            astExpressionItem.getRawSignature().replace(CHARACTER_BEGINNING, "").toCharArray()[0])
                            + "");
        }
        return normalizedExpression;

    }

    /**
     * Convert negative number x. For example, (-3) ----convert-----> (-1)*3 <br/>
     *
     * @param expression
     * @return
     * @see{ExpressionRewriterUtilsTest.class}
     */
    public static String tranformNegative(String expression) {
        String normalizedExpression = expression;

        IASTNode expressionAST = Utils.convertToIAST(expression);
        final String NEGATIVE_OPERATOR = "-";

        List<CPPASTUnaryExpression> unaryASTs = new ArrayList<>();
        ASTVisitor visitor = new ASTVisitor() {

            @Override
            public int visit(IASTExpression name) {
                if (name instanceof CPPASTUnaryExpression)
                    // Ex: -3
                    if (name.getRawSignature().startsWith(NEGATIVE_OPERATOR)
                            && name.getChildren()[0] instanceof CPPASTLiteralExpression)
                        unaryASTs.add((CPPASTUnaryExpression) name);
                return ASTVisitor.PROCESS_CONTINUE;
            }
        };

        visitor.shouldVisitExpressions = true;
        expressionAST.accept(visitor);

        for (CPPASTUnaryExpression unaryAST : unaryASTs)
            normalizedExpression = normalizedExpression.replace(unaryAST.getRawSignature(),
                    "(-1)*" + unaryAST.getChildren()[0].getRawSignature());
        return normalizedExpression;
    }

    /**
     * Ex: "size of(int)" ------> "4"
     *
     * @param expression
     * @return
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    public static String transformSizeof(String expression) throws IllegalArgumentException, IllegalAccessException {
        if (expression.contains("size ") || expression.contains("size"))
            for (String type : VariableTypes.getAllBasicFieldNames(BASIC.class)) {
                int size = VariablesSize.getSizeofTypeInByte(type);
                String sizeOf = "size of ( " + type + " )";
                expression = expression.replaceAll(RegexUtils.toRegex(sizeOf), size + "");
            }
        return expression;
    }

    /**
     * Ex:"123E+1"------> "1230"
     *
     * @param expression
     * @return
     */
    public static String transformFloatPositiveE(String expression) {
        Matcher m = Pattern.compile("\\d+E\\+\\d+").matcher(expression);
        while (m.find()) {
            String beforeE = expression.substring(0, expression.indexOf("E+"));
            String afterE = expression.substring(expression.indexOf("E+") + 2);

            StringBuilder newValue = new StringBuilder();

            if (Utils.toInt(afterE) != Utils.UNDEFINED_TO_INT) {
                int numDemicalPoint = Utils.toInt(afterE);

                if (numDemicalPoint == 0) {
                    newValue = new StringBuilder(beforeE);

                } else {
                    newValue = new StringBuilder(beforeE);
                    for (int i = 0; i < numDemicalPoint; i++)
                        newValue.append("0");
                }
            }

            expression = expression.replace(m.group(0), newValue.toString());
        }
        return expression;
    }

    /**
     * Ex: "123E-4"------> "0.0123"
     *
     * @param expression
     * @return
     * @see #{CustomJevalTest.java}
     */
    public static String transformFloatNegativeE(String expression) {
        Matcher m = Pattern.compile("\\d+E-\\d+").matcher(expression);
        while (m.find()) {
            String beforeE = expression.substring(0, expression.indexOf("E-"));
            String afterE = expression.substring(expression.indexOf("E-") + 2);

			StringBuilder newValue = new StringBuilder();

            if (Utils.toInt(afterE) != Utils.UNDEFINED_TO_INT) {
                int numDemicalPoint = Utils.toInt(afterE);

                if (numDemicalPoint == 0) {
                    newValue = new StringBuilder(beforeE);

                } else if (beforeE.length() > numDemicalPoint) {
                    for (int i = 0; i < beforeE.length() - numDemicalPoint; i++)
                        newValue.append(beforeE.toCharArray()[i]);
                    newValue.append(".");

                    for (int i = beforeE.length() - numDemicalPoint; i < beforeE.length(); i++) {
                        newValue.append(beforeE.toCharArray()[i]);
                    }
                } else {
                    newValue.append("0.");
                    for (int i = 0; i <= numDemicalPoint - 1 - beforeE.length(); i++) {
                        newValue.append("0");
                    }
                    newValue.append(beforeE);
                }
            }

            expression = expression.replace(m.group(0), newValue.toString());
        }
        return expression;
    }

    /**
     * Delete .0 in expression
     * <p>
     * Ex: 1.0----------->1
     *
     * @param expression
     * @return
     */
    public static String simplifyFloatNumber(String expression) {
        String normalizedExpression = expression;

        final String FLOAT_SYMBOL_ENDING = ".0";

        // STEP 1. Get all expressions which could be calculated
        IASTNode astExpression = Utils.convertToIAST(normalizedExpression);

        List<CPPASTLiteralExpression> literalExpressions = new ArrayList<>();
        ASTVisitor visitor = new ASTVisitor() {
            @Override
            public int visit(IASTExpression expression) {
                // Ex: 1.0
                if (expression instanceof CPPASTLiteralExpression)
                    if (expression.getRawSignature().endsWith(FLOAT_SYMBOL_ENDING))
                        literalExpressions.add((CPPASTLiteralExpression) expression);
                return PROCESS_CONTINUE;
            }
        };
        visitor.shouldVisitExpressions = true;
        astExpression.accept(visitor);

        // STEP 2. Calculate the value of each expression
        for (CPPASTLiteralExpression astExpressionItem : literalExpressions) {
            // Ex: 1.0 ------------> 1
            normalizedExpression = normalizedExpression.replace(astExpressionItem.getRawSignature(),
                    astExpressionItem.getRawSignature().replace(FLOAT_SYMBOL_ENDING, ""));
        }
        return normalizedExpression;
    }

	/**
	 * Rewrite the expression based on the table of variable.
	 * <p>
	 * <br/>
	 * For example, the expression is "x>2". But in the table, the value of x is
	 * equivalent to 5. So the expression is rewritten as "5>2".
	 * <p>
	 * <br/>
	 * Another example, we have two pointers named p1 and p2. But "p1 = p2 + 1;" (it
	 * means that pointer p1 points to the second element in p2). We have the
	 * expression "*(p1)==*(p2+2)". It will be transformed as "p2[1]==p2[2]".
	 *
	 * @param table
	 * @param expression
	 * @return
	 * @throws Exception
	 */
	public static String rewrite(IVariableNodeTable table, String expression) throws Exception {
		expression = ExpressionRewriterUtils.removeBracketedPrimary(expression);
		expression = ExpressionRewriterUtils.transformFloatPositiveE(expression);
		expression = ExpressionRewriterUtils.transformFloatNegativeE(expression);
		expression = ExpressionRewriterUtils.convertCharToNumber(expression);
		expression = ExpressionRewriterUtils.transformSizeof(expression);
		expression = ExpressionRewriterUtils.rewritePointer(expression);

        String rExcludingSingleQuote = "[^']";
        expression = expression.replaceAll(ExpressionRewriterUtils.group(rExcludingSingleQuote) + IRegex.SPACES, "$1");

        expression = ExpressionRewriterUtils.shortenExpressionInParentheses(expression);
        expression = ExpressionRewriterUtils.tranformNegative(expression);
//		expression = ExpressionRewriterUtils.convertNullToZero(expression);

        // Some small other normalizers here
        // Character '\0' (end of char*) ==> ASCII = 0
        expression = expression.replace("'\\0'", "0");
        expression = expression.replace("\\s*true\\s*", "1");
        expression = expression.replace("\\s*false\\s*", "0");

        /*
         * For all variables in the table
         */
        for (ISymbolicVariable symbolicVariable : table.getVariables()) {
            if (expression.contains(symbolicVariable.getName())) {
                /*
                 * If the variable belongs to basic type, we replace its name with its
                 * corresponding value
                 */
                if (symbolicVariable instanceof BasicSymbolicVariable) {
                    expression = ExpressionRewriterUtils
                            .replaceBasicSymbolicVariable((BasicSymbolicVariable) symbolicVariable, expression, table);
                } else {
                    /*
                     * If the variable type belongs to one dimension, Ex: int[2]
                     */
                    if (symbolicVariable instanceof OneDimensionSymbolicVariable) {
                        expression = ExpressionRewriterUtils.replaceOneDimensionSymbolicVariable(
                                (OneDimensionSymbolicVariable) symbolicVariable, expression);
                    } else {
                        /*
                         * If the variable type belongs to one level pointer, Ex: int*
                         */
                        if (symbolicVariable instanceof PointerSymbolicVariable
                                && ((PointerSymbolicVariable) symbolicVariable).getLevel() == 1) {
                            expression = ExpressionRewriterUtils
                                    .replaceOneLevelSymbolicVariable((PointerSymbolicVariable) symbolicVariable, expression);
                        } else {
                            /*
                             * If the variable type belongs to structure (class, struct, union)
                             */
                            if (symbolicVariable instanceof SimpleStructureSymbolicVariable) {
                                expression = ExpressionRewriterUtils.replaceSimpleStructureSymbolicVariable(
                                        (SimpleStructureSymbolicVariable) symbolicVariable, expression);
                            } else if (symbolicVariable instanceof PointerSymbolicVariable) {
                                // TODO: multiple level
                                expression = ExpressionRewriterUtils.replaceTwoLevelSymbolicVariable(
                                        (PointerSymbolicVariable) symbolicVariable, expression, table);
                            } else if (symbolicVariable instanceof MultipleDimensionSymbolicVariable) {
                                // TODO: multiple dim
                                expression = ExpressionRewriterUtils.replaceTwoDimensionSymbolicVariable(
                                        (MultipleDimensionSymbolicVariable) symbolicVariable, expression);
                            } else {
                                // dont handle
                            }
                        }
                    }
                }
            }
        }

        expression = shortenExpressionInBracket(expression);
        expression = replaceEnum(expression);
        expression = new CustomJeval().evaluate(expression);
        expression = rewriteLiteral(expression);
        expression = rewriteComplexArrayIndex(table, expression);
        expression = convertFieldToNormalizedField(Utils.convertToIAST(expression));

        return expression;
    }

    private static String rewriteComplexArrayIndex(IVariableNodeTable table, String expression) {
        String[] container = new String[] {expression};

	    IASTNode ast = Utils.convertToIAST(expression);

        ASTVisitor visitor = new ASTVisitor() {
            @Override
            public int visit(IASTExpression expr) {
                if (expr instanceof IASTArraySubscriptExpression) {
                    IASTArraySubscriptExpression astArrayExpr = (IASTArraySubscriptExpression) expr;
                    IASTInitializerClause astArrayIndex = astArrayExpr.getArgument();
                    if (!(astArrayIndex instanceof IASTLiteralExpression)) {
                        try {
                            String prevArrayIndex = astArrayIndex.getRawSignature();
                            String newArrayIndex = rewrite(table, prevArrayIndex);
                            String regex = Utils.toRegex(prevArrayIndex);
                            regex = "\\b" + regex + "\\b";
                            container[0] = container[0].replaceAll(regex, newArrayIndex);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                return super.visit(expr);
            }
        };

        visitor.shouldVisitExpressions = true;
        ast.accept(visitor);

        return container[0];
	}

    public static String rewriteLiteral(String expression) {
        IASTNode ast = Utils.convertToIAST(expression);
		final String[] container = {expression};

		ASTVisitor visitor = new ASTVisitor() {
			@Override
			public int visit(IASTExpression expression) {
				if (expression instanceof IASTLiteralExpression) {
					String newExpr = Utils.preprocessorLiteral(expression.getRawSignature());
					container[0] = container[0].replaceAll("\\b" + expression.getRawSignature() + "\\b", newExpr);
				}
				return super.visit(expression);
			}
		};
		visitor.shouldVisitExpressions = true;
		ast.accept(visitor);

		return container[0];
	}

	/**
	 * Ex: "!(trie->root_node != NULL)" ---> "!(trie[0].root_node != NULL)"
	 * @param str
	 * @return
	 */
	public static String convertFieldToNormalizedField(IASTNode str){
		String content = str.getRawSignature();

		List<IASTFieldReference> fieldReferences = ASTUtils.getFieldReferences(str);
		for (IASTFieldReference field : fieldReferences) {
			String replacement;
			String owner = field.getFieldOwner().getRawSignature();
			String attr = field.getFieldName().getRawSignature();
            if (field.isPointerDereference()) {
                replacement = owner + "[0]." + attr;
            } else {
                replacement = owner + "." + attr;
            }

            content = content.replace(field.getRawSignature(), replacement);
        }

		List<IASTUnaryExpression> starExprs = ASTUtils.getStarExpressions(Utils.convertToIAST(content));
		for (IASTUnaryExpression expr : starExprs) {
		    String replacement = expr.getRawSignature();
		    IASTNode ast = expr.getOperand();
		    while (true) {
		        if (ast instanceof IASTBinaryExpression) {
		            if (((IASTBinaryExpression) ast).getOperator() == IASTBinaryExpression.op_plus) {
		                IASTExpression operand1 = ((IASTBinaryExpression) ast).getOperand1();
                        IASTExpression operand2 = ((IASTBinaryExpression) ast).getOperand2();
                        if (operand1 instanceof IASTLiteralExpression) {
                            replacement = operand2.getRawSignature() + String.format("[%s]", operand1.getRawSignature());
                        } else if (operand2 instanceof IASTLiteralExpression) {
                            replacement = operand1.getRawSignature() + String.format("[%s]", operand2.getRawSignature());
                        } else {
                            replacement = "(" + ast.getRawSignature() + ")" + "[0]";
                        }
                    } else {
                        replacement = "(" + ast.getRawSignature() + ")" + "[0]";
                    }
		            break;
                } else if (ast instanceof IASTUnaryExpression) {
                    if (((IASTUnaryExpression) ast).getOperator() == IASTUnaryExpression.op_bracketedPrimary)
                        ast = ((IASTUnaryExpression) ast).getOperand();
                    else {
                        replacement = ast.getRawSignature() + "[0]";
                        break;
                    }
                } else {
		            replacement = ast.getRawSignature() + "[0]";
		            break;
                }
		    }

            content = content.replace(expr.getRawSignature(), replacement);
        }

		return content;
	}

	private static String replaceEnum(String newConstraint) {
        //replace enum with normal index of array
        List<String[]> enumItemList = new ArrayList<>();
        //find all enum related to containing file
        List<INode> uutList = Environment.getInstance().getUUTs();
        List<Level> dependentFileLevel = new ArrayList<>();
        for (INode iNode : uutList) {
            dependentFileLevel.addAll(new VariableSearchingSpace(iNode).getSpaces());
        }
        for (Level fileLevel : dependentFileLevel) {
            List<EnumNode> enumlist = Search.searchNodes(fileLevel.get(0), new EnumNodeCondition());
            for (EnumNode enumNode : enumlist){
                enumItemList.addAll(enumNode.getAllEnumItems());
            }
        }
        for (String[] item : enumItemList) {
            if (newConstraint.contains(item[0]))
                newConstraint = newConstraint.replaceAll("\\b\\s*"+item[0]+"\\s*\\b", item[1]);
        }
        return newConstraint;
    }

	private static String replaceBasicSymbolicVariable(BasicSymbolicVariable var, String expression,
			IVariableNodeTable table) throws Exception {
		String value = var.getSymbolicValue();

        if (expression.startsWith("&")) {
            /*
             * &a is ignored
             */
            expression = expression.replace("&", " ahahah ");

            /*
             * Replace the name of variable with its corresponding value
             */
            expression = expression.replaceAll("\\b" + var.getName() + "\\b", "(" + var.getSymbolicValue() + ")");

            expression = expression.replace(" ahahah ", "&");

        } else if (expression.startsWith("*")) {
            /*
             * Ex: this case happens when parsing statement "int z = *px;"
             */
            expression = ExpressionRewriterUtils.convertOneLevelPointerItemToArrayItem(expression);
            expression = ExpressionRewriterUtils.rewrite(table, expression);
        } else {
            expression = expression.replaceAll("\\b" + var.getName() + "\\b", "(" + var.getSymbolicValue() + ")");
        }
        return expression;
    }

    private static String replaceOneDimensionSymbolicVariable(OneDimensionSymbolicVariable var, String expression) {
        /*
         * Replace the name of variable with the name of its block
         */
        String newName = var.getBlock().getName();
        expression = expression.replaceAll("\\b" + var.getName() + "\\b", newName);

        for (LogicCell item : var.getBlock()) {
            String index = item.getIndex();
            String fullNameItem = newName + IRegex.SPACES + IRegex.OPENING_BRACKET + IRegex.SPACES + index
                    + IRegex.SPACES + IRegex.CLOSING_BRACKET + IRegex.SPACES;
            expression = expression.replaceAll(fullNameItem, item.getPhysicalCell().getValue());
        }

        return expression;

    }

    private static String replaceSimpleStructureSymbolicVariable(SimpleStructureSymbolicVariable var, String expression)
            throws Exception {
//        if (var instanceof UnionSymbolicVariable) {
//            /*
//             * Notice that all variables in union variable have the same value at every
//             * time.
//             */
//            UnionNode unionNode = (UnionNode) var.getNode();
//            List<IVariableNode> attributes = unionNode.getAttributes();
//
//            final String DELIMITER = ClassvsStructNormalizer.DELIMITER;
//            /*
//             * Replace all attributes in the expression with the first attribute because the
//             * value of all attributes in union instance is the same
//             */
//            if (attributes.size() > 0) {
//                String newName = var.getName() + DELIMITER + attributes.get(0).getName();
//                for (IVariableNode attribute : attributes) {
//                    String currentName = var.getName() + DELIMITER + attribute.getName();
//                    expression = expression.replace(currentName, newName);
//                }
//            }
//        } else
        if (var instanceof SimpleStructureSymbolicVariable) {
            SimpleStructureSymbolicVariable cast = var;
            for (ISymbolicVariable castItem : cast.getAttributes()) {
                if (castItem instanceof BasicSymbolicVariable) {
                    if (expression.contains(var.getName() + "." + castItem.getName())) {
                        expression = expression.replace(var.getName() + "." + castItem.getName(),
                                ((BasicSymbolicVariable) castItem).getSymbolicValue());
                    }
                }
            }
        } else {
            throw new Exception("Don't support this type: " + var.getType());
        }
        return expression;

    }

	private static String replaceTwoLevelSymbolicVariable(PointerSymbolicVariable var, String expression,
			IVariableNodeTable table) {

        if (var.getReference() != null) {
            Reference ref = var.getReference();
            expression = ExpressionRewriterUtils.convertTwoLevelPointerItemToArrayItem(expression);

            for (LogicCell logicCell : ref.getBlock()) {
                String realNameCell = var.getName() + logicCell.getIndex();
                expression = expression.replaceAll(Utils.toRegex(realNameCell), logicCell.getPhysicalCell().getValue());
            }

            /*
             * If the pointer does not point to any block, we also replace its variable name
             * with alias.
             *
             * The position of pointer is updated in addition to the start index
             */
            expression = expression.replaceAll("\\b" + Utils.toRegex(var.getName() + "["),
                    ref.getBlock().getName() + "[" + ref.getStartIndex() + "+");
        } else {
            /*
             * In this case, the pointer does not point to any location. We only replace the
             * name of pointer with it alias!
             */
            String nameVarRegex = "\\b" + var.getName() + "\\b";
            String symbolicNameVar = ISymbolicVariable.PREFIX_SYMBOLIC_VALUE + var.getName();
            expression = expression.replaceAll(nameVarRegex, symbolicNameVar);
        }
        return expression;
    }

    private static String replaceTwoDimensionSymbolicVariable(MultipleDimensionSymbolicVariable var, String expression) {
        /*
         * Replace the name of variable with the name of its block
         */
        String newName = var.getBlock().getName();
        expression = expression.replaceAll("\\b" + var.getName() + "\\b", newName);

        for (LogicCell logicCell : var.getBlock()) {
            String index = logicCell.getIndex();
            String oldName = newName + IRegex.SPACES + Utils.toRegex(index) + IRegex.SPACES;
            expression = expression.replaceAll(oldName, logicCell.getPhysicalCell().getValue());
        }

        return expression;
    }

    private static String replaceOneLevelSymbolicVariable(PointerSymbolicVariable var, String expression) {
        if (var.getReference() != null) {
            String newName = var.getReference().getBlock().getName();
            expression = ExpressionRewriterUtils.convertOneLevelPointerItemToArrayItem(expression);

            for (LogicCell item : var.getReference().getBlock())
                /*
                 * In case: *p1==2 (where p1 is pointer, and a is primitive variable; we assign
                 * p1=&a before).
                 */
                if (VariableTypes.isBasic(var.getType())) {
                    String oldIndex = "0";

                    String oldItemName = "\\b" + var.getName() + IRegex.SPACES + IRegex.OPENING_BRACKET + IRegex.SPACES
                            + oldIndex + IRegex.SPACES + IRegex.CLOSING_BRACKET + IRegex.SPACES;

                    String newItemName = newName;

                    expression = expression.replaceAll(oldItemName, newItemName);

                    expression = expression.replaceAll(newItemName, item.getPhysicalCell().getValue());

                } else if (VariableTypes.isOneLevel(var.getType()) || VariableTypes.isTwoLevel(var.getType())) {
                    String oldIndex = item.getIndex();

                    String newIndex = item.getIndex() + "+" + var.getReference().getStartIndex();

                    String oldItemName = "\\b" + var.getName() + IRegex.SPACES + IRegex.OPENING_BRACKET + IRegex.SPACES
                            + oldIndex + IRegex.SPACES + IRegex.CLOSING_BRACKET + IRegex.SPACES;

                    String newItemName = newName + "[" + newIndex + "]";

                    expression = expression.replaceAll(oldItemName, newItemName);

                    expression = expression.replace(newItemName, "(" + item.getPhysicalCell().getValue() + ")");
                } else {
                    // nothing to to
                }

            /*
             * If the pointer does not point to any block, we also replace it variable name
             * with alias.
             *
             * The position of pointer is updated in addition with the start index.
             *
             *
             */
            String oldItemName = "\\b" + var.getName() + "\\b";

            expression = expression.replaceAll(oldItemName + IRegex.SPACES + IRegex.OPENING_BRACKET,
                    var.getReference().getBlock().getName() + "[" + var.getReference().getStartIndex() + "+");

            expression = expression.replaceAll(oldItemName, var.getReference().getBlock().getName());
        } else {
            /*
             * In this case, the pointer does not point to any location. We only replace the
             * name of pointer with it alias!
             */
            String oldItemName = "\\b" + var.getName() + "\\b";

            expression = expression.replaceAll(oldItemName, ISymbolicVariable.PREFIX_SYMBOLIC_VALUE + var.getName());
        }
        return expression;
    }

    /**
     * Convert pointer access to array access
     *
     * @param expression Expression that the index of pointer does not put in pairs or
     *                   parentheses. Ex: p1[0] is available. But p1[0+(1+0)] is
     *                   unavailable
     * @return
     */
    public static String convertOneLevelPointerItemToArrayItem(String expression) {
        PointerAccessNormalizer norm = new PointerAccessNormalizer();
        norm.setOriginalSourcecode(expression);
        norm.normalize();
        return norm.getNormalizedSourcecode();
    }

    /**
     * Check later
     *
     * @param expression
     * @return
     */
    public static String convertTwoLevelPointerItemToArrayItem(String expression) {
        String expectClosingBracket = "[^\\)]*";
        String regex = IRegex.CLOSING_PARETHENESS + IRegex.POINTER + IRegex.OPENING_PARETHENESS
                + ExpressionRewriterUtils.group(IRegex.NAME_REGEX)
                + ExpressionRewriterUtils.group("[+-]" + expectClosingBracket) + IRegex.CLOSING_PARETHENESS;
        expression = expression.replaceAll(regex, "($1[0$2]");

        /*
         * **P2 --> P2[0][0]
         */
        expression = expression.replaceAll("^" + IRegex.POINTER + IRegex.POINTER + "(" + IRegex.NAME_REGEX + ")",
                "$1[0][0]");
        expression = expression.replaceAll("\\(" + IRegex.POINTER + IRegex.POINTER + "(" + IRegex.NAME_REGEX + ")",
                "($1[0][0]");

        /*
         * **(P2) --> P2[0][0]
         */
        expression = expression.replaceAll("([^a-zA-Z0-9_\\)\\]])" + IRegex.POINTER + IRegex.POINTER
                        + IRegex.OPENING_PARETHENESS + "(" + IRegex.NAME_REGEX + ")" + IRegex.CLOSING_PARETHENESS,
                "$1$2[0][0]");
        expression = expression.replaceAll("^" + IRegex.POINTER + IRegex.POINTER + "\\(([a-zA-Z0-9_]+)\\)", "$1[0][0]");
        expression = expression.replaceAll(
                "([^a-zA-Z0-9_\\)\\]])" + IRegex.POINTER + IRegex.POINTER + "(" + IRegex.NAME_REGEX + ")",
                "$1$2[0][0]");

        return expression;
    }

    private static String group(String s) {
        return "(" + s + ")";
    }

    /**
     * Shorten expression in pair of brackets <br/>
     * We have expression "a[1+2]+x>0" ---shorten---> "a[3]+x>0"
     *
     * @param expression
     * @return
     */
    public static String shortenExpressionInBracket(String expression) {
        Pattern pattern = Pattern.compile(IRegex.EXPRESSION_IN_BRACKET);
        Matcher matcher = pattern.matcher(expression);
        if (matcher.find()) {
            String str = matcher.group(1);
            String value = new CustomJeval().evaluate(str);
            expression = expression.replace(str, (int) Double.parseDouble(value) + "");
        }

        return expression;
    }

    /**
     * Shorten expression in pair of parentheses <br/>
     * We have expression "(1+2)+x>0" ---shorten---> "3+x>0"
     *
     * @param expression
     * @return
     */
    public static String shortenExpressionInParentheses(String expression) {
        Map<String, String> tokens = new HashMap<>();
        /*
         * The number of iteration is very important. For example, we have
         * "(1+(1+2))>x". In this case, we need at least two iterations to shorten this
         * expression as much as possible.
         *
         * In this first iteration, the expression becomes "(1+3)>x". And also after the
         * second iteration, we get the final expression "4>x".
         *
         * By default, the value of maximum iterations should not be small.
         */
        final int MAX_ITERATION = 4;
        for (int i = 0; i < MAX_ITERATION; i++) {
            Pattern pattern = Pattern.compile(IRegex.EXPRESSION_IN_PARETHENESS);
            Matcher matcher = pattern.matcher(expression);
            if (matcher.find()) {
                String str = matcher.group(0);
                String value = new CustomJeval().evaluate(str);

                /*
                 * Ex: transform negative expressison into another expression, e.g., "(-1.1)"
                 */
                final String NEGATIVE = "-";
                if (value.startsWith(NEGATIVE)) {
                    String replacement = tokens.size() + "@@@";
                    tokens.put("(" + value + ")", replacement);
                    expression = expression.replace(str, replacement);
                } else
                    expression = expression.replace(str, value);
            }
        }
        /*
         * Restore tokens
         */
        for (String key : tokens.keySet())
            expression = expression.replace(tokens.get(key), key);
        return expression;
    }

	public static String removeBracketedPrimary(String expression){
		IASTNode ast = Utils.convertToIAST(expression);
		final String[] content = {ast.getRawSignature()};
		ASTVisitor astVisitor = new ASTVisitor() {
			@Override
			public int visit(IASTExpression expression) {
				StringBuilder newContent = new StringBuilder(expression.getRawSignature());
				String oldContent = expression.getRawSignature();
				if (expression instanceof IASTUnaryExpression && ((IASTUnaryExpression) expression).getOperator() == 11) {
					IASTNode child = ((IASTUnaryExpression) expression).getOperand();
					if(child instanceof IASTUnaryExpression && ((IASTUnaryExpression) child).getOperator() == 11){
						newContent.deleteCharAt(0);
						newContent.deleteCharAt(newContent.length()-1);
					}
					content[0] = content[0].replace(oldContent,newContent);
				}
				return super.visit(expression);
			}
		};
		astVisitor.shouldVisitExpressions = true;
		ast.accept(astVisitor);
		return content[0];
	}

	public static String rewritePointer(String expression){
		IASTNode ast = Utils.convertToIAST(expression);
		return recursiveInRewritePointer(ast);
	}

//    private static String recursiveInRewritePointer(IASTNode ast){
//        if(ast instanceof IASTUnaryExpression){
//            if(((IASTUnaryExpression) ast).getOperator() == 4){
//                if(((IASTUnaryExpression) ast).getOperand() instanceof IASTUnaryExpression && ((IASTUnaryExpression) ((IASTUnaryExpression) ast).getOperand()).getOperator()==11){
//                    if(((IASTUnaryExpression) ((IASTUnaryExpression) ast).getOperand()).getOperand() instanceof IASTBinaryExpression && ((IASTBinaryExpression) ((IASTUnaryExpression) ((IASTUnaryExpression) ast).getOperand()).getOperand()).getOperator()==4){
//                        IASTNode node = ((IASTUnaryExpression) ((IASTUnaryExpression) ast).getOperand()).getOperand();
//                        return recursiveInRewritePointer(((IASTBinaryExpression)node).getOperand1())+"["+recursiveInRewritePointer(((IASTBinaryExpression)node).getOperand2())+"]";
//                    }
//                }
//                return recursiveInRewritePointer(((IASTUnaryExpression) ast).getOperand())+"[0]";
//            }
//            else if (((IASTUnaryExpression) ast).getOperator() == 7){
//                return "!"+recursiveInRewritePointer(((IASTUnaryExpression) ast).getOperand());
//            }
//            //TODO : review doan nay
//            else if(((IASTUnaryExpression) ast).getOperator() == 11){
//                return "("+recursiveInRewritePointer(((IASTUnaryExpression) ast).getOperand())+")";
//            }
//            else return recursiveInRewritePointer(((IASTUnaryExpression) ast).getOperand());
//
//        }
//        else if(ast instanceof IASTBinaryExpression){
//            if(((IASTBinaryExpression) ast).getOperator() == 4)
//                return recursiveInRewritePointer(((IASTBinaryExpression) ast).getOperand1())+" + "+recursiveInRewritePointer(((IASTBinaryExpression) ast).getOperand2());
//            else if(((IASTBinaryExpression) ast).getOperator() == 28)
//                return recursiveInRewritePointer(((IASTBinaryExpression) ast).getOperand1())+" == "+recursiveInRewritePointer(((IASTBinaryExpression) ast).getOperand2());
//            return ast.getRawSignature();
//        }
////		else if(ast instanceof IASTFieldReference){
////
////		}
//        else{
//            return ast.getRawSignature();
//        }
//    }

	private static String recursiveInRewritePointer(IASTNode ast){

		final String[] content = {ast.getRawSignature()};
		ASTVisitor astVisitor = new ASTVisitor() {
			@Override
			public int visit(IASTExpression expression) {
				if (expression instanceof IASTUnaryExpression && ((IASTUnaryExpression) expression).getOperator()==4) {
					String newContent = null;
					String oldContent = expression.getRawSignature();;
					IASTNode child = ((IASTUnaryExpression) expression).getOperand();
					if(child instanceof IASTUnaryExpression && ((IASTUnaryExpression) child).getOperator() == 11){
						IASTNode child1 = ((IASTUnaryExpression) child).getOperand();
						if(child1 instanceof IASTBinaryExpression && ((IASTBinaryExpression) child1).getOperator()== 4){
							newContent = ((IASTBinaryExpression) child1).getOperand1().getRawSignature()+"["+((IASTBinaryExpression) child1).getOperand2().getRawSignature()+"]";
						}else{
							newContent = child1.getRawSignature()+"[0]";
						}
					}
					else{
						newContent = child.getRawSignature() + "[0]";
					}

					if(newContent!=null) {
						content[0] = content[0].replace(oldContent, newContent);
					}
				}
				return super.visit(expression);
			}
		};
		astVisitor.shouldVisitExpressions = true;
		ast.accept(astVisitor);
		return content[0];
	}

    public static String rewriteMacro(ICommonFunctionNode functionNode, String stm) {
        List<Level> space = new VariableSearchingSpace(functionNode).getSpaces();

        // rewrite macro definition
        List<MacroDefinitionNode> macroDefinitionNodes = Search.searchInSpace(space, new MacroDefinitionNodeCondition());

        boolean isReplaced;
        do {
            isReplaced = false;
            for (MacroDefinitionNode macro : macroDefinitionNodes) {
                String oldType = macro.getOldType();
                String newType = macro.getNewType();
                if (!oldType.trim().isEmpty() && stm.contains(newType)) {
                    stm = stm.replace(newType, oldType);
                    macroDefinitionNodes.remove(macro);
                    isReplaced = true;
                    break;
                }
            }
        } while (isReplaced && !macroDefinitionNodes.isEmpty());

        // rewrite macro function
        List<MacroFunctionNode> macroFunctionNodes = Search.searchInSpace(space, new MacroFunctionNodeCondition());
        do {
            isReplaced = false;
            for (MacroFunctionNode func : macroFunctionNodes) {
                String funcName = func.getSimpleName();
                if (stm.contains(funcName)) {
                    stm = rewriteMacroFunction(stm, func);
                    macroFunctionNodes.remove(func);
                    isReplaced = true;
                    break;
                }
            }
        } while (isReplaced && !macroFunctionNodes.isEmpty());

        return stm;
    }

    private static String rewriteMacroFunction(String stm, MacroFunctionNode macroFunc) {
        String funcName = macroFunc.getSimpleName();
        String body = macroFunc.getAST().getExpansion();
        String[] paramNames = macroFunc.getArguments().stream()
                .map(INode::getName)
                .toArray(String[]::new);
        return rewriteMacroFunction(stm, funcName, paramNames, body);
    }

    private static String rewriteMacroFunction(String stm, String funcName, String[] paramNames, String originBody) {
        String rewriteStm = stm;

        String pattern = funcName + IRegex.SPACES + IRegex.OPENING_PARETHENESS
                + IRegex.ARGUMENT_LIST
                + IRegex.CLOSING_PARETHENESS;

        // Create a pattern object
        Pattern r = Pattern.compile(pattern);

        // Now create matcher object.
        Matcher m = r.matcher(rewriteStm);

        while (m.find()) {
            String functionCall = m.group(0);

            String body = originBody;

            IASTNode ast = Utils.convertToIAST(functionCall);
            if (ast instanceof IASTFunctionCallExpression) {
                IASTInitializerClause[] arguments = ((IASTFunctionCallExpression) ast).getArguments();
                if (arguments.length == paramNames.length) {
                    for (int i = 0; i < arguments.length; i++) {
                        String regex = Utils.toRegex(paramNames[i]);
                        body = body.replaceAll(regex, arguments[i].getRawSignature());
                    }
                }
            } else if (ast instanceof IASTDeclarationStatement
                    && ((IASTDeclarationStatement) ast).getDeclaration() instanceof IASTSimpleDeclaration) {
                IASTDeclarator[] declarators = ((IASTSimpleDeclaration)
                        ((IASTDeclarationStatement) ast).getDeclaration()
                ).getDeclarators();
                if (declarators.length > 0 && paramNames.length == 1) {
                    String argument = declarators[0].getNestedDeclarator().getRawSignature();
                    String regex = Utils.toRegex(paramNames[0]);
                    body = body.replaceAll(regex, argument);
                }
            }

            rewriteStm = rewriteStm.replace(functionCall, body);
            m = r.matcher(rewriteStm);
        }

        return rewriteStm;
    }

}