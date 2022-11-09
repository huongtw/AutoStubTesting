package com.dse.environment;

import com.dse.parser.ProjectParser;
import com.dse.parser.dependency.Dependency;
import com.dse.parser.object.*;
import com.dse.search.Search;
import com.dse.search.condition.NodeCondition;
import com.dse.search.condition.SourcecodeFileNodeCondition;
import com.dse.util.PathUtils;
import com.dse.util.Utils;
import com.dse.util.tostring.DependencyTreeDisplayer;
import com.google.gson.*;

import java.io.File;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Export all dependencies of a source code file to json
 */
public class DependencyFileTreeExporter {

    public static void main(String[] args) {
        ProjectParser projectParser = new ProjectParser(new File("/Users/ducanhnguyen/Documents/akautauto/datatest/duc-anh/Algorithm"));
        projectParser.setCpptoHeaderDependencyGeneration_enabled(true);
        projectParser.setExpandTreeuptoMethodLevel_enabled(true);
        IProjectNode projectRoot = projectParser.getRootTree();

        DependencyTreeDisplayer displayer = new DependencyTreeDisplayer(projectRoot);
        System.out.println( displayer.getTreeInString());

        DependencyFileTreeExporter exporter = new DependencyFileTreeExporter();
        List<INode> nodes = Search.searchNodes(projectRoot, new SourcecodeFileNodeCondition(), "main.cpp");
        exporter.export(new File("/Users/ducanhnguyen/Documents/akautauto/local/wd/t.json"), (Node) nodes.get(0));
    }

    public DependencyFileTreeExporter() {
    }

    public void export(File path, Node root) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder
                .registerTypeAdapter(Node.class, new DependencyFileTreeSerializer(root))
                .setPrettyPrinting().create();

        String json = gson.toJson(root, Node.class);
        Utils.writeContentToFile(json, path.getAbsolutePath());
    }

    class DependencyFileTreeSerializer implements JsonSerializer<Node> {
        Node root;

        public DependencyFileTreeSerializer(Node root) {
            super();
            this.root = root;
        }

        private String convertToRelativePath(String absolutePath, Node root) {
            return PathUtils.toRelative(absolutePath);
//            try {
//                return Utils.normalizePath(absolutePath).replace(Utils.normalizePath(new File(root.getAbsolutePath()).getCanonicalPath()), "");
//            } catch (IOException e) {
//                return absolutePath;
//            }
        }

        @Override
        public JsonElement serialize(Node node, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject json = new JsonObject();

            // get all nodes in the tree
            List<INode> allNodes = Search.searchNodes(node, new NodeCondition());
            allNodes.add(node);

            // export dependencies to json object
            JsonArray dependenciesJson = new JsonArray();
            for (INode n : allNodes) {
                for (Dependency dependency : n.getDependencies())
                if (dependency != null && dependency.getStartArrow() != null && dependency.getEndArrow() != null){
                    JsonObject jsonDependency = new JsonObject();

                    // create json for start node
                    INode start = dependency.getStartArrow();
                    jsonDependency.addProperty("start", convertToRelativePath(start.getAbsolutePath(), getRoot()));
                    jsonDependency.addProperty("start-type", start.getClass().getSimpleName());
                    if (!(start instanceof ISourcecodeFileNode)) {
                        // do not compute checksum for source code file
                        // the source code file may be changed by adding or deleting, but its name is never changed
                        // REASON: CHECKSUM just support to load dependency more accuracy
                        if (start instanceof CustomASTNode && ((CustomASTNode) start).getAST() != null)
                            jsonDependency.addProperty("start-md5", Utils.computeMd5(((CustomASTNode) start).getAST().getRawSignature()));
                    }

                    // create json for end node
                    INode end = dependency.getEndArrow();
                    jsonDependency.addProperty("end", convertToRelativePath(end.getAbsolutePath(), getRoot()));
                    jsonDependency.addProperty("end-type", end.getClass().getSimpleName());
                    jsonDependency.addProperty("type", dependency.getClass().getSimpleName());
                    if (!(end instanceof ISourcecodeFileNode))
                        // do not compute checksum for source code file
                        // the source code file may be changed by adding or deleting, but its name is never changed
                        // REASON: CHECKSUM just support to load dependency more accuracy
                        if (end instanceof CustomASTNode && ((CustomASTNode) end).getAST() != null)
                            jsonDependency.addProperty("end-md5", Utils.computeMd5(((CustomASTNode) end).getAST().getRawSignature()));


                    if (!dependenciesJson.contains(jsonDependency))
                        dependenciesJson.add(jsonDependency);
                }
            }

            json.addProperty("path", PathUtils.toRelative(node.getAbsolutePath()));
            json.add("dependency", dependenciesJson);
            return json;
        }

        public Node getRoot() {
            return root;
        }

        public void setRoot(Node root) {
            this.root = root;
        }
    }


}
