package com.dse.util;

/**
 * Note: Checking type for std should be put in a separate class
 *
 */
public class VariableTypeUtilsForStd extends VariableTypeUtils {
    /**
     * Check whether raw type is a specified std type
     * "list<int>" --> return true
     * <p>
     * "int a" --> return false
     *
     * @param targetStdType Ex: list, vector, stack, etc.
     * @param rawType
     * @return
     */
    public static boolean isStdType(String targetStdType, String rawType) {
        rawType = removeRedundantKeyword(rawType);

        // "A ::  X" -> "A::X"
        final String STD_SCOPE = "::";
        rawType = rawType.replaceAll("\\s*" + STD_SCOPE + "\\s*", STD_SCOPE);

        // "vector < " -> "vector<"
        rawType = rawType.replaceAll(targetStdType + "\\s*<\\s*", targetStdType + "<");

        if (rawType.startsWith(targetStdType + "<") || rawType.startsWith("std" + STD_SCOPE + targetStdType + "<"))
            return true;

        return false;
    }

    public static boolean isUniquePtr(String rawType) {
        return isStdType("unique_ptr", rawType);
    }

    public static boolean isSharedPtr(String rawType) {
        return isStdType("shared_ptr", rawType);
    }

    public static boolean isAutoPtr(String rawType) {
        return isStdType("auto_ptr", rawType);
    }

    public static boolean isWeakPtr(String rawType) {
        return isStdType("weak_ptr", rawType);
    }

    public static boolean isBadWeakPtr(String rawType) {
        return isStdType("bad_weak_ptr", rawType);
    }

    public static boolean isDefaultDelete(String rawType) {
        return rawType.equals("default_delete") || rawType.equals("std::default_delete")
                || isStdType("default_delete", rawType);
    }

    public static boolean isSTL(String rawType) {
        return isVector(rawType) || isList(rawType) || isSet(rawType)
                || isQueue(rawType) || isStack(rawType) || isSTLArray(rawType)
                || isPair(rawType) || isMap(rawType) || isMultiMap(rawType)
                || isSharedPtr(rawType) || isUniquePtr(rawType) || isAutoPtr(rawType)
                || isWeakPtr(rawType) || isBadWeakPtr(rawType)
                || isDefaultDelete(rawType) || isAllocator(rawType)
                || isFunction(rawType);
    }

    public static boolean isAllocator(String rawType) {
        return rawType.equals("allocator") || rawType.equals("std::allocator")
                || isStdType("allocator", rawType);
    }

    // ----------------------------------------
    // STD::VECTOR
    // ----------------------------------------
    public static boolean isVector(String rawType) {
        return isStdType("vector", rawType);
    }

    public static boolean isStdVectorBasic(String rawType) {
        if (isVector(rawType) && !isStdVectorMultiDimension(rawType) && !isStdVectorMultiLevel(rawType))
            return true;
        else
            return false;
    }

    public static boolean isStdVectorMultiLevel(String rawType) {
        if (isStdType("vector", rawType))
            if (rawType.matches(".*>" + MULTI_LEVEL_POINTER))
                return true;
        return false;
    }

    public static boolean isStdVectorMultiDimension(String rawType) {
        if (isStdType("vector", rawType))
            if (rawType.matches(".*>" + MULTI_DIMENSIONAL_ARRAY_INDEX))
                return true;
        return false;
    }
    // ----------------------------------------
    // STD::VECTOR - BEGIN
    // ----------------------------------------

    // ----------------------------------------
    // STD::LIST - BEGIN
    // ----------------------------------------
    public static boolean isList(String rawType) {
        return isStdType("list", rawType);
    }

    public static boolean isStdListBasic(String rawType) {
        if (isList(rawType) && !isStdListMultiDimension(rawType) && !isStdListMultiLevel(rawType))
            return true;
        else
            return false;
    }

    public static boolean isStdListMultiDimension(String rawType) {
        if (isStdType("list", rawType))
            if (rawType.matches(".*>" + MULTI_DIMENSIONAL_ARRAY_INDEX))
                return true;
        return false;
    }

    public static boolean isStdListMultiLevel(String rawType) {
        if (isStdType("list", rawType))
            if (rawType.matches(".*>" + MULTI_LEVEL_POINTER))
                return true;
        return false;
    }
    // ----------------------------------------
    // STD::LIST - END
    // ----------------------------------------

    // ----------------------------------------
    // STD::STACK - BEGIN
    // ----------------------------------------
    public static boolean isStack(String rawType) {
        return isStdType("stack", rawType);
    }

    public static boolean isStdStackBasic(String rawType) {
        if (isStack(rawType) && !isStdStackMultiDimension(rawType) && !isStdStackMultiLevel(rawType))
            return true;
        else
            return false;
    }

    public static boolean isStdStackMultiDimension(String rawType) {
        if (isStdType("stack", rawType))
            if (rawType.matches(".*>" + MULTI_DIMENSIONAL_ARRAY_INDEX))
                return true;
        return false;
    }

    public static boolean isStdStackMultiLevel(String rawType) {
        if (isStdType("stack", rawType))
            if (rawType.matches(".*>" + MULTI_LEVEL_POINTER))
                return true;
        return false;
    }
    // ----------------------------------------
    // STD::STACK - END
    // ----------------------------------------

    // ----------------------------------------
    // STD::QUEUE - BEGIN
    // ----------------------------------------
    public static boolean isQueue(String rawType) {
        return isStdType("queue", rawType);
    }

