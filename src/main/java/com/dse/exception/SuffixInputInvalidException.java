package com.dse.exception;

public class SuffixInputInvalidException extends NumberFormatException {

    public SuffixInputInvalidException() {
        super("input value has an invalid suffix");
    }

    public SuffixInputInvalidException(String suffix) {
        super("input value has an invalid suffix " + "\"" + suffix + "\"");
    }
}
