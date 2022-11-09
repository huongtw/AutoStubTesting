package com.dse.testcase_execution;

public interface IDriverGenMessage {
    String getCompileMessage();
    String getLinkMessage();
    boolean isCompileSuccess();
    boolean isLinkSuccess();
    boolean isEmpty();
}
