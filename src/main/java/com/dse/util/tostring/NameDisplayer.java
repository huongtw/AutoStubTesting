package com.dse.util.tostring;

import com.dse.parser.ProjectLoader;
import com.dse.parser.object.*;
import com.dse.search.Search;
import com.dse.search.condition.CppFileNodeCondition;

import java.io.File;

public class NameDisplayer extends ToString {

    public NameDisplayer(INode root) {
        super(root);
    }

    public static void main(String[] args) {
        // Project tree generation
        IProjectNode projectRootNode = new ProjectLoader().load(new File(""));

        // display tree of project
        ToString treeDisplayer = new NameDisplayer(projectRootNode);
        System.out.println(treeDisplayer.getTreeInString());
    }

    private void displayTree(INode n, int level) {
        if (n == null || n instanceof FolderNode && Search.searchNodes(n, new CppFileNodeCondition()).size() == 0)
            return;
        else {
//            if (n instanceof ExpressionNode)
//                treeInString += genTab(level) + "[" + n.getClass().getSimpleName() + "] " + n.getNewType() + "	\tvalue="
//                        + ((ExpressionNode) n).getValue() + "\n";
//            else
            if (n instanceof VariableNode)
                treeInString += genTab(level) + "[" + n.getClass().getSimpleName() + "] " + n.getNewType()
                        + "\t;reduced type=" + ((IVariableNode) n).getReducedRawType() + "\t; raw type="
                        + ((IVariableNode) n).getRawType() + "\n";
            else
                treeInString += genTab(level) + "[" + n.getClass().getSimpleName() + "] " + n.getNewType() + "\n";
            if (n instanceof AbstractFunctionNode)
                if (((AbstractFunctionNode) n).getRealParent() != null)
                    treeInString += genTab(level + 1) + "real parent: "
                            + ((AbstractFunctionNode) n).getRealParent().getAbsolutePath() + "\n";
            treeInString += genTab(level + 1) + "id: " + n.getId() + "\n";
            treeInString += genTab(level + 1) + n.getAbsolutePath() + "\n";
        }
        for (Object child : n.getChildren()) {
            displayTree((Node) child, ++level);
            level--;
        }
    }

    @Override
    public String toString(INode n) {
        displayTree(n, 0);
        return treeInString;
    }
}
