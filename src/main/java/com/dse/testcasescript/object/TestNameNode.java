package com.dse.testcasescript.object;

public class TestNameNode extends AbstractTestcaseNode {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return super.toString() + ": name = " + getName();
    }

    @Override
    public String exportToFile() {
        return TEST_NAME + getName();
    }
}
