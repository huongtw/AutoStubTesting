package com.dse.parser.object;

import com.dse.parser.dependency.Dependency;

import javax.swing.*;
import java.util.List;

/**
 * Interface represents a node in a tree, e.g., structure tree
 *
 * @author DucAnh
 */
public interface INode {

    INode clone();

    String getAbsolutePath();

    void setAbsolutePath(String absolutePath);

    List<Node> getChildren();

    void setChildren(List<Node> children);

    List<Dependency> getDependencies();

    void setDependencies(List<Dependency> dependencies);

    Icon getIcon();

    String getNewType();

    INode getParent();

    void setParent(INode parent);

    String getName();

    void setName(String name);

    int getId();

    void setId(int id);

    String getRelativePathToRoot();
}
