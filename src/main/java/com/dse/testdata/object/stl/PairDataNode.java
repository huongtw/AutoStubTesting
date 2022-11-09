package com.dse.testdata.object.stl;

import com.dse.testdata.object.IDataNode;
import com.dse.util.SpecialCharacter;
import com.dse.util.VariableTypeUtils;

public class PairDataNode extends STLDataNode {
    public String getFirstType() {
        if (getArguments() != null && getArguments().size() >= 2) {
            return getArguments().get(0);
        }

        return null;
    }

    public String getSecondType() {
        if (getArguments() != null && getArguments().size() >= 2) {
            return getArguments().get(1);
        }

        return null;
    }

    @Override
    public String getInputForGoogleTest(boolean isDeclared) throws Exception {
        String input = "";
        if(!isPassingVariable() || (isPassingVariable() && isDeclared) ){
            // get type of variable
            String typeVar = getRawType().replace(IDataNode.REFERENCE_OPERATOR, "");
            typeVar = VariableTypeUtils.deleteStorageClassesExceptConst(typeVar);
            typeVar = VariableTypeUtils.deleteStorageClassesExceptConst(typeVar);
            typeVar = VariableTypeUtils.deleteStructKeyword(typeVar);
            typeVar = VariableTypeUtils.deleteUnionKeyword(typeVar);

            if (isExternel())
                typeVar = "";

            // generate the statement
            if (this.isPassingVariable()) {
                input += typeVar + " " + this.getVituralName() + SpecialCharacter.END_OF_STATEMENT;

            } else if (isSTLListBaseElement()) {
                input += typeVar + " " + this.getVituralName() + SpecialCharacter.END_OF_STATEMENT;

            } else if (this.isInConstructor()) {
                input += typeVar + " " + this.getVituralName() + SpecialCharacter.END_OF_STATEMENT;
            } else if (isSutExpectedArgument() || isGlobalExpectedValue()) {
                input += typeVar + " " + this.getVituralName() + SpecialCharacter.END_OF_STATEMENT;
            }
        }

        return  input + SpecialCharacter.LINE_BREAK + super.getInputForGoogleTest(isDeclared);
    }
}
