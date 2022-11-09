package com.dse.parser.object;

import com.dse.parser.dependency.Dependency;
import com.dse.util.Utils;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Represent a node in the tree
 *
 * @author ducanhnguyen
 */
public abstract class Node implements INode {

    private String name = "";

    private int id;

    protected String absolutePath = "";

    private List<Node> children = new ArrayList<>();

    protected INode parent = null;

    private List<Dependency> dependencies = new ArrayList<>();
    
    private Icon icon = null;

    private boolean isIdSet = false;

    public Node() {
    }

    @Override
    public String getNewType() {
        return name;
    }

    @Override
    public INode getParent() {
        return parent;
    }

    @Override
    public void setParent(INode parent) {
        this.parent = parent;
    }

    @Override
    
    public String toString() {
        return getNewType();
    }

    @Override
    public INode clone() {
        try {
            return (INode) super.clone();
        } catch (CloneNotSupportedException e) {
            // e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getAbsolutePath() {
        return absolutePath;
    }

    @Override
    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = Utils.normalizePath(absolutePath);
        name = this.absolutePath.substring(this.absolutePath.lastIndexOf(File.separator) + File.separator.length());
    }

    @Override
    public List<Node> getChildren() {
        return children;
    }

    @Override
    public void setChildren(List<Node> children) {
        this.children = children;
    }

    @Override
    public List<Dependency> getDependencies() {
        return dependencies;
    }

    @Override
    public void setDependencies(List<Dependency> dependencies) {
        this.dependencies = dependencies;
    }

    @Override
    public Icon getIcon() {
        return icon;
    }

    protected void setIcon(Icon icon) {
        this.icon = icon;
    }

    @Override
    public int getId() {
        if (!isIdSet) {
            id = getAbsolutePath().hashCode();
            isIdSet = true;
        }

        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Node) {
            Node objCast = (Node) obj;
            return objCast.getAbsolutePath().equals(getAbsolutePath());
        } else
            return true;
    }

    @Override
    public String getRelativePathToRoot() {
        return getAbsolutePath().replace(Utils.getRoot(this).getAbsolutePath(), "");
    }
}
