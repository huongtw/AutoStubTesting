package com.dse.testdata.object;

import com.dse.util.SpecialCharacter;
import com.dse.util.TemplateUtils;
import com.dse.util.VariableTypeUtils;

public class OneDimensionStructureDataNode extends OneDimensionDataNode {

    @Override
    public String getInputForDisplay() throws Exception {
        String input = "";

        for (IDataNode child : this.getChildren())
            input += child.getInputForDisplay();
        if (this.isAttribute())
            input += this.getDotSetterInStr(this.getVituralName()) + SpecialCharacter.LINE_BREAK;
        return input;
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
            String declarationType = VariableTypeUtils
                    .deleteStorageClassesExceptConst(this.getRawType().replace(IDataNode.REFERENCE_OPERATOR, ""));

            String coreType = VariableTypeUtils
                    .deleteStorageClasses(declarationType).replaceAll("\\[.*\\]", "");

            if (TemplateUtils.isTemplate(declarationType))
                if (!getChildren().isEmpty()) {
                    IDataNode first = getChildren().get(0);
                    if (first instanceof ValueDataNode)
                        coreType = ((ValueDataNode) first).getRawType();
                }

            if (isExternel())
                coreType = "";

            int size = getSize();

            if (this.isPassingVariable()){
                if (size > 0) {
                    input += coreType + " " + getVituralName() + "[" + size + "]" + SpecialCharacter.END_OF_STATEMENT;
                } else {
                    input += String.format("%s *%s = NULL" + SpecialCharacter.END_OF_STATEMENT, coreType,
                            this.getVituralName());
                }
            } else if (this.isAttribute()) {
                input += "";
            } else if (isSutExpectedArgument() || isGlobalExpectedValue()) {
                if (size > 0) {
                    input += coreType + " " + getVituralName() + "[" + size + "]" + SpecialCharacter.END_OF_STATEMENT;
                } else {
                    input += String.format("%s *%s = NULL" + SpecialCharacter.END_OF_STATEMENT, coreType,
                            this.getVituralName());
                }
            } else if (isVoidPointerValue()) {
                input += coreType + " " + getVituralName() + "[" + size + "]" + SpecialCharacter.END_OF_STATEMENT;
            }
        }

        return input + SpecialCharacter.LINE_BREAK + super.getInputForGoogleTest(isDeclared);
    }
}
