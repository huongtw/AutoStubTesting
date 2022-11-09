package com.dse.testdata.gen.module.subtree;

import com.dse.parser.funcdetail.IFunctionDetailTree;
import com.dse.parser.object.*;
import com.dse.parser.object.INode;
import com.dse.search.Search2;
import com.dse.testdata.object.*;
import com.dse.testdata.object.RootDataNode;

import java.util.List;

/**
 * Given a function, this class will generate an initial tree of parameters
 */
public class InitialArgTreeGen extends AbstractInitialTreeGen {

    @Override
    public void generateCompleteTree(RootDataNode root, IFunctionDetailTree functionTree) throws Exception {
        this.root = root;
        this.functionNode = root.getFunctionNode();

        generate(root, functionNode);

//        SubprogramNode subprogramNode = new SubprogramNode(functionNode);
//        subprogramNode.setName(functionNode.getSimpleName());
//
//        if (functionNode instanceof MacroFunctionNode) {
//            subprogramNode = new MacroSubprogramDataNode((MacroFunctionNode) functionNode);
//
//        } else if (functionNode.isTemplate()) {
//            if (functionNode instanceof IFunctionNode)
//                subprogramNode = new TemplateSubprogramDataNode((IFunctionNode) functionNode);
//
//        } else {
//            // add nodes corresponding to the parameters of the function
//            List<IVariableNode> passingVariables = functionNode.getArguments();
//            for (INode passingVariable : passingVariables)
//                genInitialTree((VariableNode) passingVariable, subprogramNode);
//
//            // add a node which expressing the return value of the function
//            VariableNode returnVar = Search2.getReturnVarNode(functionNode);
//            if (returnVar != null) {
//                genInitialTree(returnVar, subprogramNode);
//                subprogramNode.setRawType(returnVar.getRawType());
//                subprogramNode.setRealType(returnVar.getRealType());
//            }
//        }
//
//        root.addChild(subprogramNode);
//        subprogramNode.setParent(root);
//
//        setVituralName(subprogramNode);
    }

    public IDataNode generate(IDataNode parent, ICommonFunctionNode functionNode) throws Exception {
        SubprogramNode subprogramNode = new SubprogramNode(functionNode);

        if (functionNode instanceof MacroFunctionNode) {
            subprogramNode = new MacroSubprogramDataNode((MacroFunctionNode) functionNode);

        } else if (functionNode.isTemplate()) {
            if (functionNode instanceof IFunctionNode)
                subprogramNode = new TemplateSubprogramDataNode((IFunctionNode) functionNode);

        } else {
            // add nodes corresponding to the parameters of the function
            List<IVariableNode> passingVariables = functionNode.getArguments();
            for (INode passingVariable : passingVariables)
                genInitialTree((VariableNode) passingVariable, subprogramNode);

            // add a node which expressing the return value of the function
            VariableNode returnVar = Search2.getReturnVarNode(functionNode);
            if (returnVar != null)
                genInitialTree(returnVar, subprogramNode);

//            subprogramNode.setType(VariableTypeUtils.getFullRawType(returnVar));
        }

        parent.addChild(subprogramNode);
        subprogramNode.setParent(parent);

        return subprogramNode;
    }
}
