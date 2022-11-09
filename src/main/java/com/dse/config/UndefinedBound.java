package com.dse.config;

public class UndefinedBound implements IFunctionConfigBound {
    public static final String UNDEFINED = "N/A";

    public UndefinedBound() {
    }

    public String show() {
        return UNDEFINED;
    }
}
