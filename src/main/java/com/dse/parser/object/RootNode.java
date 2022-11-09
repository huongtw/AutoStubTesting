package com.dse.parser.object;

import com.dse.util.NodeType;
import com.dse.util.UtilsVu;

import java.util.ArrayList;
import java.util.List;

/**
 * Bieu dien cac node trong Function Detail Tree
 * Ex: GLOBAL, UUT, STUB, ...
 *
 * @author TungLam
 */
public class RootNode extends Node {

    private NodeType type;

    List<INode> elements = new ArrayList<>();

    public RootNode(NodeType type) {
        this.type = type;
    }

    @Override
    public String getName() {
        return UtilsVu.getTypeRoot(type);
    }

    public NodeType getType() {
        return type;
    }

    public void setType(NodeType type) {
        this.type = type;
    }

    public List<INode> getElements() {
        return elements;
    }

    public void addElements(List<INode> elements) {
        this.elements.addAll(elements);
    }

    public void addElement(INode element) {
        this.elements.add(element);
        this.getChildren().add((Node) element);
    }

    public void removeElement(INode element) {
        elements.remove(element);
        this.getChildren().remove(element);
    }

    @Override
    public String toString(){
        return getName();
    }
}
