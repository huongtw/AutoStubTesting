package com.dse.testdata.comparable;

public interface IValueComparable extends IEqualityComparable {
    String assertLower(String expected, String actual);
    String assertGreater(String expected, String actual);
    String assertLowerOrEqual(String expected, String actual);
    String assertGreaterOrEqual(String expected, String actual);
}
