package com.dse.guifx_v3.about_us;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;

public class DeletedViewAboutUs extends AnchorPane {

    private AnchorPane node;
    private DeletedViewAboutUsController controller;

    private static DeletedViewAboutUs instance;
    public DeletedViewAboutUsController getController() {
        return controller;
    }

    public DeletedViewAboutUs() {
        FXMLLoader loader = new FXMLLoader(Object.class.getResource("/FXML/help/ViewAboutUs.fxml"));
        try {
            loader.setControllerFactory(new Callback<Class<?>, Object>() {

                @Override
                public Object call(Class<?> param) {
                    return controller = new DeletedViewAboutUsController();
                }
            });

            node = loader.load();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static DeletedViewAboutUs getInstance() {
        if (instance != null) {
            return instance;
        }
        instance = new DeletedViewAboutUs();
        return instance;
    }

    /**
     *
     */
    public void viewUserManual() {
        controller.setContent();
        controller.viewUserManual(node);
    }
}
