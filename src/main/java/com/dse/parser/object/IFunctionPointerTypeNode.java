package com.dse.parser.object;

public interface IFunctionPointerTypeNode extends INode {

    String getReturnType();

    String[] getArgumentTypes();

    String getFunctionName();

}
