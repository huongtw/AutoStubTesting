package com.dse.parser.dependency;

import com.dse.parser.object.INode;

/**
 * Start row: pointer/array
 *
 * end row: number
 */
public class SizeOfArrayOrPointerDependency extends Dependency {
    private int startValueOfIterator;

    public SizeOfArrayOrPointerDependency(INode startArrow, INode endArrow) {
        super(startArrow, endArrow);
    }

    public int getStartValueOfIterator() {
        return startValueOfIterator;
    }

    public void setStartValueOfIterator(int startValueOfIterator) {
        this.startValueOfIterator = startValueOfIterator;
    }
}
