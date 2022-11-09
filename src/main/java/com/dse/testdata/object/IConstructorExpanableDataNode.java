package com.dse.testdata.object;

import com.dse.parser.object.ICommonFunctionNode;

public interface IConstructorExpanableDataNode {
    ICommonFunctionNode getSelectedConstructor();

    void chooseConstructor(String constructor) throws Exception;

}
