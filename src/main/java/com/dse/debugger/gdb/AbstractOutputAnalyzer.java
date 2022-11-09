package com.dse.debugger.gdb;

import com.dse.debugger.component.frame.GDBFrame;
import com.dse.debugger.component.variable.GDBVar;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;

public abstract class AbstractOutputAnalyzer implements IOutputAnalyzer {

    protected final static Gson gson = new Gson();

    protected ArrayList<GDBFrame> extractFrame(JsonArray frameArr) {
        ArrayList<GDBFrame> res = new ArrayList<>();
        frameArr.forEach(jsonElement -> {
            JsonObject jo = jsonElement.getAsJsonObject();
            res.add(gson.fromJson(jo, GDBFrame.class));
        });
        return res;
    }

    protected ArrayList<GDBVar> extractVar(JsonArray jsonArray) {
        ArrayList<GDBVar> res = new ArrayList<>();
        jsonArray.forEach(jsonElement -> {
            JsonObject jo = jsonElement.getAsJsonObject();
            res.add(gson.fromJson(jo, GDBVar.class));
        });
        return res;
    }

    protected String parseOutputToJson(String output, String cmd) {
        switch (cmd) {
            case GDB.GDB_FRAME_LIST:
                output = output.replaceAll("frame=\\{", "\\{");
                return makeJsonFromRes(output);
            case GDB.GDB_RUN:
            case GDB.GDB_NEXT:
            case GDB.GDB_FRAME_VARIABLES:
            default:
                return makeJsonFromRes(output);
        }
    }

    protected String makeJsonFromRes(String res) {
        int start = res.indexOf(",");
        String json = res.substring(start + 1).replaceAll("=", ":");
        json = "{" + json + "}";
        return json;
    }
}
