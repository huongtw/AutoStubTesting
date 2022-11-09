package com.dse.guifx_v3.objects;

import com.dse.testcase_manager.CompoundTestCase;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.testcase_manager.TestCaseSlot;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;
import com.dse.logger.AkaLogger;

public class IterationsColumnCellFactory implements Callback<TreeTableColumn<TestCaseSlot, String>, TreeTableCell<TestCaseSlot, String>> {

    final static AkaLogger logger = AkaLogger.get(InputColumnCellFactory.class);
    private CompoundTestCase compoundTestCase;

    public IterationsColumnCellFactory(CompoundTestCase compoundTestCase) {
        this.compoundTestCase = compoundTestCase;
    }

    @Override
    public TreeTableCell<TestCaseSlot, String> call(TreeTableColumn<TestCaseSlot, String> param) {
        return new MyCell(compoundTestCase);
    }

    private static class MyCell extends TreeTableCell<TestCaseSlot, String> {
        private CompoundTestCase compoundTestCase;
        private TextField textField;

        MyCell(CompoundTestCase compoundTestCase) {
            this.compoundTestCase = compoundTestCase;
        }

        @Override
        public void startEdit() {
            logger.debug("Start editing on the cell at line " + this.getIndex());
            super.startEdit();

//            logger.debug("Clear the cell");
            setText(null);

            saveValueWhenUsersPressEnter();
            setGraphic(textField);
            textField.setText(null);
            textField.requestFocus();
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

            TestCaseSlot slot = getTreeTableRow().getItem();
            try {
                int iterations = Integer.parseInt(newValue);
                slot.setNumberOfIterations(iterations);
                getTreeTableView().refresh();
                TestCaseManager.exportCompoundTestCaseToFile(compoundTestCase);
            } catch (Exception e) {
                cancelEdit();
            }
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            TestCaseSlot slot = getTreeTableRow().getItem();

            if (slot != null) {
                setEditable(true);
                setText(String.valueOf(slot.getNumberOfIterations()));
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
