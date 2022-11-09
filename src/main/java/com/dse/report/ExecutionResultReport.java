package com.dse.report;

import com.dse.config.WorkspaceConfig;
import com.dse.testcase_execution.result_trace.gtest.Execution;
import com.dse.report.element.*;
import com.dse.testcase_manager.*;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ExecutionResultReport extends ReportView {

    private final ITestCase testCase;

//    private final Execution execution;

    public ExecutionResultReport(ITestCase testCase, LocalDateTime creationDateTime) {
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

    @Override
    protected void generate() {
        // STEP 1: generate table of contents section
        sections.add(generateTableOfContents());

        // STEP 2: generate configuration data section
        sections.add(generateConfigurationData());

        // STEP 3 + 4: generate test case section & generate execution result section
        sections.add(new TestCaseConfiguration(testCase));

        if (testCase instanceof TestCase) {
            sections.add(new BasicExecutionResult(testCase));
        } else if (testCase instanceof CompoundTestCase) {
            sections.add(new CompoundExecutionResult(testCase));
        }

        // STEP 5: generate log section
        if (testCase.getExecuteLog() != null
                && !testCase.getExecuteLog().isEmpty())
            sections.add(generateLog());

        // STEP 6: save execution result
        TestCaseManager.exportTestCaseToFile(testCase);
    }

    @Override
    protected TableOfContents generateTableOfContents() {
        TableOfContents tableOfContents = new TableOfContents();

        tableOfContents.getBody().add(new TableOfContents.Item("Configuration Data", "config-data"));

        String testCaseName = testCase.getName();

        tableOfContents.getBody().add(
                new TableOfContents.Item("Test Case Configuration", testCaseName + "-config"));

        tableOfContents.getBody().add(
                new TableOfContents.Item("Test Execution Result", testCaseName + "-result"));

        return tableOfContents;
    }

    protected Section generateLog() {
        Section section = new Section("exec-log");

        section.getTitle().add(new Section.Line("Execution Message Log", COLOR.DARK));

        CodeView log = new CodeView();
        String message = testCase.getExecuteLog();
        log.setContent(message);

        section.getBody().add(log);

        section.getBody().add(new Section.BlankLine());

        return section;
    }

    protected ConfigurationData generateConfigurationData() {
        ConfigurationData configData = new ConfigurationData();

        List<ITestCase> elements = new ArrayList<>();

        if (testCase instanceof TestCase)
            elements.add(testCase);
        else if (testCase instanceof CompoundTestCase) {
            for (TestCaseSlot slot : ((CompoundTestCase) testCase).getSlots()) {
                String elementName = slot.getTestcaseName();
                TestCase element = TestCaseManager.getBasicTestCaseByName(elementName);
                elements.add(element);
            }
        }

        TestCaseTable table = new TestCaseTable(elements);

        configData.setCreationDate(getCreationDate());
        configData.setCreationTime(getCreationTime());
        configData.setTable(table);

        configData.generate();

        return configData;
    }

    @Override
    protected void setPathDefault() {
        this.path = new WorkspaceConfig().fromJson().getExecutionReportDirectory()
                + File.separator + testCase.getName() + ".html";
    }
}
