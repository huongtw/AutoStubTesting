package com.dse.user_code.envir;

import com.dse.config.WorkspaceConfig;
import com.dse.parser.ProjectParser;
import com.dse.parser.object.INode;
import com.dse.user_code.envir.type.AbstractEnvirUserCode;
import com.dse.user_code.envir.type.EnvirUserCodeItem;
import com.dse.logger.AkaLogger;
import com.dse.util.SpecialCharacter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EnvironmentUserCode {

    private static final AkaLogger logger = AkaLogger.get(EnvironmentUserCode.class);

    private String path;

    private List<EnvirUserCodeItem> list = new ArrayList<>();

    private static EnvironmentUserCode instance;

    public static EnvironmentUserCode getInstance() {
        if (instance == null) {
            instance = new EnvironmentUserCode();
        }

        return instance;
    }

    public static void setInstance(EnvironmentUserCode instance) {
        EnvironmentUserCode.instance = instance;
    }

    public String getPath() {
        return path;
    }

    public List<EnvirUserCodeItem> list() {
        return list;
    }

    public void add(EnvirUserCodeItem item) {
        if (list.contains(item)) {
            int index = list.indexOf(item);
            list.remove(index);
            list.add(index, item);
        } else
            list.add(item);
    }

    public void setList(List<EnvirUserCodeItem> list) {
        this.list = list;
    }

    public List<String> getAllFilePath() {
        return list.stream().map(AbstractEnvirUserCode::getPath).collect(Collectors.toList());
    }

    public void importFromFile() {
        WorkspaceConfig config = new WorkspaceConfig().fromJson();

        path = config.getEnvirUserCodeDirectory();

        File[] childFiles = new File(path).listFiles();
        if (childFiles != null) {
            for (File file : childFiles) {
                if (file.isDirectory())
                    continue;

                String path = file.getAbsolutePath();
                String name = file.getName().replace(FILE_EXT, SpecialCharacter.EMPTY);
                EnvirUserCodeItem userCode = new EnvirUserCodeItem();
                userCode.setName(name);
                userCode.setPath(path);
                userCode.read();
                list.add(userCode);

                logger.debug("Import Environment User Code " + name + " successfully");
            }
        }
    }

    public void exportToFile() {
        WorkspaceConfig config = new WorkspaceConfig().fromJson();
        path = config.getEnvirUserCodeDirectory();

        for (EnvirUserCodeItem userCode : list) {
            userCode.save();
        }
    }

    public INode parseTree() {
        if (path != null) {
            File envDir = new File(path);

            if (envDir.exists()) {
                ProjectParser projectParser = new ProjectParser(envDir);
                projectParser.setExpandTreeuptoMethodLevel_enabled(true);
                projectParser.setCpptoHeaderDependencyGeneration_enabled(true);
                projectParser.setParentReconstructor_enabled(false);
                projectParser.setExtendedDependencyGeneration_enabled(false);
                projectParser.setFuncCallDependencyGeneration_enabled(false);
                projectParser.setGenerateSetterandGetter_enabled(false);
                projectParser.setGlobalVarDependencyGeneration_enabled(false);

                return projectParser.getRootTree();
            }
        }

        return null;
    }

    public static final String FILE_EXT = ".h";

    public static final String ENVIR_FOLDER_NAME = "environment";
}
