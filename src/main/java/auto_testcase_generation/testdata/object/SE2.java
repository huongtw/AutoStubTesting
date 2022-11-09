package auto_testcase_generation.testdata.object;

import java.io.*;

public class SE2 extends SE{

    @Override
    public void debug(String out) throws IOException {
        try {
            String gdbcommand = "/data/bin/gdb " + getExe();
            String line;

            Process process = Runtime.getRuntime().exec("su");

            if (process != null) {
                BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
                PrintWriter outPrinter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(process.getOutputStream())),true);

                outPrinter.println(gdbcommand);

                //Note this line does not get sent to gdb's interface after starting gdb.
                //Is it possible to connect to the new stdout of gdb's interface?
                outPrinter.println("info registers");

                outPrinter.flush();
                outPrinter.close();

                while ((line = in.readLine()) != null) {
                    System.out.println(line);
                }

                process.destroy();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
