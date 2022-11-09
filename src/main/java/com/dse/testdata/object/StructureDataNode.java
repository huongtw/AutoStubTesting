package com.dse.testdata.object;

import com.dse.util.VariableTypeUtils;
import com.dse.util.SpecialCharacter;

/**
 * Represent structure variable such as class, struct, etc.
 *
 * @author DucAnh
 */
public abstract class StructureDataNode extends ValueDataNode {

    @Override
    public String generareSourcecodetoReadInputFromFile() throws Exception {
        // Ex: Date d = data.findStructureDateByName("born");
        String typeVar = VariableTypeUtils.deleteStorageClassesExceptConst(this.getRawType())
                .replace(IDataNode.REFERENCE_OPERATOR, "");
        // Ex: A::B, ::B. We need to get B
        if (typeVar.contains(SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS))
            typeVar = typeVar.substring(typeVar.lastIndexOf(SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS) + 2);

        String loadValueStm = "data.findStructure" + typeVar + "ByName" + "(\"" + getVituralName() + "\")";

        String fullStm = typeVar + " " + this.getVituralName() + "=" + loadValueStm + SpecialCharacter.END_OF_STATEMENT;
        return fullStm;
    }

    @Override
    public String generateInputToSavedInFile() throws Exception {
        StringBuilder output = new StringBuilder();
        for (IDataNode child : getChildren())
            output.append(child.generateInputToSavedInFile()).append(SpecialCharacter.LINE_BREAK);
        return output.toString();
    }
//
//    public abstract boolean getValue();
}
