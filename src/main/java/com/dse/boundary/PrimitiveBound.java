package com.dse.boundary;

import com.dse.config.IFunctionConfigBound;
import com.google.gson.annotations.Expose;

/**
 * Bound of primitive variable
 *
 * @author ducanh
 */
public class PrimitiveBound implements IFunctionConfigBound {

    @Expose
    private String lower;

    @Expose
    private String upper;

    public PrimitiveBound(){
    }

    public PrimitiveBound(long lower, long upper) {
        this.lower = lower + "";
        this.upper = upper + "";
    }

    public PrimitiveBound(String lower, String upper) {
        this.lower = lower;
        this.upper = upper;
    }

    public String getLower() {
        return lower;
    }

    public double getLowerAsDouble() {
        return Double.parseDouble(getLower());
    }

    public void setLower(String lower) {
        this.lower = lower;
    }

    public String getUpper() {
        return upper;
    }

    public void setUpper(String upper) {
        this.upper = upper;
    }

    public double getUpperAsDouble() {
        return Double.parseDouble(getUpper());
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return "[" + lower + ".." + upper + "]";
    }
}
