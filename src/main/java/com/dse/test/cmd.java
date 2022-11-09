package com.dse.test;

import auto_testcase_generation.testdatagen.AbstractAutomatedTestdataGeneration;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class cmd {

    public static void main(String[] args) throws InterruptedException, IOException {
        String Z3Path = "/Users/ducanhnguyen/Documents/akautauto/local/solver/z3-4.8.8-x64-osx-10.14.6/bin/z3";
        String smtFile = "/Users/ducanhnguyen/Documents/akautauto/local/tmp.smt2";
        String cmd = new File(Z3Path).getName() + " -smt2 " + smtFile;
        System.out.println("Cmd = " + cmd);
        Process p = Runtime.getRuntime().exec(cmd, new String[]{}, new File(Z3Path).getParentFile());
        p.waitFor(10, TimeUnit.SECONDS);

        // display
        String result = "";
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while ((line = in.readLine()) != null)
            result += line + "\n";
        System.out.println(result);
    }
}
