package auto_testcase_generation.testdatagen.se.normalstatementparser;

import auto_testcase_generation.cfg.FunctionCallVisitor;
import auto_testcase_generation.testdatagen.se.ExpressionRewriterUtils;
import auto_testcase_generation.testdatagen.se.memory.*;
import auto_testcase_generation.testdatagen.se.memory.pointer.PointerSymbolicVariable;
import com.dse.parser.object.IFunctionNode;
import com.dse.util.Utils;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;

/**
 * Parse pointer item assigned to expression <br/>
 * Ex1: *p1 = *p1 - 1<br/>
 * Ex2: *(p1+1) = *p2 + 1
 *
 * @author ducanhnguyen
 */
public class PointerItemToExpressionParser extends NormalBinaryAssignmentParser {

    @Override
    public void parse(IASTNode ast, VariableNodeTable table, FunctionCallTable callTable) throws Exception {
        ast = Utils.shortenAstNode(ast);

        if (ast instanceof ICPPASTBinaryExpression) {
            IASTNode astLeft = ((ICPPASTBinaryExpression) ast).getOperand1();
            IASTNode astRight = ((ICPPASTBinaryExpression) ast).getOperand2();

            String reducedRightExpression = ExpressionRewriterUtils.rewrite(table, astRight.getRawSignature());
            FunctionCallVisitor visitor = new FunctionCallVisitor((IFunctionNode) table.getFunctionNode());
            astRight.accept(visitor);
            for (IASTFunctionCallExpression expr : visitor.getCallMap().keySet()) {
                String varName = callTable.get(expr);
                if (varName != null) {
                    String regex = "\\Q" + expr.getRawSignature() + "\\E";
                    reducedRightExpression = reducedRightExpression.replaceFirst(regex, varName);
                    callTable.remove(expr);
                }
            }

            String reducedLeftExpression = ExpressionRewriterUtils
                    .convertTwoLevelPointerItemToArrayItem(astLeft.getRawSignature());
            reducedLeftExpression = ExpressionRewriterUtils
                    .convertOneLevelPointerItemToArrayItem(reducedLeftExpression);

            PhysicalCell c = table.findPhysicalCellByName(reducedLeftExpression);
            if (c != null)
                /*
				 * Update value for cell
				 */
                c.setValue(reducedRightExpression);
            else {
                ISymbolicVariable ref = table.findorCreateVariableByName(reducedLeftExpression);

                LogicBlock block = null;
                if (ref instanceof PointerSymbolicVariable) {
                    PointerSymbolicVariable var = (PointerSymbolicVariable) ref;
                    block = var.getReference().getBlock();

                }

                if (block != null) {
					/*
					 * Get the reduce index of array item
					 */
                    String index = Utils.getReducedIndex(reducedLeftExpression, table);

                    PhysicalCell newCell = new PhysicalCell(reducedRightExpression);
                    block.addLogicalCell(newCell, index);
                }
            }
        }
    }

}
