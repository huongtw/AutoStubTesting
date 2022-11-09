package com.dse.debugger.utils;

import com.dse.thread.AbstractAkaTask;
import com.dse.logger.AkaLogger;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicBoolean;

public class CurprocessThread extends AbstractAkaTask<Object> {
    final static AkaLogger logger = AkaLogger.get(CurprocessThread.class);
    private Process process;
    private String command;
    private AtomicBoolean gdb_mi_enabled = new AtomicBoolean(false);

    @Override
    public Object call() {
        while (true) {
            while (!gdb_mi_enabled.get()) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            execute(command);
            gdb_mi_enabled.set(false);
        }
    }

    public synchronized void execute(String command) {
        logger.debug("[CurprocessThread] Start executing command: " + command);
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(this.getProcess().getOutputStream())), true);
        out.println(command);
        out.flush();
        logger.debug("[CurprocessThread] Finish executing command: " + command);
    }


    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public boolean isGdb_mi_enabled() {
        return gdb_mi_enabled.get();
    }

    public void setGdb_mi_enabled(boolean gdb_mi_enabled) {
        this.gdb_mi_enabled.set(gdb_mi_enabled);
    }
}
