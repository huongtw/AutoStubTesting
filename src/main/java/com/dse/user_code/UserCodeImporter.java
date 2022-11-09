package com.dse.user_code;

import com.dse.user_code.objects.AbstractUserCode;
import com.dse.user_code.objects.ParameterUserCode;
import com.dse.user_code.objects.TestCaseUserCode;
import com.google.gson.*;

public class UserCodeImporter {
    public AbstractUserCode importUserCode(JsonObject jsonObject) {
        JsonDeserializer<AbstractUserCode> deserializer = ((jsonElement, type, jsonDeserializationContext) -> {
            AbstractUserCode userCode = null;

            try {
                String typeOfUserCode = jsonObject.get("type").getAsString();
                userCode = (AbstractUserCode) Class.forName(typeOfUserCode).newInstance();

                String name = jsonObject.get("name").getAsString();
                int id = jsonObject.get("id").getAsInt();
                JsonArray includePaths = jsonObject.get("includePaths").getAsJsonArray();

                userCode.setName(name);
                userCode.setId(id);

                if (userCode instanceof ParameterUserCode) {
                    String content = jsonObject.get("content").getAsString();
                    userCode.setContent(content);
                } else if (userCode instanceof TestCaseUserCode) {
                    String setUpContent = jsonObject.get("setUpContent").getAsString();
                    ((TestCaseUserCode) userCode).setSetUpContent(setUpContent);
                    String tearDownContent = jsonObject.get("tearDownContent").getAsString();
                    ((TestCaseUserCode) userCode).setTearDownContent(tearDownContent);
                }

                for (JsonElement path : includePaths) {
                    userCode.getIncludePaths().add(path.getAsString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return userCode;
        });

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(AbstractUserCode.class, deserializer);
        Gson customGson = gsonBuilder.create();
        return customGson.fromJson(jsonObject, AbstractUserCode.class);

    }
}
