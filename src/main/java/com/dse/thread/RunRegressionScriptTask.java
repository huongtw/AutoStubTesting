package com.dse.thread;

import com.dse.cli.command.ICommand;
import com.dse.guifx_v3.controllers.main_view.BaseSceneController;
import com.dse.regression.controllers.MessagesPaneTabContentController;
import com.dse.regression.objects.RegressionScript;
import com.dse.logger.MessagesPaneLogger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RunRegressionScriptTask extends AbstractAkaTask<Void> {

    private final RegressionScript regressionScript;

    public RunRegressionScriptTask(RegressionScript regressionScript) {
        this.regressionScript = regressionScript;
    }

    @Override
    protected Void call() throws Exception {
        MessagesPaneTabContentController controller = MessagesPaneTabContentController.newInstance(regressionScript.getName());

        if (controller != null) {
            MessagesPaneLogger logger = controller.getLogger();

            logger.info("Start run regression script " + regressionScript.getName());
            List<String> commands = readData(regressionScript.getScriptFilePath());
            for (String cmd : commands) {
                try {
                    logger.info(cmd);
                    ICommand<?> command = ICommand.parse(cmd);
                    if (command != null) {
                        command.setLogger(controller.getLogger());
                        command.execute();
                    }
                    logger.info("");

                } catch (Exception ex) {
                    ex.printStackTrace();
                    logger.error(ex.getMessage());
                }
            }
            logger.info("Finish");
        }
        return null;
    }

    /**
     * Read data from file path
     *
     * @param path path to file
     * @return data in string
     */
    private static List<String> readData(String path) {
        List<String> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {

            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                data.add(sCurrentLine);
            }

        } catch (IOException e) {
            // // e.printStackTrace();
        }
        return data;
    }
}
