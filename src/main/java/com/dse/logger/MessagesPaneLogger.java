package com.dse.logger;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

public class MessagesPaneLogger extends CliLogger {
    private String name;
    private TextArea textArea;
//    private OutputStream os = System.out;
//    private static final Charset CHARSET = StandardCharsets.UTF_8;

    public MessagesPaneLogger(String name) {
        super(name);
        this.name = name;
    }

    public MessagesPaneLogger(String name, TextArea textArea) {
        super(name);
        this.name = name;
        this.textArea = textArea;
    }

    public static MessagesPaneLogger get(Class<?> c, TextArea textArea) {
        return new MessagesPaneLogger(c.getName(), textArea);
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
            Platform.runLater(() -> {
                textArea.appendText(message);
            });
//            os.write(message.getBytes(CHARSET));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String getLastLine() {
        String[] lines = textArea.getText().split("\\R");
        if (lines.length > 0) {
            return lines[lines.length - 1];
        }
        else {
            return null;
        }
    }

    private void println(String message) {
        print(message + "\n");
    }

    public String getName() {
        return name;
    }

    public TextArea getTextArea() {
        return textArea;
    }
}
