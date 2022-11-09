package com.dse.testdata.gen.module;

import com.dse.parser.object.*;
import com.dse.parser.object.INode;
import com.dse.testdata.object.*;
import com.dse.logger.AkaLogger;

/**
 * @author ducanh
 */
public class TreeExpander extends AbstractDataTreeExpander {
    final static AkaLogger logger = AkaLogger.get(TreeExpander.class);

    public TreeExpander() {

    }

    public void expandStructureNodeOnDataTree(ValueDataNode node, String name) throws Exception {
        node.getChildren().clear();
//        boolean isExist = node.getChildren().stream()
//                .anyMatch(n -> n.getName().equals(name));
//
//        if (!isExist) {
            VariableNode vParent = node.getCorrespondingVar();
            INode correspondingNode = vParent.resolveCoreType();

            if (correspondingNode instanceof StructureNode) {
                StructureNode childClass = (StructureNode) correspondingNode;
                for (IVariableNode n : childClass.getPublicAttributes()) {
                    if (n.getName().contains(name))
                        generateStructureItem((VariableNode) n, vParent + "." + name, node);
                }
            } else {
                logger.error("Do not handle the case " + correspondingNode.getClass());
            }
//        }
    }
}
