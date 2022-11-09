package com.dse.regression;

import com.dse.config.WorkspaceConfig;
import com.dse.parser.object.INode;

import java.util.*;

/**
 * Represent the difference between two version of source code projects
 */
public class ChangesBetweenSourcecodeFiles {
    public static Map<INode, Date> modifiedSourcecodeFiles = new HashMap<>();

    // nodes are deleted in the old tree
    public static List<String> deletedPaths = new ArrayList<>();

    // nodes are modified in the old tree
    public static List<INode> modifiedNodes = new ArrayList<>();

    // nodes are added in the new tree
    public static List<INode> addedNodes = new ArrayList<>();

    public static List<UnresolvedDependency> unresolvedDependencies = new ArrayList<>();

    public static void reset() {
        modifiedSourcecodeFiles = new HashMap<>();
        deletedPaths = new ArrayList<>();
        modifiedNodes = new ArrayList<>();
        addedNodes = new ArrayList<>();
        unresolvedDependencies = new ArrayList<>();
    }

    public static String getReportOfDifferences() {
        String differences = "";
        differences += "Changes are detected by analyzing " + new WorkspaceConfig().fromJson().getPhysicalJsonFile() + "\n\n";
        differences += "----------------\nRESULT-------------\n----------------\n";

        // information about deleted items
        differences += "DELETED:\n";
        if (deletedPaths.size() == 0)
            differences += "There are no deleted items\n";
        else {
            int count = 1;
            for (String path : deletedPaths)
                differences += (count++) + ". " + path + "\n";
        }

        // information about modified items
        differences += "\nMODIFIED:\n";
        if (modifiedNodes.size() == 0)
            differences += "There are no modified items\n";
        else {
            int count = 1;
            for (INode n : modifiedNodes)
                differences += (count++) + ". [" + n.getClass().getSimpleName() + "] " + n.getAbsolutePath() + "\n";
        }

        // information about added items
        differences += "\nADDED:\n";
        if (addedNodes.size() == 0)
            differences += "There are no added items\n";
        else {
            int count = 1;
            for (INode n : addedNodes)
                differences += (count++) + ". [" + n.getClass().getSimpleName() + "] " + n.getAbsolutePath() + "\n";
        }


        return differences;
    }

    public static String getModifiedSourcecodeFilesInString() {
        String output = "SOURCE CODE FILES CHANGED:\n";
        if (modifiedSourcecodeFiles.keySet().size() == 0) {
            output += "There are no modification on source code files";
        } else {
            int count = 1;
            for (INode key : modifiedSourcecodeFiles.keySet())
                output += (count++) + ". " + key.getAbsolutePath() + "\n" + "Last date modified: " + modifiedSourcecodeFiles.get(key).toString() + "\n\n";
        }
        return output;
    }

    public static String getUnresolvedDepdendenciesInString() {
        String output = "UNRESOLVED DEPENDENCIES:\n";
        if (unresolvedDependencies.size() == 0) {
            output += "There are no unresolved dependencies on source code files";
        } else {
            int count = 1;
            for (UnresolvedDependency d : unresolvedDependencies) {
                output += (count++) + ". " + d.getStart() + " -> " + d.getEnd() + "\n";
            }
        }
        return output;
    }
}
