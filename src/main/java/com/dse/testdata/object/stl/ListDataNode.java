package com.dse.testdata.object.stl;

public class ListDataNode extends ListBaseDataNode {

    @Override
    public String getElementName(int index) {
        if (index == 0)
            return "front";
        else if (index == getSize() - 1)
            return "back";
        else
            return "element #" + index;
    }

    @Override
    public String getPushMethod() {
        return "push_back";
    }
}
