package com.dse.guifx_v3.controllers.object.build_environment;

import com.dse.config.AkaConfig;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SourcePath {
    public static final String LIBRARY_INCLUDE_DIRECTORY = "LIBRARY_INCLUDE_DIRECTORY";
    public static final String SEARCH_DIRECTORY = "SEARCH_DIRECTORY";
    public static final String TYPE_HANDLED_DIRECTORY = "TYPE_HANDLED_DIRECTORY";
    static final String DEFAULT = "DEFAULT";
    private static boolean useRelativePath = false;
    private String type = DEFAULT;
    private String absolutePath;
    private String relativePath;
    private boolean isExisted = false;

    public SourcePath() {}
    public SourcePath(String absolutePath) {
        this.absolutePath = absolutePath;
        Path pathBase = Paths.get(new File(new AkaConfig().fromJson().getWorkingDirectory()).getAbsolutePath());
        Path pathAbsolute = Paths.get(absolutePath);
        try {
            Path pathRelative = pathBase.relativize(pathAbsolute);
            this.relativePath = pathRelative.toString();
        } catch (IllegalArgumentException e) {
            this.relativePath = absolutePath;
        } catch (Exception e) {
            e.printStackTrace();
        }

        File check = new File(absolutePath);
        isExisted = check.exists();
    }

    public String toString() {
        if (useRelativePath) {
            return relativePath;
        }
        return absolutePath;
    }

    public boolean isExisted() {
        return isExisted;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public static void setUseRelativePath(boolean useRelativePath) {
        SourcePath.useRelativePath = useRelativePath;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }
}