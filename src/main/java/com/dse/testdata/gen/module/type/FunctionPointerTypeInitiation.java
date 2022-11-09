package com.dse.testdata.gen.module.type;

import com.dse.parser.object.ExternalVariableNode;
import com.dse.parser.object.VariableNode;
import com.dse.testdata.object.*;
import com.dse.logger.AkaLogger;

public class FunctionPointerTypeInitiation extends AbstractTypeInitiation {
    final static AkaLogger logger = AkaLogger.get(FunctionPointerTypeInitiation.class);

    public FunctionPointerTypeInitiation(VariableNode vParent, DataNode nParent) throws Exception {
        super(vParent, nParent);
    }

    @Override
    public ValueDataNode execute() throws Exception {
        FunctionPointerDataNode child = new FunctionPointerDataNode();

        child.setParent(nParent);
        child.setRawType(vParent.getRawType());
        child.setRealType(vParent.getRealType());
        child.setName(vParent.getNewType());
        child.setCorrespondingVar(vParent);

        if (vParent instanceof ExternalVariableNode)
            child.setExternel(true);

        nParent.addChild(child);

        return child;
    }


}
