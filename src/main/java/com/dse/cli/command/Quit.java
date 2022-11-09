package com.dse.cli.command;

import com.dse.cli.Main;
import picocli.CommandLine.*;

import static com.dse.cli.command.ICommand.QUIT;

@Command(name = QUIT,
        mixinStandardHelpOptions = true,
        version = "1.0.0",
        description = "Quit Command Line Akautauto Application.")
public class Quit extends AbstractCommand<String> {
    public Quit() {
        super();
    }

    @Override
    public String call() {
        return Main.EXIT_CODE;
    }
}
