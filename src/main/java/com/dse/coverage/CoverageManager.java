package com.dse.coverage;

import auto_testcase_generation.cfg.ICFG;
import com.dse.environment.object.EnviroCoverageTypeNode;
import com.dse.coverage.highlight.SourcecodeHighlighterForCoverage;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.ISourcecodeFileNode;
import com.dse.parser.object.LambdaFunctionNode;
import com.dse.testcase_manager.TestCase;
import com.dse.logger.AkaLogger;
import com.dse.util.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLambdaExpression;

import java.io.File;
import java.util.List;

public class CoverageManager {
    private final static AkaLogger logger = AkaLogger.get(CoverageManager.class);
    public static final String EMPTY = "";
    public static final float ZERO_COVERAGE = 0;

    /**
     * Export all information about all types of coverage to external files
     *
     * @param testCase
     */
    public static void exportCoveragesOfTestCaseToFile(TestCase testCase, String typeOfCoverage) {
        // get function
        ICommonFunctionNode function = testCase.getRootDataNode().getFunctionNode();
        ISourcecodeFileNode sourcecodeFileNode = Utils.getSourcecodeFile(function);

        /**
         * export the highlighted function to file
         */
        switch (typeOfCoverage) {
            case EnviroCoverageTypeNode.STATEMENT:
            case EnviroCoverageTypeNode.BRANCH:
            case EnviroCoverageTypeNode.BASIS_PATH:
            case EnviroCoverageTypeNode.MCDC: {
                exportCoverageAtSourcecodeFileLevel(sourcecodeFileNode, testCase, typeOfCoverage);
                break;
            }
            case EnviroCoverageTypeNode.STATEMENT_AND_BRANCH: {
                exportCoverageAtSourcecodeFileLevel(sourcecodeFileNode, testCase, EnviroCoverageTypeNode.STATEMENT);
                exportCoverageAtSourcecodeFileLevel(sourcecodeFileNode, testCase, EnviroCoverageTypeNode.BRANCH);
                break;
            }
            case EnviroCoverageTypeNode.STATEMENT_AND_MCDC: {
                exportCoverageAtSourcecodeFileLevel(sourcecodeFileNode, testCase, EnviroCoverageTypeNode.STATEMENT);
                exportCoverageAtSourcecodeFileLevel(sourcecodeFileNode, testCase, EnviroCoverageTypeNode.MCDC);
                break;
            }
            default: {
                logger.debug("Do not support this kind of coverage");
            }
        }
    }

    private static String highlightSourcecode(ISourcecodeFileNode sourcecodeFileNode, TestCase testCase, String typeOfCoverage, List<ICFG> allCFG){
        SourcecodeHighlighterForCoverage sourcecodeHighlighter = new SourcecodeHighlighterForCoverage();
        sourcecodeHighlighter.setSourcecode(sourcecodeFileNode.getAST().getRawSignature());
        sourcecodeHighlighter.setTestpathContent(Utils.readFileContent(testCase.getTestPathFile()));
        sourcecodeHighlighter.setSourcecodePath(sourcecodeFileNode.getAbsolutePath());
        sourcecodeHighlighter.setTypeOfCoverage(typeOfCoverage);
        sourcecodeHighlighter.setAllCFG(allCFG);
        if (testCase.getFunctionNode() instanceof LambdaFunctionNode) {
            IASTFunctionDefinition newAST = ((LambdaFunctionNode) testCase.getFunctionNode()).getAST();
            ICPPASTLambdaExpression oldAST = ((LambdaFunctionNode) testCase.getFunctionNode()).getOriginalAST();
            sourcecodeHighlighter.setDeltaOffset(oldAST.getBody().getFileLocation().getNodeOffset() - newAST.getBody().getFileLocation().getNodeOffset());
        }
        sourcecodeHighlighter.highlight();
        String mcdcCoverageContent = sourcecodeHighlighter.getSimpliedHighlightedSourcecode();
        return mcdcCoverageContent;
    }

    private static void exportCoverageAtSourcecodeFileLevel(ISourcecodeFileNode sourcecodeFileNode, TestCase testCase, String typeOfCoverage) {
        logger.debug("Export coverage of " + testCase.getName() + " to external files");

        // coverage computation
        SourcecodeCoverageComputation computator = new SourcecodeCoverageComputation();
        computator.setTestpathContent(Utils.readFileContent(testCase.getTestPathFile()));
        computator.setConsideredSourcecodeNode(sourcecodeFileNode);
        computator.setCoverage(typeOfCoverage);
        computator.compute();

        // the file containing the highlighted source code file
        String coverageContent = highlightSourcecode(sourcecodeFileNode, testCase, typeOfCoverage, computator.getAllCFG());
        Utils.writeContentToFile(coverageContent, testCase.getHighlightedFunctionPath(typeOfCoverage));

        // the file containing code coverage (%)
        JsonObject json = new JsonObject();
        json.addProperty(typeOfCoverage,
                computator.getNumberOfVisitedInstructions() * 1.0f / computator.getNumberOfInstructions());
        json.addProperty("total", computator.getNumberOfInstructions());
        json.addProperty("visited", computator.getNumberOfVisitedInstructions());
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonString = gson.toJson(json);
        Utils.writeContentToFile(jsonString, testCase.getProgressCoveragePath(typeOfCoverage));
    }

