package com.dse.util;

import com.dse.config.WorkspaceConfig;
import com.dse.project_init.ProjectClone;

import java.io.File;
import java.util.Objects;

public class Cleaner {
    public static void main(String[] args) {
        clean(new File("datatest"));
    }

    public static void clean(File file) {
        String fileName = file.getName();

        if (file.isFile()) {

            boolean isAkaIgnore = fileName.contains(ProjectClone.CLONED_FILE_EXTENSION);
            boolean isOutput = fileName.endsWith(".o") || fileName.endsWith(".out");

            if (isAkaIgnore || isOutput) {
                System.out.println("delete " + file.getAbsolutePath());
                Utils.deleteFileOrFolder(file);
            }
        } else if (file.isDirectory()) {
            boolean isAkaWorkspace = fileName.equals(WorkspaceConfig.WORKING_SPACE_NAME);

            if (isAkaWorkspace) {
                System.out.println("delete " + file.getAbsolutePath());
                Utils.deleteFileOrFolder(file);
            } else {
                for (File child : Objects.requireNonNull(file.listFiles()))
                    clean(child);
            }
        }
    }
}
