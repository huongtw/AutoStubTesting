package com.dse.user_code;

import com.dse.user_code.objects.AbstractUserCode;
import com.dse.user_code.objects.ParameterUserCode;
import com.dse.user_code.objects.TestCaseUserCode;
import com.google.gson.*;

import java.lang.reflect.Type;

public class UserCodeExporter {
    public String export(AbstractUserCode userCode) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder
                .registerTypeAdapter(AbstractUserCode.class, new UserCodeSerializer())
                .setPrettyPrinting().create();
        return gson.toJson(userCode, AbstractUserCode.class);
    }

    static class UserCodeSerializer implements JsonSerializer<AbstractUserCode> {

        @Override
        public JsonElement serialize(AbstractUserCode userCode, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject json = new JsonObject();
            json.addProperty("type", userCode.getClass().getName());
            json.addProperty("name", userCode.getName());
            json.addProperty("id", userCode.getId());
            if (userCode instanceof ParameterUserCode) {
                json.addProperty("content", userCode.getContent());
            } else if (userCode instanceof TestCaseUserCode) {
                json.addProperty("setUpContent", ((TestCaseUserCode) userCode).getSetUpContent());
                json.addProperty("tearDownContent", ((TestCaseUserCode) userCode).getTearDownContent());
            }

            JsonArray includePaths = new JsonArray();
            for (String path : userCode.getIncludePaths()) {
                includePaths.add(path);
            }

            json.add("includePaths", includePaths);

            return json;
        }
    }
}
