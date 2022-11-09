package com.dse.guifx_v3.controllers.object.build_environment;

import com.dse.guifx_v3.controllers.build_environment.AbstractCustomController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

public class Step {
    private Label label;
    private AnchorPane anchorPane;
    private AbstractCustomController controller;
    public static String CHOOSE_COMPILER = "/FXML/envbuilding/ChooseCompiler.fxml";
    public static String NAME_ENVIRONMENT = "/FXML/envbuilding/NameEnvironment.fxml";
    public static String TESTING_METHOD = "/FXML/envbuilding/TestingMethod.fxml";
    public static String BUILD_OPTIONS = "/FXML/envbuilding/BuildOptions.fxml";
    public static String LOCATE_SOURCE_FILES = "/FXML/envbuilding/LocateSourceFiles.fxml";
    public static String CHOOSE_UUT = "/FXML/envbuilding/ChooseUUT.fxml";
    public static String USER_CODE = "/FXML/envbuilding/UserCode.fxml";
    public static String SUMARY = "/FXML/envbuilding/Summary.fxml";

    public Step(Label label, String resourceLink) {
        this.label = label;
        this.anchorPane = new AnchorPane();
        FXMLLoader loader = new FXMLLoader(Object.class.getResource(resourceLink));
        try {
            Parent parent = loader.load();
            controller = loader.getController();
            if (parent instanceof AnchorPane) {
                this.anchorPane = (AnchorPane) parent;
            }
            controller.setLabelStep(label);
            controller.validate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public Label getLabel() {
        return label;
    }
    public void setLabel(Label label) {
        this.label = label;
    }
    public AnchorPane getAnchorPane() {
        return anchorPane;
    }
    public AbstractCustomController getController() {
        return controller;
    }
}
