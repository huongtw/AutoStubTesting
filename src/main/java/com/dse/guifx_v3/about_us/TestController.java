package com.dse.guifx_v3.about_us;

import com.dse.util.Utils;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.w3c.dom.Document;

import javax.naming.spi.DirectoryManager;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.ResourceBundle;

public class TestController implements Initializable {

    @FXML
    private WebView wvUerManual;

    public WebView getWvUerManual() {
        return wvUerManual;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
//        WebEngine engine = wvUerManual.getEngine();
//        byte[] data = new byte[0];
//        try {
//            data = Files.readAllBytes(Paths.get("/home/tj/akautauto/src/main/resources/PDF/test.pdf"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        String base64 = Base64.getEncoder().encodeToString(data);
////        String base64 = "JVBERi0xLjcKCjEgMCBvYmogICUgZW50cnkgcG9pbnQKPDwKICAvVHlwZSAvQ2F0YWxvZwog' +\n" +
////                "  'IC9QYWdlcyAyIDAgUgo+PgplbmRvYmoKCjIgMCBvYmoKPDwKICAvVHlwZSAvUGFnZXMKICAv' +\n" +
////                "  'TWVkaWFCb3ggWyAwIDAgMjAwIDIwMCBdCiAgL0NvdW50IDEKICAvS2lkcyBbIDMgMCBSIF0K' +\n" +
////                "  'Pj4KZW5kb2JqCgozIDAgb2JqCjw8CiAgL1R5cGUgL1BhZ2UKICAvUGFyZW50IDIgMCBSCiAg' +\n" +
////                "  'L1Jlc291cmNlcyA8PAogICAgL0ZvbnQgPDwKICAgICAgL0YxIDQgMCBSIAogICAgPj4KICA+' +\n" +
////                "  'PgogIC9Db250ZW50cyA1IDAgUgo+PgplbmRvYmoKCjQgMCBvYmoKPDwKICAvVHlwZSAvRm9u' +\n" +
////                "  'dAogIC9TdWJ0eXBlIC9UeXBlMQogIC9CYXNlRm9udCAvVGltZXMtUm9tYW4KPj4KZW5kb2Jq' +\n" +
////                "  'Cgo1IDAgb2JqICAlIHBhZ2UgY29udGVudAo8PAogIC9MZW5ndGggNDQKPj4Kc3RyZWFtCkJU' +\n" +
////                "  'CjcwIDUwIFRECi9GMSAxMiBUZgooSGVsbG8sIHdvcmxkISkgVGoKRVQKZW5kc3RyZWFtCmVu' +\n" +
////                "  'ZG9iagoKeHJlZgowIDYKMDAwMDAwMDAwMCA2NTUzNSBmIAowMDAwMDAwMDEwIDAwMDAwIG4g' +\n" +
////                "  'CjAwMDAwMDAwNzkgMDAwMDAgbiAKMDAwMDAwMDE3MyAwMDAwMCBuIAowMDAwMDAwMzAxIDAw' +\n" +
////                "  'MDAwIG4gCjAwMDAwMDAzODAgMDAwMDAgbiAKdHJhaWxlcgo8PAogIC9TaXplIDYKICAvUm9v' +\n" +
////                "  'dCAxIDAgUgo+PgpzdGFydHhyZWYKNDkyCiUlRU9G";
//        String jsScript = "// atob() is used to convert base64 encoded PDF to binary-like data.\n" +
//                "// (See also https://developer.mozilla.org/en-US/docs/Web/API/WindowBase64/\n" +
//                "// Base64_encoding_and_decoding.)\n" +
//                "var pdfData = atob('" + base64 + "');\n" +
//                "\n" +
//                "// Loaded via <script> tag, create shortcut to access PDF.js exports.\n" +
//                "var pdfjsLib = window['pdfjs-dist/build/pdf'];\n" +
//                "\n" +
//                "// The workerSrc property shall be specified.\n" +
//                "pdfjsLib.GlobalWorkerOptions.workerSrc = 'https://mozilla.github.io/pdf.js/build/pdf.worker.js';\n" +
//                "\n" +
//                "// Using DocumentInitParameters object to load binary data.\n" +
//                "var loadingTask = pdfjsLib.getDocument({data: pdfData});\n" +
//                "loadingTask.promise.then(function(pdf) {\n" +
//                "  console.log('PDF loaded');\n" +
//                "  \n" +
//                "  // Fetch the first page\n" +
//                "  var pageNumber = 1;\n" +
//                "  pdf.getPage(pageNumber).then(function(page) {\n" +
//                "    console.log('Page loaded');\n" +
//                "    \n" +
//                "    var scale = 1.5;\n" +
//                "    var viewport = page.getViewport({scale: scale});\n" +
//                "\n" +
//                "    // Prepare canvas using PDF page dimensions\n" +
//                "    var canvas = document.getElementById('the-canvas');\n" +
//                "    var context = canvas.getContext('2d');\n" +
//                "    canvas.height = viewport.height;\n" +
//                "    canvas.width = viewport.width;\n" +
//                "\n" +
//                "    // Render PDF page into canvas context\n" +
//                "    var renderContext = {\n" +
//                "      canvasContext: context,\n" +
//                "      viewport: viewport\n" +
//                "    };\n" +
//                "    var renderTask = page.render(renderContext);\n" +
//                "    renderTask.promise.then(function () {\n" +
//                "      console.log('Page rendered');\n" +
//                "    });\n" +
//                "  });\n" +
//                "}, function (reason) {\n" +
//                "  // PDF loading error\n" +
//                "  console.error(reason);\n" +
//                "});\n";
//        String func = "function renderPDFJavaFX() {" + jsScript + "}";
//        String htmlScript = "<script src=\"https://mozilla.github.io/pdf.js/build/pdf.js\"></script>\n" +
//                "<script language='javascript'>\n" +
//                func +
//                "</script>\n" +
//                "\n" +
//                "<h1>PDF.js 'Hello, base64!' example</h1>\n" +
//                "\n" +
//                "<canvas id=\"the-canvas\"></canvas>";
////        engine.loadContent(HTML_TEST);
////        URL url = this.getClass().getResource("/HTML/index1.html");
////        engine.load(url.toString());
//        File f = new File("/home/tj/akautauto/src/main/resources/HTML/index1.html");
//        engine.load(f.toURI().toString());
////        engine.setJavaScriptEnabled(true);
////        engine.documentProperty().addListener(new ChangeListener<Document>() {
////            @Override
////            public void changed(ObservableValue<? extends Document> observable, Document oldValue, Document newValue) {
////                engine.executeScript("renderPDFJavaFX();");
////            }
////        });
//    }
//
//    private String USER_MANUAL_FILE_CONTENT = Utils.readFileContent("/home/tj/akautauto/src/main/resources/PDF/test.pdf");
//
//    private String HTML_TEST = "<!DOCTYPE html>\n" +
//            "<html lang=\"en>\n" +
//            "<head>\n" +
//            "<meta charset=\"utf-8\">\n" +
//            "<title>Embeded</title>\n" +
//            "<style>\n" +
//            "\t.container{padding: 30px;}\n" +
//            "</style>\n" +
//            "<head>\n" +
//            "<body>\n" +
//            "\t<div class=\"container\">\n" +
//            "\t\t<embed src=\"/home/tj/akautauto/src/main/resources/PDF/test.pdf#toolbar=0\" type=\"application/pdf\" width=\"100%\" height=\"600px\" />\n" +
//            "\t</div>\n" +
//            "</body>\n" +
//

        Stage stage = new Stage();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File("src/main/resources/help"));
        directoryChooser.showDialog(stage);


//        FileChooser fileChooser = new FileChooser();
//        fileChooser.setInitialDirectory(new File("src/main/resources/help"));
//        fileChooser.showOpenDialog(stage);
//
//        File directory = new File("src/main/resources/help");
//        File[] contents = directory.listFiles();
//        directory.o
//        for ( File f : contents) {
//            System.out.println(f.getAbsolutePath());
//        }

    }
}
