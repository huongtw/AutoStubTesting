package com.dse.testdata.gen.module.type;

import com.dse.parser.object.ExternalVariableNode;
import com.dse.parser.object.VariableNode;
import com.dse.testdata.object.DataNode;
import com.dse.testdata.object.EnumDataNode;
import com.dse.testdata.object.ValueDataNode;

/**
 * Khoi tao bien dau vao la kieu Enum
 */
public class EnumTypeInitiation extends AbstractTypeInitiation {
    public EnumTypeInitiation(VariableNode vParent, DataNode nParent) throws Exception {
        super(vParent, nParent);
    }

    @Override
    public ValueDataNode execute() throws Exception {
        EnumDataNode child = new EnumDataNode();

        child.setParent(nParent);
        child.setName(vParent.getNewType());
        child.setRawType(vParent.getFullType());
        child.setRealType(vParent.getRealType());
        child.setCorrespondingVar(vParent);
        if (vParent instanceof ExternalVariableNode)
            child.setExternel(true);
        nParent.addChild(child);
        return  child;
    }
}
