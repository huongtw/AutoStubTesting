package com.dse.config;

import com.dse.config.compile.LinkEntry;
import com.dse.util.CompilerUtils;
import com.dse.util.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class CommandConfig implements ICommandConfig {

    private Map<String, String> compilationCommands = new HashMap<>();

    private LinkEntry linkEntry;

    private String executablePath = "";

    public CommandConfig fromJson() {
        return fromJson(new WorkspaceConfig().fromJson().getCommandFile());
    }

    @Override
    public CommandConfig fromJson(String compilationFile) {
        if (compilationFile != null && compilationFile.length() > 0 && new File(compilationFile).exists()) {
            GsonBuilder builder = new GsonBuilder();
//            builder.excludeFieldsWithoutExposeAnnotation();
            Gson gson = builder.setPrettyPrinting().create();
            String content = Utils.readFileContent(compilationFile);
            return gson.fromJson(content, CommandConfig.class);
        } else {
            return new CommandConfig();
        }
    }

    public CommandConfig setCompilationCommands(Map<String, String> compilationCommands) {
        this.compilationCommands = compilationCommands;
        return this;
    }

    public static void main(String[] args) {
        CommandConfig config = new CommandConfig().fromJson("/Users/lamnt/Projects/akautauto/datatest/aka-working-space/few/compilation.aka");
        System.out.println();
    }

    @Override
    synchronized public void exportToJson(File compilationFile) {
        GsonBuilder builder = new GsonBuilder();
//        builder.excludeFieldsWithoutExposeAnnotation();
        Gson gson = builder.setPrettyPrinting().create();
        String json = gson.toJson(this);

        Utils.writeContentToFile(json, compilationFile.getAbsolutePath());
    }

    synchronized public void exportToJson() {
        exportToJson(new File(new WorkspaceConfig().fromJson().getCommandFile()));
    }

    @Override
    public Map<String, String> getCompilationCommands() {
        return compilationCommands;
    }

    @Override
    public String getLinkingCommand() {
        String linkCommand = linkEntry.getCommand();
        String outFlag = linkEntry.getOutFlag();
        String exePath = linkEntry.getExeFile();
        String[] binPaths = linkEntry.getBinFiles().toArray(new String[0]);

        return CompilerUtils.generateLinkCommand(linkCommand, outFlag, exePath, binPaths);
    }

    @Override
    public String getExecutablePath() {
        return executablePath;
    }

    public CommandConfig setExecutablePath(String executablePath) {
        this.executablePath = executablePath;
        return this;
    }

    public CommandConfig setLinkEntry(LinkEntry linkEntry) {
        this.linkEntry = linkEntry;
        return this;
    }

    public LinkEntry getLinkEntry() {
        return linkEntry;
    }
}
