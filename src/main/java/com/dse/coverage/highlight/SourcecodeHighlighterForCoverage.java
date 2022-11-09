package com.dse.coverage.highlight;

import auto_testcase_generation.cfg.ICFG;
import auto_testcase_generation.cfg.object.ConditionCfgNode;
import auto_testcase_generation.cfg.object.ICfgNode;
import auto_testcase_generation.cfg.object.NormalCfgNode;
import com.dse.config.Paths;
import com.dse.coverage.SourcecodeCoverageComputation;
import com.dse.environment.object.EnviroCoverageTypeNode;
import com.dse.parser.ProjectParser;
import com.dse.parser.object.ISourcecodeFileNode;
import com.dse.parser.object.LambdaFunctionNode;
import com.dse.search.Search;
import com.dse.search.condition.SourcecodeFileNodeCondition;
import com.dse.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SourcecodeHighlighterForCoverage extends AbstractHighlighterForSourcecodeLevel {

    // all cfg of functions in source code file after updating visited statements and branches
    protected List<ICFG> allCFG;
    protected String typeOfCoverage;

    public static void main(String[] args) {
        ProjectParser parser = new ProjectParser(new File(Paths.JOURNAL_TEST));
        parser.setExpandTreeuptoMethodLevel_enabled(true);
        ISourcecodeFileNode consideredSourcecodeNode = (ISourcecodeFileNode) Search
                .searchNodes(parser.getRootTree(), new SourcecodeFileNodeCondition(), "Utils.cpp").get(0);
        System.out.println(consideredSourcecodeNode.getAbsolutePath());
        //
        String tpFile = "/home/lwinne/NCKH/2021/TestProject/C++/gcem-master/include/aka-working-space/T03/test-paths/sgn_random_0.tp";

        // coverage computation
        SourcecodeCoverageComputation computator = new SourcecodeCoverageComputation();
        computator.setTestpathContent(Utils.readFileContent(tpFile));
        computator.setConsideredSourcecodeNode(consideredSourcecodeNode);
        computator.setCoverage(EnviroCoverageTypeNode.BRANCH);
        computator.compute();
        System.out.println("number of instructions = " + computator.getNumberOfInstructions());
        System.out.println("number of visited instructions = " + computator.getNumberOfVisitedInstructions());

        // highlighter
        SourcecodeHighlighterForCoverage highlighter = new SourcecodeHighlighterForCoverage();
        highlighter.setSourcecode(consideredSourcecodeNode.getAST().getRawSignature());
        highlighter.setTestpathContent(Utils.readFileContent(tpFile));
        highlighter.setSourcecodePath("/Users/ducanhnguyen/Documents/akautauto/datatest/duc-anh/Algorithm/Utils.cpp");
        highlighter.setAllCFG(computator.getAllCFG());
        highlighter.setTypeOfCoverage(computator.getCoverage());
        highlighter.highlight();
        Utils.writeContentToFile(highlighter.getFullHighlightedSourcecode(), "/Users/ducanhnguyen/Desktop/x.html");
    }

    @Override
    public void highlight() {
        if (sourcecode == null || sourcecode.length() == 0 || testpathContent == null ||
                testpathContent.length() == 0 || sourcecodePath == null || !(new File(sourcecodePath).exists()))
            return;

        List<HighlightedOffset> offsets = getVisitedInstructionsInASourcecodeFile(getAllCFG());
        offsets = arrangeByStartingOffset(offsets);

        for (HighlightedOffset offset : offsets) {
            int start = offset.getStartOffset();
            int end = offset.getEndOffset();
            String pre = sourcecode.substring(0, start);
            String after = sourcecode.substring(end);
            String middle = sourcecode.substring(start, end);

            if (offset instanceof HighlightedOffsetForNormalStatement) {
                middle = "HIGHLIGHT_NORMAL_STATEMENT_BEGIN" + middle + "HIGHLIGHT_NORMAL_STATEMENT_END";
            } else if (offset instanceof HighlightedOffsetForBranch) {
                switch (typeOfCoverage) {
                    case EnviroCoverageTypeNode.STATEMENT: {
                        middle = "HIGHLIGHT_CONDITIONAL_STATEMENT_BEGIN" + middle + "HIGHLIGHT_CONDITIONAL_STATEMENT_END";
                        break;
                    }

                    case EnviroCoverageTypeNode.BASIS_PATH:
                    case EnviroCoverageTypeNode.BRANCH:
                    case EnviroCoverageTypeNode.MCDC: {
                        if (((HighlightedOffsetForBranch) offset).isVisitedFalse() && ((HighlightedOffsetForBranch) offset).isVisitedTrue())
                            middle = "TRUE_MARKER" + "FALSE_MARKER" + "HIGHLIGHT_CONDITIONAL_STATEMENT_BEGIN" + middle + "HIGHLIGHT_CONDITIONAL_STATEMENT_END";
                        else if (((HighlightedOffsetForBranch) offset).isVisitedFalse())
                            middle = "FALSE_MARKER" + "HIGHLIGHT_CONDITIONAL_STATEMENT_BEGIN" + middle + "HIGHLIGHT_CONDITIONAL_STATEMENT_END";
                        else if (((HighlightedOffsetForBranch) offset).isVisitedTrue())
                            middle = "TRUE_MARKER" + "HIGHLIGHT_CONDITIONAL_STATEMENT_BEGIN" + middle + "HIGHLIGHT_CONDITIONAL_STATEMENT_END";
                        break;
                    }
                }

            }

            sourcecode = pre + middle + after;
        }

        // credit: http://ijotted.blogspot.com/2012/05/which-characters-should-be-escaped.html
        sourcecode = sourcecode.replace("&", "&amp;")
                .replace("'", "&#39;")
                .replace("\"", "&quot;")
                .replace(">", "&gt;")
                .replace("<", "&lt;");

        sourcecode = sourcecode.replace("HIGHLIGHT_NORMAL_STATEMENT_BEGIN", highlightSignalStartForNormalStatement)
                .replace("HIGHLIGHT_NORMAL_STATEMENT_END", highlightSignalEnd)
                .replace("HIGHLIGHT_CONDITIONAL_STATEMENT_BEGIN", highlightSignalStartForConditionalStatement)
                .replace("HIGHLIGHT_CONDITIONAL_STATEMENT_END", highlightSignalEnd);

        sourcecode = sourcecode.replace("TRUE_MARKER", trueMarker)
                .replace("FALSE_MARKER", falseMarker);

        sourcecode = addLineNumber(sourcecode);

        fullHighlightedSourcecode = addPre(sourcecode);

        simpliedHighlightedSourcecode = removeRedundantLines(sourcecode);
        simpliedHighlightedSourcecode = addPre(simpliedHighlightedSourcecode);
    }

    protected List<HighlightedOffset> getVisitedInstructionsInASourcecodeFile(List<ICFG> allCFG) {
        List<HighlightedOffset> offsets = new ArrayList<>();
        for (ICFG cfg : allCFG) {
            int deltaOffset = 0;
            if (cfg.getFunctionNode() instanceof LambdaFunctionNode)
                deltaOffset = this.deltaOffset;
            for (ICfgNode cfgNode : cfg.getAllNodes()) {
                if (cfgNode instanceof ConditionCfgNode) {
                    if (cfgNode.isVisited()) {
                        HighlightedOffsetForBranch offsetForBranch = new HighlightedOffsetForBranch();

                        offsetForBranch.setStartOffset(cfgNode.getAstLocation().getNodeOffset() + deltaOffset);
                        offsetForBranch.setEndOffset(cfgNode.getAstLocation().getNodeLength() + cfgNode.getAstLocation().getNodeOffset() + deltaOffset);
                        offsets.add(offsetForBranch);

                        if (((ConditionCfgNode) cfgNode).isVisitedFalseBranch() && ((ConditionCfgNode) cfgNode).isVisitedTrueBranch()) {
                            offsetForBranch.setVisitedTrue(true);
                            offsetForBranch.setVisitedFalse(true);
                        } else if (((ConditionCfgNode) cfgNode).isVisitedTrueBranch()) {
                            offsetForBranch.setVisitedTrue(true);
                        } else if (((ConditionCfgNode) cfgNode).isVisitedFalseBranch()) {
                            offsetForBranch.setVisitedFalse(true);
                        }
                    }
                } else if (cfgNode instanceof NormalCfgNode && cfgNode.isVisited()) {
                    HighlightedOffsetForNormalStatement offsetForNormalStatement = new HighlightedOffsetForNormalStatement();
                    offsetForNormalStatement.setStartOffset(cfgNode.getAstLocation().getNodeOffset() + deltaOffset);
                    offsetForNormalStatement.setEndOffset(cfgNode.getAstLocation().getNodeLength() + cfgNode.getAstLocation().getNodeOffset() + deltaOffset);
                    offsets.add(offsetForNormalStatement);
                }
            }
        }
        
        return offsets;
    }

    public List<ICFG> getAllCFG() {
        return allCFG;
    }

    public void setAllCFG(List<ICFG> allCFG) {
        this.allCFG = allCFG;
    }

    @Override
    public void setTestpathContent(String testpathContent) {
        super.setTestpathContent(testpathContent);
    }

    @Override
    public String getTestpathContent() {
        return super.getTestpathContent();
    }

    public void setTypeOfCoverage(String typeOfCoverage) {
        this.typeOfCoverage = typeOfCoverage;
    }

    public String getTypeOfCoverage() {
        return typeOfCoverage;
    }
}
