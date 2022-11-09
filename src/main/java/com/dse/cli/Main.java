package com.dse.cli;

import com.dse.cli.command.ICommand;
import com.dse.cli.command.Quit;
import com.dse.guifx_v3.helps.UILogger;
import com.dse.logger.CliLogger;

import java.util.Scanner;

public class Main {
    public static void maincli(String[] args) {
        CliLogger logger = CliLogger.get(Main.class);
        UILogger.setLogMode(UILogger.MODE_CLI);

        Scanner sc = new Scanner(System.in);

        String code = RUNNING_CODE;

        while (!code.equals(EXIT_CODE)) {
            String cmd = sc.nextLine();

            try {
                ICommand<?> command = ICommand.parse(cmd);

                if (command instanceof Quit) {
                    String temp = ((Quit) command).execute();
                    if (temp != null) {
                        code = temp;
                        System.exit(0);
                    }

                } else if (command != null)
                    command.execute();

            } catch (Exception ex) {
                ex.printStackTrace();
                logger.error(ex.getMessage());
            }
        }
    }

    public static final String RUNNING_CODE = "running";
    public static final String EXIT_CODE = "exit";
}
