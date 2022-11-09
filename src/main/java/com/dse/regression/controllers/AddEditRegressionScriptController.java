package com.dse.regression.controllers;

import com.dse.environment.Environment;
import com.dse.exception.FunctionNodeNotFoundException;
import com.dse.guifx_v3.objects.DefaultTreeTableCell;
import com.dse.probe_point_manager.objects.CheckBoxTreeTableRowForProbePoint;
import com.dse.probe_point_manager.objects.TestCasesTreeItemForProbePoint;
import com.dse.regression.RegressionScriptManager;
import com.dse.regression.objects.RegressionScript;
import com.dse.testcase_manager.ITestCase;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.testcasescript.TestcaseSearch;
import com.dse.testcasescript.object.*;
import com.dse.logger.AkaLogger;
import com.dse.util.Utils;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class AddEditRegressionScriptController implements Initializable {
    private final static AkaLogger logger = AkaLogger.get(AddEditRegressionScriptController.class);

    private String type;
    private ListView<RegressionScript> listView;
    private List<ITestCase> oldTestCases;
    private Stage stage;
    private RegressionScript regressionScript;

    @FXML
    TextField tfName;
    @FXML
    TextArea taCommands;
    @FXML
    TreeTableView<ITestcaseNode> ttvTestCasesNavigator;

    @FXML
    TreeTableColumn<ITestcaseNode, String> colName;

    public void initialize(URL location, ResourceBundle resources) {
        // TestCasesTreeItemForProbePoint and CheckBoxTreeTableRowForProbePoint can be used here
        TestCasesTreeItemForProbePoint root = new TestCasesTreeItemForProbePoint(Environment.getInstance().getTestcaseScriptRootNode());
        ttvTestCasesNavigator.setRoot(root);
        ttvTestCasesNavigator.setRowFactory(param -> new CheckBoxTreeTableRowForProbePoint<>());

        colName.setCellFactory(param -> new DefaultTreeTableCell<>());
    }

    public static Stage getWindow(String type, RegressionScript regressionScript, ListView listView) {
        FXMLLoader loader = new FXMLLoader(Object.class.getResource("/FXML/regression_script/AddEditRegressionScript.fxml"));
        try {
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setScene(scene);

            AddEditRegressionScriptController controller = loader.getController();
            controller.setType(type);
            controller.setRegressionScript(regressionScript);
            controller.setListView(listView);
            controller.setStage(stage);
            controller.setOldTestCases(new ArrayList<>(regressionScript.getTestCases()));
            controller.loadContent();

            if (type.equals(TYPE_ADD)) {
                stage.setTitle("Add New Regression Script");
            } else if (type.equals(TYPE_EDIT)) {
                stage.setTitle("Edit Regression Script");
                controller.setTaCommands(Utils.readFileContent(regressionScript.getScriptFilePath()));
            } else {
                return null;
            }

            return stage;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void loadContent() {
        tfName.setText(regressionScript.getName());

        colName.setCellValueFactory((TreeTableColumn.CellDataFeatures<ITestcaseNode, String> param) -> {
//            return new SimpleStringProperty(param.getValue().getValue().toString());
            ITestcaseNode node = param.getValue().getValue();
            String name = "";
            if (node instanceof TestcaseRootNode) {
                name = new File(((TestcaseRootNode) node).getAbsolutePath()).getName();

            } else if (node instanceof TestSubprogramNode) {
                try {
                    name = ((TestSubprogramNode) node).getSimpleNameToDisplayInTestcaseView();
                } catch (FunctionNodeNotFoundException e) {
                    e.printStackTrace();
                }
                
            } else if (node instanceof TestUnitNode) {
                // name of unit node is sometimes too long, need to shorten it.
                name = ((TestUnitNode) node).getShortNameToDisplayInTestcaseTree();

            } else if (node instanceof TestNewNode) {
                List<ITestcaseNode> nameNodes = TestcaseSearch.searchNode(node, new TestNameNode());
                if (nameNodes.size() == 1) {
                    name = ((TestNameNode) nameNodes.get(0)).getName();
                    ITestCase testCase = TestCaseManager.getTestCaseByName(name);

                    if (regressionScript.getTestCases().contains(testCase)) {
                        ((CheckBoxTreeItem) param.getValue()).setSelected(true);
                    }

                    if (param.getValue() != null) {
                        ((CheckBoxTreeItem) param.getValue()).selectedProperty().addListener((observable, oldValue, newValue) -> {
                            if (oldValue != null && oldValue != newValue) {
                                List<ITestCase> testCases = new ArrayList<>();
                                addTestCases(testCases, ttvTestCasesNavigator.getRoot());
                                setTaCommands(RegressionScriptManager.getInstance().exportRegressionScriptToString(testCases));
                            }
                        });
                    }
                } else {
                    logger.debug("[Error] there are 2 TestNameNode in a test case");
                }
            }

            return new ReadOnlyStringWrapper(name);
        });
    }

    private void addTestCases(List<ITestCase> testCases, TreeItem<ITestcaseNode> treeItem) {
        if (treeItem instanceof CheckBoxTreeItem) {
            if (((CheckBoxTreeItem<ITestcaseNode>) treeItem).isSelected() || ((CheckBoxTreeItem<ITestcaseNode>) treeItem).isIndeterminate()) {
                if (treeItem.getValue() instanceof TestNewNode) {
                    TestNewNode testNewNode = (TestNewNode) treeItem.getValue();
                    List<ITestcaseNode> names = TestcaseSearch.searchNode(testNewNode, new TestNameNode());

                    if (names.size() == 1) {
                        String name = ((TestNameNode) names.get(0)).getName();
                        ITestCase testCase = TestCaseManager.getTestCaseByName(name);
                        // todo: add Compound TestCase
                        if (testCase != null) {
                            testCases.add(testCase);
                        }
                    }

                } else {
                    for (TreeItem<ITestcaseNode> item : treeItem.getChildren()) {
                        addTestCases(testCases, item);
                    }
                }
            }
        }
    }

    @FXML
    public void ok() {
        // save the regression script
        if (type.equals(TYPE_ADD)) {
            // add regression script to RegressionScriptManager
            // update GUI
            // export regression script to file
            // close Add/Edit regression script window

            List<ITestCase> testCases = new ArrayList<>();
            addTestCases(testCases, ttvTestCasesNavigator.getRoot());
            regressionScript.getTestCases().addAll(testCases);

            // add regression script to RegressionScriptManager
            RegressionScriptManager.getInstance().add(regressionScript);

            // update GUI
            listView.getItems().add(regressionScript);
            listView.refresh();

            // export regression script to file
            RegressionScriptManager.getInstance().exportRegressionScript(regressionScript);
            // close Add/Edit regression script window
            if (stage != null) {
                stage.close();
            }

        } else if (type.equals(TYPE_EDIT)) {
            // export regression script to file
            // close Add/Edit regression script window

            List<ITestCase> testCases = new ArrayList<>();
            addTestCases(testCases, ttvTestCasesNavigator.getRoot());
            regressionScript.getTestCases().clear();
            regressionScript.getTestCases().addAll(testCases);
            // export regression script to file
            RegressionScriptManager.getInstance().exportRegressionScript(regressionScript);
            // close Add/Edit regression script window
            if (stage != null) {
                stage.close();
            }
        }

    }

    @FXML
    public void cancel() {
        if (stage != null) {
            stage.close();
        }
    }

    public void setTaCommands(String content) {
        taCommands.setText(content);
    }

    public void setRegressionScript(RegressionScript regressionScript) {
        this.regressionScript = regressionScript;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private void setOldTestCases(List<ITestCase> oldTestCases) {
        this.oldTestCases = oldTestCases;
    }

    public void setListView(ListView<RegressionScript> listView) {
        this.listView = listView;
    }

    public String getType() {
        return type;
    }

    public ListView<RegressionScript> getListView() {
        return listView;
    }

    public List<ITestCase> getOldTestCases() {
        return oldTestCases;
    }

    public Stage getStage() {
        return stage;
    }

    public static final String TYPE_ADD = "ADD";
    public static final String TYPE_EDIT = "EDIT";
}
