package com.dse.config.compile;

import java.util.ArrayList;
import java.util.List;

public class LinkEntry {

    private String command;
    private String outFlag;
    private String exeFile;
    private List<String> binFiles = new ArrayList<>();

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getOutFlag() {
        return outFlag;
    }

    public void setOutFlag(String outFlag) {
        this.outFlag = outFlag;
    }

    public String getExeFile() {
        return exeFile;
    }

    public void setExeFile(String exeFile) {
        this.exeFile = exeFile;
    }

    public List<String> getBinFiles() {
        return binFiles;
    }

    public void setBinFiles(List<String> binFiles) {
        this.binFiles = binFiles;
    }
}
