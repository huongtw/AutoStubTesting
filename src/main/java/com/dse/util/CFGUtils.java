package com.dse.util;

import auto_testcase_generation.cfg.CFGGenerationforBranchvsStatementvsBasispathCoverage;
import auto_testcase_generation.cfg.CFGGenerationforSubConditionCoverage;
import auto_testcase_generation.cfg.ICFG;
import auto_testcase_generation.instrument.IFunctionInstrumentationGeneration;
import auto_testcase_generation.testdata.object.TestpathString_Marker;
import auto_testcase_generation.testdatagen.coverage.CFGUpdaterv2;
import com.dse.coverage.AbstractCoverageComputation;
import com.dse.coverage.highlight.AbstractHighlighterForSourcecodeLevel;
import com.dse.environment.Environment;
import com.dse.environment.object.EnviroCoverageTypeNode;
import com.dse.environment.object.EnvironmentRootNode;
import com.dse.parser.ProjectParser;
import com.dse.parser.SourcecodeFileParser;
import com.dse.parser.object.*;
import com.dse.search.Search;
import com.dse.search.condition.AbstractFunctionNodeCondition;
import com.dse.search.condition.FunctionNodeCondition;
import com.dse.testcase_manager.TestCase;
import com.dse.testcase_manager.minimize.TestCaseMinimizer;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.dse.coverage.AbstractCoverageComputation.getValue;
import static com.dse.coverage.CoverageManager.removeRedundantLineBreak;

public class CFGUtils {

    public static List<ICFG> getAndMarkAllCFG(TestCase testCase) {
        File testPath = new File(testCase.getTestPathFile());

        ICommonFunctionNode sut = testCase.getRootDataNode().getFunctionNode();
        INode sourceNode = Utils.getSourcecodeFile(sut);

        List<ICFG> cfgList = new ArrayList<>();

        Search.searchNodes(sourceNode, new AbstractFunctionNodeCondition())
                .stream()
                .map(f -> (IFunctionNode) f)
                .forEach(function -> {
                    try {
                        ICFG cfg = getAndMarkCFG(function, testPath);
                        if (cfg != null)
                            cfgList.add(cfg);
                    } catch (Exception e) {
                        TestCaseMinimizer.logger.error(e.getMessage());
                    }
                });

        return cfgList;
    }

