package com.dse.logger;

import com.dse.guifx_v3.controllers.main_view.BaseSceneController;
import com.dse.guifx_v3.controllers.object.LoadingPopupController;
import com.dse.regression.controllers.MessagesPaneTabContentController;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

import java.io.IOException;

public class AkaLogger extends Logger {

    protected AkaLogger(String name) {
        super(name);
    }

    public static AkaLogger get(Class<?> c) {
        Logger root = Logger.getRootLogger();

        AkaLogger logger = new AkaLogger(c.getName());

        logger.repository = root.getLoggerRepository();
        logger.parent = root;

        // disable stdout target logger if using command line version
//        if (UIController.getPrimaryStage() == null) {
//            root.removeAppender("stdout");
//        }

        return logger;
    }

    private static final String FQCN = AkaLogger.class.getName();

    @Override
    public void debug(Object message) {
        if (LoadingPopupController.getInstance() != null)
            LoadingPopupController.getInstance().setText(message);

        if (MessagesPaneTabContentController.newInstance("Log") != null)
            MessagesPaneTabContentController.newInstance("Log").appendLog(message);

        message = "[" + Thread.currentThread().getName() + "] " + message;
        if (!this.repository.isDisabled(10000)) {
            if (Level.DEBUG.isGreaterOrEqual(this.getEffectiveLevel())) {
                this.forcedLog(FQCN, Level.DEBUG, message, null);
            }
        }
    }

    @Override
    public void error(Object message) {
        error(message, null);
    }

    @Override
    public void error(Object message, Throwable t) {
        if (MessagesPaneTabContentController.newInstance("Log") != null)
            MessagesPaneTabContentController.newInstance("Log").appendLog(message);

        message = "[" + Thread.currentThread().getName() + "] " + message;
        if (!this.repository.isDisabled(40000)) {
            if (Level.ERROR.isGreaterOrEqual(this.getEffectiveLevel())) {
                this.forcedLog(FQCN, Level.ERROR, message, t);
            }
        }
    }

    @Override
    public void info(Object message) {
        if (MessagesPaneTabContentController.newInstance("Log") != null)
            MessagesPaneTabContentController.newInstance("Log").appendLog(message);

        message = "[" + Thread.currentThread().getName() + "] " + message;
        if (!this.repository.isDisabled(20000)) {
            if (Level.INFO.isGreaterOrEqual(this.getEffectiveLevel())) {
                this.forcedLog(FQCN, Level.INFO, message, null);
            }
        }
    }

    public void setAppender(String fileName) {
        Logger root = Logger.getRootLogger();
        root.removeAppender("workspace");
        try {
            RollingFileAppender appender = new RollingFileAppender(
                    new PatternLayout("%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n"), fileName);
            appender.setName("workspace");
            appender.setMaxBackupIndex(5);
            root.addAppender(appender);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
