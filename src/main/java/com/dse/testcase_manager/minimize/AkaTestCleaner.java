package com.dse.testcase_manager.minimize;

import com.dse.environment.Environment;
import com.dse.testcase_manager.TestCase;
import com.dse.util.TestPathUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Deprecated
public class AkaTestCleaner extends TestCaseMinimizer {
    
    @Override
    public List<TestCase> minimize(List<TestCase> testCases, Scope scope) {
        if (testCases.size() == 1)
            return testCases;

        List<TestCase> optimizes = new ArrayList<>(testCases);

        int index = 0;

        while (index < optimizes.size()) {
            TestCase testCase = optimizes.get(index);
            List<TestCase> others = optimizes.stream()
                    .filter(tc -> !tc.equals(testCase))
                    .collect(Collectors.toList());

            boolean stop = false;

            logger.debug("Comparing test case " + testCase.getName() + " with others");
            String coverageType = Environment.getInstance().getTypeofCoverage();
            int comparison = TestPathUtils.compare(testCase, others, scope, coverageType);

            switch (Math.abs(comparison)) {
                case TestPathUtils.EQUAL:
                    optimizes.removeAll(others);
                    stop = true;
                    break;
                case TestPathUtils.HAVENT_EXEC:
                case TestPathUtils.SEPARATED_GT:
                case TestPathUtils.COMMON_GT:
                    index++;
                    break;
                case TestPathUtils.CONTAIN_GT:
                    // test case contains all others
                    if (comparison > 0) {
                        optimizes.removeAll(others);
                        stop = true;
                    }
                    // others contains current test case
                    // then remove it from list
                    else
                        optimizes.remove(testCase);

                    break;
                case TestPathUtils.ERR_COMPARE:
                    stop = true;
                    logger.error("Something wrong between " + testCase.getName() + " and the others");
                    break;
            }

            if (stop)
                break;
        }

        return optimizes;
    }
}
