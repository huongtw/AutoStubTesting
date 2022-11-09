package com.dse.config;

import com.dse.boundary.MultiplePrimitiveBound;
import com.dse.boundary.PointerOrArrayBound;
import com.dse.boundary.PrimitiveBound;
import com.dse.environment.Environment;
import com.dse.parser.object.IFunctionNode;
import com.dse.parser.object.INode;
import com.dse.search.Search;
import com.dse.search.condition.FunctionNodeCondition;
import com.dse.util.PathUtils;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FunctionConfigDeserializer implements JsonDeserializer<FunctionConfig> {

    @Override
    public FunctionConfig deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        FunctionConfig fnConfig = new FunctionConfig();

        JsonObject jsonObject = json.getAsJsonObject();

        String path = jsonObject.get(FunctionConfigSerializer.FUNCTION).getAsString();
        List<INode> nodes = Search.searchNodes(Environment.getInstance().getProjectNode(), new FunctionNodeCondition());
        for (INode node: nodes)
            if (PathUtils.equals(node.getAbsolutePath(), path)) {
                fnConfig.setFunctionNode((IFunctionNode) node);
                break;
            }

        fnConfig.setTestdataGenStrategy(jsonObject.get(FunctionConfigSerializer.TESTDATA_GEN_STRATEGY).getAsString());

        // load bound of variables
        JsonArray bound = jsonObject.get(FunctionConfigSerializer.BOUND_OF_ARGUMENTS).getAsJsonArray();
        for (int i = 0; i < bound.size(); i++) {
            JsonObject jsonElement = bound.get(i).getAsJsonObject();

            String typeClass = jsonElement.get(FunctionConfigSerializer.TYPE_CLASS).getAsString();

            try {
                IFunctionConfigBound definedBound = (IFunctionConfigBound) Class.forName(typeClass).newInstance();
                String nameVar = jsonElement.get(FunctionConfigSerializer.NAME).getAsString();

                if (definedBound instanceof PrimitiveBound) {
                    String lowerBoundVar = jsonElement.get(FunctionConfigSerializer.LOWER_BOUND_OF_PRIMITIVE_ARGUMENT).getAsString();
                    String upperBoundVar = jsonElement.get(FunctionConfigSerializer.UPPER_BOUND_OF_PRIMITIVE_ARGUMENT).getAsString();
                    fnConfig.getBoundOfArgumentsAndGlobalVariables().put(nameVar, new PrimitiveBound(lowerBoundVar, upperBoundVar));

                } else if (definedBound instanceof MultiplePrimitiveBound) {
                    MultiplePrimitiveBound multiplePrimitiveBound = new MultiplePrimitiveBound();
                    JsonArray equalAreas = jsonElement.getAsJsonArray(FunctionConfigSerializer.EQUAL_AREAS);
                    if (equalAreas != null) {
                        for (JsonElement area : equalAreas) {
                            String lowerBoundVar = ((JsonObject) area).
                                    get(FunctionConfigSerializer.LOWER_BOUND_OF_PRIMITIVE_ARGUMENT).getAsString();
                            String upperBoundVar = ((JsonObject) area).
                                    get(FunctionConfigSerializer.UPPER_BOUND_OF_PRIMITIVE_ARGUMENT).getAsString();
                            multiplePrimitiveBound.add(new PrimitiveBound(lowerBoundVar, upperBoundVar));
                        }
                    }
                    fnConfig.getBoundOfArgumentsAndGlobalVariables().put(nameVar, multiplePrimitiveBound);

                } else if (definedBound instanceof PointerOrArrayBound) {
                    JsonArray indexesStr = jsonElement.get(FunctionConfigSerializer.INDEXES).getAsJsonArray();
                    List<String> indexes = new ArrayList<>();
                    for (JsonElement indexStr : indexesStr) {
                        indexes.add(indexStr.getAsString());
                    }

                    String typeVar = jsonElement.get(FunctionConfigSerializer.TYPE_VAR).getAsString();
                    fnConfig.getBoundOfArgumentsAndGlobalVariables().put(nameVar, new PointerOrArrayBound(indexes, typeVar));

                } else if (definedBound instanceof UndefinedBound) {
                    fnConfig.getBoundOfArgumentsAndGlobalVariables().put(nameVar, new UndefinedBound());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        fnConfig.setTheMaximumNumberOfIterations(jsonObject.get(FunctionConfigSerializer.THEMAXNUMBER_OF_ITERATIONS).getAsInt());

        if (jsonObject.get(FunctionConfigSerializer.FLOAT_AND_DOUBLE_DELTA) != null) {
            fnConfig.setFloatAndDoubleDelta(jsonObject.get(FunctionConfigSerializer.FLOAT_AND_DOUBLE_DELTA).getAsDouble());
        }

        fnConfig.setBoundOfOtherNumberVars(new PrimitiveBound(
                jsonObject.get(FunctionConfigSerializer.LOWER_BOUND_OF_OTHER_NUMBER_VARS).getAsString(),
                jsonObject.get(FunctionConfigSerializer.UPPER_BOUND_OF_OTHER_NUMBER_VARS).getAsString()
        ));

        fnConfig.setBoundOfOtherCharacterVars(new PrimitiveBound(
                jsonObject.get(FunctionConfigSerializer.LOWER_BOUND_OF_OTHER_CHARACTER_VARS).getAsString(),
                jsonObject.get(FunctionConfigSerializer.UPPER_BOUND_OF_OTHER_CHARACTER_VARS).getAsString()
        ));

        fnConfig.getBoundOfArray().setLower(jsonObject.get(FunctionConfigSerializer.LOWER_BOUND_OF_OTHER_ARRAYS).getAsString());
        fnConfig.getBoundOfArray().setUpper(jsonObject.get(FunctionConfigSerializer.UPPER_BOUND_OF_OTHER_ARRAYS).getAsString());

        fnConfig.getBoundOfPointer().setLower(jsonObject.get(FunctionConfigSerializer.LOWER_BOUND_OF_OTHER_POINTERS).getAsString());
        fnConfig.getBoundOfPointer().setUpper(jsonObject.get(FunctionConfigSerializer.UPPER_BOUND_OF_OTHER_POINTERS).getAsString());

        return fnConfig;
    }
}
