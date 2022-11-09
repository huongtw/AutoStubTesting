package com.dse.parser.object;

import auto_testcase_generation.utils.ASTUtils;
import com.dse.util.SpecialCharacter;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTPointer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Ex: "typedef int (*ListCompareFunc)(ListValue value1, ListValue value2);"
 */
public class FunctionPointerTypedefDeclaration extends TypedefDeclaration implements IFunctionPointerTypeNode {

    private IASTDeclSpecifier returnAst; // "int"
    private List<IVariableNode> arguments;
    private IASTNode refNameAst; // "ListCompareFunc"

    @Override
    public void setAST(IASTSimpleDeclaration aST) {
        super.setAST(aST);
        returnAst = aST.getDeclSpecifier(); // "int"

        if (aST.getDeclarators().length != 1)
            return;

        IASTNode declarator = aST.getDeclarators()[0]; // "(*ListCompareFunc)(ListValue value1, ListValue value2)"
        if (declarator instanceof CPPASTFunctionDeclarator){
            ICPPASTParameterDeclaration[] declarations = ((CPPASTFunctionDeclarator) declarator).getParameters();
            for (ICPPASTParameterDeclaration declaration : declarations) {
                VariableNode argumentNode = new InternalVariableNode();
                argumentNode.setAST(declaration);
                argumentNode.setParent(this);
                if (!(getChildren().contains(argumentNode)))
                    getChildren().add(argumentNode);
            }

            IASTDeclarator functionDeclarator = aST.getDeclarators()[0];
            if (functionDeclarator.getNestedDeclarator() == null)
                refNameAst = functionDeclarator.getName();
            else
                refNameAst = functionDeclarator.getNestedDeclarator().getName();
        }
    }

    public IASTNode getReturnAst() {
        return returnAst;
    }

    @Override
    public String getNewType() {
        return refNameAst.getRawSignature(); // "ListCompareFunc"
    }

    @Override
    public String getOldType() {
        return getAST()
                .getRawSignature()
                .replace(SpecialCharacter.END_OF_STATEMENT, SpecialCharacter.EMPTY)
                .replaceFirst("^typedef\\s+", SpecialCharacter.EMPTY);
    }

    @Override
    public String getReturnType() {
        String returnType = returnAst.getRawSignature();
        if (returnAst instanceof IASTNamedTypeSpecifier) {
            returnType = String.valueOf(((IASTNamedTypeSpecifier) returnAst).getName().toCharArray());
        } else if (returnAst instanceof IASTSimpleDeclSpecifier) {
            returnType = returnAst.toString();
        }

        /*
         * Name of function may contain * character. Ex: SinhVien* StrDel2(char s[],int
         * k,int h){...} ==> * StrDel2(char s[],int k,int h)
         */
        boolean isReturnReference = false;
        IASTDeclarator firstDeclarator = getAST().getDeclarators()[0];
        if (firstDeclarator instanceof IASTFunctionDeclarator) {
            IASTFunctionDeclarator functionDeclarator = (IASTFunctionDeclarator) firstDeclarator;
            IASTNode firstChild = functionDeclarator.getChildren()[0];
            if (firstChild instanceof CPPASTPointer)
                isReturnReference = true;
            returnType += isReturnReference ? "*" : "";
        }

        return returnType;
    }

    public List<IVariableNode> getArguments() {
        if (this.arguments == null || this.arguments.size() == 0) {
            this.arguments = new ArrayList<>();

            for (INode child : getChildren())
                if (child instanceof IVariableNode && !(child instanceof StaticVariableNode)) {
                    this.arguments.add((IVariableNode) child);
                }
        }
        return this.arguments;
    }

    @Override
    public String[] getArgumentTypes() {
        String[] arguments = new String[getArguments().size()];
        for (int i = 0; i < arguments.length; i++) {
            arguments[i] = getArguments().get(i).getRealType();
        }
        return arguments;
    }

    @Override
    public String getFunctionName() {
        return refNameAst.getRawSignature();
    }
}
