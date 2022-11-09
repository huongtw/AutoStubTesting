package com.dse.regression.controllers;

import com.dse.logger.MessagesPaneLogger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;

import java.util.HashMap;
import java.util.Map;

public class MessagesPaneTabContentController {
    @FXML
    private TextArea textArea;
    @FXML
    private Tab tab;

    private MessagesPaneLogger logger;

    private static Map<String, MessagesPaneTabContentController> cache = new HashMap<>();

    public static MessagesPaneTabContentController newInstance(String name) {
        if (cache.containsKey(name))
            return cache.get(name);
        else {
            FXMLLoader loader = new FXMLLoader(Object.class.getResource("/FXML/regression_script/MessagesPaneTabContent.fxml"));
            try {
                loader.load();
                MessagesPaneTabContentController controller = loader.getController();
                MessagesPaneLogger logger = new MessagesPaneLogger(MessagesPaneTabContentController.class.getSimpleName(), controller.getTextArea());
                controller.setLogger(logger);
                Tab akaMessagesTab = controller.getTab();
                akaMessagesTab.setClosable(false);
                akaMessagesTab.setText(name);
                cache.put(name, controller);
                return controller;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public Tab getTab() {
        return tab;
    }

    public void setLogger(MessagesPaneLogger logger) {
        this.logger = logger;
    }

    public MessagesPaneLogger getLogger() {
        return logger;
    }

    public TextArea getTextArea() {
        return textArea;
    }

    public void appendLog(Object m) {
//        if (textArea != null) {
//            Platform.runLater(new Runnable() {
//                @Override
//                public void run() {
//                    textArea.appendText(m + "\n");
//                }
//            });
//        }
    }
}
