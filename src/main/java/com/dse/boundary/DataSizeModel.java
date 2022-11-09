package com.dse.boundary;

import java.util.HashMap;

public class DataSizeModel extends HashMap<String, PrimitiveBound> {
    private String name;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
