package com.dse.guifx_v3.controllers;

import com.dse.config.AkaConfig;
import com.dse.guifx_v3.helps.UIController;
import com.dse.license.LicenseGeneration;
import com.dse.logger.AkaLogger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.*;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;

public class LicenseController {

    public static final File LICENSE_FILE = new File(AkaConfig.LOCAL_DIRECTORY.getAbsolutePath() + "/license.lic");

    private static final AkaLogger logger = AkaLogger.get(LicenseController.class);

    private static final FileChooser.ExtensionFilter EXT_FILTER = new FileChooser.ExtensionFilter("License files (*.lic)", "*.lic");

    @FXML
    private Label customerNameLabel;

    @FXML
    private Label expiredDateLabel;

    @FXML
    private GridPane notActivatedPane;

    @FXML
    private Label licenseInfoLabel;

    @FXML
    private GridPane activatedPane;

    private static String customerName;

    private static String expiredDate;

    private Stage stage;

    private static boolean expiredLicense = true;

    private static boolean validLicense = false;

    private static boolean expiredImportedLicense = true;

    private static boolean validImportedLicense = false;

    public boolean isValidLicense() {
        return validLicense;
    }

    public boolean isExpiredLicense() {
        return expiredLicense;
    }

    public void setLicenseView() {
        logger.debug("setup license view");
        if (validLicense) {
            showLicenseInfo();
            activatedPane.setVisible(true);
            notActivatedPane.setVisible(false);
        } else {
            activatedPane.setVisible(false);
            notActivatedPane.setVisible(true);
        }
    }

    public static LicenseController getInstance() {
        FXMLLoader loader = new FXMLLoader(Object.class.getResource("/FXML/License.fxml"));
        try {
            Parent parent = loader.load();
            LicenseController controller = loader.getController();

            Scene scene = new Scene(parent);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Aka Automation Tool");
            stage.getIcons().add(new Image(LicenseController.class.getResourceAsStream("/FXML/bamboo.png")));
            stage.initStyle(StageStyle.DECORATED);
            controller.setStage(stage);
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    Platform.runLater(new Runnable() {

                        @Override
                        public void run() {
                            if (expiredLicense || !validLicense) {
                                // Shut down all automated test data generation threads
                                if (validLicense && !expiredLicense) {
                                    // do nothing
                                } else {
                                    UIController.shutdown();
                                }
                            }

                        }
                    });
                }
            });

            return controller;
        } catch (Exception e) {
            logger.error("Cant init license view", e);
            return null;
        }
    }

    public void importLicense() {
        Stage primaryStage = UIController.getPrimaryStage();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select License");
        fileChooser.getExtensionFilters().add(EXT_FILTER);
        File selectedDir = fileChooser.showOpenDialog(primaryStage);
        try {
            FileInputStream fis = new FileInputStream(selectedDir);
            Scanner scanner = new Scanner(fis);
            String encodedString = "";
            while (scanner.hasNext()) {
                encodedString += scanner.next();
            }
            scanner.close();
            fis.close();
            LicenseGeneration licenseGenerator = new LicenseGeneration();
            String decodedString = licenseGenerator.Decryption(encodedString);
            if (!decodedString.equals("")) {
                String[] infoParts = decodedString.split(" &&& ");
                String cName = infoParts[0];
                String exp = infoParts[1];
                checkExpiredDate(exp);
                if (expiredImportedLicense) {
                    showExpiredLicenseAlert(cName, exp);
                } else {
                    customerName = cName;
                    expiredDate = exp;
                    try {
                        BufferedWriter writer = new BufferedWriter(new FileWriter(LICENSE_FILE));
                        writer.write(encodedString);
                        writer.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    validLicense = true;
                    expiredLicense = false;
                    setLicenseView();
                    showValidLicenseAlert();

                }
            } else {
                showInvalidLicenseAlert();
            }
        } catch (Exception e) {
            if (selectedDir != null) {
                showInvalidLicenseAlert();
                e.printStackTrace();
            }
        }


    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }


    public void showLicenseInfo() {
        customerNameLabel.setText(customerName);
        expiredDateLabel.setText(expiredDate);
        if (expiredLicense) {
            licenseInfoLabel.setText("Your license has expired.");
            expiredDateLabel.setTextFill(Color.RED);
        } else {
            licenseInfoLabel.setText("License information");
            expiredDateLabel.setTextFill(Color.BLACK);
        }
    }

    private void showValidLicenseAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Aka Automation Tool");
        Stage stage1 = (Stage) alert.getDialogPane().getScene().getWindow();
        stage1.getIcons().add(new Image(getClass().getResourceAsStream("/FXML/bamboo.png")));
        alert.getButtonTypes().remove(0);
        alert.getButtonTypes().add(new ButtonType("Continue"));
        alert.setHeaderText("You have successfully imported\nyour license.");
        alert.showAndWait();
    }


    private void showExpiredLicenseAlert(String cName, String exp) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        Stage stage1 = (Stage) alert.getDialogPane().getScene().getWindow();
        stage1.getIcons().add(new Image(getClass().getResourceAsStream("/FXML/bamboo.png")));
        alert.setHeaderText("Your imported license has expired.");
        alert.setTitle("Aka Automation Tool");
        Label info = new Label("Customer Name: " + cName + "\n" + "Expired date: " + exp);
        alert.getDialogPane().setContent(info);
        alert.showAndWait();
    }

    private void showInvalidLicenseAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        Stage stage1 = (Stage) alert.getDialogPane().getScene().getWindow();
        stage1.getIcons().add(new Image(getClass().getResourceAsStream("/FXML/bamboo.png")));
        alert.setHeaderText("Your imported license is invalid.\n");
        alert.setTitle("Aka Automation Tool");
        alert.setContentText("Please import a new valid license.");
        alert.showAndWait();
    }

    private void checkExpiredDate(String expiredDate) {
        try {
            Date expDate = new SimpleDateFormat("dd-MM-yyyy").parse(expiredDate);
            expDate.setHours(23);
            expDate.setMinutes(59);
            expDate.setSeconds(59);
            Date current = Calendar.getInstance().getTime();
            if (current.before(expDate)) {
                expiredImportedLicense = false;
            } else {
                expiredImportedLicense = true;
            }
            validImportedLicense = true;
        } catch (ParseException e) {
            validImportedLicense = false;
            e.printStackTrace();
        }
    }

    public void getLicense() {
        if (LICENSE_FILE.exists()) {
            try {
                FileInputStream fis = new FileInputStream(LICENSE_FILE);
                try {
                    Scanner scanner = new Scanner(fis);
                    String encodedString = scanner.next();
                    scanner.close();
                    fis.close();
                    LicenseGeneration licenseGenerator = new LicenseGeneration();
                    String decodedString = licenseGenerator.Decryption(encodedString);
                    if (!decodedString.equals("")) {
                        String[] infoParts = decodedString.split(" &&& ");
                        customerName = infoParts[0];
                        expiredDate = infoParts[1];
                        checkExpiredDate(expiredDate);
                        validLicense = validImportedLicense;
                        expiredLicense = expiredImportedLicense;
                    }
                } catch (IOException e) {
                    validLicense = false;
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                validLicense = false;
                e.printStackTrace();
            }
            if (!validLicense) {
                showInvalidLicenseAlert();
            }
        } else {
            validLicense = false;
        }

    }

    @FXML
    public void closeLicenseView(ActionEvent actionEvent) {
        if (validLicense && !expiredLicense) {
            stage.close();
        } else {
            UIController.shutdown();
        }
    }


}
