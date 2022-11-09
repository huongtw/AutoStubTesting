package com.dse.guifx_v3.objects;

import com.dse.config.FunctionConfig;
import com.dse.guifx_v3.controllers.FunctionConfigurationController;
import com.dse.logger.AkaLogger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConfigFunctionValueColumnCellFactory implements Callback<TreeTableColumn<FunctionConfigParameter, String>, TreeTableCell<FunctionConfigParameter, String>> {

    private final static AkaLogger logger = AkaLogger.get(InputColumnCellFactory.class);
    private FunctionConfig functionConfig;
    private FunctionConfigurationController controller;
    private boolean isDefaultConfig = false;

    @Override
    public TreeTableCell<FunctionConfigParameter, String> call(TreeTableColumn<FunctionConfigParameter, String> param) {
        MyCell cell = new MyCell(functionConfig);
        cell.setController(controller);
        cell.setDefaultConfig(isDefaultConfig);
        return cell;
    }

    public ConfigFunctionValueColumnCellFactory(FunctionConfig functionConfig) {
        this.functionConfig = functionConfig;
    }

    public void setController(FunctionConfigurationController controller) {
        this.controller = controller;
    }

    private static class MyCell extends TreeTableCell<FunctionConfigParameter, String> {
        private TextField textField;
        private FunctionConfig functionConfig;
        private FunctionConfigurationController controller;
        private boolean isDefaultConfig = false;

        public MyCell(FunctionConfig functionConfig) {
            this.functionConfig = functionConfig;
        }

        public void setController(FunctionConfigurationController controller) {
            this.controller = controller;
        }

        @Override
        public void startEdit() {
            if (!isEditable()) {
                return;
            }

            logger.debug("Start editing on the cell at line " + this.getIndex());
            FunctionConfigParameter param = getTreeTableRow().getItem();
            if (param != null && param.getParam() != null && param.getParam().length() > 0) {
                super.startEdit();

                if (param.getParam().equals(FunctionConfigParameter.TEST_DATA_EXEC_STRATEGY)
                        || param.getParam().equals(FunctionConfigParameter.TEST_DATA_GEN_STRATEGY)
                        || param.getParam().equals(FunctionConfigParameter.SOLVING_STRATEGY)) {
                    setText(null);
                    setGraphic(createComboBox(param));
                } else {
                    if (param.getParam().equals(FunctionConfigParameter.FLOAT_AND_DOUBLE_DELTA)
                            && !isDefaultConfig) {
                        return;
                    }
                    saveValueWhenUsersPressEnter();
                    setText(null);
                    textField.setText(param.getValue());
                    setGraphic(textField);
                    textField.selectAll();
                    textField.requestFocus();
                }
            } else {
                setGraphic(null);
                setText(null);
            }
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            FunctionConfigParameter param = getTreeTableRow().getItem();
            if (param != null && param.getParam() != null && param.getParam().length() > 0) {
                if (!param.getParam().equals(FunctionConfigParameter.TEST_DATA_EXEC_STRATEGY)
                        && !param.getParam().equals(FunctionConfigParameter.TEST_DATA_GEN_STRATEGY)
                        && !param.getParam().equals(FunctionConfigParameter.SOLVING_STRATEGY)) {
                    if (param.getParam().equals(FunctionConfigParameter.FLOAT_AND_DOUBLE_DELTA)
                            && !isDefaultConfig) {
                        return;
                    }
                    commitEdit(textField.getText());
                }
            }

            getTreeTableView().refresh();
            logger.debug("Canceled the edit on the cell");
        }

        @Override
        public void commitEdit(String newValue) {
            super.commitEdit(newValue);

            FunctionConfigParameter param = getTreeTableRow().getItem();

            String oldValue = param.getValue();
            boolean success = param.setValue(newValue);
            if (success) {
                if (!oldValue.equals(newValue)) {
                    controller.setChanged(true); // enable OK button
                }

                setGraphic(null);
                setText(param.getValue());
                getTreeTableView().refresh();

                // export the current configuration to json
//                exportFunctionConfigToJson(functionConfig);
            } else {
                super.cancelEdit();
            }
        }


        @Override
        protected void updateItem(String item, boolean empty) {
            FunctionConfigParameter parameter = getTreeTableRow().getItem();
            if (parameter != null && parameter.getParam() != null && parameter.getParam().length() > 0) {
                super.updateItem(item, empty);

                if (parameter != null) {
                    setEditable(parameter.isEditable());
                    setGraphic(null);
                    if (parameter.getParam().equals(FunctionConfigParameter.TEST_DATA_EXEC_STRATEGY)
                            || parameter.getParam().equals(FunctionConfigParameter.TEST_DATA_GEN_STRATEGY)
                            || parameter.getParam().equals(FunctionConfigParameter.SOLVING_STRATEGY)
                    ) {
                        if (parameter.getValue() == null) {
                            setText("<<Choose strategy>>");
                        } else {
                            setText(parameter.getValue());
                        }
                    } else {
                        setText(parameter.getValue());
                    }
                }
            } else {
                setText(null);
                setGraphic(null);
            }
        }

        private void saveValueWhenUsersPressEnter() {
            logger.debug("Set event when users click enter on the cell");
            if (textField == null) {
                textField = new TextField();
                textField.setOnKeyReleased((KeyEvent t) -> {
                    if (t.getCode() == KeyCode.ENTER) {
                        commitEdit(textField.getText());
                    }
                });
            }
        }

        private ComboBox<String> createComboBox(FunctionConfigParameter parameter) {
            ComboBox<String> comboBox = new ComboBox<>();
            ObservableList<String> options = FXCollections.observableArrayList();

            //SOLVING_STRATEGY = "SOLVING_STRATEGY";
            if (parameter.getParam().equals(FunctionConfigParameter.SOLVING_STRATEGY)) {
                List<String> list = new ArrayList<>();
                Collections.addAll(list, FunctionConfigParameter.getSolvingStrategies());
                options = FXCollections.observableArrayList(list);
                comboBox.setValue(options.get(0));
                if (parameter.getValue() == null) {
                    comboBox.setValue("<<Choose strategy>>");
                } else {
                    comboBox.setValue(parameter.getValue());
                }
            }
            //TEST_DATA_GEN_STRATEGY = "TEST_DATA_GEN_STRATEGY";
            else if (parameter.getParam().equals(FunctionConfigParameter.TEST_DATA_GEN_STRATEGY)) {
                List<String> list = new ArrayList<>();
                Collections.addAll(list, FunctionConfigParameter.getTestDataGenStrategies());
                options = FXCollections.observableArrayList(list);
                comboBox.setValue(parameter.getParam());
                if (parameter.getValue() == null) {
                    comboBox.setValue("<<Choose strategy>>");
                } else {
                    comboBox.setValue(parameter.getValue());
                }
            }
            //TEST_DATA_EXEC_STRATEGY = "TEST_DATA_EXEC_STRATEGY";
            else if (parameter.getParam().equals(FunctionConfigParameter.TEST_DATA_EXEC_STRATEGY)) {
                List<String> list = new ArrayList<>();
                Collections.addAll(list, FunctionConfigParameter.getTestDataExecStrategies());
                options = FXCollections.observableArrayList(list);
                if (parameter.getValue() == null) {
                    comboBox.setValue("<<Choose strategy>>");
                } else {
                    comboBox.setValue(parameter.getValue());
                }
            }

            comboBox.setItems(options);
            // Chỉnh sửa cho combobox vừa với ô của tree table.
            comboBox.setMaxWidth(getTableColumn().getMaxWidth());
            // Khi chọn giá trị trong combobox thì commit giá trị đó.
            comboBox.valueProperty().addListener((ov, oldValue, newValue) -> commitEdit(newValue));
            return comboBox;
        }

        public void setDefaultConfig(boolean defaultConfig) {
            isDefaultConfig = defaultConfig;
        }
    }

    public void setDefaultConfig(boolean defaultConfig) {
        isDefaultConfig = defaultConfig;
    }
}
