package com.dse.testdata.gen.module;

import com.dse.environment.Environment;
import com.dse.logger.AkaLogger;
import com.dse.parser.funcdetail.IFunctionDetailTree;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.INode;
import com.dse.testdata.IDataTree;
import com.dse.testdata.gen.module.subtree.InitialStubTreeGen;
import com.dse.testdata.gen.module.subtree.InitialStubUnitBranchGen;
import com.dse.testdata.gen.module.subtree.InitialUUTBranchGen;
import com.dse.testdata.object.RootDataNode;
import com.dse.util.Utils;

import static com.dse.util.NodeType.STUB;

/**
 * dựng cây Function Detail Tree của một test case (có UUT, STUB, GLOBAL)
 *
 * @author DucAnh
 */
public class DataTreeGeneration extends AbstractDataTreeGeneration {
    final static AkaLogger logger = AkaLogger.get(DataTreeGeneration.class);

    private IFunctionDetailTree functionTree;
    private IDataTree dataTree;

    public DataTreeGeneration() {
    }

    public DataTreeGeneration(IDataTree dataTree, IFunctionDetailTree functionTree)  {
        this.dataTree = dataTree;
        setRoot(dataTree.getRoot());
        setFunctionNode(functionTree.getUUT());
        this.functionTree = functionTree;
    }

    @Override
    public void generateTree() throws Exception {
        root.setFunctionNode(functionNode);
        INode sourceCode = Utils.getSourcecodeFile(functionNode);

        // generate uut branch
        new InitialUUTBranchGen().generateCompleteTree(root, functionTree);

        // generate other sbf
        for (INode sbf : Environment.getInstance().getSBFs()) {
            if (!sourceCode.equals(sbf))
                new InitialStubUnitBranchGen().generate(root, sbf);
        }

        // generate stub branch
        RootDataNode stubRoot = new RootDataNode(STUB);
        root.addChild(stubRoot);
        stubRoot.setParent(root);
        new InitialStubTreeGen().generateCompleteTree(stubRoot, functionTree);
    }

    @Override
    public void setFunctionNode(ICommonFunctionNode functionNode) {
        this.functionNode = functionNode;

        if (dataTree != null)
            dataTree.setFunctionNode(functionNode);
    }
}
