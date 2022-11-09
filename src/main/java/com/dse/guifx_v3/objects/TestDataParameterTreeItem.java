package com.dse.guifx_v3.objects;

import com.dse.search.Search2;
import com.dse.testdata.object.DataNode;
import com.dse.testdata.object.SubprogramNode;
import com.dse.testdata.object.ValueDataNode;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;

public class TestDataParameterTreeItem extends TestDataTreeItem {
    private ValueDataNode inputDataNode;
    private ValueDataNode expectedOutputDataNode;
    private List<TreeItem<DataNode>> inputChildren = new ArrayList<>();
    private List<TreeItem<DataNode>> expectedOutputChildren = new ArrayList<>();

    private ColumnType selectedColumn = ColumnType.INPUT;

    public TestDataParameterTreeItem(ValueDataNode dataNode) {
        super(dataNode);
        setColumnType(ColumnType.ALL);

        SubprogramNode sut = Search2.findSubprogramUnderTest(dataNode.getTestCaseRoot());
        this.inputDataNode = dataNode;
        if (sut != null) {
            this.expectedOutputDataNode = sut.getExpectedOuput(dataNode);
        }
    }

    public ColumnType getSelectedColumn() {
        return selectedColumn;
    }

    public void setSelectedColumn(ColumnType selectedColumn) {
        if (selectedColumn != this.selectedColumn) {
            if (this.selectedColumn == ColumnType.INPUT) {
                // save children
                inputChildren.clear();
                inputChildren.addAll(getChildren());

                // switch value and children
                setValue(expectedOutputDataNode);
                getChildren().clear();
                getChildren().addAll(expectedOutputChildren);
            } else if (this.selectedColumn == ColumnType.EXPECTED) {
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
