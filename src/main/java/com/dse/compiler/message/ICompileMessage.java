package com.dse.compiler.message;

public interface ICompileMessage {
    enum MessageType {
        ERROR,
        WARNING,
        UNDEFINED,
        EMPTY
    }

    String getMessage();

    String getFilePath();

    MessageType getType();

    String getCompilationCommand();

    void setCompilationCommand(String compilationCommand);

    String getLinkingCommand();

    void setLinkingCommand(String linkingCommand);
}
