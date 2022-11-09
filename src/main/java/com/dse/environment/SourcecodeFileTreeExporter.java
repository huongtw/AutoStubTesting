package com.dse.environment;

import com.dse.parser.dependency.Dependency;
import com.dse.parser.object.*;
import com.dse.util.Utils;
import com.google.gson.*;

import java.io.File;
import java.lang.reflect.Type;

/**
 * Export a source code file to json
 */
@Deprecated
public class SourcecodeFileTreeExporter {

    public static void main(String[] args) {
    }

    public SourcecodeFileTreeExporter() {
    }

    public void export(File path, Node root) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder
                .registerTypeAdapter(Node.class, new SourcecodeFileTreeSerializer(root))
                .setPrettyPrinting().create();

        String json = gson.toJson(root, Node.class);
        Utils.writeContentToFile(json, path.getAbsolutePath());
    }

    class SourcecodeFileTreeSerializer implements JsonSerializer<Node> {
        Node root;

        public SourcecodeFileTreeSerializer(Node root) {
            super();
            this.root = root;
        }

        private String convertToRelativePath(String absolutePath, Node root) {
            return absolutePath;
//            try {
//                return Utils.normalizePath(absolutePath).replace(Utils.normalizePath(new File(root.getAbsolutePath()).getCanonicalPath()), "");
//            } catch (IOException e) {
//                return absolutePath;
//            }
        }

        @Override
        public JsonElement serialize(Node node, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject json = new JsonObject();
            json.addProperty("name", node.getName());
            json.addProperty("type", node.getClass().getName());

            if (node instanceof ISourcecodeFileNode) {
                json.addProperty("path", node.getAbsolutePath());
                json.addProperty("checksum", ((ISourcecodeFileNode) node).getMd5());
            }

            if (node instanceof VariableNode) {
                json.addProperty("rawType", ((VariableNode) node).getRawType());
            }

            /*
              Add dependencies
             */
            JsonArray dependencies = new JsonArray();
            for (Dependency dependency : node.getDependencies()) {
                INode start = dependency.getStartArrow();
                INode end = dependency.getEndArrow();

                JsonObject jsonDependency = new JsonObject();
                jsonDependency.addProperty("start", convertToRelativePath(start.getAbsolutePath(), getRoot()));
                jsonDependency.addProperty("end", convertToRelativePath(end.getAbsolutePath(), getRoot()));
                jsonDependency.addProperty("type", dependency.getClass().getSimpleName());

                dependencies.add(jsonDependency);

            }
            json.add("dependencies", dependencies);

            /*
              Add children
             */
            if (node.getChildren().size() > 0) {
                JsonArray childrenArr = new JsonArray();
                for (Node child : node.getChildren())
                    childrenArr.add(jsonSerializationContext.serialize(child, Node.class));
                json.add("children", childrenArr);
            }

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
