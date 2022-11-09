package com.dse.testdata.object;

import com.dse.environment.Environment;
import com.dse.util.SpecialCharacter;
import com.dse.util.VariableTypeUtils;

/**
 * Represent union variable
 *
 * @author ducanhnguyen
 */
public class UnionDataNode extends StructureDataNode {

    private String selectedField;

    public void setField(String selectedField) {
        this.selectedField = selectedField;
    }

    public String getSelectedField() {
        return selectedField;
    }

    @Override
    public String getInputForGoogleTest(boolean isDeclared) throws Exception {
        String input = "";

        if (isUseUserCode()) {
            if (isPassingVariable() && !isDeclared)
                return SpecialCharacter.EMPTY;
            else
                return getUserCodeContent();
        }

        if(!isPassingVariable() || (isPassingVariable() && isDeclared) ){
            String typeVar = this.getRawType().replace(IDataNode.REFERENCE_OPERATOR, "");
            typeVar = VariableTypeUtils.deleteStorageClassesExceptConst(typeVar);

            if (isExternel())
                typeVar = "";

            if (Environment.getInstance().isC()) {
                typeVar = typeVar.replace(SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS, SpecialCharacter.EMPTY);

//            INode correspondingType = getCorrespondingType();
//            if (correspondingType instanceof StructureNode && !((StructureNode) correspondingType).haveTypedef()) {
//                if (!typeVar.startsWith("union"))
//                    typeVar = "union " + typeVar;
//            }
            }

            if (this.isPassingVariable()){
                input += typeVar +" " + this.getVituralName() + SpecialCharacter.END_OF_STATEMENT;

            } else if (getParent() instanceof PointerDataNode) {
//                input += getVituralName() + " = " + VariableTypeUtils.deleteUnionKeyword(typeVar) + "()" + SpecialCharacter.END_OF_STATEMENT;

            } else if (getParent() instanceof OneDimensionDataNode){
                input += "";
            } else if (isSutExpectedArgument() || isGlobalExpectedValue())
                input += typeVar +" " + this.getVituralName() + SpecialCharacter.END_OF_STATEMENT;
            else if (isVoidPointerValue()) {
                input += typeVar +" " + this.getVituralName() + SpecialCharacter.END_OF_STATEMENT;
            }
        }

        String childCode = SpecialCharacter.EMPTY;
        if (selectedField != null) {
            IDataNode child = getChildren().stream()
                    .filter(n -> n.getName().equals(selectedField))
                    .findFirst()
                    .orElse(null);
            if (child != null) {
                childCode = child.getInputForGoogleTest(isDeclared);
            }
        }

        return  input + SpecialCharacter.LINE_BREAK + childCode;
    }

    @Override
    public UnionDataNode clone() {
        UnionDataNode clone = (UnionDataNode) super.clone();

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
