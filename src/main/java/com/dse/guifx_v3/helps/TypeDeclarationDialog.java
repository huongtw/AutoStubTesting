package com.dse.guifx_v3.helps;

import com.dse.compiler.message.ICompileMessage;
import com.dse.logger.AkaLogger;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.IFunctionNode;
import com.dse.parser.object.IVariableNode;
import com.dse.parser.object.MacroFunctionNode;
import com.dse.util.TemplateUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class TypeDeclarationDialog extends Alert {

    private static final AkaLogger logger = AkaLogger.get(TypeDeclarationDialog.class);

    private final ICommonFunctionNode functionNode;

    private final Map<String, String> typeMap = new HashMap<>();

    private TableView<String> tableView;

    private Button btnOk;

    public TypeDeclarationDialog(ICommonFunctionNode functionNode) {
        super(AlertType.NONE);

        this.functionNode = functionNode;

        setTitle("Type Declaration Dialog");
        setHeaderText("Function " + functionNode.getName());

        generateTableView();

        generateButtons();

        getDialogPane().setContent(tableView);

        initOwner(UIController.getPrimaryStage());
    }

    private void generateTableView() {
        tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setEditable(true);

        TableColumn<String, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue()));

        TableColumn<String, String> typeCol = new TableColumn<>("Type");
        typeCol.setEditable(true);
        typeCol.setCellFactory(TextFieldTableCell.forTableColumn());
        typeCol.setOnEditCommit(event -> {
            btnOk.setDisable(false);

            String newVal = event.getNewValue();
            String name = event.getRowValue();

            // comvert array to pointer
            newVal = newVal.trim();
            newVal = newVal.replaceAll("\\[(\\s*[0-9]+\\s*)*\\]","*");

            typeMap.put(name, newVal);
        });

        tableView.getColumns().setAll(nameCol, typeCol);

        if (functionNode instanceof MacroFunctionNode) {
            for (IVariableNode arg : functionNode.getArguments())
                tableView.getItems().add(arg.getName());

            tableView.getItems().add("RETURN");
        }

        if (functionNode.isTemplate() && functionNode instanceof IFunctionNode) {
            List<String> templateParams = TemplateUtils.getTypesInTemplate((IFunctionNode) functionNode);

            for (String templateParam : templateParams)
                tableView.getItems().add(templateParam);
        }

        tableView.refresh();
    }

    private void generateButtons() {
        ButtonType compileButton = new ButtonType("Compile", ButtonBar.ButtonData.LEFT);
        getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL, compileButton);

        btnOk = (Button) getDialogPane().lookupButton(ButtonType.OK);
        btnOk.setDisable(false);
        btnOk.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                onOkClick(typeMap);
            } catch (Exception e) {
                UIController.showErrorDialog("Error: " + e.getMessage(), "Invalid input", "Something wrong with input");
                e.printStackTrace();
            }
        });

        final Button btnTest = (Button) getDialogPane().lookupButton(compileButton);
        btnTest.addEventFilter(ActionEvent.ACTION, event -> {
            ICompileMessage message = onTestCompile(typeMap);
            logger.debug(message.getMessage());
//            btnOk.setDisable(message.getType() == ICompileMessage.MessageType.ERROR);
            event.consume();
        });
    }

    public abstract void onOkClick(Map<String, String> typeMap) throws Exception;

    public abstract ICompileMessage onTestCompile(Map<String, String> typeMap);
}
