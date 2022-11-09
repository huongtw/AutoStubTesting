package com.dse.thread.task.view_report;

import com.dse.guifx_v3.helps.UIController;
import com.dse.guifx_v3.helps.UILogger;
import com.dse.report.IReport;
import com.dse.report.ReportManager;
import com.dse.report.TestCaseManagementReport;
import com.dse.testcasescript.object.ITestcaseNode;
import javafx.scene.control.Alert;

import java.time.LocalDateTime;
import java.util.List;

public class ViewManagementReportTask extends AbstractViewReportTask {

    public ViewManagementReportTask(List<ITestcaseNode> nodes) {
        super(nodes);
    }

    @Override
    protected void onSuccess(IReport report) {
        UIController.showDetailDialog(Alert.AlertType.INFORMATION, "Test case management report generation",
                "Success", "The test case management report has been exported at \n" + report.getPath());
        UILogger.getUiLogger().log("The test case management report has been exported at \n" + report.getPath());
    }

    @Override
    protected void onException(Exception ex) {
        UIController.showErrorDialog(ex.getMessage(),
                "Fail", "Test case management report generation");
    }

    @Override
    protected IReport call() throws Exception {
        IReport report = new TestCaseManagementReport(nodes, LocalDateTime.now());
        ReportManager.export(report);

        return report;
    }
}
