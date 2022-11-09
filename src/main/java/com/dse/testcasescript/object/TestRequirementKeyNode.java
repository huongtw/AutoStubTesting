package com.dse.testcasescript.object;

public class TestRequirementKeyNode extends AbstractTestcaseNode {
    private String requirement;

    public String getRequirement() {
        return requirement;
    }

    public void setRequirement(String requirement) {
        this.requirement = requirement;
    }

    @Override
    public String toString() {
        return super.toString() + ": " + getRequirement();
    }

    @Override
    public String exportToFile() {
        return TEST_REQUIREMENT_KEY + requirement;
    }
}
