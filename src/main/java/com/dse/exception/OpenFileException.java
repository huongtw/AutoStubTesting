package com.dse.exception;

import java.io.IOException;

/**
 * Exception when opening a file fails
 */
public class OpenFileException extends IOException {
    public OpenFileException(String exception) {
        super(exception);
    }
}
