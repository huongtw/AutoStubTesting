package com.dse.testdata.object;

import com.dse.user_code.objects.AbstractUserCode;
import com.dse.util.SpecialCharacter;

/**
 * Other unsupported data types
 */
public class OtherUnresolvedDataNode extends UnresolvedDataNode {

    @Override
    public boolean haveValue() {
        return userCode != null;
    }

    @Override
    public String getInputForGoogleTest(boolean isDeclared) {
        String input = SpecialCharacter.EMPTY;

        if (isUseUserCode()) {
            if (isPassingVariable() && !isDeclared)
                return SpecialCharacter.EMPTY;
            else
                return getUserCodeContent();
        }

        if(!isPassingVariable() || (isPassingVariable() && isDeclared) ){
            if (!isExternel()) {
                String typeVar = getRawType();

                if (this.isPassingVariable()) {
                    input = typeVar + " " + this.getVituralName() + SpecialCharacter.END_OF_STATEMENT;

                } else if (this.isAttribute()) {
                    input = SpecialCharacter.EMPTY;

                } else if (this.isArrayElement()) {
                    input = SpecialCharacter.EMPTY;

                } else if (isSTLListBaseElement()) {
                    input = typeVar + " " + this.getVituralName() + SpecialCharacter.END_OF_STATEMENT;

                } else if (this.isInConstructor()){
                    input = typeVar + " " + this.getVituralName() + SpecialCharacter.END_OF_STATEMENT;

                } else {
                    input = typeVar + " " + this.getVituralName() + SpecialCharacter.END_OF_STATEMENT;
                }
            }
        }

        return input;

//        if (userCode == null) return "";
//        String userCodeContent = userCode.getContent();
//
//        if (userCodeContent == null || userCodeContent.trim().length() == 0)
//            if (isPassingVariable())
//                return getRawType() + " " + getVituralName() + SpecialCharacter.END_OF_STATEMENT;
//            else
//                return "";
//        else if (userCodeContent.trim().endsWith(DEFAULT_USER_CODE)) {
//            return "/*No code*/";
//        } else {
//            String normalize = userCodeContent.replace(VALUE_TAG, getVituralName());
//
//            if (normalize.trim().matches("sizeof\\(.+\\)=.+")) {
//                return String.format("/* Sizeof usage error: %s */", normalize);
//            }
//
//            return normalize;
//        }
    }

    @Override
    public void setUserCode(AbstractUserCode userCode) {
        this.userCode = userCode;
    }
}
