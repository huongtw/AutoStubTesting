package com.dse.coverage.highlight;

public class HighlightedOffsetForNormalStatement extends HighlightedOffset{

    @Override
    protected Object clone() throws CloneNotSupportedException {
        HighlightedOffsetForNormalStatement cloned = new HighlightedOffsetForNormalStatement();
        cloned.setEndOffset(this.getEndOffset());
        cloned.setStartOffset(this.getStartOffset());
        return cloned;
    }

    @Override
    public String toString() {
        return "(" + startOffset + ":" + endOffset + ")";
    }
}
