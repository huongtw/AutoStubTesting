package com.dse.guifx_v3.helps;

import com.dse.compiler.message.ICompileMessage;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.IVariableNode;
import com.dse.parser.object.MacroFunctionNode;
import com.dse.testdata.Iterator;
import com.dse.testdata.object.ValueDataNode;
import com.dse.util.TemplateUtils;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class DataNodeIterationDialog extends Alert {

    private ValueDataNode dataNode;

    private List<Iterator> iterators;

    public DataNodeIterationDialog(ValueDataNode dataNode) {
        super(AlertType.NONE);

        this.dataNode = dataNode;
        this.iterators = dataNode.getIterators();

        setTitle("Entering List Data");
        setHeaderText("Argument " + dataNode.getDisplayNameInParameterTree());

        TableView<Iterator> tableView = generateTableView();

        getDialogPane().setContent(tableView);

        generateButtons(tableView);

        initOwner(UIController.getPrimaryStage());
    }

    private void generateButtons(TableView<Iterator> tableView) {
        ButtonType addButton = new ButtonType("Add", ButtonBar.ButtonData.LEFT);
        getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL, addButton);

        final Button btnOk = (Button) getDialogPane().lookupButton(ButtonType.OK);
        btnOk.setText("Expand");
        btnOk.addEventFilter(ActionEvent.ACTION, event -> {
            Iterator iterator = tableView.getSelectionModel().getSelectedItem();

            if (iterator == null)
                event.consume();
            else
                onExpand(iterator);
        });

        final Button btnAdd = (Button) getDialogPane().lookupButton(addButton);
        btnAdd.addEventFilter(ActionEvent.ACTION, event -> {
            Iterator iterator = new Iterator(dataNode.clone());

            if (!iterators.isEmpty()) {
                Iterator last = iterators.get(iterators.size() - 1);
                iterator.setStartIdx(last.getStartIdx() + last.getRepeat());
            }

            iterators.add(iterator);

            tableView.getItems().setAll(iterators);
            tableView.refresh();

            event.consume();
        });
    }

    public abstract void onExpand(Iterator iterator);

    private void formatIterators() {
        for (int i = 0; i < iterators.size(); i++) {
            Iterator iterator = iterators.get(i);

            if (i == 0) {
                iterator.setStartIdx(1);
            } else {
                Iterator prev = iterators.get(i - 1);
                int startIdx = prev.getStartIdx() + prev.getRepeat();
                iterator.setStartIdx(startIdx);
            }
        }
    }

    private TableView<Iterator> generateTableView() {
        TableView<Iterator> tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setEditable(true);

        TableColumn<Iterator, Iterator> delCol = new TableColumn<>("Delete");
        delCol.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        delCol.setCellFactory(param -> new TableCell<Iterator, Iterator>() {
            private final Button deleteButton = new Button("Delete");

            @Override
            protected void updateItem(Iterator item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || iterators.indexOf(item) == 0) {
                    setGraphic(null);
                    return;
                }

                setGraphic(deleteButton);
                deleteButton.setOnAction(event -> {
                    iterators.remove(item);
                    formatIterators();
                    getTableView().getItems().setAll(iterators);
                    getTableView().refresh();
                });
            }
        });

        TableColumn<Iterator, String> repeatCol = new TableColumn<>("Repeat");
        repeatCol.setEditable(true);
        repeatCol.setCellValueFactory(param -> new SimpleStringProperty(String.valueOf(param.getValue().getRepeat())));
        repeatCol.setCellFactory(TextFieldTableCell.forTableColumn());

        repeatCol.setOnEditCommit(event -> {
            String newVal = event.getNewValue();

            int repeat = Integer.parseInt(newVal);

            Iterator iterator = event.getRowValue();

            iterator.setRepeat(repeat);

            formatIterators();
        });

        tableView.getColumns().setAll(repeatCol, delCol);

        tableView.getItems().setAll(iterators);

        tableView.refresh();

        return tableView;
    }
}
