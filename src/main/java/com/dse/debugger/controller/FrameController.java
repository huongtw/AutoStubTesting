package com.dse.debugger.controller;

import com.dse.debugger.component.frame.GDBFrame;
import com.dse.debugger.component.frame.GDBFrameListCell;
import com.dse.debugger.gdb.analyzer.OutputGDB;
import com.dse.guifx_v3.helps.UIController;
import com.dse.logger.AkaLogger;
import com.google.gson.JsonParser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TitledPane;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class FrameController implements Initializable {
    private final static AkaLogger logger = AkaLogger.get(FrameController.class);

    private static FrameController frameController = null;
    private static TitledPane titledPane = null;

    private static void prepare() {
        FXMLLoader loader = new FXMLLoader(FrameController.class.getResource("/FXML/debugger/Frames.fxml"));
        try {
            titledPane = loader.load();
            frameController = loader.getController();
        } catch (Exception e) {
            logger.debug("Can not load Frames UI");
            e.printStackTrace();
        }
    }

    public static TitledPane getTitledPane() {
        if (titledPane == null) prepare();
        return titledPane;
    }

    public static FrameController getFrameController() {
        if (frameController == null) prepare();
        return frameController;
    }

    private ObservableList<GDBFrame> frameList = FXCollections.observableArrayList();

    @FXML
    ListView<GDBFrame> frameListView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.frameListView.setItems(this.frameList);
        this.frameListView.setCellFactory(e -> new GDBFrameListCell());
        this.frameListView.getSelectionModel().selectedItemProperty()
                .addListener(((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        if (oldValue != newValue) {
                            focusFrame(newValue);
                        }
                    }
                }));
    }

    public void clearAll() {
        this.frameListView.getItems().clear();
    }

    private void focusFrame(GDBFrame frame) {
        System.out.println(frame);
        if (frame.getFile() == null || frame.getFullName() == null) {
            UIController.showErrorDialog("Can not access the frame corresponding to " + frame.getFunc() + " at " + frame.getAddr(), "Gdb error", "Something wrong with gdb");
        } else {
            DebugController debugController = DebugController.getDebugController();
            OutputGDB outputGDB = debugController.getGdb().selectFrame(frame);
            if (outputGDB == null) {
                UIController.showErrorDialog("Can not change current frame", "Error", "Try again");
                return;
            }
            if (outputGDB.isError()) {
                String msg = JsonParser.parseString(outputGDB.getJson()).getAsJsonObject().get("msg").getAsString();
                UIController.showErrorDialog(msg, "Error", "Try again");
            } else {
                WatchController.getWatchController().updateWatches();
                VariableController.getVariableController().updateVariables();
                debugController.openCurrentHitLine(frame.getLine(), frame.getFile(), false);
            }
        }
    }

    public void updateFrames() {
        logger.debug("Start updating frames");
        DebugController debugController = DebugController.getDebugController();
        ArrayList<GDBFrame> listFrame = debugController.getGdb().getFrames();
        this.frameList.clear();
        this.frameList.addAll(listFrame);
        if (listFrame.size() > 0) {
            GDBFrame curFrame = listFrame.get(0);
            String realFilePath = curFrame.getFile();
//            String fullName = curFrame.getFullName();
            if (curFrame.getFullName() == null || realFilePath == null) {
                logger.debug("current frame is " + curFrame.getFunc());
            } else {
                File file = new File(realFilePath);

                if (file.exists())
                    debugController.openCurrentHitLine(curFrame.getLine(), realFilePath, true);
                else {
                    logger.debug("GDB reach the last line of main function");
                }
            }
            this.frameListView.getSelectionModel().select(0);
        }
        logger.debug("End updating frames");
    }
}
