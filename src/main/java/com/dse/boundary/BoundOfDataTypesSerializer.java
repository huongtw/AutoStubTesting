package com.dse.boundary;

import com.dse.config.IFunctionConfigBound;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.Map;

public class BoundOfDataTypesSerializer  implements JsonSerializer<BoundOfDataTypes> {

    public JsonElement serialize(BoundOfDataTypes src, Type typeOfSrc, JsonSerializationContext context) {
        Map<String, PrimitiveBound> boundMap = src.getBounds();
        JsonArray jsonTypeArr = new JsonArray();

        for (String varName: boundMap.keySet()){
            IFunctionConfigBound bound = boundMap.get(varName);
            if (bound instanceof PrimitiveBound){
                String lower = ((PrimitiveBound) bound).getLower();
                String upper = ((PrimitiveBound) bound).getUpper();

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("type", varName);
                jsonObject.addProperty("lower", lower);
                jsonObject.addProperty("upper", upper);
                jsonTypeArr.add(jsonObject);
            }
        }

        return jsonTypeArr;
    }
}
