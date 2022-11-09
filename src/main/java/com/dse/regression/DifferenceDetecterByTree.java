package com.dse.regression;

import com.dse.parser.SourcecodeFileParser;
import com.dse.parser.object.CustomASTNode;
import com.dse.parser.object.INode;
import com.dse.parser.object.ISourcecodeFileNode;
import com.dse.search.Search;
import com.dse.search.condition.NodeCondition;
import com.dse.search.condition.SourcecodeFileNodeCondition;
import com.dse.logger.AkaLogger;
import com.dse.util.PathUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * When users open an environment, then make modification on the original source code.
 * <p>
 * <p>
 * Given two tree of nodes corresponding to the two version, this module detects these changes.
 */
@Deprecated
public class DifferenceDetecterByTree extends AbstractDifferenceDetecter {
    private final static AkaLogger logger = AkaLogger.get(DifferenceDetecterByTree.class);

    public void diff(INode modifiedSrcNode, String elementFolderOfOldVersion) {
        if (modifiedSrcNode instanceof ISourcecodeFileNode)
            try {
                // Analyze changed source code file again
                SourcecodeFileParser srcParser = new SourcecodeFileParser();
                srcParser.setSourcecodeNode((ISourcecodeFileNode) modifiedSrcNode);
                INode rootLatestVersion = srcParser.parseSourcecodeFile(new File(modifiedSrcNode.getAbsolutePath()));

                List<INode> allNodesInLatestVersion = Search.searchNodes(rootLatestVersion, new NodeCondition());
                List<INode> allNodesInPreVersion = Search.searchNodes(modifiedSrcNode, new NodeCondition());

                findAddedNodes(allNodesInLatestVersion, allNodesInPreVersion);
                findDeletedNodes(allNodesInPreVersion, rootLatestVersion);
                findModifiedNodes(allNodesInPreVersion, rootLatestVersion);

            } catch (Exception e) {
                logger.debug("Can not parse " + modifiedSrcNode.getAbsolutePath());
                e.printStackTrace();
            }
    }

    private void findAddedNodes(List<INode> allNodesInLatestVersion, List<INode> allNodesInPreVersion) {
        // find added nodes
        for (INode latestVersion : allNodesInLatestVersion) {
            boolean founded = false;
            for (INode preVersion : allNodesInPreVersion)
                if (PathUtils.equals(preVersion.getAbsolutePath(), latestVersion.getAbsolutePath())) {
                    founded = true;
                    break;
                }

            if (!founded)
                addedNodes.add(latestVersion);
        }
    }

    private void findDeletedNodes(List<INode> allNodesInPreVersion, INode rootLatestVersion) {
        for (INode nodeInPreVersion : allNodesInPreVersion)
            if (nodeInPreVersion instanceof CustomASTNode) {
                List<INode> nodesInLatestVersion = Search.searchNodes(rootLatestVersion, new NodeCondition(), nodeInPreVersion.getAbsolutePath());
                if (nodesInLatestVersion.size() == 0) {
                    // the node is deleted
                    if (!deletedNodes.contains(nodeInPreVersion))
                        deletedNodes.add(nodeInPreVersion);
                }
            }
    }

    private void findModifiedNodes(List<INode> allNodesInPreVersion, INode rootLatestVersion) {
        for (INode nodeInPreVersion : allNodesInPreVersion)
            if (nodeInPreVersion instanceof CustomASTNode) {

                List<INode> nodesInLatestVersion = Search.searchNodes(rootLatestVersion, new NodeCondition(), nodeInPreVersion.getAbsolutePath());
                if (nodesInLatestVersion.size() == 0) {
                    // the node is deleted
                    if (!deletedNodes.contains(nodeInPreVersion))
                        deletedNodes.add(nodeInPreVersion);

                } else if (nodesInLatestVersion.size() == 1) {
                    // can found a previous version of a node
                    if (nodesInLatestVersion.get(0) instanceof CustomASTNode) {
                        CustomASTNode latestNode = (CustomASTNode) nodesInLatestVersion.get(0);
                        CustomASTNode preNode = (CustomASTNode) nodeInPreVersion;

                        // compare the source code of two versions of a node
                        if (latestNode.getAST() != null && preNode.getAST() != null) {
                            /*compare two source code versions of a node*/
                            if (!latestNode.getAST().getRawSignature().equals(preNode.getAST().getRawSignature())) {
                                if (!modifiedNodes.contains(nodeInPreVersion))
                                    modifiedNodes.add(nodeInPreVersion);
                            }
                        }
                    }
                }
            }
    }

    public List<INode> getModifiedSourcecodeFiles(INode physicalTreeRoot) {
        // get all source codes file available in physical tree file
        List<INode> nodes = Search.searchNodes(physicalTreeRoot, new SourcecodeFileNodeCondition());

        // find changed file in these files
        List<INode> changedNode = new ArrayList<>();
        for (INode node : nodes)
            if (node instanceof ISourcecodeFileNode) {
                ISourcecodeFileNode castedNode = (ISourcecodeFileNode) node;
                Date oldLastModifiedDate = castedNode.getLastModifiedDate();
                Date newModifiedDate = getDate(new File(castedNode.getAbsolutePath()));
                if (!oldLastModifiedDate.equals(newModifiedDate)) {
                    changedNode.add(node);
                    logger.debug("File " + castedNode.getAbsolutePath() + " is changed since " + newModifiedDate.toString());

                    modifiedSourcecodeFiles.put(node, newModifiedDate);
                }
            }

        return changedNode;
    }
}
