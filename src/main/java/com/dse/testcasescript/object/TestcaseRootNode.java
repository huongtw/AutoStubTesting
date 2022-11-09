package com.dse.testcasescript.object;

public class TestcaseRootNode extends AbstractTestcaseNode {
    private String absolutePath;

    public String exportToFile() {
        StringBuilder output = new StringBuilder();
        for (ITestcaseNode child : getChildren()) {
            output.append(child.exportToFile()).append("\n");
        }
        return output.toString();
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }
}
