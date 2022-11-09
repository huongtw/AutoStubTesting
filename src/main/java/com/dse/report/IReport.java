package com.dse.report;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

public interface IReport {
//    String STYLES_PATH = "local/report-templates/style.css";

//    String TEST_CASE_DATA_REPORT_TEMPLATE_PATH = "local/report-templates/test_case_report.html";

    String getName();

    void setName(String name);

    String getCreationDate();

    String getCreationTime();

    void setCreationDateTime(LocalDateTime creationTime);

    String toHtml();

//    void setHtmlContent(String htmlContent);

//    static IReport fromHtml() {
//        return null;
//    }

    String getPath();

    void setPath(String path);
}
