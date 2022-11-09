package com.dse.parser.object;

import com.dse.testdata.comparable.*;
import com.dse.testdata.object.ValueDataNode;
import com.dse.util.SpecialCharacter;

import java.util.ArrayList;
import java.util.List;

public class NumberOfCallNode extends ValueDataNode {

    private String value;

    public NumberOfCallNode() {}

    public NumberOfCallNode(String name) {
        super.setName(name);
        super.setRealType("int");
        super.setRawType("int");
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean haveValue() {
        return value != null && !value.isEmpty();
    }

    public void setValue(int value) {
        this.value = value + "";
    }

    @Override
    public String[] getAllSupportedAssertMethod() {
        List<String> supportedMethod = new ArrayList<>();
        supportedMethod.add(SpecialCharacter.EMPTY);
        supportedMethod.add(AssertMethod.ASSERT_EQUAL);
        supportedMethod.add(AssertMethod.USER_CODE);
        return supportedMethod.toArray(new String[0]);
    }
}
