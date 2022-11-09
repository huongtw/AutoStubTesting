package com.dse.guifx_v3.controllers.main_view;

import com.dse.exception.OpenFileException;
import com.dse.guifx_v3.controllers.TestCasesNavigatorController;
import com.dse.guifx_v3.helps.UIController;
import com.dse.guifx_v3.helps.UILogger;
import com.dse.guifx_v3.objects.TreeNode;
import com.dse.parser.object.*;
import com.dse.testcasescript.object.ITestcaseNode;
import com.dse.util.Utils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.layout.AnchorPane;
import com.dse.logger.AkaLogger;

import java.net.URL;
import java.util.ResourceBundle;

public class LeftPaneController implements Initializable {
    private final static AkaLogger logger = AkaLogger.get(LeftPaneController.class);

    /**
     * Singleton patern like
     */
    private static AnchorPane leftPane = null;
    private static LeftPaneController leftPaneController = null;

    private static void prepare() {
        FXMLLoader loader = new FXMLLoader(Object.class.getResource("/FXML/LeftPane.fxml"));
        try {
            Parent parent = loader.load();
            leftPane = (AnchorPane) parent;
            leftPaneController = loader.getController();
        } catch (Exception e) {
             e.printStackTrace();
        }
    }

    public static AnchorPane getLeftPane() {
        if (leftPane == null) {
            prepare();
        }
        return leftPane;
    }

    public static LeftPaneController getLeftPaneController() {
        if (leftPaneController == null) {
            prepare();
        }
        return leftPaneController;
    }

    @FXML
    private TabPane topTabPane;
    @FXML
    private Tab tabTestCases;
    @FXML
    private Tab tabProjectStructure;
    @FXML
    private AnchorPane projectStructure;
//    @FXML
//    private ListView<String> messagesArea;
    @FXML
    private TestCasesNavigatorController navigatorController;

    public void initialize(URL location, ResourceBundle resources) {
//        messagesArea.setItems(UILogger.getLogs());
        navigatorController = TestCasesNavigatorController.getInstance();
        tabTestCases.setContent(TestCasesNavigatorController.getNavigatorTreeTable());
    }

    public void renderProjectTree(IProjectNode current_project_root) {
//        current_project_root.setName(new File(Paths.CURRENT_PROJECT.ORIGINAL_PROJECT_PATH).getName());
        TreeNode root = new TreeNode(current_project_root);
        TreeView<String> projectTree = new TreeView<>(root);

        projectTree.setCellFactory(param -> new MyTreeCell());

        projectStructure.getChildren().add(projectTree);
        AnchorPane.setTopAnchor(projectTree, 0.0);
        AnchorPane.setBottomAnchor(projectTree, 0.0);
        AnchorPane.setLeftAnchor(projectTree, 0.0);
        AnchorPane.setRightAnchor(projectTree, 0.0);

        topTabPane.getSelectionModel().select(tabProjectStructure);
    }

    public void renderNavigator(ITestcaseNode testcaseNode) {
        navigatorController.loadContent(testcaseNode);
    }

    public void clear() {
        projectStructure.getChildren().clear();
        navigatorController.clear();
    }

    // For context menu of project tree (when right click)
    private class MyTreeCell extends TextFieldTreeCell<String> {
        INode node = null;

        @Override
        public void updateItem(String itemName, boolean empty) {
            super.updateItem(itemName, empty);
            if (getTreeItem() != null)
                node = ((TreeNode) getTreeItem()).getItem();

            if (node != null) {
                // initialize popup
                setContextMenu(new ContextMenu());

                // add options to popup
                addOpenTheLocationOption(node);
                addViewSourceCodeOptionOnTool(node);
                addViewSourceCodeOptionOnExplorer(node);
                viewDependency(node);
            } else {
                setContextMenu(null);
            }
        }

        private void addOpenTheLocationOption(INode node) {
            assert (node != null);

            if (node instanceof FolderNode || node instanceof ProjectNode) {
                MenuItem view_location = new MenuItem("Open the location");
                view_location.setOnAction(event -> {
                    if (node != null) {
                        UILogger.getUiLogger().logToBothUIAndTerminal("Opening the location of " + node.getName() + " [" + node.getClass().getSimpleName() + "]");
                        try {
                            UIController.openTheLocation(node);
                        } catch (OpenFileException e) {
                            UIController.showErrorDialog(e.getMessage(), "Open folder",
                                    "Can not open folder");
                        } catch (Exception e) {
                            UIController.showErrorDialog("Error code " + e.getMessage(), "Open folder",
                                    "Can not open folder");
                        }
                    } else {
                        UIController.showErrorDialog("The location does not exist", "Open folder",
                                "Can not open folder");

                    }
                });
                getContextMenu().getItems().add(view_location);
            }
        }

        private void addViewSourceCodeOptionOnExplorer(INode node) {
            assert (node != null);

            if (node instanceof ISourcecodeFileNode || node instanceof UnknowObjectNode) {
                MenuItem viewSourceCode = new MenuItem("Open the source code on explorer");
                viewSourceCode.setOnAction(event -> {
                    UILogger.getUiLogger().logToBothUIAndTerminal("Opening the source code of " + node.getName() + " [" + node.getClass().getSimpleName() + "] on explorer");
                    try {
                        UIController.openTheLocation(node);
                    } catch (OpenFileException e) {
                        UIController.showErrorDialog(e.getMessage(), "Open source code file",
                                "Can not open source code file");
                    }
                });
                getContextMenu().getItems().add(viewSourceCode);
            }
        }

        private void viewDependency(INode node) {
            if (node == null)
                return;
            MenuItem viewDependency = new MenuItem("View dependency");
            getContextMenu().getItems().add(viewDependency);
            viewDependency.setOnAction(event -> {
                Utils.viewDependency(node);
            });
        }
        private void addViewSourceCodeOptionOnTool(INode node) {
            assert (node != null);

            if (node instanceof ICommonFunctionNode || node instanceof StructureNode ||
                    node instanceof NamespaceNode || node instanceof ISourcecodeFileNode) {

                MenuItem view_source_code = new MenuItem("View Source Code on this tool");
                view_source_code.setOnAction(event -> {
                        UILogger.getUiLogger().logToBothUIAndTerminal("Opening source code of " + node.getName() + " [" + node.getClass().getSimpleName() + "] on this tool");
                    try {
                        UIController.viewSourceCode(node);
                    } catch (Exception e){
                        UIController.showErrorDialog("Error code: " + e.getMessage(), "Open source code file",
                                "Can not open source code file");
                    }
                });
                getContextMenu().getItems().add(view_source_code);
            }
        }
    }
}
