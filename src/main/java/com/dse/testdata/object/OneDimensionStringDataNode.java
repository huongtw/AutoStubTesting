package com.dse.testdata.object;

import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;
import com.dse.util.VariableTypeUtils;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class OneDimensionStringDataNode extends OneDimensionDataNode {
    private String generateDetailedInputforDisplay() throws Exception {
        StringBuilder input = new StringBuilder();
        for (IDataNode child : this.getChildren())
            input.append(child.getInputForDisplay());
        return input.toString();

    }

//    private String generateDetailedInputforGTest() throws Exception {
//        String input;
//
//        String type = VariableTypeUtils
//                .deleteStorageClasses(this.getRawType().replace(IDataNode.REFERENCE_OPERATOR, ""));
//        type = type.substring(0, type.indexOf("["));
//        if (isExternel()) {
//            type = "";
//        }
//        String declaration;
//        if (this.getSize() > 0) {
//            /*
//              Máº·c Ä‘á»‹nh táº¥t cáº£ má»�i pháº§n tá»­ trong máº£ng Ä‘á»�u lÃ  kÃ­ tá»±
//              tráº¯ng. Ta BUá»˜C pháº£i lÃ m Ä‘iá»�u nÃ y Ä‘á»ƒ cÃ¡c pháº§n tá»­ trong
//              máº£ng liÃªn tá»¥c
//             */
//            StringBuilder space = new StringBuilder();
//            for (int i = 0; i < this.getSize(); i++)
//                space.append(" ");
//            /*
//
//             */
//            declaration = String.format("%s %s[%s]=\"%s\"", type, this.getVituralName(), this.getSize() + 1, space.toString());
//        } else
//            declaration = String.format("%s %s[%s]", type, this.getVituralName(), this.getSize() + 1);
//        StringBuilder initialization = new StringBuilder();
//
//        for (IDataNode child : this.getChildren())
//            initialization.append(child.getInputForGoogleTest());
//        input = declaration + SpecialCharacter.END_OF_STATEMENT + initialization;
//
//        if (this.isAttribute())
//            input += this.getSetterInStr(this.getVituralName()) + SpecialCharacter.END_OF_STATEMENT;
//        return input;
//
//    }

    private String generateSimplifyInputforDisplay() {
        StringBuilder input = new StringBuilder();

        Map<Integer, String> values = new TreeMap<>();
        for (IDataNode child : this.getChildren()) {
            NormalDataNode nChild = (NormalDataNode) child;

            String index = Utils.getIndexOfArray(nChild.getName()).get(0);
            values.put(Utils.toInt(index), nChild.getValue());
        }

        for (Integer key : values.keySet()) {
            int ASCII = Utils.toInt(values.get(key));

            switch (ASCII) {
                case 34:/* nhay kep */
                    input.append("\\\"");
                    break;

                case 92:/* gach cheo */
                    input.append("\\\\");
                    break;

                case 39:
                /* nhay don */
                    input.append("\\'");
                    break;

                default:
                    input.append((char) ASCII);
            }
        }
        input = new StringBuilder(this.getDotSetterInStr("\"" + input + "\"") + SpecialCharacter.LINE_BREAK);
        return input.toString();

    }

    private String generateSimplifyInputforGTest() {
        String input = "";

        String type = VariableTypeUtils
                .deleteStorageClasses(this.getRawType().replace(IDataNode.REFERENCE_OPERATOR, ""));
        type = type.substring(0, type.indexOf("["));
        if (isExternel()) {
            type = "";
        }

        StringBuilder initialization = new StringBuilder();

        Map<Integer, String> values = new TreeMap<>();
        for (IDataNode child : this.getChildren()) {
            NormalDataNode nChild = (NormalDataNode) child;

            String index = Utils.getIndexOfArray(nChild.getName()).get(0);
            values.put(Utils.toInt(index), nChild.getValue());
        }

        for (Integer key : values.keySet()) {
            int ASCII = Utils.toInt(values.get(key));
            switch (ASCII) {
                case 34:/* nhay kep */
                    initialization.append("\\\"");
                    break;
                case 92:/* gach cheo */
                    initialization.append("\\\\");
                    break;
                case 39:
				/* nhay don */
                    initialization.append("\\'");
                    break;
                default:
                    initialization.append((char) ASCII);
            }
        }

        if (this.isAttribute())
            input = this.getSetterInStr(Utils.putInString(initialization.toString())) + SpecialCharacter.END_OF_STATEMENT;
        else if (this.isPassingVariable())
            input = type + " " + this.getVituralName() + "[]=" + Utils.putInString(initialization.toString())
                    + SpecialCharacter.END_OF_STATEMENT;
        return input;

    }

    @Override
    public String getInputForDisplay() throws Exception {
        String input;

        if (this.canConvertToString() && this.isVisible())
            input = this.generateSimplifyInputforDisplay();
        else
            input = this.generateDetailedInputforDisplay();
        return input;
    }

    @Override
    public String getInputForGoogleTest(boolean isDeclared) throws Exception {
        String declaration="";
        if(!isPassingVariable() || (isPassingVariable() && isDeclared) ){
            String type = VariableTypeUtils
                    .deleteStorageClassesExceptConst(this.getRawType().replace(IDataNode.REFERENCE_OPERATOR, ""));
            String coreType = type.replaceAll("\\[.*\\]", "");
            if (isExternel()) {
                coreType = "";
            }
            List<String> indexes = Utils.getIndexOfArray(type);

            if (indexes.size() > 0) {
                String dimension = "";
                for (String index : indexes)
                    if (index.length() == 0)
                        dimension += Utils.asIndex(this.getSize());
                    else
                        dimension += Utils.asIndex(index);

                if (this.getParent() instanceof StructureDataNode)
                    declaration = "";
                else if (isSutExpectedArgument() || isGlobalExpectedValue()) {
//                    String firstIndex = indexes.get(0);
                    if (getSize() > 0) {
                        declaration = String.format("%s %s%s" + SpecialCharacter.END_OF_STATEMENT, coreType,
                                this.getVituralName(), dimension);
                    } else
                        declaration += String.format("%s *%s = NULL" + SpecialCharacter.END_OF_STATEMENT, coreType,
                                this.getVituralName());
                } else if (isVoidPointerValue()) {
                    declaration = String.format("%s %s%s" + SpecialCharacter.END_OF_STATEMENT, coreType,
                            this.getVituralName(), dimension);
                } else if (getSize() > 0)
                    declaration = String.format("%s %s%s" + SpecialCharacter.END_OF_STATEMENT, coreType,
                            this.getVituralName(), dimension);
                else {
                    declaration += String.format("%s *%s = NULL" + SpecialCharacter.END_OF_STATEMENT, coreType,
                            this.getVituralName());
                }
            } else if (this.getParent() instanceof StructureDataNode) {
                declaration = "";

            } else if (getSize() > 0) {
                declaration = String.format("%s %s[%s]" + SpecialCharacter.END_OF_STATEMENT, coreType,
                        this.getVituralName(), this.getSize());
            } else {
                declaration += String.format("%s *%s = NULL" + SpecialCharacter.END_OF_STATEMENT, coreType,
                        this.getVituralName());
            }
        }

        return declaration + SpecialCharacter.END_OF_STATEMENT + super.getInputForGoogleTest(isDeclared);
    }

    private boolean isVisible() {
        for (IDataNode child : this.getChildren()) {

            int ASCII = Utils.toInt(((NormalDataNode) child).getValue());

            if (!Utils.isVisibleCh(ASCII))
                return false;
        }
        return true;
    }

    @Override
    public String generareSourcecodetoReadInputFromFile() throws Exception {
        if (getParent() instanceof RootDataNode) {
            String typeVar = VariableTypeUtils.deleteStorageClasses(this.getRawType())
                    .replace(IDataNode.REFERENCE_OPERATOR, "")
                    .replace(IDataNode.ONE_LEVEL_POINTER_OPERATOR, "");
            typeVar = typeVar.substring(0, typeVar.indexOf("["));

            String loadValueStm = "data.findOneDimensionOrLevelBasicByName<" + typeVar + ">(\"" + getVituralName()
                    + "\", DEFAULT_VALUE_FOR_CHARACTER)";

            String fullStm = typeVar + "* " + this.getVituralName() + "=" + loadValueStm
                    + SpecialCharacter.END_OF_STATEMENT;
            return fullStm;
        } else {
            // belong to structure node
            // Handle later;
            return "";
        }
    }
}
