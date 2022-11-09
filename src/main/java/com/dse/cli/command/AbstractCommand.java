package com.dse.cli.command;

import com.dse.logger.CliLogger;
import com.dse.util.SpecialCharacter;
import picocli.CommandLine;

public abstract class AbstractCommand<V> implements ICommand<V> {
    protected CliLogger logger;

    protected String[] args = new String[0];

    public AbstractCommand() {
            logger = CliLogger.get(getClass());
    }

    @Override
    public V execute() {
        CommandLine commandLine = generateCommand();

        int exitCode = commandLine.execute(args);

        if (exitCode == CommandLine.ExitCode.OK)
            return commandLine.getExecutionResult();

        return null;
    }

    private CommandLine generateCommand() {
        CommandLine commandLine = new CommandLine(this);
        commandLine = registerConverter(commandLine);

        return commandLine;
    }

    @Override
    public void parseArguments(String cmd) {
        // remove space between equal symbol
        String normalize = cmd.replaceAll("\\s*=\\s*", "=");

        String argsString = normalize
                .replace(getCommandName(), SpecialCharacter.EMPTY)
                .trim();

        if (!argsString.isEmpty())
            args = argsString.split("\\s+");
    }

    public String getCommandName() {
        CommandLine commandLine = generateCommand();

        return commandLine.getCommandName();
    }

    @Override
    public String[] getArgs() {
        return args;
    }

    @Override
    public CommandLine registerConverter(CommandLine cmd) {
        return cmd;
    }

    public void setLogger(CliLogger logger) {
        this.logger = logger;
    }

}
