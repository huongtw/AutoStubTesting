package com.dse.guifx_v3.objects;

import com.dse.testdata.object.DataNode;
import javafx.scene.control.TreeItem;

public class TestDataTreeItem extends TreeItem<DataNode> {
    private ColumnType columnType;
    public static void loadChildren(TestDataTreeItem treeItem) {

    }

    public TestDataTreeItem(ColumnType columnType) {
        this.columnType = columnType;
    }
//
//    public TestDataTreeItem() {
//        this.columnType = ColumnType.INPUT;
//    }
    public TestDataTreeItem(DataNode dataNode) {
        super(dataNode);
        this.columnType = ColumnType.NONE;
    }

    public ColumnType getColumnType() {
        return columnType;
    }

    public void setColumnType(ColumnType columnType) {
        this.columnType = columnType;
    }

    public enum ColumnType {
        NONE,
        ALL,
        INPUT,
        EXPECTED
    }
}
