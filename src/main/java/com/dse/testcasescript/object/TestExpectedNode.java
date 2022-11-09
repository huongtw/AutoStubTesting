package com.dse.testcasescript.object;

/**
 * This command is used to provide expected values for parameters and global objects.
 * <p>
 * Example 1: "TEST.EXPECTED: math.sine.return:0.0..1.0"
 * Example 2: "TEST.EXPECTED:namespaces.func_is_implemented.return:false"
 */
public class TestExpectedNode extends AbstractTestcaseNode {
    private String unit;
    private String subprogram;
    private String parameter;
    private String expectedValue;

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getSubprogram() {
        return subprogram;
    }

    public void setSubprogram(String subprogram) {
        this.subprogram = subprogram;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public String getExpectedValue() {
        return expectedValue;
    }

    public void setExpectedValue(String expectedValue) {
        this.expectedValue = expectedValue;
    }

    @Override
    public String exportToFile() {
        return String.format("%s %s.%s.%s%s%s",
                TEST_EXPECTED, getUnit(), getSubprogram(), getParameter(), DELIMITER_BETWEEN_KEY_AND_VALUE, getExpectedValue());
    }

    public static final String DELIMITER_BETWEEN_KEY_AND_VALUE = ":";
    public static final String DELIMITER_BETWEEN_ATTRIBUTES = "\\.";
    public static final int UNIT_INDEX_IN_IDENTIFIER = 0;
    public static final int SUBPROGRAM_INDEX_IN_IDENTIFIER = 1;
    public static final int PARAMETER_INDEX_IN_IDENTIFIER = 2;
}
