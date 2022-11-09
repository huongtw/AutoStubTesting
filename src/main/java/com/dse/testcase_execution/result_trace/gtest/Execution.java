package com.dse.testcase_execution.result_trace.gtest;

import com.dse.testcase_manager.ITestCase;
import com.dse.util.SpecialCharacter;
import com.dse.logger.AkaLogger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "testsuites")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"tests", "failures", "disabled", "errors", "timestamp", "time", "name", "testSuites"})
public class Execution {
    public static final String PASSED = "PASSED";

    public static final String FAILED = "FAILED";

    public static final String RUNTIME_ERROR = "Runtime Error";

    @XmlAttribute
    private int tests;

    @XmlAttribute
    private int failures;

    @XmlAttribute
    private int disabled;

    @XmlAttribute
    private int errors;

    @XmlAttribute
    private String timestamp;

    @XmlAttribute
    private double time;

    @XmlAttribute
    private String name;

    @XmlElement(name="testsuite")
    private List<TestSuite> testSuites;

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

    public List<TestSuite> getTestSuites() {
        return testSuites;
    }

    public List<TestCase> getTestCases() {
        List<TestCase> testCases = new ArrayList<>();

        for (TestSuite testSuite : testSuites)
            testCases.addAll(testSuite.getTestCases());

        return testCases;
    }

    public TestCase getTestCaseByName(String name) {
        String normalize = name.replaceAll("[^\\w]", SpecialCharacter.UNDERSCORE);

        for (TestCase testCase : getTestCases())
            if (testCase.getName().equals(normalize))
                return testCase;

        return null;
    }

    public String getTimestamp() {
        return timestamp;
    }

    private static final AkaLogger logger = AkaLogger.get(Execution.class);

    public static Execution load(ITestCase testCase) {
        String resultFilePath = testCase.getExecutionResultFile();

        if (resultFilePath == null) {
            logger.error(testCase.getName() + " haven't executed yet.");
            return null;
        }

        try {
            JAXBContext context = JAXBContext.newInstance(Execution.class);
            Unmarshaller importer = context.createUnmarshaller();
            return (Execution) importer.unmarshal(new File(testCase.getExecutionResultFile()));
        } catch (JAXBException e) {
            logger.error("Cant find Gtest report of " + testCase.getName());
        }

        return null;
    }
}
