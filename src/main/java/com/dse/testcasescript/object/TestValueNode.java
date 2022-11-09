package com.dse.testcasescript.object;

public class TestValueNode extends AbstractTestcaseNode {
    private String identifier;
    private String value;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return super.toString() + ": identifier = " + getIdentifier() + ", value = " + getValue();
    }

    @Override
    public String exportToFile() {
        return TEST_VALUE + getIdentifier() + DELIMITER_BETWEEN_KEY_AND_VALUE + getValue();
    }

    public static final int TEST_VALUE_KEYWORD = 0;
    public static final int IDENTIFIER_INDEX = 1;
    public static final int VALUE_INDEX = 2;
    public static final String DELIMITER_BETWEEN_KEY_AND_VALUE = "=";
}
