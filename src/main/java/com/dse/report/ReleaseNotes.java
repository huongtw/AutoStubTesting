package com.dse.report;

import com.dse.config.WorkspaceConfig;
import com.dse.logger.AkaLogger;
import com.dse.report.element.*;
import com.dse.report.element.Table;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ReleaseNotes extends ReportView {

    public static final String EXCEL_PATH = "/help/ReleaseNotes.xlsx";

    private static final AkaLogger logger = AkaLogger.get(ReleaseNotes.class);

    private static final String STATE_WHAT_NEWS = "What News?";
    private static final String STATE_RECENT_CHANGES = "Recent Changes";
    private static final String STATE_KNOWN_ISSUES = "Known Issues";
    private static final String STATE_NOTES = "Notes";

    private final List<Cell> whatNews = new ArrayList<>();
    private final List<Cell> recentChanges = new ArrayList<>();
    private final List<Cell> knownIssues = new ArrayList<>();
    private final List<Cell> notes = new ArrayList<>();

    private final Workbook workbook;
    private final int index;

    public ReleaseNotes(Workbook workbook, int index) {
        // set report name
        super("Release Notes");

        this.workbook = workbook;
        this.index = index;

        parseSheet(workbook.getSheetAt(index));

        // generate test case report
        generate();
    }

    public Workbook getWorkbook() {
        return workbook;
    }

    public int getIndex() {
        return index;
    }

    @Override
    protected void generate() {
        // STEP 1: generate table of contents section
        sections.add(generateTableOfContents());

        // STEP 2: generate configuration data section
        sections.add(generateConfigurationData());
        sections.add(new Section.BlankLine());

        // STEP 3: generate what news section
        if (!whatNews.isEmpty()) {
            logger.debug("Generate what news section");
            sections.add(generateWhatNews());
        }

        // STEP 4: generate resolved issues section
        if (!recentChanges.isEmpty()) {
            logger.debug("Generate recent changes section");
            sections.add(generateResolvedSection());
            sections.add(new Section.BlankLine());
        }

        // STEP 5: generate known issues section
        if (!knownIssues.isEmpty()) {
            logger.debug("Generate known issues section");
            sections.add(generateKnownSection());
            sections.add(new Section.BlankLine());
        }

        if (!notes.isEmpty()) {
            logger.debug("Generate notes section");
            sections.add(generateNotesSection());
        }

        if (workbook.getNumberOfSheets() > 1) {
            sections.add(new Section.BlankLine());
            sections.add(generateNavigator());
        }
    }

    private void parseSheet(Sheet sheet) {
        String state = null;

        // Get all rows
        Iterator<Row> rowIterator = sheet.iterator();
        while (rowIterator.hasNext()) {
            Row nextRow = rowIterator.next();

            // Get all cells
            Iterator<Cell> cellIterator = nextRow.cellIterator();

            // Read cells and set value for book object
            while (cellIterator.hasNext()) {
                // Read cell
                Cell cell = cellIterator.next();
                String cellValue = ExcelReader.getCellValue(cell);

                if (cellValue == null || cellValue.isEmpty()) {
                    continue;
                }

                if (cellValue.equals(STATE_NOTES) || cellValue.equals(STATE_KNOWN_ISSUES)
                    || cellValue.equals(STATE_RECENT_CHANGES) || cellValue.equals(STATE_WHAT_NEWS)) {
                    state = cellValue;
                    break;
                } else if (state != null) {
                    switch (state) {
                        case STATE_WHAT_NEWS: {
                            whatNews.add(cell);
                            break;
                        }

                        case STATE_RECENT_CHANGES: {
                            recentChanges.add(cell);
                            break;
                        }

                        case STATE_KNOWN_ISSUES: {
                            knownIssues.add(cell);
                            break;
                        }

                        case STATE_NOTES: {
                            notes.add(cell);
                            break;
                        }
                    }
                }

            }
        }
    }

    private IElement generateNotesSection() {
        Section section = new Section("notes");
        section.getTitle().add(new Section.Line("Notes", COLOR.DARK));
        for (Cell cell : notes) {
            String content = ExcelReader.getCellValue(cell);
            if (content != null) {
                section.getTitle().add(new Section.Line(content, COLOR.WHITE));
            }
        }
        return section;
    }

    private Section generateResolvedSection() {
        Section section = new Section("recent-changes");
        section.getTitle().add(new Section.Line("Recent Changes", COLOR.DARK));

        Table table = toTable(recentChanges);
        section.getBody().add(table);

        return section;
    }

    private Table toTable(List<Cell> cells) {
        Table table = new Table(false);

        int prevRowIndex = -1;
        Table.Row row = null;
        for (Cell cell : cells) {
            int rowIndex = cell.getRowIndex();
            if (rowIndex != prevRowIndex) {
                if (row != null)
                    table.getRows().add(row);
                row = new Table.Row();
            }
            Table.Cell<Text> textCell = ExcelReader.toReportCell(cell);
            if (row != null)
                row.getCells().add(textCell);
            prevRowIndex = rowIndex;
        }

        if (row != null && !table.getRows().contains(row)) {
            table.getRows().add(row);
        }

        return table;
    }

    private Section generateKnownSection() {
        Section section = new Section("known-issues");
        section.getTitle().add(new Section.Line("Known Issues", COLOR.DARK));

        Table table = toTable(knownIssues);
        section.getBody().add(table);

        return section;
    }

    private WhatNewsSection generateWhatNews() {
        WhatNewsSection whatNewsSection = new WhatNewsSection();
        for (Cell cell : whatNews) {
            String content = ExcelReader.getCellValue(cell);
            if (content.startsWith("-"))
                content = content.substring(1).trim();
            whatNewsSection.getBody().add(new WhatNewsSection.Item(content));
        }
        return whatNewsSection;
    }

    @Override
    protected void setPathDefault() {
        this.path = new WorkspaceConfig().fromJson().getReportDirectory()
                + File.separator + "release-notes.html";
    }

    @Override
    protected TableOfContents generateTableOfContents() {
        TableOfContents tableOfContents = new TableOfContents();

        tableOfContents.getBody().add(new TableOfContents.Item("Configuration Data", "config-data"));
        if (!whatNews.isEmpty())
            tableOfContents.getBody().add(new TableOfContents.Item("What News", "what-news"));
        if (!recentChanges.isEmpty())
            tableOfContents.getBody().add(new TableOfContents.Item("Recent Changes", "recent-changes"));
        if (!knownIssues.isEmpty())
            tableOfContents.getBody().add(new TableOfContents.Item("Known Issues", "known-issues"));
        if (!notes.isEmpty())
            tableOfContents.getBody().add(new TableOfContents.Item("Notes", "notes"));

        return tableOfContents;
    }

    @Override
    protected Section generateConfigurationData() {
        Section section = new Section("config-data");

        section.getTitle().add(new Section.Line("Configuration Data", COLOR.DARK));

        Sheet sheet = workbook.getSheetAt(index);
        String versionId = sheet.getSheetName();
        String releaseDate = "?? ??? ????";
        Comment comment = sheet.getCellComments().values()
                .stream()
                .findFirst()
                .orElse(null);
        if (comment != null)
            releaseDate = comment.getString().getString();

        Table table = new Table();
        table.getRows().add(new Table.Row("Version: ", versionId));
        table.getRows().add(new Table.Row("Release Date:", releaseDate));
        section.getBody().add(table);

        return section;
    }

    private IElement generateNavigator() {
        int max = workbook.getNumberOfSheets();
        Table table = new Table();
        Table.Row row = new Table.Row();

        if (index > 0) {
            String newerId = workbook.getSheetAt(index - 1).getSheetName();
            Text backText = new Text("← " + newerId);
            backText.setHref("#newer");
            Table.Cell<Text> backCell = new Table.Cell<>(backText);
            row.getCells().add(backCell);
        } else {
            row.getCells().add(new Table.Cell<>(""));
        }

        if (index < max - 1) {
            String prevId = workbook.getSheetAt(index + 1).getSheetName();
            Text prevText = new Text(prevId + " →");
            prevText.setHref("#prev");
            Table.Cell<Text> prevCell = new Table.Cell<>(prevText);
            prevCell.setAlign(TEXT_ALIGN.RIGHT);
            row.getCells().add(prevCell);
        } else {
            row.getCells().add(new Table.Cell<>(""));
        }

        table.getRows().add(row);
        return table;
    }
}
