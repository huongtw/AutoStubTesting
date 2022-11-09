package com.dse.guifx_v3.controllers;

import com.dse.debugger.controller.DebugController;
import com.dse.guifx_v3.controllers.main_view.MDIWindowController;
import com.dse.guifx_v3.helps.UIController;
import com.dse.guifx_v3.objects.DelayColumnCellFactory;
import com.dse.guifx_v3.objects.IterationsColumnCellFactory;
import com.dse.parser.object.INode;
import com.dse.testcase_manager.*;
import com.dse.user_code.UserCodeManager;
import com.dse.user_code.controllers.ChooseTestCaseUserCodeController;
import com.dse.util.Utils;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeTableCell;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import com.dse.logger.AkaLogger;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.fxmisc.richtext.CodeArea;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class CompoundTestCaseTreeTableViewController implements Initializable {
    private final static AkaLogger logger = AkaLogger.get(CompoundTestCaseTreeTableViewController.class);
    @FXML
    private TreeTableColumn<TestCaseSlot, String> colSlot;
    @FXML
    private TreeTableColumn<TestCaseSlot, String> colUnits;
    @FXML
    private TreeTableColumn<TestCaseSlot, String> colSubprograms;
    @FXML
    private TreeTableColumn<TestCaseSlot, String> colTestCases;
    @FXML
    private TreeTableColumn<TestCaseSlot, String> colIterations;
    @FXML
    private TreeTableColumn<TestCaseSlot, String> colDelay;
    @FXML
    private TreeTableColumn<TestCaseSlot, Boolean> colBreak;
    @FXML
    private TreeTableView<TestCaseSlot> ttvCompoundTestCase;
    @FXML
    private Tab tabUserCode;
    @FXML
    private SplitPane spUserCode;
    @FXML
    private ListView<String> lvIncludeFiles;
    @FXML
    private Button bAddIncludeFile;
    @FXML
    private Button bDeleteIncludeFile;

    private CodeArea caSetUp;
    private CodeArea caTearDown;
    private CompoundTestCase compoundTestCase;

    public void initialize(URL location, ResourceBundle resources) {
        ttvCompoundTestCase.setRoot(root);
        ttvCompoundTestCase.setEditable(true); // need for editing columns
        colSlot.setCellValueFactory(param -> new SimpleStringProperty(Integer.toString(param.getValue().getValue().getSlotNum())));
        colUnits.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue().getUnit()));
        colSubprograms.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue().getSubprogramName()));
        colTestCases.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue().getTestcaseName()));
        colIterations.setCellValueFactory(param -> new SimpleStringProperty(Integer.toString(param.getValue().getValue().getNumberOfIterations())));
        colDelay.setCellValueFactory(param -> new SimpleStringProperty(Integer.toString(param.getValue().getValue().getDelay())));
        colBreak.setCellValueFactory(param -> {
            TestCaseSlot slot = param.getValue().getValue();
            SimpleBooleanProperty simpleBooleanProperty = new SimpleBooleanProperty(slot.isBreak());
            simpleBooleanProperty.addListener((observable, oldValue, newValue) -> {
                slot.setBreak(newValue);
                // Todo: add or delete br in test driver file
            });
            return simpleBooleanProperty;
        });
        colBreak.setCellFactory(CheckBoxTreeTableCell.forTreeTableColumn(colBreak));
        //colReport is ignore here

        // sort the treetable by slot number
        colSlot.setComparator(Comparator.comparingInt(Integer::parseInt));
        ttvCompoundTestCase.getSortOrder().add(colSlot);

        // handle drag drop event for change order of slots or for add slot
        ttvCompoundTestCase.setRowFactory(param -> {
            TreeTableRow<TestCaseSlot> row = new MyRow(compoundTestCase);
            row.setOnDragDetected(event -> {

                // drag was detected, start drag-and-drop gesture
                TreeItem<TestCaseSlot> selected = ttvCompoundTestCase.getSelectionModel().getSelectedItem();
                // to access TestCaseSlot use 'selected.getValue()'

                if (selected != null) {
                    Dragboard db = ttvCompoundTestCase.startDragAndDrop(TransferMode.ANY);

                    // Keep whats being dragged on the clipboard
                    ClipboardContent content = new ClipboardContent();
                    content.putString(String.valueOf(selected.getValue().getSlotNum()));
                    db.setContent(content);

                    event.consume();
                }
            });
            // handle drag over for add slot and for change order
            row.setOnDragOver(event -> {
                // data is dragged over the target
                Dragboard db = event.getDragboard();
                // the first condition below seem like redundent
                if (db.hasString()) {
                    if (event.getGestureSource() == ttvCompoundTestCase) {
                        // logger.debug("drag over to change order");
                        event.acceptTransferModes(TransferMode.MOVE);
                        ttvCompoundTestCase.getSelectionModel().select(row.getTreeItem());
                    } else {
//                                logger.debug("drag over to add slot");
                        /* allow for both copying and moving, whatever user chooses */
                        event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                    }
                }

                event.consume();
            });
            row.setOnDragDropped(event -> {

                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasString()) {

                    if (event.getGestureSource() == ttvCompoundTestCase) { // change order
                        if (!row.isEmpty()) {
                            try {
                                logger.debug("drag drop to change order");
                                int source = Integer.parseInt(db.getString());
                                int target = row.getTreeItem().getValue().getSlotNum();
                                compoundTestCase.changeOrder(source, target);
                                TestCaseManager.exportCompoundTestCaseToFile(compoundTestCase);

                                ttvCompoundTestCase.refresh();
                                ttvCompoundTestCase.sort();
                                // select the source row
                                ttvCompoundTestCase.getSelectionModel().select(row.getIndex());
                                success = true;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else { // add slot
                        logger.debug("Drag drop to add slot " + db.getString());
                        addSlot(db.getString());
                        success = true;
                    }
                }
                event.setDropCompleted(success);

                event.consume();
            });

            return row;
        });

        ttvCompoundTestCase.setOnDragOver(event -> { // for the case the treetable view is empty
            if (event.getGestureSource() != ttvCompoundTestCase && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
        });

        ttvCompoundTestCase.setOnDragDropped(event -> { // for the case the treetableview is empty
            logger.debug("Found drop event on the compound test case " + compoundTestCase.getPath());
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString() && event.getGestureSource() != ttvCompoundTestCase) {
                logger.debug("drag drop to add slot");
                logger.debug(db.getString());
                addSlot(db.getString());
                success = true;
            }
            event.setDropCompleted(success);

            event.consume();
        });

        lvIncludeFiles.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                return new ListCell<String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) setText(item);
                        else setText("");
                    }
                };
            }
        });
    }

    @FXML
    void addIncludeFile() {
        final FileChooser fileChooser = new FileChooser();
//        addExtensionFilterWhenAddIncludeFile(fileChooser);
        File includeFile = fileChooser.showOpenDialog(UIController.getPrimaryStage());
        if (includeFile != null && includeFile.exists()) {
            lvIncludeFiles.getItems().add(includeFile.getAbsolutePath());
        }
    }

    @FXML
    void deleteIncludeFile() {
        String path = lvIncludeFiles.getSelectionModel().getSelectedItem();
        if (path != null) {
            lvIncludeFiles.getItems().remove(path);
            lvIncludeFiles.refresh();
        }
    }

    @FXML
    void loadExistedUserCode() {
        ChooseTestCaseUserCodeController controller = ChooseTestCaseUserCodeController.getInstance();
        if (controller != null) {
            controller.setAbstractTestCase(compoundTestCase);
            controller.showStageAndWait(UIController.getPrimaryStage());
            lvIncludeFiles.getItems().clear();
            lvIncludeFiles.getItems().addAll(compoundTestCase.getTestCaseUserCode().getIncludePaths());

            caSetUp.clear();
            caTearDown.clear();
            caSetUp.replaceText(compoundTestCase.getTestCaseUserCode().getSetUpContent());
            caTearDown.replaceText(compoundTestCase.getTestCaseUserCode().getTearDownContent());
        }
    }

    @FXML
    void saveTestCaseUserCode() {
        compoundTestCase.getTestCaseUserCode().setSetUpContent(caSetUp.getText());
        compoundTestCase.getTestCaseUserCode().setTearDownContent(caTearDown.getText());
        compoundTestCase.getTestCaseUserCode().getIncludePaths().clear();
        compoundTestCase.getTestCaseUserCode().getIncludePaths().addAll(lvIncludeFiles.getItems());
        TestCaseManager.exportCompoundTestCaseToFile(compoundTestCase);
    }

    private TreeItem<TestCaseSlot> root = new TreeItem<>();

    private void addSlot(TestCaseSlot slot) {
        // add to GUI tree tableview
        slot.setSlotNum(root.getChildren().size());
        TreeItem<TestCaseSlot> item = new TreeItem<>(slot);
        root.getChildren().add(item);
        // add to compound testcase
        compoundTestCase.getSlots().add(slot);
        ttvCompoundTestCase.sort();
    }

    public void addSlot(String name) {
        TestCase testCase = TestCaseManager.getBasicTestCaseByName(name);
        if (testCase != null) {
            addSlot(new TestCaseSlot(testCase));
        } else {
            CompoundTestCase compoundTestCase = TestCaseManager.getCompoundTestCaseByName(name);
            if (compoundTestCase != null) {
                if (!compoundTestCase.getName().equals(this.compoundTestCase.getName())) {
                    addSlot(new TestCaseSlot(compoundTestCase));
                } else {
                    logger.debug("Ignore adding compound test into itself");
                    UIController.showErrorDialog("Can not adding a compound test case into itself", "Compound generation error", "Compound generation failed");
                }
            } else {
                logger.debug("TestCase not found");
            }
        }
        TestCaseManager.exportCompoundTestCaseToFile(compoundTestCase);
    }

    public void loadContent(CompoundTestCase compoundTestCase) {
        List<TestCaseSlot> slots = compoundTestCase.getSlots();
        for (TestCaseSlot slot : slots) {
            TreeItem<TestCaseSlot> item = new TreeItem<>(slot);
            root.getChildren().add(item);
        }
        ttvCompoundTestCase.sort();

        // initialize code area
        String setUp = compoundTestCase.getTestCaseUserCode().getSetUpContent();
        String tearDown = compoundTestCase.getTestCaseUserCode().getTearDownContent();
        CodeArea setUpCodeArea = UserCodeManager.formatCodeArea(setUp, true, true);
        CodeArea tearDownCodeArea = UserCodeManager.formatCodeArea(tearDown, true, true);
        setCaSetUp(setUpCodeArea);
        setCaTearDown(tearDownCodeArea);
        spUserCode.getItems().set(0, setUpCodeArea);
        spUserCode.getItems().set(1, tearDownCodeArea);

        lvIncludeFiles.getItems().addAll(compoundTestCase.getTestCaseUserCode().getIncludePaths());
    }

    public void setCompoundTestCase(CompoundTestCase compoundTestCase) {
        this.compoundTestCase = compoundTestCase;
        // set compoundtestcase for iterations column
        colIterations.setCellFactory(new IterationsColumnCellFactory(compoundTestCase));
        // set compoundtestcase for delay column
        colDelay.setCellFactory(new DelayColumnCellFactory(compoundTestCase));
    }

    public void setCaSetUp(CodeArea caSetUp) {
        this.caSetUp = caSetUp;
    }

    public void setCaTearDown(CodeArea caTearDown) {
        this.caTearDown = caTearDown;
    }

    // refresh after delete a testcase
    public void refresh() {
        // remove slots that referent to deleted testcase
        compoundTestCase.validateSlots();
        // update on tree table view

        List<TreeItem> list = new ArrayList<>();
        for (TreeItem<TestCaseSlot> item : root.getChildren()) {
            if (!compoundTestCase.getSlots().contains(item.getValue())) {
                list.add(item);
            }
        }

        for (TreeItem item : list) {
            root.getChildren().remove(item);
        }
        ttvCompoundTestCase.refresh();
    }

    private static class MyRow extends TreeTableRow<TestCaseSlot> {
        private CompoundTestCase compoundTestCase;

        MyRow(CompoundTestCase compoundTestCase) {
            this.compoundTestCase = compoundTestCase;
        }

        @Override
        protected void updateItem(TestCaseSlot item, boolean empty) {
            super.updateItem(item, empty);
            if (getTreeItem() != null && getTreeItem().getValue() != null) {
                // initialize popup
                setContextMenu(new ContextMenu());
                addMoveUp(item);
                addMoveDown(item);
                addDelete(item);
                addOpenSource(item);
            }
        }

        private void addMoveUp(TestCaseSlot item) {
            MenuItem mi = new MenuItem("Move Up");
            getContextMenu().getItems().add(mi);

            mi.setOnAction(event -> moveUp(item));
        }

        private void addMoveDown(TestCaseSlot item) {
            MenuItem mi = new MenuItem("Move Down");
            getContextMenu().getItems().add(mi);

            mi.setOnAction(event -> moveDown(item));
        }

        private void addDelete(TestCaseSlot item) {
            MenuItem mi = new MenuItem("Delete");
            getContextMenu().getItems().add(mi);

            mi.setOnAction(event -> deleteSlot(item));
        }

        private void addOpenSource(TestCaseSlot item) {
            MenuItem mi = new MenuItem("Open Source In Debug");
            getContextMenu().getItems().add(mi);
            mi.setOnAction(event -> {
                if (MDIWindowController.getMDIWindowController().checkDebugOpen()) {
                    String testcaseName = item.getTestcaseName();
                    TestCase testCase = TestCaseManager.getBasicTestCaseByNameWithoutData(testcaseName);
                    assert testCase != null;
                    INode sourceNode = Utils.getSourcecodeFile(testCase.getFunctionNode());
                    String path = sourceNode.getAbsolutePath();
                    DebugController.getDebugController().openSource(path);
                    MDIWindowController.getMDIWindowController().viewDebug(compoundTestCase);
                } else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "You need to open test case in debug first?", ButtonType.OK);
                    alert.showAndWait();
                    if (alert.getResult() == ButtonType.OK) {
                        alert.close();
                    }
                }
            });
        }

        private void deleteSlot(TestCaseSlot item) {
            logger.debug("Delete slot " + item.getTestcaseName() + " of " + item.getSubprogramName());
            // move to bottom and delete to keep the order of remain slots
            compoundTestCase.changeOrder(item.getSlotNum(), compoundTestCase.getSlots().size() - 1);
            compoundTestCase.getSlots().remove(item); // delete from disk
            TestCaseManager.exportCompoundTestCaseToFile(compoundTestCase);

            TreeItem<TestCaseSlot> treeItem = getTreeItem();
            treeItem.getParent().getChildren().remove(treeItem);
            getTreeTableView().refresh();
            getTreeTableView().sort();
        }

        private void moveDown(TestCaseSlot item) {
            logger.debug("Move Down");
            int num = item.getSlotNum();
            if (num < compoundTestCase.getSlots().size() - 1) {
                compoundTestCase.changeOrder(num, num + 1);
                TestCaseManager.exportCompoundTestCaseToFile(compoundTestCase);
                getTreeTableView().sort();
            }
        }

        private void moveUp(TestCaseSlot item) {
            logger.debug("Move Up");
            int num = item.getSlotNum();
            if (num > 0) {
                compoundTestCase.changeOrder(num, num - 1);
                TestCaseManager.exportCompoundTestCaseToFile(compoundTestCase);
                getTreeTableView().sort();
            }
        }
    }
}
