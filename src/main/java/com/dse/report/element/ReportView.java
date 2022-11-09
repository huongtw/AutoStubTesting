package com.dse.report.element;

import com.dse.report.IReport;
import com.dse.testcase_manager.CompoundTestCase;
import com.dse.testcase_manager.TestCase;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.testcase_manager.TestCaseSlot;
import com.dse.util.DateTimeUtils;
import com.dse.util.Utils;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class ReportView implements IElement, IReport {
    protected String name;
    protected LocalDateTime creationDateTime;
    protected String path;

    protected List<IElement> sections = new ArrayList<>();

    public ReportView(String name) {
        this.name = name;
    }

    @Override
    public String toHtml() {
        String body = "";

        for (IElement section : sections)
            body += section.toHtml();

        String html = String.format("<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                    "<title>%s</title>" +
                    "<link rel=\"stylesheet\" type=\"text/css\" href=\"%s\">" +
                "</head>" +
                "<body>" +
                    "<div class=\"report-view\">%s</div>" +
                "</body>" +
                "</html>", name, new File(STYLES_PATH).getAbsolutePath(), body);

        return html;
    }

    protected abstract void generate();

    protected abstract void setPathDefault();

    protected abstract TableOfContents generateTableOfContents();

    protected abstract Section generateConfigurationData();

    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public List<IElement> getSections() {
        return sections;
    }

    public void setSections(List<IElement> sections) {
        this.sections = sections;
    }

    public String getCreationDate() {
        return DateTimeUtils.getDate(creationDateTime);
    }

    public String getCreationTime() {
        return DateTimeUtils.getTime(creationDateTime);
    }

    public void setCreationDateTime(LocalDateTime creationDateTime) {
        this.creationDateTime = creationDateTime;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
