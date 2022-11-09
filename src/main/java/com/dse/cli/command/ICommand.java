package com.dse.cli.command;

import com.dse.logger.CliLogger;
import com.dse.util.Utils;
import picocli.CommandLine;

import java.util.concurrent.Callable;

public interface ICommand<V> extends Callable<V> {
    void parseArguments(String cmd);

    V execute();

    String[] getArgs();

    String getCommandName();

    CommandLine registerConverter(CommandLine cmd);

    static ICommand<?> parse(String cmd) throws IllegalAccessException, InstantiationException {
        ICommand<?> command;

        Class<?>[] classes = Utils.getAllSubClass(AbstractCommand.class);

        for (Class<?> c : classes) {
            command = (ICommand<?>) c.newInstance();
            String commandName = command.getCommandName();

            if (cmd.startsWith(commandName)) {
                command.parseArguments(cmd);
                return command;
            }
        }

        return null;
    }

    void setLogger(CliLogger logger);

    String OPEN = "open";
    String LIST = "list";
    String DELETE_ENV = "delete env";
    String DELETE_TC = "delete tc";
    String RUN = "run";
    String EXECUTE = "execute";
    String REGRESS = "regress";
    String REPORT = "report";
    String QUIT = "quit";
}
