package com.dse.testdata.object.stl;


public class SetDataNode extends ListBaseDataNode {
    @Override
    public String getElementName(int index) {
        if (index == 0)
            return "begin";
        else if (index == getSize() - 1)
            return "end";
        else
            return "element #" + index;
    }

    @Override
    public String getPushMethod() {
        return "insert";
    }
}

