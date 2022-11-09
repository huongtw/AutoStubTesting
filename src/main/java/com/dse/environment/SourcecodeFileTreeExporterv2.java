package com.dse.environment;

import com.dse.parser.object.*;
import com.dse.search.Search;
import com.dse.search.condition.NodeCondition;
import com.dse.util.PathUtils;
import com.dse.util.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Export a source code file to json
 */
public class SourcecodeFileTreeExporterv2 {
    public static final String NAME_SOURCE_COLDE_FILE_TAG = "NAME_FILE";

    public static void main(String[] args) {
    }

    public SourcecodeFileTreeExporterv2() {
    }

    public void export(File path, INode root) {
        if (root instanceof ISourcecodeFileNode) {
            String relativePath = PathUtils.toRelative(root.getAbsolutePath());

            Map<String, String> nodesInformation = new HashMap<>();
            nodesInformation.put(NAME_SOURCE_COLDE_FILE_TAG, relativePath);

            List<INode> nodes = Search.searchNodes(root, new NodeCondition());
            for (INode node : nodes)
                if (node instanceof CustomASTNode) {
                    if (((CustomASTNode) node).getAST() != null && !(node instanceof DefinitionFunctionNode)) {
                        String nodeRelativePath = PathUtils.toRelative(node.getAbsolutePath());
                        nodesInformation.put(nodeRelativePath, Utils.computeMd5(((CustomASTNode) node).getAST().getRawSignature()));
                    }
//                    else
//                        nodesInformation.put(node.getAbsolutePath(), "NO-CHECKSUM");
                } else {
//                    nodesInformation.put(node.getAbsolutePath(), "NO-CHECKSUM");
                }

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(nodesInformation);
            Utils.writeContentToFile(json, path.getAbsolutePath());
        }
    }
}
