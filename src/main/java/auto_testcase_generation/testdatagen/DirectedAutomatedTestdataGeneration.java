package auto_testcase_generation.testdatagen;

import auto_testcase_generation.cfg.ICFG;
import auto_testcase_generation.cfg.object.*;
import auto_testcase_generation.cfg.testpath.FullTestpath;
import auto_testcase_generation.cfg.testpath.ITestpathInCFG;
import auto_testcase_generation.testdatagen.se.ISymbolicExecution;
import auto_testcase_generation.testdatagen.se.Parameters;
import auto_testcase_generation.testdatagen.se.SymbolicExecution;
import auto_testcase_generation.cfg.testpath.FullTestpath;
import auto_testcase_generation.cfg.testpath.ITestpathInCFG;
import auto_testcase_generation.testdatagen.se.ISymbolicExecution;
import auto_testcase_generation.testdatagen.se.NewVariableInSe;
import auto_testcase_generation.testdatagen.se.Parameters;
import auto_testcase_generation.testdatagen.se.SymbolicExecution;
import auto_testcase_generation.testdatagen.se.normalization.ConstraintNormalizer;
import auto_testcase_generation.testdatagen.se.*;
import auto_testcase_generation.testdatagen.se.solver.RunZ3OnCMD;
import auto_testcase_generation.testdatagen.se.solver.SmtLibGeneration;
import auto_testcase_generation.testdatagen.se.solver.solutionparser.Z3SolutionParser;
import auto_testcase_generation.testdatagen.testdatainit.BasicTypeRandom;
import com.dse.config.AkaConfig;
import com.dse.config.FunctionConfig;
import com.dse.config.WorkspaceConfig;
import com.dse.coverage.CoverageManager;
import com.dse.coverage.CoverageDataObject;
import com.dse.coverage.SourcecodeCoverageComputation;
import com.dse.coverage.basicpath.BasicPath;
import com.dse.environment.Environment;
import auto_testcase_generation.testdatagen.testdatainit.CompleteIteratingThroughLoop;
import com.dse.environment.object.EnviroCoverageTypeNode;
import com.dse.guifx_v3.controllers.TestCasesExecutionTabController;
import com.dse.guifx_v3.controllers.TestCasesNavigatorController;
import com.dse.guifx_v3.helps.*;
import com.dse.guifx_v3.objects.TestCaseExecutionDataNode;
import com.dse.guifx_v3.objects.TestCasesTreeItem;
import com.dse.testcase_execution.result_trace.AssertionResult;
import com.dse.testcase_manager.TestPrototype;
import com.dse.testcase_manager.minimize.*;
import com.dse.parser.object.*;
import com.dse.testcase_execution.TestcaseExecution;
import com.dse.testcase_manager.ITestCase;
import com.dse.testcase_manager.TestCase;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.testdata.gen.module.SimpleTreeDisplayer;
import com.dse.testdata.object.RootDataNode;
import com.dse.logger.AkaLogger;
import com.dse.util.Utils;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCompoundStatement;
import com.dse.util.CFGUtils;
import com.dse.util.Utils;
import com.dse.util.VariableTypeUtils;
import javafx.collections.ObservableList;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.io.File;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class DirectedAutomatedTestdataGeneration extends ConcolicAutomatedTestdataGeneration {
    private final static AkaLogger logger = AkaLogger.get(DirectedAutomatedTestdataGeneration.class);

    public DirectedAutomatedTestdataGeneration(ICommonFunctionNode fn, String coverageType) {
        super(fn, coverageType);
    }

    @Override
    protected String getTestCaseNamePrefix(ICommonFunctionNode fn) {
        return fn.getSimpleName() + ITestCase.POSTFIX_TESTCASE_BY_DIRECTED_METHOD;
    }

    @Override
    protected List<ICfgNode> getTestPathByCoverage(ICFG currentCFG, String coverageType) {
        currentCFG.setIdforAllNodes();

        switch (coverageType) {
            case EnviroCoverageTypeNode.STATEMENT: {
                logger.debug("Visited stm = " + currentCFG.getVisitedStatements().size());
                int nStm = currentCFG.getVisitedStatements().size() + currentCFG.getUnvisitedStatements().size();
                logger.debug("Total stm = " + nStm);

                if (currentCFG.getUnvisitedStatements().size() == 0) {
                    maximizeCov = true;
                    return new ArrayList<>();
                } else {
                    List <ICfgNode> unvisitedInstructions = currentCFG.getUnvisitedStatements();
                    ICfgNode unvisitedInstruction = unvisitedInstructions.get(
                            new BasicTypeRandom().generateInt(0, unvisitedInstructions.size() - 1));
                    logger.debug("Choose unvisited stm \"" + unvisitedInstruction + "\"");

                    // find a shortest test path through unvisited instructions
                    List<ICfgNode> shortestTestpath = findShortestTestpath(unvisitedInstruction, currentCFG, 0);
                    logger.debug("Shortest test path: " + shortestTestpath);

                    return shortestTestpath;
                }
            }

            case EnviroCoverageTypeNode.STATEMENT_AND_BRANCH:
            case EnviroCoverageTypeNode.STATEMENT_AND_MCDC:
            case EnviroCoverageTypeNode.BASIS_PATH:
            case EnviroCoverageTypeNode.BRANCH:
            case EnviroCoverageTypeNode.MCDC: {
                logger.debug("Visited branches = " + currentCFG.getVisitedBranches().size());
                int nStm = currentCFG.getVisitedBranches().size() + currentCFG.getUnvisitedBranches().size();
                logger.debug("Total branches = " + nStm);

                if (currentCFG.getUnvisitedBranches().size() == 0) {
                    maximizeCov = true;
                    return new ArrayList<>();
                } else {
                    BranchInCFG selectedBranch = currentCFG.getUnvisitedBranches().get(
                            new BasicTypeRandom().generateInt(0, currentCFG.getUnvisitedBranches().size() - 1));
                    ICfgNode unvisitedInstructions = selectedBranch.getEnd();
                    logger.debug("Choose unvisited branches \"" + selectedBranch.getStart() + "\" -> \"" + selectedBranch.getEnd() + "\"");

                    // find a shortest test path through unvisited instructions
                    List<ICfgNode> shortestTestpath = findShortestTestpath(unvisitedInstructions, currentCFG, 0);
                    logger.debug("Shortest test path: " + shortestTestpath);

                    return shortestTestpath;
                }
            }
        }
        return new ArrayList<>();
    }

    public List<ICfgNode> findShortestTestpath(ICfgNode target, ICFG currentCFG, int randTimes) {
        List<ICfgNode> shortestPath = new ArrayList<>();
        Map<String, ICfgNode> mapping = new HashMap<>();

        Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

        for (ICfgNode node : currentCFG.getExpandedNodes()) {
            String name = toDescription(node);
            graph.addVertex(name);

            mapping.put(name, node);
        }

        constructGraphEdges(graph, currentCFG);

        // Find shortest paths
        logger.debug("Shortest path from begin node of CFG to " + target + " :");
        DijkstraShortestPath dijkstraAlg = new DijkstraShortestPath<>(graph);
        ICfgNode beginNode = currentCFG.getBeginNode();
        ShortestPathAlgorithm.SingleSourcePaths<String, DefaultEdge> iPaths = dijkstraAlg.getPaths(toDescription(beginNode));
        org.jgrapht.GraphPath<String, DefaultEdge> shortestTestPath = iPaths.getPath(toDescription(target));

        for (String str : shortestTestPath.getVertexList()) {
            ICfgNode n = mapping.get(str);
            shortestPath.add(n);
        }

        // random the num of iterate for loop
        if (haveLoop(shortestPath)) {
            shortestPath = completePathForFixedLoop(shortestPath, dijkstraAlg, mapping);
        }
        logger.debug("shortestPath = " + shortestPath);

        // Normalize path
        List<ICfgNode> normalizedShortestPath = normalizePath(shortestPath);
        logger.debug("normalizedShortestPath = " + normalizedShortestPath);

        return normalizedShortestPath;
    }

    public static void constructGraphEdges(Graph<String, DefaultEdge> graph, ICFG currentCFG) {
        for (ICfgNode node : currentCFG.getExpandedNodes()) {
            if (node instanceof SwitchCfgNode) {
                for (ICfgNode casenode : ((SwitchCfgNode) node).getCases()) {
                    addEdge(graph, node, casenode);
                }
            } else {
                ICfgNode trueNode = node.getTrueNode();
                ICfgNode falseNode = node.getFalseNode();

                if (trueNode != null && falseNode != null) {
                    if (!trueNode.equals(falseNode)) {
                        addEdge(graph, node, trueNode);
                        addEdge(graph, node, falseNode);
                    } else
                        addEdge(graph, node, trueNode);
                } else if (trueNode != null) {
                    addEdge(graph, node, trueNode);
                } else if (falseNode != null) {
                    addEdge(graph, node, falseNode);
                }
            }

        }
    }

    private static void addEdge(Graph<String, DefaultEdge> graph, ICfgNode from, ICfgNode to) {
        if (to instanceof NormalCfgNode && !((NormalCfgNode) to).getSubCFGs().isEmpty()) {
            Collection<ICFG> subCFGs = ((NormalCfgNode) to).getSubCFGs().values();
            ICfgNode prevNode = from;
            for (ICFG subCFG : subCFGs) {
                ICfgNode newTo = subCFG.getBeginNode();
                graph.addEdge(toDescription(prevNode), toDescription(newTo));
                prevNode = subCFG.getAllNodes().get(subCFG.getAllNodes().size() - 1);
            }
            graph.addEdge(toDescription(prevNode), toDescription(to));
        } else {
            graph.addEdge(toDescription(from), toDescription(to));
        }
    }
}