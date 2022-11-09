package com.dse.testdata.object;

import com.dse.parser.object.ClassNode;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.INode;

import java.util.ArrayList;
import java.util.List;

/**
 * Represent real class variable
 */
public class SubTemplateClassDataNode extends SubClassDataNode {
    /**
     * Lay tat ca cac constructor cua mot class
     *
     * @return list cac constructor cua class
     */
    public List<ICommonFunctionNode> getConstructorsOnlyInCurrentClass() {
        List<ICommonFunctionNode> constructors = new ArrayList<>();

        INode correspondingNode = getCorrespondingType();

        if (correspondingNode instanceof ClassNode) {
            if (((ClassNode) correspondingNode).isTemplate())
                correspondingNode = correspondingNode.getChildren().get(0);

            ClassNode correspondingClass = (ClassNode) correspondingNode;
            constructors.addAll(correspondingClass.getConstructors());
        }

        return constructors;
    }
}