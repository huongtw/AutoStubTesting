package com.dse.testcasescript.object;

public class TestAddNode extends TestActionNode {
    @Override
    public String exportToFile() {
        StringBuilder output = new StringBuilder(TEST_ADD);

        for (ITestcaseNode child : getChildren())
            output.append("\n").append(child.exportToFile());

        output.append("\n" + TEST_END);
        return output.toString();
    }
}
