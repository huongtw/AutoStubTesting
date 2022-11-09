package com.dse.testcase_manager;

import com.dse.testcase_execution.result_trace.AssertionResult;
import com.dse.user_code.objects.TestCaseUserCode;
import com.dse.util.PathUtils;
import com.dse.util.Utils;
import com.google.gson.*;

import java.io.File;
import java.lang.reflect.Type;

public class CompoundTestCaseExporter {

    public static void main(String[] args) {
        CompoundTestCase compoundTestCase = new CompoundTestCase();

        compoundTestCase.setNameAndPath("TestCompoundTestCaseExporter.001", "datatest/hoannv/compound_testcase/TestCompoundTestCaseExporter.001");
        CompoundTestCaseExporter exporter = new CompoundTestCaseExporter();
        exporter.export(new File(compoundTestCase.getPath()), compoundTestCase);
    }

    public void export(File path, CompoundTestCase compoundTestCase) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder
                .registerTypeAdapter(CompoundTestCase.class, new CustomSerializer())
                .setPrettyPrinting().create();
        String json = gson.toJson(compoundTestCase, CompoundTestCase.class);
        Utils.writeContentToFile(json, path.getAbsolutePath());
    }

    private static class CustomSerializer implements JsonSerializer<CompoundTestCase> {

        @Override
        public JsonElement serialize(CompoundTestCase compoundTestCase, Type type, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("name", compoundTestCase.getName());

            // export test case user code
            TestCaseManager.compressRelateInfo(jsonObject, compoundTestCase);

            JsonArray slots = new JsonArray();
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.setPrettyPrinting().create();
            for (TestCaseSlot slot : compoundTestCase.getSlots()) {
                JsonElement slotElement = gson.toJsonTree(slot, TestCaseSlot.class);
                slots.add(slotElement);
            }
            jsonObject.add("slots", slots);
            return jsonObject;
        }
    }
}
