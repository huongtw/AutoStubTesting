package com.dse.testdata.object;

import com.dse.environment.Environment;
import com.dse.parser.object.EnumNode;
import com.dse.parser.object.EnumTypedefNode;
import com.dse.parser.object.INode;
import com.dse.testdata.comparable.AssertMethod;
import com.dse.testdata.comparable.IEqualityComparable;
import com.dse.testdata.comparable.ValueStatementGenerator;
import com.dse.util.SourceConstant;
import com.dse.util.SpecialCharacter;
import com.dse.util.VariableTypeUtils;

import java.util.List;

/**
 * Created by DucToan on 27/07/2017
 */
public class EnumDataNode extends StructureDataNode implements IEqualityComparable {
    /**
     * Represent value of variable
     */
    private String value;
    private boolean valueIsSet = false;

    @Override
    public String getInputForDisplay() {
        return this.getRawType() + " " + this.getName() + " = " + this.getValue() + SpecialCharacter.END_OF_STATEMENT;
    }

    @Override
    public String getInputForGoogleTest(boolean isDeclared) throws Exception {
        String input= "";

        if (isUseUserCode()) {
            if (isPassingVariable() && !isDeclared)
                return SpecialCharacter.EMPTY;
            else
                return super.getInputForGoogleTest(isDeclared);
        }

        if(!isPassingVariable() || (isPassingVariable() && isDeclared) ){
            input = super.getInputForGoogleTest(isDeclared);

            // get type of variable
            String typeVar = this.getRawType().replace(IDataNode.REFERENCE_OPERATOR, "");
            typeVar = VariableTypeUtils.deleteStorageClassesExceptConst(typeVar);

            if (Environment.getInstance().isC()) {
                typeVar = typeVar.replace(SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS, SpecialCharacter.EMPTY);

                if (getCorrespondingType() instanceof EnumNode && !(getCorrespondingType() instanceof EnumTypedefNode)) {
                    if (!typeVar.startsWith("enum "))
                        typeVar = "enum " + typeVar;
                }

//            INode correspondingType = getCorrespondingType();
//            if (correspondingType instanceof StructureNode && !((StructureNode) correspondingType).haveTypedef()) {
//                if (!typeVar.startsWith("enum"))
//                    typeVar = "enum " + typeVar;
//            }
            }

            if (this.getValue() != null) {
                String valueVar = getValue();

                if (!getAllNameEnumItems().contains(valueVar)) {
                    if (!Environment.getInstance().isC())
                        valueVar = typeVar + "(" + valueVar + ")";
                    } else if (!Environment.getInstance().isC()) {
                        valueVar = typeVar + SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS + valueVar;
                    }

                    if (isExternel())
                        typeVar = "";

                    // generate the statement
                    if (this.isPassingVariable()) {
                        input += typeVar + " " + this.getVituralName() + "=" + valueVar + SpecialCharacter.END_OF_STATEMENT;

                    } else if (this.isAttribute()) {
                        input += this.getVituralName() + "=" + valueVar + SpecialCharacter.END_OF_STATEMENT;

                    } else if (this.isArrayElement()) {
                        input += this.getVituralName() + "=" + valueVar + SpecialCharacter.END_OF_STATEMENT;

                    } else if (isSTLListBaseElement()) {
                        input += typeVar + " " + this.getVituralName() + "=" + valueVar + SpecialCharacter.END_OF_STATEMENT;

                    } else if (this.isInConstructor()){
                        input += typeVar + " " + this.getVituralName() + "=" + valueVar + SpecialCharacter.END_OF_STATEMENT;

                    } else {
                        input += typeVar + " " + this.getVituralName() + "=" + valueVar + SpecialCharacter.END_OF_STATEMENT;
                    }

            } else if (isPassingVariable() || isInConstructor()) {
                input += typeVar + " " + getVituralName() + SpecialCharacter.END_OF_STATEMENT;
            }
        }

        return input + SpecialCharacter.LINE_BREAK;
    }

    /**
     * Get all name defined in the declaration of enum. For example, enum "Color {
     * RED=10, GREEN=40, BLUE}" -----> "RED", "GREEN", "BLUE"
     *
     * @return all enum items name
     */
    public List<String> getAllNameEnumItems() {
        INode coreType = getCorrespondingVar().resolveCoreType();
        if (coreType instanceof EnumNode) {
            return ((EnumNode) coreType).getAllNameEnumItems();
        }
        return null;
    }

    @Override
    public String generateInputToSavedInFile() {
        return getName() + "=" + getValue();
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
//        if (getAllNameEnumItems().contains(value)) {
            this.value = value;
//        } else {
//            try {
//                Integer.parseInt(value);
//                this.value = value;
//            } catch (NumberFormatException e) {
//                e.printStackTrace();
//            }
//        }
    }

    public void setValueIsSet(boolean valueIsSet) {
        this.valueIsSet = valueIsSet;
    }

    @Override
    public boolean haveValue() {
        return valueIsSet;
    }

    public boolean isSetValue() {
        return valueIsSet;
    }

    @Override
    public String getAssertion() {
        String expectedName = getVituralName();
        String actualName = getActualName();

        String assertMethod = getAssertMethod();

        if (assertMethod != null) {
            switch (assertMethod) {
                case AssertMethod.ASSERT_EQUAL:
                    return assertEqual(expectedName, actualName);
                case AssertMethod.ASSERT_NOT_EQUAL:
                    return assertNotEqual(expectedName, actualName);
                case AssertMethod.USER_CODE:
                    return getAssertUserCode().normalize();
            }
        }

        return SpecialCharacter.EMPTY;
    }

    @Override
    public String assertEqual(String expected, String actual) {
        return new ValueStatementGenerator(this).assertEqual(expected, actual);
    }

    @Override
    public String assertNotEqual(String expected, String actual) {
        return new ValueStatementGenerator(this).assertNotEqual(expected, actual);
    }
}
