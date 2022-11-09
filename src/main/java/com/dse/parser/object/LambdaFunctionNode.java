package com.dse.parser.object;

import com.dse.config.IFunctionConfig;
import com.dse.resolver.NewTypeResolver;
import com.dse.testdata.object.NullPointerDataNode;
import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;
import com.dse.util.VariableTypeUtils;
import com.dse.util.VariableTypeUtilsForStd;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLambdaExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTLambdaExpression;

import java.util.List;
import java.util.stream.Collectors;

// IASTFunctionDefinition
public class LambdaFunctionNode extends AbstractFunctionNode {

    private ICPPASTLambdaExpression originalAST;
    private String body;
//    String name;
    private String simpleName;

    public String getBody() {
        return body;
    }

    public void setBody(IASTExpression expression) {
        this.body = ((CPPASTLambdaExpression) expression).getBody().getRawSignature();
    }

    @Override
    public String getNameOfFunctionConfigTab() {
        return null;
    }

    public ICPPASTLambdaExpression getOriginalAST() {
        return originalAST;
    }

    private IASTFunctionDefinition convertToStandardFunctionAST(ICPPASTLambdaExpression lambdaExpression){
        String newFunctionSrc = getReturnType() + SpecialCharacter.SPACE +
                getSimpleName() +
                generateCallOfArguments(this) +
                lambdaExpression.getBody().getRawSignature();

        IASTFunctionDefinition convertedAST = Utils.getFunctionsinAST(newFunctionSrc.toCharArray()).get(0);
        setAST(convertedAST);
        return convertedAST;
    }
    public static StringBuilder generateCallOfArguments(ICommonFunctionNode functionNode){
        StringBuilder functionCall = new StringBuilder();
        functionCall.append("(");
        for (IVariableNode v : functionNode.getArguments())
            if (VariableTypeUtilsForStd.isUniquePtr(v.getRawType()))
                functionCall.append(String.format("std::move(%s),", v.getName()));

            else if (VariableTypeUtils.isNullPtr(v.getRawType())) {
                functionCall.append(NullPointerDataNode.NULL_PTR).append(",");

            } else if (v.resolveCoreType() instanceof FunctionPointerTypeNode && v.getName().isEmpty())
                functionCall.append(((FunctionPointerTypeNode) v.resolveCoreType()).getFunctionName()).append(",");
            else
                functionCall.append(v.getRawType() + " " + v.getName()).append(",");

        functionCall.append(")");
        functionCall = new StringBuilder(functionCall.toString().replace(",)", ")"));
        return functionCall;
    }
    public void setOriginalAST(ICPPASTLambdaExpression aST) {
        this.originalAST = aST;
        int offset = getOriginalAST().getFileLocation().getNodeOffset();
        this.simpleName = "akaLambdaFunction" + offset;
        StringBuilder nameBuilder = new StringBuilder(simpleName);
        nameBuilder.append("(");

        if (originalAST.getDeclarator() != null)
            for (IASTNode child : getOriginalAST().getDeclarator().getChildren()){
                if (child instanceof IASTParameterDeclaration) {
                    IASTParameterDeclaration astArgument = (IASTParameterDeclaration) child;
                    VariableNode argumentNode = new InternalVariableNode();
                    argumentNode.setAST(astArgument);
                    argumentNode.setParent(this);
                    nameBuilder.append(argumentNode.getRawType()).append(",");
                    getChildren().add(argumentNode);
                }
            }
        nameBuilder.append(")");
        String name = nameBuilder.toString();
        name = name.replace(",)", ")");
        setName(name);

        convertToStandardFunctionAST(originalAST);
    }

    @Override
    public IFunctionConfig getFunctionConfig() {
        return null;
    }

    @Override
    public List<IVariableNode> getArguments() {
        return getChildren().stream()
                .filter(node -> node instanceof VariableNode)
                .map(node -> (IVariableNode) node)
                .collect(Collectors.toList());
    }

//    public List<IVariableNode> getArgumentsAndGlobalVariables(LambdaFunctionNode lambdaFunctionNode) {
//        return null;
//    }

    @Override
    public String getReturnType() {
        if (this.getOriginalAST().getDeclarator() != null) {
            if (this.getOriginalAST().getDeclarator().getTrailingReturnType() != null)
                return this.getOriginalAST().getDeclarator().getTrailingReturnType().getDeclSpecifier().getRawSignature();
        } else {
            for (IASTStatement statement : getOriginalAST().getBody().getStatements()) {
                if (statement instanceof IASTReturnStatement) {
                    IASTExpression returnExpr = ((IASTReturnStatement) statement).getReturnValue();
                    String returnType = new NewTypeResolver(getParent()).solve(returnExpr);
                    if (returnType == null)
                        return "auto";
                    else
                        return returnType;
                }
//            return ((IASTReturnStatement) getOriginalAST().getBody().getStatements()[0]).getReturnValue().getExpressionType().toString();

            }
        }
        return "void";
    }

    @Override
    public String getSimpleName() {
        return simpleName;
    }

    @Override
    public String getSingleSimpleName() {
        return simpleName;
    }

    @Override
    public boolean isTemplate() {
        return false;
    }

    @Override
    public int getVisibility() {
        return ICPPASTVisibilityLabel.v_public;
    }

//    @Override
//    public String getNameOfFunctionConfigJson() {
//        return null;
//    }
//
//
//    @Override
//    public String getTemplateFilePath() {
//        return null;
//    }
//
//    @Override
//    public List<IVariableNode> getExternalVariables() {
//        return null;
//    }
//
//    @Override
//    public boolean hasVoidPointerArgument() {
//        return false;
//    }
//
//    @Override
//    public boolean hasFunctionPointerArgument() {
//        return false;
//    }
}
