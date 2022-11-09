package com.dse.testcase_execution.result_trace.gtest;

import com.dse.testcase_execution.result_trace.IResultTrace;

import javax.xml.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"name", "status", "time", "className", "failure"})
public class TestCase {

    @XmlAttribute
    private String name;

    @XmlAttribute
    private String status;

    @XmlAttribute
    private double time;

    @XmlAttribute(name = "classname")
    private String className;

    @XmlElement(name = "failure")
    private List<Failure> failure;

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public double getTime() {
        return time;
    }

    public String getClassName() {
        return className;
    }

    public List<IResultTrace> getFailure() {
        if (failure == null)
            return null;

        return failure.stream()
                .map(f -> (IResultTrace) f)
                .collect(Collectors.toList());
    }

    public String getResult() {
        return (failure == null) ? Execution.PASSED : Execution.FAILED;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setFailure(List<Failure> failure) {
        this.failure = failure;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTime(double time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "Test Case: " + name;
    }
}
