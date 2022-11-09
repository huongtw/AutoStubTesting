package com.dse.testcasescript.object;

import java.util.List;

public interface ITestcaseNode {

    void setSelectedInTestcaseNavigator(boolean selectedInTestcaseNavigator);

    boolean isSelectedInTestcaseNavigator();

    List<ITestcaseNode> getChildren();

    void addChild(ITestcaseNode child);

    void setParent(ITestcaseNode parent);

    ITestcaseNode getParent();

    String exportToFile();

    List<String> getBlockOfTag(String endOfBlockSignal, int fromLineIndex, String[] lines);
}
