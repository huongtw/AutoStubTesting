package com.dse.debugger.component.watches;

import com.google.gson.annotations.SerializedName;

public class WatchPoint {
    @SerializedName("exp")
    private String exp;
    @SerializedName("number")
    private String number;

    private boolean isNull = false;

    public WatchPoint(String exp, String number){
        this.exp = exp;
        this.number = number;
        this.isNull = false;
    }

    @Override
    public String toString() {
        return "WatchPoint{" +
                "exp='" + exp + '\'' +
                ", number='" + number + '\'' +
                '}';
    }

    public String getExp() {
        return exp;
    }

    public String getNumber() {
        return number;
    }

    public boolean isNull() {
        return isNull;
    }

    public void setNull(boolean aNull) {
        isNull = aNull;
    }
}
