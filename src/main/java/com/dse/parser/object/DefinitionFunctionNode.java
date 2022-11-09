package com.dse.parser.object;

import com.dse.config.FunctionConfig;
import com.dse.config.IFunctionConfig;
import com.dse.config.Paths;
import com.dse.parser.ProjectParser;
import com.dse.search.Search;
import com.dse.search.condition.DefinitionFunctionNodeCondition;
import com.dse.util.SpecialCharacter;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclaration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Represent method are only declared but not defined in this function
 *
 * @author DucAnh
 */
public class DefinitionFunctionNode extends CustomASTNode<CPPASTSimpleDeclaration> implements ICommonFunctionNode{

    private int visibility;

    public static void main(String[] args) {
        ProjectParser parser = new ProjectParser(new File(Paths.BTL));
        parser.setExpandTreeuptoMethodLevel_enabled(true);
        parser.setCpptoHeaderDependencyGeneration_enabled(true);
        INode func = Search.searchNodes(parser.getRootTree(), new DefinitionFunctionNodeCondition(), "nhap_qua_File(ifstream&)")
                .get(0);
    }

    public void setNameForArgsWithNoName() {
        int count = 0;
        for (INode child : getChildren()) {
            if (child instanceof IVariableNode) {
                if (child.getName().equals("")) {
                    child.setName("arg_" + count);
                    count++;
                }
            }
        }
    }

    @Override
    public List<IVariableNode> getArguments() {
        List<IVariableNode> arguments = new ArrayList<>();

//        if (this.getReturnType() == null || this.getReturnType().length() == 0) {
//            // constructor node
//            int index = 0;
//            for (INode child : getChildren())
//                if (child instanceof IVariableNode) {
//                    index++;
//                    //if (child.getName() == null || child.getName().length() == 0)
//                    // some definition functions does not have the name of arguments, we need to generate a virtual name
//                    child.setName(ConstructorNode.PREFIX_NAME_BY_INDEX + index);
//                    arguments.add((IVariableNode) child);
//                }
//        } else {
            for (INode child : getChildren())
                if (child instanceof IVariableNode) {
                    arguments.add((IVariableNode) child);
                }
//        }

        return arguments;
    }

    @Override
    public String getNewType() {
        StringBuilder output = new StringBuilder(getAST().getDeclarators()[0].getName().getRawSignature());
//        String output = getAST().getDeclarators()[0].getName().toString();
        output.append("(");
        for (IVariableNode paramater : getArguments())
            output.append(paramater.getRawType()).append(",");
        output.append(")");
        output = new StringBuilder(output.toString().replace(",)", ")").replaceAll("\\s*\\)", "\\)"));
        return output.toString();
    }

    @Override
    public String getReturnType() {
        IASTDeclSpecifier declSpecifier = getAST().getDeclSpecifier();
        String returnType = declSpecifier.getRawSignature();
        if (declSpecifier instanceof IASTNamedTypeSpecifier) {
            returnType = String.valueOf(((IASTNamedTypeSpecifier) declSpecifier).getName().toCharArray());
        } else if (declSpecifier instanceof IASTSimpleDeclSpecifier) {
            returnType = declSpecifier.toString();
        }

        for (IASTNode operator : getAST().getDeclarators()[0].getPointerOperators())
            if (operator.getRawSignature().equals("*"))
                returnType += "*";

        return returnType;
    }

    @Override
    public String getSimpleName() {
        String name = getName();
        int end = name.indexOf('(');
        return name.substring(0, end).replaceAll(" ", "");

//        return Utils.getCPPASTNames(getAST()).get(0).getRawSignature();
    }

    @Override
    public String getSingleSimpleName() {
        String singleSimpleName = getSimpleName();
        if (!singleSimpleName.contains(SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS))
            return singleSimpleName;
        else
            return singleSimpleName
                    .substring(singleSimpleName.lastIndexOf(SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS) + 2);
    }

    @Override
    public void setAST(CPPASTSimpleDeclaration aST) {
        super.setAST(aST);

        // find arguments
        for (IASTNode child : getAST().getDeclarators()[0].getChildren())
            if (child instanceof IASTParameterDeclaration) {
                IASTParameterDeclaration astArgument = (IASTParameterDeclaration) child;

                VariableNode argumentNode = new VariableNode();
                argumentNode.setAST(astArgument);
                argumentNode.setParent(this);
                argumentNode.setAbsolutePath(getAbsolutePath() + File.separator + argumentNode.getNewType());
                getChildren().add(argumentNode);
            }
    }

    @Override
    public boolean isTemplate() {
        return AST.getParent() instanceof ICPPASTTemplateDeclaration;
    }


    @Override
    public int getVisibility() {
        if (visibility > 0)
            return visibility;

        INode realParent = getParent();

        if (!(realParent instanceof StructureNode)) {
            visibility = ICPPASTVisibilityLabel.v_public;
        } else {
            String name = getSingleSimpleName();

            IASTSimpleDeclaration astStructure = ((StructureNode) realParent).AST;
            IASTDeclSpecifier declSpec = astStructure.getDeclSpecifier();

            visibility = realParent instanceof ClassNode ?
                    ICPPASTVisibilityLabel.v_private : ICPPASTVisibilityLabel.v_public;

            if (declSpec instanceof IASTCompositeTypeSpecifier) {
                IASTDeclaration[] declarations = ((IASTCompositeTypeSpecifier) declSpec).getDeclarations(true);
                for (IASTDeclaration declaration : declarations) {
                    if (declaration instanceof ICPPASTVisibilityLabel)
                        visibility = ((ICPPASTVisibilityLabel) declaration).getVisibility();
                    else if (declaration instanceof IASTSimpleDeclaration) {
                        IASTDeclarator declarator = ((IASTSimpleDeclaration) declaration).getDeclarators()[0];

                        if (declarator instanceof ICPPASTFunctionDeclarator) {
                            String functionName = declarator.getName().getLastName().getRawSignature();
                            int parameters = ((ICPPASTFunctionDeclarator) declarator).getParameters().length;

                            if (functionName.equals(name) && getArguments().size() == parameters)
                                return visibility;
                        }
                    }
                }
            }
        }

        return visibility;
    }

    @Override
    public IFunctionConfig getFunctionConfig() {
        return null;
    }

    @Override
    public void setFunctionConfig(FunctionConfig functionConfig) {
        // nothing to do
    }

    @Override
    public boolean isMethod() {
        return parent instanceof StructureNode;
    }

    @Override
    public List<IVariableNode> getArgumentsAndGlobalVariables() {
        return new ArrayList<>();
    }

    @Override
    public String getNameOfFunctionConfigJson() {
        return "no-name.json";
    }

    @Override
    public String getTemplateFilePath() {
        return null;
    }

    @Override
    public String getNameOfFunctionConfigTab() {
        return "no-name";
    }

    @Override
    public List<IVariableNode> getExternalVariables() {
        return new ArrayList<>();
    }

    @Override
    public boolean hasVoidPointerArgument() {
        return false;
    }

    @Override
    public boolean hasFunctionPointerArgument() {
        return false;
    }

    @Override
    public List<StaticVariableNode> getStaticVariables() {
        return new ArrayList<>();
    }
}
