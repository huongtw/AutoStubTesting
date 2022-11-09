package com.dse.guifx_v3.objects;

import javafx.stage.DirectoryChooser;

// A singleton class that saves the last directory used by the user.
public class PersistentDirectoryChooser {
    private static DirectoryChooser instance = null;

    private PersistentDirectoryChooser() {

    }

    public static DirectoryChooser getInstance() {
        if (instance == null) {
            instance = new DirectoryChooser();
        }
        return instance;
    }
}