package com.dse.testdata.gen.module.subtree;

import com.dse.parser.FunctionCallParser;
import com.dse.parser.object.FunctionNode;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.VariableNode;
import com.dse.testdata.gen.module.InitialTreeGen;
import com.dse.testdata.object.DataNode;
import com.dse.testdata.object.IDataNode;
import com.dse.testdata.object.ValueDataNode;
import com.dse.logger.AkaLogger;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractInitialTreeGen implements IInitialSubTreeGen {
    protected final static AkaLogger logger = AkaLogger.get(AbstractInitialTreeGen.class);

    protected ICommonFunctionNode functionNode;
    protected IDataNode root;

    @Override
    public ValueDataNode genInitialTree(VariableNode vCurrentChild, DataNode nCurrentParent) throws Exception {
        return new InitialTreeGen().genInitialTree(vCurrentChild, nCurrentParent);
    }

    public ICommonFunctionNode getFunctionNode() {
        return functionNode;
    }

    public void setFunctionNode(ICommonFunctionNode functionNode) {
        this.functionNode = functionNode;
    }

    public IDataNode getRoot() {
        return root;
    }

    public void setRoot(IDataNode root) {
        this.root = root;
    }

    public static List<String> filterCalledFunctions(ICommonFunctionNode functionNode) {
        FunctionCallParser parser = new FunctionCallParser();
        IASTNode fASTNode = null;
        if (functionNode instanceof FunctionNode) {
            fASTNode = ((FunctionNode) functionNode).getAST();
            fASTNode.accept(parser);
        }
        if (fASTNode == null) return null;
        List<IASTFunctionCallExpression> expressions = parser.getExpressions();
        List<String> calledFunctionsName = new ArrayList<>();
        for (IASTFunctionCallExpression exp : expressions) {
            String expName = exp.getFunctionNameExpression().getRawSignature();
            if (!calledFunctionsName.contains(expName)) {
                calledFunctionsName.add(expName);
            }
        }
        return calledFunctionsName;
    }
}
