package com.dse.testdata.object.stl;

public class WeakPtrDataNode extends SmartPointerDataNode {

    @Override
    public String[] getConstructors() {
        return CONSTRUCTOR;
    }

    private static final String[] CONSTRUCTOR = new String[] {
            "weak_ptr()",
            "weak_ptr(std::shared_ptr<T> p)"
    };
}
