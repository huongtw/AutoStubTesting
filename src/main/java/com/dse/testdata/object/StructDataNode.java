package com.dse.testdata.object;

import com.dse.environment.Environment;
import com.dse.parser.object.INode;
import com.dse.util.SpecialCharacter;
import com.dse.util.VariableTypeUtils;

/**
 * Represent struct variable
 *
 * @author DucAnh
 */
public class StructDataNode extends StructureDataNode {

    @Override
    public String getInputForGoogleTest(boolean isDeclared) throws Exception {
        if (isUseUserCode()) {
            if (isPassingVariable() && !isDeclared)
                return SpecialCharacter.EMPTY;
            else
                return getUserCodeContent();
        }

        if (Environment.getInstance().isC()) {
            return getCInputGTest(isDeclared);
        } else
            return getCppInputGTest(isDeclared);
    }

    private String getCInputGTest(boolean isDeclared) throws Exception {
        String input = "";
        if(!isPassingVariable() || (isPassingVariable() && isDeclared) ){
            String typeVar = this.getRawType().replace(IDataNode.REFERENCE_OPERATOR, "");
            typeVar = VariableTypeUtils.deleteStorageClassesExceptConst(typeVar);

            INode correspondingType = getCorrespondingType();

            typeVar = typeVar.replace(SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS, SpecialCharacter.EMPTY);

//        if (correspondingType instanceof StructureNode && !((StructureNode) correspondingType).haveTypedef()) {
//            if (!typeVar.startsWith("struct"))
//                typeVar = "struct " + typeVar;
//        }

            if (isExternel())
                typeVar = "";

            if (this.isPassingVariable()){
                input += typeVar +" " + this.getVituralName() + SpecialCharacter.END_OF_STATEMENT;

            } else if (getParent() instanceof OneDimensionDataNode || getParent() instanceof PointerDataNode){
                input += "";

            } else if (isSutExpectedArgument() || isGlobalExpectedValue()) {
                input += typeVar +" " + this.getVituralName() + SpecialCharacter.END_OF_STATEMENT;

            } else if (isInstance()) {
                input += "";

            } else if (isVoidPointerValue()) {
                input += typeVar +" " + this.getVituralName() + SpecialCharacter.END_OF_STATEMENT;
            }
        }

        return  input + SpecialCharacter.LINE_BREAK + super.getInputForGoogleTest(isDeclared);
    }

    private String getCppInputGTest(boolean isDeclared) throws Exception {
        String input = "";
        if(!isPassingVariable() || (isPassingVariable() && isDeclared) ){
            String typeVar = this.getRawType().replace(IDataNode.REFERENCE_OPERATOR, "");
            typeVar = VariableTypeUtils.deleteStorageClassesExceptConst(typeVar);

            if (isExternel())
                typeVar = "";

            if (isInstance()) {
                typeVar = getRealType();
                input += String.format("%s = (%s*) malloc(sizeof(%s));", getVituralName(),typeVar, typeVar);

            } else if (this.isPassingVariable()){
                input += typeVar +" " + this.getVituralName() + SpecialCharacter.END_OF_STATEMENT;

            } else if (getParent() instanceof OneDimensionDataNode || getParent() instanceof PointerDataNode){
                input += "";

            } else if (isSutExpectedArgument() || isGlobalExpectedValue()) {
                input += typeVar +" " + this.getVituralName() + SpecialCharacter.END_OF_STATEMENT;

            } else if (isInstance()) {
                input += "";

            }
//        else if (isPassingVariable())  {
//            input += typeVar +" " + this.getVituralName() + SpecialCharacter.END_OF_STATEMENT;
//        }
        }

        return  input + SpecialCharacter.LINE_BREAK + super.getInputForGoogleTest(isDeclared);
    }

    @Override
    public StructDataNode clone() {
        StructDataNode clone = (StructDataNode) super.clone();

        for (IDataNode child : getChildren()) {
            if (child instanceof ValueDataNode) {
                ValueDataNode cloneChild = ((ValueDataNode) child).clone();
                clone.getChildren().add(cloneChild);
                cloneChild.setParent(clone);
            }
        }

        return clone;
    }
}
