package auto_testcase_generation.cte.core;

import auto_testcase_generation.cte.core.cteNode.CteClass;
import auto_testcase_generation.cte.core.cteNode.CteNode;
import auto_testcase_generation.cte.core.cteTable.CteTable;
import auto_testcase_generation.cte.core.cteTable.CteTableTestcase;
import com.dse.testcase_manager.TestCaseDataExporter;
import com.google.gson.*;

import java.lang.reflect.Type;

public class CteDataExporter {
    public JsonElement exportNodeToJSonElement(CteNode CteNode) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder
                .registerTypeAdapter(CteNode.class, new CteNodeSerializer())
                .setPrettyPrinting().create();
        return gson.toJsonTree(CteNode, CteNode.class);
    }

    public JsonElement exportTableToJsonElement(CteTable table) {
        JsonObject jsonTable = new JsonObject();
        JsonArray lines = new JsonArray();
        for (CteTableTestcase line : table.getLines()) {
            JsonObject json = new JsonObject();
            json.addProperty("name", line.getName());
            JsonArray chosenNode = new JsonArray();
            for (CteNode Node1 : line.getChosenNodes()) {
                chosenNode.add(Node1.getId());
            }
            json.add("chosenNode", chosenNode);
            lines.add(json);
        }

        jsonTable.add("lines", lines);
        return jsonTable;

    }
    static class CteNodeSerializer implements JsonSerializer<CteNode> {

        @Override
        public JsonElement serialize(CteNode node, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject json = new JsonObject();
            json.addProperty("type", node.getClass().getName());
            json.addProperty("name", node.getName());
            json.addProperty("id", node.getId());
            if (node.getTestcase() != null) {
                JsonElement jsonTestcase = new TestCaseDataExporter().exportToJsonElement(node.getTestcase());
                json.add("testcase", jsonTestcase);
            }
            if (node.getChildren().size() > 0) {
                JsonArray childrenArr = new JsonArray();
                for (CteNode child : node.getChildren())
                    childrenArr.add(jsonSerializationContext.serialize(child, CteNode.class));
                json.add("children", childrenArr);
            }
            return json;
        }
    }

}
