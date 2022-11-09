package com.dse.testcasescript.object;

public class TestReplaceNode extends TestActionNode {
    @Override
    public String exportToFile() {
        StringBuilder output = new StringBuilder(TEST_REPLACE);

        for (ITestcaseNode child : getChildren())
            output.append("\n").append(child.exportToFile());

        output.append("\n" + TEST_END);
        return output.toString();
    }
}
