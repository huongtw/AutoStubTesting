package com.dse.report.element;

import com.dse.coverage.CoverageDataObject;
import com.dse.coverage.CoverageManager;
import com.dse.coverage.FunctionCoverageComputation;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.ISourcecodeFileNode;
import com.dse.parser.object.MacroFunctionNode;
import com.dse.testdata.object.IterationSubprogramNode;
import com.dse.testdata.object.RootDataNode;
import com.dse.util.PathUtils;
import com.dse.util.TestPathUtils;
import com.dse.coverage.function_call.ConstructorCall;
import com.dse.coverage.function_call.FunctionCall;
import com.dse.environment.object.EnviroCoverageTypeNode;
import com.dse.testcase_execution.result_trace.AssertionResult;
import com.dse.testcase_execution.result_trace.ResultTrace;
import com.dse.testcase_execution.result_trace.gtest.Execution;
import com.dse.testcase_execution.result_trace.IResultTrace;
import com.dse.environment.Environment;
import com.dse.guifx_v3.helps.UIController;
import com.dse.parser.object.INode;
import com.dse.search.Search2;
import com.dse.testcase_manager.ITestCase;
import com.dse.testcase_manager.TestCase;
import com.dse.testdata.object.IDataNode;
import com.dse.testdata.object.SubprogramNode;
import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParser;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BasicExecutionResult extends AbstractExecutionResult {

    private static final int MAX_DUPLICATE = 3;
    private static final boolean SKIP_DUPLICATE = false;

    public BasicExecutionResult(ITestCase testCase) {
        super(testCase);
    }

    @Override
    protected void generate(ITestCase tc) {
        TestCase testCase = (TestCase) tc;

        Line sectionTitle = new Line(testCase.getName() + " Execution Result", COLOR.DARK);
        title.add(sectionTitle);

        String testPath = testCase.getTestPathFile();

        if (testPath == null || !new File(testPath).exists()) {
            body.add(new Line("Test case haven't executed yet", COLOR.LIGHT));
        } else
            generateBody(testCase);

        body.add(new BlankLine());
    }

    private static final String EMPTY_COVERAGE = "<pre>[^\\w]*</pre>\\s*";

    private void generateCoverageHighlight(TestCase testCase) {
        body.add(new Section.CenteredLine("Coverage Highlight", COLOR.MEDIUM));

        String coverageHighlight = null;

        String typeOfCoverage = Environment.getInstance().getTypeofCoverage();
        switch (typeOfCoverage){
            case EnviroCoverageTypeNode.STATEMENT:
            case EnviroCoverageTypeNode.BRANCH:
            case EnviroCoverageTypeNode.BASIS_PATH:
            case EnviroCoverageTypeNode.MCDC:{
                coverageHighlight = Utils.readFileContent(testCase.getHighlightedFunctionPath(typeOfCoverage));
                break;
            }

            case EnviroCoverageTypeNode.STATEMENT_AND_BRANCH: {
                coverageHighlight = Utils.readFileContent(testCase.getHighlightedFunctionPath(EnviroCoverageTypeNode.BRANCH));
                break;
            }

            case EnviroCoverageTypeNode.STATEMENT_AND_MCDC:{
                coverageHighlight = Utils.readFileContent(testCase.getHighlightedFunctionPath(EnviroCoverageTypeNode.MCDC));
                break;
            }
        }

        if (testCase.getFunctionNode() instanceof MacroFunctionNode) {
            body.add(new Section.CenteredLine("Don't support to instrument macro functions", COLOR.WHITE));
        } else if (coverageHighlight == null || coverageHighlight.matches(EMPTY_COVERAGE)) {
            body.add(new Section.CenteredLine("Cannot instrument this function", COLOR.WHITE));
        } else {
            body.add(new CodeView(coverageHighlight));
        }
    }

    private void generateBody(TestCase testCase) {
        generateCoverageHighlight(testCase);
        boolean pass = generateEventsSection(testCase);

        File testPath = new File(testCase.getTestPathFile());

        String result = Execution.RUNTIME_ERROR;

        double executedTime = testCase.getExecutedTime();

        if (testPath.exists()) {
            String testPathContent = Utils.readFileContent(testPath);
            if (testPathContent.contains(TestPathUtils.END_TAG)) {
                result = pass ? Execution.PASSED : Execution.FAILED;
            }
        }

        Table overall = generateOverallResultsTable(testCase, result, executedTime);
        body.add(0, overall);
    }

    private boolean generateEventsSection(TestCase testCase) {
        List<IResultTrace> failures = ResultTrace.load(testCase);

        List<FunctionCall> calledFunctions = TestPathUtils.traceFunctionCall(testCase.getTestPathFile());

        RootDataNode rootDataNode = testCase.getRootDataNode();

        List<SubprogramNode> subprograms = Search2.searchNodes(rootDataNode, SubprogramNode.class);
        subprograms.removeIf(s -> s instanceof IterationSubprogramNode);

        int skip = 0;
        String firstLine = Utils.readFileContent(testCase.getTestPathFile()).split("\\R")[0];
        if (firstLine.startsWith(TestPathUtils.SKIP_TAG))
            skip = Integer.parseInt(firstLine.substring(TestPathUtils.SKIP_TAG.length()));

        /*
         * Result PASS/ALL
         */
        AssertionResult results = new AssertionResult();

        int duplicate = 0;
        FunctionCall prev = null;

        Event.Position position;

//        for (int i = 0; i < calledFunctions.size() && position != Event.Position.LAST; i++) {
        for (int i = 0; i < calledFunctions.size(); i++) {
            FunctionCall current = calledFunctions.get(i);
            SubprogramNode subprogram = findSubprogram(subprograms, current);

            position = current.getCategory();

            if (prev != null && prev.equals(current))
                duplicate++;
            else
                duplicate = 0;

            // skip current event if access the same subprogram as previous
            if (SKIP_DUPLICATE && duplicate >= MAX_DUPLICATE)
                continue;

            int index = i + skip + 1;

            if (subprogram != null && Utils.getSourcecodeFile(subprogram.getFunctionNode()) != null) {
                Event event;

                if (position == Event.Position.MIDDLE && current.getIterator() > 1)
                    event = new Event(subprogram, failures, index, position, current.getIterator());
                else
                    event = new Event(subprogram, failures, index, position);

                if (position == Event.Position.LAST) {
                    Map<SubprogramNode, Integer> numberOfCalls = new HashMap<>();
                    List<SubprogramNode> stubSubprograms = Search2.searchStubSubprograms(rootDataNode);
                    for (SubprogramNode stubSub : stubSubprograms) {
                        int numberOfCall = 0;
                        for (FunctionCall functionCall : calledFunctions) {
                            String calledPath = functionCall.getAbsolutePath();
                            String stubPath = stubSub.getFunctionNode().getAbsolutePath();
                            if (PathUtils.equals(calledPath, stubPath)) {
                                numberOfCall++;
                            }
                        }
                        numberOfCalls.put(stubSub, numberOfCall);
                    }

                    event.setNumberOfCalls(numberOfCalls);
                }

                event.generate();
                results.append(event.getResults());

                body.add(event);
            }

            prev = calledFunctions.get(i);
        }

        testCase.setExecutionResult(results);

        String textColor = results.isAllPass() ? "green" : "red";
        double passPercent = results.getPercent();

        Table resultsSummary = new Table();
        resultsSummary.getRows().add(
            new Table.Row(
                new Text(String.format("Expected Results matched %.2f%%", passPercent)),
                new Text(String.format("(%d/%d) PASS", results.getPass(), results.getTotal()), TEXT_STYLE.BOLD, textColor)
            )
        );

        body.add(resultsSummary);

        return results.isAllPass();
    }

    private SubprogramNode findSubprogram(List<SubprogramNode> subprograms, FunctionCall call) {
        for (SubprogramNode subprogram : subprograms) {
            if (subprogram != null) {
                INode functionNode = subprogram.getFunctionNode();
                String functionPath = Utils.normalizePath(functionNode.getAbsolutePath());
                String callPath = Utils.normalizePath(call.getAbsolutePath());
                if (call instanceof ConstructorCall) {
                    if (functionPath.equals(callPath)
                            && subprogram.getPathFromRoot().equals(((ConstructorCall) call).getParameterPath()))
                        return subprogram;
                } else {
                    if (functionPath.equals(callPath))
                        return subprogram;
                }
            }
        }

        try {
            INode called = UIController.searchFunctionNodeByPath(call.getAbsolutePath());
            return new SubprogramNode(called);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        return null;
    }

    private Table generateOverallResultsTable(TestCase testCase, String result, double executedTime) {
        Text resultText = new Text(result, TEXT_STYLE.BOLD, result.equals(Execution.PASSED) ? "green" : "red");

        String typeOfCoverage = Environment.getInstance().getTypeofCoverage();

        String srcCovPercentage = SpecialCharacter.EMPTY;
        String funcCovPercentage = SpecialCharacter.EMPTY;
        switch (typeOfCoverage) {
            case EnviroCoverageTypeNode.BRANCH:
            case EnviroCoverageTypeNode.STATEMENT:
            case EnviroCoverageTypeNode.MCDC:
            case EnviroCoverageTypeNode.BASIS_PATH: {
                srcCovPercentage = getFileCoveragePercentage(testCase, typeOfCoverage);
                funcCovPercentage = getFunctionCoveragePercentage(testCase, typeOfCoverage);
                break;
            }

            case EnviroCoverageTypeNode.STATEMENT_AND_BRANCH: {
//                String statementCoveragePercent = getFileCoveragePercentage(testCase, EnviroCoverageTypeNode.STATEMENT)
//                        + " (" + EnviroCoverageTypeNode.STATEMENT + "); ";
//
//                String branchCoveragePercent = getFileCoveragePercentage(testCase, EnviroCoverageTypeNode.BRANCH)
//                        + " (" + EnviroCoverageTypeNode.BRANCH + ");";
//
//                srcCovPercentage = statementCoveragePercent + branchCoveragePercent;

                srcCovPercentage = getFileCoveragePercentage(testCase, EnviroCoverageTypeNode.STATEMENT)
                        + " (" + EnviroCoverageTypeNode.STATEMENT + "); "
                        + getFileCoveragePercentage(testCase, EnviroCoverageTypeNode.BRANCH)
                        + " (" + EnviroCoverageTypeNode.BRANCH + ");";

                funcCovPercentage = getFunctionCoveragePercentage(testCase, EnviroCoverageTypeNode.STATEMENT)
                        + " (" + EnviroCoverageTypeNode.STATEMENT + "); "
                        + getFunctionCoveragePercentage(testCase, EnviroCoverageTypeNode.BRANCH)
                        + " (" + EnviroCoverageTypeNode.BRANCH + ");";

                break;
            }

            case EnviroCoverageTypeNode.STATEMENT_AND_MCDC: {
//                String statementCoveragePercent = getFileCoveragePercentage(testCase, EnviroCoverageTypeNode.STATEMENT)
//                        + " (" + EnviroCoverageTypeNode.STATEMENT + "); ";
//
//                String mcdcCoveragePercent = getFileCoveragePercentage(testCase, EnviroCoverageTypeNode.MCDC)
//                        + " (" + EnviroCoverageTypeNode.MCDC + ");";
//
//                srcCovPercentage = statementCoveragePercent + mcdcCoveragePercent;

                srcCovPercentage = getFileCoveragePercentage(testCase, EnviroCoverageTypeNode.STATEMENT)
                        + " (" + EnviroCoverageTypeNode.STATEMENT + "); "
                        + getFileCoveragePercentage(testCase, EnviroCoverageTypeNode.MCDC)
                        + " (" + EnviroCoverageTypeNode.MCDC + ");";

                funcCovPercentage = getFunctionCoveragePercentage(testCase, EnviroCoverageTypeNode.STATEMENT)
                        + " (" + EnviroCoverageTypeNode.STATEMENT + "); "
                        + getFunctionCoveragePercentage(testCase, EnviroCoverageTypeNode.MCDC)
                        + " (" + EnviroCoverageTypeNode.MCDC + ");";

                break;
            }
        }

        Table overall = new Table();

        overall.getRows().add(new Table.Row(new Table.Cell<Text>("Result:"), new Table.Cell<>(resultText)));
        overall.getRows().add(new Table.Row("Source Code Coverage:", String.format("%s", srcCovPercentage)));
        overall.getRows().add(new Table.Row("Function Coverage:", String.format("%s", funcCovPercentage)));

        if (executedTime >= 0) {
            overall.getRows()
                    .add(new Table.Row("Executed Time:", String.format("%.3fs", executedTime)));
        }

        return overall;
    }

    private String getFileCoveragePercentage(TestCase testCase, String coverageType) {
        CoverageDataObject coverageData = CoverageManager
                .getCoverageOfMultiTestCaseAtSourcecodeFileLevel(Collections.singletonList(testCase), coverageType);

        if (coverageData == null)
            return SpecialCharacter.EMPTY;

        String progress;
        double percentage = coverageData.getProgress();
        if (percentage != 1) {
            percentage = Utils.round(percentage * 100, 2);
            progress = percentage + "%";
        } else {
            progress = "100%";
        }

        return String.format("%s - %d/%d", progress, coverageData.getVisited(), coverageData.getTotal());
    }

    private String getFunctionCoveragePercentage(TestCase testCase, String coverageType) {
        CoverageDataObject coverageData = CoverageManager
                .getCoverageOfMultiTestCaseAtFunctionLevel(Collections.singletonList(testCase), coverageType);

        if (coverageData == null)
            return SpecialCharacter.EMPTY;

        String progress;
        double percentage = coverageData.getProgress();
        if (percentage != 1) {
            percentage = Utils.round(percentage * 100, 2);
            progress = percentage + "%";
        } else {
            progress = "100%";
        }

        return String.format("%s - %d/%d", progress, coverageData.getVisited(), coverageData.getTotal());
    }
}
