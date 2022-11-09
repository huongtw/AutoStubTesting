package com.dse.guifx_v3.about_us;

import com.dse.guifx_v3.controllers.main_view.MDIWindowController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class DeletedViewAboutUsController implements Initializable {

    @FXML
    private TextArea textArea;

    private static String _PDF_USER_MANUAL_PATH = "help/test.txt";

    private static DeletedViewAboutUsController instance = null;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    /**
     * Read data from file path.
     * @param path file path
     * @return
     */
    private List<String> readData(String path) {
        URL resource = getClass().getClassLoader().getResource(path);
        List<String> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(resource.getPath()))) {

            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                sCurrentLine += "\n";
                data.add(sCurrentLine);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    public static DeletedViewAboutUsController getInstance() {
        if (instance != null) {
            instance.setContent();
            return instance;
        }
        FXMLLoader loader = new FXMLLoader(Object.class.getResource("/FXML/help/ViewAboutUs.fxml"));
        try {
            loader.load();
            instance = loader.getController();
            return instance;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Set content for User Manual tab.
     */
    public void setContent() {
        if (textArea == null) {
            textArea = new TextArea();
        }
        List<String> content = readData(_PDF_USER_MANUAL_PATH);
        String str = "";
        for (String s : content) {
            str += s;
        }
        textArea.setText(str);
        textArea.setWrapText(true);
//        spHelp.setContent(textArea);
    }

    public void viewUserManual(AnchorPane node) {
//        MDIWindowController.getMDIWindowController().viewAboutUs(node);
    }


}
