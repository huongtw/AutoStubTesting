package com.dse.testdata.object;

/**
 * Represent variable as pointer (one level, two level, etc.)
 *
 * @author ducanhnguyen
 */
public class PointerCharacterDataNode extends PointerDataNode {
//    @Override
//    public String getInputForGoogleTest() throws Exception {
//        if (Environment.getInstance().isC()) {
//            String input = "";
//
//            String type = VariableTypeUtils
//                    .deleteStorageClasses(getType().replace(IDataNode.REFERENCE_OPERATOR, ""));
//
//            String coreType = "";
//            if (getChildren() != null && !getChildren().isEmpty())
//                coreType = ((ValueDataNode) getChildren().get(0)).getType();
//            else
//                coreType = type.substring(0, type.lastIndexOf('*'));
//
//            if (isExternel())
//                type = "";
//
//            String allocation = "";
//
//            String tempName = getVituralName() + "_temp";
//            tempName = tempName.replaceAll("[^\\w]", "_");
//
//            if (isPassingVariable() || isSTLListBaseElement() || isInConstructor() || isGlobalExpectedValue() || isSutExpectedArgument()) {
//
//                if (this.isNotNull()) {
//                    allocation = String.format("%s %s;\n", coreType, tempName);
//                    allocation += String.format("%s %s = &%s" + SpecialCharacter.END_OF_STATEMENT, type,
//                            this.getVituralName(), tempName);
//                } else {
//                    allocation = String.format("%s %s = " + IDataNode.NULL_POINTER_IN_C + SpecialCharacter.END_OF_STATEMENT,
//                            type, this.getVituralName());
//                }
//                input += allocation;
//            } else if (isArrayElement() || isAttribute()) {
//                if (this.isNotNull()) {
//                    allocation = String.format("%s %s;\n", coreType, tempName);
//                    allocation += String.format("%s = &%s" + SpecialCharacter.END_OF_STATEMENT,
//                            this.getVituralName(), tempName);
//                } else
//                    allocation = String.format("%s = " + IDataNode.NULL_POINTER_IN_C + SpecialCharacter.END_OF_STATEMENT
//                            , this.getVituralName());
//                input += allocation;
//            } else {
//                if (this.isNotNull()) {
//                    allocation = String.format("%s %s;\n", coreType, tempName);
//                    allocation += String.format("%s = &%s" + SpecialCharacter.END_OF_STATEMENT,
//                            this.getVituralName(), tempName);
//                } else
//                    allocation = String.format("%s = " + IDataNode.NULL_POINTER_IN_C + SpecialCharacter.END_OF_STATEMENT
//                            , this.getVituralName());
//                input += allocation;
//            }
//
//            input = input.replace(SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS, SpecialCharacter.EMPTY);
//
//            return input + SpecialCharacter.LINE_BREAK + superSuperInputGTest();
//        } else
//            return super.getInputForGoogleTest();
//    }
}
