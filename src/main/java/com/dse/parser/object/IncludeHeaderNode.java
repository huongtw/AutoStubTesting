package com.dse.parser.object;

import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;

public class IncludeHeaderNode extends CustomASTNode<IASTPreprocessorIncludeStatement> {

    @Override
    public String getNewType() {
        return getAST().getName().getRawSignature();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IncludeHeaderNode) {
            IncludeHeaderNode objCast = (IncludeHeaderNode) obj;
            return objCast.getNewType().equals(getNewType());
        } else
            return false;
    }
}
