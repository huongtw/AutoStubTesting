package com.dse.testdata.object.stl;


public class MapDataNode extends ListBaseDataNode {
    @Override
    public String getElementName(int index) {
        return "element #" + index;
    }

    @Override
    public String getPushMethod() {
        return "insert";
    }
}

