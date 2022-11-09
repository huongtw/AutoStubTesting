package com.dse.probe_point_manager.objects;

import com.dse.parser.object.IFunctionNode;
import com.dse.parser.object.ISourcecodeFileNode;
import com.dse.testcase_manager.TestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ProbePoint implements Comparable<ProbePoint> {
    private String name;
    private String before;
    private String content;
    private String after;
    private IFunctionNode functionNode;
    private int lineInFunction;
    private List<TestCase> testCases = new ArrayList<>();
    private ISourcecodeFileNode sourcecodeFileNode;
    private int lineInSourceCodeFile;
    private String path;
    private boolean isValid = true;

    public static ProbePoint getNewRandomNameProbePoint() {
        ProbePoint probePoint = new ProbePoint();
        probePoint.setName("PROBE_POINT." + new Random().nextInt(100000));
        return probePoint;
    }

    public String getName() {
        return name;
    }

    public ISourcecodeFileNode getSourcecodeFileNode() {
        return sourcecodeFileNode;
    }

    public int getLineInSourceCodeFile() {
        return lineInSourceCodeFile;
    }

    public void setSourcecodeFileNode(ISourcecodeFileNode sourcecodeFileNode) {
        this.sourcecodeFileNode = sourcecodeFileNode;
    }

    public void setLineInSourceCodeFile(int lineInSourceCodeFile) {
        this.lineInSourceCodeFile = lineInSourceCodeFile;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBefore() {
        return before;
    }

    public void setBefore(String before) {
        this.before = before;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public String getAfter() {
        return after;
    }

    public void setAfter(String after) {
        this.after = after;
    }

    public List<TestCase> getTestCases() {
        return testCases;
    }

    public void setFunctionNode(IFunctionNode functionNode) {
        this.functionNode = functionNode;
    }

    public void setLineInFunctionNode(int lineInFunction) {
        this.lineInFunction = lineInFunction;
    }

    public IFunctionNode getFunctionNode() {
        return functionNode;
    }

    public int getLineInFunction() {
        return lineInFunction;
    }

    public void setLineInFunction(int lineInFunction) {
        this.lineInFunction = lineInFunction;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    @Override
    public String toString() {
        return "ProbePoint{" +
                "name='" + name + '\'' +
                ", before='" + before + '\'' +
                ", content='" + content + '\'' +
                ", after='" + after + '\'' +
                ", functionNode=" + functionNode +
                ", lineInFunction=" + lineInFunction +
                ", testCases=" + testCases +
                ", sourcecodeFileNode=" + sourcecodeFileNode +
                ", lineInSourceCodeFile=" + lineInSourceCodeFile +
                ", path='" + path + '\'' +
                '}';
    }

    @Override
    public int compareTo(ProbePoint o) {
        if (this.lineInSourceCodeFile < o.lineInSourceCodeFile) {
            return -1;
        } else if (this.lineInSourceCodeFile > o.lineInSourceCodeFile) {
            return 1;
        }
        return this.name.compareTo(o.name);
    }
}


