package com.dse.exception;

public class FunctionNodeNotFoundException extends Exception {

    private final String functionPath;

    public FunctionNodeNotFoundException(String functionPath) {
        super("Function " + functionPath + " not found");
        this.functionPath = functionPath;
    }

    public String getFunctionPath() {
        return functionPath;
    }
}
