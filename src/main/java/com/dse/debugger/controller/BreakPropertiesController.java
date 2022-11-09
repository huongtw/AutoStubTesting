package com.dse.debugger.controller;

import com.dse.debugger.component.breakpoint.BreakPoint;
import com.dse.debugger.gdb.analyzer.OutputGDB;
import com.dse.guifx_v3.helps.UIController;
import com.dse.logger.AkaLogger;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class BreakPropertiesController implements Initializable {
    private static final AkaLogger logger = AkaLogger.get(BreakPropertiesController.class);

    private BreakPoint breakPoint;
    private Stage stage;

    @FXML
    CheckBox enable;

    @FXML
    CheckBox condition;

    @FXML
    Label bNum;

    @FXML
    Label hit;

    @FXML
    Label lineNum;

    @FXML
    Label func;

    @FXML
    TextArea textArea;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        condition.selectedProperty().addListener(e -> textArea.setDisable(!condition.isSelected()));
    }

    public static Stage getWindow(BreakPoint breakPoint) {
        FXMLLoader loader = new FXMLLoader(Object.class.getResource("/FXML/debugger/BreakProperties.fxml"));
        try {
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Breakpoint properties");

            BreakPropertiesController controller = loader.getController();
            controller.setBreakPoint(breakPoint);
            controller.setUp();
            controller.setStage(stage);
            return stage;
        } catch (Exception e) {
            logger.debug("Can not load Break Properties FXML");
            e.printStackTrace();
            return null;
        }
    }

    private void setStage(Stage stage) {
        this.stage = stage;
    }

    private void setUp(){
        bNum.setText(String.valueOf(breakPoint.getNumber()));
        hit.setText(String.valueOf(breakPoint.getTimes()));
        lineNum.setText(String.valueOf(breakPoint.getLine()));
        func.setText(breakPoint.getFunc());
        if (breakPoint.getSelected()) {
            enable.setSelected(true);
        } else {
            enable.setSelected(false);
        }
        if (breakPoint.getCond() == null) {
            condition.setSelected(false);
            textArea.setDisable(true);
        } else {
            condition.setSelected(true);
            textArea.setText(this.breakPoint.getCond());
            textArea.setDisable(false);
        }
    }

    private void setBreakPoint(BreakPoint breakPoint) {
        this.breakPoint = breakPoint;
    }

    public void ok() {
        // check if enable or not
        if (enable.isSelected() != this.breakPoint.getSelected()) {
            if (enable.isSelected()){
                DebugController.getDebugController().getGdb().enableBreakPoint(this.breakPoint);
                this.breakPoint.setSelected(true);
                this.breakPoint.setEnabled("y");
            } else {
                DebugController.getDebugController().getGdb().disableBreakPoint(this.breakPoint);
                this.breakPoint.setSelected(false);
                this.breakPoint.setEnabled("n");
            }
        }
        // check expression
        String express = textArea.getText();
        while (express.endsWith("\n")) {
            express = express.substring(0,express.length()-1);
        }
        if (condition.isSelected()){
            OutputGDB output = DebugController.getDebugController().getGdb().evaluateExpression(express);
            System.out.println(output);
            if (output.isError()) {
                String msg = JsonParser.parseString(output.getJson()).getAsJsonObject().get("msg").getAsString();
                UIController.showErrorDialog(msg, "Error", "Try again");
                return;
            }
            OutputGDB addConOutput = DebugController.getDebugController().getGdb().addConditionOnBreakPoint(this.breakPoint,express);
            System.out.println(addConOutput);
            if (addConOutput.isError()) {
                String msg = JsonParser.parseString(output.getJson()).getAsJsonObject().get("msg").getAsString();
                UIController.showErrorDialog(msg, "Error", "Try again");
                return;
            }
            DebugController.getDebugController().updateBreakPointFile();
            UIController.showSuccessDialog("Add condition successfully","Notification","Success");
        } else {
            if (breakPoint.getCond() != null && !breakPoint.getCond().equals("")) {
                OutputGDB removeConOutput = DebugController.getDebugController().getGdb().removeConditionOnBreakPoint(breakPoint);
//                if (removeConOutput.isError()) {
//                    String msg = JsonParser.parseString(output.getJson()).getAsJsonObject().get("msg").getAsString();
//                    UIController.showErrorDialog(msg, "Error", "Try again");
//                    return;
//                }
                DebugController.getDebugController().updateBreakPointFile();
//                UIController.showSuccessDialog("Remove condition successfully","Notification","Success");
            }
        }
        this.stage.close();
    }

    public void cancel() {
        if (stage != null) {
            this.stage.close();
        }
    }
}
