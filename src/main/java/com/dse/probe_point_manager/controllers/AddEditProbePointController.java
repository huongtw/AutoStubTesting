package com.dse.probe_point_manager.controllers;

import com.dse.exception.FunctionNodeNotFoundException;
import com.dse.guifx_v3.controllers.main_view.MDIWindowController;
import com.dse.environment.Environment;
import com.dse.guifx_v3.helps.UIController;
import com.dse.guifx_v3.objects.DefaultTreeTableCell;
import com.dse.probe_point_manager.ProbePointManager;
import com.dse.probe_point_manager.ProbePointUtils;
import com.dse.probe_point_manager.objects.CheckBoxTreeTableRowForProbePoint;
import com.dse.probe_point_manager.objects.ProbePoint;
import com.dse.probe_point_manager.objects.TestCasesTreeItemForProbePoint;
import com.dse.project_init.ProjectClone;
import com.dse.testcase_manager.TestCase;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class AddEditProbePointController implements Initializable {

    private final static AkaLogger logger = AkaLogger.get(AddEditProbePointController.class);

    private String type;
    private ProbePoint probePoint;
    private ListView<ProbePoint> listView;
    private List<TestCase> oldTestCases;
    private Stage stage;

    @FXML
    TextField tfName;
    @FXML
    TextField tfContent;
    @FXML
    TextArea taBefore;
    @FXML
    TextArea taAfter;
    @FXML
    TreeTableView<ITestcaseNode> ttvTestCasesNavigator;

    @FXML
    TreeTableColumn<ITestcaseNode, String> colName;
    @FXML
    TreeTableColumn<ITestcaseNode, String> colIsCheck;

    public void initialize(URL location, ResourceBundle resources) {
        TestCasesTreeItemForProbePoint root = new TestCasesTreeItemForProbePoint(Environment.getInstance().getTestcaseScriptRootNode());
        ttvTestCasesNavigator.setRoot(root);
        ttvTestCasesNavigator.setRowFactory(param -> new CheckBoxTreeTableRowForProbePoint<>());

        colName.setCellFactory(param -> new DefaultTreeTableCell<>());

    }

    public static Stage getWindow(String type, ProbePoint probePoint, ListView listView) {
        FXMLLoader loader = new FXMLLoader(Object.class.getResource("/FXML/probe_point/AddEditProbePoint.fxml"));
        try {
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setScene(scene);
            if (type.equals(TYPE_ADD)) {
                stage.setTitle("Add New Probe Point");
            } else if (type.equals(TYPE_EDIT)) {
                stage.setTitle("Edit Probe Point");
            } else {
                return null;
            }

            AddEditProbePointController controller = loader.getController();
            controller.setType(type);
            controller.setProbePoint(probePoint);
            controller.setListView(listView);
            controller.setStage(stage);
            controller.setOldTestCases(new ArrayList<>(probePoint.getTestCases()));
            controller.loadContent();
            return stage;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void loadContent() {
        tfName.setText(probePoint.getName());
        tfName.setEditable(false);
        tfContent.setText(probePoint.getContent());
        String afterCode = readData(probePoint.getAfter());
        String beforeCode = readData(probePoint.getBefore());
        taBefore.setText(beforeCode);
        taAfter.setText(afterCode);
        tfContent.setText(probePoint.getContent());
        tfContent.setEditable(false);

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
                    TestCase testCase = TestCaseManager.getBasicTestCaseByName(name);
                    if (probePoint.getTestCases().contains(testCase)) {
                        ((CheckBoxTreeItem) param.getValue()).setSelected(true);
                    }

                } else {
                    logger.debug("[Error] there are 2 TestNameNode in a test case");
                }
            }

            return new ReadOnlyStringWrapper(name);
        });

    }

    public void setType(String type) {
        this.type = type;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setProbePoint(ProbePoint probePoint) {
        this.probePoint = probePoint;
    }

    public void setListView(ListView<ProbePoint> listView) {
        this.listView = listView;
    }

    public void setOldTestCases(List<TestCase> oldTestCases) {
        this.oldTestCases = oldTestCases;
    }

    private void addTestCases(List<TestCase> testCases, TreeItem<ITestcaseNode> treeItem) {
        if (treeItem instanceof CheckBoxTreeItem) {
            if (((CheckBoxTreeItem<ITestcaseNode>) treeItem).isSelected() || ((CheckBoxTreeItem<ITestcaseNode>) treeItem).isIndeterminate()) {
                if (treeItem.getValue() instanceof TestNewNode) {
                    TestNewNode testNewNode = (TestNewNode) treeItem.getValue();
                    List<ITestcaseNode> names = TestcaseSearch.searchNode(testNewNode, new TestNameNode());

                    if (names.size() == 1) {
                        String name = ((TestNameNode) names.get(0)).getName();
                        TestCase testCase = TestCaseManager.getBasicTestCaseByName(name);
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
        // save the probe point
        if (type.equals(TYPE_ADD)) {
            // must add to function map before check valid
            ProbePointManager.getInstance().addToFunctionMap(probePoint);
            if (isValid()) {
                // add probe point to ProbePointManager
                ProbePointManager.getInstance().add(probePoint); // sure to be true because if not, the isValid is return false

                // update GUI
                listView.getItems().add(probePoint);
                MDIWindowController.getMDIWindowController().updateLVProbePoints();
                listView.refresh();

                // export probe point's data to file info.json
                ProbePointManager.getInstance().exportProbePointToFile(probePoint);
                ProbePointManager.getInstance().updateTestCasesPPMapAfterAdd(probePoint);

                if (probePoint.getTestCases().size() > 0) { // probe points that has no testcases are not need to be included
//                    // generate before.probe, after.probe with #if define TEST_CASE_ID
//                    genIncludeFiles();
                    // insert "#includes of before, after of the probe point
                    ProbePointUtils.insertIncludes(probePoint);
                }

                // if only isValid then close windows
                // close "add/edit probe point" windows
                if (stage != null) {
                    this.stage.close();
                }

            } else {
                // delete from function map if not valid
                ProbePointManager.getInstance().removeFromFunctionMap(probePoint);
            }
        } else if (type.equals(TYPE_EDIT)) {
            if (isValid()){
                // probe points that has no testcases are not need to be included
                if (oldTestCases.size() == 0 && probePoint.getTestCases().size() > 0) {
                    // insert "#includes of before, after of the probe point
                    ProbePointUtils.insertIncludes(probePoint);
                } else if (oldTestCases.size() > 0 && probePoint.getTestCases().size() == 0) {
                    // remove "#includes of before, after of the probe point
                    String filePath = ProjectClone.getClonedFilePath(probePoint.getSourcecodeFileNode().getAbsolutePath());
                    ProbePointUtils.removeIncludes(probePoint);
                    // check after remove, whether the remove incorrect or not
                    if (!ProbePointUtils.checkCompile(probePoint.getTestCases(), filePath, false)) {
                        logger.error("The akaignore file is incorrect after remove temporary includes");
                    }
                }

                // update probe point's data in file info.json
                ProbePointManager.getInstance().exportProbePointToFile(probePoint);
                ProbePointManager.getInstance().updateTestCasesPPMapAfterEdit(probePoint, oldTestCases, probePoint.getTestCases());

                // if only isValid then close windows
                // close "add/edit probe point" windows
                if (stage != null) {
                    this.stage.close();
                }
            } else {
                // do nothing
            }
        }
    }

    @FXML
    public void cancel() {
        if (stage != null) {
            if (type.equals(TYPE_ADD)) {
                ProbePointManager.getInstance().deleteProbePointFile(probePoint);
            } else if (type.equals(TYPE_EDIT)) {
                // todo: handle cancel edition
                // maybe backup and restore
            }
            this.stage.close();
        }
    }

    @FXML
    public void check() {
        if (type.equals(TYPE_ADD)) {
            // must add to function map before check valid
            ProbePointManager.getInstance().addToFunctionMap(probePoint);
            isValid();
            ProbePointManager.getInstance().removeFromFunctionMap(probePoint);
        } else {
            isValid();
        }

    }

    private boolean isValid() {
        boolean isValid = false;
        if (checkApplyProbePointsAtOneLocation()) {
            if (checkCompilable()) {
                UIController.showSuccessDialog("The probe point is valid", "Probe point valid", "Validated");
                isValid = true;
            } else {
//                UIController.showErrorDialog("Can not compile source code with the probe point", "Compile error", "Create/Edit probe point failed");
            }
        } else {
            UIController.showErrorDialog("A testcase apply more than one probe point at one line", "Test case apply error", "Create/Edit probe point failed");
        }
        return isValid;
    }

    // check if there are any testcase apply more than one probe point at a line
    private boolean checkApplyProbePointsAtOneLocation() {
        List<TestCase> testCases = new ArrayList<>();
        addTestCases(testCases, ttvTestCasesNavigator.getRoot());
        boolean isValid = true;
        for (TestCase testCase : testCases) {
            // find all probe points that testCase applied
            List<ProbePoint> probePoints = ProbePointManager.getInstance().getProbePointsByTestCase(testCase);
            for (ProbePoint p : probePoints) {
                if (p != probePoint && p.getLineInSourceCodeFile() == probePoint.getLineInSourceCodeFile()) {
                    logger.error("A testcase apply more than one probe point at one line.");
                    isValid = false;
                    break;
                }
            }
        }

        if (isValid) {
            logger.debug("The probe point is valid.");
        }
        return isValid;
    }

    // check if source code with the probe point is compilable
    private boolean checkCompilable() {
        boolean isCompilable = false;
        // set Stage for showing "failed to compile" popup window as need
        ProbePointUtils.setAddEditProbePointControllerStage(this.stage);
        List<TestCase> testCases = new ArrayList<>();
        addTestCases(testCases, ttvTestCasesNavigator.getRoot());

        probePoint.getTestCases().clear();
        probePoint.getTestCases().addAll(testCases);
        // generate/update include files that contain data of before, after of probe point
        logger.debug("Generate/update include files of probe point");
        genIncludeFiles();

        if (type.equals(TYPE_ADD)) {
            if (!ProbePointManager.getInstance().isExisted(probePoint)) {
                // insert "#includes of before, after of the probe point
                logger.debug("Insert includes line of before, after of probe point");
                ProbePointUtils.insertIncludes(probePoint);

                // compile, if success then change isCompilable to true
                String filePath = ProjectClone.getClonedFilePath(probePoint.getSourcecodeFileNode().getAbsolutePath());
                if (ProbePointUtils.checkCompile(probePoint.getTestCases(), filePath, true)) {
                    isCompilable = true;
                }

                // remove "#includes of before, after of the probe point
                logger.debug("remove includes line of before, after of probe point");
                ProbePointUtils.removeIncludes(probePoint);
                // check after remove, whether the remove incorrect or not
                if (!ProbePointUtils.checkCompile(probePoint.getTestCases(), filePath, false)) {
                    logger.error("The akaignore file is incorrect after remove temporary includes");
                }
            }
        } else if (type.equals(TYPE_EDIT)) {
            if (oldTestCases.size() == 0) {
                // insert "#includes of before, after of the probe point
                ProbePointUtils.insertIncludes(probePoint);
            }

            // compile, if success then change isCompilable to true
            String filePath = ProjectClone.getClonedFilePath(probePoint.getSourcecodeFileNode().getAbsolutePath());
            if (ProbePointUtils.checkCompile(probePoint.getTestCases(), filePath, true)) {
                isCompilable = true;
            }

            if (oldTestCases.size() == 0) {
                // remove "#includes of before, after of the probe point
                ProbePointUtils.removeIncludes(probePoint);
                // check after remove, whether the remove incorrect or not
                if (!ProbePointUtils.checkCompile(probePoint.getTestCases(), filePath, false)) {
                    logger.error("The akaignore file is incorrect after remove temporary includes");
                }
            }
        }

        return isCompilable;
    }

//    private void genIncludeFilesToCheck() {
//        String beforePath = probePoint.getPath() + File.separator + "before.probe";
//        String afterPath = probePoint.getPath() + File.separator + "after.probe";
//        String after = taAfter.getText();
//        String before = taBefore.getText();
//        Utils.writeContentToFile(after, afterPath);
//        Utils.writeContentToFile(before, beforePath);
//        probePoint.setBefore(beforePath);
//        probePoint.setAfter(afterPath);
//    }

    private void genIncludeFiles() {
        String beforePath = probePoint.getPath() + File.separator + "before.probe";
        String afterPath = probePoint.getPath() + File.separator + "after.probe";
        String after = genContentFile(taAfter.getText());
        String before = genContentFile(taBefore.getText());
        Utils.writeContentToFile(after, afterPath);
        Utils.writeContentToFile(before, beforePath);
        probePoint.setBefore(beforePath);
        probePoint.setAfter(afterPath);
    }

    private String genContentFile(String oriContent) {
        List<TestCase> testCaseList = probePoint.getTestCases();
        if (testCaseList.size() == 0) return oriContent;
        StringBuilder builder = new StringBuilder();
        builder.append("#if ");
        for (TestCase testCase : testCaseList) {
            String akaName = "AKA_TC_" + testCase.getName().replace(".", "_").toUpperCase();
            String tempCondition = "defined(" + akaName + ")";
            builder.append(tempCondition);
            if (testCaseList.indexOf(testCase) < testCaseList.size() - 1) {
                builder.append(" || ");
            }
        }
        builder.append("\n").append(oriContent).append("\n").append("#endif");
        return builder.toString();
    }

    /**
     * Read data from file path except first line and last line
     *
     * @param path path to file
     * @return data in string
     */
    private String readData(String path) {
        if (path == null) {
            return "";
        }
        List<String> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {

            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                data.add(sCurrentLine);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        int size = data.size();

        //#if defined(AKA_TC_MAX_74184)
        //int j = 10;
        //#endif
        // if the probe point is applied to at least a testcase then remove if, endif lines
        if (probePoint.getTestCases().size() > 0) {
            data.remove(size - 1);
            data.remove(0);
        }
        String sampleCode = String.join("\n", data);
        return sampleCode;
    }

    public static final String TYPE_ADD = "ADD";
    public static final String TYPE_EDIT = "EDIT";
}
