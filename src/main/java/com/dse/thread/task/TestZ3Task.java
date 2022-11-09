package com.dse.thread.task;

import com.dse.guifx_v3.controllers.object.LoadingPopupController;
import com.dse.guifx_v3.helps.UIController;
import com.dse.thread.AbstractAkaTask;
import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class TestZ3Task extends AbstractAkaTask<TestZ3Task.Result> {

    private final String z3Path;

    public TestZ3Task(String z3Path) {
        this.z3Path = z3Path;

        LoadingPopupController loadingPopup = LoadingPopupController.newInstance("Loading");
        loadingPopup.initOwnerStage(UIController.getPrimaryStage());
        loadingPopup.show();

        setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                LoadingPopupController.getInstance().close();

                TestZ3Task.Result result = getValue();
                if (result.isError()) {
                    UIController.showErrorDialog(result.msg, "Z3 Solver", "Z3 Solver Error");
                } else {
                    UIController.showSuccessDialog("Interaction successfully with Z3 Solver", "Z3 Solver", result.msg);
                }
            }
        });

        setOnCancelled(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                LoadingPopupController.getInstance().close();
            }
        });

        setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                LoadingPopupController.getInstance().close();
            }
        });
    }

    @Override
    protected Result call() throws Exception {
        StringBuilder builder = new StringBuilder();
        boolean isError = false;
        String line;

        Process p = null;
        if (Utils.isWindows()) {
            p = Runtime.getRuntime().exec(
                    new String[]{Utils.doubleNormalizePath(z3Path), "-version"}
            );
        } else if (Utils.isUnix()) {
            p = Runtime.getRuntime().exec(
                    new String[]{"./" + new File(z3Path).getName(), "-version"}
                    , new String[]{},
                    new File(z3Path).getParentFile());
        } else if (Utils.isMac()) {
            p = Runtime.getRuntime().exec(new String[]{z3Path, "-version"});
        }

        p.waitFor();

        // read output
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        while ((line = in.readLine()) != null)
            builder.append(line).append(SpecialCharacter.LINE_BREAK);

        // read errors if exists
        if (p.getErrorStream() != null) {
            BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while ((line = error.readLine()) != null) {
                isError = true;
                builder.append(line).append(SpecialCharacter.LINE_BREAK);
            }
        }

        return new Result(isError, builder.toString());
    }

    public static class Result {

        private final boolean isError;
        private final String msg;

        public Result(boolean isError, String msg) {
            this.isError = isError;
            this.msg = msg;
        }

        public boolean isError() {
            return isError;
        }

        public String getMsg() {
            return msg;
        }
    }
}
