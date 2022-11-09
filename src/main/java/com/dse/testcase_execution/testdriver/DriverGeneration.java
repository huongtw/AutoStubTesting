package com.dse.testcase_execution.testdriver;

import com.dse.util.SpecialCharacter;

public abstract class DriverGeneration implements IDriverGeneration {

    protected String testDriver = SpecialCharacter.EMPTY;

    protected abstract String getTestDriverTemplate();

    @Override
    public String getTestDriver() {
        return testDriver;
    }

    @Override
    public String toString() {
        return "DriverGeneration: " + testDriver;
    }
}
