package com.dse.coverage.highlight;

import auto_testcase_generation.cfg.CFGGenerationforBranchvsStatementvsBasispathCoverage;
import auto_testcase_generation.cfg.ICFG;
import auto_testcase_generation.cfg.object.ICfgNode;
import com.dse.config.Paths;
import com.dse.parser.ProjectParser;
import com.dse.parser.object.IFunctionNode;
import com.dse.parser.object.INode;
import com.dse.search.Search;
import com.dse.search.condition.FunctionNodeCondition;
import com.dse.logger.AkaLogger;
import com.dse.util.Utils;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class FunctionHighlighter extends AbstractHighlighter{
    private final static AkaLogger logger = AkaLogger.get(FunctionHighlighter.class);

    public static void main(String[] args) throws Exception {
        // Generate CFG
        ProjectParser parser = new ProjectParser(new File(Paths.JOURNAL_TEST));
        parser.setExpandTreeuptoMethodLevel_enabled(true);
        INode function = Search
                .searchNodes(parser.getRootTree(), new FunctionNodeCondition(), "Tritype(int,int,int)").get(0);
        System.out.println(((IFunctionNode) function).getAST().getRawSignature());

        CFGGenerationforBranchvsStatementvsBasispathCoverage cfgGen = new CFGGenerationforBranchvsStatementvsBasispathCoverage((IFunctionNode) function);
        ICFG cfg = cfgGen.generateCFG();
        cfg.setFunctionNode((IFunctionNode) function);
        cfg.setIdforAllNodes();
        System.out.println(cfg.toString());

        // Set visited statements
        cfg.getAllNodes().get(2).setVisit(true);
        cfg.getAllNodes().get(6).setVisit(true);
        cfg.getAllNodes().get(5).setVisit(true);

        //
        Utils.writeContentToFile(new FunctionHighlighter().highlightVisitedBlock(cfg), "/Users/ducanhnguyen/Desktop/x.html");
    }

    @Override
    public void highlight() {
        //
    }

    public String highlightMCDC(ICFG cfg) {
        // The way we highlight function in MCDC coverage
        String highlightedFunction = highlightVisitedBlock(cfg);

        // add some information here
        return highlightedFunction;
    }

    public String highlightBranches(ICFG cfg) {
        // The way we highlight function in branch coverage and in statement coverage are the same
        String highlightedFunction = highlightVisitedBlock(cfg);

        // add some information here
        return highlightedFunction;
    }

    public String highlightVisitedBlock(ICFG cfg) {
        String originFunction = cfg.getFunctionNode().getAST().getRawSignature();

        // create a map
        Map<Object, ICfgNode> visitedNodesMap = new TreeMap<>(Collections.reverseOrder());
        int startingOffsetOfFunctionInSourcecode = cfg.getFunctionNode().getAST().getFileLocation().getNodeOffset();
        List<ICfgNode> nodes = cfg.getVisitedStatements();
        for (ICfgNode node : nodes) {
            Integer key = new Integer(node.getAstLocation().getNodeOffset() - startingOffsetOfFunctionInSourcecode);
            visitedNodesMap.put(key, node);
        }

        // sort the map
        for (Object key : visitedNodesMap.keySet()) {
            int start = ((Integer) key).intValue();
            int end = start + visitedNodesMap.get(key).getAstLocation().getNodeLength();

            String pre = originFunction.substring(0, start);
            String after = originFunction.substring(end);

            String middle = originFunction.substring(start, end);
            middle = "<b style=\"background-color:#7bd243;\">" + middle + "</b>";
            originFunction = pre + middle + after;
        }

        // display line numbers
        originFunction = addLineNumber(originFunction);
        originFunction = addPre(originFunction);
        return originFunction;
    }

    public String highlightStatements(ICFG cfg) {
        // The way we highlight function in MCDC coverage
        String highlightedFunction = highlightVisitedBlock(cfg);

        // add some information here
        return highlightedFunction;
    }

}
