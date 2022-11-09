package com.dse.report.element;

public class CodeView implements IElement {
    private String html;

    public CodeView() {

    }

    public CodeView(String html) {
        setHtml(html);
    }

    public void setContent(String content) {
        content = content.replace("&", "&amp;")
                .replace("'", "&#39;")
                .replace("\"", "&quot;")
                .replace(">", "&gt;")
                .replace("<", "&lt;");

        html = String.format("<pre>%s</pre>", content);
    }

    public void setHtml(String html) {
        this.html = html;
    }

    @Override
    public String toHtml() {
        return String.format("<div class=\"code-view\">%s</div>", html);
    }
}
