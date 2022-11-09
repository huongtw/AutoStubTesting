package com.dse.coverage;

import auto_testcase_generation.cfg.ICFG;
import auto_testcase_generation.instrument.IFunctionInstrumentationGeneration;
import auto_testcase_generation.testdata.object.TestpathString_Marker;
import auto_testcase_generation.testdatagen.coverage.CFGUpdaterv2;
import auto_testcase_generation.testdatagen.coverage.ICoverageComputation;
import com.dse.coverage.basicpath.BasicPath;
import com.dse.environment.object.EnviroCoverageTypeNode;
import com.dse.coverage.highlight.AbstractHighlighterForSourcecodeLevel;
import com.dse.parser.object.AbstractFunctionNode;
import com.dse.parser.object.IFunctionNode;
import com.dse.parser.object.INode;
import com.dse.parser.object.MacroFunctionNode;
import com.dse.search.Search;
import com.dse.search.condition.AbstractFunctionNodeCondition;
import com.dse.search.condition.MacroFunctionNodeCondition;
import com.dse.logger.AkaLogger;
import com.dse.util.CFGUtils;
import com.dse.util.PathUtils;
import com.dse.util.TestPathUtils;
import com.dse.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractCoverageComputation implements ICoverageComputation {

    private final static AkaLogger logger = AkaLogger.get(AbstractCoverageComputation.class);

    protected String testpathContent; // may visit may source code files
    protected INode consideredSourcecodeNode; // the source code file which we need to compute coverage at file level
    protected String coverage;
    protected int numberOfVisitedInstructions; // depend on coverage, instruction is statement, condition, or sub-condition
    protected int numberOfInstructions;
    protected List<ICFG> allCFG = new ArrayList<>();

    protected abstract Map<String, TestpathsOfAFunction> removeRedundantTestpath(Map<String, TestpathsOfAFunction> affectedFunctions);

    protected int getNumberOfInstructions(INode consideredSourcecodeNode, String coverage) {
        int nInstructions = 0;
        switch (coverage) {
            case EnviroCoverageTypeNode.STATEMENT: {
                nInstructions = getNumberofStatements(consideredSourcecodeNode);
                break;
            }
            case EnviroCoverageTypeNode.BRANCH: {
                nInstructions = getNumberofBranches(consideredSourcecodeNode);
                break;
            }

            case EnviroCoverageTypeNode.MCDC: {
                nInstructions = getNumberofMcdcs(consideredSourcecodeNode);
                break;
            }

            case EnviroCoverageTypeNode.BASIS_PATH: {
                nInstructions = getNumberOfBasisPath(consideredSourcecodeNode);
                break;
            }
        }
        return nInstructions;
    }

    /**
     * @return the number of visited instructions (>=0), return -1 if there is error happening
     */
    protected int getNumberOfVisitedInstructions(Map<String, TestpathsOfAFunction> affectedFunctions,
                                               String coverage, INode consideredSourcecodeNode,
                                               List<ICFG> allCFG) {
        final int ERROR = -1;
        int nVisitedInstructions = 0;
        for (String functionPath : affectedFunctions.keySet())
            // only consider functions in a specified source code file
            if (functionPath.contains(consideredSourcecodeNode.getAbsolutePath())) {
                logger.debug("Analyzing " + PathUtils.toRelative(functionPath));
                // Find the function node
                TestpathsOfAFunction testpathsOfAFunction = affectedFunctions.get(functionPath);

                List<INode> functionNodes = Search.searchNodes(consideredSourcecodeNode, new AbstractFunctionNodeCondition(), PathUtils.toAbsolute(functionPath));
                if (functionNodes.size() == 0)
                    functionNodes = Search.searchNodes(consideredSourcecodeNode, new MacroFunctionNodeCondition(), PathUtils.toAbsolute(functionPath));

                if (functionNodes.size() != 1)
                    return ERROR;

                // generate cfg of the function
                INode functionNode = functionNodes.get(0);
                try {
                    ICFG cfg = null;

                    if (functionNode instanceof MacroFunctionNode) {
                        IFunctionNode tmpFunctionNode = ((MacroFunctionNode) functionNode).getCorrespondingFunctionNode();
                        cfg = CFGUtils.createCFG(tmpFunctionNode, coverage);
                        if(cfg != null) {
                            allCFG.add(cfg);
                            cfg.setFunctionNode(tmpFunctionNode);
                        }
                    } else if (functionNode instanceof AbstractFunctionNode) {
                        cfg = CFGUtils.createCFG((IFunctionNode) functionNode, coverage);
                        if(cfg != null) {
                            allCFG.add(cfg);
                            cfg.setFunctionNode((IFunctionNode) functionNode);
                        }
                    }

                    if (cfg == null)
                        return ERROR;

                    // compute coverage of a cfg
                    TestpathString_Marker testpath = new TestpathString_Marker();

                    if (testpathsOfAFunction != null && testpathsOfAFunction.getTestpathsInArray() != null)
                        testpath.setEncodedTestpath(testpathsOfAFunction.getTestpathsInArray());
                    else
                        testpath.setEncodedTestpath(new String[]{});

                    CFGUpdaterv2 cfgUpdaterv2 = new CFGUpdaterv2(testpath, cfg);
                    cfgUpdaterv2.updateVisitedNodes();
                    switch (coverage) {
                        case EnviroCoverageTypeNode.STATEMENT: {
                            nVisitedInstructions += cfg.getVisitedStatements().size();
                            break;
                        }
                        case EnviroCoverageTypeNode.MCDC:
                        case EnviroCoverageTypeNode.BRANCH: {
                            nVisitedInstructions += cfg.getVisitedBranches().size();
                            break;
                        }

                        case EnviroCoverageTypeNode.BASIS_PATH: {
                            List<BasicPath> visitedPaths = cfg.getVisitedBasisPaths();
                            nVisitedInstructions += visitedPaths
                                    .size();
                            break;
                        }
                    }
                    logger.debug("Num of visited instructions = " + nVisitedInstructions);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        return nVisitedInstructions;
    }

    /**
     * @return a hash map, where key is the path of function, value is a list.
     * Each item in list is corresponding to a test path.
     */
    protected Map<String, TestpathsOfAFunction> categoryTestpathByFunctionPath(String[] testpaths, String coverage) {
        Map<String, TestpathsOfAFunction> tps = new HashMap<>();

        TestCaseBegin testCaseBegin = null;

        for (String testpath : testpaths) {
            String functionAddress = getValue(testpath, IFunctionInstrumentationGeneration.FUNCTION_ADDRESS);

            if (testpath.startsWith(TestPathUtils.BEGIN_TAG)) {
                testCaseBegin = new TestCaseBegin();
                testCaseBegin.testpath = testpath;
            }

            if (functionAddress != null && functionAddress.length() > 0) {
                functionAddress = PathUtils.toAbsolute(functionAddress);
                functionAddress = Utils.normalizePath(functionAddress);

                switch (coverage) {
                    case EnviroCoverageTypeNode.BRANCH:
                    case EnviroCoverageTypeNode.BASIS_PATH:
                    case EnviroCoverageTypeNode.STATEMENT: {
                        if (AbstractHighlighterForSourcecodeLevel.isSubCondition(testpath))
                            // ignore the test path which goes through subcondition
                            continue;
                        else
                            break;
                    }

                    case EnviroCoverageTypeNode.MCDC: {
                        //TODO: offset
                        if (AbstractHighlighterForSourcecodeLevel.isFullCondition(testpath))
                            // ignore the test path which goes through full condition
                            continue;
                        else
                            break;
                    }
                }

                if (!tps.containsKey(functionAddress)) {
                    tps.put(functionAddress, new TestpathsOfAFunction());
                }

                Offset offset = new Offset();
                offset.startingOffsetInFunction = Utils.toInt(getValue(testpath, IFunctionInstrumentationGeneration.START_OFFSET_IN_FUNCTION));
                offset.endOffsetInFunction = Utils.toInt(getValue(testpath, IFunctionInstrumentationGeneration.END_OFFSET_IN_FUNCTION));
                offset.startingOffsetInSourcecodeFile = Utils.toInt(getValue(testpath, IFunctionInstrumentationGeneration.START_OFFSET_IN_SOURCE_CODE_FILE));
                offset.endOffsetInSourcecodeFile = Utils.toInt(getValue(testpath, IFunctionInstrumentationGeneration.END_OFFSET_IN_SOURCE_CODE_FILE));
                offset.testpath = testpath;

                if (testCaseBegin != null) {
                    if (!tps.get(functionAddress).testpaths.contains(testCaseBegin)) {
                        tps.get(functionAddress).testpaths.add(testCaseBegin);
                    }
                }

                tps.get(functionAddress).testpaths.add(offset);
            }
        }
        return tps;
    }

    protected abstract int getNumberOfBasisPath(INode consideredSourcecodeNode);

    public static String getValue(String line, String property) {
        if (line.contains(IFunctionInstrumentationGeneration.DELIMITER_BETWEEN_PROPERTIES)) {
            String[] tokens = line.split(IFunctionInstrumentationGeneration.DELIMITER_BETWEEN_PROPERTIES);
            for (String token : tokens)
                if (token.split(IFunctionInstrumentationGeneration.DELIMITER_BETWEEN_PROPERTY_AND_VALUE)[0].equals(property))
                    return token.split(IFunctionInstrumentationGeneration.DELIMITER_BETWEEN_PROPERTY_AND_VALUE)[1];
        }
        return null;
    }

    protected abstract int getNumberofBranches(INode consideredSourcecodeNode);

    protected abstract int getNumberofStatements(INode consideredSourcecodeNode);

    protected abstract int getNumberofMcdcs(INode consideredSourcecodeNode);

    public void setTestpathContent(String testpathContent) {
        this.testpathContent = testpathContent;
    }

    public String getTestpathContent() {
        return testpathContent;
    }

    public void setConsideredSourcecodeNode(INode consideredSourcecodeNode) {
        this.consideredSourcecodeNode = consideredSourcecodeNode;
    }

    public INode getConsideredSourcecodeNode() {
        return consideredSourcecodeNode;
    }

    public String getCoverage() {
        return coverage;
    }

    public void setCoverage(String coverage) {
        this.coverage = coverage;
    }

    public int getNumberOfInstructions() {
        return numberOfInstructions;
    }

    public void setNumberOfInstructions(int numberOfInstructions) {
        this.numberOfInstructions = numberOfInstructions;
    }

    public int getNumberOfVisitedInstructions() {
        return numberOfVisitedInstructions;
    }

    public void setNumberOfVisitedInstructions(int numberOfVisitedInstructions) {
        this.numberOfVisitedInstructions = numberOfVisitedInstructions;
    }

    public List<ICFG> getAllCFG() {
        return allCFG;
    }

    public void setAllCFG(List<ICFG> allCFG) {
        this.allCFG = allCFG;
    }

    static class TestpathsOfAFunction {
        List<TestPathItem> testpaths = new ArrayList<>();

        @Override
        public String toString() {
            return testpaths.toString();
        }

        String[] getTestpathsInArray() {
            String[] tpInArray = new String[testpaths.size()];
            int count = 0;
            for (TestPathItem offset : testpaths) {
                tpInArray[count] = offset.testpath;
                count++;
            }
            return tpInArray;
        }
    }

    static abstract class TestPathItem {
        String testpath;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TestPathItem that = (TestPathItem) o;

            return testpath != null ? testpath.equals(that.testpath) : that.testpath == null;
        }
    }

    static abstract class TestCaseItem extends TestPathItem {
    }

    static class TestCaseBegin extends TestCaseItem {
        @Override
        public String toString() {
            return testpath.substring(TestPathUtils.BEGIN_TAG.length());
        }
    }

    static class Offset extends TestPathItem {
        int startingOffsetInSourcecodeFile;
        int endOffsetInSourcecodeFile;
        int startingOffsetInFunction;
        int endOffsetInFunction;

        @Override
        public String toString() {
            return "offset in source code file = " + startingOffsetInSourcecodeFile + ":" + endOffsetInSourcecodeFile + ", offset in function = " + startingOffsetInFunction + ":" + endOffsetInFunction + "\n";
        }
    }
}
