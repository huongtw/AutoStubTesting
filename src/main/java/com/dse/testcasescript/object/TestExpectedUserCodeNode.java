package com.dse.testcasescript.object;

import com.dse.logger.AkaLogger;

import java.util.List;

/**
 * Example:
 * "TEST.EXPECTED_USER_CODE:file_io.CreateFile.return
 * {{ <<file_io.CreateFile.return>> != NULL }}
 * TEST.END_EXPECTED_USER_CODE:"
 */
public class TestExpectedUserCodeNode extends AbstractTestcaseNode {
    final static AkaLogger logger = AkaLogger.get(TestExpectedUserCodeNode.class);
    private String unit;
    private String subprogram;
    private String parameter;
    private String expectedUserCode;

    public void analyzeBlock(List<String> block) {
        if (block.size() == 3) {
            // get unit, subprogram, and parameter (first line)
            String firstLine = block.get(0); // Example: "TEST.EXPECTED_USER_CODE:file_io.CreateFile.return"
            String DELIMITER_BETWEEN_ATTRIBUTES = "\\.";
            final int UNIT_INDEX = 0;
            final int SUBPROGRAM_INDEX = 1;
            final int PARAMETER_INDEX = 2;
            String[] tokens = firstLine.substring(firstLine.indexOf(AbstractTestcaseNode.DELIMITER_BETWEEN_COMMAND_AND_VALUE) + 1)
                    .split(DELIMITER_BETWEEN_ATTRIBUTES);
            setUnit(tokens[UNIT_INDEX]);
            setParameter(tokens[PARAMETER_INDEX]);
            setSubprogram(tokens[SUBPROGRAM_INDEX]);

            // get expected user code (second line)
            setExpectedUserCode(block.get(1)); // Example: {{ <<file_io.CreateFile.return>> != NULL }}

        } else {
            logger.error("Size of block in " + getClass() + " > 2");
        }
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public String getSubprogram() {
        return subprogram;
    }

    public void setSubprogram(String subprogram) {
        this.subprogram = subprogram;
    }

    public String getExpectedUserCode() {
        return expectedUserCode;
    }

    public void setExpectedUserCode(String expectedUserCode) {
        this.expectedUserCode = expectedUserCode;
    }

    @Override
    public String exportToFile() {
        // For example:
        // "TEST.EXPECTED_USER_CODE:file_io.CreateFile.return
        //{{ <<file_io.CreateFile.return>> != NULL }}
        //TEST.END_EXPECTED_USER_CODE:"
        return TEST_EXPECTED_USER_CODE + getUnit() + "." + getSubprogram() + "." + getParameter() + "\n" + getExpectedUserCode() + "\n" + TEST_END_EXPECTED_USER_CODE;
    }

    @Override
    public String toString() {
        return super.toString() + ": unit.subprogram.parameter = " + getUnit() + "." + getSubprogram() + "." + getParameter() + "; expected user code = \"" + getExpectedUserCode() + "\"";
    }
}
