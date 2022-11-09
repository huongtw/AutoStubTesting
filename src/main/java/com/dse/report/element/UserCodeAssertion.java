package com.dse.report.element;

import com.dse.testcase_execution.result_trace.AssertionResult;
import com.dse.testcase_execution.result_trace.IResultTrace;
import com.dse.testdata.comparable.AssertMethod;
import com.dse.util.SourceConstant;

import java.util.List;

public class UserCodeAssertion extends Table {

    private final List<IResultTrace> failures;

    private final AssertionResult results = new AssertionResult();

    public UserCodeAssertion(List<IResultTrace> failures) {
        this.failures = failures;

        generate();
    }

    private void generate() {
        getRows().add(new Table.Row("Expression", "Expected Value", "Actual Value"));
        failures.forEach(f -> {
            String actualName = f.getActualName();
            String actualOutputRegex = "\\b" + SourceConstant.ACTUAL_OUTPUT + "\\b";
            actualName = actualName.replaceFirst(actualOutputRegex, RETURN_NAME);

            String assertMethod = f.getUserCode();

            boolean isMatch = isMatch(assertMethod, f);

            String actual = f.getActual();
            String expected = findExpectedFromFailure(assertMethod, f.getExpected());

            String bgColor;
            if (isMatch) {
//                results[0]++;
                results.increasePass();
                bgColor = COLOR.GREEN;
            } else {
                bgColor = COLOR.RED;
            }

            Cell<Text> actualCell = new Cell<>(actual, bgColor);
            Cell<Text> expectedCell = new Cell<>(expected, bgColor);

            Row row = new Row(actualName);
            row.getCells().add(expectedCell);
            row.getCells().add(actualCell);

            getRows().add(row);
        });

//        results[1] = failures.size();
        results.setTotal(failures.size());
    }

    public AssertionResult getResults() {
        return results;
    }

    protected boolean isMatch(String assertMethod, IResultTrace failure) {
        String actual = failure.getActual();
        String expected = failure.getExpected();

        return AssertMethod.isMatch(assertMethod, actual, expected);
    }

    protected String findExpectedFromFailure(String assertMethod, String expected) {
        return AssertMethod.findExpectedFromFailure(assertMethod, expected);
    }

    private static final String RETURN_NAME = "return";
}
