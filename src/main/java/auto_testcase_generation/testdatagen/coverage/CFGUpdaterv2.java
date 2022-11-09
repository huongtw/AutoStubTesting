package auto_testcase_generation.testdatagen.coverage;

import auto_testcase_generation.cfg.CFGGenerationforBranchvsStatementvsBasispathCoverage;
import auto_testcase_generation.cfg.ICFG;
import auto_testcase_generation.cfg.object.*;
import auto_testcase_generation.cfg.testpath.FullTestpath;
import auto_testcase_generation.cfg.testpath.ITestpathInCFG;
import auto_testcase_generation.instrument.FunctionInstrumentationForStatementvsBranch_Markerv2;
import auto_testcase_generation.testdata.object.StatementInTestpath_Mark;
import auto_testcase_generation.testdata.object.TestpathString_Marker;
import com.dse.config.Paths;
import com.dse.coverage.basicpath.BasicPath;
import com.dse.parser.ProjectParser;
import com.dse.parser.object.IFunctionNode;
import com.dse.search.Search;
import com.dse.search.condition.FunctionNodeCondition;
import com.dse.util.PathUtils;
import com.dse.util.TestPathUtils;
import com.dse.util.Utils;
import com.dse.logger.AkaLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Update the visited state of CFG.
 */
public class CFGUpdaterv2 implements ICFGUpdater {
    final static AkaLogger logger = AkaLogger.get(CFGUpdaterv2.class);
    private TestpathString_Marker testpath;
    private ICFG cfg;

    public CFGUpdaterv2(TestpathString_Marker testpath, ICFG cfg) {
        this.testpath = testpath;
        this.cfg = cfg;
    }

    public static void main(String[] args) throws Exception {
        // find function
        ProjectParser parser = new ProjectParser(new File(Paths.JOURNAL_TEST));
        parser.setExpandTreeuptoMethodLevel_enabled(true);
        IFunctionNode function = (IFunctionNode) Search
                .searchNodes(parser.getRootTree(), new FunctionNodeCondition(), "Tritype(int,int,int)").get(0);
        logger.debug(function.getAST().getRawSignature());

        // generate cfg
        CFGGenerationforBranchvsStatementvsBasispathCoverage cfgGen = new CFGGenerationforBranchvsStatementvsBasispathCoverage(function);
        ICFG cfg = cfgGen.generateCFG();
        cfg.setIdforAllNodes();
        for (ICfgNode node : cfg.getAllNodes())
            node.setVisit(false);

        // get test paths from file
        TestpathString_Marker testpath = new TestpathString_Marker();
        String content = Utils.readFileContent(new File("/Users/ducanhnguyen/Documents/akautauto/local/working-directory/3/testpaths/Tritype@4898456.tp"));
        content = content.replace("\r", "\n");
        content = content.replace("\n\n", "\n");
        String[] lines = content.split("\n");
        testpath.setEncodedTestpath(lines);

        // compute coverage
        CFGUpdaterv2 updaterv2 = new CFGUpdaterv2(testpath, cfg);
        cfg.setFunctionNode(function);
        logger.debug(cfg);
        updaterv2.updateVisitedNodes();
        logger.debug("Visited statements = " + cfg.getVisitedStatements());
        logger.debug("Statement coverage = " + cfg.computeStatementCoverage());

        List<BranchInCFG> visitedBranches = cfg.getVisitedBranches();
        logger.debug("Visited branches =  " + visitedBranches);
        logger.debug("Branch coverage = " + cfg.computeBranchCoverage());


    }

    @Override
    public void updateVisitedNodes() {
        // find all nodes corresponding to statements or conditions
        Set<String> visitedOffsets = getAllVisitedNodesByItsOffset(testpath);

        // update the visited state of nodes
        updateStateOfVisitedNodeByItsOffset(cfg, visitedOffsets);

        // create a chain of visited statement in order
        updateVisitedStateOfBranches(testpath, cfg);

        updateVisitedStateOfBasicPath(testpath, cfg);
    }

