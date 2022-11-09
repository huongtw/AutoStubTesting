package com.dse.logger;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class CliLogger {
    private String name;
    private OutputStream os = System.out;
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    protected CliLogger(String name) {
        this.name = name;
    }

    protected CliLogger(String name, OutputStream os) {
        this.name = name;
        this.os = os;
    }

    public static CliLogger get(Class<?> c) {
        return new CliLogger(c.getName());
    }

    public void info(Object message) {
        println(message.toString());
    }

    public void error(Object message) {
        print("ERROR: ");
        println(message.toString());
    }

    private void print(String message) {
        try {
            os.write(message.getBytes(CHARSET));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void println(String message) {
        print(message + "\n");
    }

    public String getName() {
        return name;
    }

    public OutputStream getOutputStream() {
        return os;
    }
}
