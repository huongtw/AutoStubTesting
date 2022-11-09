package com.dse.testdata.gen.module.type;

import com.dse.parser.object.ExternalVariableNode;
import com.dse.parser.object.INode;
import com.dse.parser.object.STLTypeNode;
import com.dse.parser.object.VariableNode;
import com.dse.testdata.gen.module.TreeExpander;
import com.dse.testdata.object.DataNode;
import com.dse.testdata.object.ValueDataNode;
import com.dse.testdata.object.stl.*;
import com.dse.util.TemplateUtils;
import com.dse.util.VariableTypeUtils;
import com.dse.util.VariableTypeUtilsForStd;

import java.util.Arrays;

/**
 * Khoi tao bien truyen vao la kieu structure
 */
public class STLTypeInitiation extends AbstractTypeInitiation {
    private String[] templateArguments;

    public STLTypeInitiation(VariableNode vParent, DataNode nParent) throws Exception {
        super(vParent, nParent);
    }

    public STLTypeInitiation(VariableNode vParent, DataNode nParent, String templateType) throws Exception {
        super(vParent, nParent);
        //Eg: vector <int, float> -> vector<int,float> -> int,float -> {int, float}
        templateArguments = TemplateUtils.getTemplateArguments(templateType);
    }

    @Override
    public ValueDataNode execute() throws Exception {
        String type = vParent.getRawType();

        INode correspondingNode = vParent.getCorrespondingNode();
        if (correspondingNode instanceof STLTypeNode) {
            type = ((STLTypeNode) correspondingNode).getType();
        }

        STLDataNode child;

        if (VariableTypeUtilsForStd.isVector(type))
            child = new VectorDataNode();
        else if (VariableTypeUtilsForStd.isList(type))
            child = new ListDataNode();
        else if (VariableTypeUtilsForStd.isStack(type))
            child = new StackDataNode();
        else if (VariableTypeUtilsForStd.isQueue(type))
            child = new QueueDataNode();
        else if (VariableTypeUtilsForStd.isSet(type))
            child = new SetDataNode();
        else if (VariableTypeUtilsForStd.isPair(type))
            child = new PairDataNode();
        else if (VariableTypeUtilsForStd.isSTLArray(type)) {
            child = new STLArrayDataNode();
            int size = Integer.parseInt(templateArguments[1]);
            ((STLArrayDataNode) child).setSize(size);
            ((STLArrayDataNode) child).setSizeIsSet(true);
        } else if (VariableTypeUtilsForStd.isMap(type)) {
            child = new MapDataNode();
//            templateArguments = new ArrayList<>();
            String templateArgument = type.replace(VariableTypeUtils.STL.MAP.MAP, VariableTypeUtils.STL.PAIR.PAIR);
            templateArguments = new String[] {templateArgument};
        } else if (VariableTypeUtilsForStd.isMultiMap(type)) {
            child = new MultiMapDataNode();
            String templateArgument = String.format("std::pair<%s, %s>", templateArguments[0], templateArguments[1]);
            templateArguments = new String[] {templateArgument};
        } else if (VariableTypeUtilsForStd.isUniquePtr(type)) {
            child = new UniquePtrDataNode();
        } else if (VariableTypeUtilsForStd.isSharedPtr(type)) {
            child = new SharedPtrDataNode();
        } else if (VariableTypeUtilsForStd.isAutoPtr(type)) {
            child = new AutoPtrDataNode();
        } else if (VariableTypeUtilsForStd.isWeakPtr(type)) {
            child = new WeakPtrDataNode();
        } else if (VariableTypeUtilsForStd.isDefaultDelete(type)) {
            child = new DefaultDeleteDataNode();
        } else if (VariableTypeUtilsForStd.isAllocator(type)) {
            child = new AllocatorDataNode();
        } else if (VariableTypeUtilsForStd.isFunction(type)) {
            child = new StdFunctionDataNode();
        } else
            throw new Exception("Not support variable " + vParent);

        child.setArguments(Arrays.asList(templateArguments));

        child.setParent(nParent);
        child.setName(vParent.getNewType());
        child.setRawType(VariableTypeUtils.getFullRawType(vParent));
        child.setRealType(vParent.getRealType());
        child.setCorrespondingVar(vParent);

        (new TreeExpander()).expandTree(child);

        if (vParent instanceof ExternalVariableNode)
            child.setExternel(true);

        nParent.addChild(child);
        return child;
    }
}