    public static boolean isStdQueueBasic(String rawType) {
        if (isQueue(rawType) && !isStdQueueMultiDimension(rawType) && !isStdQueueMultiLevel(rawType))
            return true;
        else
            return false;
    }

    public static boolean isStdQueueMultiDimension(String rawType) {
        if (isStdType("queue", rawType))
            if (rawType.matches(".*>" + MULTI_DIMENSIONAL_ARRAY_INDEX))
                return true;
        return false;
    }

    public static boolean isStdQueueMultiLevel(String rawType) {
        if (isStdType("queue", rawType))
            if (rawType.matches(".*>" + MULTI_LEVEL_POINTER))
                return true;
        return false;
    }
    // ----------------------------------------
    // STD::QUEUE - END
    // ----------------------------------------

    // ----------------------------------------
    // STD::SET - BEGIN
    // ----------------------------------------
    public static boolean isSet(String rawType) {
        return isStdType("set", rawType);
    }

    public static boolean isStdSetBasic(String rawType) {
        if (isSet(rawType) && !isStdSetMultiDimension(rawType) && !isStdSetMultiLevel(rawType))
            return true;
        else
            return false;
    }

    public static boolean isStdSetMultiDimension(String rawType) {
        if (isStdType("set", rawType))
            if (rawType.matches(".*>" + MULTI_DIMENSIONAL_ARRAY_INDEX))
                return true;
        return false;
    }

    public static boolean isStdSetMultiLevel(String rawType) {
        if (isStdType("set", rawType))
            if (rawType.matches(".*>" + MULTI_LEVEL_POINTER))
                return true;
        return false;
    }
    // ----------------------------------------
    // STD::SET - END
    // ----------------------------------------

    // ----------------------------------------
    // STD::PAIR - BEGIN
    // ----------------------------------------
    public static boolean isPair(String rawType) {
        return isStdType("pair", rawType);
    }

    public static boolean isStdPairBasic(String rawType) {
        if (isPair(rawType) && !isStdPairMultiDimension(rawType) && !isStdPairMultiLevel(rawType))
            return true;
        else
            return false;
    }

    public static boolean isStdPairMultiDimension(String rawType) {
        if (isStdType("pair", rawType))
            if (rawType.matches(".*>" + MULTI_DIMENSIONAL_ARRAY_INDEX))
                return true;
        return false;
    }

    public static boolean isStdPairMultiLevel(String rawType) {
        if (isStdType("pair", rawType))
            if (rawType.matches(".*>" + MULTI_LEVEL_POINTER))
                return true;
        return false;
    }
    // ----------------------------------------
    // STD::PAIR - END
    // ----------------------------------------

    // ----------------------------------------
    // STD::MAP - BEGIN
    // ----------------------------------------
    public static boolean isMap(String rawType) {
        return isStdType("map", rawType);
    }

    public static boolean isStdMapBasic(String rawType) {
        if (isMap(rawType) && !isStdMapMultiDimension(rawType) && !isStdMapMultiLevel(rawType))
            return true;
        else
            return false;
    }

    public static boolean isStdMapMultiDimension(String rawType) {
        if (isStdType("map", rawType))
            if (rawType.matches(".*>" + MULTI_DIMENSIONAL_ARRAY_INDEX))
                return true;
        return false;
    }

    public static boolean isStdMapMultiLevel(String rawType) {
        if (isStdType("map", rawType))
            if (rawType.matches(".*>" + MULTI_LEVEL_POINTER))
                return true;
        return false;
    }
    // ----------------------------------------
    // STD::MAP - END
    // ----------------------------------------

    // ----------------------------------------
    // STD::MULTIMAP - BEGIN
    // ----------------------------------------
    public static boolean isMultiMap(String rawType) {
        return isStdType("multimap", rawType);
    }

    public static boolean isStdMultiMapBasic(String rawType) {
        if (isMultiMap(rawType) && !isStdMultiMapMultiDimension(rawType) && !isStdMultiMapMultiLevel(rawType))
            return true;
        else
            return false;
    }

    public static boolean isStdMultiMapMultiDimension(String rawType) {
        if (isStdType("multimap", rawType))
            if (rawType.matches(".*>" + MULTI_DIMENSIONAL_ARRAY_INDEX))
                return true;
        return false;
    }

    public static boolean isStdMultiMapMultiLevel(String rawType) {
        if (isStdType("multimap", rawType))
            if (rawType.matches(".*>" + MULTI_LEVEL_POINTER))
                return true;
        return false;
    }
    // ----------------------------------------
    // STD::MULTIMAP - END
    // ----------------------------------------

    // ----------------------------------------
    // STD::FUNCTION - BEGIN
    // ----------------------------------------
    public static boolean isFunction(String rawType) {
        return isStdType("function", rawType);
    }

    public static boolean isStdFunctionBasic(String rawType) {
        if (isFunction(rawType) && !isStdFunctionMultiDimension(rawType) && !isStdFunctionMultiLevel(rawType))
            return true;
        else
            return false;
    }

    public static boolean isStdFunctionMultiDimension(String rawType) {
        if (isStdType("function", rawType))
            if (rawType.matches(".*>" + MULTI_DIMENSIONAL_ARRAY_INDEX))
                return true;
        return false;
    }

    public static boolean isStdFunctionMultiLevel(String rawType) {
        if (isStdType("function", rawType))
            if (rawType.matches(".*>" + MULTI_LEVEL_POINTER))
                return true;
        return false;
    }

    // ----------------------------------------
    // STD::FUNCTION - END
    // ----------------------------------------
}