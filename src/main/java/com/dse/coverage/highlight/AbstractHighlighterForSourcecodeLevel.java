package com.dse.coverage.highlight;

import auto_testcase_generation.instrument.IFunctionInstrumentationGeneration;
import com.dse.util.SpecialCharacter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractHighlighterForSourcecodeLevel extends AbstractHighlighter {
    public static final int EXPANSION = 8; // the number of lines displayed except from the highlighted lines

    protected String testpathContent; // the file containing a test path after executing a single test case/compound test case
    protected String sourcecodePath; // the file containing source code, used to removed redundant lines in test path
    protected String sourcecode; // the content of source code
    protected String fullHighlightedSourcecode; // all lines of source code
    protected String simpliedHighlightedSourcecode; // remove redundant lines, i.e., not highlighted lines
    protected int deltaOffset = 0;

    protected String removeRedundantLines(String sourcecode) {
        String[] lines = sourcecode.split("\n");
        List<Integer> addedLines = new ArrayList<>();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.contains(highlightSignalStartForNormalStatement)) {
                /*
                 * Add some lines of code above the highlighted line
                 */
                if (i >= EXPANSION) {
                    for (int j = i - EXPANSION; j < i; j++) {
                        if (!addedLines.contains(j)) {
                            addedLines.add(j);
                        }
                    }
                }

                if (!addedLines.contains(i)) {
                    addedLines.add(i);
                }

                /*
                 * Add some lines of code below the highlighted line
                 */
                if (i + EXPANSION <= lines.length - 1) {
                    for (int j = i + 1; j < i + EXPANSION; j++) {
                        if (!addedLines.contains(j)) {
                            addedLines.add(j);
                        }
                    }
                }
            }
        }

        StringBuilder shortenContent = new StringBuilder();

        if (!addedLines.isEmpty()) {
            // sort added line list
            addedLines = addedLines.stream()
                    .sorted()
                    .collect(Collectors.toList());

            int prev = -1;

            for (int cur : addedLines) {
                if (prev + 1 != cur) {
                    shortenContent.append(SHORTEN_CONTENT);
                }

                shortenContent.append(lines[cur]).append("\n");

                prev = cur;

                // check last line
                if (addedLines.indexOf(cur) == addedLines.size() - 1 && cur < lines.length - 1) {
                    shortenContent.append(SHORTEN_CONTENT);
                }
            }
        }

        return shortenContent.toString();
    }

    private static final String SHORTEN_CONTENT = "...\n...\n...\n...\n";

    public static boolean isFullCondition(String line) {
        return line.toLowerCase()
                .contains((IFunctionInstrumentationGeneration.IS_FULL_CONDITION + IFunctionInstrumentationGeneration.DELIMITER_BETWEEN_PROPERTY_AND_VALUE + "TRUE").toLowerCase());
    }

    public static boolean isSubCondition(String line) {
        return line.toLowerCase()
                .contains((IFunctionInstrumentationGeneration.IS_SUB_CONDITION + IFunctionInstrumentationGeneration.DELIMITER_BETWEEN_PROPERTY_AND_VALUE + "TRUE").toLowerCase());
    }

    public static boolean isNormalStatement(String line) {
        return line.toLowerCase()
                .contains((IFunctionInstrumentationGeneration.IS_NORMAL_STATEMENT + IFunctionInstrumentationGeneration.DELIMITER_BETWEEN_PROPERTY_AND_VALUE + "TRUE").toLowerCase());
    }

    /**
     * Check whether a line in test path should be analyzed or not
     *
     * @param tokens         of line
     * @param sourcecodePath
     * @return
     */
    protected boolean shouldBeAnalyzed(String[] tokens, String sourcecodePath) {
        for (String token : tokens) {
            String key = token.split(IFunctionInstrumentationGeneration.DELIMITER_BETWEEN_PROPERTY_AND_VALUE)[0];
            String value = token.split(IFunctionInstrumentationGeneration.DELIMITER_BETWEEN_PROPERTY_AND_VALUE)[1];
            if (key.equals(IFunctionInstrumentationGeneration.FUNCTION_ADDRESS)
                    && value.startsWith(sourcecodePath))
                return true;
        }
        return false;
    }

    public List<HighlightedOffset> arrangeByStartingOffset(List<HighlightedOffset> offsets) {
        for (int i = 0; i < offsets.size() - 1; i++)
            for (int j = i + 1; j < offsets.size(); j++)
                if (offsets.get(i).getStartOffset() < offsets.get(j).getStartOffset()) {
                    HighlightedOffset tmp = offsets.get(i);
                    offsets.remove(i);
                    offsets.add(i, offsets.get(j - 1));
                    offsets.remove(j);
                    offsets.add(j, tmp);
                }
        return offsets;
    }

    public String getTestpathContent() {
        return testpathContent;
    }

    public void setTestpathContent(String testpathContent) {
        this.testpathContent = testpathContent;
    }

    public String getSourcecode() {
        return sourcecode;
    }

    public void setSourcecode(String sourcecode) {
        this.sourcecode = sourcecode;
    }

    public String getFullHighlightedSourcecode() {
        return fullHighlightedSourcecode;
    }

    public void setFullHighlightedSourcecode(String fullHighlightedSourcecode) {
        this.fullHighlightedSourcecode = fullHighlightedSourcecode;
    }

    public String getSimpliedHighlightedSourcecode() {
        return simpliedHighlightedSourcecode;
    }

    public void setSimpliedHighlightedSourcecode(String simpliedHighlightedSourcecode) {
        this.simpliedHighlightedSourcecode = simpliedHighlightedSourcecode;
    }

    public void setSourcecodePath(String sourcecodePath) {
        this.sourcecodePath = sourcecodePath;
    }

    public String getSourcecodePath() {
        return sourcecodePath;
    }

    public void setDeltaOffset(int deltaOffset) {
        this.deltaOffset = deltaOffset;
    }
}
