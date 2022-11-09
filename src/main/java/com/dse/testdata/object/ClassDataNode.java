package com.dse.testdata.object;

import com.dse.parser.dependency.finder.VariableSearchingSpace;
import com.dse.parser.object.*;
import com.dse.parser.object.INode;
import com.dse.resolver.DeclSpecSearcher;
import com.dse.search.Search;
import com.dse.util.TemplateUtils;
import com.dse.util.VariableTypeUtils;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represent class variable
 */
public class ClassDataNode extends StructureDataNode {

    /**
     * SubClass represent the real class data node
     */
    protected SubClassDataNode subClass = null;

    /**
     * Set the sub class data node
     *
     * @param classNode represent the real class type
     */
    public void setSubClass(INode classNode) {
        if (classNode instanceof ClassNode) {
            String fullClassName = Search.getScopeQualifier(classNode);

            VariableNode correspondingVar = VariableTypeUtils
                    .cloneAndReplaceType(fullClassName, getCorrespondingVar(), classNode);

            expandSubClass(correspondingVar);
        } else
            System.out.println(classNode.getAbsolutePath() + " is not a class node");
    }

    private void expandSubClass(VariableNode correspondingVar) {
        subClass = createSubClassDataNode();

        subClass.setName(correspondingVar.getName());
        subClass.setRawType(correspondingVar.getRawType());
        subClass.setRealType(correspondingVar.getRealType());
        subClass.setCorrespondingVar(correspondingVar);

        subClass.setParent(this);
        getChildren().clear();
        addChild(subClass);
    }

    protected SubClassDataNode createSubClassDataNode() {
        return new SubClassDataNode();
    }

    public void setSubClass(SubClassDataNode subClass) {
        this.subClass = subClass;
        subClass.setParent(this);
        getChildren().clear();
        addChild(subClass);
    }

    /**
     * Set the sub class data node
     *
     * @param subClassName the name of the sub class node
     */
    public void setSubClass(String subClassName) {
        if (subClassName.isEmpty()) {
            subClass = null;
            getChildren().clear();
        }

        List<INode> candidateClasses = getDerivedClass();
        if (!candidateClasses.contains(getCorrespondingType()))
            candidateClasses.add(getCorrespondingType());

        for (INode subClassNode: candidateClasses) {
//        for (INode subClassNode: getDerivedClass()) {
            if (subClassNode.getName().equals(subClassName)) {
                setSubClass(subClassNode);
                break;
            }
        }
    }

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
    public List<INode> getDerivedClass() {
        ClassNode typeNode = (ClassNode) getCorrespondingType();

        List<INode> derivedClass = Search.getDerivedNodesInSpace(typeNode, getTestCaseRoot().getFunctionNode());

        List<INode> listWithoutAbstract = derivedClass.stream()
                .filter(s -> !((StructOrClassNode) s).isAbstract())
                .collect(Collectors.toList());

        if (!listWithoutAbstract.isEmpty())
            derivedClass = listWithoutAbstract;

        addTemplateDerivedClass(derivedClass);

        return derivedClass;
    }

    private void addTemplateDerivedClass(List<INode> derivedClass) {
        ICommonFunctionNode functionNode = getTestCaseRoot().getFunctionNode();

        for (INode classNode : derivedClass) {
            if (classNode instanceof ClassNode && ((ClassNode) classNode).isTemplate()) {
                derivedClass.remove(classNode);

                String[] templateParams = TemplateUtils.getTemplateParameters(classNode);
                if (templateParams != null) {

                    VariableSearchingSpace space = new VariableSearchingSpace(functionNode);
                    DeclSpecSearcher searcher = new DeclSpecSearcher(String.format("%s<.+>", classNode.getName()),
                            space.generateExtendSpaces(), false);

                    for (IASTDeclSpecifier declSpec : searcher.getDeclSpecs()) {
                        String simpleType = VariableTypeUtils.getSimpleRealType(declSpec.getRawSignature());
                        String[] templateArguments = TemplateUtils.getTemplateArguments(simpleType);

                        if (templateArguments.length == templateParams.length) {
                            INode clone = classNode.clone();
                            clone.setName(simpleType);

                            derivedClass.add(clone);
                        }
                    }
                }
            }
        }
    }

    /**
     * Check whether sub class is set yet
     *
     * @return true if subclass is set
     */
    public boolean isSetSubClass() {
        return getSubClass() != null;
    }
}