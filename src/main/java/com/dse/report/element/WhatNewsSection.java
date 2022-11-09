package com.dse.report.element;

import java.util.ArrayList;
import java.util.List;

public class WhatNewsSection extends Section {

    public WhatNewsSection() {
        super("what-news");
        generateTitle();
    }

    private void generateTitle() {
        Line title = new Line("What News", COLOR.DARK);
        getTitle().add(title);
    }

    @Override
    public String toHtml() {
        String titleHtml = "";

        for (IElement element : title)
            titleHtml += element.toHtml();

        String bodyHtml = "";

        for (IElement item : body)
            bodyHtml += item.toHtml();

        bodyHtml = String.format("<div class=\"row content bg-white\"><ul>%s</ul></div>", bodyHtml);

        String html = String.format("<section id=\"%s\">%s</section>", id, titleHtml + bodyHtml);

        return html;
    }

    public static class Item implements IElement {

        private String content;

        public Item(String content) {
            this.content = content;
        }

        @Override
        public String toHtml() {
            return String.format("<li>%s</li>", content);
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getContent() {
            return content;
        }
    }

}
