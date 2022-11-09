package com.dse.testcase_manager;

import com.dse.testcase_execution.result_trace.AssertionResult;
import com.dse.user_code.objects.TestCaseUserCode;
import com.dse.util.DateTimeUtils;
import com.dse.util.PathUtils;
import com.dse.util.Utils;
import com.google.gson.*;

import java.io.File;
import java.time.LocalDateTime;

public class CompoundTestCaseImporter {

    public static void main(String[] args) {
        String path = "datatest/hoannv/compound_testcase/COMPOUND.97322.json";
        File file = new File(path);
        CompoundTestCaseImporter importer = new CompoundTestCaseImporter();
        CompoundTestCase compoundTestCase = importer.importCompoundTestCase(file);
        System.out.println(compoundTestCase.getName());
    }

    public CompoundTestCase importCompoundTestCase(File file) {
        JsonDeserializer<CompoundTestCase> deserializer = (jsonElement, type, jsonDeserializationContext) -> {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            CompoundTestCase compoundTestCase = new CompoundTestCase();
            String name = jsonObject.get("name").getAsString();
            if (name != null) {
                compoundTestCase.setName(name);
            }

            TestCaseUserCode testCaseUserCode = TestCaseManager.readUserCode(jsonObject);

            if (testCaseUserCode != null)
                compoundTestCase.setTestCaseUserCode(testCaseUserCode);

            if (jsonObject.get("result") != null) {
                String[] tempList = jsonObject.get("result").getAsString().split("/");
                AssertionResult result = new AssertionResult();
                result.setPass(Integer.parseInt(tempList[0]));
                result.setTotal(Integer.parseInt(tempList[1]));
                compoundTestCase.setExecutionResult(result);
            }

            TestCaseManager.extractRelateInfo(jsonObject, compoundTestCase);

            Gson gson = new Gson();
            JsonArray jsonSlots = jsonObject.get("slots").getAsJsonArray();
            if (jsonSlots != null) {
                for (JsonElement jsonSlot : jsonSlots) {
                    TestCaseSlot slot = gson.fromJson(jsonSlot, TestCaseSlot.class);
                    compoundTestCase.getSlots().add(slot);
                }
            }
            return compoundTestCase;
        };

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(CompoundTestCase.class, deserializer);
        Gson customGson = gsonBuilder.create();
        JsonElement json = JsonParser.parseString(Utils.readFileContent(file));
        return customGson.fromJson(json, CompoundTestCase.class);
    }
}