    private void updateVisitedStateOfBasicPath(TestpathString_Marker testpath, ICFG cfg) {
        String[] testpathItems = testpath.getEncodedTestpath();

        List<String> visitedOffsets = new ArrayList<>();

        for (int i = 0; i < testpathItems.length; i++) {
            String item = testpathItems[i];
            if (item.contains(FunctionInstrumentationForStatementvsBranch_Markerv2.START_OFFSET_IN_FUNCTION)) {
                StatementInTestpath_Mark line = TestpathString_Marker.lineExtractor(item);
                if (line.getPropertyByName(FunctionInstrumentationForStatementvsBranch_Markerv2.START_OFFSET_IN_FUNCTION) != null &&
                        line.getPropertyByName(FunctionInstrumentationForStatementvsBranch_Markerv2.START_OFFSET_IN_FUNCTION) != null) {
                    // is statement or condition
                    visitedOffsets.add(line.getPropertyByName(FunctionInstrumentationForStatementvsBranch_Markerv2.START_OFFSET_IN_FUNCTION).getValue());
                }
            }
            if (i != 0 && (item.startsWith(TestPathUtils.BEGIN_TAG) || i == testpathItems.length - 1)) {
                markVisitedBasicPath(visitedOffsets, cfg);
                visitedOffsets = new ArrayList<>();
            }
        }
    }

    private void markVisitedBasicPath(List<String> visitedOffsets, ICFG cfg) {
        List<ICfgNode> visitedNodes = new ArrayList<>();

        for (ICfgNode node : cfg.getAllNodes()) {
            if (node.getAstLocation() != null) {
                String offsetInCFG = node.getAstLocation().getNodeOffset() - cfg.getFunctionNode().getAST().getFileLocation().getNodeOffset() + "";
                if (visitedOffsets.contains(offsetInCFG)) {
                    visitedNodes.add(node);
                }
            }
        }

        for (BasicPath basicPath : cfg.getAllBasicPaths()) {
            List<ICfgNode> clone = new ArrayList<>(visitedNodes);

            boolean isVisited = true;

            for (ICfgNode node : basicPath) {
                if (node.getAstLocation() != null) {
                    if (!visitedNodes.contains(node)) {
                        isVisited = false;
                        break;
                    } else {
                        clone.remove(node);
                    }
                }
            }

            if (isVisited && clone.isEmpty())
                basicPath.setVisited(true);
        }
    }

    private void updateVisitedStateOfBranches(TestpathString_Marker testpath, ICFG cfg) {
        String visitedStatementInStr = " ";
        for (String offset : testpath.getStandardTestpathByProperty(FunctionInstrumentationForStatementvsBranch_Markerv2.START_OFFSET_IN_FUNCTION))
            visitedStatementInStr += offset + " ";
//        logger.debug("visitedStatementInStr = " + visitedStatementInStr);

        int offsetOfFunctionInSourcecodeFile = cfg.getFunctionNode().getAST().getFileLocation().getNodeOffset();
        for (ICfgNode visitedNode : cfg.getVisitedStatements()) {
            if (visitedNode instanceof ConditionCfgNode) { // condition
                // analyze the true branch
                ICfgNode trueBranchNode = visitedNode.getTrueNode();
                boolean isUpdatedAsVisited = updateTheStateOfBranches(trueBranchNode, visitedNode, offsetOfFunctionInSourcecodeFile, visitedStatementInStr);
                if (isUpdatedAsVisited)
                    ((ConditionCfgNode) visitedNode).setVisitedTrueBranch(true);

                // analyze the false branch
                ICfgNode falseBranchNode = visitedNode.getFalseNode();
                isUpdatedAsVisited = updateTheStateOfBranches(falseBranchNode, visitedNode, offsetOfFunctionInSourcecodeFile, visitedStatementInStr);
                if (isUpdatedAsVisited)
                    ((ConditionCfgNode) visitedNode).setVisitedFalseBranch(true);
            }
        }
    }

    private Set<String> getAllVisitedNodesByItsOffset(TestpathString_Marker testpath) {
        Set<String> visitedOffsets = new HashSet<>();
        List<StatementInTestpath_Mark> lines = testpath.getStandardTestpathSetByAllProperties();
        for (StatementInTestpath_Mark line : lines) {
            if (line.getPropertyByName(FunctionInstrumentationForStatementvsBranch_Markerv2.START_OFFSET_IN_FUNCTION) != null &&
                    line.getPropertyByName(FunctionInstrumentationForStatementvsBranch_Markerv2.START_OFFSET_IN_FUNCTION) != null) {
                // is statement or condition
                String path = line.getPropertyByName(FunctionInstrumentationForStatementvsBranch_Markerv2.FUNCTION_ADDRESS).getValue();
                path = PathUtils.toAbsolute(path);
                path = Utils.normalizePath(path);
                String targetPath = Utils.normalizePath(cfg.getFunctionNode().getAbsolutePath());
                if (path.equals(targetPath)) {
                    visitedOffsets.add(line.getPropertyByName(FunctionInstrumentationForStatementvsBranch_Markerv2.START_OFFSET_IN_FUNCTION).getValue());
                }
            }
        }
        return visitedOffsets;
    }

