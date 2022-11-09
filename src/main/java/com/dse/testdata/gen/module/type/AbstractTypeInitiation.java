package com.dse.testdata.gen.module.type;

import com.dse.parser.object.VariableNode;
import com.dse.testdata.object.DataNode;
import com.dse.testdata.object.IDataNode;
import com.dse.testdata.object.ValueDataNode;

public abstract class AbstractTypeInitiation implements ITypeInitiation {
    protected VariableNode vParent;
    protected DataNode nParent;

//    public AbstractTypeInitiation() {
//
//    }

    public AbstractTypeInitiation(VariableNode vParent, DataNode nParent) throws Exception {
        this.vParent = vParent;
        this.nParent = nParent;
//        execute();
    }

    @Override
    public ValueDataNode execute() throws Exception {
        return null;

    }


}
