package auto_testcase_generation.testdatagen.se.solver;

import auto_testcase_generation.testdatagen.AbstractAutomatedTestdataGeneration;
import com.dse.logger.AkaLogger;
import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Chạy file smt-lib trên cmd sử dụng SMT-Solver Z3
 *
 * @author anhanh
 */
public class RunZ3OnCMD {
    final static AkaLogger logger = AkaLogger.get(RunZ3OnCMD.class);

    private final String Z3Path;
    private final String smtLibPath;
    private String result = SpecialCharacter.EMPTY;

    public RunZ3OnCMD(String Z3Path, String smtLibPath) {
        this.Z3Path = Z3Path;
        this.smtLibPath = smtLibPath;
    }

    public synchronized void execute() throws Exception {
        logger.debug("RunZ3OnCMD begin");

        Date startTime = Calendar.getInstance().getTime();

        Process p = null;
        if (Utils.isWindows()) {
            p = Runtime.getRuntime().exec(
                    new String[]{Utils.doubleNormalizePath(Z3Path), "-smt2", smtLibPath}
//                    , new String[]{},
//                    new File(Z3Path).getParentFile()
            );
        } else if (Utils.isUnix()) {
            p = Runtime.getRuntime().exec(
                    new String[]{"./" + new File(Z3Path).getName(), "-smt2", smtLibPath}
                    , new String[]{},
                    new File(Z3Path).getParentFile());
        } else if (Utils.isMac()) {
            p = Runtime.getRuntime().exec(new String[]{Z3Path, "-smt2", smtLibPath});
        }

        InputStream terminal_eer = p.getErrorStream();
        InputStream terminal_out = p.getInputStream();

        assert p != null;
        p.waitFor(1, TimeUnit.MINUTES);
//        p.waitFor();

        AbstractAutomatedTestdataGeneration.numOfSolverCalls++;
        Date end = Calendar.getInstance().getTime();
        AbstractAutomatedTestdataGeneration.solverRunningTime += end.getTime() - startTime.getTime();

        BufferedReader in = new BufferedReader(new InputStreamReader(terminal_out));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null)
            builder.append(line).append(SpecialCharacter.LINE_BREAK);
        result = builder.toString();

        // Display errors if exists
        if (terminal_eer != null) {
            BufferedReader error = new BufferedReader(new InputStreamReader(terminal_eer));
            String err;
            boolean hasError = false;
            while ((err = error.readLine()) != null) {
                logger.error(err);
                hasError = true;
            }
            if (hasError)
                AbstractAutomatedTestdataGeneration.numOfSolverCallsbutCannotSolve++;
        }

        logger.debug("RunZ3OnCMD end");
    }

    public String getSolution() {
        return result;
    }
}
