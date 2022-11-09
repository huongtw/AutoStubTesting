package com.dse.testdata.gen.module.type;

import com.dse.parser.object.ExternalVariableNode;
import com.dse.parser.object.VariableNode;
import com.dse.testdata.object.*;
import com.dse.logger.AkaLogger;
import com.dse.util.VariableTypeUtils;

/**
 * Khoi tao bien dau vao la kieu co ban
 */
public class ProblemTypeInitiation extends AbstractTypeInitiation {
    final static AkaLogger logger = AkaLogger.get(ProblemTypeInitiation.class);

    public ProblemTypeInitiation(VariableNode vParent, DataNode nParent) throws Exception {
        super(vParent, nParent);
    }

    @Override
    public ValueDataNode execute() throws Exception {
        String type =vParent.getRealType();
        ValueDataNode child = null;
        if (VariableTypeUtils.isVoidPointer(type)) {
            child = new VoidPointerDataNode();
        } else if (VariableTypeUtils.isVoid(type)){
            child = new VoidDataNode();
        } else
            child = new OtherUnresolvedDataNode();

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
