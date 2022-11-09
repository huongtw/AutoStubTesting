package auto_testcase_generation.testdatagen.se.normalstatementparser;

import auto_testcase_generation.testdatagen.se.memory.FunctionCallTable;
import com.dse.util.Utils;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;

import auto_testcase_generation.testdatagen.se.memory.VariableNodeTable;

public class ThrowParser extends StatementParser {
    /**
     * Name of exception
     */
    private String exceptionName = "";

    @Override
    public void parse(IASTNode ast, VariableNodeTable table, FunctionCallTable callTable) throws Exception {
        ast = Utils.shortenAstNode(ast);

        if (ast instanceof IASTExpressionStatement) {
            IASTExpressionStatement tmp = (IASTExpressionStatement) ast;
            IASTUnaryExpression unaryStm = (IASTUnaryExpression) tmp.getExpression();
            exceptionName = unaryStm.getOperand().getRawSignature();
        }
    }

    public String getExceptionName() {
        return exceptionName;
    }

    public void setExceptionName(String exceptionName) {
        this.exceptionName = exceptionName;
    }
}
