package com.dse.report.converter;

import com.dse.parser.object.NumberOfCallNode;
import com.dse.report.element.Table;
import com.dse.search.Search2;
import com.dse.testcase_execution.result_trace.IResultTrace;
import com.dse.testdata.Iterator;
import com.dse.testdata.object.DataNode;
import com.dse.testdata.object.IDataNode;
import com.dse.testdata.object.IterationSubprogramNode;
import com.dse.testdata.object.SubprogramNode;
import com.dse.testdata.object.ValueDataNode;

import java.util.List;

public class MiddleAssertionConverter extends AssertionConverter {
    private int iterator;

    public MiddleAssertionConverter(List<IResultTrace> failures, int fcalls, int iterator) {
        super(failures, fcalls);
        this.iterator = iterator;
    }

    @Override
    public Table execute(SubprogramNode root) {
        Table table = generateHeaderRows(root);

        List<IDataNode> children = root.getChildren();

        if (!children.isEmpty()) {
            if (children.get(0) instanceof NumberOfCallNode) {
                NumberOfCallNode numberofcallnode = (NumberOfCallNode)(children.get(0));
                List<IDataNode> calls = numberofcallnode.getChildren();

                for (IDataNode call: calls) {
                    if (((IterationSubprogramNode)call).getIndex() == this.iterator) {
                        for (IDataNode child : call.getChildren()) {
                            if (child instanceof ValueDataNode) {
                                table = recursiveConvert(table, child, 2);
                            }
                        }
                    }
                }
            } else {
                for (IDataNode child : children) {
                    if (child instanceof ValueDataNode) {
                        table = recursiveConvert(table, child, 2);
                    }
                }
            }
        }

        return table;
    }

    // private ValueDataNode getCorrespondingIterator(ValueDataNode dataNode) {
    //     for (Iterator iterator : dataNode.getIterators()) {
    //         int start = iterator.getStartIdx();
    //         int repeat = iterator.getRepeat();

    //         if (repeat == Iterator.FILL_ALL
    //                 || (this.iterator >= start && this.iterator < start + repeat)) {
    //             return iterator.getDataNode();
    //         }
    //     }

    //     return dataNode;
    // }

    @Override
    public boolean isShowInReport(IDataNode node) {
        if (node instanceof ValueDataNode) {
            ValueDataNode valueNode = (ValueDataNode) node;
            DataNode expected = Search2.getExpectedValue(valueNode);
            if (expected != null && super.isShowInReport(expected)) {
                return true;
            }
        }

        return super.isShowInReport(node);
    }
}
