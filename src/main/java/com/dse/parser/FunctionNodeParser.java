package com.dse.parser;

import com.dse.guifx_v3.helps.UILogger;
import com.dse.parser.dependency.FunctionCallDependencyGeneration;
import com.dse.parser.dependency.GlobalVariableDependencyGeneration;
import com.dse.parser.object.*;
import com.dse.search.Search;
import com.dse.search.condition.FunctionNodeCondition;
import com.dse.util.tostring.DependencyTreeDisplayer;
import com.dse.logger.AkaLogger;

import java.io.File;
import java.util.List;

/**
 * Find dependencies related to a function
 */
public class FunctionNodeParser {
    final static UILogger uiLogger = UILogger.getUiLogger();
    final static AkaLogger logger = AkaLogger.get(FunctionNodeParser.class);
    private INode functionNode;
    private INode root;

    private boolean funcCallDependencyGeneration_enabled = false;
    private boolean globalVarDependencyGeneration_enabled = false;

    public static void main(String[] args) {
        // Parse the project
        ProjectParser projectParser = new ProjectParser(new File("/Users/ducanhnguyen/Documents/akautauto/datatest/duc-anh/Sample1/"));
        projectParser.setCpptoHeaderDependencyGeneration_enabled(true);
        projectParser.setExpandTreeuptoMethodLevel_enabled(true);
        projectParser.setParentReconstructor_enabled(true);
        projectParser.setGenerateSetterandGetter_enabled(true);
        projectParser.setExtendedDependencyGeneration_enabled(true);
        projectParser.setFuncCallDependencyGeneration_enabled(false);
        projectParser.setGlobalVarDependencyGeneration_enabled(false);
        IProjectNode projectRoot = projectParser.getRootTree();

        // Get a function node
        List<INode> searchNodes = Search.searchNodes(projectRoot, new FunctionNodeCondition(), "StackLinkedList/setFront(Node*)");
        if (searchNodes.size() == 1) {
            // find dependency
            FunctionNode functionNode = (FunctionNode) searchNodes.get(0);
            FunctionNodeParser functionParser = new FunctionNodeParser();
            functionParser.setRoot(projectRoot);
            functionParser.setFunctionNode(functionNode);
            functionParser.setFuncCallDependencyGeneration_enabled(true);
            functionParser.setGlobalVarDependencyGeneration_enabled(true);
            functionParser.findDependencies();

            // Display tree of project
            System.out.println(new DependencyTreeDisplayer(functionParser.getFunctionNode()).getTreeInString());
            InternalVariableNode firstVar = (InternalVariableNode) functionNode.getChildren().get(0);
            logger.debug("Type of " + firstVar + " : " + firstVar.resolveCoreType().getAbsolutePath());
        }
    }

    public void findDependencies() {
        if (root != null) {
            if (funcCallDependencyGeneration_enabled)
                try {
                    uiLogger.log("calling funcCallDependencyGeneration");
                    logger.debug("calling funcCallDependencyGeneration");
                    new FunctionCallDependencyGeneration().dependencyGeneration(functionNode);
                } catch (Exception ex) {
                    logger.error("Cant find funcCallDependency of " + functionNode.getName(), ex);
                    ex.printStackTrace();
                }

            if (globalVarDependencyGeneration_enabled)
                try {
                    uiLogger.log("calling globalVarDependencyGeneration");
                    logger.debug("calling globalVarDependencyGeneration");
                    new GlobalVariableDependencyGeneration().dependencyGeneration(functionNode);
                } catch (Exception ex) {
                    logger.error("Cant find globalVarDependency of " + functionNode.getName(), ex);
                    ex.printStackTrace();
                }
        }
    }


    public boolean isFuncCallDependencyGeneration_enabled() {
        return funcCallDependencyGeneration_enabled;
    }

    public void setFuncCallDependencyGeneration_enabled(boolean funcCallDependencyGeneration_enabled) {
        this.funcCallDependencyGeneration_enabled = funcCallDependencyGeneration_enabled;
    }

    public boolean isGlobalVarDependencyGeneration_enabled() {
        return globalVarDependencyGeneration_enabled;
    }

    public void setGlobalVarDependencyGeneration_enabled(boolean globalVarDependencyGeneration_enabled) {
        this.globalVarDependencyGeneration_enabled = globalVarDependencyGeneration_enabled;
    }

    public void setRoot(INode root) {
        this.root = root;
    }

    public INode getRoot() {
        return root;
    }

    public void setFunctionNode(INode functionNode) {
        this.functionNode = functionNode;
    }

    public INode getFunctionNode() {
        return functionNode;
    }
}