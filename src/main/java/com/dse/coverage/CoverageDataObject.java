package com.dse.coverage;


/**
 *  This class is used to contains data needed to display when view coverages
 */

public class CoverageDataObject {
    private float progress;
    private int total;
    private int visited;
    private String content;

    public String getContent() {
        return content;
    }

    public int getVisited() {
        return visited;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = progress;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public void setVisited(int visited) {
        this.visited = visited;
    }
}
