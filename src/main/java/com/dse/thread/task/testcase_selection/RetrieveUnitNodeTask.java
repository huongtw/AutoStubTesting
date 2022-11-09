package com.dse.thread.task.testcase_selection;

import com.dse.testcasescript.SelectionUpdater;
import com.dse.testcasescript.object.ITestcaseNode;

import java.util.List;

public class RetrieveUnitNodeTask extends AbstractRetrieveNodeTask {

    @Override
    protected List<ITestcaseNode> call() throws Exception {
        return SelectionUpdater.getAllSelectedSourcecodeNodes(root);
    }

}
