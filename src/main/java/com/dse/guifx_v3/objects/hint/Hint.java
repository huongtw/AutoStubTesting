package com.dse.guifx_v3.objects.hint;

import javafx.scene.Node;
import javafx.scene.control.Tooltip;

public class Hint {

    public static void tooltipNode(Node Node, String text) {
        Tooltip tooltip = new Tooltip(text);
        Node.setOnMouseExited(event -> {
            tooltip.hide();
        });

        Node.setOnMouseEntered(event -> {
            tooltip.show(Node, event.getScreenX() + 10, event.getScreenY() + 0.0);
        });
    }
}
