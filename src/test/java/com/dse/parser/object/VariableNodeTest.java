//package com.dse.parser.object;
//
//import com.dse.config.Paths;
//import com.dse.environment.Environment;
//import com.dse.parser.ProjectParser;
//import com.dse.search.Search;
//import com.dse.search.condition.FunctionNodeCondition;
//import com.dse.logger.AkaLogger;
//import org.junit.Test;
//
//import java.io.File;
//
//import static org.junit.Assert.assertEquals;
//
//public class VariableNodeTest {
//    public final static AkaLogger logger = AkaLogger.get(VariableNodeTest.class);
//
//    @Test
//    public void setAST() {
//        ProjectParser parser = new ProjectParser(new File(Paths.FSOFT.C_ALGORITHM));
//
//        parser.setExpandTreeuptoMethodLevel_enabled(true);
//        parser.setExtendedDependencyGeneration_enabled(true);
//        parser.setCpptoHeaderDependencyGeneration_enabled(true);
//        parser.setGenerateSetterandGetter_enabled(false);
//        parser.setFuncCallDependencyGeneration_enabled(false);
//        parser.setGlobalVarDependencyGeneration_enabled(false);
//        INode root = parser.getRootTree();
//        Environment.getInstance().setProjectNode((ProjectNode) root);
//        FunctionNode sampleNode = (FunctionNode) Search.searchNodes(root, new FunctionNodeCondition(),
//                "int_equal(void*,void*)").get(0);
//        IVariableNode var = sampleNode.getArguments().get(0);
//
//        assertEquals(var.getRawType().equals("void*"), true);
//        assertEquals(var.getReducedRawType().equals("void*"), true);
//        assertEquals(var.getCoreType().equals("void*"), true);
//        assertEquals(var.getFullType().equals("void*"), true);
//        assertEquals(var.getRealType().equals("void*"), true);
//    }
//}