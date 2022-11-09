package com.dse.thread.task.testcase_selection;

import com.dse.testcasescript.SelectionUpdater;
import com.dse.testcasescript.object.ITestcaseNode;
import com.dse.logger.AkaLogger;

import java.util.List;

public class RetrieveTestCaseNodeTask extends AbstractRetrieveNodeTask {

    private static final AkaLogger logger = AkaLogger.get(RetrieveTestCaseNodeTask.class);

    @Override
    protected List<ITestcaseNode> call() throws Exception {
        logger.debug("Retrieve all selected test case nodes on navigator");
        return SelectionUpdater.getAllSelectedTestcases(root);
    }

}
