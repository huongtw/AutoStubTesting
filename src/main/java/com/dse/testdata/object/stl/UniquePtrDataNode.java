package com.dse.testdata.object.stl;

public class UniquePtrDataNode extends SmartPointerDataNode {

    @Override
    public String[] getConstructors() {
        return CONSTRUCTOR;
    }

    private static final String[] CONSTRUCTOR = new String[] {
            "unique_ptr()",
            "unique_ptr(nullptr_t p)",
            "unique_ptr(T* p)",
            "unique_ptr(T* p, std::default_delete<T> del)"
    };
}
