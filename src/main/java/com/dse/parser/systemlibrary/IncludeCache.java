package com.dse.parser.systemlibrary;

import com.dse.parser.object.INode;

import java.util.HashMap;
import java.util.List;

public class IncludeCache extends HashMap<String, List<INode>> {
    private static IncludeCache instance;

    public static IncludeCache getInstance() {
        if (instance == null)
            instance = new IncludeCache();

        return instance;
    }
}
