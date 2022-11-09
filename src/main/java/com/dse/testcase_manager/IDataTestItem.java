package com.dse.testcase_manager;

import com.dse.parser.object.ICommonFunctionNode;
import com.dse.testdata.object.DataNode;
import com.dse.testdata.object.RootDataNode;

import java.util.List;
import java.util.Map;

public interface IDataTestItem extends ITestItem {

    RootDataNode getRootDataNode();

    void setRootDataNode(RootDataNode rootDataNode);

    ICommonFunctionNode getFunctionNode();

    void setFunctionNode(ICommonFunctionNode functionNode);

    Map<DataNode, List<String>> getAdditionalIncludePathsMap();

    void putOrUpdateDataNodeIncludes(DataNode dataNode);

    void putOrUpdateDataNodeIncludes(DataNode dataNode, String includePath);
}
