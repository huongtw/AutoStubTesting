package auto_testcase_generation.instrument;

import com.dse.parser.ProjectParser;
import com.dse.parser.SourcecodeFileParser;
import com.dse.parser.object.IFunctionNode;
import com.dse.parser.object.INode;
import com.dse.parser.object.MacroFunctionNode;
import com.dse.search.Search;
import com.dse.search.condition.MacroFunctionNodeCondition;
import com.dse.util.SourceConstant;
import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.internal.core.dom.parser.cpp.*;

import java.io.File;
import java.util.List;

/**
 * Instrument macro function.
 * <p>
 * Example:
 *
 * <pre>
 * #define SKIP_SPACES(p, limit)  \
 * char *lim = (limit);         \
 * while (p < lim) {            \
 * if (*p++ != ' ') {         \
 * p--; break; }}
 *
 * </pre>
 */
public class FunctionInstrumentationForMacro extends AbstractFunctionInstrumentation{
    private IASTPreprocessorFunctionStyleMacroDefinition macroAST;
    private String functionPath;
    private MacroFunctionNode macroFunctionNode;

    public FunctionInstrumentationForMacro(IASTPreprocessorFunctionStyleMacroDefinition macroAST){
        this.macroAST = macroAST;
    }

    public static void main(String[] args) throws Exception {
        ProjectParser parser = new ProjectParser(new File("/home/lamnt/IdeaProjects/akautauto/datatest/fsoft/gcem/tests"));
        parser.setExpandTreeuptoMethodLevel_enabled(true);

        List<INode> nodes = Search.searchNodes(parser.getRootTree(), new MacroFunctionNodeCondition(),
                "GCEM_TEST_COMPARE_VALS(gcem_fn,std_fn,...)");
        System.out.println(nodes.size());
        MacroFunctionNode function = (MacroFunctionNode) nodes.get(3);
//        System.out.printlssssssssssssssssssssssxx-pn(new FunctionInstrumentationForSubCondition(function.convertMacroFunctionToRealFunction(function.getAST()))
//                .generateInstrumentedFunction());

        FunctionInstrumentationForMacro functionInstrumentationForMacro = new FunctionInstrumentationForMacro(function.getAST());
        functionInstrumentationForMacro.setFunctionPath(function.getAbsolutePath());
        System.out.println("instrument = " + functionInstrumentationForMacro.generateInstrumentedFunction());
    }

    public void setMacroFunctionNode(MacroFunctionNode macroFunctionNode) {
        this.macroFunctionNode = macroFunctionNode;
    }

    @Override
    public String generateInstrumentedFunction() {
        if (macroAST == null)
            return "";

        if (isMacroFunction(macroAST) != FUNCTION_LIKE_MACROS)
            return macroAST.getRawSignature();

        IASTFunctionDefinition newFunctionAST = new MacroFunctionNode().convertMacroFunctionToRealFunction(macroAST);

        if (isOneStatementMacro(macroAST)) {
//            return instrumentOneStatementMacro();
            return macroAST.getRawSignature();
        }

        if (newFunctionAST != null) {
            System.out.println(newFunctionAST.getRawSignature());
            FunctionInstrumentationForAllCoverages instrumentationForAllCoverages =
                    new FunctionInstrumentationForAllCoverages(newFunctionAST, null);

            instrumentationForAllCoverages.setFunctionPath(functionPath);
            String instrumentation = instrumentationForAllCoverages.generateInstrumentedFunction();

            try {
                IASTTranslationUnit newAST = new SourcecodeFileParser().getIASTTranslationUnit(instrumentation.toCharArray());
                IASTFunctionDefinition iastFunctionDefinition = (IASTFunctionDefinition) newAST.getChildren()[0];
                instrumentation = normalizeToMacro(iastFunctionDefinition);
                return instrumentation;

            } catch (Exception e) {
                e.printStackTrace();
                return macroAST.getRawSignature();
            }
        } else
            return macroAST.getRawSignature();
    }

