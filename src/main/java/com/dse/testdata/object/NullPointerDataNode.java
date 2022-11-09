package com.dse.testdata.object;

import com.dse.util.SpecialCharacter;

public class NullPointerDataNode extends ValueDataNode {
    public static String NULL_PTR = "nullptr";

    @Override
    public String getInputForGoogleTest(boolean isDeclared) throws Exception {
        return SpecialCharacter.EMPTY;
    }
}
