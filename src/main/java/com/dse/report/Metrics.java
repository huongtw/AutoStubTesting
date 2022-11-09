package com.dse.report;

import com.dse.coverage.CoverageManager;
import com.dse.coverage.CoverageDataObject;
import com.dse.environment.object.EnviroCoverageTypeNode;
import com.dse.environment.Environment;
import com.dse.parser.object.*;
import com.dse.report.element.Section;
import com.dse.report.element.Table;
import com.dse.report.element.Text;
import com.dse.search.Search;
import com.dse.search.SearchCondition;
import com.dse.search.condition.AbstractFunctionNodeCondition;
import com.dse.search.condition.MacroFunctionNodeCondition;
import com.dse.testcase_manager.AbstractTestCase;
import com.dse.testcase_manager.TestCase;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.logger.AkaLogger;
import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Metrics extends Section {

    private static final AkaLogger logger = AkaLogger.get(Metrics.class);

    private final List<INode> units;

    public Metrics(List<INode> units) {
        super("metrics");
        this.units = units;
        try {
            generate();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void generate() throws InterruptedException {
        title.add(new Line("Metrics", COLOR.DARK));
        title.add(new Line(Environment.getInstance().getTypeofCoverage(), COLOR.MEDIUM));

        Table table = new Table(false);
        Table.HeaderRow headerRow = new Table.HeaderRow("Unit", "Subprogram");
        String[] types = Environment.getInstance().getTypeofCoverage().split("\\+");
        Arrays.stream(types)
                .forEach(text -> {
                    char[] title = text.toLowerCase().toCharArray();
                    title[0] = Character.toUpperCase(title[0]);
                    Table.Cell<Text> cell = new Table.Cell<Text>(new String(title) +  " (File coverage)", COLOR.MEDIUM);

                    Table.Cell<Text> funcCell = new Table.Cell<Text>(new String(title) + " (Function coverage)", COLOR.MEDIUM);
                    headerRow.getCells().add(cell);
                    headerRow.getCells().add(funcCell);
                });

        table.getRows().add(headerRow);
        for (INode unit : units) {
            table.getRows().addAll(generateUnitRow(unit));

            if (units.indexOf(unit) != units.size() - 1)
                table.getRows().add(new Table.Row(new Table.SpanCell<Text>(SpecialCharacter.EMPTY, headerRow.getCells().size())));
        }

        body.add(table);
    }

    private String getSubprogramDisplayName(ICommonFunctionNode subprogram) {
        String tmpName = AbstractTestCase.removeSysPathInName(subprogram.getAbsolutePath());
        String simpleName = (new File(tmpName)).getName();

        String[] pathElements= null;
        if (Utils.isWindows())
            pathElements = tmpName.split("\\\\");
        else if (Utils.isUnix()|| Utils.isMac())
            pathElements = tmpName.split(File.separator);


        for (int i = pathElements.length - 2; i >= 0; i--)
            if (!pathElements[i].contains("."))
                simpleName = pathElements[i] + SpecialCharacter.STRUCTURE_OR_NAMESPACE_ACCESS + simpleName;
            else
                break;

        simpleName = AbstractTestCase.redoTheReplacementOfSysPathInName(simpleName);
        return simpleName;
    }

    private List<Table.Row> generateUnitRow(INode unit) throws InterruptedException {
        List<Table.Row> rows = new ArrayList<>();

        List<SearchCondition> conditions = new ArrayList<>();
        conditions.add(new AbstractFunctionNodeCondition());
        conditions.add(new MacroFunctionNodeCondition());
        List<ICommonFunctionNode> subprograms = Search
                .searchNodes(unit, conditions)
                .stream().map(f -> (ICommonFunctionNode) f)
                .collect(Collectors.toList());

        if (!Environment.getInstance().isOnWhiteBoxMode() )
            subprograms.removeIf(sub -> sub.getVisibility() != ICPPASTVisibilityLabel.v_public);

        subprograms.removeIf(sub -> sub.isTemplate() && sub.getParent() instanceof ICommonFunctionNode);
        subprograms.removeIf(sub -> sub instanceof StructureNode.DefaultConstructor);

        String typeOfCoverage = Environment.getInstance().getTypeofCoverage();

        String[] typeItems = typeOfCoverage.split("\\+");

        if (subprograms.isEmpty()) {
            Table.Row row = new Table.Row();

            Table.Cell<Text> cell = new Table.Cell<Text>(unit.getName());
            row.getCells().add(cell);

            Table.SpanCell<Text> spanCell = new Table.SpanCell<>(SpecialCharacter.EMPTY, 1 + typeItems.length * 2);
            row.getCells().add(spanCell);

            rows.add(row);
        }

        List<TestCase> allTestCases = new ArrayList<>();

//        ExecutorService es = Executors.newFixedThreadPool(5);
//        List<Callable<Table.Row>> tasks = new ArrayList<Callable<Table.Row>>();

        AtomicInteger index = new AtomicInteger();
        int size = subprograms.size();

        for (ICommonFunctionNode subprogram : subprograms) {
//            Callable<Table.Row> c = new Callable<Table.Row>() {
//                @Override
//                public Table.Row call() throws Exception {
                    logger.debug(String.format("[%d/%d] Calculate metrics in Unit %s / Subprogram %s", index.incrementAndGet(), size, unit.getName(), subprogram.getName()));

                    // get unit name column
                    String unitCol = SpecialCharacter.EMPTY;
                    if (index.get() == 1)
                        unitCol = unit.getName();

                    // get subprogram name column
                    String subprogramCol = getSubprogramDisplayName(subprogram);

                    Table.Row row = new Table.Row(unitCol, subprogramCol);

                    List<TestCase> testCases = getAllTestCaseOf(subprogram);
                    allTestCases.addAll(testCases);

                    List<Table.Cell<Text>> cells = computeCoverage(typeOfCoverage, testCases);
                    row.getCells().addAll(cells);

                    rows.add(row);

//                    return row;
//                }
//            };
//
//            tasks.add(c);
        }

//        List<Future<Table.Row>> futureSubRows = es.invokeAll(tasks);

//        List<Table.Row> subRows = futureSubRows.stream()
//                .map(f -> {
//                    try {
//                        return f.get();
//                    } catch (InterruptedException | ExecutionException e) {
//                        e.printStackTrace();
//                        return null;
//                    }
//                }).filter(Objects::nonNull)
//                .collect(Collectors.toList());
//
//        rows.addAll(subRows);

        Table.Row totalRow = generateEndRow(subprograms.size(), allTestCases, typeItems);

        rows.add(totalRow);

        return rows;
    }

    private List<Table.Cell<Text>> computeCoverage(String typeOfCoverage, List<TestCase> testCases) {
        List<Table.Cell<Text>> cells = new ArrayList<>();

        switch (typeOfCoverage) {
            case EnviroCoverageTypeNode.BRANCH:
            case EnviroCoverageTypeNode.STATEMENT:
            case EnviroCoverageTypeNode.MCDC:
            case EnviroCoverageTypeNode.BASIS_PATH: {
                Table.Cell<Text> coverageCell, functionCoverageCell;

                if (!testCases.isEmpty()) {
                    CoverageDataObject srcCoverageData = CoverageManager
                            .getCoverageOfMultiTestCaseAtSourcecodeFileLevel(testCases, typeOfCoverage);

                    if (srcCoverageData != null) {
                        int visited = srcCoverageData.getVisited();
                        int total = srcCoverageData.getTotal();
                        coverageCell = generateCoverageCell(visited, total);
                    } else {
                        coverageCell = generateCoverageCell(0, 0);
                    }

                    CoverageDataObject funcCoverageData = CoverageManager
                            .getCoverageOfMultiTestCaseAtFunctionLevel(testCases, typeOfCoverage);
                    if (funcCoverageData != null) {
                        functionCoverageCell = generateCoverageCell(funcCoverageData.getVisited(), funcCoverageData.getTotal());
                    } else {
                        functionCoverageCell = generateCoverageCell(0, 0);
                    }

                } else {
                    coverageCell = new Table.Cell<>(SpecialCharacter.EMPTY);
                    functionCoverageCell = new Table.Cell<>(SpecialCharacter.EMPTY);
                }

                cells.add(coverageCell);
                cells.add(functionCoverageCell);

                break;
            }

            case EnviroCoverageTypeNode.STATEMENT_AND_BRANCH:
            case EnviroCoverageTypeNode.STATEMENT_AND_MCDC: {
                Table.Cell<Text>[] coverageCells = new Table.Cell[4];

                String[] typeItems = typeOfCoverage.split("\\+");

                if (!testCases.isEmpty()) {
                    for (int i = 0; i < typeItems.length; i++) {
                        String coverageType = typeItems[i];

                        int srcTotal = 0, srcVisited = 0, funcVisited = 0, funcTotal = 0;

                        CoverageDataObject srcCoverageData = CoverageManager
                                .getCoverageOfMultiTestCaseAtSourcecodeFileLevel(testCases, coverageType);
                        if (srcCoverageData != null) {
                            srcVisited = srcCoverageData.getVisited();
                            srcTotal = srcCoverageData.getTotal();
                        }

                        coverageCells[i*2] = generateCoverageCell(srcVisited, srcTotal);

                        CoverageDataObject funcCoverageData = CoverageManager
                                .getCoverageOfMultiTestCaseAtFunctionLevel(testCases, coverageType);
                        if (funcCoverageData != null) {
                            funcVisited = funcCoverageData.getVisited();
                            funcTotal = funcCoverageData.getTotal();
                        }
                        coverageCells[i*2+1] = generateCoverageCell(funcVisited, funcTotal);
                    }

                } else {
                    for (int i = 0; i < coverageCells.length; i++)
                        coverageCells[i] = new Table.Cell<>(SpecialCharacter.EMPTY);
                }

                cells.addAll(Arrays.asList(coverageCells));

                break;
            }
        }

        return cells;
    }

    private String getBackgroundColor(int visited, int total) {
        String bgColor;
        if (visited == total && visited != 0)
            bgColor = COLOR.GREEN;
        else if (visited != 0)
            bgColor = COLOR.YELLOW;
        else
            bgColor = COLOR.RED;

        return bgColor;
    }

    private Table.Row generateEndRow(int subprograms, List<TestCase> testCases, String[] coverageType) {
        Table.Row endRow = new Table.Row(
                new Text("TOTAL", TEXT_STYLE.BOLD),
                new Text(String.valueOf(subprograms), TEXT_STYLE.BOLD)
        );

        for (String type : coverageType) {
            CoverageDataObject object = CoverageManager
                    .getCoverageOfMultiTestCaseAtSourcecodeFileLevel(testCases, type);

            if (object != null) {
                Table.Cell<Text> statusCell = generateCoverageCell(object.getVisited(), object.getTotal());
                endRow.getCells().add(statusCell);
            } else {
                endRow.getCells().add(new Table.Cell<Text>(""));
            }
            endRow.getCells().add(new Table.Cell<Text>(""));

        }

        return endRow;
    }

    private Table.Cell<Text> generateCoverageCell(int visited, int total) {
        String coverage = String.format("%.2f%% (%d/%d)", (double) visited * 100 / (double) total, visited, total);
        String bgColor = getBackgroundColor(visited, total);

        return new Table.Cell<>(coverage, bgColor);
    }

    private List<TestCase> getAllTestCaseOf(ICommonFunctionNode function) {
        Set<String> testCaseNames = TestCaseManager.getFunctionToTestCasesMap().get(function);
        List<TestCase> testCases = new ArrayList<>();

        if (testCaseNames != null)
            for (String name : testCaseNames) {
                TestCase testCase = TestCaseManager.getBasicTestCaseByNameWithoutData(name);
                if (testCase != null && new File(testCase.getPath()).exists())
                    testCases.add(testCase);
            }

        return testCases;
    }
}
