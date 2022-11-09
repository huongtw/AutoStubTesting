package com.dse.testdata;

import com.dse.testdata.object.ValueDataNode;

public class Iterator {

    private ValueDataNode dataNode;

    private int startIdx = 1;

    private int repeat = FILL_ALL;

    public static final int FILL_ALL = -1;
    
    public Iterator() {
        
    }

    public String getDisplayName() {
        String prefixPath;

        if (repeat == Iterator.FILL_ALL) {
            prefixPath = String.format("iterate %d..*", startIdx);
        } else {
            int to = startIdx + repeat - 1;
            prefixPath = String.format("iterate %d..%d", startIdx, to);
        }

        return prefixPath;
    }

    public Iterator(ValueDataNode dataNode) {
        this.dataNode = dataNode;
    }

    public ValueDataNode getDataNode() {
        return dataNode;
    }

    public void setDataNode(ValueDataNode dataNode) {
        this.dataNode = dataNode;
    }

    public int getRepeat() {
        return repeat;
    }

    public void setRepeat(int repeat) {
        this.repeat = repeat;
    }

    public int getStartIdx() {
        return startIdx;
    }

    public void setStartIdx(int startIdx) {
        this.startIdx = startIdx;
    }
}
