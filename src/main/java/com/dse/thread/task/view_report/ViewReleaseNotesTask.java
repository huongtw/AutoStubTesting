package com.dse.thread.task.view_report;

import com.dse.guifx_v3.controllers.main_view.MDIWindowController;
import com.dse.guifx_v3.controllers.object.LoadingPopupController;
import com.dse.guifx_v3.helps.UIController;
import com.dse.logger.AkaLogger;
import com.dse.report.ExcelReader;
import com.dse.report.ReleaseNotes;
import com.dse.thread.AbstractAkaTask;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import org.apache.poi.ss.usermodel.Workbook;

public class ViewReleaseNotesTask extends AbstractAkaTask<ReleaseNotes> {

    private static final AkaLogger logger = AkaLogger.get(ViewReleaseNotesTask.class);

    private Workbook workbook;
    private int index = 0;

    public ViewReleaseNotesTask() {
        LoadingPopupController loadingPopup = LoadingPopupController.newInstance("Generate Report");
        loadingPopup.initOwnerStage(UIController.getPrimaryStage());
        loadingPopup.show();

        setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                loadingPopup.close();
                MDIWindowController.getMDIWindowController().viewReleaseNotes(getValue());
            }
        });

        setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                loadingPopup.close();
                UIController.showErrorDialog(getException().getMessage(),
                        "Fail", "Test case management report generation");
            }
        });
    }

    public ViewReleaseNotesTask(Workbook workbook, int index) {
        this();
        this.workbook = workbook;
        this.index = index;
    }

    @Override
    protected ReleaseNotes call() throws Exception {
        if (workbook == null) {
            logger.debug("Load release notes data");
            workbook = ExcelReader.readExcel(ReleaseNotes.EXCEL_PATH);
        }

        return new ReleaseNotes(workbook, index);
    }
}
