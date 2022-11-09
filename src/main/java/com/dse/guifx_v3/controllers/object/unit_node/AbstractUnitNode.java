package com.dse.guifx_v3.controllers.object.unit_node;

import java.util.ArrayList;
import java.util.List;

public class AbstractUnitNode {
    private List<AbstractUnitNode> children = new ArrayList<>();

    public String getName() {
        return null;
    }
    public String getStubType() {
        return null;
    }
    public List<AbstractUnitNode> getChildren() {
        return children;
    }
}
