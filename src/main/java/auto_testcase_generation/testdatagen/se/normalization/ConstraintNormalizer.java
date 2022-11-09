package auto_testcase_generation.testdatagen.se.normalization;

import auto_testcase_generation.cfg.object.ConditionCfgNode;
import auto_testcase_generation.utils.ASTUtils;
import com.dse.parser.normalizer.AbstractFunctionNormalizer;
import com.dse.parser.normalizer.IFunctionNormalizer;
import com.dse.util.Utils;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTBinaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIdExpression;

import java.util.*;

/**
 * Convert pointer access to array index
 *
 * @author DucAnh
 */
public class ConstraintNormalizer extends AbstractFunctionNormalizer implements IFunctionNormalizer {

    public static void main(String[] args) {
        String test = "!((mTEMPDSPA)!=0)" ;
        ConstraintNormalizer norm = new ConstraintNormalizer();
        norm.setOriginalSourcecode(test);
        norm.normalize();
        System.out.println(test);
        System.out.println(norm.getNormalizedSourcecode());
    }

    @Override
    public void normalize() {
        if (originalSourcecode != null && originalSourcecode.length() > 0)
            try {
                normalizeSourcecode = simplifyConstraint(originalSourcecode);
            } catch (Exception e) {
                e.printStackTrace();
                normalizeSourcecode = originalSourcecode;
            }
        else
            normalizeSourcecode = originalSourcecode;
    }
    // !(a!=b || a!=c)
    private String simplifyConstraint(String statement) throws Exception {
        IASTNode root = Utils.convertToIAST(statement);
        String newStatement="";
        if(root instanceof  IASTUnaryExpression){
            int op = ((IASTUnaryExpression)root).getOperator();
            if(op == IASTUnaryExpression.op_not){
                newStatement = reconstructString((IASTExpression) root, 2, false, false);
            }
        }else if(root instanceof IASTBinaryExpression){
            newStatement = "("+reconstructString((IASTExpression) root, 2, false, false)+")";
        }else if(root instanceof IASTExpressionStatement){
            newStatement = reconstructString((IASTExpressionStatement) root, 2, false, false);
        }
        if(newStatement.isEmpty())
            return statement;
        else {
            newStatement = specialCase(newStatement);
        }
        return newStatement;
    }
    String reconstructString(IASTNode root , int isNegative, boolean isExpression, boolean isBinaryExp) {
        String newStatement="";
        while(true){
            if(root instanceof IASTUnaryExpression){
                int op = ((IASTUnaryExpression)root).getOperator();
                if(op == IASTUnaryExpression.op_not){
                    root = ((IASTUnaryExpression) root).getOperand();
                    isNegative++;
                    newStatement = reconstructString((IASTExpression) root, isNegative, isExpression, isBinaryExp);
                    return newStatement;
                }
                else if(op == IASTUnaryExpression.op_star){
                    String fr = root.getRawSignature();
                    if(isExpression && isNegative % 2 != 0){
                        newStatement += fr;
                    }else newStatement += "!"+fr;

                    return newStatement;
                }
                else {
                    root = ((IASTUnaryExpression) root).getOperand();
                    int op2=0;
                    if(root instanceof IASTUnaryExpression){
                        op2 = ((IASTUnaryExpression)root).getOperator();
                    }
                    else if (root instanceof IASTIdExpression){
                        String fr = root.getRawSignature();
                        if(isNegative % 2 != 0){
                            newStatement += "!"+fr;
                        }else newStatement += fr;
                        return newStatement;
                    }
                    else if(root instanceof IASTBinaryExpression)
                        op2 = ((IASTBinaryExpression)root).getOperator();

                    if(op2 == IASTBinaryExpression.op_assign && isNegative % 2 != 0){
                        newStatement = "!("+reconstructString((IASTExpression) root, isNegative, isExpression, isBinaryExp)+")";
                    }else{
                        newStatement = "("+reconstructString((IASTExpression) root, isNegative, isExpression, isBinaryExp)+")";
                    }

                    return newStatement;
                }
            }
            else if(root instanceof IASTBinaryExpression ){

                String add="";
                boolean isExpressionSign = false;
                isBinaryExp = true;
                int subOp = ((IASTBinaryExpression) root).getOperator();
                if(isNegative % 2 != 0 ){
                    if (subOp == IASTBinaryExpression.op_logicalOr){
                        add = " && ";
                    }
                    else if (subOp == IASTBinaryExpression.op_logicalAnd){
                        add = " || ";
                    }
                    else if (subOp == IASTBinaryExpression.op_equals){
                        add += " != ";
                    }
                    else if (subOp == IASTBinaryExpression.op_notequals){
                        add += " == ";
                    }
                    else if (subOp == IASTBinaryExpression.op_greaterThan){
                        add += " <= ";
                    }
                    else if (subOp == IASTBinaryExpression.op_greaterEqual){
                        add += " < ";
                    }
                    else if (subOp == IASTBinaryExpression.op_lessThan){
                        add += " >= ";
                    }
                    else if (subOp == IASTBinaryExpression.op_lessEqual){
                        add += " > ";
                    }
                }
                else {
                    if (subOp == IASTBinaryExpression.op_logicalOr){
                        add = " || ";
                    }
                    else if (subOp == IASTBinaryExpression.op_logicalAnd){
                        add = " && ";
                    }
                    else if (subOp == IASTBinaryExpression.op_equals){
                        add += " == ";
                    }
                    else if (subOp == IASTBinaryExpression.op_notequals){
                        add += " != ";
                    }
                    else if (subOp == IASTBinaryExpression.op_greaterThan){
                        add += " > ";
                    }
                    else if (subOp == IASTBinaryExpression.op_greaterEqual){
                        add += " >= ";
                    }
                    else if (subOp == IASTBinaryExpression.op_lessThan){
                        add += " < ";
                    }
                    else if (subOp == IASTBinaryExpression.op_lessEqual){
                        add += " <= ";
                    }
                }
                if(add.equals("")){
                    if(subOp == IASTBinaryExpression.op_plus){
                        add = " + ";
                    }
                    else if(subOp == IASTBinaryExpression.op_minus){
                        add = " - ";
                    }
                    else if(subOp == IASTBinaryExpression.op_multiply){
                        add = " * ";
                    }
                    else if(subOp == IASTBinaryExpression.op_divide){
                        add = " / ";
                    }
                    else if(subOp == IASTBinaryExpression.op_modulo){
                        add = " % ";
                    }
                    else if(subOp == IASTBinaryExpression.op_plusAssign){
                        add = " += ";
                    }
                    else if(subOp == IASTBinaryExpression.op_minusAssign){
                        add = " -= ";
                    }
                    else if(subOp == IASTBinaryExpression.op_multiplyAssign){
                        add = " *= ";
                    }
                    else if(subOp == IASTBinaryExpression.op_divideAssign){
                        add = " /= ";
                    }
                    else if(subOp == IASTBinaryExpression.op_moduloAssign){
                        add = " %= ";
                    }
                    else if(subOp == IASTBinaryExpression.op_shiftLeft){
                        add = " << ";
                    }
                    else if(subOp == IASTBinaryExpression.op_shiftRight){
                        add = " >> ";
                    }
                    else if(subOp == IASTBinaryExpression.op_shiftLeftAssign){
                        add = " <<= ";
                    }
                    else if(subOp == IASTBinaryExpression.op_shiftRightAssign){
                        add = " >>= ";
                    }
                    else if(subOp == IASTBinaryExpression.op_binaryAnd){
                        add = " & ";
                    }
                    else if(subOp == IASTBinaryExpression.op_binaryOr){
                        add = " | ";
                    }
                    else if(subOp == IASTBinaryExpression.op_binaryXor){
                        add = " ^ ";
                    }
                    else if(subOp == IASTBinaryExpression.op_binaryAndAssign){
                        add = " &= ";
                    }
                    else if(subOp == IASTBinaryExpression.op_binaryOrAssign){
                        add = " |= ";
                    }
                    else if(subOp == IASTBinaryExpression.op_binaryXor){
                        add = " ^= ";
                    }
                    else if(subOp == IASTBinaryExpression.op_assign){
                        add = " = ";
                    }
                }
                else if(add.equals(" || ") || add.equals(" && ")){
                    isExpressionSign = true;
                }
                IASTExpression op1 = ((IASTBinaryExpression)root).getOperand1();
                IASTExpression op2 = ((IASTBinaryExpression)root).getOperand2();

                newStatement += reconstructString(op1,isNegative, isExpressionSign, isBinaryExp);
                newStatement += add;
                newStatement += reconstructString(op2,isNegative, isExpressionSign, isBinaryExp);

                return newStatement;
            }
//            else if(root instanceof  IASTIdExpression || root instanceof IASTLiteralExpression){
//                if(isExpression && isNegative % 2 != 0){
//                    newStatement += "!"+root.getRawSignature();
//                }else newStatement += root.getRawSignature();
//
//                return newStatement;
//            }
            else if(root instanceof IASTExpressionStatement){
                root = ((IASTExpressionStatement) root).getExpression();
                newStatement = reconstructString((IASTExpression) root, isNegative, isExpression, isBinaryExp);
            }
//            else if (root instanceof IASTIdExpression){
//                String fr = root.getRawSignature();
//                if(isNegative % 2 != 0){
//                    newStatement += "!"+fr;
//                }else newStatement += fr;
//                    return newStatement;
//            }
            else{
                String fr = root.getRawSignature();
                if(isExpression && isNegative % 2 != 0){
                    newStatement += "!"+fr;
                }
                else if(!isBinaryExp && isNegative % 2 != 0){
                    newStatement += "!"+fr;
                }
                else newStatement += fr;
                return newStatement;
            }
        }
    };
    String specialCase (String newStatement){
        char[] chars = newStatement.replaceAll("\\s", "").toCharArray();
        int start = 0;
        int end = 0;
        for (char c : chars) {
            if(Character.toString(c).equals("(") )
                start++;
            else if(Character.toString(c).equals(")")){
                end++;
            }
        }
        if(start == 2 && end == 2 && newStatement.contains("((") && newStatement.contains("))")){
            newStatement = newStatement.substring(1,newStatement.length()-1);
        }
        return newStatement;
    }
}
