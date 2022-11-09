package com.dse.guifx_v3.controllers.object.build_environment;

import com.dse.exception.OpenFileException;
import com.dse.guifx_v3.helps.Factory;
import com.dse.guifx_v3.helps.UIController;
import com.dse.util.Utils;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.util.Callback;

public class LocateSourceFilesCellFactory implements Callback<ListView<SourcePath>, ListCell<SourcePath>> {
    @Override
    public ListCell<SourcePath> call(ListView<SourcePath> param) {
        return new ListCell<SourcePath>() {
            private Image iconDefault = new Image(Factory.class.getResourceAsStream("/icons/directory.png"));
            private Image iconSearchDirectory = new Image(Factory.class.getResourceAsStream("/icons/subprogram.png"));
            private Image iconIncludeDirectory = new Image(Factory.class.getResourceAsStream("/icons/class.png"));
            private Image iconTypeHandleDirectory = new Image(Factory.class.getResourceAsStream("/icons/header.png"));
            @Override
            public void updateItem(SourcePath item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    if (!item.isExisted()) {
                        setTextFill(Color.RED);
                    } else {
                        setTextFill(Color.BLACK);
                    }
                    setText(item.toString());
                    updateIcon(item.getType());

                    // create context menu
                    ContextMenu contextMenu = new ContextMenu();
                    MenuItem miOpenDirectory = new MenuItem("Open directory");
                    MenuItem miSetSearchDirectory = new MenuItem("Set as Search directory");
                    MenuItem miSetTypeHandledDirectory = new MenuItem("Set as Type handled directory");
                    MenuItem miSetLibraryIncludeDirectory = new MenuItem("Set as Library include directory");

                    miOpenDirectory.setOnAction(event ->{
                        String path = item.getAbsolutePath();
                        try {
                            Utils.openFolderorFileOnExplorer(path);
                        } catch (OpenFileException e) {
                            UIController.showErrorDialog(e.getMessage(), "Open source code file",
                                    "Can not open source code file");
                        }
                    });
                    miSetSearchDirectory.setOnAction(event -> {
                        item.setType(SourcePath.SEARCH_DIRECTORY);
                        updateIcon(item.getType());
                    });
                    miSetTypeHandledDirectory.setOnAction(event -> {
                        item.setType(SourcePath.TYPE_HANDLED_DIRECTORY);
                        updateIcon(item.getType());
                    });
                    miSetLibraryIncludeDirectory.setOnAction(event -> {
                        item.setType(SourcePath.LIBRARY_INCLUDE_DIRECTORY);
                        updateIcon(item.getType());
                    });
                    contextMenu.getItems().addAll(miOpenDirectory, miSetSearchDirectory, miSetTypeHandledDirectory, miSetLibraryIncludeDirectory);
                    setContextMenu(contextMenu);
                }
            }
            private void updateIcon(String type) {
                ImageView imageView = new ImageView();
                switch (type) {
                    case SourcePath.LIBRARY_INCLUDE_DIRECTORY:
                        imageView.setImage(iconIncludeDirectory);
                        break;
                    case SourcePath.SEARCH_DIRECTORY:
                        imageView.setImage(iconSearchDirectory);
                        break;
                    case SourcePath.TYPE_HANDLED_DIRECTORY:
                        imageView.setImage(iconTypeHandleDirectory);
                        break;
                    case SourcePath.DEFAULT:
                        imageView.setImage(iconDefault);
                        break;
                    default:
                        System.out.println("Error");
                }
                setGraphic(imageView);
            }
        };
    }
}