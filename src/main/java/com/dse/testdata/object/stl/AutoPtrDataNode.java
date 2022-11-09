package com.dse.testdata.object.stl;

public class AutoPtrDataNode extends SmartPointerDataNode {

    @Override
    public String[] getConstructors() {
        return CONSTRUCTOR;
    }

    private static final String[] CONSTRUCTOR = new String[] {
            "auto_ptr(T* p)"
    };
}
