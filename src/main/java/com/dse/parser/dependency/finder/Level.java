package com.dse.parser.dependency.finder;

import com.dse.parser.object.INode;
import com.dse.parser.object.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * A level represents a list of equivalent
 *
 * @author DucAnh
 */
public class Level extends ArrayList<Node> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public static final String STRUCTUTRE_AND_NAMESPACE_SCOPE = "STRUCTUTRE AND NAMESPACE SCOPE";
    public static final String FILE_SCOPE = "FILE SCOPE";
    public static final String INCLUDED_SCOPE = "INCLUDED SCOPE";
    public static final String EXTENDED_INCLUDED_SCOPE = "EXTENDED INCLUDED SCOPE";

    private String name;

    public Level() {

    }

    public Level(List<Node> node) {
        this.addAll(node);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        for (INode n : this)
            output.append(n.getAbsolutePath()).append(", ");
        return output.toString();
    }
}
