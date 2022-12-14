package auto_testcase_generation.instrument;

import com.dse.parser.ProjectParser;
import com.dse.parser.object.IFunctionNode;
import com.dse.parser.object.ProjectNode;
import com.dse.search.Search;
import com.dse.search.condition.FunctionNodeCondition;
import com.dse.testcase_execution.DriverConstant;
import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTryBlockStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionCallExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDefinition;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Extend the previous instrumentation function by adding extra information
 * (e.g., the line of statements) to markers. <br/>
 * Ex: int a = 0; ----instrument-----> mark("line 12:int a = 0"); int a = 0;
 * <p>
 * <br/>
 *
 * @author DucAnh
 */
@Deprecated
public class FunctionInstrumentationForStatementvsBranch_Marker extends AbstractFunctionInstrumentation {

    private IFunctionNode functionNode;
    private IASTFunctionDefinition astFunctionNode;

    public FunctionInstrumentationForStatementvsBranch_Marker(IFunctionNode fn) {
        functionNode = fn;
    }

    public static void main(String[] args) throws Exception {
        ProjectParser parser = new ProjectParser(new File("/home/hoannv/IdeaProjects/akautauto/datatest/duc-anh/Algorithm2"));
        parser.setExpandTreeuptoMethodLevel_enabled(true);
        parser.setCpptoHeaderDependencyGeneration_enabled(true);

        ProjectNode root = parser.getRootTree();
        IFunctionNode function = (IFunctionNode) Search
                .searchNodes(root, new FunctionNodeCondition(), "uninit_var(int[3],int[3])").get(0);
        System.out.println("Original function:\n" + function.getAST().getRawSignature());

        FunctionInstrumentationForStatementvsBranch_Marker fnIns = new FunctionInstrumentationForStatementvsBranch_Marker(function);
        String instrument = fnIns.generateInstrumentedFunction();
        System.out.println("instrument = " + instrument);
    }

    @Override
    public String generateInstrumentedFunction() {
        // We have two ways to pass ast: directly or indirectly via functionNode
        if (astFunctionNode != null && functionNode == null)
            return instrument(astFunctionNode);
        else if (astFunctionNode == null && functionNode != null) {
            this.astFunctionNode = functionNode.getAST();
            return instrument(functionNode.getAST());
        }
        else
            return "";
    }

    protected String addExtraCall(IASTStatement stm, String extra, String margin) {
        if (extra != null)
            extra = putInMark(extra, true);

        if (stm instanceof IASTCompoundStatement)
            return parseBlock((IASTCompoundStatement) stm, extra, margin);
        else {
            String inside = margin + SpecialCharacter.TAB;

            String b = SpecialCharacter.OPEN_BRACE + SpecialCharacter.LINE_BREAK + inside + inside +
                    parseStatement(stm, inside) + SpecialCharacter.LINE_BREAK + margin +
                    SpecialCharacter.CLOSE_BRACE;
            return b;
        }
    }

    protected String instrument(IASTFunctionDefinition astFunctionNode) {
        String b = getShortenContent(astFunctionNode.getDeclSpecifier()) + SpecialCharacter.SPACE +
                getShortenContent(astFunctionNode.getDeclarator()) +
                parseBlock((IASTCompoundStatement) astFunctionNode.getBody(), null, "");
        return b;
    }

    protected String parseBlock(IASTCompoundStatement block, String extra, String margin) {
        StringBuilder b = new StringBuilder();

        if (block.getParent() instanceof CPPASTFunctionDefinition) {
            b = new StringBuilder("{" + putInMark(addMarkerByProperty(STATEMENT, "{") + DELIMITER_BETWEEN_PROPERTIES
                    + addMarkerByProperty(LINE_NUMBER_OF_BLOCK_IN_FUNCTION,
                    block.getFileLocation().getStartingLineNumber() + "")
                    + DELIMITER_BETWEEN_PROPERTIES + addMarkerByProperty(OPENNING_FUNCTION_SCOPE, "true"), true)
                   /* + "\n"*/);
        } else
            b = new StringBuilder("{" + putInMark(
                    addMarkerByProperty(STATEMENT, "{") + DELIMITER_BETWEEN_PROPERTIES + addMarkerByProperty(
                            LINE_NUMBER_OF_BLOCK_IN_FUNCTION, block.getFileLocation().getStartingLineNumber() + ""),
                    true)/* + "\n"*/);

        String inside = margin + SpecialCharacter.TAB;
        if (extra != null)
            b.append(inside);

        for (IASTStatement stm : block.getStatements())
            b.append(inside).append(parseStatement(stm, inside))/*.append(SpecialCharacter.LINE_BREAK)*/
                    /*.append(SpecialCharacter.LINE_BREAK)*/;

        b.append(margin)
                .append(putInMark(addMarkerByProperty(STATEMENT, "}") + DELIMITER_BETWEEN_PROPERTIES
                                + addMarkerByProperty(LINE_NUMBER_OF_BLOCK_IN_FUNCTION,
                        block.getFileLocation().getStartingLineNumber() + ""),
                        true))
                .append(SpecialCharacter.CLOSE_BRACE);
        return b.toString();
    }

