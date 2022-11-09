package com.dse.thread.task;

import com.dse.thread.AbstractAkaTask;

public class RunInAnotherThreadTask extends AbstractAkaTask<Object> {
    MyCallBack callBack;

    @Override
    protected Object call() throws Exception {
        if (callBack != null) {
            callBack.call();
        }

        return null;
    }

    public interface MyCallBack {
        void call();
    }

    public void setCallBack(MyCallBack callBack) {
        this.callBack = callBack;
    }
}
