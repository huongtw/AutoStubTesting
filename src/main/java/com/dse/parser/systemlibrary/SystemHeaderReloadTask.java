package com.dse.parser.systemlibrary;

import com.dse.config.WorkspaceConfig;
import com.dse.parser.object.HeaderNode;
import com.dse.parser.object.INode;
import com.dse.thread.AbstractAkaTask;
import com.dse.logger.AkaLogger;

import java.io.File;

public class SystemHeaderReloadTask extends AbstractAkaTask<HeaderNode> {
    private static final AkaLogger logger = AkaLogger.get(SystemHeaderReloadTask.class);

    private String path;
    private INode root;

    private static final String WD = new WorkspaceConfig().fromJson().getHeaderPreprocessorDirectory();

    public SystemHeaderReloadTask(INode root, String path) {
        this.path = path;
        this.root = root;
    }

    private static final String OUT_EXTENSION = ".i";

    @Override
    protected HeaderNode call() {
        String fileOut = WD + File.separator + new File(path).getName() + OUT_EXTENSION;

        if (!new File(fileOut).exists()) {
            logger.error(fileOut + " does not exits");
            return null;
        }

        try {
            SystemHeaderParser parser = new SystemHeaderParser(fileOut, path);
            HeaderNode headerNode = parser.parse();

            root.getChildren().add(headerNode);
            logger.debug(headerNode + " done");

            return headerNode;

        } catch (Exception e) {
            logger.error("Fail to parse " + this.path + ": " + e.getMessage());
            return null;
        }
    }
}
