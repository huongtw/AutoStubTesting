package com.dse.testdata;

import com.dse.testdata.object.DataNode;
import com.dse.testdata.object.ValueDataNode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;

public interface IInputCellHandler {
    void update(TreeTableCell<DataNode, String> cell, DataNode dataNode);

    void commitEdit(ValueDataNode dataNode, String newValue) throws Exception;
}
