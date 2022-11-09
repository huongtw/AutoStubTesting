package com.dse.debugger.component.frame;

import com.google.gson.annotations.SerializedName;

public class GDBFrame {
    @SerializedName("level")
    private int level;
    @SerializedName("addr")
    private String addr;
    @SerializedName("func")
    private String func;
    @SerializedName("line")
    private int line;
    @SerializedName("file")
    private String file;
    @SerializedName("fullname")
    private String fullName;

    public int getLevel() {
        return level;
    }

    public String getAddr() {
        return addr;
    }

    public String getFunc() {
        return func;
    }

    public int getLine() {
        return line;
    }

    public String getFile() {
        return file;
    }

    public String getFullName() {
        return fullName;
    }

    @Override
    public String toString() {
        return "GDBFrame{" +
                "level=" + level +
                ", addr='" + addr + '\'' +
                ", func='" + func + '\'' +
                ", line=" + line +
                ", file='" + file + '\'' +
                ", fullName='" + fullName + '\'' +
                '}';
    }
}
