package com.dse.compiler.message.error_tree;

import com.dse.compiler.message.error_tree.node.IErrorNode;

import java.util.ArrayList;
import java.util.List;

public class CompileErrorSearch {
    public synchronized static List<IErrorNode> searchNodes(IErrorNode root, Class<? extends IErrorNode> condition) {
        List<IErrorNode> output = new ArrayList<>();

        for (IErrorNode child : root.getChildren()) {
            if (condition.isInstance(child))
                output.add(child);
            output.addAll(CompileErrorSearch.searchNodes(child, condition));
        }

        return output;
    }
}
