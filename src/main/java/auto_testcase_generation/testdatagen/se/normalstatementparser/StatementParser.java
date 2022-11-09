package auto_testcase_generation.testdatagen.se.normalstatementparser;

import auto_testcase_generation.testdatagen.se.memory.FunctionCallTable;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;

import auto_testcase_generation.testdatagen.se.memory.VariableNodeTable;

import java.util.Map;

/**
 * The top abstract class used to parse statement
 *
 * @author ducanhnguyen
 */
public abstract class StatementParser {
    /**
     * Parse the statement
     *
     * @param ast   the AST of the statement
     * @param table table of variables
     * @param callTable table of function calls
     * @throws Exception
     */
    public abstract void parse(IASTNode ast, VariableNodeTable table, FunctionCallTable callTable) throws Exception;
}
