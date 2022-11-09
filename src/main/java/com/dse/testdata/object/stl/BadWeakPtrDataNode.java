package com.dse.testdata.object.stl;

public class BadWeakPtrDataNode extends SmartPointerDataNode {

    @Override
    public String[] getConstructors() {
        return CONSTRUCTOR;
    }

    private static final String[] CONSTRUCTOR = new String[] {
            "bad_weak_ptr()"
    };
}
