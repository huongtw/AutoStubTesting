package com.dse.debugger.gdb.analyzer;

public class OutputGDB {
    private boolean isError;
    private String json;

    public OutputGDB(boolean isError, String json) {
        this.isError = isError;
        this.json = json;
    }

    public boolean isError() {
        return isError;
    }

    public void setError(boolean error) {
        this.isError = error;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    @Override
    public String toString() {
        return "OutputGDB{" +
                "isError=" + isError +
                ", json='" + json + '\'' + '}';
    }
}
