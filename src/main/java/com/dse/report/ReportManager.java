package com.dse.report;

import com.dse.config.WorkspaceConfig;
import com.dse.guifx_v3.helps.UILogger;
import com.dse.util.Utils;
import com.dse.logger.AkaLogger;

import java.io.File;

public class ReportManager {
    private final static AkaLogger logger = AkaLogger.get(ReportManager.class);

    public static void export(IReport report) {
        Utils.writeContentToFile(report.toHtml(), report.getPath());
        UILogger.getUiLogger().log("Report path = " + report.getPath());
        logger.debug("Report path = " + report.getPath());
    }

    public static String readContentFromFile(String name) {
        String filePath = new WorkspaceConfig().fromJson().getFullReportDirectory() + File.separator + name + ".html";

        return Utils.readFileContent(filePath);
    }
}
