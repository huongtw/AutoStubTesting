package com.dse.util.tostring;

import com.dse.parser.object.INode;
import com.dse.parser.object.Node;

public class ConditionTreeDisplayer extends ToString {

    public ConditionTreeDisplayer(INode root) {
        super(root);
    }

    private void displayTree(INode n, int level) {
        if (n == null)
            return;
        else {
            treeInString += genTab(level) + "[" + n.getClass().getSimpleName() + "] " + n.getNewType() + "\n";

        }
        for (Object child : n.getChildren()) {
            displayTree((Node) child, ++level);
            level--;
        }
    }

    @Override
    public String toString(INode n) {
        displayTree(n, 0);
        return treeInString;
    }
}
