package com.dse.environment.object;

import java.util.List;

public interface IEnvironmentNode {
    void addChild(IEnvironmentNode node);

    List<IEnvironmentNode> getChildren();

    void setParent(IEnvironmentNode parent);

    IEnvironmentNode getParent();

    String toString();

    /**
     * Export to file
     * @return
     */
    String exportToFile();

    List<String> getBlockOfTag(String endOfBlock, int startLine, String[] lines);
}
