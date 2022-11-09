package com.dse.testdata.object;

import com.dse.util.SpecialCharacter;
import com.dse.util.TemplateUtils;
import com.dse.util.VariableTypeUtils;

public class OneDimensionPointerDataNode extends OneDimensionDataNode {

    @Override
    public String getInputForGoogleTest(boolean isDeclared) throws Exception {
        String declaration = "";

        if (isUseUserCode()) {
            if (isPassingVariable() && !isDeclared)
                return SpecialCharacter.EMPTY;
            else
                return getUserCodeContent();
        }

        if(!isPassingVariable() || (isPassingVariable() && isDeclared) ){
            // get type
            String type = VariableTypeUtils
                    .deleteStorageClassesExceptConst(this.getRawType().replace(IDataNode.REFERENCE_OPERATOR, ""));

            String coreType = type.replaceAll("\\[.*\\]", "");

            if (TemplateUtils.isTemplate(type))
                if (!getChildren().isEmpty()) {
                    IDataNode first = getChildren().get(0);
                    if (first instanceof ValueDataNode)
                        coreType = ((ValueDataNode) first).getRawType();
                }

            if (isExternel() && !isDeclared) {
                coreType = "";
            }

            // get indexes
//        List<String> indexes = Utils.getIndexOfArray(TemplateUtils.deleteTemplateParameters(type));
//        if (indexes.size() > 0) {
            String dimension = "[" + getSize() + "]";

            // generate declaration
            if (this.isAttribute()) {
                declaration += "";
            } else if (this.isPassingVariable()) {
                if (getSize() > 0) {
                    declaration += String.format("%s %s%s" + SpecialCharacter.END_OF_STATEMENT, coreType,
                            this.getVituralName(), dimension);
                } else {
                    declaration += String.format("%s **%s = NULL" + SpecialCharacter.END_OF_STATEMENT, coreType,
                            this.getVituralName());
                }
            } else if (isSutExpectedArgument() || isGlobalExpectedValue()) {
                if (getSize() > 0) {
                    declaration += String.format("%s %s%s" + SpecialCharacter.END_OF_STATEMENT, coreType,
                            this.getVituralName(), dimension);
                } else {
                    declaration += String.format("%s *%s = NULL" + SpecialCharacter.END_OF_STATEMENT, coreType,
                            this.getVituralName());
                }
            } else if (isVoidPointerValue()) {
                declaration += String.format("%s %s%s" + SpecialCharacter.END_OF_STATEMENT, coreType,
                        this.getVituralName(), dimension);
            }
//        }
        }

        return declaration + SpecialCharacter.LINE_BREAK + super.getInputForGoogleTest(isDeclared);
    }
}
