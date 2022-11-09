package com.dse.testdata.object;

import com.dse.parser.dependency.Dependency;
import com.dse.parser.dependency.RealParentDependency;
import com.dse.parser.object.*;
import com.dse.logger.AkaLogger;
import com.dse.parser.object.INode;

import java.util.ArrayList;
import java.util.List;

/**
 * Represent real class variable
 */
public class SubClassDataNode extends ClassDataNode implements IConstructorExpanableDataNode {
    private final static AkaLogger logger = AkaLogger.get(SubClassDataNode.class);
    /**
     * Constructor ma user chon de nhap test data
     */
    protected ICommonFunctionNode selectedContructor;

    public ConstructorDataNode getConstructorDataNode() {
        for (IDataNode child : getChildren())
            if (child instanceof ConstructorDataNode)
                return (ConstructorDataNode) child;

        return null;
    }
//
//    public void setConstructorDataNode(ConstructorDataNode constructorDataNode) {
//        this.constructorDataNode = constructorDataNode;
//    }

    /**
     * Chon mot constructor de nhap test data
     *
     * @param constructor ma user lua chon
     * @throws Exception ontructor khong ton tai!
     */
    public void chooseConstructor(ICommonFunctionNode constructor) throws Exception {
        if (isConstructor(constructor)) {
            selectedContructor = constructor;
        } else throw new Exception("Contructor khong ton tai!");
    }

    // Hoan
    public void chooseConstructor(String constructorName) throws Exception {
//        for (ICommonFunctionNode node: getConstructors()) {
        List<ICommonFunctionNode> constructors = getConstructorsOnlyInCurrentClass();
            for (ICommonFunctionNode node: constructors) {
            // should not use equal because constructor name might have its scope "::"
            if (constructorName.endsWith(node.getName())) {
                chooseConstructor(node);
                break;
            }
        }
    }
    /**
     * Lay tat ca cac constructor cua mot class
     *
     * @return list cac constructor cua class
     */
    public List<ICommonFunctionNode> getConstructorsOnlyInCurrentClass() throws Exception {
        List<ICommonFunctionNode> constructors = new ArrayList<>();

        INode correspondingType = getCorrespondingType();

        if (correspondingType != null) {
            // Find the node corresponding to the current class
            if (((ClassNode) correspondingType).isTemplate())
                correspondingType = correspondingType.getChildren().get(0);

//            INode nodeCorrespondingToType = null;
//            List<INode> subClasses = ((ClassNode) correspondingType).getDerivedNodes();
//            for (INode subClass : subClasses)
//                if (subClass instanceof ClassNode) {
//                    String simpleName = VariableTypeUtils.getSimpleRawType(getType());
//                    if (subClass.getName().equals(simpleName)) {
//                        nodeCorrespondingToType = subClass;
//                        break;
//                    }
//                }

            // get constructors
            ArrayList<ICommonFunctionNode> cons = ((ClassNode) correspondingType).getConstructors();
            for (ICommonFunctionNode con : cons)
                if (!constructors.contains(con))
//                    if (!(con instanceof DefinitionFunctionNode))
                        constructors.add(con);

            for (Dependency dependency : correspondingType.getDependencies())
                if (dependency instanceof RealParentDependency)
                    if (dependency.getEndArrow().getAbsolutePath().equals(correspondingType.getAbsolutePath())) {
                        if (dependency.getStartArrow() instanceof ConstructorNode)
                            constructors.add((ICommonFunctionNode) dependency.getStartArrow());
                    }

        } else
            logger.error("get null corresponding type");

        return constructors;
    }

    /**
     * Lay ra constructor duoc user lua chon
     *
     * @return duoc user lua chon
     */
    public ICommonFunctionNode getSelectedConstructor() {
        return selectedContructor;
    }

    /**
     * Kiem tra mot constructor co thuoc ve class hien tai hay khong?
     *
     * @param constructor can kiem tra
     * @return true - constructor thuoc ve class hien tai va nguoc lai
     */
    private boolean isConstructor(ICommonFunctionNode constructor) {
        try {
            return getConstructorsOnlyInCurrentClass().contains(constructor);
            //return getConstructors().contains(constructor);
        } catch (Exception e) {
            return false;
        }
    }

    public List<IDataNode> getAttributes(){
        List<IDataNode> attributes = new ArrayList<>();
        for (IDataNode child : getChildren())
            if (!(child instanceof ConstructorDataNode))
                attributes.add(child);

        return attributes;
    }

    @Override
    public String getDisplayNameInParameterTree() {
        return getRawType();
    }

    @Override
    public String generateInputToSavedInFile() {
        return getCorrespondingVar().getName() + "=" + getRawType();
    }

//    @Override
//    public String getInputForGoogleTest() throws Exception {
//        String input = super.getInputForGoogleTest();
//
//        if (this.isArrayElement()) {
//            // can not use new
////            input += getVituralName() + " = new " + getType() + "(";
//            input += getVituralName() + " = " + getType() + "(";
//
//            ConstructorDataNode constructor = (ConstructorDataNode) this.getChildren().get(0);
//            for (IDataNode parameter : constructor.getChildren()) {
//                input += parameter.getVituralName() + ",";
//            }
//            input +=")";
//            input = input.replace(",)", ")") + SpecialCharacter.END_OF_STATEMENT;
//
//        } else if (!(this.getParent() instanceof PointerDataNode)) {
//            input += getType() + " " + getVituralName() + "(";
//
//            ConstructorDataNode constructor = (ConstructorDataNode) this.getChildren().get(0);
//            for (IDataNode parameter : constructor.getChildren()) {
//                input += parameter.getVituralName() + ",";
//            }
//            input += ")";
//            input = input.replace(",)", ")") + SpecialCharacter.END_OF_STATEMENT;
//
//        } else {
//            // which case?
//            input += getType() + " " + getVituralName() + " = new " + getType() + "(";
//
//            ConstructorDataNode constructor = (ConstructorDataNode) this.getChildren().get(0);
//            for (IDataNode parameter : constructor.getChildren()) {
//                input += parameter.getVituralName() + ",";
//            }
//            input += ")";
//            input = input.replace(",)", ")") + SpecialCharacter.END_OF_STATEMENT;
//        }
//        return input;
//    }
}