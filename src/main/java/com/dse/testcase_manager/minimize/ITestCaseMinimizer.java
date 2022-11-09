package com.dse.testcase_manager.minimize;

import com.dse.testcase_manager.TestCase;

import java.util.List;

public interface ITestCaseMinimizer {

    void clean(List<TestCase> testCases, Scope scope);

    List<TestCase> minimize(List<TestCase> testCases, Scope scope) throws Exception;
}
