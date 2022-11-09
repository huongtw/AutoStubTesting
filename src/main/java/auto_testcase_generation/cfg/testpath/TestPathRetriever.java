package auto_testcase_generation.cfg.testpath;

import auto_testcase_generation.cfg.ICFG;
import auto_testcase_generation.cfg.object.*;
import auto_testcase_generation.instrument.FunctionInstrumentationForStatementvsBranch_Markerv2;
import auto_testcase_generation.testdata.object.StatementInTestpath_Mark;
import auto_testcase_generation.testdata.object.TestpathString_Marker;
import auto_testcase_generation.testdatagen.coverage.CFGUpdaterv2;
import com.dse.environment.Environment;
import com.dse.parser.object.AbstractFunctionNode;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.IFunctionNode;
import com.dse.parser.object.MacroFunctionNode;
import com.dse.testcase_manager.TestCase;
import com.dse.util.CFGUtils;
import com.dse.util.TestPathUtils;
import com.dse.util.Utils;
import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.io.File;
import java.util.*;

import static auto_testcase_generation.testdatagen.DirectedAutomatedTestdataGeneration.constructGraphEdges;
import static auto_testcase_generation.testdatagen.DirectedAutomatedTestdataGeneration.toDescription;

/**
 * Ex:
 * Stack<ICfgNode> testpath = new TestPathRetriever(testcase).get();
 */
public class TestPathRetriever {

    private Stack<ICfgNode> testPath;
    private Map<String, ICfgNode> mapping;
    private Graph<String, DefaultEdge> graph;

    public TestPathRetriever(TestCase testCase) {
        try {
            ICommonFunctionNode sut = testCase.getFunctionNode();
            String cov = Environment.getInstance().getTypeofCoverage();

            ICFG currentCFG = null;
            if (sut instanceof MacroFunctionNode) {
                IFunctionNode tmpFunctionNode = ((MacroFunctionNode) sut).getCorrespondingFunctionNode();
                currentCFG = CFGUtils.createCFG(tmpFunctionNode, cov);
                currentCFG.setFunctionNode(tmpFunctionNode);
            } else if (sut instanceof AbstractFunctionNode) {
                currentCFG = CFGUtils.createCFG((IFunctionNode) sut, cov);
                currentCFG.setFunctionNode((IFunctionNode) sut);
            }

            String testPathFile = testCase.getTestPathFile();
            if (currentCFG != null && testPathFile != null && new File(testPathFile).exists()) {
                mapping = new HashMap<>();
                graph = new DefaultDirectedGraph<>(DefaultEdge.class);

                for (ICfgNode node : currentCFG.getExpandedNodes()) {
                    String name = toDescription(node);
                    graph.addVertex(name);
                    mapping.put(name, node);
                }

                constructGraphEdges(graph, currentCFG);

                String testPathContent = Utils.readFileContent(testPathFile);
                String[] lines = testPathContent.split("\\R");
                TestpathString_Marker testPathMarker = new TestpathString_Marker();
                testPathMarker.setEncodedTestpath(lines);

                CFGUpdaterv2 cfgUpdaterv2 = new CFGUpdaterv2(testPathMarker, currentCFG);
                cfgUpdaterv2.updateVisitedNodes();

                testPath = traverse(currentCFG, testPathMarker);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Stack<ICfgNode> get() {
        return testPath;
    }

    private Stack<ICfgNode> traverse(ICFG cfg, TestpathString_Marker marker) {
        Stack<ICfgNode> stack = new Stack<>();

        ICfgNode begin = cfg.getAllNodes().stream()
                .filter(n -> n instanceof BeginFlagCfgNode)
                .findFirst()
                .orElse(null);

        if (begin != null) {
            stack.push(begin);

            Map<String, ICfgNode> cache = new HashMap<>();

            for (StatementInTestpath_Mark line : marker.getStandardTestpathByAllProperties()) {
                if (line.getPropertyByName(FunctionInstrumentationForStatementvsBranch_Markerv2.START_OFFSET_IN_FUNCTION) != null) {

                    String offset = line.getPropertyByName(FunctionInstrumentationForStatementvsBranch_Markerv2.START_OFFSET_IN_FUNCTION).getValue();

                    ICfgNode node;

                    if (cache.containsKey(offset)) {
                        node = cache.get(offset);
                    } else {
                        node = cfg.getAllNodes().stream()
                                .filter(n -> {
                                    if (n.getAstLocation() != null) {
                                        String offsetInCFG = n.getAstLocation().getNodeOffset()
                                                - cfg.getFunctionNode().getAST().getFileLocation().getNodeOffset() + "";
                                        return offset.equals(offsetInCFG);
                                    }
                                    return false;
                                })
                                .findFirst()
                                .orElse(null);
                        if (node != null) {
                            cache.put(offset, node);
                        }
                    }

                    if (node != null) {
                        push(stack, node);
                    }
                }
            }

            for (int i = marker.getEncodedTestpath().length - 1; i >= 0; i--) {
                String line = marker.getEncodedTestpath()[i];
                if (line.contains(TestPathUtils.END_TAG)) {
                    ICfgNode exit = cfg.getAllNodes().stream()
                            .filter(n -> n instanceof EndFlagCfgNode)
                            .findFirst()
                            .orElse(null);

                    if (exit != null) {
                        push(stack, exit);
                    }

                    break;
                }
            }
        }

        return stack;
    }

    private void push(Stack<ICfgNode> stack, ICfgNode node) {
        ICfgNode top = stack.peek();
        if (isNextBy(top, node)) {
            stack.push(node);
        } else {
            List<ICfgNode> subPath = findPathBetween(top, node);
            for (int i = 1; i < subPath.size(); i++) {
                stack.push(subPath.get(i));
            }
        }
    }

    private List<ICfgNode> findPathBetween(ICfgNode from, ICfgNode to) {
        List<ICfgNode> shortestPath = new ArrayList<>();

        // Find shortest paths
        DijkstraShortestPath<String, DefaultEdge> dijkstraAlg = new DijkstraShortestPath<>(graph);
        ShortestPathAlgorithm.SingleSourcePaths<String, DefaultEdge> iPaths = dijkstraAlg.getPaths(toDescription(from));
        org.jgrapht.GraphPath<String, DefaultEdge> shortestTestPath = iPaths.getPath(toDescription(to));

        if (shortestTestPath != null) {
            for (String str : shortestTestPath.getVertexList()) {
                ICfgNode n = mapping.get(str);
                shortestPath.add(n);
            }
        }

        return shortestPath;
    }

    private boolean isNextBy(ICfgNode from, ICfgNode to) {
        if (from instanceof ConditionCfgNode) {
            return from.getFalseNode() == to || from.getTrueNode() == to;
        } else if (from instanceof SwitchCfgNode) {
            return ((SwitchCfgNode) from).getCases().contains(to);
        } else {
            return from.getFalseNode() == to || from.getTrueNode() == to;
        }
    }
}
