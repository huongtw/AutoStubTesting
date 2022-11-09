package com.dse.testcasescript.object;

import com.dse.logger.AkaLogger;

import java.util.List;

public class TestValueUserCodeNode extends AbstractTestcaseNode {
    final static AkaLogger logger = AkaLogger.get(TestValueUserCodeNode.class);

    private String unit;
    private String subprogram;
    private String parameter;
    private String userCode;

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
            setUserCode(block.get(1)); // Example: {{ <<file_io.CreateFile.return>> != NULL }}

        } else {
            logger.error("Size of block in " + getClass() + " > 2");
        }
    }

    @Override
    public String toString() {
        return super.toString() + ": unit = " + getUnit() + ", subprogram = " + getSubprogram() + ", parameter = " + getParameter() + ", user code = " + getUserCode();
    }

    @Override
    public String exportToFile() {
        // For example:
        // "TEST.VALUE_USER_CODE:file_io.WriteLine.fp
        //<<file_io.WriteLine.*fp>> = ( <<file_io.CreateFile.return>> );
        //TEST.END_VALUE_USER_CODE:"
        return String.format("%s %s.%s.%s\n%s\n%s",
                TEST_VALUE_USER_CODE, getUnit(), getSubprogram(), getParameter(), getUserCode(), TEST_END_VALUE_USER_CODE);
    }


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

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }
}
