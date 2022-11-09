package com.dse.debugger.utils;

import com.dse.util.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.dse.logger.AkaLogger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DebugCommandConfig {
    final static AkaLogger logger = AkaLogger.get(DebugCommandConfig.class);

    @Expose
    private final Map<String, String> debugCommands = new HashMap<>();

    @Expose
    private String linkingCommand = "";

    @Expose
    private String executablePath = "";

    public DebugCommandConfig() {

    }

    synchronized public void exportToJson(File compilationFile) {
        GsonBuilder builder = new GsonBuilder();
        builder.excludeFieldsWithoutExposeAnnotation();
        Gson gson = builder.setPrettyPrinting().create();
        String json = gson.toJson(this);

        Utils.writeContentToFile(json, compilationFile.getAbsolutePath());
    }

    public DebugCommandConfig fromJson(String debugCommandFile) {
        if (debugCommandFile != null && debugCommandFile.length() > 0 && new File(debugCommandFile).exists()) {
            GsonBuilder builder = new GsonBuilder();
            builder.excludeFieldsWithoutExposeAnnotation();
            Gson gson = builder.setPrettyPrinting().create();
            return gson.fromJson(Utils.readFileContent(debugCommandFile), DebugCommandConfig.class);
        } else {
            logger.error("The debug command file is not set up");
            return new DebugCommandConfig();
        }
    }

    public String getLinkingCommand() {
        return linkingCommand;
    }

    public Map<String, String> getDebugCommands() {
        return debugCommands;
    }

    public String getExecutablePath() {
        return executablePath;
    }

    public void setExecutablePath(String executablePath) {
        this.executablePath = executablePath;
    }

    public void setLinkingCommand(String linkingCommand) {
        this.linkingCommand = linkingCommand;
    }
}
