package com.dse.testcasescript.object;

/**
 * Slot means a test case in a compound
 */
public class TestSlotNode extends AbstractTestcaseNode {
    private int slotNum;
    private String unit;
    private String subprogramName;
    private int numberOfIterations = 1; // by default
    private String testcaseName; // All capital
    private int delay = 0; // in seconds, before executing the next slot

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

    @Override
    public String exportToFile() {
        return TEST_SLOT + getSlotNum() + "," + getUnit() + "," + getSubprogramName() + "," + getNumberOfIterations() + "," + getTestcaseName() + "," + getDelay();
    }

    @Override
    public String toString() {
        return super.toString() + ": slotnum =" + getSlotNum() + ", subprogram name = " + getSubprogramName() + "...";
    }

    public static int SLOT_INDEX = 0;
    public static int UNIT_INDEX = 1;
    public static int SUBPROGRAM_NAME_INDEX = 2;
    public static int NUMBER_OF_ITERATIONS_INDEX = 3;
    public static int TESTCASE_NAME_INDEX = 4;
    public static int DELAY_INDEX = 5;

    public static String DELIMITER_BETWEEN_ATTRIBUTES = ",";
}
