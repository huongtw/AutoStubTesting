package com.dse.coverage.basicpath;

import auto_testcase_generation.cfg.object.ICfgNode;
import auto_testcase_generation.cfg.object.NormalCfgNode;
import auto_testcase_generation.cfg.object.SimpleCfgNode;

import java.util.ArrayList;
import java.util.List;

public class BasicPath extends ArrayList<ICfgNode> {

    private boolean visited = false;

    public BasicPath(List<ICfgNode> list) {
        super(list);
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }
}
