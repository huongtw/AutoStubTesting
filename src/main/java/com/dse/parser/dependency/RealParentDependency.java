package com.dse.parser.dependency;

import com.dse.parser.object.INode;

/**
 * Represent the real parent of a function.
 *
 * For example: source code file x.cpp
 #include "../Person.h"
 int Person::getDoubleWeight(){...} // this function is defined in the class .../Person.h/Person
 *
 *
 * The function Person::getDoubleWeight() has a physical parent: x.cpp
 *
 * The function Person::getDoubleWeight() has a real parent (or logical parent): .../Person.h/Person
 */
public class RealParentDependency extends Dependency {

    /**
     *
     * @param owner the function
     * @param refferedNode class or namespace which the function belongs to
     */
    public RealParentDependency(INode owner, INode refferedNode) {
        super(owner, refferedNode);
    }

}
