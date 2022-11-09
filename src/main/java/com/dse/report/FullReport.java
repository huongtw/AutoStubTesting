package com.dse.report;

import com.dse.config.WorkspaceConfig;
import com.dse.testcase_execution.result_trace.gtest.Execution;
import com.dse.report.element.*;
import com.dse.testcase_manager.CompoundTestCase;
import com.dse.testcase_manager.ITestCase;
import com.dse.testcase_manager.TestCase;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Collections;

public class FullReport extends ReportView {

    private ITestCase testCase;

//    private Execution execution;

    public FullReport(ITestCase testCase, LocalDateTime creationDateTime) {
        // set report name
        super(String.format("%s - Test Case Report", testCase.getName()));

        // set report attributes
        this.testCase = testCase;
        this.creationDateTime = creationDateTime;

//        // load GTest execution (xml file)
//        this.execution = Execution.load(testCase);

        // set report location path to default
        setPathDefault();

        // generate test case report
        generate();
    }

    protected void generate() {
        // STEP 1: generate table of contents section
        sections.add(generateTableOfContents());

        // STEP 2: generate configuration data section
        sections.add(generateConfigurationData());

        // STEP 3 + 4: generate test case section & generate execution result section
        if (testCase instanceof TestCase) {
            sections.add(new BasicTestCaseSection(testCase));
            sections.add(new BasicExecutionResult(testCase));
        } else if (testCase instanceof CompoundTestCase) {
            sections.add(new CompoundTestCaseSection(testCase));
            sections.add(new CompoundExecutionResult(testCase));
        }

    }

    protected TableOfContents generateTableOfContents() {
        TableOfContents tableOfContents = new TableOfContents();

        tableOfContents.getBody().add(new TableOfContents.Item("Configuration Data", "config-data"));

        String testCaseName = testCase.getName();

        TableOfContents.Item testCaseItem = new TableOfContents.Item(testCaseName, testCaseName);

        if (testCase instanceof TestCase)
            testCaseItem = generateTestCaseContentItem(testCaseName);
        else if (testCase instanceof CompoundTestCase)
            testCaseItem.getItems().add(
                    new TableOfContents.Item("Test Case Configuration", testCaseName + "-config"));

        tableOfContents.getBody().add(testCaseItem);

        tableOfContents.getBody().add(
                new TableOfContents.Item("Test Execution Result", testCaseName + "-result"));

        return tableOfContents;
    }

    protected ConfigurationData generateConfigurationData() {
        ConfigurationData configData = new ConfigurationData();
//
//        List<ITestCase> elements = new ArrayList<>();
//
//        if (testCase instanceof TestCase)
//            elements.add(testCase);
//        else if (testCase instanceof CompoundTestCase) {
//            for (TestCaseSlot slot : ((CompoundTestCase) testCase).getSlots()) {
//                String elementName = slot.getTestcaseName();
//                TestCase element = TestCaseManager.getBasicTestCaseByName(elementName);
//                elements.add(element);
//            }
//        }

        TestCaseTable table = new TestCaseTable(Collections.singletonList(testCase));

        configData.setCreationDate(getCreationDate());
        configData.setCreationTime(getCreationTime());
        configData.setTable(table);

        configData.generate();

        return configData;
    }

    private TableOfContents.Item generateTestCaseContentItem(String testCaseName) {
        TableOfContents.Item testCaseItem = new TableOfContents.Item(testCaseName, testCaseName);
        testCaseItem.getItems()
                .add(new TableOfContents.Item("Test Case Configuration", testCaseName + "-config"));
        testCaseItem.getItems()
                .add(new TableOfContents.Item("Test Case Data", testCaseName + "-data"));

        return testCaseItem;
    }

    protected void setPathDefault() {
        this.path = new WorkspaceConfig().fromJson().getFullReportDirectory()
                + File.separator + testCase.getName() + ".html";
    }
}
