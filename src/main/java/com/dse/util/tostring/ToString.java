package com.dse.util.tostring;


import com.dse.parser.object.INode;

public abstract class ToString {

    protected String treeInString = "";

    public ToString(INode root) {
        treeInString = this.toString(root);
    }

    protected String genTab(int level) {
        StringBuilder tab = new StringBuilder();
        for (int i = 0; i < level; i++)
            tab.append("     ");
        return tab.toString();
    }

    public String getTreeInString() {
        return treeInString;
    }

    abstract public String toString(INode n);
}
