package com.dse.guifx_v3.controllers;

import com.dse.guifx_v3.helps.UIController;
import com.dse.guifx_v3.objects.TestCaseExecutionDataNode;
import com.dse.testcase_manager.TestCase;
import com.dse.testcase_manager.TestCaseManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import com.dse.logger.AkaLogger;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class TestCasesExecutionTabController implements Initializable {

    private final static AkaLogger logger = AkaLogger.get(TestCasesExecutionTabController.class);

    @FXML
    private TableColumn<TestCaseExecutionDataNode, String> colId;
    @FXML
    private TableColumn<TestCaseExecutionDataNode, String> colValue;
    @FXML
    private TableColumn<TestCaseExecutionDataNode, String> colName;
    @FXML
    private TableColumn<TestCaseExecutionDataNode, String> colCoverage;
    @FXML
    private TableView<TestCaseExecutionDataNode> tvTestCasesExecution;
    @FXML
    private TabPane executionDetails;

    private List<String> detailsTabNames = new ArrayList<>();
    private Tab tab;

    public final ObservableList<TestCaseExecutionDataNode> data = FXCollections.observableArrayList();

    public void initialize(URL location, ResourceBundle resources) {
        tvTestCasesExecution.setItems(data);

        tvTestCasesExecution.setRowFactory(param -> {
            MyRow row = new MyRow();
            row.setOnMouseClicked(event -> {
                if (row.getItem() != null) {
                    if (event.getClickCount() == 2 && (!row.isEmpty())) {
                        openExecutionDetail(row.getItem());
                    }
                }
            });
            return row;
        });
        executionDetails.getTabs().clear();

        colId.setCellValueFactory(param -> {
            if (param != null && param.getValue() != null && param.getValue().getValue() != null) {
                int id = param.getValue().getId();
                if (id >= 0) {
                    return new SimpleStringProperty(String.valueOf(id));
                } else {
                    return null;
                }
            } else {
                return null;
            }
        });
        colName.setCellValueFactory(param -> {
            if (param != null && param.getValue() != null && param.getValue().getValue() != null) {
                String name = param.getValue().getName();
                if (name != null) {
                    return new SimpleStringProperty(name);
                } else {
                    return null;
                }
            } else {
                return null;
            }
        });
        colValue.setCellValueFactory(param -> {
            if (param != null && param.getValue() != null && param.getValue().getValue() != null) {
                String value = param.getValue().getShortenValue();
                if (value != null) {
                    return new SimpleStringProperty(value);
                } else {
                    return null;
                }
            } else {
                return null;
            }
        });
        colCoverage.setCellValueFactory(param -> {
            if (param != null && param.getValue() != null && param.getValue().getValue() != null) {
                String coverage = param.getValue().getCoverage();
                if (coverage != null) {
                    return new SimpleStringProperty(coverage);
                } else {
                    return null;
                }
            } else {
                return null;
            }
        });
    }

    private void openExecutionDetail(TestCaseExecutionDataNode dataNode) {
        String name = TestCasesExecutionTabController.EXECUTION_DETAIL_TAB_PREFIX + dataNode.getId();
        if (detailsTabNames.contains(name)) {
            Tab tab = getExecutionDetailTabByName(name);
            executionDetails.getSelectionModel().select(tab);
        } else {
            Tab tab = new Tab(name);
            ListView<String> details = new ListView<>();
            details.setItems(dataNode.getDetail());
            tab.setContent(details);
            detailsTabNames.add(name);
            executionDetails.getTabs().add(tab);
            executionDetails.getSelectionModel().select(tab);
            tab.setOnClosed(event -> detailsTabNames.remove(name));
//            new DataTreeGeneration().getValues()
        }

    }

    private Tab getExecutionDetailTabByName(String name) {
        for (Tab tab : executionDetails.getTabs()) {
            if (tab.getText().equals(name)) {
                return tab;
            }
        }

        logger.debug("Can not find Execution Detail tab with name: " + name);
        return null;
    }

    public void setTab(Tab tab) {
        this.tab = tab;
    }

    public Tab getTab() {
        return tab;
    }


    private class MyRow extends TableRow<TestCaseExecutionDataNode> {

        @Override
        protected void updateItem(TestCaseExecutionDataNode item, boolean empty) {
            super.updateItem(item, empty);

            if (item != null) {
                setContextMenu(new ContextMenu());
                TestCase testCase = TestCaseManager.getBasicTestCaseByName(item.getName());
                addOpenTestCase(testCase);
                addViewCoverageForATestcase(testCase);
                addViewDetails(item);
            } else {
                setContextMenu(null);
            }
        }

        /**
         * View coverage of test case generated automatically
         * @param testCase
         */
        public void addViewCoverageForATestcase(TestCase testCase) {
            MenuItem miViewCoverage = new MenuItem("View Coverage");
            getContextMenu().getItems().add(miViewCoverage);
            miViewCoverage.setOnAction(event -> {
                if (testCase != null) {
                    UIController.viewCoverageOfATestcase(testCase);
                }
            });
        }

        public void addOpenTestCase(TestCase testCase) {
            MenuItem miOpenTestCase = new MenuItem("Open Test Case");
            getContextMenu().getItems().add(miOpenTestCase);
            miOpenTestCase.setOnAction(event -> {
                if (testCase != null) {
                    UIController.viewTestCase(testCase);
                }
            });
        }

        public void addViewDetails(TestCaseExecutionDataNode item) {
            MenuItem miViewDetails = new MenuItem("View Details");
            getContextMenu().getItems().add(miViewDetails);

            miViewDetails.setOnAction(event -> {
                openExecutionDetail(item);
            });
        }
    }

    public ObservableList<TestCaseExecutionDataNode> getData() {
        return data;
    }

    private static final String EXECUTION_DETAIL_TAB_PREFIX = "Test Case Id: ";
}
