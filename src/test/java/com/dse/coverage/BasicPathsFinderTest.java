package com.dse.coverage;

import auto_testcase_generation.cfg.ICFG;
import auto_testcase_generation.cfg.object.ConditionCfgNode;
import com.dse.coverage.basicpath.BasicPath;
import com.dse.coverage.basicpath.BasicPathsObtain;
import com.dse.environment.Environment;
import com.dse.environment.object.EnviroCoverageTypeNode;
import com.dse.environment.object.EnvironmentRootNode;
import com.dse.parser.ProjectParser;
import com.dse.parser.object.IFunctionNode;
import com.dse.parser.object.ProjectNode;
import com.dse.search.Search;
import com.dse.search.condition.FunctionNodeCondition;
import com.dse.util.CFGUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.Set;

public class BasicPathsFinderTest {

    protected void baseTest(String projectDir, String functionName) {
        ProjectParser projectParser = new ProjectParser(new File(projectDir));

        projectParser.setCpptoHeaderDependencyGeneration_enabled(true);
        projectParser.setExpandTreeuptoMethodLevel_enabled(true);
        projectParser.setExtendedDependencyGeneration_enabled(true);
        projectParser.setFuncCallDependencyGeneration_enabled(true);

        ProjectNode projectRoot = projectParser.getRootTree();

        Environment.getInstance().setProjectNode(projectRoot);
        EnvironmentRootNode rootNode = Environment.getInstance().getEnvironmentRootNode();
        EnviroCoverageTypeNode typeNode = new EnviroCoverageTypeNode();
        typeNode.setCoverageType(EnviroCoverageTypeNode.BASIS_PATH);
        typeNode.setParent(rootNode);
        rootNode.getChildren().add(typeNode);

        List<IFunctionNode> functionNodes = Search.searchNodes(projectRoot, new FunctionNodeCondition(), functionName);
        IFunctionNode functionNode = functionNodes.get(0);

        try {

            ICFG cfg = CFGUtils.createCFG(functionNode, EnviroCoverageTypeNode.BASIS_PATH);
            // số basic path = số node condition + 1
            long condNodes = cfg.getAllNodes().stream()
                    .filter(n -> n instanceof ConditionCfgNode)
                    .count();
            Set<BasicPath> basicPaths = new BasicPathsObtain(cfg).set();
            Assert.assertEquals(condNodes + 1, basicPaths.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void baseTest(String projectDir) {
        ProjectParser projectParser = new ProjectParser(new File(projectDir));

        projectParser.setCpptoHeaderDependencyGeneration_enabled(true);
        projectParser.setExpandTreeuptoMethodLevel_enabled(true);
        projectParser.setExtendedDependencyGeneration_enabled(true);
        projectParser.setFuncCallDependencyGeneration_enabled(true);

        ProjectNode projectRoot = projectParser.getRootTree();

        Environment.getInstance().setProjectNode(projectRoot);
        EnvironmentRootNode rootNode = Environment.getInstance().getEnvironmentRootNode();
        EnviroCoverageTypeNode typeNode = new EnviroCoverageTypeNode();
        typeNode.setCoverageType(EnviroCoverageTypeNode.BASIS_PATH);
        typeNode.setParent(rootNode);
        rootNode.getChildren().add(typeNode);

        List<IFunctionNode> functionNodes = Search.searchNodes(projectRoot, new FunctionNodeCondition());

        for (IFunctionNode functionNode : functionNodes) {
            try {
                ICFG cfg = CFGUtils.createCFG(functionNode, EnviroCoverageTypeNode.BASIS_PATH);
                // số basic path = số node condition + 1
                long condNodes = cfg.getAllNodes().stream()
                        .filter(n -> n instanceof ConditionCfgNode)
                        .count();
                Set<BasicPath> basicPaths = new BasicPathsObtain(cfg).set();
                Assert.assertEquals(functionNode.getName() + " is incorrect", condNodes + 1, basicPaths.size());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void test1() {
        baseTest("datatest/basicpath/", "main()");
    }

    @Test
    public void test2() {
        baseTest("datatest/basicpath/", "fn_delete_element(int,int,int[])");
    }

    @Test
    public void testCppAlgorithm() {
        baseTest("datatest/duc-anh/Algorithm");
    }

}