package com.dse.testcase_execution.result_trace.gtest;

import com.dse.testcase_execution.result_trace.AbstractResultTrace;
import com.dse.util.SpecialCharacter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


@XmlType(propOrder = {"message"})
public class Failure extends AbstractResultTrace {

    @XmlAttribute
    private String message;

    public String getMessage() {
        return message;
    }

    /*
     * Example:
     *
     *       Expected: AKA_STUB_r
     *       Which is: 4            [i - 3] actual
     * To be equal to: r
     *       Which is: 3            [i - 1] expected
     * Aka function calls: 2        [i]     fcalls tag
     *
     */

    public String getActual() {
        String[] lines = getMessageLines(message);
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].trim().startsWith(FCALL_TAG)) {
                return lines[i - 3].substring(OFFSET).trim();
            }
        }

        return PROBLEM_VALUE;
    }

    @Override
    public String getExpectedName() {
        String[] lines = getMessageLines(message);
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].trim().startsWith(FCALL_TAG)) {
                return lines[i - 2].substring("To be equal to: ".length()).trim();
            }
        }
        return PROBLEM_VALUE;
    }

    @Override
    public String getActualName() {
        String[] lines = getMessageLines(message);
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].trim().startsWith(FCALL_TAG)) {
                return lines[i - 4].substring("Expected: ".length());
            }
        }
        return PROBLEM_VALUE;
    }

    public String getExpected() {
        String[] lines = getMessageLines(message);
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].trim().startsWith(FCALL_TAG)) {
                return lines[i - 1].substring(OFFSET);
            }
        }

        return PROBLEM_VALUE;
    }

    protected static final int OFFSET = 10;

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder("Failure: ");

        for (String line : getMessageLines(message)) {
            output.append(line).append(SpecialCharacter.LINE_BREAK);
        }

        return output.toString();
    }
}
