package com.dse.environment;

import com.dse.parser.object.*;
import com.dse.util.Utils;
import com.dse.util.tostring.DependencyTreeDisplayer;
import com.google.gson.*;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;

@Deprecated
public class SourcecodeTreeImporter {
    private File sourcecodeJsonFile;

    public static void main(String[] args) {
        SourcecodeTreeImporter importer = new SourcecodeTreeImporter();
        importer.setSourcecodeJsonFile(new File("/Users/ducanhnguyen/Documents/akautauto/local/wd/rrrr/duc-anh/Merge.cpp.aka"));
        Node root = importer.load(importer.getSourcecodeJsonFile());
        System.out.println(new DependencyTreeDisplayer(root).getTreeInString());
    }

    public Node load(File sourcecodeJsonFile) {
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
                        String name = jsonObject.get("name").getAsString();
                        n.setName(name);
                        n.setChildren(new ArrayList<>());
                        n.setDependencies(new ArrayList<>());

                        if (n instanceof CFileNode || n instanceof CppFileNode || n instanceof HeaderNode) {
                            n.setAbsolutePath(jsonObject.get("path").getAsString());
                            String content = Utils.readFileContent(n.getAbsolutePath());

                            IASTTranslationUnit ast = Utils.getIASTTranslationUnitforCpp(content.toCharArray());
                            ((ISourcecodeFileNode) n).setAST(ast);

                            ((ISourcecodeFileNode) n).setMd5(jsonObject.get("checksum").getAsString());

                        } else if (n instanceof InternalVariableNode) {
                            ((InternalVariableNode) n).setRawType(jsonObject.get("rawType").getAsString());

                        } else if (n instanceof DefinitionFunctionNode){

                        } else if (n instanceof FunctionNode){

                        }

                        if (jsonObject.get("children") != null)
                            for (JsonElement child : jsonObject.get("children").getAsJsonArray()) {
                                Node childNode = context.deserialize(child, Node.class);
                                if (childNode != null) {
                                    n.getChildren().add(childNode);
                                    childNode.setParent(n);
                                    childNode.setAbsolutePath(childNode.getParent().getAbsolutePath() + File.separator + childNode.getName());
                                }
                            }
                    }


                } catch (IllegalAccessException | ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return n;
            }
        };

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Node.class, deserializer);
        Gson customGson = gsonBuilder.create();
        String json = Utils.readFileContent(sourcecodeJsonFile);
        Node root = customGson.fromJson(json, Node.class);
        return root;
    }

    public File getSourcecodeJsonFile() {
        return sourcecodeJsonFile;
    }

    public void setSourcecodeJsonFile(File sourcecodeJsonFile) {
        this.sourcecodeJsonFile = sourcecodeJsonFile;
    }
}
