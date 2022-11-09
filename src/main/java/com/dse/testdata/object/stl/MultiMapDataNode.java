package com.dse.testdata.object.stl;


public class MultiMapDataNode extends ListBaseDataNode {
    @Override
    public String getElementName(int index) {
        return "element #" + index;
    }

    @Override
    public String getPushMethod() {
        return "insert";
    }
}

