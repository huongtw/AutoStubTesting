package com.dse.testdata.gen.module.subtree;

import com.dse.config.Paths;
import com.dse.environment.Environment;
import com.dse.parser.ProjectParser;
import com.dse.parser.funcdetail.IFunctionDetailTree;
import com.dse.parser.object.*;
import com.dse.parser.object.INode;
import com.dse.search.Search;
import com.dse.search.condition.FunctionNodeCondition;
import com.dse.search.condition.SourcecodeFileNodeCondition;
import com.dse.testdata.object.*;
import com.dse.testdata.object.RootDataNode;
import com.dse.util.NodeType;

import java.io.File;
import java.util.List;

public class InitialStubUnitBranchGen extends AbstractInitialTreeGen {

    public static void main(String[] args) {
        ProjectParser projectParser = new ProjectParser(new File(Paths.JOURNAL_TEST));

        projectParser.setCpptoHeaderDependencyGeneration_enabled(true);
        projectParser.setExpandTreeuptoMethodLevel_enabled(true);
        projectParser.setExtendedDependencyGeneration_enabled(true);

        ProjectNode projectRoot = projectParser.getRootTree();
        Environment.getInstance().setProjectNode(projectRoot);

        INode source = Search.searchNodes(projectRoot, new SourcecodeFileNodeCondition()).get(1);

        RootDataNode stub = new RootDataNode(NodeType.STUB);
        IDataNode unit = new InitialStubUnitBranchGen().generate(stub, source);
        System.out.println();
    }

    @Override
    public void generateCompleteTree(RootDataNode root, IFunctionDetailTree functionTree) throws Exception {

    }

    public IDataNode generate(IDataNode parent, INode physicalNode) {
        if (physicalNode instanceof ISourcecodeFileNode) {
            UnitNode unitNode = new StubUnitNode(physicalNode);

            List<INode> functions = Search.searchNodes(physicalNode, new FunctionNodeCondition());

            for (INode child : functions) {
                if (child instanceof FunctionNode) {
                    FunctionNode functionNode = (FunctionNode) child;
                    SubprogramNode subprogramNode = new SubprogramNode(functionNode);

                    if (functionNode.isTemplate())
                        subprogramNode = new TemplateSubprogramDataNode(functionNode);

                    unitNode.addChild(subprogramNode);
                    subprogramNode.setParent(unitNode);
                }
            }

            parent.addChild(unitNode);
            unitNode.setParent(parent);

            return unitNode;
        }

        return null;
    }
}
