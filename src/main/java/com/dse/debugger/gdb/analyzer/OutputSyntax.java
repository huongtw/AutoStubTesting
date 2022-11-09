package com.dse.debugger.gdb.analyzer;

public enum OutputSyntax {

    END_LOG("(gdb) "),

    ASYNC_CLASS("stopped"),

    // result class
    DONE_RESULT("^done"),
    RUNNING_RESULT("running"),
    CONNECTED_RESULT("connected"),
    ERROR_RESULT("^error"),
    EXIT_RESULT("exit"),

    EXIT_ERROR("exited"),
    BREAKPOINT_HIT("breakpoint-hit"),
    EXIT_HIT("exited-normally");

    private String syntax;

    OutputSyntax(String syntax) {
        this.syntax = syntax;
    }

    public String getSyntax() {
        return this.syntax;
    }
}
