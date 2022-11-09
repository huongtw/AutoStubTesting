package com.dse.thread.task.testcase_selection;

import com.dse.environment.Environment;
import com.dse.testcasescript.object.ITestcaseNode;
import com.dse.thread.AbstractAkaTask;
import com.dse.thread.AkaThread;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public abstract class AbstractRetrieveNodeTask extends AbstractAkaTask<List<ITestcaseNode>> {

    protected ITestcaseNode root;

    protected AbstractRetrieveNodeTask() {
        this.root = Environment.getInstance().getTestcaseScriptRootNode();
    }

    @Override
    public List<ITestcaseNode> get() {
        try {
            return super.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void start() {
        new AkaThread(this).start();
    }
}
