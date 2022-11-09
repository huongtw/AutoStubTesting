package com.dse.resolver;

import com.dse.util.Utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class StandardLibraryFinder {
    private static final String SUMMARY_PATH = "/standard_library.json";

    public static void main(String[] args) {
        String lib = new StandardLibraryFinder().findLibrary("cos");
        System.out.println(lib);
    }

    public String findLibrary(String target) {
        String json = Utils.readResourceContent(SUMMARY_PATH);;

        JsonArray jsonArray = JsonParser.parseString(json).getAsJsonArray();

        for (JsonElement library : jsonArray) {
            JsonObject libraryJsonObj = library.getAsJsonObject();
            String libraryName = libraryJsonObj.get("name").getAsString();
            JsonArray functions = libraryJsonObj.get("functions").getAsJsonArray();

            for (JsonElement function : functions) {
                JsonObject functionJsonObj = function.getAsJsonObject();
                String functionName = functionJsonObj.get("name").getAsString();

                if (target.equals(functionName)) {
                    return libraryName;
                }
            }
        }

        return null;
    }
}
