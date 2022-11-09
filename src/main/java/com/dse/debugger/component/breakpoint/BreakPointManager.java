package com.dse.debugger.component.breakpoint;

import com.dse.debugger.component.Point;
import com.dse.logger.AkaLogger;
import com.dse.util.PathUtils;
import com.dse.util.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

public class BreakPointManager {
    private final AkaLogger logger = AkaLogger.get(BreakPointManager.class);

    private static BreakPointManager breakPointManager = null;

    private List<Point> activePoints;

    public List<Point> getActivePoints() {
        return activePoints;
    }

    private static void prepare(){
        breakPointManager = new BreakPointManager();
    }

    public static BreakPointManager getBreakPointManager() {
        if (breakPointManager == null) prepare();
        return breakPointManager;
    }

    private String breakFilePath;
    private ObservableMap<String, TreeSet<BreakPoint>> breakPointMap = null;

    public void setup(String path) {
        this.breakFilePath = path;
        File breakFile = new File(path);
        if (breakFile.exists()) {
            Gson gson = new Gson();
            String json = Utils.readFileContent(path);
            Type mapType = new TypeToken<HashMap<String, TreeSet<BreakPoint>>>(){}.getType();
            breakPointMap = FXCollections.observableMap(gson.fromJson(json,mapType));
            activePoints = new ArrayList<>();
        } else {
            GsonBuilder builder = new GsonBuilder().excludeFieldsWithoutExposeAnnotation();
            Gson gson = builder.setPrettyPrinting().create();
            String jsonString = gson.toJson(breakPointMap);
            breakPointMap = FXCollections.observableHashMap();
            activePoints = new ArrayList<>();
            Utils.writeContentToFile(jsonString,path);
        }
    }

    public TreeSet<BreakPoint> searchBreaksFromPath(String path) {
        return breakPointMap.get(path);
    }

    public BreakPoint searchBreakFromLineAndPath(String path, int line){
        String normalizedPath = Utils.normalizePath(path);
        TreeSet<BreakPoint> breakSet = breakPointMap.get(normalizedPath);
        if (breakSet == null) return null;
        for (BreakPoint br : breakSet) {
            if (br.getLine() == line) {
                return br;
            }
        }
        return null;
    }
    String normalizeFile(String path) {
        path = path.replaceAll("\\\\", "/");
        return path;
    }

    public ArrayList<BreakPoint> getAllBreaks(){
        ArrayList<BreakPoint> res = new ArrayList<>();
        breakPointMap.keySet().forEach(key -> {
            TreeSet<BreakPoint> temp = breakPointMap.get(key);
            res.addAll(temp);
            for(int i = 0; i < res.size(); i++) {
                res.get(i).setFile(normalizeFile(key));
                res.get(i).setFull(key);
            }
        });
        return res;
    }

    public ObservableMap<String, TreeSet<BreakPoint>> getBreakPointMap() {
        return this.breakPointMap;
    }




}

