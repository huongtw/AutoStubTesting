package com.dse.debugger.gdb.analyzer;

public enum GDBToken {
    CONSOLE_STREAM_OUTPUT("~"),
    TARGET_STREAM_OUTPUT("@"),
    LOG_STREAM_OUTPUT("&"),
    STATUS_ASYNC_OUTPUT("+"),
    EXEC_ASYNC_OUTPUT("*"),
    NOTIFY_ASYNC_OUTPUT("="),
    RESULT_RECORD("^");

    private String token;

    GDBToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return this.token;
    }

    ;
}
