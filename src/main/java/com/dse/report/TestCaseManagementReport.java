package com.dse.report;

import com.dse.config.WorkspaceConfig;
import com.dse.coverage.function_call.FunctionCall;
import com.dse.environment.Environment;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.INode;
import com.dse.report.element.*;
import com.dse.search.Search;
import com.dse.search.condition.AbstractFunctionNodeCondition;
import com.dse.testcase_execution.result_trace.AssertionResult;
import com.dse.testcase_manager.CompoundTestCase;
import com.dse.testcase_manager.ITestCase;
import com.dse.testcase_manager.TestCase;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.testcasescript.object.*;
import com.dse.testdata.object.SubprogramNode;
import com.dse.util.PathUtils;
import com.dse.util.SpecialCharacter;
import com.dse.util.TestPathUtils;
import com.dse.util.Utils;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TestCaseManagementReport extends ReportView {
    private boolean isEnvironment = false;
    private boolean containCompound = false;
    private List<INode> units = new ArrayList<>();

    public TestCaseManagementReport(List<ITestcaseNode> nodes, LocalDateTime creationDt) {
        super("Test Case Management Report");

        // set report creation date time
        setCreationDateTime(creationDt);

        // set report location path to default
        setPathDefault();

        // find all selected unit
        findAllSelectedUnitsByTestNode(nodes);

        generate();
    }

    public TestCaseManagementReport(String unitName, LocalDateTime creationDt) {
        super("Test Case Management Report");

        // set report creation date time
        setCreationDateTime(creationDt);

        // set report location path to default
        setPathDefault();

        // find all selected unit
        findSelectedUnitByName(unitName);

        generate();
    }

    private void findSelectedUnitByName(String name) {
        List<INode> uuts = new ArrayList<>(Environment.getInstance().getUUTs());
        uuts.addAll(Environment.getInstance().getSBFs());

        units = uuts.stream().filter(u ->
                u.getName().equals(name)).collect(Collectors.toList());
    }

    private void findAllSelectedUnitsByTestNode(List<ITestcaseNode> nodes) {
        List<INode> allUnits = Environment.getInstance().getUUTs();
        allUnits.addAll(Environment.getInstance().getSBFs());

        for (ITestcaseNode node : nodes) {
            if (node instanceof TestCompoundSubprogramNode)
                containCompound = true;

            else if (node instanceof TestUnitNode) {
                String unitPath = ((TestUnitNode) node).getName();
                for (INode unit : allUnits) {
                    if (PathUtils.equals(unitPath, unit.getAbsolutePath())) {
                        units.add(unit);
                        break;
                    }
                }

            } else if (node instanceof TestcaseRootNode) {
                units = allUnits;
                containCompound = true;
                isEnvironment = true;
                break;
            }
        }
    }

    @Override
    protected void generate() {
        sections.add(generateTableOfContents());

        sections.add(generateConfigurationData());
        sections.add(new Section.BlankLine());

        sections.add(generateOverallResults());
        sections.add(new Section.BlankLine());

        sections.add(new TestCaseManagement(containCompound, units));
        sections.add(new Section.BlankLine());

        sections.add(new Metrics(units));
    }

    @Override
    protected TableOfContents generateTableOfContents() {
        TableOfContents tableOfContents = new TableOfContents();

        tableOfContents.getBody().add(new TableOfContents.Item("Configuration Data", "config-data"));

        tableOfContents.getBody().add(
                new TableOfContents.Item("Overall Results", "overall-results"));

        tableOfContents.getBody().add(
                new TableOfContents.Item("Test Case Management", "tcs-manage"));

        tableOfContents.getBody().add(
                new TableOfContents.Item("Metrics", "metrics"));

        return tableOfContents;
    }

    @Override
    protected Section generateConfigurationData() {
        Section section = new Section("config-data");

        section.getTitle().add(new Section.Line("Configuration Data", COLOR.DARK));

        Table table = new Table();

        String scope = SpecialCharacter.EMPTY;
        if (isEnvironment)
            scope = "All units under test";
        else {
            if (containCompound)
                scope = TestSubprogramNode.COMPOUND_SIGNAL;

            for (INode unit : units) {
                scope += ", " + unit.getName();
            }

            if (scope.startsWith(", "))
                scope = scope.substring(2);
        }

        table.getRows().add(new Table.Row("This report include data for: ", scope));
        table.getRows().add(new Table.Row("Date of Report Creation:", getCreationDate()));
        table.getRows().add(new Table.Row("Time of Report Creation:", getCreationTime()));
        section.getBody().add(table);

        return section;
    }

    protected Section generateOverallResults() {
        Section section = new Section("overall-results");

        section.getTitle().add(new Section.Line("Overall Results", COLOR.DARK));

        Table table = new Table();
        table.getRows().add(new Table.HeaderRow("Category", "Results"));

        AssertionResult testCaseResults = new AssertionResult();
        AssertionResult expectedResults = new AssertionResult();

        List<String> testCaseNames = getAllTestCaseUnderUnit();

        for (String testCaseName : testCaseNames) {
            ITestCase testCase = TestCaseManager.getTestCaseByNameWithoutData(testCaseName);

            if (testCase != null) {
                AssertionResult result = testCase.getExecutionResult();

                // calculate result
                if (result == null) {
                    result = calculateResult(testCase);
                }

                if (result != null) {
                    if (result.isAllPass()) {
                        testCaseResults.increasePass();
                    }

                    testCaseResults.increaseTotal();
                    expectedResults.append(result);

                }
            }
        }

        table.getRows().add(new Table.Row(
                new Table.Cell<Text>("Test Cases:"),
                new Table.Cell<Text>(String.format("PASS %d/%d", testCaseResults.getPass(), testCaseResults.getTotal()),
                        getBackgroundColor(testCaseResults))
        ));

        table.getRows().add(new Table.Row(
                new Table.Cell<Text>("Expecteds:"),
                new Table.Cell<Text>(String.format("PASS %d/%d", expectedResults.getPass(), expectedResults.getTotal()),
                        getBackgroundColor(expectedResults))
        ));

        section.getBody().add(table);

        return section;
    }

    private List<String> getAllTestCaseUnderUnit() {
        List<String> testCaseNames = new ArrayList<>();

        if (isEnvironment) {
            testCaseNames.addAll(new ArrayList<>(TestCaseManager.getNameToCompoundTestCaseMap().keySet()));
            testCaseNames.addAll(new ArrayList<>(TestCaseManager.getNameToBasicTestCaseMap().keySet()));

        } else {
            if (containCompound)
                testCaseNames.addAll(TestCaseManager.getNameToCompoundTestCaseMap().keySet());

            for (INode unit : units) {
                List<ICommonFunctionNode> subprograms = Search.searchNodes(unit, new AbstractFunctionNodeCondition());

                for (ICommonFunctionNode subprogram : subprograms) {
                    Set<String> testcaseTmpNames = TestCaseManager.getFunctionToTestCasesMap().get(subprogram);
                    if (testcaseTmpNames != null) {
                        testCaseNames.addAll(testcaseTmpNames);
                    }
                }
            }
        }

        return testCaseNames.stream().distinct().collect(Collectors.toList());
    }

    private AssertionResult calculateResult(ITestCase testCase) {
        if (testCase instanceof TestCase) {
            new BasicExecutionResult(testCase);
        } else if (testCase instanceof CompoundTestCase) {
            new CompoundExecutionResult(testCase);
        }
        return testCase.getExecutionResult();
    }

    protected String getBackgroundColor(AssertionResult result) {
        String bgColor;

        if (result.isAllPass())
            bgColor = COLOR.GREEN;
        else if (result.getPass() == 0)
            bgColor = COLOR.RED;
        else
            bgColor = COLOR.YELLOW;

        return bgColor;
    }

    @Override
    protected void setPathDefault() {
        this.path = new WorkspaceConfig().fromJson().getReportDirectory()
                + File.separator + "test-cases-management.html";
    }
}
