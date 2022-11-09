package com.dse.report.element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Table implements IElement {
    protected List<IElement> rows = new ArrayList<>();

    protected boolean fixed = true;

    public Table() {

    }

    public Table(boolean fixed) {
        this.fixed = fixed;
    }

    @Override
    public String toHtml() {
        String html = "";

        for (IElement row : rows)
            html += row.toHtml();

        String prefix = fixed ? "class=\"fixed-layout\"" : "";

        html = String.format("<table %s>%s</table>", prefix, html);

        return html;
    }

    public List<IElement> getRows() {
        return rows;
    }

    public void setRows(List<IElement> rows) {
        this.rows = rows;
    }

    public static class Row implements IElement {
        protected List<Cell> cells = new ArrayList<>();

        public Row() {}

        public Row(String... cells) {
            for (String cell : cells)
                this.cells.add(new Cell<Text>(cell));
        }

        public Row(Text... cells) {
            for (Text cell : cells)
                this.cells.add(new Cell<>(cell));
        }

        public Row(Cell... cells) {
            this.cells = Arrays.asList(cells);
        }

        @Override
        public String toHtml() {
            String html = "";

            for (Cell cell : cells)
                html += cell.toHtml();

            html = String.format("<tr>%s</tr>", html);

            return html;
        }

        public void setCells(List<Cell> cells) {
            this.cells = cells;
        }

        public List<Cell> getCells() {
            return cells;
        }
    }

    public static class HeaderRow extends Row {

        public HeaderRow(String... cells) {
            super(cells);

            for (Cell cell : this.cells)
                cell.setColor(COLOR.MEDIUM);
        }

        @Override
        public String toHtml() {
            String html = "";

            for (Cell cell : cells)
                html += cell.toHtml();

            html = String.format("<tr class=\"bold\">%s</tr>", html);

            return html;
        }
    }

    public static class BlankRow extends Row {
        private int cols;

        private String color = COLOR.LIGHT;

        public BlankRow(int cols) {
            super();
            this.cols = cols;
        }

        public BlankRow(int cols, String color) {
            super();
            this.cols = cols;
            this.color = color;
        }

        @Override
        public String toHtml() {
            String html = "";

            for (int i = 0; i < cols; i++)
                html += "<td></td>";

            html = String.format("<tr class=\"%s\">%s</tr>", color, html);

            return html;
        }

        public void setCols(int cols) {
            this.cols = cols;
        }

        public int getCols() {
            return cols;
        }
    }

    public static class SpanCell<T extends IElement> extends Cell<T> {
        private int colspan = 1;

        public SpanCell(String text, int colspan) {
            super(text);
            this.colspan = colspan;
        }

        public SpanCell(String text) {
            super(text);
        }

        public SpanCell(T content) {
            super(content);
        }

        public SpanCell(T content, String color) {
            super(content, color);
        }

        public SpanCell(String content, String color) {
            super(content, color);
        }

        public SpanCell(String content, String color, int colspan) {
            super(content, color);
            this.colspan = colspan;
        }

        @Override
        public String toHtml() {
            String origin = super.toHtml();

            String colspan = String.format("<td colspan=\"%d\"", this.colspan);

            return origin.replace("<td", colspan);
        }

        public int getColspan() {
            return colspan;
        }

        public void setColspan(int colspan) {
            this.colspan = colspan;
        }
    }

    public static class Cell<T extends IElement> implements IElement {
        private T content;

        private String color = COLOR.LIGHT;

        private String align = TEXT_ALIGN.LEFT;

        public Cell(String text) {
            this.content = (T) new Text(text);
        }

        public Cell(T content) {
            this.content = content;
        }

        public Cell(T content, String color) {
            this.content = content;
            this.color = color;
        }

        public Cell(String content, String color) {
            this.content = (T) new Text(content);
            this.color = color;
        }

        @Override
        public String toHtml() {
            String prefix = color + (align.isEmpty() ? align : " " + align);

            return String.format("<td class=\"%s\">%s</td>", prefix, content.toHtml());
        }

        public void setColor(String color) {
            this.color = color;
        }

        public void setAlign(String align) {
            this.align = align;
        }

        public void setContent(T content) {
            this.content = content;
        }

        public String getColor() {
            return color;
        }

        public T getContent() {
            return content;
        }
    }
}
