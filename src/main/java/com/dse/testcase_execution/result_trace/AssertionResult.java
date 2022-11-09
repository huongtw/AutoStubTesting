package com.dse.testcase_execution.result_trace;

public class AssertionResult {

    private int pass, total;

    public AssertionResult() {
        pass = 0;
        total = 0;
    }

    public int increasePass() {
        pass++;
        return pass;
    }

    public int increaseTotal() {
        total++;
        return total;
    }

    public int getPass() {
        return pass;
    }

    public int getTotal() {
        return total;
    }

    public void setPass(int pass) {
        this.pass = pass;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public boolean isAllPass() {
        return pass == total;
    }

    public void append(AssertionResult other) {
        total += other.total;
        pass += other.pass;
    }

    public double getPercent() {
        return (double) pass * 100 / (double) total;
    }
}
