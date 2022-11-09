package com.dse.debugger;

import com.dse.debugger.component.breakpoint.BreakPoint;
import com.dse.debugger.gdb.OutputAnalyzer;
import com.dse.debugger.gdb.analyzer.OutputGDB;
import com.dse.environment.Environment;
import com.dse.logger.AkaLogger;
import com.dse.util.SpecialCharacter;
import javafx.concurrent.Task;

import java.io.*;

import static com.dse.debugger.gdb.IGDBMI.GDB_BR;

public class GDBProcess  {

    private static final AkaLogger logger = AkaLogger.get(GDBProcess.class);

    private final Process process;

    public GDBProcess(String confPath, String exePath) throws IOException {
        String debugCommand = Environment.getInstance().getCompiler().getDebugCommand();
        String command = String.format("%s -x %s %s --interpreter=mi", debugCommand, confPath, exePath);
        this.process = Runtime.getRuntime().exec(command, null, new File(exePath).getParentFile());
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(process.getOutputStream())), true);
        out.println("-info-gdb-mi-command -info-gdb-mi-command");
        out.flush();
        String initLog = new LogReader(process).call();
        logger.debug("Start GDB:\n" + initLog);
    }


    private static class LogReader extends Task<String> {

        private final Process process;

        public LogReader(Process process) {
            this.process = process;
        }

        @Override
        protected String call() {
            InputStreamReader isr = new InputStreamReader(process.getInputStream());
            BufferedReader br = new BufferedReader(isr);

            StringBuilder response = new StringBuilder();

            while (true) {
                try {
                    String s = br.readLine();

                    if (s == null)
                        break;

                    if (s.startsWith("^done,command={exists=\"true\"}") || s.contains("info-gdb-mi-command")) {
                        br.readLine();
                        break;
                    }

                    logger.debug(s);

                    response.append(s)
                            .append(SpecialCharacter.LINE_BREAK);

                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }

            return response.toString();
        }
    }

    public synchronized LogReader execute(String cmd) {
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(process.getOutputStream())), true);
        out.println(cmd);
        out.flush();

        return new LogReader(process);
    }

    public synchronized String executeAndLog(String cmd) {
        logger.debug("Execute " + cmd);
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(process.getOutputStream())), true);
        out.println(cmd);
        out.flush();
        out.println("-info-gdb-mi-command -info-gdb-mi-command");
        out.flush();

        return new LogReader(process).call();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        GDBProcess gdbProcess = new GDBProcess("/Users/lamnt/Projects/akautauto/datatest/aka-working-space/fot0/debugger/gdb.conf","/Users/lamnt/Projects/akautauto/datatest/aka-working-space/fot0/exe/debug_func_1_basis_0.exe");

        Thread.sleep(1000);

        gdbProcess.executeAndLog("skip -gfile \"/Users/lamnt/Projects/akautauto/datatest/aka-working-space/fot0/test-drivers/func_1_basis_0.c\"");
        gdbProcess.executeAndLog("-gdb-set logging file \"/Users/lamnt/Projects/akautauto/datatest/aka-working-space/fot0/debugger/commands/func_1_basis_0.log\"");
        gdbProcess.executeAndLog("-gdb-set logging redirect on");
        gdbProcess.executeAndLog("-gdb-set logging overwrite on");
        gdbProcess.executeAndLog("-enable-pretty-printing");
        gdbProcess.executeAndLog("-enable-timings");
        gdbProcess.executeAndLog("-break-insert \"fot0.fpt.akaignore.c:21\"");
        Thread.sleep(2000);

        gdbProcess.executeAndLog("run");
    }
}
