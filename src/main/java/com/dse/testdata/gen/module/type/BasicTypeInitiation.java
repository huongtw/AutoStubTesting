package com.dse.testdata.gen.module.type;

import com.dse.parser.object.ExternalVariableNode;
import com.dse.parser.object.VariableNode;
import com.dse.testdata.object.*;
import com.dse.util.VariableTypeUtils;

/**
 * Khoi tao bien dau vao la kieu co ban
 */
public class BasicTypeInitiation extends AbstractTypeInitiation {
    public BasicTypeInitiation(VariableNode vParent, DataNode nParent) throws Exception {
        super(vParent, nParent);
    }

    @Override
    public ValueDataNode execute() throws Exception {
        ValueDataNode child = null;

        String realTypeOfParent = vParent.getRealType();
        if (VariableTypeUtils.isCh(realTypeOfParent))
            child = new NormalCharacterDataNode();
        else if (VariableTypeUtils.isNum(realTypeOfParent))
            child = new NormalNumberDataNode();
        else if (VariableTypeUtils.isStdInt(realTypeOfParent))
            child = new NormalNumberDataNode();
        else if (VariableTypeUtils.isVoidPointer(realTypeOfParent)){
            child = new VoidPointerDataNode();
        } else if (VariableTypeUtils.isVoid(realTypeOfParent)){
            child = new VoidDataNode();
        } else if (VariableTypeUtils.isStrBasic(realTypeOfParent)) {
            child = new NormalStringDataNode();
        }else{
            child = new OtherUnresolvedDataNode();
        }

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
