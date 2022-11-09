package com.dse.testcasescript.object;

import java.util.List;

public class TestCommentNode extends AbstractTestcaseNode {
    private String comment;

    @Override
    public List<ITestcaseNode> getChildren() {
        return super.getChildren();
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    @Override
    public String exportToFile() {
        return getComment();
    }

    @Override
    public String toString() {
        return super.toString() + " " + getComment();
    }
}
