package auto_testcase_generation.testdatagen;

import auto_testcase_generation.cfg.ICFG;
import auto_testcase_generation.cfg.object.ICfgNode;
import com.dse.config.AkaConfig;
import com.dse.coverage.basicpath.BasicPath;
import com.dse.coverage.basicpath.BasicPathsObtain;
import com.dse.guifx_v3.helps.UIController;
import com.dse.guifx_v3.helps.UILogger;
import com.dse.logger.AkaLogger;
import com.dse.parser.object.AbstractFunctionNode;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.IFunctionNode;
import com.dse.parser.object.MacroFunctionNode;
import com.dse.testcase_manager.ITestCase;
import com.dse.testcase_manager.TestCase;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.testcase_manager.TestPrototype;
import com.dse.util.CFGUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BasisPathTestdataGeneration extends SymbolicExecutionTestdataGeneration {

    private final static AkaLogger logger = AkaLogger.get(BasisPathTestdataGeneration.class);

    public BasisPathTestdataGeneration(ICommonFunctionNode fn, String coverageType) {
        super(fn, coverageType);
    }

    @Override
    protected void start(List<TestCase> testCases, ICommonFunctionNode fn, String coverageType, List<TestPrototype> allPrototypes, List<String> generatedTestcases, List<String> analyzedTestpathMd5, boolean showReport) {
        ICFG currentCFG = null;
        try {
            if (fn instanceof MacroFunctionNode) {
                IFunctionNode tmpFunctionNode = ((MacroFunctionNode) fn).getCorrespondingFunctionNode();
                currentCFG = CFGUtils.createCFG(tmpFunctionNode, coverageType);
                currentCFG.setFunctionNode(tmpFunctionNode);
            } else if (fn instanceof AbstractFunctionNode) {
                currentCFG = CFGUtils.createCFG((IFunctionNode) fn, coverageType);
                currentCFG.setFunctionNode((IFunctionNode) fn);
            }
        } catch (Exception e) {
            logger.error("Cant generate CFG for " + fn.getName());
            e.printStackTrace();
        }

        if (!new File(new AkaConfig().fromJson().getZ3Path()).exists()) {
            UIController.showErrorDialog("Can't find the path location of Z3", "Z3 Solver", "Unable to find Z3 path");
        } else if (currentCFG != null) {
            logger.debug("Generate CFG for " + fn.getName() + " successfully");
            Set<BasicPath> basicPaths = new BasicPathsObtain(currentCFG).set();
            logger.debug("Number of basis paths: " + basicPaths.size());

            for (BasicPath basicPath : basicPaths) {
                List<ICfgNode> normalizedPath = normalizePath(basicPath);
                logger.debug("Consider path: " + normalizedPath);
                int code = solve(normalizedPath, fn, generatedTestcases, analyzedTestpathMd5);
                if (code != AUTOGEN_STATUS.EXECUTION.BE_ABLE_TO_EXECUTE_TESTCASE) {
                    logger.error("Cant generate test case using " + basicPath);
                }
            }

            onGenerateSuccess(showReport);
        }
    }

    @Override
    protected String getTestCaseNamePrefix(ICommonFunctionNode fn) {
        return fn.getSimpleName() + ITestCase.POSTFIX_TESTCASE_BY_BASIS_METHOD;
    }
}
