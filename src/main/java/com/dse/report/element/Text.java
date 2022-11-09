package com.dse.report.element;

public class Text implements IElement {
    private String value;

    private String style = TEXT_STYLE.NORMAL;

    private String href;

    private String id;

    private String color = "black";

    public Text(String value) {
        this.value = value;
    }

    public Text(String value, String style) {
        this.value = value;
        this.style = style;
    }

    public Text(String value, String style, String color) {
        this.value = value;
        this.style = style;
        this.color = color;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public String toHtml() {
        if (this.value != null) {
            String value = this.value.replace("<", "&lt;").replace(">", "&gt;");

            if (id != null && href != null)
                return String.format("<a href=\"%s\" id=\"%s\" class=\"%s\" sstyle=\"color: %s;\">%s</a>", href, id, style, color, value);
            else if (id != null)
                return String.format("<span id=\"%s\" class=\"%s\" style=\"color: %s;\">%s</span>", id, style, color, value);
            else if (href != null)
                return String.format("<a href=\"%s\" class=\"%s\" style=\"color: %s;\">%s</a>", href, style, color, value);
            else
                return String.format("<span class=\"%s\" style=\"color: %s;\">%s</span>", style, color, value);
        } else
            return "";
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getHref() {
        return href;
    }

    public void set(String value) {
        this.value = value;
    }

    public String get() {
        return value;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
