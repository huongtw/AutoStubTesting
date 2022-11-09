package expander;

import com.dse.parser.ProjectParser;
import com.dse.parser.object.FunctionNode;
import com.dse.parser.object.IFunctionNode;
import com.dse.search.Search;
import com.dse.search.condition.FunctionNodeCondition;
import com.dse.testcasescript.TestcaseAnalyzer;
import com.dse.testcasescript.object.ITestcaseNode;
import com.dse.testdata.gen.module.subtree.InitialArgTreeGen;
import com.dse.testdata.object.RootDataNode;
import com.dse.util.Utils;
import org.apache.log4j.Logger;

import java.io.File;

@Deprecated
public class AbstractTestcaseScriptExpanderTest {
    final static Logger logger = Logger.getLogger(AbstractTestcaseScriptExpanderTest.class);

    protected FunctionNode findAFunction(String projectPath, String functionName) {
        ProjectParser parser = new ProjectParser(new File(Utils.normalizePath(projectPath)));
        parser.setCpptoHeaderDependencyGeneration_enabled(true);
        parser.setExpandTreeuptoMethodLevel_enabled(true);
        parser.setParentReconstructor_enabled(true);
        parser.setFuncCallDependencyGeneration_enabled(true);
        parser.setGlobalVarDependencyGeneration_enabled(true);
        parser.setParentReconstructor_enabled(true);
        parser.setExtendedDependencyGeneration_enabled(true);

        FunctionNode function = (FunctionNode) Search
                .searchNodes(parser.getRootTree(), new FunctionNodeCondition(), functionName).get(0);

        return function;
    }

    protected ITestcaseNode getTestScriptTree(String testscriptPath) {
        TestcaseAnalyzer testscriptAnalyzer = new TestcaseAnalyzer();
        ITestcaseNode rootTestscript = testscriptAnalyzer.analyze(new File(Utils.normalizePath(testscriptPath)));
        return rootTestscript;
    }

    protected RootDataNode createDataTree(IFunctionNode function) throws Exception {
        RootDataNode rootDataTree = new RootDataNode();
        rootDataTree.setFunctionNode(function);
        InitialArgTreeGen dataTreeGen = new InitialArgTreeGen();
        dataTreeGen.generateCompleteTree(rootDataTree, null);

        return rootDataTree;
    }
}