package com.dse.report.element;

public class ConfigurationData extends Section {

    private String creationDate, creationTime;

    private TestCaseTable table;

    public ConfigurationData() {
        super("config-data");
    }

    public void generate() {
        generateTitle();
        generateBody();
    }

    private void generateTitle() {
        title.add(new Line("Configuration Data", COLOR.DARK));
    }

    private void generateBody() {
        body.add(new Line(new Text("This Report includes data for:", TEXT_STYLE.BOLD), COLOR.WHITE));

        body.add(table);

        body.add(new BlankLine());

        Table creationTable = new Table();
        creationTable.getRows().add(new Table.Row("Date of Report Creation:", creationDate));
        creationTable.getRows().add(new Table.Row("Time of Report Creation:", creationTime));
        body.add(creationTable);

        body.add(new BlankLine());
    }

//    @Override
//    public String toHtml() {
//        return TEMPLATE.replace(INSERT_TEST_CASE_TABLE_TAG, table.toHtml())
//                .replace(INSERT_REPORT_CREATION_DATE_TAG, creationDate)
//                .replace(INSERT_REPORT_CREATION_TIME_TAG, creationTime);
//    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }

    public TestCaseTable getTable() {
        return table;
    }

    public void setTable(TestCaseTable table) {
        this.table = table;
    }
}
