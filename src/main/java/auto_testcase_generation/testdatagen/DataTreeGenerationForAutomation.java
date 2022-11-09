package auto_testcase_generation.testdatagen;

import com.dse.environment.Environment;
import com.dse.parser.funcdetail.FunctionDetailTree;
import com.dse.parser.funcdetail.IFunctionDetailTree;
import com.dse.parser.object.INode;
import com.dse.testdata.IDataTree;
import com.dse.testdata.InputCellHandler;
import com.dse.testdata.gen.module.AbstractDataTreeGeneration;
import com.dse.testdata.gen.module.subtree.*;
import com.dse.testdata.object.*;
import com.dse.testdata.object.RootDataNode;
import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;

import java.util.Map;

import static com.dse.util.NodeType.*;

@Deprecated
public class DataTreeGenerationForAutomation extends AbstractDataTreeGeneration {
    private IDataTree dataTree;
    private IFunctionDetailTree functionTree;

    public DataTreeGenerationForAutomation(IDataTree dataTree, Map<String, String> staticSolution)  {
        this.dataTree = dataTree;
        this.values = staticSolution;
        setRoot(dataTree.getRoot());
        functionNode = dataTree.getFunctionNode();
        functionTree = new FunctionDetailTree(functionNode);
    }

    @Override
    public void generateTree() throws Exception {
        root.setFunctionNode(functionNode);
        INode sourceCode = Utils.getSourcecodeFile(functionNode);


        // generate uut branch
        new InitialUUTBranchGen().generateCompleteTree(root, functionTree);
        recursiveExpandUutBranch(root);

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

    private void recursiveExpandUutBranch(DataNode node) throws Exception {
        // STEP 1: set & get virtual name of current node
        node.setVirtualName();
        String key = node.getVituralName();

        // STEP 2: get raw value from static solutions
        String value = values.get(key);

        // STEP 3: commit edit with value
        if (value != null) {
            if (node instanceof ArrayDataNode || node instanceof PointerDataNode) {
                // Ex: key = "p", value="sizeof(p)=1"
                // get the size of array
                value = value.substring(value.indexOf(SpecialCharacter.EQUAL) + 1);
            } else if (node instanceof ClassDataNode && !(node instanceof SubClassDataNode)) {
                // Ex: key = "sv", value="Student(int,int)"
                // get name of the constructor
                if (value.contains("("))
                    value = value.substring(0, value.indexOf('('));
            }

            if (node instanceof ValueDataNode)
                new InputCellHandler().commitEdit((ValueDataNode) node, value);
        }

        // STEP 4: repeat with its children
        for (IDataNode child : node.getChildren())
            recursiveExpandUutBranch((DataNode) child);
    }
}
