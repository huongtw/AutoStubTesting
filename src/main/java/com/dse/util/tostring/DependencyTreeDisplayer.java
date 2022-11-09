package com.dse.util.tostring;


import com.dse.parser.ProjectParser;
import com.dse.parser.dependency.Dependency;
import com.dse.parser.object.*;

import java.io.File;

public class DependencyTreeDisplayer extends ToString {

    public static void main(String[] args) {
        // Project tree generation
        ProjectParser projectParser = new ProjectParser(new File("C:\\Users\\dragon\\Dropbox\\Unit Test FGA\\meeting Oct 31\\dataset\\ducanh\\NamespaceMergerTest"));
        projectParser.setCpptoHeaderDependencyGeneration_enabled(true);
        projectParser.setFuncCallDependencyGeneration_enabled(false);
        projectParser.setExpandTreeuptoMethodLevel_enabled(true);
        projectParser.setParentReconstructor_enabled(false);
        projectParser.setGenerateSetterandGetter_enabled(false);
        projectParser.setGlobalVarDependencyGeneration_enabled(false);
        IProjectNode projectRootNode = projectParser.getRootTree();

        // display tree of project
        ToString treeDisplayer = new DependencyTreeDisplayer(projectRootNode);
        System.out.println(treeDisplayer.getTreeInString());
    }
    public DependencyTreeDisplayer(INode root) {
        super(root);
    }

    private void displayTree(INode n, int level) {
        if (n == null)
            return;
        else {
            if (n instanceof AttributeOfStructureVariableNode)
                treeInString += genTab(level) + "[" + n.getClass().getSimpleName() + "] " + n.toString() + "; type = " + ((AttributeOfStructureVariableNode) n).getRawType() + "\n";
            else if (n instanceof VariableNode || n instanceof FunctionNode || n instanceof TypedefDeclaration)
                treeInString += genTab(level) + "[" + n.getClass().getSimpleName() + "] " + n.toString() + "\n";
            else
                treeInString += genTab(level) + "[" + n.getClass().getSimpleName() + "] " + n.getNewType() + "\n";

            treeInString += genTab(level) + n.getAbsolutePath() + "\n";
            for (Dependency d : n.getDependencies())
                if (d.getStartArrow().equals(n))
                    treeInString += genTab(level + 1) + "[" + d.getClass().getSimpleName() + "]"
                            + d.getEndArrow().getAbsolutePath() + "\n";

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
