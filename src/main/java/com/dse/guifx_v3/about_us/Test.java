package com.dse.guifx_v3.about_us;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class Test extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(Object.class.getResource("/FXML/help/test.fxml"));
        try {
            Parent parent = loader.load();
//            mdiWindow = (AnchorPane) parent;
//            mdiWindowController = loader.getController();
            Scene scene = new Scene(parent);
            primaryStage.setScene(scene);

            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