    /**
     *
     * @param testCases test cases of a function
     * @param typeOfCoverage
     * @return
     */
    public static CoverageDataObject getCoverageOfMultiTestCaseAtFunctionLevel(List<TestCase> testCases, String typeOfCoverage) {
        if (testCases.size() == 0)
            return null;

        // get all test paths
        String allTestpaths = "";
        for (TestCase testCase : testCases)
            if (testCase != null && testCase.getTestPathFile() != null && new File(testCase.getTestPathFile()).exists())
                allTestpaths += Utils.readFileContent(testCase.getTestPathFile()) + "\n";

        // coverage
        if (allTestpaths.length() > 0 && testCases != null && testCases.get(0) != null) {
            CoverageDataObject coverageDataObject = new CoverageDataObject();

            ISourcecodeFileNode sourcecodeNode = Utils.getSourcecodeFile(testCases.get(0).getFunctionNode());
            FunctionCoverageComputation covComputation = new FunctionCoverageComputation();
            covComputation.setFunctionNode(testCases.get(0).getFunctionNode());
            covComputation.setCoverage(typeOfCoverage);
            covComputation.setConsideredSourcecodeNode(sourcecodeNode);
            covComputation.setTestpathContent(allTestpaths);
            covComputation.compute();
            coverageDataObject.setProgress(covComputation.getNumberOfVisitedInstructions() * 1.0f / covComputation.getNumberOfInstructions());
            coverageDataObject.setTotal(covComputation.getNumberOfInstructions());
            coverageDataObject.setVisited(covComputation.getNumberOfVisitedInstructions());

            coverageDataObject.setContent("");

            return coverageDataObject;
        } else if (testCases.get(0) != null && testCases.get(0).getFunctionNode() != null){
            // we have compilable test cases, but we can not execute them successfully
            CoverageDataObject coverageDataObject = new CoverageDataObject();

            ISourcecodeFileNode sourcecodeNode = Utils.getSourcecodeFile(testCases.get(0).getFunctionNode());
            FunctionCoverageComputation covComputation = new FunctionCoverageComputation();
            covComputation.setCoverage(typeOfCoverage);
            covComputation.setConsideredSourcecodeNode(sourcecodeNode);
            covComputation.setTestpathContent(allTestpaths);
            covComputation.setFunctionNode(testCases.get(0).getFunctionNode());
            covComputation.compute();

            coverageDataObject.setProgress(Float.NaN);
            coverageDataObject.setTotal(covComputation.getNumberOfInstructions());
            coverageDataObject.setVisited(0);
            return coverageDataObject;
        } else
            return null;
    }

        /**
         * Compute coverage of multiple test cases
         * @param testCases test cases of a function
         * @param typeOfCoverage
         * @return
         */
    public static CoverageDataObject getCoverageOfMultiTestCaseAtSourcecodeFileLevel(List<TestCase> testCases, String typeOfCoverage) {
        if (testCases.size() == 0)
            return null;

        // get all test paths
        String allTestpaths = "";
        for (TestCase testCase : testCases)
            if (testCase != null && testCase.getTestPathFile() != null && new File(testCase.getTestPathFile()).exists())
                allTestpaths += Utils.readFileContent(testCase.getTestPathFile()) + "\n";

        // coverage
        if (allTestpaths.length() > 0 && testCases != null && testCases.get(0) != null) {
            CoverageDataObject coverageDataObject = new CoverageDataObject();

            ISourcecodeFileNode sourcecodeNode = Utils.getSourcecodeFile(testCases.get(0).getFunctionNode());
            SourcecodeCoverageComputation sourcecodeCoverageComputation = new SourcecodeCoverageComputation();
            sourcecodeCoverageComputation.setCoverage(typeOfCoverage);
            sourcecodeCoverageComputation.setConsideredSourcecodeNode(sourcecodeNode);
            sourcecodeCoverageComputation.setTestpathContent(allTestpaths);
            sourcecodeCoverageComputation.compute();
            coverageDataObject.setProgress(sourcecodeCoverageComputation.getNumberOfVisitedInstructions() * 1.0f / sourcecodeCoverageComputation.getNumberOfInstructions());
            coverageDataObject.setTotal(sourcecodeCoverageComputation.getNumberOfInstructions());
            coverageDataObject.setVisited(sourcecodeCoverageComputation.getNumberOfVisitedInstructions());

            // highlight after coverage computation
            SourcecodeHighlighterForCoverage sourcecodeHighlighter = new SourcecodeHighlighterForCoverage();
            sourcecodeHighlighter.setTypeOfCoverage(typeOfCoverage);
            sourcecodeHighlighter.setAllCFG(sourcecodeCoverageComputation.getAllCFG());
            sourcecodeHighlighter.setSourcecode(Utils.readFileContent(sourcecodeNode.getAbsolutePath()));
            sourcecodeHighlighter.setSourcecodePath(sourcecodeNode.getAbsolutePath());
            sourcecodeHighlighter.setTestpathContent(allTestpaths);
            sourcecodeHighlighter.highlight();
            String fullHighlight = sourcecodeHighlighter.getFullHighlightedSourcecode();
            coverageDataObject.setContent(fullHighlight);

            return coverageDataObject;
        } else if (testCases != null && testCases.get(0) != null && testCases.get(0).getFunctionNode() != null){
            // we have compilable test cases, but we can not execute them successfully
            CoverageDataObject coverageDataObject = new CoverageDataObject();

            ISourcecodeFileNode sourcecodeNode = Utils.getSourcecodeFile(testCases.get(0).getFunctionNode());
            SourcecodeCoverageComputation sourcecodeCoverageComputation = new SourcecodeCoverageComputation();
            sourcecodeCoverageComputation.setCoverage(typeOfCoverage);
            sourcecodeCoverageComputation.setConsideredSourcecodeNode(sourcecodeNode);
            sourcecodeCoverageComputation.setTestpathContent(allTestpaths);
            sourcecodeCoverageComputation.compute();

            coverageDataObject.setProgress(Float.NaN);
            coverageDataObject.setTotal(sourcecodeCoverageComputation.getNumberOfInstructions());
            coverageDataObject.setVisited(0);
            return coverageDataObject;
        } else
            return null;
    }

