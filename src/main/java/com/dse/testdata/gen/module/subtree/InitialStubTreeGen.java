package com.dse.testdata.gen.module.subtree;

import com.dse.environment.EnvironmentSearch;
import com.dse.environment.object.EnviroDontStubNode;
import com.dse.environment.object.EnviroSBFNode;
import com.dse.environment.object.EnviroUUTNode;
import com.dse.environment.object.IEnvironmentNode;
import com.dse.environment.Environment;
import com.dse.parser.funcdetail.IFunctionDetailTree;
import com.dse.parser.object.*;
import com.dse.search.Search;
import com.dse.search.Search2;
import com.dse.search.condition.AbstractFunctionNodeCondition;
import com.dse.testdata.object.*;
import com.dse.logger.AkaLogger;
import com.dse.util.PathUtils;

import java.util.ArrayList;
import java.util.List;

public class InitialStubTreeGen extends AbstractInitialTreeGen {
    final static AkaLogger logger = AkaLogger.get(InitialStubTreeGen.class);

    @Override
    public void generateCompleteTree(RootDataNode root, IFunctionDetailTree functionTree) throws Exception {
//        if (root != null && root.getFunctionNode() != null) {
        this.functionNode = root.getFunctionNode();

        for (INode stub : Environment.getInstance().getStubs()) {
            IDataNode stubUnit = new InitialStubUnitBranchGen().generate(root, stub);
        }

        List<INode> stubLibraries = Search.searchNodes(Environment.getInstance().getSystemLibraryRoot(), new AbstractFunctionNodeCondition());
        for (INode stubLibrary : stubLibraries) {
            if (stubLibrary instanceof IFunctionNode && !(stubLibrary.getParent() instanceof AbstractFunctionNode)) {
                SubprogramNode subprogramNode = new SubprogramNode(stubLibrary);

                if (((IFunctionNode) stubLibrary).isTemplate())
                    subprogramNode = new TemplateSubprogramDataNode((IFunctionNode) stubLibrary);

                root.addChild(subprogramNode);
                subprogramNode.setParent(root);
            }
        }
//            for (Dependency d : functionNode.getDependencies()) {
//                if (d instanceof FunctionCallDependency && ((FunctionCallDependency) d).fromNode(functionNode)) {
//                    INode fnCall = d.getEndArrow();
//                    if (functionTree.isStub(fnCall)) {
//                        SubprogramNode subprogram = new SubprogramNode(fnCall);
//
////                        addSubprogram(subprogram);
//
//                        root.addChild(subprogram);
//                        subprogram.setParent(root);
//                    }
//                }
//            }
//        }
    }

    private List<INode> getStubUnit(final List<INode> units) {
        List<INode> stubs = new ArrayList<>(units);

        stubs.removeIf(u -> u instanceof HeaderNode || !isStubUnit(u));

        return stubs;
    }

    private boolean isStubUnit(INode unit) {
        List<IEnvironmentNode> uuts = EnvironmentSearch.searchNode(Environment.getInstance().getEnvironmentRootNode(), new EnviroUUTNode());
        List<IEnvironmentNode> sbfs = EnvironmentSearch.searchNode(Environment.getInstance().getEnvironmentRootNode(), new EnviroSBFNode());

        for (IEnvironmentNode uut : uuts) {
            if (PathUtils.equals(unit.getAbsolutePath(), ((EnviroUUTNode) uut).getName()))
                return false;
        }

        for (IEnvironmentNode sbf : sbfs) {
            if (PathUtils.equals(unit.getAbsolutePath(), ((EnviroSBFNode) sbf).getName()))
                return false;
        }

        return true;
    }

    public void addSubprogram(IterationSubprogramNode node) throws Exception {
        INode fn = node.getFunctionNode();

        if (fn instanceof ICommonFunctionNode) {
            // step 1
            List<IVariableNode> passingVariables = ((ICommonFunctionNode) fn).getArguments();
            for (INode passingVariable : passingVariables)
                new InitialArgTreeGen().genInitialTree((VariableNode) passingVariable, node);

            // step 2
            VariableNode returnVar = Search2.getReturnVarNode((ICommonFunctionNode) fn);
            genInitialTree(returnVar, node);

        } else {
            logger.error("Do not handle " + fn.getClass());
        }
    }
}
