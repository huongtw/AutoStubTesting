package com.dse.guifx_v3.objects;

import com.dse.testdata.object.DataNode;
import com.dse.testdata.object.GlobalRootDataNode;
import com.dse.testdata.object.RootDataNode;
import com.dse.logger.AkaLogger;
import com.dse.testdata.object.ValueDataNode;
import com.dse.util.NodeType;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;

public class TestDataGlobalVariableTreeItem extends TestDataTreeItem{
    private final static AkaLogger logger = AkaLogger.get(TestDataGlobalVariableTreeItem.class);
    private ValueDataNode inputDataNode;
    private ValueDataNode expectedOutputDataNode;
    private List<TreeItem<DataNode>> inputChildren = new ArrayList<>();
    private List<TreeItem<DataNode>> expectedOutputChildren = new ArrayList<>();

    private TestDataTreeItem.ColumnType selectedColumn = TestDataTreeItem.ColumnType.INPUT;

    public TestDataGlobalVariableTreeItem(ValueDataNode dataNode) {
        super(dataNode);
        setColumnType(TestDataTreeItem.ColumnType.ALL);

        if (dataNode.getParent() instanceof GlobalRootDataNode
                && ((RootDataNode) dataNode.getParent()).getLevel().equals(NodeType.GLOBAL)) {
            GlobalRootDataNode parent = (GlobalRootDataNode) dataNode.getParent();
            this.inputDataNode = dataNode;
            this.expectedOutputDataNode = parent.getGlobalInputExpOutputMap().get(dataNode);
            if (expectedOutputDataNode == null) {
                logger.debug("Failed on get ExpectedOutputDataNode. Size of map: " + parent.getGlobalInputExpOutputMap().size());
            }
        }
    }

    public TestDataTreeItem.ColumnType getSelectedColumn() {
        return selectedColumn;
    }

    public void setSelectedColumn(TestDataTreeItem.ColumnType selectedColumn) {
        if (selectedColumn != this.selectedColumn) {
            if (this.selectedColumn == TestDataTreeItem.ColumnType.INPUT) {
                // save children
                inputChildren.clear();
                inputChildren.addAll(getChildren());

                // switch value and children
                setValue(expectedOutputDataNode);
                getChildren().clear();
                getChildren().addAll(expectedOutputChildren);
            } else if (this.selectedColumn == TestDataTreeItem.ColumnType.EXPECTED) {
                // save children
                expectedOutputChildren.clear();
                expectedOutputChildren.addAll(getChildren());

                // switch value and children
                setValue(inputDataNode);
                getChildren().clear();
                getChildren().addAll(inputChildren);
            }

            this.selectedColumn = selectedColumn;
        }
    }

    public ValueDataNode getInputDataNode() {
        return inputDataNode;
    }

    public ValueDataNode getExpectedOutputDataNode() {
        return expectedOutputDataNode;
    }
}
