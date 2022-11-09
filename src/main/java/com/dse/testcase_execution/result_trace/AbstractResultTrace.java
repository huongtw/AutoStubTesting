package com.dse.testcase_execution.result_trace;

public abstract class AbstractResultTrace implements IResultTrace {

    protected String[] getMessageLines(String message) {
        if (message == null)
            return null;

        String[] lines = message.split("\\R");

        for (int i = 0; i < lines.length; i++)
            lines[i] = lines[i].trim();

        return lines;
    }

    @Override
    public String getUserCode() {
        return null;
    }

    protected static final String PROBLEM_VALUE = "Can't find variable value";
    protected static final String FCALL_TAG = "Aka function calls: ";
}
