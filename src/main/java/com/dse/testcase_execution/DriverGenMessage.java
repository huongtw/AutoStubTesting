package com.dse.testcase_execution;

public class DriverGenMessage implements IDriverGenMessage {

    private String compileMessage;
    private String linkMessage;

    @Override
    public boolean isEmpty() {
        return compileMessage.isEmpty() && linkMessage.isEmpty();
    }

    @Override
    public String getCompileMessage() {
        return compileMessage;
    }

    @Override
    public String getLinkMessage() {
        return linkMessage;
    }

    @Override
    public boolean isCompileSuccess() {
        return !compileMessage.contains(" error:");
    }

    @Override
    public boolean isLinkSuccess() {
        return linkMessage.isEmpty();
    }

    public void setCompileMessage(String compileMessage) {
        this.compileMessage = compileMessage;
    }

    public void setLinkMessage(String linkMessage) {
        this.linkMessage = linkMessage;
    }

    @Override
    public String toString() {
        return "DriverGenMessage{" +
                "compileMessage='" + compileMessage + '\'' +
                ", linkMessage='" + linkMessage + '\'' +
                '}';
    }
}
