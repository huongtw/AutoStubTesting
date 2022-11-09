package com.dse.search;

import com.dse.parser.object.INode;
import com.dse.parser.object.LambdaFunctionNode;

public class LambdaFunctionNodeCondition extends SearchCondition{
    @Override
    public boolean isSatisfiable(INode iNode) {
        if (iNode instanceof LambdaFunctionNode)
            return true;
        return false;
    }
}
