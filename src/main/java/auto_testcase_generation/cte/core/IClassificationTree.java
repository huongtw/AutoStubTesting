package auto_testcase_generation.cte.core;

import auto_testcase_generation.cte.core.cteNode.CteNode;
import com.dse.parser.object.FunctionNode;

public interface IClassificationTree {
    CteNode searchNodeById(String nodeId);
    FunctionNode getfNode();
}
