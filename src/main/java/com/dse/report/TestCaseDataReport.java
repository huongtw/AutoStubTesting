package com.dse.report;

import com.dse.config.WorkspaceConfig;
import com.dse.testcase_execution.result_trace.gtest.Execution;
import com.dse.report.element.*;
import com.dse.testcase_manager.*;
import com.dse.util.SpecialCharacter;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

public class TestCaseDataReport extends ReportView {
    private List<ITestCase> testCases;

    public TestCaseDataReport(List<ITestCase> testCases, LocalDateTime creationDateTime) {
        // set report name
        super("Test Case Data Report");

        // set report attributes
        this.testCases = testCases;
        this.creationDateTime = creationDateTime;

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

        // STEP 3: generate test case section
        for (ITestCase testCase : testCases) {
            Execution execution = Execution.load(testCase);

            if (testCase instanceof TestCase)
                sections.add(new BasicTestCaseSection(testCase));
            else if (testCase instanceof CompoundTestCase)
                sections.add(new CompoundTestCaseSection(testCase));
        }
    }

    protected TableOfContents generateTableOfContents() {
        TableOfContents tableOfContents = new TableOfContents();

        tableOfContents.getBody().add(new TableOfContents.Item("Configuration Data", "config-data"));

        for (ITestCase testCase : testCases) {
            String testCaseName = testCase.getName();

            TableOfContents.Item testCaseItem = new TableOfContents.Item(testCaseName, testCaseName);

            if (testCase instanceof TestCase)
                testCaseItem = generateTestCaseContentItem(testCaseName);
            else if (testCase instanceof CompoundTestCase)
                testCaseItem.getItems().add(
                        new TableOfContents.Item("Test Case Configuration", testCaseName + "-config"));

            tableOfContents.getBody().add(testCaseItem);
        }

        return tableOfContents;
    }

    protected ConfigurationData generateConfigurationData() {
        ConfigurationData configData = new ConfigurationData();

        TestCaseTable table = new TestCaseTable(testCases);

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
        path = new WorkspaceConfig().fromJson().getTestDataReportDirectory() + File.separator;

        if (testCases.size() == 1)
            path += testCases.get(0).getName() + ".html";
        else
            path += (getCreationDate() + SpecialCharacter.UNDERSCORE + getCreationTime())
                    .replace(SpecialCharacter.SPACE, SpecialCharacter.UNDERSCORE_CHAR)
                    .replaceAll("[^\\w]", SpecialCharacter.UNDERSCORE) + ".html";
    }
}
