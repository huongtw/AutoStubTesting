package com.dse.report.element;

import java.util.ArrayList;
import java.util.List;

public class Section implements IElement {
    protected String id;

    protected List<IElement> title = new ArrayList<>();

    protected List<IElement> body = new ArrayList<>();

    public Section(String id) {
        this.id = id;
    }

    @Override
    public String toHtml() {
        String titleHtml = "";

        for (IElement element : title)
            titleHtml += element.toHtml();

        titleHtml = String.format("<div class=\"title\">%s</div>", titleHtml);

        String bodyHtml = "";

        for (IElement element : body)
            bodyHtml += element.toHtml();

        bodyHtml = String.format("<div class=\"content\">%s</div>", bodyHtml);

        String html = String.format("<section id=\"%s\">%s</section>", id, titleHtml + bodyHtml);

        return html;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setBody(List<IElement> body) {
        this.body = body;
    }

    public List<IElement> getBody() {
        return body;
    }

    public List<IElement> getTitle() {
        return title;
    }

    public void setTitle(List<IElement> title) {
        this.title = title;
    }

    public static class Line extends AbstractLine {
        protected Text content;

//        protected String style = TEXT_STYLE.NORMAL;

        protected String align = TEXT_ALIGN.LEFT;

        public Line(Text content, String color) {
            this.content = content;
            this.color = color;
            if (color == COLOR.DARK)
                this.content.setColor("white");
        }

        public Line(String content, String color) {
            this.content = new Text(content);
            this.color = color;
            if (color == COLOR.DARK)
                this.content.setColor("white");
        }

        @Override
        public String toHtml() {
            String prefix = "row"
                    + (color.isEmpty() ? color : " " + color)
//                    + (style.isEmpty() ? style : " " + style)
                    + (align.isEmpty() ? align : " " + align);

            String html = String.format("<div class=\"%s\">%s</div>", prefix, content.toHtml());

            return html;
        }

        public Text getContent() {
            return content;
        }
    }

    public static class CenteredLine extends Line {

        public CenteredLine(Text content, String color) {
            super(content, color);
            this.align = TEXT_ALIGN.CENTER;
        }

        public CenteredLine(String content, String color) {
            super(content, color);
            this.align = TEXT_ALIGN.CENTER;
        }

    }

    public static class BlankLine extends AbstractLine {
        public BlankLine() {

        }

        public BlankLine(String color) {
            this.color = color;
        }

        @Override
        public String toHtml() {
            String prefix = "row blank" + (color.isEmpty() ? color : " " + color);

            return String.format("<div class=\"%s\"></div>", prefix);
        }
    }


    public abstract static class AbstractLine implements IElement {
        protected String color = COLOR.WHITE;

        protected boolean marginTop, marginBottom;

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }
    }
}
