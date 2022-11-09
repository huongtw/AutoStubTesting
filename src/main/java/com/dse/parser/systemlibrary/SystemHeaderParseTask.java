package com.dse.parser.systemlibrary;

import com.dse.config.WorkspaceConfig;
import com.dse.parser.object.HeaderNode;
import com.dse.parser.object.INode;
import com.dse.thread.AbstractAkaTask;
import com.dse.logger.AkaLogger;

import java.io.File;

public class SystemHeaderParseTask extends AbstractAkaTask<HeaderNode> {
    private static final AkaLogger logger = AkaLogger.get(SystemHeaderParseTask.class);

    private String path;
    private INode root;

    private static final String WD = new WorkspaceConfig().fromJson().getHeaderPreprocessorDirectory();

    public SystemHeaderParseTask(INode root, String path) {
        this.path = path;
        this.root = root;
    }

    @Override
    protected HeaderNode call() {
        SystemHeaderGenerator generator = new SystemHeaderGenerator(WD, path);
        String sourcePath = generator.generate();

        if (sourcePath == null || !new File(sourcePath).exists())
            return null;

        try {
            SystemHeaderParser parser = new SystemHeaderParser(sourcePath, path);
            HeaderNode headerNode = parser.parse();

            root.getChildren().add(headerNode);
            logger.debug(headerNode + " done");

            return headerNode;

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Fail to parse " + path + ": " + e.getMessage());
            return null;
        }
    }
}
