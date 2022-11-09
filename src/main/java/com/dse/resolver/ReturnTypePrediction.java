package com.dse.resolver;

import com.dse.parser.dependency.finder.MethodFinder;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.IFunctionNode;
import com.dse.parser.object.INode;
import com.dse.parser.object.IVariableNode;
import com.dse.util.VariableTypeUtils;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;

public class ReturnTypePrediction {
    private enum FunctionCalledCase {
        DECLARATION,
        PARAMETER,
        RETURN_STM,
        BODY_STM,
        BINARY_EXPR,
        CASTING,
        UNSUPPORTED
    }

    private IASTNode checkedParent;
    private INode context;

    public ReturnTypePrediction(INode context) {
        this.context = context;
    }

    public String getReturnType(IASTFunctionCallExpression expression) {
        // function called in a declaration (SimpleDeclaration)
        // function called as param of another function call (FunctionCallExpression)
        // function called in assignment (ExpressionStatement - binary expression)
        // function called in return statement (ReturnStatement)
        // function called as a statement (ExpressionStatement - function call expr)
        // function called in for/while/if
        FunctionCalledCase calledCase = getCaseOfCalled(expression);
        switch (calledCase) {
            case DECLARATION:
                IASTSimpleDeclaration declaration = (IASTSimpleDeclaration) checkedParent;
                return declaration.getDeclSpecifier().getRawSignature();
            case BINARY_EXPR:
                IASTBinaryExpression binaryExpression = (IASTBinaryExpression) checkedParent;
                return predictBinaryExprCase(binaryExpression, expression);
            case PARAMETER:
                IASTFunctionCallExpression callee = (IASTFunctionCallExpression) checkedParent;
                return predictParameterCase(callee, expression);
            case RETURN_STM:
                if (context instanceof IFunctionNode)
                    return ((IFunctionNode) context).getReturnType();
            case BODY_STM:
                return VariableTypeUtils.VOID_TYPE.VOID;
            case CASTING:
                return ((IASTExpression) checkedParent).getExpressionType().toString();
        }

        return null;
    }

    private String predictParameterCase(IASTFunctionCallExpression callee, IASTFunctionCallExpression called) {
        IASTInitializerClause[] args = callee.getArguments();
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof IASTFunctionCallExpression) {
                if (args[i] == called) {
                    MethodFinder finder = new MethodFinder(context);
                    finder.ignoreArgsType = true;
                    ICommonFunctionNode calleeFuntionNode = (ICommonFunctionNode) finder.find(callee);

                    if (calleeFuntionNode == null)
                        return null;

                    IVariableNode correspondingVar = calleeFuntionNode.getArguments().get(i);
                    return correspondingVar.getRealType();
                }
            }
        }

        return null;
    }

    private String predictBinaryExprCase(IASTBinaryExpression binaryExpression, IASTFunctionCallExpression expression) {
        IASTExpression variable;

        int operator = binaryExpression.getOperator();
        if (operator == IASTBinaryExpression.op_shiftLeft || operator == IASTBinaryExpression.op_shiftRight
                || operator == IASTBinaryExpression.op_shiftLeftAssign
                || operator == IASTBinaryExpression.op_shiftRightAssign)
        return VariableTypeUtils.BASIC.NUMBER.INTEGER.INT;

        if (expression == binaryExpression.getOperand2())
            variable = binaryExpression.getOperand1();
        else
            variable = binaryExpression.getOperand2();

        return new NewTypeResolver(context, 0).exec(variable);
    }

    private FunctionCalledCase getCaseOfCalled(IASTFunctionCallExpression functionCallExpr) {
        IASTNode parent = functionCallExpr.getParent();
        while (parent != null) {

            if (parent instanceof IASTCastExpression) {
                checkedParent = parent;
                return FunctionCalledCase.CASTING;
            } else if (parent instanceof IASTBinaryExpression) {
                checkedParent = parent;
                return FunctionCalledCase.BINARY_EXPR;
            } else if (parent instanceof IASTSimpleDeclaration) {
                checkedParent = parent;
                return FunctionCalledCase.DECLARATION;
            } else if (parent instanceof IASTFunctionCallExpression) {
                checkedParent = parent;
                return FunctionCalledCase.PARAMETER;
            } else if (parent instanceof IASTReturnStatement) {
                checkedParent = parent;
                return FunctionCalledCase.RETURN_STM;
            } else if (parent instanceof IASTExpressionStatement) {
                IASTExpression expr = ((IASTExpressionStatement) parent).getExpression();
                if (expr instanceof IASTFunctionCallExpression) {
                    checkedParent = parent;
                    return FunctionCalledCase.BODY_STM;
                } else parent = parent.getParent();
            } else
                parent = parent.getParent();
        }

        return FunctionCalledCase.UNSUPPORTED;
    }
}