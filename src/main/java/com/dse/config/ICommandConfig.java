package com.dse.config;

import java.io.File;
import java.util.Map;

public interface ICommandConfig {
    ICommandConfig fromJson(String compilationFile);
    ICommandConfig fromJson();
    void exportToJson(File compilationFile);
    void exportToJson();
    Map<String, String> getCompilationCommands();
    String getLinkingCommand();
    String getExecutablePath();
}
