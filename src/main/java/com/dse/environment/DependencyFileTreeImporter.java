package com.dse.environment;

import com.dse.parser.ProjectParser;
import com.dse.parser.object.INode;
import com.dse.parser.object.IProjectNode;
import com.dse.parser.object.Node;
import com.dse.search.Search;
import com.dse.search.condition.SourcecodeFileNodeCondition;
import com.dse.util.Utils;
import com.dse.util.tostring.DependencyTreeDisplayer;
import com.google.gson.*;

import java.io.File;
import java.lang.reflect.Type;
import java.util.List;

public class DependencyFileTreeImporter {
    private File sourcecodeJsonFile;

    public static void main(String[] args) {
        // get node
        ProjectParser projectParser = new ProjectParser(new File("/Users/ducanhnguyen/Documents/akautauto/datatest/duc-anh/Algorithm"));
        projectParser.setCpptoHeaderDependencyGeneration_enabled(true);
        projectParser.setExpandTreeuptoMethodLevel_enabled(true);
        IProjectNode projectRoot = projectParser.getRootTree();

        DependencyTreeDisplayer displayer = new DependencyTreeDisplayer(projectRoot);
        System.out.println(displayer.getTreeInString());

        DependencyFileTreeExporter exporter = new DependencyFileTreeExporter();
        List<INode> nodes = Search.searchNodes(projectRoot, new SourcecodeFileNodeCondition(), "main.cpp");
        Node n = (Node) nodes.get(0);
        exporter.export(new File("/Users/ducanhnguyen/Documents/akautauto/local/wd/duc-anh/main.cpp.aka"), n);

        // import
        DependencyFileTreeImporter importer = new DependencyFileTreeImporter();
        importer.setSourcecodeJsonFile(new File("/Users/ducanhnguyen/Documents/akautauto/local/wd/duc-anh/main.cpp.aka"));
        Node root = importer.load(importer.getSourcecodeJsonFile(), n);
        System.out.println(new DependencyTreeDisplayer(root).getTreeInString());
    }

    public Node load(File sourcecodeJsonFile, Node root) {
        // change serialization for specific types
        JsonDeserializer<Node> deserializer = new JsonDeserializer<Node>() {
            @Override
            public Node deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                return null;
            }
        };

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Node.class, deserializer);
        Gson customGson = gsonBuilder.create();
        String json = Utils.readFileContent(sourcecodeJsonFile);
        root = customGson.fromJson(json, Node.class);
        return root;
    }

    public File getSourcecodeJsonFile() {
        return sourcecodeJsonFile;
    }

    public void setSourcecodeJsonFile(File sourcecodeJsonFile) {
        this.sourcecodeJsonFile = sourcecodeJsonFile;
    }
}
