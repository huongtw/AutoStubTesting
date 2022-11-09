package com.dse.parser.dependency;

import com.dse.config.Paths;
import com.dse.parser.ProjectParser;
import com.dse.parser.object.*;
import com.dse.search.Search;
import com.dse.search.condition.HeaderNodeCondition;
import com.dse.search.condition.IncludeHeaderNodeCondition;
import com.dse.util.Utils;
import com.dse.util.tostring.ReducedDependencyTreeDisplayer;
import com.dse.logger.AkaLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class IncludeHeaderDependencyGeneration extends AbstractDependencyGeneration{
    final static AkaLogger logger = AkaLogger.get(IncludeHeaderDependencyGeneration.class);

    public IncludeHeaderDependencyGeneration() {
    }

    public static void main(String[] args) {
        ProjectParser parser = new ProjectParser(new File(
                Paths.JOURNAL_TEST));
        Node root = parser.getRootTree();

        new ReducedDependencyTreeDisplayer(root);
    }

    private String toRelativePath(String path, INode owner) {
        String outPath = path + "";

        //TODO: relative path "../.h" or absolute path out of the project
        final String doubleDot = "..";
        if (path.contains(doubleDot + File.separator)) {
            String pattern = Pattern.quote(File.separator);

            INode folderParent = owner.getParent();
            while (!(folderParent instanceof FolderNode))
                folderParent = folderParent.getParent();

            List<String> pathElements = new ArrayList<>(Arrays.asList(folderParent.getAbsolutePath().split(pattern)));
            String[] includePathElements = path.split(pattern);

            for (String e : includePathElements) {
                if (e.equals(doubleDot)) {
                    int lastIdx = pathElements.size() - 1;
                    pathElements.remove(lastIdx);
                }
                else
                    pathElements.add(e);
            }

            outPath = String.join(File.separator, pathElements);
        }

        INode root = Utils.getRoot(owner);

        if (outPath.contains(root.getAbsolutePath()))
            outPath = outPath.replace(root.getAbsolutePath() + File.separator, "");

        return outPath;
    }

    public void dependencyGeneration(INode owner) {
        if (owner instanceof ISourcecodeFileNode) {
            if (!((ISourcecodeFileNode) owner).isIncludeHeaderDependencyState()) {
                ((ISourcecodeFileNode) owner).setIncludeHeaderDependencyState(true);

                List<INode> includeHeaderNodes = Search.searchNodes(owner,
                        new IncludeHeaderNodeCondition());

                for (INode includeHeaderNode : includeHeaderNodes) {
                    List<INode> searchedNodes = findIncludeNodes(includeHeaderNode, owner);

                    if (searchedNodes.size() >= 1) {
                        INode refferedNode = searchedNodes.get(0);

                        IncludeHeaderDependency d = new IncludeHeaderDependency(owner, refferedNode);
                        if (!owner.getDependencies().contains(d)
                                && !refferedNode.getDependencies().contains(d)) {
                            owner.getDependencies().add(d);
                            refferedNode.getDependencies().add(d);

                            logger.debug("Found an include dependency: " + d.toString());
                        }
                    }
                }
            } else{
                logger.debug(owner.getAbsolutePath() + " is analyzed include dependency before");
            }
        }
    }

    public List<INode> findIncludeNodes(INode includeHeader, INode source) {
        String includeFilePath = includeHeader.getNewType();
        INode root = Utils.getRoot(source);

        /*
         * Library function se khong tim duoc trong day
         */
        includeFilePath = Utils.normalizePath(includeFilePath);
        includeFilePath = toRelativePath(includeFilePath, source);

        List<INode> searchedNodes = Search
                .searchNodes(root, new HeaderNodeCondition(),File.separator + includeFilePath);

        return searchedNodes;
    }
}
