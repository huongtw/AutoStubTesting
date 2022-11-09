package com.dse.config;

import com.dse.compiler.Terminal;
import com.dse.guifx_v3.main.AppStart;
import com.dse.logger.AkaLogger;
import com.dse.util.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AkaConfig {

    private final static AkaLogger logger = AkaLogger.get(AkaConfig.class);

    public static final String VERSION = "5.8.22";

    public static final boolean IS_SUPPORT_SWITCH_CASE = true;

    private static final String AKA_CONFIG_DIR_PATH = System.getProperty("user.home") + File.separator + ".akaconfig";
    public static final File LOCAL_DIRECTORY = new File(AKA_CONFIG_DIR_PATH);
    public static final File SETTING_PROPERTIES_PATH = new File(AKA_CONFIG_DIR_PATH + File.separator + "application.aka");

    @Expose
    private String localDirectory = LOCAL_DIRECTORY.getAbsolutePath();

    @Expose
    private String workingDirectory = "";

    @Expose
    private String openingWorkspaceDirectory = "";

    @Expose
    private String openWorkspaceConfig = "";

    @Expose
    private String z3Path = "";

    @Expose
    private List<String> recentEnvironments = new ArrayList<>(); // path to env files

    @Expose
    private List<String> openingWorkspaces = new ArrayList<>();

    public AkaConfig() {
    }

    synchronized public AkaConfig fromJson() {
        if (SETTING_PROPERTIES_PATH.exists()) {
            GsonBuilder builder = new GsonBuilder();
            builder.excludeFieldsWithoutExposeAnnotation();
            Gson gson = builder.setPrettyPrinting().create();
            return gson.fromJson(Utils.readFileContent(SETTING_PROPERTIES_PATH), AkaConfig.class);
        } else {
            logger.error("The " + SETTING_PROPERTIES_PATH.getAbsolutePath() + " does not exist!");
            AkaConfig config = new AkaConfig();
            config.exportToJson();
            return config;
        }
    }

    synchronized public void exportToJson() {
        GsonBuilder builder = new GsonBuilder();
        builder.excludeFieldsWithoutExposeAnnotation();
        Gson gson = builder.setPrettyPrinting().create();
        String json = gson.toJson(this);
        Utils.writeContentToFile(json, SETTING_PROPERTIES_PATH.getAbsolutePath());
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public AkaConfig setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
        new File(workingDirectory).mkdirs();
        return this;
    }

    public String getOpeningWorkspaceDirectory() {
        return openingWorkspaceDirectory;
    }

    public AkaConfig setOpeningWorkspaceDirectory(String openingWorkspaceDirectory) {
        this.openingWorkspaceDirectory = openingWorkspaceDirectory;
        new File(openingWorkspaceDirectory).mkdirs();

        this.workingDirectory = new File(openingWorkspaceDirectory).getParent();

        return this;
    }

    public String getLocalDirectory() {
        return localDirectory;
    }

    public AkaConfig setLocalDirectory(String localDirectory) {
        this.localDirectory = localDirectory;
        new File(localDirectory).mkdirs();
        return this;
    }

    public String getOpenWorkspaceConfig() {
        return openWorkspaceConfig;
    }

    public AkaConfig setOpenWorkspaceConfig(String openWorkspaceConfig) {
        this.openWorkspaceConfig = openWorkspaceConfig;
        return this;
    }

    public AkaConfig updateOpeningWorkspaces() {
        Terminal terRunningPID = null;
        String jps;
        String javaHome = System.getenv("JAVA_HOME");
        if (javaHome == null) {
            return this;
        }
        if (Utils.isWindows()) {
            jps = javaHome + "\\bin\\jps";
        } else {
            jps = javaHome + "/bin/jps";
        }
        try {
            terRunningPID = new Terminal(jps);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return this;
        }
        String[] res = terRunningPID.get().split("\n");
        Set<String> runningPid = new HashSet<>();
        for (int i = 1; i < res.length; i++) {
            runningPid.add(res[i].split(" ")[0]);
        }
        List<String> openning = new ArrayList<>();
        openingWorkspaces.forEach(i -> {
            if (runningPid.contains(i.split("@")[1])) {
                openning.add(i);
            }
        });
        this.openingWorkspaces = openning;
        exportToJson();
        return this;
    }

    public Set<String> getOpeningWorkspaces() {
        Set<String> opening = new HashSet<>();
        openingWorkspaces.forEach(i -> opening.add(i.split("@")[0]));
        return opening;
    }

    public AkaConfig setOpeningWorkspaces(List<String> openingWorkspaces) {
        this.openingWorkspaces = openingWorkspaces;
        return this;
    }

    public AkaConfig removeOpeningWorkspaces() {
        for (String i : openingWorkspaces) {
            if (AppStart.getPID().equals(i.split("@")[1])) {
                openingWorkspaces.remove(i);
                break;
            }
        }
        return this;
    }


    public AkaConfig addOpeningWorkspaces(String openingWorkspaceDirectory) {
        for (String i : openingWorkspaces) {
            if (AppStart.getPID().equals(i.split("@")[1])) {
                openingWorkspaces.remove(i);
                break;
            }
        }
        this.openingWorkspaces.add(openingWorkspaceDirectory + "@" + AppStart.getPID());
        return this;
    }

    public List<String> getRecentEnvironments() {
        for (int i = recentEnvironments.size() - 1; i >= 0; i--)
            if (!new File(recentEnvironments.get(i)).exists())
                recentEnvironments.remove(i);
        return recentEnvironments;
    }

    public AkaConfig setRecentEnvironments(List<String> recentEnvironments) {
        this.recentEnvironments = recentEnvironments;
        return this;
    }

    public String getZ3Path() {
        return z3Path;
    }

    public AkaConfig setZ3Path(String z3Path) {
        this.z3Path = z3Path;
        return this;
    }
}
