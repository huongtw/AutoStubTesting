package com.dse.testdata.object;

import com.dse.util.SpecialCharacter;
import com.dse.util.VariableTypeUtils;
import org.apache.commons.math3.random.RandomDataGenerator;

import java.util.HashMap;
import java.util.Map;

public class NormalStringDataNode extends NormalDataNode {
    private long allocatedSize;

    @Deprecated
    @Override
    public String getValue() {
        // Need to get value of a string via its children instead
        return super.getValue();
    }

    @Override
    public void setValue(String value) {
        super.setValue(value);
    }

    @Override
    public String getInputForGoogleTest(boolean isDeclared) throws Exception {
        // String input = super.getInputForGoogleTest() + SpecialCharacter.LINE_BREAK;
        String input = "";
        String value = "\"\"";
        if (!isPassingVariable() || (isPassingVariable() && isDeclared)) {
            // Get type of variable
            String typeVar = VariableTypeUtils.deleteStorageClassesExceptConst(this.getRawType())
                    .replace(IDataNode.REFERENCE_OPERATOR, "");
            typeVar = VariableTypeUtils.deleteStorageClassesExceptConst(typeVar);
            typeVar = VariableTypeUtils.deleteStructKeyword(typeVar);
            typeVar = VariableTypeUtils.deleteUnionKeyword(typeVar);

            if (this.getChildren().size() > 0) {
                if (isExternel())
                    typeVar = "";

                // Create a temporary initialization
                long len = getChildren().size();
                String randomName = "akaRandomName" + new RandomDataGenerator().nextInt(0, 999999);
                String elementType = getStringToCharacterTypeMap()
                        .get(VariableTypeUtils.removeRedundantKeyword(typeVar));
                if (elementType == null || elementType.length() == 0)
                    return "";

                String init = String.format("%s* %s = new %s[%s];", elementType, randomName, elementType, len + 1);
                for (int i = 0; i < len; i++) {
                    String v = ((NormalCharacterDataNode) this.getChildren().get(i)).getValue();
                    String ascii = NormalCharacterDataNode.toASCII(v);
                    if (ascii != null) {
                        init += String.format("%s[%s] = %s;", randomName, i, ascii, "");
                    }
                    // if (v.startsWith(NormalCharacterDataNode.OCTAL_NUMBER_PREFIX))
                    // init += String.format("%s[%s] = %s;", randomName, i,
                    // v.replace(NormalCharacterDataNode.OCTAL_NUMBER_PREFIX, ""));
                    // else
                    // init += String.format("%s[%s] = '%s';", randomName, i, v, "");
                }
                init += String.format("%s[%s] = '\\0';", randomName, len);
                input += init;
                value = randomName;
            } else if (this.getValue() != null) {
                value = this.getValue();
            }

            // Generate the statement
            if (isPassingVariable() || isInConstructor()) {
                input += typeVar + SpecialCharacter.SPACE + getVituralName() + SpecialCharacter.EQUAL + value
                        + SpecialCharacter.END_OF_STATEMENT;
            } else if (isAttribute()) {
                input += getVituralName() + SpecialCharacter.EQUAL + value + SpecialCharacter.END_OF_STATEMENT;
            } else if (isArrayElement()) {
                input += getVituralName() + SpecialCharacter.EQUAL + value + SpecialCharacter.END_OF_STATEMENT;
            } else if (isSTLListBaseElement()) {
                input += typeVar + SpecialCharacter.SPACE + getVituralName() + SpecialCharacter.EQUAL + value
                        + SpecialCharacter.END_OF_STATEMENT;
            } else if (isInConstructor()) {
                input += typeVar + SpecialCharacter.SPACE + getVituralName() + SpecialCharacter.EQUAL + value
                        + SpecialCharacter.END_OF_STATEMENT;
            } else {
                input += typeVar + SpecialCharacter.SPACE + getVituralName() + SpecialCharacter.EQUAL + value
                        + SpecialCharacter.END_OF_STATEMENT;
            }
        }
        return input + SpecialCharacter.LINE_BREAK;
    }

    public static Map<String, String> getStringToCharacterTypeMap() {
        Map<String, String> stringToCharacterType = new HashMap<>();

        stringToCharacterType.put("std::u32string", "char32_t");
        stringToCharacterType.put("u32string", "char32_t");

        stringToCharacterType.put("std::wstring", "wchar_t");
        stringToCharacterType.put("wstring", "wchar_t");

        stringToCharacterType.put("std::u16string", "char16_t");
        stringToCharacterType.put("u16string", "char16_t");

        stringToCharacterType.put("std::string", "char");
        stringToCharacterType.put("string", "char");

        return stringToCharacterType;
    }

    public long getAllocatedSize() {
        return allocatedSize;
    }

    public void setAllocatedSize(long allocatedSize) {
        this.allocatedSize = allocatedSize;
    }
}
