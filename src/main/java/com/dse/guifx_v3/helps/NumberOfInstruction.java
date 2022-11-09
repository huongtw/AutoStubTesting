package com.dse.guifx_v3.helps;

import com.google.gson.annotations.Expose;

public class NumberOfInstruction {
    @Expose
    private String srcNodePath;
    @Expose
    private int nStatements; // number of statements in the function node
    @Expose
    private int nBranches; // number of branches in the function node

    public NumberOfInstruction() {
    }

    public String getSrcNodePath() {
        return srcNodePath;
    }

    public void setSrcNodePath(String srcNodePath) {
        this.srcNodePath = srcNodePath;
    }

    public int getnStatements() {
        return nStatements;
    }

    public void setnStatements(int nStatements) {
        this.nStatements = nStatements;
    }

    public int getnBranches() {
        return nBranches;
    }

    public void setnBranches(int nBranches) {
        this.nBranches = nBranches;
    }
}
