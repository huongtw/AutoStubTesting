package com.dse.cli.command;

import picocli.CommandLine.Option;
import picocli.CommandLine.Command;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.dse.cli.command.ICommand.EXECUTE;


@Command(name = EXECUTE,
        mixinStandardHelpOptions = true,
        version = "1.0.0",
        description = "Execute scripts in current environment.")

public class Execute extends AbstractCommand<String[]> {
    @Option(names = {"-p", "--path"}, paramLabel = "<path>",
            required = true, arity = "1",
            description = "The path to the script.")
    private String path;

    public Execute() {
        super();
    }

    @Override
    public String[] call() throws Exception {
        File scriptFile = new File(path);
        if (scriptFile.isFile()) {
            logger.info("Start execute regression script " + scriptFile.getName());
            List<String> commands = readData(path);
            for (String cmd : commands) {
                try {
                    logger.info(cmd);
                    ICommand<?> command = ICommand.parse(cmd);
                    if (command != null) {
                        command.execute();
                    }
                    logger.info("");

                } catch (Exception ex) {
                    ex.printStackTrace();
                    logger.error(ex.getMessage());
                }
            }
            logger.info("Finish");

        } else {
            logger.error("File not found: " + path);
        }
        return new String[0];
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
