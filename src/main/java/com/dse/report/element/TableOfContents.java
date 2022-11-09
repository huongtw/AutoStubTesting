package com.dse.report.element;

import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TableOfContents extends Section {
    public TableOfContents() {
        super("table-of-contents");
        generateTitle();
    }

    @Override
    public String toHtml() {
        String titleHtml = "";

        for (IElement element : title)
            titleHtml += element.toHtml();

        String bodyHtml = "";

        for (IElement item : body)
            bodyHtml += item.toHtml();

        bodyHtml = String.format("<div class=\"row content bg-light\"><ul>%s</ul></div>", bodyHtml);

        String html = String.format("<section id=\"%s\">%s</section>", id, titleHtml + bodyHtml);

        return html;
    }

    private void generateTitle() {
        Line title = new Line("Table of Contents", COLOR.DARK);
        getTitle().add(title);
    }

    public static class Item implements IElement {
        private List<Item> items = new ArrayList<>();

        private String content;

        private String href;

        public Item(String content, String href) {
            this.content = content;
            this.href = href;
        }

        @Override
        public String toHtml() {
            String onClickEvent = String.format("document.getElementById(\"%s\").scrollIntoView()", href);
            String html = String.format("<li><a href=\"#%s\" onclick=%s>%s</a>", href, onClickEvent, content);

            if (!items.isEmpty()) {
                html += "<ul>";

                for (Item item : items)
                    html += item.toHtml();

                html += "</ul>";
            }

            html += "</li>";

            return html;
        }

        public List<Item> getItems() {
            return items;
        }

        public void setItems(List<Item> items) {
            this.items = items;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getContent() {
            return content;
        }

        public void setHref(String href) {
            this.href = href;
        }

        public String getHref() {
            return href;
        }
    }
}
