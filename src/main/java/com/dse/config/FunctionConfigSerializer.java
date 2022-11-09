package com.dse.config;

import com.dse.boundary.MultiplePrimitiveBound;
import com.dse.boundary.PointerOrArrayBound;
import com.dse.boundary.PrimitiveBound;
import com.dse.util.PathUtils;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.Map;

public class FunctionConfigSerializer implements JsonSerializer<FunctionConfig> {

    public JsonElement serialize(FunctionConfig src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject json = new JsonObject();

        if (src.getFunctionNode() != null)
            json.addProperty(FUNCTION, PathUtils.toRelative(src.getFunctionNode().getAbsolutePath()));
        else
            json.addProperty(FUNCTION, "");

        json.addProperty(TESTDATA_GEN_STRATEGY, src.getTestdataGenStrategy());
        json.addProperty(THEMAXNUMBER_OF_ITERATIONS, src.getTheMaximumNumberOfIterations());
        json.addProperty(FLOAT_AND_DOUBLE_DELTA, src.getFloatAndDoubleDelta());

        json.addProperty(LOWER_BOUND_OF_OTHER_NUMBER_VARS, src.getBoundOfOtherNumberVars().getLower());
        json.addProperty(UPPER_BOUND_OF_OTHER_NUMBER_VARS, src.getBoundOfOtherNumberVars().getUpper());

        json.addProperty(LOWER_BOUND_OF_OTHER_CHARACTER_VARS, src.getBoundOfOtherCharacterVars().getLower());
        json.addProperty(UPPER_BOUND_OF_OTHER_CHARACTER_VARS, src.getBoundOfOtherCharacterVars().getUpper());

        json.addProperty(LOWER_BOUND_OF_OTHER_ARRAYS, src.getBoundOfArray().getLower());
        json.addProperty(UPPER_BOUND_OF_OTHER_ARRAYS, src.getBoundOfArray().getUpper());

        json.addProperty(LOWER_BOUND_OF_OTHER_POINTERS, src.getBoundOfPointer().getLower());
        json.addProperty(UPPER_BOUND_OF_OTHER_POINTERS, src.getBoundOfPointer().getUpper());

        Map<String, IFunctionConfigBound> fnBounds = src.getBoundOfArgumentsAndGlobalVariables();
        // bound of primitive element
        {
            JsonArray bound = new JsonArray();
            for (String key : fnBounds.keySet()) {
                IFunctionConfigBound paramBound = src.getBoundOfArgumentsAndGlobalVariables().get(key);
                if (paramBound instanceof PrimitiveBound) {
                    String lower = ((PrimitiveBound) paramBound).getLower();
                    String upper = ((PrimitiveBound) paramBound).getUpper();
                    JsonObject obj = new JsonObject();
                    obj.addProperty(TYPE_CLASS, paramBound.getClass().getName());
                    obj.addProperty(NAME, key);
                    obj.addProperty(LOWER_BOUND_OF_PRIMITIVE_ARGUMENT, lower);
                    obj.addProperty(UPPER_BOUND_OF_PRIMITIVE_ARGUMENT, upper);
                    bound.add(obj);

                } else if (paramBound instanceof MultiplePrimitiveBound) {
                    JsonObject obj = new JsonObject();
                    obj.addProperty(TYPE_CLASS, paramBound.getClass().getName());
                    obj.addProperty(NAME, key);
                    JsonArray equalAreas = new JsonArray();
                    obj.add(EQUAL_AREAS, equalAreas);
                    for (PrimitiveBound primitiveBound : ((MultiplePrimitiveBound) paramBound)) {
                        JsonObject boundObject = new JsonObject();
                        boundObject.addProperty(LOWER_BOUND_OF_PRIMITIVE_ARGUMENT, primitiveBound.getLower());
                        boundObject.addProperty(UPPER_BOUND_OF_PRIMITIVE_ARGUMENT, primitiveBound.getUpper());
                        equalAreas.add(boundObject);
                    }
                    bound.add(obj);

                } else if (paramBound instanceof PointerOrArrayBound) {
                    JsonObject obj = new JsonObject();
                    obj.addProperty(TYPE_CLASS, paramBound.getClass().getName());
                    obj.addProperty(TYPE_VAR, ((PointerOrArrayBound) paramBound).getType());
                    obj.addProperty(NAME, key);

                    JsonArray indexes = new JsonArray();
                    for (String index : ((PointerOrArrayBound) paramBound).getIndexes())
                        indexes.add(index);
                    obj.add(INDEXES, indexes);
                    bound.add(obj);

                } else if (paramBound instanceof UndefinedBound){
                    JsonObject obj = new JsonObject();
                    obj.addProperty(TYPE_CLASS, paramBound.getClass().getName());
                    obj.addProperty(NAME, key);
                    bound.add(obj);
                }
            }
            json.add(BOUND_OF_ARGUMENTS, bound);
        }

        return json;
    }

    public static final String TESTDATA_GEN_STRATEGY = "testdataGenStrategy";
    public static final String THEMAXNUMBER_OF_ITERATIONS = "theMaxNumberOfIterations";
    public static final String FLOAT_AND_DOUBLE_DELTA = "floatAndDoubleDelta";

    public static final String BOUND_OF_ARGUMENTS = "boundOfArguments";
    public static final String EQUAL_AREAS = "equalAreas";
    public static final String LOWER_BOUND_OF_PRIMITIVE_ARGUMENT = "lower";
    public static final String UPPER_BOUND_OF_PRIMITIVE_ARGUMENT = "upper";

    public static final String LOWER_BOUND_OF_OTHER_NUMBER_VARS = "lowerBoundOfOtherNumberVars";
    public static final String UPPER_BOUND_OF_OTHER_NUMBER_VARS = "upperBoundOfOtherNumberVars";

    public static final String LOWER_BOUND_OF_OTHER_CHARACTER_VARS = "lowerBoundOfOtherCharacterVars";
    public static final String UPPER_BOUND_OF_OTHER_CHARACTER_VARS = "upperBoundOfOtherCharacterVars";

    public static final String INDEXES = "indexes";

    public static final String FUNCTION = "function";
    public static final String TYPE_CLASS = "type_class";
    public static final String TYPE_VAR = "type_var";
    public static final String NAME = "name";

    public static final String LOWER_BOUND_OF_OTHER_ARRAYS = "lowerBoundOfOtherArrays";
    public static final String UPPER_BOUND_OF_OTHER_ARRAYS = "upperBoundOfOtherArrays";

    public static final String LOWER_BOUND_OF_OTHER_POINTERS = "lowerBoundOfOtherPointers";
    public static final String UPPER_BOUND_OF_OTHER_POINTERS = "upperBoundOfOtherPointers";

}
