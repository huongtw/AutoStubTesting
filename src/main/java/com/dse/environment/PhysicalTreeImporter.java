package com.dse.environment;

import com.dse.environment.object.EnviroDontStubNode;
import com.dse.environment.object.EnviroSBFNode;
import com.dse.environment.object.EnviroUUTNode;
import com.dse.environment.object.IEnvironmentNode;
import com.dse.parser.object.*;
import com.dse.search.Search;
import com.dse.search.condition.SourcecodeFileNodeCondition;
import com.dse.util.PathUtils;
import com.dse.util.Utils;
import com.dse.util.tostring.DependencyTreeDisplayer;
import com.google.gson.*;
import com.dse.logger.AkaLogger;

import java.io.File;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class PhysicalTreeImporter {
    final static AkaLogger logger = AkaLogger.get(PhysicalTreeImporter.class);
    private File physicalTreePath;

    public static void main(String[] args) {
        PhysicalTreeImporter importer = new PhysicalTreeImporter();
        importer.setPhysicalTreePath(new File("/Users/ducanhnguyen/Documents/akautauto/local/wd/rrrr/physical_tree.json"));
        Node n = importer.importTree(importer.getPhysicalTreePath());
        System.out.println(new DependencyTreeDisplayer(n).getTreeInString());
    }

    public Node importTree(File physicalTreePath) {
        // change serialization for specific types
        JsonDeserializer<Node> deserializer = new JsonDeserializer<Node>() {
            @Override
            public Node deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                JsonObject jsonObject = json.getAsJsonObject();
                Node n = null;
                try {
                    String type = jsonObject.get("type").getAsString();
                    n = (Node) Class.forName(type).newInstance();

                    if (n != null) {
                        String relativePath = jsonObject.get("path").getAsString();
                        String path = PathUtils.toAbsolute(relativePath);
//                        String path = jsonObject.get("path").getAsString();
                        n.setAbsolutePath(path);
                        n.setName(new File(n.getAbsolutePath()).getName());
                        n.setChildren(new ArrayList<>());
                        n.setDependencies(new ArrayList<>());

                            if (n instanceof ISourcecodeFileNode){
                                String lastModifiedDate = jsonObject.get("last-modified").getAsString();
                                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                                ((ISourcecodeFileNode) n).setLastModifiedDate(sdf.parse(lastModifiedDate));
                            }

                        if (jsonObject.get("children") != null)
                            for (JsonElement child : jsonObject.get("children").getAsJsonArray()) {
                                Node childNode = context.deserialize(child, Node.class);
                                if (childNode!=null) {
                                    n.getChildren().add(childNode);
                                    childNode.setParent(n);
                                }
                            }
                    }


                } catch (Exception e) {
                    logger.debug("Can not import physical tree from file " + physicalTreePath.getAbsolutePath());
                    e.printStackTrace();
                }
                return n;
            }
        };

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Node.class, deserializer);
        Gson customGson = gsonBuilder.create();
        String json = Utils.readFileContent(physicalTreePath.getAbsolutePath());
        Node root = customGson.fromJson(json, Node.class);

        return root;
    }

//    private void loadUnitTestableState() {
//        List<INode> sources = Search.searchNodes(Environment.getInstance().getProjectNode(), new SourcecodeFileNodeCondition());
//
//        List<IEnvironmentNode> uuts = EnvironmentSearch.searchNode(Environment.getInstance().getEnvironmentRootNode(), new EnviroUUTNode());
//        for (IEnvironmentNode uut : uuts) {
//            for (INode source : sources) {
//                if (((EnviroUUTNode) uut).getName().equals(source.getAbsolutePath())) {
//                    ((EnviroUUTNode) uut).setUnit(source);
//                    break;
//                }
//            }
//        }
//
//        List<IEnvironmentNode> sbfs = EnvironmentSearch.searchNode(Environment.getInstance().getEnvironmentRootNode(), new EnviroSBFNode());
//        for (IEnvironmentNode sbf : sbfs) {
//            for (INode source : sources) {
//                if (((EnviroSBFNode) sbf).getName().equals(source.getAbsolutePath())) {
//                    ((EnviroSBFNode) sbf).setUnit(source);
//                    break;
//                }
//            }
//        }
//
//        List<IEnvironmentNode> dontStubs = EnvironmentSearch.searchNode(Environment.getInstance().getEnvironmentRootNode(), new EnviroDontStubNode());
//        for (IEnvironmentNode dontStub : dontStubs) {
//            for (INode source : sources) {
//                if (((EnviroDontStubNode) dontStub).getName().equals(source.getAbsolutePath())) {
//                    ((EnviroDontStubNode) dontStub).setUnit(source);
//                    break;
//                }
//            }
//        }
//    }


    public File getPhysicalTreePath() {
        return physicalTreePath;
    }

    public void setPhysicalTreePath(File physicalTreePath) {
        this.physicalTreePath = physicalTreePath;
    }
}
