package auto_testcase_generation.instrument;

import com.dse.parser.object.ICommonFunctionNode;
import com.dse.testcase_execution.DriverConstant;
import com.dse.util.PathUtils;
import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;
import org.eclipse.cdt.core.dom.ast.*;

public abstract class AbstractFunctionInstrumentation implements IFunctionInstrumentationGeneration{

    protected String functionPath;
    protected IASTFunctionDefinition astFunctionNode;
    protected ICommonFunctionNode functionNode;

    /**
     * Add information which we want to print out in instrumented code
     *
     * @param node The AST of node needed to be instrumented
     * @return a string which store the extra information
     */
    protected String addContentOfMarkFunction(IASTNode node, IASTNode astFunctionNode, String functionPath, boolean isFullCondition, boolean isSubCondition) {
        if (node == null || node.getFileLocation() == null)
            return "";
        int lineInSourcecodeFile = node.getFileLocation().getStartingLineNumber();
        int startOffsetInSourcecodeFile = node.getFileLocation().getNodeOffset();
        int endOffsetInSourcecodeFile = node.getFileLocation().getNodeOffset() + node.getFileLocation().getNodeLength();

        int lineInFunction = (node.getFileLocation().getStartingLineNumber() - astFunctionNode.getFileLocation().getStartingLineNumber());
        int startOffsetInFunction = (node.getFileLocation().getNodeOffset() - astFunctionNode.getFileLocation().getNodeOffset());
        int endOffsetInFunction = (node.getFileLocation().getNodeOffset() + node.getFileLocation().getNodeLength() - astFunctionNode.getFileLocation().getNodeOffset());

        String relativePath = PathUtils.toRelative(functionPath);

        String marker =
                // paramater
                LINE_NUMBER_IN_SOURCE_CODE_FILE + DELIMITER_BETWEEN_PROPERTY_AND_VALUE + lineInSourcecodeFile + DELIMITER_BETWEEN_PROPERTIES +
                        // paramater
                        START_OFFSET_IN_SOURCE_CODE_FILE + DELIMITER_BETWEEN_PROPERTY_AND_VALUE + startOffsetInSourcecodeFile + DELIMITER_BETWEEN_PROPERTIES +
                        // paramater
                        END_OFFSET_IN_SOURCE_CODE_FILE + DELIMITER_BETWEEN_PROPERTY_AND_VALUE + endOffsetInSourcecodeFile + DELIMITER_BETWEEN_PROPERTIES +
                        // paramater
                        LINE_NUMBER_IN_FUNCTION + DELIMITER_BETWEEN_PROPERTY_AND_VALUE + lineInFunction + DELIMITER_BETWEEN_PROPERTIES
                        // paramater
                        + START_OFFSET_IN_FUNCTION + DELIMITER_BETWEEN_PROPERTY_AND_VALUE + startOffsetInFunction + DELIMITER_BETWEEN_PROPERTIES
                        // parameter
                        + END_OFFSET_IN_FUNCTION + DELIMITER_BETWEEN_PROPERTY_AND_VALUE + endOffsetInFunction;

        if (isSubCondition && !isFullCondition)
            // parameter
            marker += DELIMITER_BETWEEN_PROPERTIES + IS_SUB_CONDITION + DELIMITER_BETWEEN_PROPERTY_AND_VALUE + isSubCondition;
        else if (!isSubCondition && isFullCondition)
            // parameter
            marker += DELIMITER_BETWEEN_PROPERTIES + IS_FULL_CONDITION + DELIMITER_BETWEEN_PROPERTY_AND_VALUE + isFullCondition;
        else if (!isSubCondition && !isFullCondition)
            // parameter
            marker += DELIMITER_BETWEEN_PROPERTIES + IS_NORMAL_STATEMENT + DELIMITER_BETWEEN_PROPERTY_AND_VALUE + "true";

        if (functionPath != null && functionPath.length() > 0)
            marker += DELIMITER_BETWEEN_PROPERTIES + FUNCTION_ADDRESS + DELIMITER_BETWEEN_PROPERTY_AND_VALUE + Utils.doubleNormalizePath(relativePath);

        return marker;
    }

