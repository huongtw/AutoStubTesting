package com.dse.testcase_manager;

import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.INode;
import com.dse.logger.AkaLogger;
import com.dse.util.Utils;

public class TestCaseSlot {
    private final static AkaLogger logger = AkaLogger.get(TestCaseSlot.class);
    private int slotNum;
    private String unit;
    private String subprogramName;
    private int numberOfIterations = 1; // by default
    private String testcaseName; // All capital
    private boolean isBreak = false; // by default
    private int delay = 0; // in seconds, before executing the next slot

    private TestCaseSlot() {

    }

    public TestCaseSlot(TestCase testCase) {
        ICommonFunctionNode functionNode = testCase.getRootDataNode().getFunctionNode();
        if (functionNode != null) {
            INode sourceNode = Utils.getSourcecodeFile(functionNode);
            unit = sourceNode.getName();
            subprogramName = testCase.getRootDataNode().getFunctionNode().getName();
            testcaseName = testCase.getName();
            delay = 1;
        } else {
            logger.error("The function node of the test case is null");
        }
    }

    public TestCaseSlot(CompoundTestCase compoundTestCase) {
        unit = "<<COMPOUND>>";
        subprogramName = "<<COMPOUND>>";
        testcaseName = compoundTestCase.getName();
        delay = 10;
    }

    @Override
    protected TestCaseSlot clone() {
        TestCaseSlot slot = new TestCaseSlot();
        slot.slotNum = slotNum;
        slot.unit = unit;
        slot.subprogramName = subprogramName;
        slot.numberOfIterations = numberOfIterations;
        slot.testcaseName = testcaseName;
        slot.isBreak = isBreak;
        slot.delay = delay;
        return slot;
    }

    public boolean validate() {
        return TestCaseManager.checkTestCaseExisted(testcaseName);
    }

    public int getSlotNum() {
        return slotNum;
    }

    public void setSlotNum(int slotNum) {
        this.slotNum = slotNum;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getSubprogramName() {
        return subprogramName;
    }

    public void setSubprogramName(String subprogramName) {
        this.subprogramName = subprogramName;
    }

    public int getNumberOfIterations() {
        return numberOfIterations;
    }

    public void setNumberOfIterations(int numberOfIterations) {
        this.numberOfIterations = numberOfIterations;
    }

    public String getTestcaseName() {
        return testcaseName;
    }

    public void setTestcaseName(String testcaseName) {
        this.testcaseName = testcaseName;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public boolean isBreak() {
        return isBreak;
    }

    public void setBreak(boolean aBreak) {
        isBreak = aBreak;
    }

    @Override
    public String toString() {
        return "TestCaseSlot{" +
                "slotNum=" + slotNum +
                ", unit='" + unit + '\'' +
                ", subprogramName='" + subprogramName + '\'' +
                ", numberOfIterations=" + numberOfIterations +
                ", testcaseName='" + testcaseName + '\'' +
                ", isBreak=" + isBreak +
                ", delay=" + delay +
                '}';
    }
}
