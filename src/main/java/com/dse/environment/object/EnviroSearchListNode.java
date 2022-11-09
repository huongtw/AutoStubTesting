package com.dse.environment.object;

import com.dse.util.PathUtils;

public class EnviroSearchListNode extends AbstractEnvironmentNode {
    private String searchList; // just a path

    public String getSearchList() {
        return searchList;
    }

    public void setSearchList(String searchList) {
        this.searchList = searchList;
    }

    @Override
    public String toString() {
        return super.toString() + " search_list = " + getSearchList();
    }

    @Override
    public String exportToFile() {
        return ENVIRO_SEARCH_LIST + " " + PathUtils.toRelative(searchList);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EnviroSearchListNode) {
            return ((EnviroSearchListNode) obj).getSearchList().equals(getSearchList());
        } else
            return false;
    }
}
