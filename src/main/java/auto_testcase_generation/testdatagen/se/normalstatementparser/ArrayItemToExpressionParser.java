package auto_testcase_generation.testdatagen.se.normalstatementparser;

import auto_testcase_generation.cfg.FunctionCallVisitor;
import auto_testcase_generation.testdatagen.se.memory.*;
import com.dse.parser.object.IFunctionNode;
import com.dse.util.Utils;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;

import auto_testcase_generation.testdatagen.se.ExpressionRewriterUtils;
import auto_testcase_generation.testdatagen.se.memory.array.ArraySymbolicVariable;
import auto_testcase_generation.testdatagen.se.memory.pointer.PointerSymbolicVariable;

/**
 * Assign an array element to an expression <br/>
 * Ex: a[1]=2+x
 *
 * @author ducanhnguyen
 */
public class ArrayItemToExpressionParser extends NormalBinaryAssignmentParser {
    @Override
    public void parse(IASTNode ast, VariableNodeTable table, FunctionCallTable callTable) throws Exception {
        ast = Utils.shortenAstNode(ast);
        if (ast instanceof IASTBinaryExpression) {
            IASTNode leftAST = ((IASTBinaryExpression) ast).getOperand1();
            IASTNode rightAST = ((IASTBinaryExpression) ast).getOperand2();

            if (rightAST != null && leftAST != null) {
                String reducedRightExpression = ExpressionRewriterUtils.rewrite(table, rightAST.getRawSignature());
                FunctionCallVisitor visitor = new FunctionCallVisitor((IFunctionNode) table.getFunctionNode());
                rightAST.accept(visitor);
                for (IASTFunctionCallExpression expr : visitor.getCallMap().keySet()) {
                    String varName = callTable.get(expr);
                    if (varName != null) {
                        String regex = "\\Q" + expr.getRawSignature() + "\\E";
                        reducedRightExpression = reducedRightExpression.replaceFirst(regex, varName);
                        callTable.remove(expr);
                    }
                }

                String name = Utils.getNameVariable(leftAST.getRawSignature());
                ISymbolicVariable ref = table.findorCreateVariableByName(name);

				/*
                 * Get the block that contains data of variable
				 */
                LogicBlock block = null;
                if (ref instanceof ArraySymbolicVariable)
                    block = ((ArraySymbolicVariable) ref).getBlock();

                else if (ref instanceof PointerSymbolicVariable)
                    block = ((PointerSymbolicVariable) ref).getReference().getBlock();

                if (block != null) {
					/*
					 * Get the reduce index of array item
					 */
                    String index = Utils.getReducedIndex(leftAST.getRawSignature(), table);

					/*
					 * If the cell exists in the block
					 */
                    if (block.findCellByIndex(index) != null)
                        block.updateCellByIndex(index, reducedRightExpression);
                    else
					/*
					 * If the cell does not in the block, a new cell is added to
					 * the block
					 */ {
                        PhysicalCell newCell = new PhysicalCell(reducedRightExpression);
                        block.addLogicalCell(newCell, index);
                    }
                }
            } else
                throw new Exception("Dont support " + ast.getRawSignature());
        } else
            throw new Exception("Dont support " + ast.getRawSignature());
    }
}
