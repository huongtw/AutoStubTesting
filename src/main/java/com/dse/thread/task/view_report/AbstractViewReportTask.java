package com.dse.thread.task.view_report;

import com.dse.guifx_v3.controllers.main_view.MDIWindowController;
import com.dse.guifx_v3.controllers.object.LoadingPopupController;
import com.dse.environment.Environment;
import com.dse.guifx_v3.helps.UIController;
import com.dse.report.IReport;
import com.dse.testcasescript.object.ITestcaseNode;
import com.dse.thread.AbstractAkaTask;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

import java.util.List;
import java.util.concurrent.ExecutionException;

public abstract class AbstractViewReportTask extends AbstractAkaTask<IReport> {

    protected final List<ITestcaseNode> nodes;

    protected AbstractViewReportTask(List<ITestcaseNode> nodes) {
        this.nodes = nodes;

        LoadingPopupController loadingPopup = LoadingPopupController.newInstance("Generate Report");
        loadingPopup.initOwnerStage(UIController.getPrimaryStage());
        loadingPopup.show();

        setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                loadingPopup.close();

                try {
                    IReport report = get();

                    onSuccess(report);

                    // display on MDIWindow
                    MDIWindowController.getMDIWindowController().viewReport(report.getName(), report.toHtml());
                } catch (InterruptedException | ExecutionException e) {
                    onException(e);
                }
            }
        });
    }

    protected abstract void onSuccess(IReport report);

    protected abstract void onException(Exception ex);
}