    protected String parseStatement(IASTStatement stm, String margin) {
        StringBuilder b = new StringBuilder();

        if (stm instanceof IASTCompoundStatement)
            b.append(parseBlock((IASTCompoundStatement) stm, null, margin));

        else if (stm instanceof IASTIfStatement) {
            IASTIfStatement astIf = (IASTIfStatement) stm;
            IASTStatement astElse = astIf.getElseClause();
            String cond = getShortenContent(astIf.getConditionExpression());
            b.append("if (").append(putInMark(addDefaultMarkerContentForIf(astIf.getConditionExpression()), false))
                    .append(" && (").append(cond).append(")) ");

            b.append(addExtraCall(astIf.getThenClause(), "", margin));

            if (astElse != null) {
                b/*.append(SpecialCharacter.LINE_BREAK)*/.append(margin).append("else ");
                b.append(addExtraCall(astElse, "", margin));
            }

        } else if (stm instanceof IASTForStatement) {
            IASTForStatement astFor = (IASTForStatement) stm;
            b.append(putInMark(addBeginningScopeMarkerForForLoop(), true));

            // Add marker for initialization
            IASTStatement astInit = astFor.getInitializerStatement();
            if (!(astInit instanceof IASTNullStatement)) {
                b.append(putInMark(addDefaultMarkerContentforNormalStatement(astInit), true));
            }

            b.append("for (").append(getShortenContent(astInit));
            // Add marker for condition
            IASTExpression astCond = (IASTExpression) Utils.shortenAstNode(astFor.getConditionExpression());
            if (astCond != null) {
                b/*.append(SpecialCharacter.LINE_BREAK)*/.append("\t\t\t");
                b.append(putInMark(addDefaultMarkerContentforNormalStatement(astCond), false)).append(" && ").append(getShortenContent(astCond)).append(";");
            }

            // Add marker for increment
            IASTExpression astIter = astFor.getIterationExpression();
            if (astIter != null) {
                b/*.append(SpecialCharacter.LINE_BREAK)*/.append("\t\t\t");
//				b.append(putInMark(addDefaultMarkerContentforNormalStatement(astIter), false)).append(" && ").append("(")
//						.append(getShortenContent(astIter)).append(")");
                b.append("({" + putInMark(addDefaultMarkerContentforNormalStatement(astIter), false)).append(";").
                        append(getShortenContent(astIter)).append(";})");
            }
            b.append(") ");

            // For loop: no condition
            if (astCond == null)
                b.append(parseStatement(astFor.getBody(), margin));
            else
                b.append(addExtraCall(astFor.getBody(), "", margin));

            b.append(putInMark(addEndScopeMarkerForForLoop(), true));

        } else if (stm instanceof IASTWhileStatement) {
            IASTWhileStatement astWhile = (IASTWhileStatement) stm;
            String cond = getShortenContent(astWhile.getCondition());

            b.append("while (")
                    .append(putInMark(addDefaultMarkerContentforNormalStatement(astWhile.getCondition()), false))
                    .append(" && (").append(cond).append(")) ");

            b.append(addExtraCall(astWhile.getBody(), "", margin));

        } else if (stm instanceof IASTDoStatement) {
            IASTDoStatement astDo = (IASTDoStatement) stm;
            String cond = getShortenContent(astDo.getCondition());

            b.append("do ").append(addExtraCall(astDo.getBody(), "", margin))/*.append(SpecialCharacter.LINE_BREAK)*/
                    .append(margin).append("while (")
                    .append(putInMark(addDefaultMarkerContentforNormalStatement(astDo.getCondition()), false))
                    .append(" && (").append(cond).append("));");

        } else if (stm instanceof ICPPASTTryBlockStatement) {
            ICPPASTTryBlockStatement astTry = (ICPPASTTryBlockStatement) stm;

            b.append(DriverConstant.MARK + "(\"start try;\");");

            b/*.append(SpecialCharacter.LINE_BREAK)*/.append(margin).append("try ");
            b.append(addExtraCall(astTry.getTryBody(), null, margin));

            for (ICPPASTCatchHandler catcher : astTry.getCatchHandlers()) {
                b/*.append(SpecialCharacter.LINE_BREAK)*/.append(margin).append("catch (");

                String exception = catcher.isCatchAll() ? "..." : getShortenContent(catcher.getDeclaration());
                b.append(exception).append(") ");

                b.append(addExtraCall(catcher.getCatchBody(), exception, margin));
            }

            b.append(margin).append(DriverConstant.MARK + "(\"end catch;\");");

        } else if (stm instanceof IASTBreakStatement || stm instanceof IASTContinueStatement) {
            b.append(putInMark(addDefaultMarkerContentforNormalStatement(stm), true));
            b.append(getShortenContent(stm));

        } else if (stm instanceof IASTReturnStatement) {
            b.append(putInMark(addDefaultMarkerContentforNormalStatement(stm), true));
            b.append(getShortenContent(stm));

        } else {
            String raw = getShortenContent(stm);
            b.append(putInMark(addDefaultMarkerContentforNormalStatement(stm), true));// add markers
            b.append(raw);
        }

        return b.toString();
    }

