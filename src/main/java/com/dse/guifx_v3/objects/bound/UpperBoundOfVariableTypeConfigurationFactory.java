package com.dse.guifx_v3.objects.bound;

import com.dse.guifx_v3.objects.InputColumnCellFactory;
import com.dse.logger.AkaLogger;
import com.dse.boundary.BoundOfDataTypes;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;


public class UpperBoundOfVariableTypeConfigurationFactory implements Callback<TreeTableColumn<BoundOfVariableTypeConfiguration, String>,
        TreeTableCell<BoundOfVariableTypeConfiguration, String>> {

    private final static AkaLogger logger = AkaLogger.get(InputColumnCellFactory.class);

    private BoundOfDataTypes boundOfDataTypes;

    public UpperBoundOfVariableTypeConfigurationFactory(BoundOfDataTypes boundOfDataTypes){
        this.boundOfDataTypes = boundOfDataTypes;
    }

    @Override
    public TreeTableCell<BoundOfVariableTypeConfiguration, String> call(TreeTableColumn<BoundOfVariableTypeConfiguration, String> param) {
        return new MyCell(boundOfDataTypes);
    }

    private static class MyCell extends TreeTableCell<BoundOfVariableTypeConfiguration, String> {
        private TextField textField;
        private BoundOfDataTypes boundOfDataTypes;

        public MyCell(BoundOfDataTypes boundOfDataTypes) {
            this.boundOfDataTypes = boundOfDataTypes;
        }

        @Override
        public void startEdit() {
            logger.debug("Start editing on the cell at line " + this.getIndex());
            BoundOfVariableTypeConfiguration param = getTreeTableRow().getItem();
            if (param != null && param.getLower() != null && param.getUpper() != null
                && param.getVariableType() != null) {
                super.startEdit();

                setGraphic(null);
                saveValueWhenUsersPressEnter();
                setGraphic(textField);
                    textField.requestFocus();
            }
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            getTreeTableView().refresh();
            logger.debug("Canceled the edit on the cell");
        }

        @Override
        public void commitEdit(String newValue) {
            super.commitEdit(newValue);
            BoundOfVariableTypeConfiguration param = getTreeTableRow().getItem();
            boolean success = param.setUpper(newValue);
            if (success) {
                setGraphic(null);
                setText(param.getUpper());
                getTreeTableView().refresh();

                // export to file
//                Environment.exportBoundOfDataTypeToFile(boundOfDataTypes);
            } else {

                cancelEdit();
            }
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            BoundOfVariableTypeConfiguration parameter = getTreeTableRow().getItem();
            if (parameter != null && parameter.getUpper() != null && parameter.getLower() != null
                    && parameter.getVariableType() != null) {
                super.updateItem(item, empty);

                if (parameter != null) {
                    setEditable(true);
                    setText(parameter.getUpper());
                }
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
    }
}