    public static String addMarkerForAstNode(IASTNode node, int lineInFunction,
                                             int startOffsetInFunction, int endOffsetInFunction, String functionPath){
        int lineInSourcecodeFile = node.getFileLocation().getStartingLineNumber();
        int startOffsetInSourcecodeFile = node.getFileLocation().getNodeOffset();
        int endOffsetInSourcecodeFile = node.getFileLocation().getNodeOffset() + node.getFileLocation().getNodeLength();
        String marker =
                // paramater
                LINE_NUMBER_IN_SOURCE_CODE_FILE + DELIMITER_BETWEEN_PROPERTY_AND_VALUE + lineInSourcecodeFile + DELIMITER_BETWEEN_PROPERTIES +
                        // paramater
                        START_OFFSET_IN_SOURCE_CODE_FILE + DELIMITER_BETWEEN_PROPERTY_AND_VALUE + startOffsetInSourcecodeFile + DELIMITER_BETWEEN_PROPERTIES +
                        // paramater
                        END_OFFSET_IN_SOURCE_CODE_FILE + DELIMITER_BETWEEN_PROPERTY_AND_VALUE + endOffsetInSourcecodeFile + DELIMITER_BETWEEN_PROPERTIES +
                        // paramater
                        LINE_NUMBER_IN_FUNCTION + DELIMITER_BETWEEN_PROPERTY_AND_VALUE + lineInFunction + DELIMITER_BETWEEN_PROPERTIES
                        // paramater
                        + START_OFFSET_IN_FUNCTION + DELIMITER_BETWEEN_PROPERTY_AND_VALUE + startOffsetInFunction + DELIMITER_BETWEEN_PROPERTIES
                        // parameter
                        + END_OFFSET_IN_FUNCTION + DELIMITER_BETWEEN_PROPERTY_AND_VALUE + endOffsetInFunction;

        String relativePath = PathUtils.toRelative(functionPath);

        if (functionPath != null && functionPath.length() > 0)
            marker += DELIMITER_BETWEEN_PROPERTIES + FUNCTION_ADDRESS + DELIMITER_BETWEEN_PROPERTY_AND_VALUE + Utils.doubleNormalizePath(relativePath);

        return marker;
    }
    protected String addContentOfMarkFunction(IASTNode node, IASTNode astFunctionNode, String functionPath) {
        return addContentOfMarkFunction(node, astFunctionNode, functionPath, false, false);
    }

    protected boolean isCondition(IASTNode condition) {
        boolean isCondition = false;
        // Ex1: abc
        // Ex2: 123
        // Ex3: sv.name
        condition = Utils.shortenAstNode(condition);
        if (condition instanceof IASTIdExpression || condition instanceof IASTFieldReference) {
            isCondition = true;

        } else if (condition instanceof IASTBinaryExpression) {
            IASTBinaryExpression binaryCon = (IASTBinaryExpression) condition;
            int operator = binaryCon.getOperator();

            switch (operator) {
                case IASTBinaryExpression.op_logicalAnd:
                case IASTBinaryExpression.op_logicalOr:
                    isCondition = true;
                    break;
            }
        }
        return isCondition;
    }
    /**
     * Ex: "( x ==1 )"------> "x==1". We normalize condition
     */
    protected String getShortenContent(IASTNode node) {
        if (node != null) {
            if (!node.getRawSignature().endsWith(SpecialCharacter.END_OF_STATEMENT)) {
                node = Utils.shortenAstNode(node);
            }
            return node.getRawSignature();
        } else {
            return "";
        }
    }

    /**
     * Put a string in a marker
     */
    protected String putInMark(String str, boolean isAStatement) {
        return DriverConstant.MARK
                + "(\"" + str + "\")"
                + (isAStatement ? ";" : "");
    }

    public String getFunctionPath() {
        return functionPath;
    }

    public void setFunctionPath(String functionPath) {
        this.functionPath = functionPath;
    }
}
