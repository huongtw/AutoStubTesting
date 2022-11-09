package com.dse.environment.object;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractEnvironmentNode implements IEnvironmentNode, ICommandList {
    private List<IEnvironmentNode> children = new ArrayList<>();
    private IEnvironmentNode parent = null;

    @Override
    public void addChild(IEnvironmentNode node) {
        if (!children.contains(node)) {
            children.add(node);
            node.setParent(this);
        }
    }

    @Override
    public List<IEnvironmentNode> getChildren() {
        return children;
    }

    @Override
    public IEnvironmentNode getParent() {
        return parent;
    }

    @Override
    public void setParent(IEnvironmentNode parent) {
        this.parent = parent;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    public List<String> getBlockOfTag(String endOfBlockSignal, int fromLineIndex, String[] lines) {
        List<String> block = new ArrayList<>();
        while (fromLineIndex < lines.length && !lines[fromLineIndex].trim().startsWith(endOfBlockSignal)) {
            block.add(lines[fromLineIndex]);
            fromLineIndex++;
        }
        block.add(endOfBlockSignal);
        return block;
    }

    @Override
    public String exportToFile() {
        return ENVIRO_NEW;
    }
}
