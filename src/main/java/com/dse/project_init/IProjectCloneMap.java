package com.dse.project_init;

import com.dse.parser.object.AbstractFunctionNode;
import org.eclipse.cdt.core.dom.ast.IASTNode;

public interface IProjectCloneMap {
    IASTNode getClonedASTNode(IASTNode origin);

    int getLineInFunction(IASTNode origin);

    int getLineInFunction(AbstractFunctionNode functionNode, int line);
}

