package com.dse.thread.task;

import com.dse.compiler.message.ICompileMessage;
import com.dse.parser.object.INode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class BuildEnvironmentResult {

    private int exitCode;

    private AtomicInteger fileIndex;

    private ICompileMessage compileMessage, linkMessage;

    private List<INode> sourceCodes;

    private Map<String, String> compilationCommands = new HashMap<>();

    public int getExitCode() {
        return exitCode;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public AtomicInteger getFileIndex() {
        return fileIndex;
    }

    public void setFileIndex(AtomicInteger fileIndex) {
        this.fileIndex = fileIndex;
    }

    public ICompileMessage getCompileMessage() {
        return compileMessage;
    }

    public void setCompileMessage(ICompileMessage compileMessage) {
        this.compileMessage = compileMessage;
    }

    public ICompileMessage getLinkMessage() {
        return linkMessage;
    }

    public void setLinkMessage(ICompileMessage linkMessage) {
        this.linkMessage = linkMessage;
    }

    public List<INode> getSourceCodes() {
        return sourceCodes;
    }

    public void setSourceCodes(List<INode> sourceCodes) {
        this.sourceCodes = sourceCodes;
    }

    public Map<String, String> getCompilationCommands() {
        return compilationCommands;
    }

    public void setCompilationCommands(Map<String, String> compilationCommands) {
        this.compilationCommands = compilationCommands;
    }
}
