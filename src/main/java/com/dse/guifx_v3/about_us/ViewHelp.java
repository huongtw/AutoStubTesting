package com.dse.guifx_v3.about_us;

import com.dse.config.AkaConfig;
import com.dse.util.Utils;

import java.io.File;
import java.io.IOException;

public class ViewHelp {

    private final static String _USER_MANUAL_CHM_PATH = "/help/user_manual/UserManual.chm";
    private static final File CHM_FILE = new File("local/user_manual/UserManual_v" + AkaConfig.VERSION + ".chm");
    private static final File USER_MANUAL_DIR = new File("local/user_manual");

    public ViewHelp() throws IOException {
        if (!CHM_FILE.exists()) {
            Utils.deleteFileOrFolder(USER_MANUAL_DIR);
            Utils.copyFileFromResource(_USER_MANUAL_CHM_PATH, CHM_FILE);
        }
    }

    public void showUserManualDirectoryInUnixOS() throws Exception {
        Utils.openFolderorFileOnExplorer(USER_MANUAL_DIR.getAbsolutePath());
    }
    public void showUserManualInWindowOS() throws Exception {
        String absolutePath = CHM_FILE.getAbsolutePath();
        String[] commands = {"cmd", "/c", absolutePath};
        Runtime.getRuntime().exec(commands);
    }
}
