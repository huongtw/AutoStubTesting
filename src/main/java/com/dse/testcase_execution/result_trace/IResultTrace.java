package com.dse.testcase_execution.result_trace;

public interface IResultTrace {
    String getExpected();
    String getActual();
    String getMessage();
    String getExpectedName();
    String getActualName();
    String getUserCode();
}
