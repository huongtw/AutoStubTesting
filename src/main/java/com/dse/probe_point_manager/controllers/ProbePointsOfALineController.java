package com.dse.probe_point_manager.controllers;

import com.dse.config.WorkspaceConfig;
import com.dse.guifx_v3.controllers.main_view.MDIWindowController;
import com.dse.guifx_v3.helps.UIController;
import com.dse.parser.object.*;
import com.dse.probe_point_manager.ProbePointManager;
import com.dse.probe_point_manager.ProbePointUtils;
import com.dse.probe_point_manager.objects.ProbePoint;
import com.dse.search.Search;
import com.dse.search.condition.FunctionNodeCondition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.fxmisc.richtext.CodeArea;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ProbePointsOfALineController implements Initializable {
    @FXML
    private ListView<ProbePoint> lvExistedProbePoints;

    private Stage stage;
    private CodeArea area;
    private ISourcecodeFileNode sourcecodeFileNode;
    private int lineInSourceCodeFile;
    private ProbePointsOfALineController controller;
    private IFunctionNode functionNode;
    private int lineInFunction;

    public static ProbePointsOfALineController getInstance(ISourcecodeFileNode sourcecodeFileNode, int lineNumber, CodeArea area) {
        FXMLLoader loader = new FXMLLoader(Object.class.getResource("/FXML/probe_point/ExistedProbePointsOfALineManage.fxml"));
        try {
            Parent parent = loader.load();
            ProbePointsOfALineController controller = loader.getController();
            Scene scene = new Scene(parent);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Manage probe points at a line");

            controller.setStage(stage);
            controller.setSourcecodeFileNode(sourcecodeFileNode);
            controller.setArea(area);
            controller.setLineInSourceCodeFile(lineNumber);
            controller.setUpFunctionNode();
            controller.loadContent();

            return controller;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void setUpFunctionNode() {
        List<INode> listNode = Search.searchNodes(sourcecodeFileNode, new FunctionNodeCondition());
        for (INode node : listNode) {
            if (node instanceof IFunctionNode) {
                IFunctionNode tempNode = (IFunctionNode) node;
                IASTFileLocation location = ((CustomASTNode) tempNode).getAST().getFileLocation();
                int start = location.getStartingLineNumber();
                int end = location.getEndingLineNumber();
                if (start <= lineInSourceCodeFile && end >= lineInSourceCodeFile) {
                    this.functionNode = tempNode;
                    this.lineInFunction = lineInSourceCodeFile - start;
                    return;
                }
            }
        }
    }

    public void initialize(URL location, ResourceBundle resources) {
        lvExistedProbePoints.setCellFactory(param -> new ListCell<ProbePoint>() {
            @Override
            protected void updateItem(ProbePoint item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setGraphic(null);
                    setText(null);
                } else if (item.getName() != null) {
                    setText(item.getName());
                    ContextMenu contextMenu = new ContextMenu();
                    setContextMenu(contextMenu);

                    addEditProbePoint(item);
                    addDeleteProbePoint(item);
                }
            }

            private void addEditProbePoint(ProbePoint item) {
                MenuItem mi = new MenuItem("Edit");
                mi.setOnAction(event -> {
                    if (item != null) {
                        Stage window = AddEditProbePointController.getWindow(AddEditProbePointController.TYPE_EDIT, item, lvExistedProbePoints);
                        if (window != null) {
                            window.setResizable(false);
                            window.initModality(Modality.WINDOW_MODAL);
                            window.initOwner(UIController.getPrimaryStage().getScene().getWindow());
                            window.show();
                        }
                    }
                });
                getContextMenu().getItems().add(mi);
            }

            private void addDeleteProbePoint(ProbePoint item) {
                MenuItem mi = new MenuItem("Delete");
                mi.setOnAction(event -> {
//                    if (ProbePointUtils.deleteProbePointInFile(item)) {
//                        lvExistedProbePoints.getItems().remove(item);
//                        ProbePointManager.getInstance().remove(item);
//                        MDIWindowController.getMDIWindowController().updateLVProbePoints();
//                        lvExistedProbePoints.refresh();
//                    }
                    ProbePointUtils.deleteProbePointInFile(item);
                    lvExistedProbePoints.getItems().remove(item);
                    ProbePointManager.getInstance().remove(item);
                    MDIWindowController.getMDIWindowController().updateLVProbePoints();
                    lvExistedProbePoints.refresh();
                });
                getContextMenu().getItems().add(mi);
            }

        });
    }

    private void loadContent() {
        List<ProbePoint> probePoints = ProbePointManager.getInstance().searchProbePoints(sourcecodeFileNode, lineInSourceCodeFile);
        for (ProbePoint pp : probePoints) {
            lvExistedProbePoints.getItems().add(pp);
        }
    }

    public void setArea(CodeArea area) {
        this.area = area;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void newProbePoint() {
        ProbePoint probePoint = ProbePoint.getNewRandomNameProbePoint();
        probePoint.setContent(area.getParagraph(lineInSourceCodeFile).getText());
        probePoint.setSourcecodeFileNode(sourcecodeFileNode);
        probePoint.setLineInSourceCodeFile(lineInSourceCodeFile);
        probePoint.setFunctionNode(functionNode);
        probePoint.setLineInFunctionNode(lineInFunction);
        probePoint.setPath((new WorkspaceConfig().fromJson().getProbePointDirectory()) + File.separator + probePoint.getName() + ".pp");

        Stage window = AddEditProbePointController.getWindow(AddEditProbePointController.TYPE_ADD, probePoint, lvExistedProbePoints);
        if (window != null) {
            window.setResizable(false);
            window.initModality(Modality.WINDOW_MODAL);
            window.initOwner(this.stage.getScene().getWindow());
            window.show();
        }
    }

    @FXML
    public void editProbePoint() {
        ProbePoint probePoint = lvExistedProbePoints.getSelectionModel().getSelectedItem();

        if (probePoint != null) {
            Stage window = AddEditProbePointController.getWindow(AddEditProbePointController.TYPE_EDIT, probePoint, lvExistedProbePoints);
            if (window != null) {
                window.setResizable(false);
                window.initModality(Modality.WINDOW_MODAL);
                window.initOwner(stage.getScene().getWindow());
                window.show();
            }
        }
    }

    @FXML
    public void deleteProbePoint() {
        ProbePoint probePoint = lvExistedProbePoints.getSelectionModel().getSelectedItem();

        if (probePoint != null) {
//            if (ProbePointUtils.deleteProbePointInFile(probePoint)) {
//                lvExistedProbePoints.getItems().remove(probePoint);
//                ProbePointManager.getInstance().remove(probePoint);
//                MDIWindowController.getMDIWindowController().updateLVProbePoints();
//            } else {
//                //todo : delete in file failed
//            }
            ProbePointUtils.deleteProbePointInFile(probePoint);
            lvExistedProbePoints.getItems().remove(probePoint);
            ProbePointManager.getInstance().remove(probePoint);
            MDIWindowController.getMDIWindowController().updateLVProbePoints();
        }
    }

    public void setLineInSourceCodeFile(int lineInSourceCodeFile) {
        this.lineInSourceCodeFile = lineInSourceCodeFile;
    }

    public void setSourcecodeFileNode(ISourcecodeFileNode sourcecodeFileNode) {
        this.sourcecodeFileNode = sourcecodeFileNode;
    }

    @FXML
    public void save() {
        stage.fireEvent(
                new WindowEvent(
                        stage,
                        WindowEvent.WINDOW_CLOSE_REQUEST
                )
        );
    }

    @FXML
    public void saveAndCheck() {
        stage.close();
    }
}
