package com.dse.coverage;

import auto_testcase_generation.cfg.ICFG;
import com.dse.config.WorkspaceConfig;
import com.dse.environment.object.EnviroCoverageTypeNode;
import com.dse.environment.Environment;
import com.dse.guifx_v3.helps.InstructionMapping;
import com.dse.parser.object.*;
import com.dse.search.Search;
import com.dse.search.condition.AbstractFunctionNodeCondition;
import com.dse.search.condition.MacroFunctionNodeCondition;
import com.dse.logger.AkaLogger;
import com.dse.util.CFGUtils;
import com.dse.util.PathUtils;
import com.dse.util.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Compute the number of statements/branches
 */
public class InstructionComputator {

    final static AkaLogger logger = AkaLogger.get(InstructionComputator.class);

    public static final int BRANCH_COV_INDEX = 0;
    public static final int STATEMENT_COV_INDEX = 1;
    public static final int MCDC_COV_INDEX = 2;
    public static final int BASIC_PATH_COV_INDEX = 3;

    private static final String LOG_FORMAT = "[%d/%d] calculate num of visited %s in %s";
    private static final String ERROR_FORMAT = "Error with %s: %s";

    private final int LENGTH = 4;

    private final int[] result = new int[LENGTH];

    private int count, size;

    public static void compute() {
        logger.debug("Compute the number of statements/branches and saved in file");
//        List<INode> sourcecodeNodes = Search.searchNodes(projectNode, new SourcecodeFileNodeCondition());
        List<INode> sbfUnits = Environment.getInstance().getSBFs();
        List<INode> uutUnits = Environment.getInstance().getUUTs();
        List<INode> sourcecodeNodes = new ArrayList<>();
        sourcecodeNodes.addAll(sbfUnits);
        sourcecodeNodes.addAll(uutUnits);

        logger.debug("Number of source code file: " + sourcecodeNodes.size());

        InstructionMapping nStatements = new InstructionMapping();
        InstructionMapping nBranches = new InstructionMapping();
        InstructionMapping nMcdcs = new InstructionMapping();
        InstructionMapping nBasicPaths = new InstructionMapping();

        List<String> computedSourcecodes = new ArrayList<>();
        for (INode sourcecodeNode : sourcecodeNodes)
            if (sourcecodeNode instanceof ISourcecodeFileNode &&
                    // to avoid duplicating instruction computation
                    !computedSourcecodes.contains(sourcecodeNode.getAbsolutePath())) {
                computedSourcecodes.add(sourcecodeNode.getAbsolutePath());

                try {
                    InstructionComputator instructionComputator = new InstructionComputator();
                    int[] instructionComputation = instructionComputator.getNumberOfBranchesAndStatements(sourcecodeNode);

                    int nStatement = instructionComputation[InstructionComputator.STATEMENT_COV_INDEX];
                    nStatements.put(PathUtils.toRelative(sourcecodeNode.getAbsolutePath()), nStatement);

                    int nBranch = instructionComputation[InstructionComputator.BRANCH_COV_INDEX];
                    nBranches.put(PathUtils.toRelative(sourcecodeNode.getAbsolutePath()), nBranch);

                    int nMcdc = instructionComputation[InstructionComputator.MCDC_COV_INDEX];
                    nMcdcs.put(PathUtils.toRelative(sourcecodeNode.getAbsolutePath()), nMcdc);

                    int nBasicPath = instructionComputation[InstructionComputator.BASIC_PATH_COV_INDEX];
                    nBasicPaths.put(PathUtils.toRelative(sourcecodeNode.getAbsolutePath()), nBasicPath);
                } catch (Exception e) {
                    logger.error("Cant compute", e);
                    e.printStackTrace();
                }

            }

        logger.debug("Compute done. Save to environment");

        Environment.getInstance().setStatementsMapping(nStatements);
        Environment.getInstance().setBranchesMapping(nBranches);
        Environment.getInstance().setMcdcMapping(nMcdcs);
        Environment.getInstance().setBasicPathMapping(nBasicPaths);

        logger.debug("Compute done. Save to file");

        // save the number of statements to file
        {
            String nStmFilePath = new WorkspaceConfig().fromJson().getnStatementPath();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String nStatementsJson = gson.toJson(nStatements);
            Utils.writeContentToFile(nStatementsJson, nStmFilePath);
        }

        // save the number of branches to file
        {
            String nBranchFilePath = new WorkspaceConfig().fromJson().getnBranchPath();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String nBranchesJson = gson.toJson(nBranches);
            Utils.writeContentToFile(nBranchesJson, nBranchFilePath);
        }

        // save the number of mcdc to file
        {
            String nMcdcFilePath = new WorkspaceConfig().fromJson().getnMcdcPath();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String nMcdcsJson = gson.toJson(nMcdcs);
            Utils.writeContentToFile(nMcdcsJson, nMcdcFilePath);
        }

        // save the number of basic path to file
        {
            String nBasicPathFilePath = new WorkspaceConfig().fromJson().getnBasicPath();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String nBasicPathsJson = gson.toJson(nBasicPaths);
            Utils.writeContentToFile(nBasicPathsJson, nBasicPathFilePath);
        }

        logger.debug("Save done.");
    }

