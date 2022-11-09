package com.dse.testcase_execution.result_trace.gtest;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"name", "tests", "failures", "disabled", "errors", "time", "testCases"})
public class TestSuite {

    @XmlAttribute
    private String name;

    @XmlAttribute
    private int tests;

    @XmlAttribute
    private int failures;

    @XmlAttribute
    private int disabled;

    @XmlAttribute
    private int errors;

    @XmlAttribute
    private double time;

    @XmlElement(name = "testcase")
    private List<TestCase> testCases;

    public String getName() {
        return name;
    }

    public int getTests() {
        return tests;
    }

    public int getFailures() {
        return failures;
    }

    public int getDisabled() {
        return disabled;
    }

    public int getErrors() {
        return errors;
    }

    public double getTime() {
        return time;
    }

    public List<TestCase> getTestCases() {
        return testCases;
    }

    @Override
    public String toString() {
        return "Test Suite: " + name;
    }
}
