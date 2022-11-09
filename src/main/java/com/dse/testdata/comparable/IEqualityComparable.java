package com.dse.testdata.comparable;

public interface IEqualityComparable extends IComparable {
    String assertEqual(String expected, String actual);
    String assertNotEqual(String expected, String actual);
}
