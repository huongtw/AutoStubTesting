package com.dse.user_code.objects;

public class UsedParameterUserCode extends ParameterUserCode {

    private String type;

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    /**
     *  TYPE_REFERENCE is type of user code that is manage by the tool, saved in
     *  user codes directory
     *  TYPE_CODE is type of user code that has content saved in test case data
     */
    public final static String TYPE_REFERENCE = "@REFERENCE";
    public final static String TYPE_CODE = "@CODE";
}