    public static String getDetailProgressCoverage(TestCase testCase, String typeOfCoverage) {
        if (typeOfCoverage.equals(EnviroCoverageTypeNode.STATEMENT_AND_BRANCH) || typeOfCoverage.equals(EnviroCoverageTypeNode.STATEMENT_AND_MCDC))
            // only accept single coverage type
            return null;

        String progressFilePath = testCase.getProgressCoveragePath(typeOfCoverage);

        if (new File(progressFilePath).exists()) {
            String content = Utils.readFileContent(progressFilePath);
            if (!content.isEmpty()) {
                JsonObject jsonObject = JsonParser.parseString(content).getAsJsonObject();
                String totalString = jsonObject.get("total").getAsString();
                String visitedString = jsonObject.get("visited").getAsString();
                if ((totalString != null) && (visitedString != null)) {
                    int total = Integer.parseInt(totalString);
                    int visited = Integer.parseInt(visitedString);
                    return visited + "/" + total;
                } else {
                    logger.debug("Data of progress coverage file is incorrect, path: " + progressFilePath);
                }
            }
        } else {
            //logger.debug("The progress file path doesn't exist: " + progressFilePath);
            return null;
        }

        return null;
    }

    public static float getFunctionProgress(TestCase testCase, String coverageType) {
        ICommonFunctionNode functionNode = testCase.getFunctionNode();
        ISourcecodeFileNode sourcecodeNode = Utils.getSourcecodeFile(functionNode);
        String testpathContent = Utils.readFileContent(testCase.getTestPathFile());

        FunctionCoverageComputation covComputation = new FunctionCoverageComputation();
        covComputation.setFunctionNode(functionNode);
        covComputation.setCoverage(coverageType);
        covComputation.setConsideredSourcecodeNode(sourcecodeNode);
        covComputation.setTestpathContent(testpathContent);
        covComputation.compute();

        return covComputation.getNumberOfVisitedInstructions() * 1.0f / covComputation.getNumberOfInstructions();
    }

    public static float getProgress(TestCase testCase, String typeOfCoverage) {
        String progressFilePath = testCase.getProgressCoveragePath(typeOfCoverage);
        if (progressFilePath != null && new File(progressFilePath).exists()) {
            String content = Utils.readFileContent(progressFilePath);
            if (!content.isEmpty()) {
                JsonObject jsonObject = JsonParser.parseString(content).getAsJsonObject();

                String progressString = null;
                switch (typeOfCoverage) {
                    case EnviroCoverageTypeNode.STATEMENT:
                        progressString = jsonObject.get(EnviroCoverageTypeNode.STATEMENT).getAsString();
                        break;
                    case EnviroCoverageTypeNode.BRANCH:
                        progressString = jsonObject.get(EnviroCoverageTypeNode.BRANCH).getAsString();
                        break;
                    case EnviroCoverageTypeNode.BASIS_PATH:
                        progressString = jsonObject.get(EnviroCoverageTypeNode.BASIS_PATH).getAsString();
                        break;
                    case EnviroCoverageTypeNode.MCDC:
                        progressString = jsonObject.get(EnviroCoverageTypeNode.MCDC).getAsString();
                        break;
                    default:
                        logger.debug("Data of progress file doesn't match supported coverage types");
                }

                if (progressString != null) {
                    return Float.parseFloat(progressString);
                } else {
                    logger.debug("Data of progress coverage file is incorrect, path: " + progressFilePath);
                }
            }
        } else {
            return 0;
        }

        return 0;
    }

    public static String removeRedundantLineBreak(String content) {
        content = content.replace("\r", "\n");
        content = content.replace("\n\n", "\n");
        return content;
    }

}
