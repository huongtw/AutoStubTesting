package com.dse.coverage.highlight;

public class HighlightedOffsetForBranch extends HighlightedOffset {
    private boolean isVisitedTrue = false;
    private boolean isVisitedFalse = false;

    public boolean isVisitedFalse() {
        return isVisitedFalse;
    }

    public void setVisitedFalse(boolean visitedFalse) {
        isVisitedFalse = visitedFalse;
    }

    public boolean isVisitedTrue() {
        return isVisitedTrue;
    }

    public void setVisitedTrue(boolean visitedTrue) {
        isVisitedTrue = visitedTrue;
    }

    @Override
    public String toString() {
        return "(" + startOffset + ":" + endOffset + ", + isVisitedTrue = " + isVisitedTrue + "; isVisitedFalse: " + isVisitedFalse + ")";
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        HighlightedOffsetForBranch cloned = new HighlightedOffsetForBranch();
        cloned.setEndOffset(this.getEndOffset());
        cloned.setStartOffset(this.getStartOffset());
        cloned.setVisitedFalse(this.isVisitedFalse());
        cloned.setVisitedTrue(this.isVisitedTrue());
        return cloned;
    }
}
