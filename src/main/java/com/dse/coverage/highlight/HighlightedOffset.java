package com.dse.coverage.highlight;

public abstract class HighlightedOffset {
    protected int startOffset; // source code file level
    protected int endOffset; // source code file level

    public HighlightedOffset() {
    }

    public void setEndOffset(int endOffset) {
        this.endOffset = endOffset;
    }

    public int getEndOffset() {
        return endOffset;
    }

    public void setStartOffset(int startOffset) {
        this.startOffset = startOffset;
    }

    public int getStartOffset() {
        return startOffset;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof HighlightedOffset) {
            if (((HighlightedOffset) obj).getEndOffset() == this.getEndOffset()
                    && ((HighlightedOffset) obj).getStartOffset() == this.getStartOffset())
                return true;
            else
                return false;
        } else
            return false;
    }
}
