package com.dse.boundary;

import com.google.gson.*;

import java.lang.reflect.Type;

public class BoundOfDataTypesDeserializer implements JsonDeserializer<BoundOfDataTypes> {

    @Override
    public BoundOfDataTypes deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        BoundOfDataTypes boundOfDataTypes = new BoundOfDataTypes();
        for (JsonElement child : json.getAsJsonArray()) {
            if (child instanceof JsonObject) {
                JsonObject object = (JsonObject) child;
                boundOfDataTypes.getBounds().put(object.get("type").getAsString(),
                        new PrimitiveBound(object.get("lower").getAsString(),
                                object.get("upper").getAsString()));
            }
        }
        return boundOfDataTypes;
    }
}
