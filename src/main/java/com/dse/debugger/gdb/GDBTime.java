package com.dse.debugger.gdb;

import com.google.gson.annotations.SerializedName;

public class GDBTime {
    @SerializedName("wallclock")
    double wallclock;
    @SerializedName("user")
    double user;
    @SerializedName("system")
    double system;
}
