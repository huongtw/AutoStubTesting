package com.dse.parser.object;

public class SystemLibraryRoot extends ProjectNode {
    /**
     * Directory path where archive library stub codes
     * Absolute Path -> Virtual path (Root: LIBRARY)
     */
    private String location;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

}
