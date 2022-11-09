package com.dse.coverage.gcov;

import com.dse.config.AkaConfig;
import com.dse.config.WorkspaceConfig;
import com.dse.util.PathUtils;
import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import org.apache.log4j.Category;

import java.io.File;

public class LcovWorkspaceConfig extends WorkspaceConfig {
    @Expose
    private String lcovPath;

    public String getLcovPath() {
        return lcovPath;
    }

    public  void setLcovPath(String lcovPath) {
        this.lcovPath = lcovPath;
        String absLcovPath = PathUtils.toAbsolute(lcovPath);
        (new File(absLcovPath)).mkdirs();
        Utils.writeContentToFile(LcovCmd.getCommandDataContent(), absLcovPath + File.separator + "lcovCommand.dat");
    }

    @Override
    public LcovWorkspaceConfig fromJson() {
        return load( new AkaConfig().fromJson().getOpenWorkspaceConfig());
    }

    @Override
    public LcovWorkspaceConfig load(String path) {
        File currentWorkingSpaceConfig = new File(path);
        if (currentWorkingSpaceConfig.exists()) {
            GsonBuilder builder = new GsonBuilder();
            builder.excludeFieldsWithoutExposeAnnotation();
            Gson gson = builder.setPrettyPrinting().create();
            LcovWorkspaceConfig setting = gson.fromJson(Utils.readFileContent(currentWorkingSpaceConfig), LcovWorkspaceConfig.class);
            return setting;
        } else {
            return new LcovWorkspaceConfig();
        }
    }

    @Override
    public void exportToJson(String workspaceConfig) {
        super.exportToJson(workspaceConfig);
    }

    @Override
    public void exportToJson() {
        super.exportToJson();
    }
}
