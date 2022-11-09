package com.dse.user_code.objects;

import com.dse.testdata.comparable.AssertUserCodeMapping;
import com.dse.util.SpecialCharacter;

public class AssertUserCode extends UsedParameterUserCode {

    public String normalize() {
        try {
            return AssertUserCodeMapping.convert(getContent());
        } catch (Exception ex) {
            return getContent();
        }
    }
}