    protected String getShortenContent(IASTNode node) {
        if (node != null) {
            if (node.getRawSignature().endsWith(SpecialCharacter.END_OF_STATEMENT)) {

            } else
                /*
                 * Ex: "( x ==1   )"------> "x=1". We normalize condition
                 */
                node = Utils.shortenAstNode(node);

            return node.getRawSignature();
        } else
            return "";
    }

    /**
     * Shorten a statement
     *
     * @param str
     * @return A shortened statement
     */
    protected String esc(String str) {
        str = str.replaceAll("\\\\", "\\\\\\\\").replaceAll("\\\"", "\\\\\""); // not sure why we need

		/*
		  The statement that is divided in multiple line is convert into a single line,
		  e.g.,

		  <pre>
		  int a =

		  		0;
		  </pre>

		  ----------------> "int a = 0"

		 */
        str = str.replaceAll("(\\n)|(\\r\\n)", " ");
        return str;
    }

    /**
     * Add information which we want to print out in instrumented code
     *
     * @param node The AST of node needed to be instrumented
     * @return a string which store the extra information
     */
    protected String addDefaultMarkerContentforNormalStatement(IASTNode node) {
        String lineProperty = LINE_NUMBER_IN_FUNCTION + DELIMITER_BETWEEN_PROPERTY_AND_VALUE
                + (node.getFileLocation().getStartingLineNumber() - astFunctionNode.getFileLocation().getStartingLineNumber());
        String colProperty = OFFSET_IN_FUNCTION + DELIMITER_BETWEEN_PROPERTY_AND_VALUE
                + (node.getFileLocation().getNodeOffset() - astFunctionNode.getFileLocation().getNodeOffset());
        String statementProperty = STATEMENT + DELIMITER_BETWEEN_PROPERTY_AND_VALUE + esc(getShortenContent(node));
        String recursive = IS_RECURSIVE + DELIMITER_BETWEEN_PROPERTY_AND_VALUE + "true";

        if (containRecursiveCall(node)) {
            return lineProperty + DELIMITER_BETWEEN_PROPERTIES + colProperty + DELIMITER_BETWEEN_PROPERTIES
                    + statementProperty + (DELIMITER_BETWEEN_PROPERTIES + recursive);
        } else
            return lineProperty + DELIMITER_BETWEEN_PROPERTIES + colProperty + DELIMITER_BETWEEN_PROPERTIES
                    + statementProperty;
    }

    protected String addDefaultMarkerContentForIf(IASTNode node) {
        String condition = IN_CONTROL_BLOCK + DELIMITER_BETWEEN_PROPERTY_AND_VALUE + IF_BLOCK;
        return addDefaultMarkerContentforNormalStatement(node) + DELIMITER_BETWEEN_PROPERTIES + condition;
    }

    protected String addMarkerByProperty(String nameProperty, String value) {
        return nameProperty + DELIMITER_BETWEEN_PROPERTY_AND_VALUE + value;
    }

