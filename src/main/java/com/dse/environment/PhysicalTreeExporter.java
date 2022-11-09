package com.dse.environment;

import com.dse.config.AkaConfig;
import com.dse.parser.VectorCastProjectLoader;
import com.dse.parser.object.ISourcecodeFileNode;
import com.dse.parser.object.Node;
import com.dse.parser.object.ProjectNode;
import com.dse.parser.object.ISourcecodeFileNode;
import com.dse.util.PathUtils;
import com.dse.util.Utils;
import com.google.gson.*;

import java.io.File;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Export a tree of folders and files to json
 */
public class PhysicalTreeExporter {

    public static void main(String[] args) {
        // Construct the physical tree
        List<File> sourcecodeList = new ArrayList<>();
        sourcecodeList.add(new File("datatest"));
        sourcecodeList.add(new File("datatest/duc-anh/TSDV_log4cpp"));
        sourcecodeList.add(new File("datatest/duc-anh/TSDV_log4cpp/src"));

        List<File> typeHandledDirectories = new ArrayList<>();
        typeHandledDirectories.add(new File("datatest/duc-anh/TSDV_log4cpp/include"));
        typeHandledDirectories.add(new File("datatest/duc-anh/TSDV_log4cpp/include/log4cpp"));
        typeHandledDirectories.add(new File("datatest/duc-anh/TSDV_log4cpp/include/log4cpp/threading"));

        VectorCastProjectLoader loader = new VectorCastProjectLoader();
        loader.setSourcecodeList(sourcecodeList);
        loader.setTypeHandledDirectories(typeHandledDirectories);
        Node rootOfPhysicalTree = (Node) loader.constructPhysicalTree();

        new PhysicalTreeExporter().export(new File("/physical_tree.json"), rootOfPhysicalTree);
    }

    public PhysicalTreeExporter() {

    }

    public void export(File path, Node root) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder
                .registerTypeAdapter(Node.class, new PhysicalTreeSerializer(root))
                .setPrettyPrinting().create();

        String json = gson.toJson(root, Node.class);
        Utils.writeContentToFile(json, path.getAbsolutePath());
    }

    class PhysicalTreeSerializer implements JsonSerializer<Node> {
        Node root;

        public PhysicalTreeSerializer(Node root) {
            super();
            this.root = root;
        }

        @Override
        public JsonElement serialize(Node node, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject json = new JsonObject();

            if (new File(node.getAbsolutePath()).exists()) {
                String absolutePath = node.getAbsolutePath();
                String relativePath = PathUtils.toRelative(absolutePath);
                json.addProperty("path", relativePath);
                json.addProperty("type", node.getClass().getName());

                if (node instanceof ISourcecodeFileNode) {
                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                    long lastModified = new File(node.getAbsolutePath()).lastModified();
                    json.addProperty("last-modified", sdf.format(lastModified));
                    json.addProperty("checksum", ((ISourcecodeFileNode) node).getMd5());
                }

                if (node.getChildren().size() > 0) {
                    JsonArray childrenArr = new JsonArray();
                    for (Node child : node.getChildren())
                        if (new File(child.getAbsolutePath()).exists())
                            // just analyze file-level nodes
                            childrenArr.add(jsonSerializationContext.serialize(child, Node.class));
                    json.add("children", childrenArr);
                }
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
