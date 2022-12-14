package auto_testcase_generation.testdatagen.se.normalstatementparser;

import auto_testcase_generation.testdatagen.se.memory.FunctionCallTable;
import com.dse.util.Utils;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCastExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCastExpression;

import auto_testcase_generation.testdatagen.se.memory.VariableNodeTable;
import auto_testcase_generation.utils.ASTUtils;

/**
 * Parse type casting assignment <br/>
 * Ex: y = (char) x
 *
 * @author ducanhnguyen
 */
public class NormalVariableToTypeCastingParser extends NormalVariableToExpressionParser {

    @Override
    public void parse(IASTNode ast, VariableNodeTable table, FunctionCallTable callTable) throws Exception {
        ast = Utils.shortenAstNode(ast);

        if (ast instanceof ICPPASTBinaryExpression) {

            IASTNode right = ((ICPPASTBinaryExpression) ast).getOperand2();

            if (right instanceof CPPASTCastExpression) {
                CPPASTCastExpression castRightAST = (CPPASTCastExpression) right;

                switch (castRightAST.getOperator()) {
                    // Ex: y = (char) x
                    case IASTCastExpression.op_cast:
                        // Ex: c = static_cast<char>(x)
                    case ICPPASTCastExpression.op_static_cast:
                    /*
					 * Get the casting expression
					 */
                        IASTNode operand = Utils.shortenAstNode(castRightAST.getOperand());
                        String operandStr = operand.getRawSignature();

                        IASTNode left = ((ICPPASTBinaryExpression) ast).getOperand1();
                        String newAssignment = left.getRawSignature() + " = " + operandStr;
                        IASTNode newAST = Utils.convertToIAST(newAssignment);
                        new BinaryAssignmentParser().parse(newAST, table, callTable);

                        break;
                    case ICPPASTCastExpression.op_const_cast:
                    case ICPPASTCastExpression.op_dynamic_cast:
                    case ICPPASTCastExpression.op_reinterpret_cast:
                        throw new Exception("Dont support " + ast.getRawSignature());

                    default:
                        break;
                }
            } else
                throw new Exception("Dont support " + ast.getRawSignature());
        }

    }
}
