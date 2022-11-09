package com.dse.debugger.gdb.analyzer;

public enum GDBStatus{
    ERROR,CONTINUABLE,EXIT;

    private String reason = null;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}