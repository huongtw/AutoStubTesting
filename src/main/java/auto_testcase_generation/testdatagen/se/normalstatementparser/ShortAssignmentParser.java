package auto_testcase_generation.testdatagen.se.normalstatementparser;

import auto_testcase_generation.testdatagen.se.memory.FunctionCallTable;
import com.dse.util.Utils;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTArraySubscriptExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIdExpression;

import auto_testcase_generation.testdatagen.se.memory.VariableNodeTable;
import auto_testcase_generation.utils.ASTUtils;

/**
 * Parse short assignment Ex1: a/=1; Ex2: b-=2; Ex3: c+=3; Ex4: e+=4; Ex5: f%=5;
 *
 * @author ducanhnguyen
 */
public class ShortAssignmentParser extends BinaryAssignmentParser {

    @Override
    public void parse(IASTNode ast, VariableNodeTable table, FunctionCallTable callTable) throws Exception {
        ast = Utils.shortenAstNode(ast);
        if (ast instanceof IASTBinaryExpression) {

            IASTBinaryExpression astBinary = (IASTBinaryExpression) ast;

            IASTExpression right = astBinary.getOperand2();
            IASTExpression left = astBinary.getOperand1();

            String reducedRight = right.getRawSignature();

            switch (astBinary.getOperator()) {
                case IASTBinaryExpression.op_multiplyAssign:
                    reducedRight = left + "*" + reducedRight;
                    break;

                case IASTBinaryExpression.op_divideAssign:
                    reducedRight = left + "/" + reducedRight;
                    break;

                case IASTBinaryExpression.op_moduloAssign:
                    reducedRight = left + "%" + reducedRight;
                    break;

                case IASTBinaryExpression.op_plusAssign:
                    reducedRight = left + "+" + reducedRight;
                    break;

                case IASTBinaryExpression.op_minusAssign:
                    reducedRight = left + "-" + reducedRight;
                    break;
            }

			/*
             * If the left expression is an array item. VD: p1[0]
			 */
            if (left instanceof ICPPASTArraySubscriptExpression) {
                IASTNode newAST = Utils.convertToIAST(left.getRawSignature() + "=" + reducedRight);
                new ArrayItemToExpressionParser().parse(newAST, table, callTable);

            } else
			/*
			 * In case left expression represents name of variable
			 */
                if (left instanceof CPPASTIdExpression) {
                    IASTNode newAST = Utils.convertToIAST(left.getRawSignature() + " = " + reducedRight);
                    new NormalVariableToExpressionParser().parse(newAST, table, callTable);

                } else
                    throw new Exception("Dont support " + ast.getRawSignature());
        }
    }

}
