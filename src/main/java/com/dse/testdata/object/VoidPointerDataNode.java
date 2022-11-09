package com.dse.testdata.object;

import com.dse.environment.Environment;
import com.dse.testdata.comparable.*;
import com.dse.user_code.objects.AbstractUserCode;
import com.dse.util.SpecialCharacter;
import com.dse.util.VariableTypeUtils;
import org.apache.commons.math3.random.RandomDataGenerator;

/**
 * for "void*"
 */
public class VoidPointerDataNode extends UnresolvedDataNode implements INullableComparable {

//    private AbstractUserCode userCode = null;
    private String referenceType = null;
    private InputMethod inputMethod;

    @Override
    public boolean haveValue() {
        if (inputMethod == InputMethod.USER_CODE) {
            return userCode != null;
        }

        return super.haveValue();
    }

//    @Override
//    public String getAssertionForGoogleTest(String method, String source, String target) throws Exception {
//        StringBuilder assertStm = new StringBuilder();
//
//        String actualOutputName = getVituralName().replace(source, target);
//
//        for (IDataNode child : getChildren()) {
//            if (child instanceof ValueDataNode) {
//                ValueDataNode childVal = (ValueDataNode) child;
//                String childRawType =childVal.getRawType();
//                String actualOutputChildName = childVal.getVituralName().replace(source, target);
//                String castStm = String.format("%s %s = (%s) %s;", childRawType, actualOutputChildName, childRawType, actualOutputName);
//                assertStm.append(castStm);
//                String assertChildStm = childVal.getAssertionForGoogleTest(method, source, target);
//                assertStm.append(assertChildStm);
//            }
//        }
//
//        return assertStm.toString();
//    }

    private String getInputFromUserCode() {
        if (userCode == null) return "";

        String userCodeContent = userCode.getContent();
        if (userCodeContent == null || userCodeContent.trim().length() == 0)
            return "";

        if (userCodeContent.trim().endsWith(DEFAULT_USER_CODE)) {
            return "/*No code*/";
        } else {
            if (userCodeContent.contains("=") && userCodeContent.indexOf("=") == userCodeContent.lastIndexOf("=")) {

                if (Environment.getInstance().getCompiler().isGPlusPlusCommand()){
                    // Case 1: parameter "void* data = 2222;"---> "auto xxx = 222; void* data = &xxx";
                    // Case 2: parameter "s.data = 2222;" (s.data is void*)---> "auto xxx = 222; s.data = &xxx";
                    String initialization = userCodeContent.substring(userCodeContent.indexOf("=") + 1).trim();

                    String newVar = "voidPointerTmp" + new RandomDataGenerator().nextInt(0, 999999);
                    String stm = "auto " + newVar + "=" + initialization;
                    if (!stm.endsWith(";"))
                        stm += ";";

                    String normalize = "";
                    if (this.isPassingVariable())
                        normalize = String.format("%s \n %s %s = &%s;", stm, getRawType(), getVituralName(), newVar);
                    else
                        normalize = String.format("%s \n %s = &%s;", stm, getVituralName(), newVar);
                    return normalize;

                } else if (Environment.getInstance().getCompiler().isGccCommand()){
                    // Case 1: parameter "void* data = 2222;"---> "void* xxx = 222; void* data = xxx";
                    // Case 2: parameter "s.data = 2222;" (s.data is void*)---> "void* xxx = 222; s.data = xxx";

                    String initialization = userCodeContent.substring(userCodeContent.indexOf("=") + 1).trim();

                    String newVar = "voidPointerTmp" + new RandomDataGenerator().nextInt(0, 999999);
                    String stm = "void* " + newVar + "=" + initialization;
                    if (!stm.endsWith(";"))
                        stm += ";";

                    String normalize = "";
                    if (this.isPassingVariable()) // case 1
                        normalize = String.format("%s \n %s %s = %s;", stm, getRawType(), getVituralName(), newVar);
                    else // case 2
                        normalize = String.format("%s \n %s = %s;", stm, getVituralName(), newVar);
                    return normalize;
                }
            }
            return "/* Do not know how to create initialization of void pointer */";
        }
    }

    @Override
    public String getInputForGoogleTest(boolean isDeclared) throws Exception {
        String input = SpecialCharacter.EMPTY;

        if (isUseUserCode()) {
            if (isPassingVariable() && !isDeclared)
                return SpecialCharacter.EMPTY;
            else
                return getUserCodeContent();
        }

        if(!isPassingVariable() || (isPassingVariable() && isDeclared) ){
            // get type of variable
            String typeVar = VariableTypeUtils.deleteStorageClassesExceptConst(this.getRawType())
                    .replace(IDataNode.REFERENCE_OPERATOR, "");
            typeVar = VariableTypeUtils.deleteStorageClassesExceptConst(typeVar);

            if (isExternel())
                typeVar = "";

            if (!getChildren().isEmpty() && getChildren().get(0) instanceof ValueDataNode) {
                ValueDataNode refDataNode = (ValueDataNode) getChildren().get(0);

                if (this.isPassingVariable()) {
                    input = typeVar + " " + this.getVituralName() + SpecialCharacter.END_OF_STATEMENT;

                } else if (this.isAttribute() || this.isArrayElement()) {

                } else if (isSTLListBaseElement()) {
                    input = typeVar + " " + this.getVituralName() + SpecialCharacter.END_OF_STATEMENT;

                } else if (this.isInConstructor()){
                    input = typeVar + " " + this.getVituralName() + SpecialCharacter.END_OF_STATEMENT;

                } else {
                    input = typeVar + " " + this.getVituralName() + SpecialCharacter.END_OF_STATEMENT;
                }

                String valueInit = refDataNode.getInputForGoogleTest(false);
                input += valueInit;
                input += String.format("%s = %s;", getVituralName(), refDataNode.getVituralName());

            } else if (isPassingVariable() || isInConstructor()) {
                input = typeVar + " " + getVituralName() + SpecialCharacter.END_OF_STATEMENT;
            }
        }

        return input;
    }

    @Override
    public void setUserCode(AbstractUserCode userCode) {
        this.userCode = userCode;
    }

    @Override
    public String getAssertion() {
        String actualName = getActualName();

        String output = SpecialCharacter.EMPTY;

        String assertMethod = getAssertMethod();
        if (assertMethod != null) {
            switch (assertMethod) {
                case AssertMethod.ASSERT_NULL:
                    output = assertNull(actualName);
                    break;

                case AssertMethod.ASSERT_NOT_NULL:
                    output = assertNotNull(actualName);
                    break;

                case AssertMethod.USER_CODE:
                    output = getAssertUserCode().normalize();
                    break;
            }
        }

        return output + super.getAssertion();
    }

    @Override
    public String assertNull(String name) {
        return new NullableStatementGenerator(this).assertNull(name);
    }

    @Override
    public String assertNotNull(String name) {
        return new NullableStatementGenerator(this).assertNotNull(name);
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String realType) {
        this.referenceType = realType;
    }

    public InputMethod getInputMethod() {
        return inputMethod;
    }

    public void setInputMethod(InputMethod inputMethod) {
        this.inputMethod = inputMethod;
    }

    public enum InputMethod {
        AVAILABLE_TYPES, USER_CODE
    };
}