    protected String addBeginningScopeMarker() {
        return STATEMENT + DELIMITER_BETWEEN_PROPERTY_AND_VALUE + "{" + DELIMITER_BETWEEN_PROPERTIES
                + ADDITIONAL_BODY_CONTROL_MARKER + DELIMITER_BETWEEN_PROPERTY_AND_VALUE + "true";
    }

    protected String addEndScopeMarker() {
        return STATEMENT + DELIMITER_BETWEEN_PROPERTY_AND_VALUE + "}" + DELIMITER_BETWEEN_PROPERTIES
                + ADDITIONAL_BODY_CONTROL_MARKER + DELIMITER_BETWEEN_PROPERTY_AND_VALUE + "true";
    }

    protected String addBeginningScopeMarkerForForLoop() {
        return STATEMENT + DELIMITER_BETWEEN_PROPERTY_AND_VALUE + "{" + DELIMITER_BETWEEN_PROPERTIES
                + ADDITIONAL_BODY_CONTROL_MARKER + DELIMITER_BETWEEN_PROPERTY_AND_VALUE + "true"
                + DELIMITER_BETWEEN_PROPERTIES + SOURROUNDING_MARKER + DELIMITER_BETWEEN_PROPERTY_AND_VALUE + "for";
    }

    protected String addEndScopeMarkerForForLoop() {
        return STATEMENT + DELIMITER_BETWEEN_PROPERTY_AND_VALUE + "}" + DELIMITER_BETWEEN_PROPERTIES
                + ADDITIONAL_BODY_CONTROL_MARKER + DELIMITER_BETWEEN_PROPERTY_AND_VALUE + "true"
                + DELIMITER_BETWEEN_PROPERTIES + SOURROUNDING_MARKER + DELIMITER_BETWEEN_PROPERTY_AND_VALUE + "for";
    }

    private boolean containRecursiveCall(IASTNode stm) {
        // Get all function calls
        List<CPPASTFunctionCallExpression> functionCalls = new ArrayList<>();
        ASTVisitor visitor = new ASTVisitor() {
            @Override
            public int visit(IASTExpression statement) {
                if (statement instanceof CPPASTFunctionCallExpression) {
                    functionCalls.add((CPPASTFunctionCallExpression) statement);
                    return ASTVisitor.PROCESS_SKIP;
                } else
                    return ASTVisitor.PROCESS_CONTINUE;
            }
        };
        visitor.shouldVisitExpressions = true;
        stm.accept(visitor);

        // Create link from statement to the called function
        boolean foundRecursive = false;
        for (CPPASTFunctionCallExpression functionCall : functionCalls) {
            String name = functionCall.getChildren()[0].getRawSignature();
            if (astFunctionNode.getDeclarator().getRawSignature().startsWith(name + "(")) {

                foundRecursive = true;
                break;
            }
        }
        return foundRecursive;
    }

    public static final String TYPE_STATEMENT = "type";

    public enum TYPE_STATEMENT_ENUM {
        DO_BEGINNING("do_beginning"), DO_ENDING("do_ending"), WHILE_BEGINING("while_beginning"), WHILE_ENDING(
                "while_ending"), IF_BEGINNING("if_beginning"), IF_ENDING("if_ending"),
        ;

        private String url;

        TYPE_STATEMENT_ENUM(String url) {
            this.url = url;
        }

        public String url() {
            return url;
        }
    }

    public static final String IN_CONTROL_BLOCK = "control-block";
    public static final String IF_BLOCK = "if";
    public static final String FOR_BLOCK = "for";

    public static final String LINE_NUMBER_IN_FUNCTION = "line-in-function";
    public static final String LINE_NUMBER_OF_BLOCK_IN_FUNCTION = "line-of-blockin-function";
    public static final String OFFSET_IN_FUNCTION = "offset";
    public static final String STATEMENT = "statement";
    public static final String DELIMITER_BETWEEN_PROPERTIES = IFunctionInstrumentationGeneration.DELIMITER_BETWEEN_PROPERTIES;
    public static final String DELIMITER_BETWEEN_PROPERTY_AND_VALUE = IFunctionInstrumentationGeneration.DELIMITER_BETWEEN_PROPERTY_AND_VALUE;
    public static final String OPENNING_FUNCTION_SCOPE = "openning-function"; // {true, false}
    public static final String IS_RECURSIVE = "is-recursive"; // {true, false}
    // Beside the executed statements, we also add several additional codes to print
    // out further information
    public static final String ADDITIONAL_BODY_CONTROL_MARKER = "additional-code";
    public static final String SOURROUNDING_MARKER = "surrounding-control-block";
}