    public static ICFG getAndMarkCFG(TestCase testCase) {
        ICommonFunctionNode sut = testCase.getRootDataNode().getFunctionNode();

        if (!(sut instanceof IFunctionNode))
            return null;

        IFunctionNode function = (IFunctionNode) sut;

        File testPath = new File(testCase.getTestPathFile());

        if (testPath.exists()) {
            try {
                return getAndMarkCFG(function, testPath);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return null;
    }

    private static ICFG getAndMarkCFG(IFunctionNode function, File testPath) throws Exception {
        String coverage = Environment.getInstance().getTypeofCoverage();

        String content = Utils.readFileContent(testPath);
        String[] lines = removeRedundantLineBreak(content).split("\\R");
        List<String> filterLines = new ArrayList<>();

        for (String testpath : lines) {
            String functionAddress = getValue(testpath, IFunctionInstrumentationGeneration.FUNCTION_ADDRESS);

            if (functionAddress != null && !functionAddress.isEmpty()) {
                functionAddress = PathUtils.toAbsolute(functionAddress);

                if (!PathUtils.equals(functionAddress, function.getAbsolutePath()))
                    continue;

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

                filterLines.add(testpath);
            }
        }


        ICFG cfg = createCFG(function, coverage);

        // Update the cfg
        TestpathString_Marker marker = new TestpathString_Marker();
        marker.setEncodedTestpath(filterLines.toArray(new String[0]));

        CFGUpdaterv2 updater = new CFGUpdaterv2(marker, cfg);
        updater.updateVisitedNodes();

        return cfg;
    }

    public static void main(String[] args) throws Exception {
        ProjectParser projectParser = new ProjectParser(new File("datatest/duc-anh/Algorithm"));

        projectParser.setCpptoHeaderDependencyGeneration_enabled(true);
        projectParser.setExpandTreeuptoMethodLevel_enabled(true);
        projectParser.setExtendedDependencyGeneration_enabled(true);
        projectParser.setFuncCallDependencyGeneration_enabled(true);

        ProjectNode projectRoot = projectParser.getRootTree();

        Environment.getInstance().setProjectNode(projectRoot);
        EnvironmentRootNode rootNode = Environment.getInstance().getEnvironmentRootNode();
        EnviroCoverageTypeNode typeNode = new EnviroCoverageTypeNode();
        typeNode.setCoverageType(EnviroCoverageTypeNode.STATEMENT);
        typeNode.setParent(rootNode);
        rootNode.getChildren().add(typeNode);

        List<IFunctionNode> functionNodes = Search.searchNodes(projectRoot, new FunctionNodeCondition(), "check_armstrong(long long)");
        IFunctionNode functionNode = functionNodes.get(0);

//        ICFG cfg = createExpandCFG(functionNode, EnviroCoverageTypeNode.STATEMENT);

        System.out.println();
    }

//    public static ICFG createExpandCFG(IFunctionNode functionNode, String coverageType) throws Exception {
//        ICFG cfg = createCFG(functionNode, coverageType);
//
//        for (ICfgNode cfgNode : cfg.getAllNodes()) {
//            if (cfgNode instanceof NormalCfgNode) {
//                IASTNode astNode = ((NormalCfgNode) cfgNode).getAst();
//
//                CalledCFGGeneration calledCFGGeneration = new CalledCFGGeneration(functionNode, coverageType);
//
//                astNode.accept(calledCFGGeneration);
//
//                ICFG calledCFG = calledCFGGeneration.getCalledCFG();
//            }
//        }
//
//        return cfg;
//    }

    /**
     * Create CFG of a function.
     *
     * This function may call to a macro function or not.
     *
     * In case of a call to macro functions, CDT might parse the macro call inside the function, which
     * might lead to the incorrect CFG.
     * For example:
     * #define MACRO_CALL(a) if (a>0) return 1; else return 0;
     * int test(){return MACRO_CALL(a);}
     * Consider test(), we need to get CFG of test() only, without considering the body of MACRO_CALL(a).
     *
     *
     *
     * Therefore, to disable the problem of macro expansion in CFG generation of the function,
     * we need to disable macro.
     */
    public static ICFG createCFG(IFunctionNode fn, String coverageType) throws Exception {
        if (fn == null)
            return null;

        /*
         * Find existing cfg of the function node
         */
        ICFG cfg = null;
        switch (coverageType) {
            case EnviroCoverageTypeNode.STATEMENT_AND_BRANCH:
            case EnviroCoverageTypeNode.STATEMENT:
            case EnviroCoverageTypeNode.BRANCH:
            case EnviroCoverageTypeNode.BASIS_PATH:{
                cfg = Environment.getInstance().getCfgsForBranchAndStatement().get(fn.getAbsolutePath());
                break;
            }
            case EnviroCoverageTypeNode.STATEMENT_AND_MCDC:
            case EnviroCoverageTypeNode.MCDC: {
                cfg = Environment.getInstance().getCfgsForMcdc().get(fn.getAbsolutePath());
                break;
            }
        }
        if (cfg == null) {
            // STEP 1: Create a function with disable macro flag
            FunctionNode tmpFunction = new FunctionNode();
            tmpFunction.setAST(Utils.disableMacroInFunction(fn.getAST(), fn));
            tmpFunction.setAbsolutePath(fn.getAbsolutePath());
            tmpFunction.setParent(fn.getParent());

            // STEP 2: generate CFG of the alternative function
            switch (coverageType) {
                case EnviroCoverageTypeNode.STATEMENT_AND_BRANCH:
                case EnviroCoverageTypeNode.STATEMENT:
                case EnviroCoverageTypeNode.BRANCH:
                case EnviroCoverageTypeNode.BASIS_PATH: {
                    cfg = new CFGGenerationforBranchvsStatementvsBasispathCoverage(tmpFunction).generateCFG();
                    Environment.getInstance().getCfgsForBranchAndStatement().put(fn.getAbsolutePath(), cfg);
                    break;
                }
                case EnviroCoverageTypeNode.STATEMENT_AND_MCDC:
                case EnviroCoverageTypeNode.MCDC: {
                    cfg = new CFGGenerationforSubConditionCoverage(tmpFunction).generateCFG();
                    Environment.getInstance().getCfgsForMcdc().put(fn.getAbsolutePath(), cfg);
                    break;
                }
            }
        }

        if (cfg != null) {
            cfg.setFunctionNode(fn);
            cfg.resetVisitedStateOfNodes();
            cfg.setIdforAllNodes();
        }
        return cfg;
    }

//    public static ICFG cloneAndRefactor(IFunctionNode fn, String coverageType, Map<String, String> varsMap) throws Exception {
//        ICFG cfg = null;
//
//        // STEP 1: Create a function with disable macro flag
//        FunctionNode tmpFunction = new FunctionNode();
//        tmpFunction.setAST(disableMacroAndRefactorArg(fn, varsMap));
//        tmpFunction.setAbsolutePath(fn.getAbsolutePath());
//
//        // STEP 2: generate CFG of the alternative function
//        switch (coverageType) {
//            case EnviroCoverageTypeNode.STATEMENT:
//            case EnviroCoverageTypeNode.BRANCH:
//            case EnviroCoverageTypeNode.BASIS_PATH: {
//                cfg = new CFGGenerationforBranchvsStatementvsBasispathCoverage(tmpFunction).generateCFG();
//                Environment.getInstance().getCfgsForBranchAndStatement().put(fn.getAbsolutePath(), cfg);
//                break;
//            }
//
//            case EnviroCoverageTypeNode.MCDC: {
//                cfg = new CFGGenerationforSubConditionCoverage(tmpFunction).generateCFG();
//                Environment.getInstance().getCfgsForMcdc().put(fn.getAbsolutePath(), cfg);
//                break;
//            }
//        }
//
//        if (cfg != null) {
//            cfg.setFunctionNode(fn);
//            cfg.resetVisitedStateOfNodes();
//            cfg.setIdforAllNodes();
//        }
//
//        return cfg;
//    }
//
//    private static IASTFunctionDefinition disableMacroAndRefactorArg(IFunctionNode functionNode, Map<String, String> argsMap){
//        IASTFunctionDefinition astFunctionNode = functionNode.getAST();
//
//        if (astFunctionNode.getFileLocation() == null)
//            return astFunctionNode;
//
//        IASTFunctionDefinition output;
//        // insert spaces to ensure that the location of new function and of the old function are the same
//        try {
//            int startLine = astFunctionNode.getFileLocation().getStartingLineNumber();
//            int startOffset = astFunctionNode.getFileLocation().getNodeOffset();
//
//            String content = astFunctionNode.getRawSignature();
//
//            Map<String, String> localVarsMap = new HashMap<>();
//            for (Map.Entry<String, String> entry : argsMap.entrySet()) {
//                String oldVarName = entry.getValue();
//                String newVarName = "p" + Utils.capitalize(oldVarName);
//                localVarsMap.put(oldVarName, newVarName);
//            }
//
//            content = refactorMap(content, localVarsMap);
//
//            if ((functionNode instanceof ConstructorNode || functionNode instanceof DestructorNode)
//                    && functionNode.getParent() instanceof ClassNode) {
//
//                // put the function in a class to void error when constructing ast
//                String className = functionNode.getParent().getName();
//                content = "class " + className + "{" + content + "};";
//
//                String newContent = Utils.insertSpaceToFunctionContent(startLine,
//                        startOffset - new String("class " + className + "{").length(), content);
//
//                newContent = refactorMap(newContent, argsMap);
//
//                IASTTranslationUnit unit = new SourcecodeFileParser().getIASTTranslationUnit(newContent.toCharArray());
//
//                output = (IASTFunctionDefinition) unit.getChildren()[0].
//                        getChildren()[0].getChildren()[1];
//
//            } else {
//                String newContent = Utils.insertSpaceToFunctionContent(startLine, startOffset, content);
//
//                newContent = refactorMap(newContent, argsMap);
//
//                IASTTranslationUnit unit = new SourcecodeFileParser().getIASTTranslationUnit(newContent.toCharArray());
//                output = (IASTFunctionDefinition) unit.getChildren()[0];
//            }
//
//        } catch (Exception e) {
//            output = astFunctionNode;
//        }
//
//        return output;
//    }
//
//    private static String refactorMap(String origin, Map<String, String> argsMap) {
//        String newContent = origin;
//        for (Map.Entry<String, String> entry : argsMap.entrySet()) {
//            String regex = "\\b" + entry.getKey() + "\\b";
//            newContent = newContent.replaceAll(regex, entry.getValue());
//        }
//        return newContent;
//    }
}
