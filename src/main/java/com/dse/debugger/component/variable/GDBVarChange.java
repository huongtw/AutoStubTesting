package com.dse.debugger.component.variable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GDBVarChange {
    @Expose
    @SerializedName("name")
    private String name;
    @Expose
    @SerializedName("value")
    private String value;
    @Expose
    @SerializedName("in_scope")
    private String in_scope;
    @Expose
    @SerializedName("type_changed")
    private String type_changed;
    @Expose
    @SerializedName("new_type")
    private String new_type;
    @Expose
    @SerializedName("new_num_children")
    private String new_num_children;
    @Expose
    @SerializedName("displayhint")
    private String displayhint;
    @Expose
    @SerializedName("has_more")
    private String has_more;
    @Expose
    @SerializedName("dynamic")
    private String dynamic;
    @Expose
    @SerializedName("new_children")
    private String new_children;

    private String realName;

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "GDBVarChange{" +
                "real_name='" + realName + "\'" +
                ",name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", in_scope='" + in_scope + '\'' +
                ", type_changed='" + type_changed + '\'' +
                ", new_type='" + new_type + '\'' +
                ", new_num_children='" + new_num_children + '\'' +
                ", displayhint='" + displayhint + '\'' +
                ", has_more='" + has_more + '\'' +
                ", dynamic='" + dynamic + '\'' +
                ", new_children='" + new_children + '\'' +
                '}';
    }
}
