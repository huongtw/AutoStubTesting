package com.dse.report.converter;

import com.dse.report.element.Table;
import com.dse.testdata.object.*;
import com.dse.util.NodeType;

public interface IHtmlConverter {
    Table recursiveConvert(Table table, IDataNode node, int level);

    Table.Row convertSingleNode(IDataNode node, int level);
}
