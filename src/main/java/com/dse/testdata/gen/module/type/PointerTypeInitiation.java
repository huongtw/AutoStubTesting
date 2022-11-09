package com.dse.testdata.gen.module.type;

import com.dse.parser.object.ExternalVariableNode;
import com.dse.parser.object.VariableNode;
import com.dse.testdata.object.*;
import com.dse.util.TemplateUtils;
import com.dse.util.VariableTypeUtils;

/**
 * Khoi tao bien dau vao la kieu con tro bac 1
 */
public class PointerTypeInitiation extends AbstractTypeInitiation {
    public PointerTypeInitiation(VariableNode vParent, DataNode nParent) throws Exception {
        super(vParent, nParent);
    }

    @Override
    public ValueDataNode execute() throws Exception {
//        String rawType = Utils.getRealType(vParent.getReducedRawType(), vParent.getParent());
//        String rawType = vParent.getRealType();
//        String elementType = VariableTypeUtils.getElementTypeOfPointer(vParent.getRawType());
        //vParent.getCoreType();//VariableTypeUtils.getSimpleRawType(vParent).replaceAll(IRegex.POINTER, "");

        String reduceRawType = vParent.getRealType();
        ValueDataNode child;
        if (VariableTypeUtils.isVoidPointer(reduceRawType))
            child = new VoidPointerDataNode();
        else if (VariableTypeUtils.isVoid(reduceRawType))
            child = new VoidDataNode();
        else if (VariableTypeUtils.isChMultiLevel(reduceRawType))
            child = new PointerCharacterDataNode();
        else if (VariableTypeUtils.isNumMultiLevel(reduceRawType))
            child = new PointerNumberDataNode();
        else if (VariableTypeUtils.isStrMultiLevel(reduceRawType))
            child = new PointerStringDataNode();
        else if (VariableTypeUtils.isStructureMultiLevel(reduceRawType))
            child = new PointerStructureDataNode();
        else
            child = new OtherUnresolvedDataNode();

        child.setParent(nParent);
        child.setRawType(vParent.getRawType());
        child.setRealType(vParent.getRealType());
        child.setName(vParent.getNewType());
        child.setCorrespondingVar(vParent);

        if (child instanceof PointerDataNode) {
            ((PointerDataNode) child).setAllocatedSize(PointerDataNode.NULL_VALUE);
            ((PointerDataNode) child).setLevel(getLevel(vParent.getRealType()));
        }

        if (vParent instanceof ExternalVariableNode)
            child.setExternel(true);

        nParent.addChild(child);
        return child;
    }

    public static int getLevel(String rawType) {
        rawType = TemplateUtils.deleteTemplateParameters(rawType);
        return (int) rawType.chars().filter(c -> c == '*').count();
    }


}
