package com.dse.regression;

/**
 * An unresolved dependency when loading the environment
 */
public class UnresolvedDependency {
    private String start;
    private  String end;
    private String typeOfDependency;

    public UnresolvedDependency(String start, String end, String typeOfDependency){
        this.start = start;
        this.end = end;
        this.typeOfDependency = typeOfDependency;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getTypeOfDependency() {
        return typeOfDependency;
    }

    public void setTypeOfDependency(String typeOfDependency) {
        this.typeOfDependency = typeOfDependency;
    }
}
