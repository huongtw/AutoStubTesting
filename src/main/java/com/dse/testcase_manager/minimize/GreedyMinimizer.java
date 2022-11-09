package com.dse.testcase_manager.minimize;

import auto_testcase_generation.cfg.object.ICfgNode;
import com.dse.environment.Environment;
import com.dse.testcase_manager.ITestCase;
import com.dse.testcase_manager.TestCase;
import com.dse.util.TestPathUtils;

import java.util.*;

public class GreedyMinimizer extends TestCaseMinimizer {

    @Override
    public List<TestCase> minimize(List<TestCase> testCases, Scope scope) {
        List<TestCase> minimizedSet = new ArrayList<>();

        if (!testCases.isEmpty()) {

            List<Object> allNodes = new ArrayList<>();
            Map<TestCase, List<Object>> subsets = new HashMap<>();

            testCases.forEach(tc -> {
                logger.debug("Gather information in " + tc.getName());
                String coverageType = Environment.getInstance().getTypeofCoverage();
                List<Object> visitedNodes = TestPathUtils.getVisited(tc, scope, coverageType);
                visitedNodes.forEach(n -> {
                    if (!allNodes.contains(n))
                        allNodes.add(n);
                });
                subsets.put(tc, visitedNodes);
            });

            List<Object> uncoveredNodes = new ArrayList<>(allNodes);

            while (!uncoveredNodes.isEmpty()) {
                /* select a subset that has the maximum number of uncovered elements */
                /*
                 * select selectedNodes belong to subsets
                 * that |selectedNodes communicate uncoveredNodes| is maximum
                 */
                Map.Entry<TestCase, List<Object>> selectedNodes = subsets.entrySet().stream()
                        .max(new Comparator<Map.Entry<TestCase, List<Object>>>() {
                            @Override
                            public int compare(Map.Entry<TestCase, List<Object>> o1, Map.Entry<TestCase, List<Object>> o2) {
//                                logger.debug("Comparing test case " + o1.getKey().getName() + " with " + o2.getKey().getName());
                                List<Object> list1 = o1.getValue();
                                List<Object> list2 = o2.getValue();
                                int common1 = countCommon(list1, uncoveredNodes);
                                int common2 = countCommon(list2, uncoveredNodes);
                                if (common1 == common2) {
                                    return compareStatus(o1.getKey(), o2.getKey());
                                } else {
                                    return Integer.compare(common1, common2);
                                }
                            }
                        })
                        .orElse(null);

                if (selectedNodes == null)
                    break;

                uncoveredNodes.removeIf(n -> selectedNodes.getValue().contains(n));
                logger.debug("Select " + selectedNodes.getKey().getName());
                minimizedSet.add(selectedNodes.getKey());
            }
        }

        return minimizedSet;
    }

    private int compareStatus(TestCase t1, TestCase t2) {
        int v1 = TestCase.STATUS_SUCCESS.equals(t1.getStatus()) ? 1 : 0;
        int v2 = TestCase.STATUS_SUCCESS.equals(t2.getStatus()) ? 1 : 0;
        return Integer.compare(v1, v2);
    }

    private int countCommon(List<Object> selectedNodes, List<Object> uncoveredNodes) {
        List<Object> common = new ArrayList<>(selectedNodes);
        common.retainAll(uncoveredNodes);
        return common.size();
    }
}
