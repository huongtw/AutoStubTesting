package com.dse.testdata.object;

import com.dse.parser.dependency.finder.Level;
import com.dse.parser.dependency.finder.VariableSearchingSpace;
import com.dse.parser.object.*;
import com.dse.parser.object.INode;
import com.dse.resolver.DeclSpecSearcher;
import com.dse.search.Search;
import com.dse.search.Search2;
import com.dse.testdata.InputCellHandler;
import com.dse.util.*;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclaration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Represent class variable
 */
public class TemplateClassDataNode extends ClassDataNode {
    /**
     * Get the real class data node
     *
     * @return the real class data node
     */
    public SubClassDataNode getSubClass() {
        return subClass;
    }

    /**
     * Get the list of the derived class of current class
     *
     * @return the list of the derived class of current class
     */
    @Override
    public List<INode> getDerivedClass() {
        ClassNode typeNode = (ClassNode) getCorrespondingType();
        ICommonFunctionNode functionNode = getTestCaseRoot().getFunctionNode();

        String type = getCorrespondingVar().getRawType();
        String[] templateArguments = TemplateUtils.getTemplateArguments(type);
        for (int i = 0; i < templateArguments.length; i++) {
            templateArguments[i] = TemplateUtils.deleteTemplateParameters(templateArguments[i]);
            templateArguments[i] = VariableTypeUtils.getSimpleRealType(templateArguments[i]);
        }

        List<INode> output = new ArrayList<>();
        List<INode> derivedClass = Search.getDerivedNodesInSpace(typeNode, functionNode);
        List<Level> space = new VariableSearchingSpace(functionNode).generateExtendSpaces();

        for (INode derived : derivedClass) {
            String regex = derived.getName() + (((ClassNode) derived).isTemplate() ? "<.+>" : "");
            DeclSpecSearcher searcher = new DeclSpecSearcher(regex, space, false);

            for (IASTDeclSpecifier declSpec : searcher.getDeclSpecs()) {
                String derivedType = declSpec.getRawSignature();

                derivedType = TemplateUtils.recursiveGetBaseClass(derivedType, (ClassNode) derived, derivedClass);

                String[] derivedTemplateArguments = TemplateUtils.getTemplateArguments(derivedType);

                if (templateArguments.length == derivedTemplateArguments.length) {
                    boolean isEquals = true;

                    if (getParent() instanceof RootDataNode && ((RootDataNode) getParent()).getLevel() == NodeType.GLOBAL) {

                    } else {
                        for (int i = 0; i < templateArguments.length; i++) {
                            derivedTemplateArguments[i] = TemplateUtils.deleteTemplateParameters(derivedTemplateArguments[i]);
                            derivedTemplateArguments[i] = VariableTypeUtils.getSimpleRealType(derivedTemplateArguments[i]);

                            if (!derivedTemplateArguments[i].equals(templateArguments[i])) {
                                isEquals = false;
                                break;
                            }
                        }
                    }

                    if (isEquals) {
                        INode clone = derived.clone();
                        clone.setName(declSpec.getRawSignature());
                        output.add(clone);
                    }
                }
            }

        }

        if (output.isEmpty()) {
            INode clone = typeNode.clone();

            String name = type;
            if (name.startsWith(VariableTypeUtils.STD_SCOPE))
                name = type.replaceFirst(VariableTypeUtils.STD_SCOPE, SpecialCharacter.EMPTY);

            clone.setName(name);
            output.add(clone);
        }

        return output;
    }

    /**
     * Set the sub class data node
     *
     * @param classNode represent the real class type
     */
    public void setSubClass(INode classNode) {
        super.setSubClass(classNode);

        updateInstanceVariable(classNode);

        // Subprogram under test is method of current instance
        // then expand with corresponding template arguments
        if (isMethodOfInstance(classNode)) {
            ICommonFunctionNode sut = getTestCaseRoot().getFunctionNode();

            if (sut instanceof IFunctionNode) {
                INode realParent = sut.getParent();

                if (((IFunctionNode) sut).getRealParent() != null)
                    realParent = ((IFunctionNode) sut).getRealParent();

                String prototype = generateMethodPrototype((IFunctionNode) sut, realParent, classNode);
                expandTemplateMethod(prototype, (IFunctionNode) sut);
            }
        }
    }