    public int[] getNumberOfBranchesAndStatements(INode consideredSourcecodeNode) {

        // constructor, destructor, normal function
        List<INode> functionNodes = Search.searchNodes(consideredSourcecodeNode, new AbstractFunctionNodeCondition());
        // macro function
        List<INode> macroFunctionNodes = Search.searchNodes(consideredSourcecodeNode, new MacroFunctionNodeCondition());

        size = (functionNodes.size() + macroFunctionNodes.size()) * LENGTH;

        for (INode node : functionNodes) {
            if (node instanceof IFunctionNode) {
                if(node.getName().equals(node.getParent().getName()))
                {
                    continue;
                }

                else {
                    IFunctionNode functionNode = (IFunctionNode) node;
                    computeNode(functionNode, functionNode.getName());
                }
            }
        }

        for (INode node : macroFunctionNodes)
            if (node instanceof MacroFunctionNode) {
                MacroFunctionNode macroFunctionNode = (MacroFunctionNode) node;
                IFunctionNode correspondingNode = macroFunctionNode.getCorrespondingFunctionNode();
                computeNode(correspondingNode, macroFunctionNode.getName());
            }

        return result;
    }

    private void computeNode(IFunctionNode functionNode, String functionName) {
        try {
            logger.debug(String.format(LOG_FORMAT, ++count, size, EnviroCoverageTypeNode.STATEMENT, functionName));
            result[STATEMENT_COV_INDEX] += computeNodeByType(functionNode, EnviroCoverageTypeNode.STATEMENT);

            logger.debug(String.format(LOG_FORMAT, ++count, size, EnviroCoverageTypeNode.BRANCH, functionName));
            result[BRANCH_COV_INDEX] += computeNodeByType(functionNode, EnviroCoverageTypeNode.BRANCH);

            logger.debug(String.format(LOG_FORMAT, ++count, size, EnviroCoverageTypeNode.MCDC, functionName));
            result[MCDC_COV_INDEX] += computeNodeByType(functionNode, EnviroCoverageTypeNode.MCDC);

            logger.debug(String.format(LOG_FORMAT, ++count, size, EnviroCoverageTypeNode.BASIS_PATH, functionName));
            result[BASIC_PATH_COV_INDEX] += computeNodeByType(functionNode, EnviroCoverageTypeNode.BASIS_PATH);
        } catch (Exception ex) {
            logger.debug(String.format(LOG_FORMAT, ++count, size, EnviroCoverageTypeNode.STATEMENT, functionName));
//            result[STATEMENT_COV_INDEX] += 0;

            logger.debug(String.format(LOG_FORMAT, ++count, size, EnviroCoverageTypeNode.BRANCH, functionName));
//            result[BRANCH_COV_INDEX] += 0;

            logger.debug(String.format(LOG_FORMAT, ++count, size, EnviroCoverageTypeNode.MCDC, functionName));
//            result[MCDC_COV_INDEX] += 0;
        }

    }

    private int computeNodeByType(IFunctionNode functionNode, String coverageType) {
        try {
            ICFG cfg = CFGUtils.createCFG(functionNode, coverageType);

            switch (coverageType) {
                case EnviroCoverageTypeNode.BRANCH:
                case EnviroCoverageTypeNode.MCDC:
                    return cfg.getVisitedBranches().size() + cfg.getUnvisitedBranches().size();

                case EnviroCoverageTypeNode.STATEMENT:
                    return cfg.getVisitedStatements().size() + cfg.getUnvisitedStatements().size();

                case EnviroCoverageTypeNode.BASIS_PATH:
                    return cfg.getVisitedBasisPaths().size() + cfg.getUnvisitedBasisPaths().size();

                default:
                    return 0;
            }

        } catch (Exception ex) {
            logger.debug(String.format(ERROR_FORMAT, functionNode.getName(), ex.getMessage()));
            return 0;
        }
    }
}
