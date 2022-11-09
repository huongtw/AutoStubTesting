package com.dse.probe_point_manager.objects;

import com.dse.parser.object.SourcecodeFileNode;
import com.dse.testcase_manager.TestCase;
import com.google.gson.*;

import java.lang.reflect.Type;

public class ProbePointExporter {

    public String export(ProbePoint probePoint) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder
                .registerTypeAdapter(ProbePoint.class, new ProbePointSerializer())
                .setPrettyPrinting().create();
        return gson.toJson(probePoint, ProbePoint.class);
    }

    static class ProbePointSerializer implements JsonSerializer<ProbePoint> {

        @Override
        public JsonElement serialize(ProbePoint probePoint, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject json = new JsonObject();
            json.addProperty("name", probePoint.getName());
            json.addProperty("before", probePoint.getBefore());
            json.addProperty("content", probePoint.getContent());
            json.addProperty("after", probePoint.getAfter());
            json.addProperty("sourceCodeFileNode", probePoint.getSourcecodeFileNode().getAbsolutePath());
            json.addProperty("lineInSourceCodeFile", probePoint.getLineInSourceCodeFile());
            json.addProperty("lineInFunction",probePoint.getLineInFunction());
            json.addProperty("functionNode", probePoint.getFunctionNode().getAbsolutePath());
            json.addProperty("path", probePoint.getPath());
            json.addProperty("isValid", probePoint.isValid());

            JsonArray testCases = new JsonArray();
            if (probePoint.getTestCases() != null) {
                for (TestCase testCase : probePoint.getTestCases()) {
                    testCases.add(testCase.getName());
                }
            }

            json.add("testCases", testCases);

            return json;
        }
    }
}
