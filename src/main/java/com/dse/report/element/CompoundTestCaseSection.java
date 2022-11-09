package com.dse.report.element;

import com.dse.testcase_execution.result_trace.gtest.Execution;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.INode;
import com.dse.testcase_manager.*;
import com.dse.util.Utils;

public class CompoundTestCaseSection extends AbstractTestCaseSection {

    public CompoundTestCaseSection(ITestCase testCase) {
        super(testCase);
    }

    @Override
    protected void generate(ITestCase testCase) {
        Line sectionTitle = new Line("&lt;&lt;COMPOUND&gt;&gt; Test Case Section: " + testCase.getName(), COLOR.DARK);
        title.add(sectionTitle);

        body.add(new TestCaseConfiguration(testCase));

        if (testCase instanceof CompoundTestCase) {
            body.add(generateCompoundDataSection((CompoundTestCase) testCase));
//            for (TestCaseSlot slot : ((CompoundTestCase) testCase).getSlots()) {
//                String elementName = slot.getTestcaseName();
//                TestCase element = TestCaseManager.getBasicTestCaseByName(elementName);
//
//                body.add(new BasicTestCaseSection(element, execution));
//            }
        }
    }

    private Section generateCompoundDataSection(CompoundTestCase testCase) {
        Section section = new Section(testCase.getName() + "-data");

        section.getTitle().add(new Line("Compound Data Listing", COLOR.MEDIUM));

        Table overall = new Table();
        overall.getRows().add(new Table.Row("Compound Test Name:", testCase.getName()));
        overall.getRows().add(new Table.Row("Compound File Name:", testCase.getSourceCodeFile()));
        section.getBody().add(overall);

        Table data = new Table(false);
        data.getRows().add(new Table.HeaderRow("Slot", "Unit", "Subprogram", "Test Cases", "Iterations"));
        for (TestCaseSlot slot : testCase.getSlots()) {
            String index = String.valueOf(testCase.getSlots().indexOf(slot));
            String iterations = String.valueOf(slot.getNumberOfIterations());
            String elementName = slot.getTestcaseName();
            TestCase element = TestCaseManager.getBasicTestCaseByName(elementName);

            if (element == null)
                continue;

            ICommonFunctionNode subprogram = element.getFunctionNode();
            INode unit = Utils.getSourcecodeFile(subprogram);

            data.getRows().add(new Table.Row(index, unit.getName(), subprogram.getName(), elementName, iterations));
        }
        section.getBody().add(data);
        section.getBody().add(new BlankLine());

        return section;
    }
}
