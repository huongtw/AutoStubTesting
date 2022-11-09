package com.dse.guifx_v3.controllers;

import com.dse.logger.AkaLogger;
import com.dse.util.SpecialCharacter;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.w3c.dom.Document;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public class CoverageViewTabController implements Initializable {

    private final static AkaLogger logger = AkaLogger.get(CoverageViewTabController.class);

    private int line = -1;

    @FXML
    private ProgressBar pBCoverageProgressFunc;
    @FXML
    private Label lbPercentageFunc;
    @FXML
    private Label lProgressDetailFunc;
    @FXML
    private ProgressBar pBCoverageProgressFnInTab2;
    @FXML
    private Label lbPercentageFnInTab2;
    @FXML
    private Label lProgressDetailFnInTab2;

    @FXML
    private TabPane tabPane;

    @FXML
    private Tab tCoverageTab1;

    @FXML
    private Tab tCoverageTab2;

    @FXML
    private ProgressBar pBCoverageProgress; // display percentage

    @FXML
    private ProgressBar pBCoverageProgressInTab2; // display percentage

    @FXML
    private ScrollPane spCoverage; // display the highlight source code

    @FXML
    private ScrollPane spCoverageInTab2; // display the highlight source code

    @FXML
    private Label lbPercentage;

    @FXML
    private Label lbPercentageInTab2;

    @FXML
    private Label lProgressDetail;

    @FXML
    private Label lProgressDetailInTab2;

    public void setLine(int line) {
        this.line = line;
    }

    public void initialize(URL location, ResourceBundle resources) {
        tabPane.getTabs().remove(tCoverageTab2);
    }

    // TAB 1-----------------------------
    public void updateProgress(float progress) {
        pBCoverageProgress.setProgress(progress);
        float percentage = progress * 100;
        lbPercentage.setText(percentage + "%");


    }

    public void updateProgressForFunctionLevel(float progress){
        pBCoverageProgressFunc.setProgress(progress);
        float percentage = progress * 100;
        lbPercentageFunc.setText(percentage + "%");
    }

    public void loadContentToCoverageViewInTab1(String nameTab, String content) {
        loadContentToCoverageView(nameTab, content, 1);
    }

    public void updateProgressDetail(String ratio) {
        lProgressDetail.setText(ratio);
        /// detail.setText
    }

    public void updateProgressDetailForFunctionLevel(String ratio){
        lProgressDetailFunc.setText(ratio);
    }

    // TAB 2-----------------------------
    public void updateProgressInTab2(float progress) {
        pBCoverageProgressInTab2.setProgress(progress);
        float percentage = progress * 100;
        lbPercentageInTab2.setText(percentage + "%");
    }

    public void updateProgressFunctionInTab2(float progress){
        pBCoverageProgressFnInTab2.setProgress(progress);
        float percentage = progress * 100;
        lbPercentageFnInTab2.setText(percentage + "%");
    }

    public void loadContentToCoverageViewInTab2(String nameTab, String content) {
        if (!tabPane.getTabs().contains(tCoverageTab2))
            tabPane.getTabs().add(tCoverageTab2);
//        tCoverageTab2.setText(nameTab);
//
//        final WebView coverage = new WebView();
//        final WebEngine webEngine = coverage.getEngine();
//        webEngine.loadContent(content);
//        spCoverageInTab2.setContent(coverage);
//
//        spCoverageInTab2.widthProperty().addListener((observable, oldValue, newValue) -> {
//            Double width = (Double) newValue;
//            coverage.setPrefWidth(width);
//        });
//        spCoverageInTab2.heightProperty().addListener((observable, oldValue, newValue) -> {
//            Double height = (Double) newValue;
//            coverage.setPrefHeight(height);
//        });
        loadContentToCoverageView(nameTab, content, 2);
    }

    private void loadContentToCoverageView(String nameTab, String content, int tabIndex) {
        Tab coverageTab;
        ScrollPane scrollPane;

        if (tabIndex == 1) {
            coverageTab = tCoverageTab1;
            scrollPane = spCoverage;
        } else {
            coverageTab = tCoverageTab2;
            scrollPane = spCoverageInTab2;
        }

        coverageTab.setText(nameTab);

        final WebView coverage = new WebView();
        final WebEngine webEngine = coverage.getEngine();

        final float scrollPercent = calculateYPosPercent(content);
        webEngine.documentProperty().addListener(new ChangeListener<Document>() {
            @Override
            public void changed(ObservableValue<? extends Document> observable, Document oldValue, Document newValue) {
                String heightText = webEngine.executeScript(
                        "window.getComputedStyle(document.body, null).getPropertyValue('height')"
                ).toString();
                double height = Double.parseDouble(heightText.replace("px", ""));
                double yPos = (height * scrollPercent) - 10f;
                if (yPos < 0) yPos = 0;

                webEngine.documentProperty().removeListener(this);

                String newContent = scrollWebView(yPos) + content;
                webEngine.loadContent(newContent);
            }
        });

        webEngine.loadContent(content);
        scrollPane.setContent(coverage);

        scrollPane.widthProperty().addListener((observable, oldValue, newValue) -> {
            Double width = (Double) newValue;
            coverage.setPrefWidth(width);
        });
        scrollPane.heightProperty().addListener((observable, oldValue, newValue) -> {
            Double height = (Double) newValue;
            coverage.setPrefHeight(height);
        });
    }

    public void updateProgressDetailInTab2(String ratio) {
        lProgressDetailInTab2.setText(ratio);
    }
    public void updateProgressDetailFunctionInTab2(String ratio) {
        lProgressDetailFnInTab2.setText(ratio);
    }

    public Label getLbPercentage() {
        return lbPercentage;
    }

    public static StringBuilder scrollWebView(double yPos) {
        StringBuilder script = new StringBuilder().append("<html>");
        script.append("<head>");
        script.append("   <script language=\"javascript\" type=\"text/javascript\">");
        script.append("       function toBottom(){");
        script.append("           window.scrollTo(0, ").append(yPos).append(");");
        script.append("       }");
        script.append("   </script>");
        script.append("</head>");
        script.append("<body onload='toBottom()'>");
        return script;
    }

    private float calculateYPosPercent(String content) {
        if (content != null) {
            String[] lines = content.split("\\R");

            int lastLineInt = -1;
            for (int i = lines.length - 1; i >= 0; i--) {
                if (lines[i].contains(LINE_TAG)) {
                    lastLineInt = getLine(lines[i]);
                    break;
                }
            }

            int line = this.line;

            if (line < 0) {
                String firstHighLightLine = Arrays.stream(lines)
                        .filter(l -> l.contains("style=\"background-color:yellow"))
                        .findFirst()
                        .orElse(null);
                if (firstHighLightLine != null) {
                    line = getLine(firstHighLightLine);
                }
            }

            return ((float) line / ((float) lastLineInt));
        }

        return 0;
    }

    private int getLine(String htmlLine) {
        String lineStr = htmlLine.replace(LINE_TAG, SpecialCharacter.EMPTY)
                .replaceAll("</b>.*", SpecialCharacter.EMPTY)
                .trim();

        return Integer.parseInt(lineStr);
    }

    private static final String LINE_TAG = "<b style=\"color: grey;\">";
}
