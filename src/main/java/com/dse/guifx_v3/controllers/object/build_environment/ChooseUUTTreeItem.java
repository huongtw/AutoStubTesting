package com.dse.guifx_v3.controllers.object.build_environment;

import com.dse.guifx_v3.controllers.object.unit_node.AbstractUnitNode;
import javafx.beans.property.BooleanProperty;
import javafx.scene.control.TreeItem;

import java.util.List;

public class ChooseUUTTreeItem extends TreeItem<AbstractUnitNode> {
    private ChooseUUTTreeItem() {
        setValue(new AbstractUnitNode());
    }
    public ChooseUUTTreeItem(AbstractUnitNode node) {
        setValue(node);

        if (canBeExpanded(this.getValue())) {
            getChildren().add(new ChooseUUTTreeItem());
        }

        expandedProperty().addListener((observable, oldValue, newValue) -> {
            BooleanProperty bb = (BooleanProperty) observable;
            ChooseUUTTreeItem item = (ChooseUUTTreeItem) bb.getBean();

            if (item.canBeExpanded(item.getValue())) {
                if (isExpanded()) {
                    item.loadChildren(item.getValue());
                } else {
                    item.getChildren().clear();
                    item.getChildren().add(new ChooseUUTTreeItem());
                }
            }
        });

    }

    private boolean canBeExpanded(AbstractUnitNode node) {
        if (node == null) {
            return false;
        } else return node.getChildren().size() > 0;
    }

    private void loadChildren(AbstractUnitNode node) {
        if (canBeExpanded(node)) {
            this.getChildren().clear();

            List<AbstractUnitNode> itemChildren = node.getChildren();
            for (AbstractUnitNode child : itemChildren)
            {
                this.getChildren().add(new ChooseUUTTreeItem(child));
            }
        }

    }
}