    private void updateInstanceVariable(INode classNode) {
        IVariableNode correspondingVar = getCorrespondingVar();
        if (correspondingVar instanceof InstanceVariableNode) {
            String newType = TemplateUtils.recursiveGetBaseClass(classNode.getName(), (ClassNode) classNode, getDerivedClass());
            String oldType = correspondingVar.getRawType();

            newType = oldType.substring(0, oldType.indexOf(TemplateUtils.OPEN_TEMPLATE_ARG))
                    + newType.substring(newType.indexOf(TemplateUtils.OPEN_TEMPLATE_ARG));

            correspondingVar.setRawType(newType);
            correspondingVar.setReducedRawType(newType);
            correspondingVar.setCoreType(TemplateUtils.getCoreType(newType));
        }
    }

    public boolean isMethodOfInstance(INode classNode) {
        ICommonFunctionNode sut = getTestCaseRoot().getFunctionNode();

        if (!(sut instanceof IFunctionNode))
            return false;

        INode realParent = ((IFunctionNode) sut).getRealParent() == null ? sut.getParent() : ((IFunctionNode) sut).getRealParent();

        RootDataNode root = (RootDataNode) getRoot();

        return  (getName().startsWith(SourceConstant.INSTANCE_VARIABLE)
                && root.getLevel() == NodeType.GLOBAL && sut.isTemplate()
                && realParent instanceof ClassNode && ((ClassNode) realParent).isTemplate()
                && (realParent.getAbsolutePath().equals(classNode.getAbsolutePath()))
                    || realParent.getAbsolutePath().equals(classNode.getAbsolutePath()
                        + File.separator + classNode.getNewType()));
    }

    private void expandTemplateMethod(String prototype, IFunctionNode sut) {
        try {
            DefinitionFunctionNode newFunction = generateDefinitionForMethod(prototype, sut);

            SubprogramNode sutDataNode = Search2.findSubprogramUnderTest(getTestCaseRoot());
            if (sutDataNode instanceof TemplateSubprogramDataNode) {
                sutDataNode.getChildren().clear();
                ((TemplateSubprogramDataNode) sutDataNode).setDefinition(newFunction);

            }

        } catch (Exception ex) {
            InputCellHandler.logger.error("cant expand template method " + sut.getName()
                    + " by prototype " + prototype + ": " + ex.getMessage());
        }
    }

    private String generateMethodPrototype(IFunctionNode sut, INode realParent, INode classNode) {
        String prototype = sut.toString();
        String[] templateParams = TemplateUtils.getTemplateParameters(realParent);
        String[] templateArguments = TemplateUtils.getTemplateArguments(classNode.getName());

        for (int i = 0; i < templateParams.length; i++) {
            prototype = prototype.replaceAll("\\b" + Pattern.quote(templateParams[i]) + "\\b", templateArguments[i]);
        }

        return prototype;
    }

    private DefinitionFunctionNode generateDefinitionForMethod(String prototype, IFunctionNode functionNode) {
        IASTNode ast = Utils.convertToIAST(prototype);

        if (ast instanceof IASTDeclarationStatement)
            ast = ((IASTDeclarationStatement) ast).getDeclaration();

        if (ast instanceof CPPASTSimpleDeclaration) {
            DefinitionFunctionNode suggestion = new DefinitionFunctionNode();
            suggestion.setAbsolutePath(functionNode.getAbsolutePath());
            suggestion.setAST((CPPASTSimpleDeclaration) ast);
            suggestion.setName(suggestion.getNewType());

            return suggestion;
        }

        return null;
    }

    @Override
    protected SubClassDataNode createSubClassDataNode() {
        return new SubTemplateClassDataNode();
    }
}