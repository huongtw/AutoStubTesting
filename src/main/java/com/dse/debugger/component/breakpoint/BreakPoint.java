package com.dse.debugger.component.breakpoint;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.List;

public class BreakPoint implements Comparable<BreakPoint> {
    @Expose
    @SerializedName("number")
    private String number = "";
    @Expose
    @SerializedName("file")
    private String file;
    @Expose
    @SerializedName("line")
    private int line;
    @Expose
    @SerializedName("fullname")
    private String full;
    @Expose
    @SerializedName("times")
    private int times;
    @Expose
    @SerializedName("disp")
    private String disp;
    @SerializedName("addr")
    @Expose
    private String addr;
    @Expose
    @SerializedName("func")
    private String func;
    @Expose
    @SerializedName("enabled")
    private String enabled;
    @Expose
    @SerializedName("cond")
    private String cond;

    private SimpleBooleanProperty selected = new SimpleBooleanProperty(true);

    private List<BreakPoint> children = null;

    public List<BreakPoint> getChildren() {
        return children;
    }

    public void setChildren(List<BreakPoint> children) {
        this.children = children;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public String getFull() {
        return full;
    }

    public void setFull(String full) {
        this.full = full;
    }

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    public String getDisp() {
        return disp;
    }

    public void setDisp(String disp) {
        this.disp = disp;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getFunc() {
        return func;
    }

    public void setFunc(String func) {
        this.func = func;
    }

    public String getEnabled() {
        return enabled;
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }

    public String getCond() {
        return cond;
    }

    public void setCond(String cond) {
        this.cond = cond;
    }

    public boolean getSelected() {
        return selected.get();
    }

    public SimpleBooleanProperty selectedProperty() {
        return selected;
    }

    public void setSelected(boolean selectedProperty) {
        this.selected.set(selectedProperty);
    }

    @Override
    public String toString() {
        return "BreakPoint{" +
                "number=" + number +
                ", file='" + file + '\'' +
                ", line=" + line +
                ", full='" + full + '\'' +
                ", times=" + times +
                ", disp='" + disp + '\'' +
                ", addr='" + addr + '\'' +
                ", func='" + func + '\'' +
                ", enabled='" + enabled + '\'' +
                ", cond='" + cond + '\'' +
                ", selected=" + selected +
                '}';
    }

    @Override
    public int compareTo(BreakPoint o) {
//        if (this.line == o.line) {
//            return 0;
//        }
//        if (this.number.compareTo(o.number) < 0){
//            return -1;
//        }
//        return 1;
        if(file.equals(o.file)) {
            return Integer.compare(line, o.line);
        } else {
            return file.compareTo(o.file);
        }
    }

    public void update(BreakPoint breakPoint) {
//        this.number = breakPoint.number;
//        this.addr = breakPoint.addr;
//        this.cond = breakPoint.cond;
//        this.disp = breakPoint.disp;
//        this.file = breakPoint.file;
        this.times = breakPoint.times;
    }
}