    private void updateStateOfVisitedNodeByItsOffset(ICFG cfg, Set<String> visitedOffsets) {
        List<ICfgNode> nodes = cfg.getAllNodes();
        for (ICfgNode node : nodes)
            if (node.getAstLocation() != null) {
                String offsetInCFG = node.getAstLocation().getNodeOffset() - cfg.getFunctionNode().getAST().getFileLocation().getNodeOffset() + "";
                if (visitedOffsets.contains(offsetInCFG)) {
                    node.setVisit(true);
                }
            }
    }

    private boolean updateTheStateOfBranches(ICfgNode branchNode, ICfgNode visitedNode,
                                             int offsetOfFunctionInSourcecodeFile, String visitedStatementInStr) {
        if (visitedNode instanceof ConditionCfgNode && branchNode != null) {
            boolean isUpdated = false;
            List<ICfgNode> flagNodes = new ArrayList<>();

            String flag = visitedNode.getTrueNode().equals(branchNode) ? "+" : "-";
            if (visitedNode instanceof ConditionForCfgNode)
                flag = "";

            int offsetVisitedNode = visitedNode.getAstLocation().getNodeOffset();

            // ignore nodes corresponding to flag
            while (branchNode != null && branchNode.isSpecialCfgNode()) {
                if (branchNode instanceof ScopeCfgNode && ((ScopeCfgNode) branchNode).isCloseScope()) {
                    break;
                }
                flagNodes.add(branchNode);
                branchNode = branchNode.getTrueNode();  // for these type of nodes, true node and false node are the same
            }

            if (branchNode == null || (branchNode instanceof ScopeCfgNode && ((ScopeCfgNode) branchNode).isCloseScope())) {
                // the current node point to the end node of cfg
                for (ICfgNode flagNode : flagNodes)
                    flagNode.setVisit(true);

                String comparison = " " + (offsetVisitedNode - offsetOfFunctionInSourcecodeFile) + " "
                        /*+ flag + (offsetVisitedNode - offsetOfFunctionInSourcecodeFile)*/ + flag + " ";
                comparison = comparison.replaceAll("\\s+", " ");

                return visitedStatementInStr.contains(comparison);
            } else {
                // make a comparison to check whether the branch node is visited
                String comparison = " " + (offsetVisitedNode - offsetOfFunctionInSourcecodeFile) + " " +
                        (branchNode.getAstLocation().getNodeOffset() - offsetOfFunctionInSourcecodeFile) + " ";
                //if (cond){stm;}
                //stm;
                String comparison2 = " " + (offsetVisitedNode - offsetOfFunctionInSourcecodeFile) + " " +
                       /* "-" + (offsetVisitedNode - offsetOfFunctionInSourcecodeFile) +*/ "-" + " " +
                        (branchNode.getAstLocation().getNodeOffset() - offsetOfFunctionInSourcecodeFile) + " ";


                if (visitedStatementInStr.contains(comparison) || visitedStatementInStr.contains(comparison2)) {
                    isUpdated = true;
                    branchNode.setVisit(true);
//                    logger.debug("updated the branch " + comparison);
                    // update the flag nodes between the condition node and the normal nodes in its checked branch
                    for (ICfgNode flagNode : flagNodes)
                        flagNode.setVisit(true);
                    return isUpdated;
                } else
                    return false;
            }
        } else
            return false;
    }

    @Override
    public String[] getTestpath() {
        return new String[0];
    }

    @Override
    public void setTestpath(String[] testpath) {
        // nothing to do
    }

    @Override
    public ICFG getCfg() {
        return cfg;
    }

    @Override
    public void setCfg(ICFG cfg) {
        this.cfg = cfg;
    }

    @Override
    public ITestpathInCFG getUpdatedCFGNodes() {
        ITestpathInCFG updatedCFGNodes = new FullTestpath();
        for (ICfgNode node : getCfg().getVisitedStatements()) {
            updatedCFGNodes.getAllCfgNodes().add(node);
        }
        return updatedCFGNodes;
    }

    @Override
    public void setUpdatedCFGNodes(ITestpathInCFG updatedCFGNodes) {
        // nothing to do
    }

    @Override
    public void unrollChangesOfTheLatestPath() {
        // nothing to do
    }
}
