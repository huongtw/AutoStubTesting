package com.dse.parser.funcdetail;

import com.dse.config.Paths;
import com.dse.parser.ProjectParser;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.IFunctionNode;
import com.dse.parser.object.INode;
import com.dse.parser.object.RootNode;
import com.dse.search.Search;
import com.dse.search.condition.FunctionNodeCondition;
import com.dse.util.NodeType;
import com.dse.util.tostring.DependencyTreeDisplayer;
import com.dse.logger.AkaLogger;

import java.io.File;

/**
 * Example:
 * <p>
 * Input: SimpleStackLinklist.cpp/disp(Node*)
 * <p>
 * Output:
 * <par>
 * --[ROOT]
 * -------[GLOBAL]
 * -------[UUT]
 * --------------[FunctionNode] real name: disp(Node*)
 * -------[DONT STUB]
 * -------[STUB]
 * </par>
 */
public class FunctionDetailTree implements IFunctionDetailTree {
    final static AkaLogger logger = AkaLogger.get(FunctionDetailTree.class);
    /**
     * Function node ma Function Detail Tree bieu dien
     */
    private ICommonFunctionNode functionNode;

    /**
     * Root cua Function Detail Tree
     */
    private RootNode root = new RootNode(NodeType.ROOT);

    /**
     * Ket qua bieu dien cay duoi dang string
     */
    private String treeInString;

    public static void main(String[] args) throws Exception {
        // parse the project
        ProjectParser parser = new ProjectParser(new File(Paths.SAMPLE01));
        parser.setGlobalVarDependencyGeneration_enabled(false);
        parser.setFuncCallDependencyGeneration_enabled(false);
        parser.setExtendedDependencyGeneration_enabled(true);
        parser.setGenerateSetterandGetter_enabled(false);
        parser.setParentReconstructor_enabled(true);
        parser.setExpandTreeuptoMethodLevel_enabled(true);
        parser.setCpptoHeaderDependencyGeneration_enabled(true);
        INode root = parser.getRootTree();

        // export the tree to string
        DependencyTreeDisplayer displayer = new DependencyTreeDisplayer(root);
        logger.debug(displayer.getTreeInString());

        // Find a function
        IFunctionNode function = (IFunctionNode) Search
                .searchNodes(root, new FunctionNodeCondition(), "SimpleStackLinklist.cpp/disp(Node*)").get(0);

        // Display tree
        FunctionDetailTree tree = new FunctionDetailTree(function);
        logger.debug("Tree of function:\n" + tree);
    }

    public FunctionDetailTree(ICommonFunctionNode functionNode) {
        this.functionNode = functionNode;
        new FuncDetailTreeGeneration(root, functionNode);
    }

    public void stubAll() throws Exception {
        for (INode fn : getSubTreeRoot(NodeType.DONT_STUB).getElements()) {
            if (fn instanceof IFunctionNode)
                stub(fn);
        }
    }

    public void dontStubAll() throws Exception {
        for (INode fn : getSubTreeRoot(NodeType.STUB).getElements()) {
            if (fn instanceof IFunctionNode)
                dontStub(fn);
        }
    }

    @Override
    public void stub(INode fn) throws Exception {
        if (!isStub(fn)) {
            getSubTreeRoot(NodeType.DONT_STUB).removeElement(fn);
            getSubTreeRoot(NodeType.STUB).addElement(fn);
        } else {
            //throw new Exception("Ham khong co trong danh sach function call");
            logger.error("The function " + fn.getAbsolutePath() + " does not in the list of function calls");
        }
    }

    @Override
    public void dontStub(INode fn) throws Exception {
        if (isStub(fn)) {
            getSubTreeRoot(NodeType.STUB).removeElement(fn);
            getSubTreeRoot(NodeType.DONT_STUB).addElement(fn);
        } else {
            // throw new Exception("Ham khong co trong danh sach function call");
            logger.error("The function " + fn.getAbsolutePath() + " does not in the list of function calls");
        }
    }

    @Override
    public boolean isStub(INode fn) throws Exception {
        if (getSubTreeRoot(NodeType.STUB).getElements().contains(fn))
            return true;
        else if (getSubTreeRoot(NodeType.DONT_STUB).getElements().contains(fn))
            return false;
        else {
            logger.error("The function " + fn.getAbsolutePath() + " does not in the list of function calls");
            return false;
//            throw new Exception("Ham khong co trong danh sach function call");
        }
    }

    public NodeType getTypeOf(IFunctionNode fn){
        if (fn == getUUT()){
            return NodeType.UUT;
        }
        try {
            if (isStub(fn)){
                return NodeType.STUB;
            } else return NodeType.DONT_STUB;
        } catch (Exception e) {
             e.printStackTrace();
        }
        return null;
    }

    @Override
    public RootNode getSubTreeRoot(NodeType type) {
        for (INode node : root.getElements())
            if (node instanceof RootNode && ((RootNode) node).getType() == type)
                return (RootNode) node;

        return null;
    }

    @Override
    public ICommonFunctionNode getUUT() {
        return functionNode;
    }

    private void displayTree(INode n, int level) {
        if (n != null) {
            if (n instanceof RootNode) {
                treeInString += genTab(level) + "[" + n.getName() + "]" + "\n";
                if (((RootNode) n).getElements() != null) {
                    for (INode element : ((RootNode) n).getElements()) {
                        displayTree(element, ++level);
                        level--;
                    }
                }
            } else {
                treeInString += genTab(level) + "[" +
                        n.getClass().getSimpleName() + "] real name: " + n.getName() + "\n";
            }
        }
    }

    private String genTab(int level) {
        StringBuilder tab = new StringBuilder();
        for (int i = 0; i < level; i++)
            tab.append("     ");
        return tab.toString();
    }

    @Override
    public String toString() {
        treeInString = "";
        displayTree(root, 0);
        return treeInString;
    }

    public ICommonFunctionNode getFunctionNode() {
        return functionNode;
    }

    public void setFunctionNode(IFunctionNode functionNode) {
        this.functionNode = functionNode;
    }

    public RootNode getRoot() {
        return root;
    }

    public void setRoot(RootNode root) {
        this.root = root;
    }
}
