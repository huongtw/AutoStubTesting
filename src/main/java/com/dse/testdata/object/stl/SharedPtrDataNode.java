package com.dse.testdata.object.stl;

public class SharedPtrDataNode extends SmartPointerDataNode {

    @Override
    public String[] getConstructors() {
        return CONSTRUCTOR;
    }

    private static final String[] CONSTRUCTOR = new String[] {
            "shared_ptr()",
            "shared_ptr(nullptr_t p)",
            "shared_ptr(T* p)",
            "shared_ptr(T* p, std::default_delete<T> del)",
            "shared_ptr(nullptr_t p, std::default_delete<T> del)",
            "shared_ptr(T* p, std::default_delete<T> del, std::allocator<T> alloc)",
            "shared_ptr(nullptr_t p, std::default_delete<T> del, std::allocator<T> alloc)"
    };
}
