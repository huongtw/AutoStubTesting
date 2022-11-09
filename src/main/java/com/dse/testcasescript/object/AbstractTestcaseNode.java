package com.dse.testcasescript.object;

import com.dse.testcasescript.ITestcaseCommandList;

import java.util.ArrayList;
import java.util.List;

public class AbstractTestcaseNode implements ITestcaseNode, ITestcaseCommandList {
    private ITestcaseNode parent;
    private List<ITestcaseNode> children = new ArrayList<>();
    private boolean isSelectedInTestcaseNavigator = false;

    @Override
    public ITestcaseNode getParent() {
        return parent;
    }

    @Override
    public void setParent(ITestcaseNode parent) {
        this.parent = parent;
    }

    @Override
    public List<ITestcaseNode> getChildren() {
        return children;
    }

    public void setChildren(List<ITestcaseNode> children) {
        this.children = children;
    }

    @Override
    public void addChild(ITestcaseNode child) {
        getChildren().add(child);
        child.setParent(this);
    }

    @Override
    public String exportToFile() {
        return ""; // no content by default
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
    public String toString() {
        return "[" + getClass().getSimpleName() + "] ";
    }

    public void setSelectedInTestcaseNavigator(boolean selectedInTestcaseNavigator) {
        isSelectedInTestcaseNavigator = selectedInTestcaseNavigator;
    }

    public boolean isSelectedInTestcaseNavigator() {
        return isSelectedInTestcaseNavigator;
    }
}
