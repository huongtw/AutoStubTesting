package com.dse.thread.task.testcase_selection;

import com.dse.environment.Environment;
import com.dse.testcase_manager.ITestCase;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.testcasescript.object.ITestcaseNode;
import com.dse.testcasescript.object.TestNameNode;
import com.dse.thread.AbstractAkaTask;
import com.dse.logger.AkaLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class RetrieveTestCaseTask extends AbstractAkaTask<List<ITestCase>> {

    private static final AkaLogger logger = AkaLogger.get(RetrieveTestCaseTask.class);

    @Override
    protected List<ITestCase> call() throws Exception {
        List<ITestCase> testCases = new ArrayList<>();

        if (Environment.getInstance().isCoverageModeActive()) {
            List<ITestcaseNode> nodes = getAllSelectedNodes();

            testCases = nodes.stream()
                    .filter(n -> n instanceof TestNameNode)
                    .map(n -> {
                        String testCaseName = ((TestNameNode) n).getName();
                        logger.debug("Loading test case " + testCaseName);
                        return TestCaseManager.getTestCaseByName(testCaseName);
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        return testCases;
    }

    @Override
    public List<ITestCase> get() {
        try {
            return super.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private List<ITestcaseNode> getAllSelectedNodes() throws Exception {
        RetrieveTestCaseNodeTask testCaseNodeTask = new RetrieveTestCaseNodeTask();

        ExecutorService es = Executors.newSingleThreadExecutor();
        es.submit(testCaseNodeTask);

        es.shutdown();
        es.awaitTermination(5, TimeUnit.MINUTES);

        return testCaseNodeTask.get();
    }
}
