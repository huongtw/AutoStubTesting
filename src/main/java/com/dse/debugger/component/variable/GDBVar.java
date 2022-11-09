package com.dse.debugger.component.variable;

import com.dse.debugger.component.watches.WatchPoint;
import com.google.gson.annotations.SerializedName;

public class GDBVar {
    @SerializedName("type")
    private String type;
    @SerializedName("value")
    private String value;
    @SerializedName("name")
    private String name;
    @SerializedName("numchild")
    private int numchild;
    @SerializedName("dynamic")
    private String dynamic;

    private int size = 0;

    private int startIdx = 0;

    private int endIdx = 0;

    private String realName;

    private WatchPoint watchPoint = null;

    public GDBVar() {

    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public GDBVar(String value){
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumchild(){
        return numchild;
    }

    public void setNumchild(int numchild) {
        this.numchild = numchild;
    }

    public String getDynamic() {
        return dynamic;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getStartIdx() {
        return startIdx;
    }

    public void setStartIdx(int startIdx) {
        this.startIdx = startIdx;
    }

    public int getEndIdx() {
        return endIdx;
    }

    public void setEndIdx(int endIdx) {
        this.endIdx = endIdx;
    }

    public WatchPoint getWatchPoint() {
        return watchPoint;
    }

    public void setWatchPoint(WatchPoint watchPoint) {
        this.watchPoint = watchPoint;
    }

    @Override
    public String toString() {
        return "GDBVar{" +
                "type='" + type + '\'' +
                ", value='" + value + '\'' +
                ", name='" + name + '\'' +
                ", numchild=" + numchild +
                ", dynamic='" + dynamic + '\'' +
                ", size=" + size +
                ", startIdx=" + startIdx +
                ", endIdx=" + endIdx +
                ", realName='" + realName + '\'' +
                ", watchPoint='" + watchPoint + '\'' +
                '}';
    }
}
