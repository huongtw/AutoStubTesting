package com.dse.coverage.highlight;

import auto_testcase_generation.instrument.IFunctionInstrumentationGeneration;
import com.dse.config.Paths;
import com.dse.coverage.SourcecodeCoverageComputation;
import com.dse.environment.object.EnviroCoverageTypeNode;
import com.dse.parser.ProjectParser;
import com.dse.parser.object.ISourcecodeFileNode;
import com.dse.search.Search;
import com.dse.search.condition.SourcecodeFileNodeCondition;
import com.dse.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Given a list of offsets, we highlight these offset in source code
 */
public class SourcecodeHighlighterFromOffset extends AbstractHighlighterForSourcecodeLevel {

    public static void main(String[] args) {
        ProjectParser parser = new ProjectParser(new File(Paths.JOURNAL_TEST));
        parser.setExpandTreeuptoMethodLevel_enabled(true);
        ISourcecodeFileNode consideredSourcecodeNode = (ISourcecodeFileNode) Search
                .searchNodes(parser.getRootTree(), new SourcecodeFileNodeCondition(), "Utils.cpp").get(0);
        System.out.println(consideredSourcecodeNode.getAbsolutePath());
        //
        String tpFile = "/Users/ducanhnguyen/Documents/akautauto/local/working-directory/cov/test-paths/Tritype.365917.tp";

        // coverage computation
        SourcecodeCoverageComputation computator = new SourcecodeCoverageComputation();
        computator.setTestpathContent(Utils.readFileContent(tpFile));
        computator.setConsideredSourcecodeNode(consideredSourcecodeNode);
        computator.setCoverage(EnviroCoverageTypeNode.STATEMENT);
        computator.compute();
        System.out.println("number of instructions = " + computator.getNumberOfInstructions());
        System.out.println("number of visited instructions = " + computator.getNumberOfVisitedInstructions());

        // highlighter
        SourcecodeHighlighterFromOffset highlighter = new SourcecodeHighlighterFromOffset();
        highlighter.setSourcecode(consideredSourcecodeNode.getAST().getRawSignature());
        highlighter.setTestpathContent(Utils.readFileContent(tpFile));
        highlighter.setSourcecodePath("/Users/ducanhnguyen/Documents/akautauto/datatest/duc-anh/Algorithm/Utils.cpp");
        highlighter.highlight();
        Utils.writeContentToFile(highlighter.getFullHighlightedSourcecode(), "/Users/ducanhnguyen/Desktop/x.html");
    }

    @Override
    public void highlight() {
        if (sourcecode == null || sourcecode.length() == 0 || testpathContent == null ||
             testpathContent.length() == 0|| sourcecodePath == null || !(new File(sourcecodePath).exists()))
            return;

        List<HighlightedOffset> offsets = getPossibleOffsets(testpathContent, sourcecodePath);
        offsets = arrangeByStartingOffset(offsets);

        for (HighlightedOffset offset : offsets) {
            int start = offset.getStartOffset();
            int end = offset.getEndOffset();
            String pre = sourcecode.substring(0, start);
            String after = sourcecode.substring(end);

            String middle = sourcecode.substring(start, end);
            middle = "KJHFFUHRURH" + middle + "JLFHLEIUEJFN";
            sourcecode = pre + middle + after;
        }
        // credit: http://ijotted.blogspot.com/2012/05/which-characters-should-be-escaped.html
        sourcecode = sourcecode.
                replace("&", "&amp;").replace("'", "&#39;")
                .replace("\"", "&quot;").
                        replace(">", "&gt;").replace("<", "&lt;");

        sourcecode = sourcecode.replace("KJHFFUHRURH", highlightSignalStartForNormalStatement).replace("JLFHLEIUEJFN", highlightSignalEnd);
        sourcecode = addLineNumber(sourcecode);

        fullHighlightedSourcecode = addPre(sourcecode);

        simpliedHighlightedSourcecode = removeRedundantLines(sourcecode);
        simpliedHighlightedSourcecode = addPre(simpliedHighlightedSourcecode);
    }

    protected List<HighlightedOffset> getPossibleOffsets(String testpathContent, String sourcecodePath) {
        List<HighlightedOffset> offsets = new ArrayList<>();
        String[] lines = testpathContent.split("\n");
        for (String line : lines) {
            if (!line.contains(IFunctionInstrumentationGeneration.DELIMITER_BETWEEN_PROPERTIES))
                continue;

            String[] tokens = line.split(IFunctionInstrumentationGeneration.DELIMITER_BETWEEN_PROPERTIES);

            if (!shouldBeAnalyzed(tokens, sourcecodePath))
                continue;

            HighlightedOffset offset = null;
            switch (coverage){
                case EnviroCoverageTypeNode.BRANCH:{
                    if (isSubCondition(line)){
                        // ignore
                    } else if (isFullCondition(line)) {
                        offset = new HighlightedOffsetForBranch();
                    } else
                        offset = new HighlightedOffsetForNormalStatement();
                    break;
                }
                case EnviroCoverageTypeNode.MCDC:{
                    if (isSubCondition(line)){
                        offset = new HighlightedOffsetForBranch();
                    } else if (isFullCondition(line)) {
                        // ignore
                    } else
                        offset = new HighlightedOffsetForNormalStatement();
                    break;
                }
                case EnviroCoverageTypeNode.STATEMENT:{
                    if (isSubCondition(line)){
                        // ignore
                    } else if (isFullCondition(line)) {
                        // ignore
                    } else
                        offset = new HighlightedOffsetForNormalStatement();
                    break;
                }
            }

            if (offset == null)
                continue;

            for (String token : tokens) {
                String key = token.split(IFunctionInstrumentationGeneration.DELIMITER_BETWEEN_PROPERTY_AND_VALUE)[0];
                String value = token.split(IFunctionInstrumentationGeneration.DELIMITER_BETWEEN_PROPERTY_AND_VALUE)[1];

                if (key.equals(IFunctionInstrumentationGeneration.START_OFFSET_IN_SOURCE_CODE_FILE)) {
                    offset.setStartOffset(Utils.toInt(value));
                } else if (key.equals(IFunctionInstrumentationGeneration.END_OFFSET_IN_SOURCE_CODE_FILE)) {
                    offset.setEndOffset(Utils.toInt(value));
                }
            }
            if (!offsets.contains(offset))
                offsets.add(offset);
        }
        return offsets;
    }

}
