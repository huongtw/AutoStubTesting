package com.dse.guifx_v3.controllers.build_environment;

import com.dse.environment.object.EnviroLibraryStubNode;
import com.dse.report.element.Text;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class PopUpWindowController {
    private ListView<Label> listView;
    private String title;
    private Stage popUpWindow;
    private int type;

    @FXML
    private Label prompt;
    @FXML
    private TextField tfLibraryName;

    @FXML
    private TextField tfHeader;

    public void getLibraryStubs() {
        String libraryName = tfLibraryName.getText();
        if (libraryName == null || libraryName.trim().isEmpty()) return;

        String headerName = tfHeader.getText();
        // HOT-FIX, TO BE REMOVED
        // if (!headerName.matches("[a-zA-Z0-9_]+") && headerName.indexOf('(') != -1) {
        //     headerName = headerName.substring(0, headerName.indexOf('(')).trim();
        //     headerName = headerName.substring(headerName.lastIndexOf(' ') + 1);
        // }
        // HOT-FIX, TO BE REMOVED
        if (headerName == null || headerName.trim().isEmpty()) return;

        Label descriptionLibStub = new Label(libraryName + EnviroLibraryStubNode.SEPERATOR_BETWEEN_LIB_AND_ITS_HEADER + headerName);
//        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> listView.getSelectionModel().select(checkBox));
        listView.getItems().add(descriptionLibStub);

        if (popUpWindow != null) {
            popUpWindow.close();
        }
    }

    public void cancel() {
        if (popUpWindow != null) {
            popUpWindow.close();
        }
    }

    public static Stage getWindow(int type, ListView<Label> listView) {
        FXMLLoader loader;
        try {
            loader = new FXMLLoader(Object.class.getResource("/FXML/envbuilding/ChooseUUT_PopUpWindow.fxml"));
            Parent parent = loader.load();
            Scene scene = new Scene(parent);
            PopUpWindowController controller = loader.getController();

            if (type == PopUpWindowController.LIBRARY_STUB) {
                controller.setPrompt("Enter a library function:");
                controller.setTitle("Library Stubs");
            } else if (type == PopUpWindowController.ADDITIONAL_STUB) {
                controller.setPrompt("Enter a function to stub:");
                controller.setTitle("Additional Stub");
            } else if (type == PopUpWindowController.SUPPRESSED_STUB) {
                controller.setPrompt("Enter a function to suppress:");
                controller.setTitle("Suppressed Stub");
            } else if (type == PopUpWindowController.SUPPRESSED_TESTABLE_FUNCTIONS) {
                controller.setPrompt("Enter a function to suppress:");
                controller.setTitle("Suppressed Testable Functions");
            } else if (type == PopUpWindowController.NOT_SUPPORTED_TYPES) {
                controller.setPrompt("Enter a type name:");
                controller.setTitle("Not Supported Types");
            } else {
                System.out.println("The type is invalid");
            }

            controller.setListView(listView);
            controller.setType(type);

            Stage popUpWindow = new Stage();
            popUpWindow.setScene(scene);
            popUpWindow.setTitle(controller.title);
            popUpWindow.setResizable(false);
            controller.setPopUpWindow(popUpWindow);
            return popUpWindow;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @FXML
    public void keypressEnter(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            getLibraryStubs();
        }
    }

    public void setPrompt(String prompt) {
        this.prompt.setText(prompt);
    }

    public void setListView(ListView<Label> listView) {
        this.listView = listView;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setPopUpWindow(Stage popUpWindow) {
        this.popUpWindow = popUpWindow;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public final static int LIBRARY_STUB = 1;
    public final static int ADDITIONAL_STUB = 2;
    public final static int SUPPRESSED_STUB = 3;
    public final static int SUPPRESSED_TESTABLE_FUNCTIONS = 4;
    public final static int NOT_SUPPORTED_TYPES = 5;
}
