package auto_testcase_generation.cte.UI.Controller;

import auto_testcase_generation.cte.UI.CteClassificationTree.CteViewManager;
import com.dse.guifx_v3.controllers.object.LoadingPopupController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ColorChangeController {
    @FXML
    private  ColorPicker colorPicker;

    @FXML
    private CheckBox forSubTree;

    @FXML
    private Button closeBut;

    @FXML
    private Button saveBut;

    private Stage stage;

    private CteViewManager viewManager;
    private static ColorChangeController instance;
    public static ColorChangeController newInstance(CteViewManager manager, Color currentColor)
    {
        FXMLLoader loader = new FXMLLoader(Object.class.getResource("/FXML/cte/ColorChange.fxml"));
        try {
            Parent parent = loader.load();

            Scene scene = new Scene(parent);

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setResizable(false);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("Color Table");
            stage.getIcons().add(new Image("/icons/cte/colorPicker.png"));

            instance = loader.getController();
            instance.setStage(stage);
            instance.setViewManager(manager);
            instance.setUpColorPicker(currentColor);
            instance.setUp();

        } catch (Exception e) {
            e.printStackTrace();
            instance = null;
        }
        return instance;
    }

    public static ColorChangeController getInstance() {
        return instance;
    }

    public void show() {
        stage.show();
    }

    public void close() {
        Platform.runLater(() -> {
            if (stage != null) {
                stage.close();
            }
        });
    }

    public void setUp()
    {
        closeBut.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                close();
            }
        });

        saveBut.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                boolean isForSubTree = forSubTree.isSelected();
                viewManager.changeColorForNodes(colorPicker.getValue(), isForSubTree);
                close();
            }
        });
    }

    public Stage getStage() {
        return stage;
    }

    public void initOwnerStage(Stage ownerStage) {
        stage.initOwner(ownerStage);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public CteViewManager getViewManager() {
        return viewManager;
    }

    public void setViewManager(CteViewManager viewManager) {
        this.viewManager = viewManager;
    }

    public void setUpColorPicker(Color curColor)
    {
        colorPicker.setValue(curColor);
    }


}
