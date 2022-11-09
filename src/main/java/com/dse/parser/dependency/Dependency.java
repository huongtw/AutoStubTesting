package com.dse.parser.dependency;

import com.dse.parser.object.INode;
import com.google.gson.annotations.Expose;

/**
 * If A use/extend B then A and B are start of arrow and end of arrow,
 * respectively
 *
 * @author DucAnh
 */
public class Dependency {
    @Expose
    private String startArrowName;
    @Expose
    private String endArrowName;

    protected INode startArrow;
    protected INode endArrow;

    public Dependency() {
    }

    public Dependency(INode startArrow, INode endArrow) {
        this.startArrow = startArrow;
        this.endArrow = endArrow;

        this.startArrowName = this.startArrow.getAbsolutePath();
        this.endArrowName = this.endArrow.getAbsolutePath();
    }

    public INode getEndArrow() {
        return endArrow;
    }

    public void setEndArrow(INode endArrow) {
        this.endArrow = endArrow;
        this.endArrowName = this.endArrow.getAbsolutePath();
    }

    public INode getStartArrow() {
        return startArrow;
    }

    public void setStartArrow(INode startArrow) {
        this.startArrow = startArrow;
        this.startArrowName = this.startArrow.getAbsolutePath();
    }

    @Override
    public String toString() {
        return "[" + getClass().getSimpleName() + "]:\t" + "[" + startArrow.getClass().getSimpleName() + "] " + startArrow.getAbsolutePath() + " -> "
                + "[" + startArrow.getClass().getSimpleName() + "] " + endArrow.getAbsolutePath();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Dependency) {
            Dependency objCast = (Dependency) obj;
            return objCast.getEndArrow().getAbsolutePath().equals(endArrow.getAbsolutePath())
                    && objCast.getStartArrow().getAbsolutePath().equals(startArrow.getAbsolutePath());
        } else
            return false;
    }

    public String getStartArrowName() {
        return startArrowName;
    }

    public void setStartArrowName(String startArrowName) {
        this.startArrowName = startArrowName;
    }

    public String getEndArrowName() {
        return endArrowName;
    }

    public void setEndArrowName(String endArrowName) {
        this.endArrowName = endArrowName;
    }
}
