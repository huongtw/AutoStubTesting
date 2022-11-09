package com.dse.debugger.controller;

import com.dse.debugger.component.variable.GDBTreeCellVar;
import com.dse.debugger.component.variable.GDBVar;
import com.dse.debugger.component.watches.WatchMode;
import com.dse.debugger.component.watches.WatchPoint;
import com.dse.logger.AkaLogger;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ResourceBundle;

public class WatchController implements Initializable {

    private static final AkaLogger logger = AkaLogger.get(WatchController.class);

    private static WatchController watchController = null;
    private static TitledPane titledPane = null;

    private static void prepare() {
        FXMLLoader loader = new FXMLLoader(WatchController.class.getResource("/FXML/debugger/Watches.fxml"));
        try {
            titledPane = loader.load();
            watchController = loader.getController();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static TitledPane getTitledPane() {
        if (titledPane == null) prepare();
        return titledPane;
    }

    public static WatchController getWatchController() {
        if (watchController == null) prepare();
        return watchController;
    }

    @FXML
    VBox column;

    @FXML
    Button addBtn;

    @FXML
    Button delBtn;

    @FXML
    Button upBtn;

    @FXML
    Button downBtn;

    @FXML
    Button dupBtn;

    @FXML
    ToolBar supportTools;

    @FXML
    TreeView<GDBVar> watchTree;

    private TreeItem<GDBVar> rootNode;

    private ArrayList<WatchPoint> watchList = new ArrayList<>();

    private int watchSize;

    private TextField addField = new TextField();

    private WatchMode watchMode;

    @Override
    public void initialize(URL location, ResourceBundle resource) {
        rootNode = new TreeItem<>();
        rootNode.setExpanded(true);
        watchTree.setRoot(rootNode);
        watchTree.setShowRoot(false);
        watchTree.setEditable(false);
        watchTree.setCellFactory(e -> new GDBTreeCellVar());

        delBtn.setDisable(true);
        upBtn.setDisable(true);
        downBtn.setDisable(true);
        dupBtn.setDisable(true);
        setUpTextField();
        checkNumberOfWatches(null);
        watchTree.getSelectionModel().selectedItemProperty().
                addListener((observable, oldValue, newValue) -> checkNumberOfWatches(getRootSelected()));

        watchSize = 0;
        watchMode = WatchMode.NORMAL;
    }

    private void changeMode(WatchMode mode) {
        watchMode = mode;
    }

    private void checkNumberOfWatches(TreeItem<GDBVar> selecting) {
        ObservableList<TreeItem<GDBVar>> children = rootNode.getChildren();
        int num = children.size();
        if (selecting == null) {
            delBtn.setDisable(true);
            upBtn.setDisable(true);
            downBtn.setDisable(true);
            dupBtn.setDisable(true);
            return;
        }
        if (num == 1) {
            delBtn.setDisable(false);
            upBtn.setDisable(true);
            downBtn.setDisable(true);
            dupBtn.setDisable(false);
        }
        if (num > 1) {
            delBtn.setDisable(false);
            dupBtn.setDisable(false);
            int idx = rootNode.getChildren().indexOf(selecting);
            if (idx == 0) {
                downBtn.setDisable(false);
                upBtn.setDisable(true);
            } else if (idx == num - 1) {
                downBtn.setDisable(true);
                upBtn.setDisable(false);
            } else {
                downBtn.setDisable(false);
                upBtn.setDisable(false);
            }
        }
    }

    private TreeItem<GDBVar> getSelected() {
        return watchTree.getSelectionModel().getSelectedItem();
    }

    private TreeItem<GDBVar> getRootSelected() {
        TreeItem<GDBVar> current = getSelected();
        if (current == null)
            return null;
        while (current.getParent() != null && current.getParent().getValue() != null && current.getParent() != rootNode) {
            current = current.getParent();
        }
        return current;
    }

    private void setUpTextField() {
        addField.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.ENTER) {
                        String text = addField.getText();
                        column.getChildren().remove(1);
                        add(text);
                    }
                }
        );
    }

    private void add(String exp) {
        if (watchMode == WatchMode.ADVANCE) {
            addWatchPoint(exp);
        } else {
            addNormalWatchPoint(exp);
        }
    }

    /**
     * Add watch point that make program stop when value of watch point change
     *
     * @param exp expression
     */
    private void addWatchPoint(String exp) {
        TreeItem<GDBVar> item = DebugController.getDebugController().getGdb().addNewWatch(exp);
        if (item != null) {
            TreeItem<GDBVar> selecting = getRootSelected();
            int idx = 0;
            if (selecting != null) {
                idx = rootNode.getChildren().indexOf(selecting);
            }
            watchList.add(idx, item.getValue().getWatchPoint());
            rootNode.getChildren().add(idx, item);
            watchTree.getSelectionModel().select(item);
            checkNumberOfWatches(item);
        }
    }

    /**
     * Add normal watch point to show variables
     *
     * @param exp expression
     */
    private void addNormalWatchPoint(String exp) {
        TreeItem<GDBVar> item = DebugController.getDebugController().getGdb().addNormalWatch(exp);
        if (item != null && item.getValue().getType() != null) {
            TreeItem<GDBVar> selecting = getRootSelected();
            int idx = 0;
            if (selecting != null && item.getValue().getType() == null) {
                idx = rootNode.getChildren().indexOf(selecting);
            }
            WatchPoint watchPoint = new WatchPoint(exp, String.valueOf(watchSize + 1));
            item.getValue().setWatchPoint(watchPoint);
            watchList.add(idx, watchPoint);
            rootNode.getChildren().add(idx, item);
            watchTree.getSelectionModel().select(item);
            checkNumberOfWatches(item);
        }
    }

    public void showTextField() {
        if (column.getChildren().size() == 2) {
            column.getChildren().add(1, addField);
            addField.setText(null);
            addField.selectAll();
            addField.requestFocus();
        }
    }

    public void deleteWatchPoint() {
        TreeItem<GDBVar> selecting = getRootSelected();
        ObservableList<TreeItem<GDBVar>> children = rootNode.getChildren();
        if (selecting != null) {
            if (watchMode == WatchMode.ADVANCE) {
                boolean isDeleted = DebugController.getDebugController().getGdb().deletePoint(selecting.getValue().getWatchPoint().getNumber());
                if (isDeleted) {
                    logger.debug("Deleting watches point");
                    children.remove(selecting);
                    watchList.remove(selecting.getValue().getWatchPoint());
                }
            } else if (watchMode == WatchMode.NORMAL) {
                boolean isDeleted = DebugController.getDebugController().getGdb().deleteVariable(selecting.getValue());
                if (isDeleted) {
                    logger.debug("Deleting normal watch point");
                    children.remove(selecting);
                    watchList.remove(selecting.getValue().getWatchPoint());
                }
            }
        }
    }

    public void moveUp() {
        TreeItem<GDBVar> selecting = getRootSelected();
        if (selecting != null) {
            int idx = rootNode.getChildren().indexOf(selecting);
            if (idx > 0) {
                TreeItem<GDBVar> safeItem = rootNode.getChildren().get(idx - 1);
                rootNode.getChildren().set(idx - 1, selecting);
                rootNode.getChildren().set(idx, safeItem);
                watchTree.getSelectionModel().select(selecting);
                Collections.swap(watchList, idx, idx - 1);
            }
        }
    }

    public void moveDown() {
        TreeItem<GDBVar> selecting = getRootSelected();
        if (selecting != null) {
            int idx = rootNode.getChildren().indexOf(selecting);
            if (idx < rootNode.getChildren().size() - 1) {
                TreeItem<GDBVar> safeItem = rootNode.getChildren().get(idx + 1);
                rootNode.getChildren().set(idx + 1, selecting);
                rootNode.getChildren().set(idx, safeItem);
                watchTree.getSelectionModel().select(selecting);
                Collections.swap(watchList, idx, idx + 1);
            }
        }
    }

    public void duplicateWatchPoint() {
        String exp;
        TreeItem<GDBVar> selecting = getRootSelected();
        if (selecting == null) {
            logger.debug("No selecting item to duplicate");
            return;
        }
        WatchPoint watchPoint = selecting.getValue().getWatchPoint();
        if (watchPoint == null) {
            logger.debug("No watch point in this item");
            return;
        }
        exp = watchPoint.getExp();
        add(exp);
    }

    public void updateWatches() {
        ArrayList<TreeItem<GDBVar>> updatedWatches = new ArrayList<>();
        watchList.forEach(watchPoint -> {
            TreeItem<GDBVar> item = DebugController.getDebugController().getGdb().getTreeItemVarWithExp(watchPoint.getExp());
            watchPoint.setNull(false);
            if (item == null) {
                if (watchMode == WatchMode.NORMAL) {
                    watchPoint.setNull(true);
                    GDBVar tempVar = new GDBVar();
                    tempVar.setRealName(watchPoint.getExp());
                    item = new TreeItem<>(tempVar);
                    item.setValue(tempVar);
                } else if (watchMode == WatchMode.ADVANCE) {
                    // todo: handle in the future
                    return;
                }
            }
            item.getValue().setWatchPoint(watchPoint);
            updatedWatches.add(item);
        });
        clearAll();
        rootNode.getChildren().addAll(updatedWatches);
        updatedWatches.forEach(item -> watchList.add(item.getValue().getWatchPoint()));
    }

    public void showButton(boolean isDisable) {
        supportTools.setDisable(isDisable);
    }

    public void clearAll() {
        watchList.clear();
        rootNode.getChildren().clear();
        watchTree.getSelectionModel().clearSelection();
    }
}