    private String instrumentOneStatementMacro() {
        IFunctionNode correspondingNode = macroFunctionNode.getCorrespondingFunctionNode();
        IASTStatement astStatement = correspondingNode.getAST().getBody();

        if (astStatement instanceof IASTCompoundStatement) {

            IASTStatement[] bodyStatements = ((IASTCompoundStatement) astStatement).getStatements();

            for (IASTStatement statement : bodyStatements) {
                if (statement != null) {
                    String body = statement.getRawSignature();
                    if (body.contains(SpecialCharacter.END_OF_STATEMENT)) {
                        body = body.replace(SpecialCharacter.END_OF_STATEMENT, SpecialCharacter.EMPTY);
                    }

                    int index = macroAST.getRawSignature().indexOf(body);

                    int startOffsetInFunction = statement.getFileLocation().getNodeOffset()
                            - correspondingNode.getAST().getFileLocation().getNodeOffset();

                    int endOffsetInFunction = statement.getFileLocation().getNodeOffset()
                            + statement.getFileLocation().getNodeLength()
                            - correspondingNode.getAST().getFileLocation().getNodeOffset();

                    String markStm = AbstractFunctionInstrumentation
                            .addMarkerForAstNode(macroAST, 0, startOffsetInFunction, endOffsetInFunction, functionPath);

                    markStm = String.format("%s(\"%s\")", SourceConstant.MARK_STM, markStm);

                    StringBuilder builder = new StringBuilder();
                    boolean isWrapInBracket = body.matches("\\(.+\\)");
                    if (!isWrapInBracket)
                        body = "(" + body + ")";
                    builder.append(markStm).append(" ? ").append(body).append(" : ").append(body);

                    String finalContent = builder.toString();

                    boolean isStatement = macroAST.getRawSignature().trim()
                            .endsWith(SpecialCharacter.END_OF_STATEMENT);

                    if (isStatement)
                        finalContent += SpecialCharacter.END_OF_STATEMENT;
                    else
                        finalContent = "(" + finalContent + ")";

                    return macroAST.getRawSignature().substring(0, index) + finalContent;
                }
            }
        }

        return "";
    }

    private boolean isOneStatementMacro(IASTPreprocessorFunctionStyleMacroDefinition macroDefinition) {
        String body = macroDefinition.getRawSignature().substring(macroDefinition.getRawSignature().indexOf(")") + 1);
        body = body.replace("\\", "").trim();
        if (body.startsWith("{") && body.endsWith("}"))
            body = body.substring(1, body.length() - 1);

        IASTNode ast = Utils.convertToIAST(body);
        if (ast instanceof CPPASTDoStatement || ast instanceof CPPASTWhileStatement || ast instanceof CPPASTForStatement
                || ast instanceof CPPASTIfStatement || ast instanceof CPPASTSwitchStatement)
            return false;
        else if (ast instanceof IASTCompoundStatement)
            if (ast.getChildren().length == 1)
                return true;
            else
                return false;

        return true;
    }

    private String getOriginalName(){
        return macroAST.getRawSignature().substring(0, macroAST.getRawSignature().indexOf(")") + 1);
    }

    private String normalizeToMacro(IASTFunctionDefinition instrumentNode) {
        String normalizedBody = instrumentNode.getBody().getRawSignature();
        normalizedBody = normalizedBody.replace("\r", "\n");
        normalizedBody = normalizedBody.replace("\n", "\\\n");
        return this.getOriginalName() + " " + normalizedBody;
    }

    private int isMacroFunction(IASTPreprocessorFunctionStyleMacroDefinition macroDefinition) {
        if (macroDefinition.getParameters().length > 0) {
            return FUNCTION_LIKE_MACROS;
        } else
            return OTHER;
    }

    public IASTPreprocessorFunctionStyleMacroDefinition getMacroAST() {
        return macroAST;
    }

    public void setMacroAST(IASTPreprocessorFunctionStyleMacroDefinition macroAST) {
        this.macroAST = macroAST;
    }

    public String getFunctionPath() {
        return functionPath;
    }

    public void setFunctionPath(String functionPath) {
        this.functionPath = functionPath;
    }

    // https://gcc.gnu.org/onlinedocs/cpp/Object-like-Macros.html#Object-like-Macros
    public static final int OBJECT_LIKE_MACROS = 0;
    // https://gcc.gnu.org/onlinedocs/cpp/Function-like-Macros.html#Function-like-Macros
    public static final int FUNCTION_LIKE_MACROS = 1;

    public static final int OTHER = 2;

}
