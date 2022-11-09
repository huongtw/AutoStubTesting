package auto_testcase_generation.testdatagen.se.normalstatementparser;

import java.util.List;

import auto_testcase_generation.cfg.FunctionCallVisitor;
import auto_testcase_generation.testdatagen.se.memory.FunctionCallTable;
import auto_testcase_generation.utils.ASTUtils;
import com.dse.parser.object.IFunctionNode;
import com.dse.util.Utils;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;

import auto_testcase_generation.testdatagen.se.ExpressionRewriterUtils;
import auto_testcase_generation.testdatagen.se.memory.PhysicalCell;
import auto_testcase_generation.testdatagen.se.memory.VariableNodeTable;

/**
 * Parse multiple assignments, e.g., "x=y=z+1"
 *
 * @author ducanhnguyen
 */
public class MultipleAssignmentParser extends BinaryAssignmentParser {

    @Override
    public void parse(IASTNode ast, VariableNodeTable table, FunctionCallTable callTable) throws Exception {
        ast = Utils.shortenAstNode(ast);
        if (ast instanceof IASTBinaryExpression) {
            List<String> expressions = ASTUtils.getAllExpressionsInBinaryExpression((IASTBinaryExpression) ast);
            int last = expressions.size() - 1;

            String finalExpression = expressions.get(last);
            finalExpression = ExpressionRewriterUtils.rewrite(table, finalExpression);

            IASTNode finalExprAst = Utils.convertToIAST(finalExpression);
            FunctionCallVisitor visitor = new FunctionCallVisitor((IFunctionNode) table.getFunctionNode());
            finalExprAst.accept(visitor);
            for (IASTFunctionCallExpression expr : visitor.getCallMap().keySet()) {
                String varName = callTable.get(expr);
                if (varName != null) {
                    String regex = "\\Q" + expr.getRawSignature() + "\\E";
                    finalExpression = finalExpression.replaceFirst(regex, varName);
                    callTable.remove(expr);
                }
            }

			/*
             * All variable corresponding to expressions, except the final
			 * expression, is assigned to the final expression
			 */
            for (int i = 0; i < last; i++) {
                String currentExpression = expressions.get(i);
                PhysicalCell cell = table.findPhysicalCellByName(currentExpression);

                if (cell != null)
                    cell.setValue(finalExpression);
            }
        }
    }

}
