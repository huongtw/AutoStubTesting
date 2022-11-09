package com.dse.thread.task;

import com.dse.config.AkaConfig;
import com.dse.exception.OpenFileException;
import com.dse.thread.AbstractAkaTask;
import com.dse.util.Utils;

public class OpenWorkspaceTask extends AbstractAkaTask<Boolean> {

    @Override
    protected Boolean call() {
        String envPath = new AkaConfig().fromJson().getOpeningWorkspaceDirectory();
        try {
            Utils.openFolderorFileOnExplorer(envPath);
            return true;
        } catch (OpenFileException e) {
            e.printStackTrace();
            return false;
        }
    }

}
