package auto_testcase_generation.testdata.object;

import com.dse.debugger.gdb.IGDBMI;
import com.dse.testcase_execution.DriverConstant;
import com.dse.util.Utils;

import java.io.*;

public class SE {
    protected String testdriver;
    protected String src2;
    protected String exe;
    protected boolean isDone = false;

    /**
     * read the result of command from the terminal
     */
    public static void displayResultinTerminal(Process gdbProcess) throws IOException {
        System.out.println("Getting the result from the terminal");
        BufferedReader in = new BufferedReader(new InputStreamReader(gdbProcess.getInputStream()));
        String line = "";
        while (true) {
            line = in.readLine();
            if (line == null)
                break;
            if (line.trim().startsWith("=breakpoint-modified"))
                System.out.println("\n");

            if (line.trim().equals("^error,msg=\"The program is not being run.\"")
                    || line.trim().equals("(gdb)") // no command
                    || line.trim().startsWith("^done,bkpt={number=\"") // set breakpoint
                    || line.trim().startsWith("~")
                    || line.trim().startsWith("^error")
            ) {
            } else
                System.out.println(line);
        }

        in.close();
        gdbProcess.getInputStream().close();
    }

//    public void compile() throws InterruptedException, IOException, NoSuchFieldException, IllegalAccessException {
//        String compilationCommand = "";
//        if (Environment.getInstance().getCompiler().isGPlusPlusCommand()) {
//            compilationCommand = String.format("g++ -g -std=c++11 -o \"%s\" \"%s\" %s", exe, testdriver, SourceConstant.COMPILE_FLAG_FOR_GOOGLETEST);
//        } else if (Environment.getInstance().getCompiler().isGccCommand()) {
//            compilationCommand = String.format("g++ -g -std=c++11 -o \"%s\" \"%s\" %s", exe, testdriver, SourceConstant.COMPILE_FLAG_FOR_CUNIT);
//        }
//        System.out.println(compilationCommand);
//
//        Environment.getInstance().setCompiler(new Compiler(AvailableCompiler.CPP_11_GNU_NATIVE.class));
//        String[] script = CompilerUtils.prepareForTerminal(Environment.getInstance().getCompiler(), compilationCommand);
//
//        String response = new Terminal(script).get();
//        System.out.println("Response = " + response);
//    }

    /**
     * Add breakpoint to main function
     *
     * @param process
     */
    public void runCommandInGdb(Process process, String command, boolean shouldClosed) {
        System.out.println("Executing command: " + command);
        // execute gdb command
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(process.getOutputStream())), true);
        out.println(command);
        out.flush();
        if (shouldClosed) {
            out.close();
        }
    }

    private void setBreakpointAll(String path, Process gdbProcess) {
        String content = Utils.readFileContent(path);
        int nLines = content.replaceAll("\n\r", "\n")
                .replaceAll("\r\n", "\n")
                .replaceAll("\r", "\n").split("\n").length;
        for (int i = 1; i < 10; i++)
            runCommandInGdb(gdbProcess, String.format(BREAK_INSERT + " \"%s:%s\"", path, i), false);

        System.out.println("sdfds");
    }

    public void debug(String out) throws IOException {
        // Start gdb with mi interpreter
        String outFile = new File(out).getCanonicalPath();
        String gdbcommand = String.format("%s %s %s", PATH_GDB, outFile, RUN_WITH_GDB_MI);
        System.out.println("Command: " + gdbcommand);
        Process gdbProcess = Runtime.getRuntime().exec(gdbcommand);

        if (gdbProcess != null) {
            // https://www.zeuthen.desy.de/dv/documentation/unixguide/infohtml/gdb/GDB_002fMI-Breakpoint-Commands.html#GDB_002fMI-Breakpoint-Commands
            setBreakpointAll(getSrc2(), gdbProcess);
            runCommandInGdb(gdbProcess, EXEC_RUN, false);
            runCommandInGdb(gdbProcess, SKIP_FUNCTION, false);
            runCommandInGdb(gdbProcess, EXEC_RUN, false);

//            runCommandInGdb(gdbProcess, IGDBMI.GDB_CREATE_VARIABLE + " r1\"", false);

            for (int i = 0; i < 1000; i++) {
//
//                runCommandInGdb(gdbProcess, IGDBMI.GDB_GET_CHILD_VARIABLE + " r1\"", false);
////                runCommandInGdb(gdbProcess, PRINT, false);
                runCommandInGdb(gdbProcess, EXEC_CONTINUE, false);
            }

            runCommandInGdb(gdbProcess, EXEC_STOP, true);

            displayResultinTerminal(gdbProcess);
            gdbProcess.destroy();
        }
    }

    public String getExe() {
        return exe;
    }

    public void setExe(String exe) {
        this.exe = exe;
    }

    public void setTestdriver(String testdriver) {
        this.testdriver = testdriver;
    }

    public String getTestdriver() {
        return testdriver;
    }

    public void setSrc2(String src2) {
        this.src2 = src2;
    }

    public String getSrc2() {
        return src2;
    }

    public static final String PATH_GDB = "/usr/local/bin/gdb";
    public static final String RUN_WITH_GDB_MI = "--interpreter=mi";
    public static final String BREAK_INSERT = IGDBMI.GDB_BR;
    public static final String BREAK_LIST = IGDBMI.GDB_BR_LIST;
    public static final String SKIP_FUNCTION = IGDBMI.GDB_SKIP_FUNCTION + DriverConstant.MARK;
    public static final String EXEC_RUN = IGDBMI.GDB_RUN;
    public static final String EXEC_CONTINUE = IGDBMI.GDB_NEXT;
    public static final String EXEC_STOP = IGDBMI.GDB_EXIT;
    public static final String PRINT = IGDBMI.GDB_FRAME_ARGUMENTS;

}
