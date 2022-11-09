package auto_testcase_generation.testdatagen.testdatainit;

import auto_testcase_generation.cfg.object.*;
import auto_testcase_generation.cfg.testpath.FullTestpath;
import auto_testcase_generation.cfg.testpath.ITestpathInCFG;
import auto_testcase_generation.testdatagen.ConcolicAutomatedTestdataGeneration;
import auto_testcase_generation.testdatagen.DFSAutomatedTestdataGeneration;
import auto_testcase_generation.testdatagen.DirectedAutomatedTestdataGeneration;
import auto_testcase_generation.testdatagen.se.ExpressionRewriterUtils;
import auto_testcase_generation.testdatagen.se.ISymbolicExecution;
import auto_testcase_generation.testdatagen.se.Parameters;
import auto_testcase_generation.testdatagen.se.SymbolicExecution;
import auto_testcase_generation.testdatagen.se.memory.VariableNodeTable;
import com.dse.logger.AkaLogger;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.util.Utils;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static auto_testcase_generation.testdatagen.ConcolicAutomatedTestdataGeneration.toDescription;

public class CompleteIteratingThroughLoop {

    private final static AkaLogger logger = AkaLogger.get(DFSAutomatedTestdataGeneration.class);

    private static int findFirstLoop(List<ICfgNode> shortestPath, int start, int end) {
        for (int i = start; i < end; i++) {
            if (shortestPath.get(i) instanceof ConditionForCfgNode
                    || shortestPath.get(i) instanceof ConditionWhileCfgNode
                    || shortestPath.get(i) instanceof ConditionDoCfgNode)
                return i;
        }
        return -1;
    }

    private static List<Integer> findPeerLoops(List<ICfgNode> shortestPath, int start, int end,
                                               IASTCompoundStatement anchor) {
        List<Integer> indexOfPeerForNodes = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            if (shortestPath.get(i) instanceof ConditionForCfgNode
                    || shortestPath.get(i) instanceof ConditionWhileCfgNode
                    || shortestPath.get(i) instanceof ConditionDoCfgNode) {
                // ASK-CHUNG: sao lai getParent().getParent() ?? hard code a
                if (((AbstractConditionLoopCfgNode) shortestPath.get(i))
                        .getAst().getParent().getParent().equals(anchor))
                    indexOfPeerForNodes.add(i);
            }
        }

        return indexOfPeerForNodes;
    }

    private static List<ICfgNode> findNestedPath(ICommonFunctionNode functionNode, List<ICfgNode> shortestPath, List<ICfgNode> tmpShortestPath, DijkstraShortestPath dijkstraAlg, Map<String,
            ICfgNode> mapping, int currentIndexInShortestPath, int end, int endIdx, int numOfRandLoop) throws Exception {
        List<ICfgNode> nestedTestPath = new ArrayList<>();

        for (int j = currentIndexInShortestPath; j < endIdx; j++) {
            if (shortestPath.get(j) instanceof AdditionalScopeCfgNode
                    && shortestPath.get(j).getContent().equals("{")) {
                for (int ii = currentIndexInShortestPath; ii <= j; ii++) {
                    tmpShortestPath.add(shortestPath.get(ii));
                }
                for (int k = j; k < end; k++) {
                    if (shortestPath.get(k) instanceof AdditionalScopeCfgNode
                            && shortestPath.get(k).getContent().equals("}")) {
                        nestedTestPath = randomIterateForLoop(functionNode, dijkstraAlg, mapping, shortestPath, j + 1, k, numOfRandLoop, null);
                    } else {
                        nestedTestPath = randomIterateForLoop(functionNode, dijkstraAlg, mapping, shortestPath, j + 1, end, numOfRandLoop, null);
                    }
                }
            }
        }

        return nestedTestPath;
    }

    private static int getNumOfRandLoop(int randTimes, List<ICfgNode> shortestPath, ICommonFunctionNode functionNode, int currentIndexInShortestPath, List<ICfgNode> tmpShortestPath) throws Exception {
        int numOfRandLoop;
        if (randTimes != 0)
            numOfRandLoop = randTimes;
        else {
            AbstractConditionLoopCfgNode conditionNode = (AbstractConditionLoopCfgNode) shortestPath.get(currentIndexInShortestPath);
            String content = ExpressionRewriterUtils.rewrite(new VariableNodeTable(functionNode), conditionNode.getContent());
            content = ExpressionRewriterUtils.rewriteMacro(functionNode, content);
            IASTNode conditionNodeAst = Utils.convertToIAST(content);
            String op1 = ((ICPPASTBinaryExpression) conditionNodeAst).getOperand1().toString();

            // get value of op1
            // <:8
            // <=: 10; >=: 11; !=: 29
            int valueOfOp1;
            try {
                valueOfOp1 = Integer.parseInt(op1);
            } catch (NumberFormatException ex) {
                //find if value of op2 is constant
                Parameters paramaters = new Parameters();
                paramaters.addAll(functionNode.getArgumentsAndGlobalVariables());
                ITestpathInCFG testpathInCFG = new FullTestpath();
                testpathInCFG.getAllCfgNodes().addAll(tmpShortestPath);
                ISymbolicExecution se = new SymbolicExecution(testpathInCFG, paramaters, functionNode);
//                    if (se.getTableMapping().findPhysicalCellByName(op2).getValue())
                if (se.getTableMapping().findPhysicalCellByName(op1) != null) {
                    try {
                        valueOfOp1 = Integer.parseInt(se.getTableMapping().findPhysicalCellByName(op1).getValue());
                    } catch (NumberFormatException e) {
                        return ThreadLocalRandom.current().nextInt(3, 11);
                    }
//                    valueOfOp1 = Integer.parseInt(se.getTableMapping().findPhysicalCellByName(op1).getValue());
                } else {
                    return ThreadLocalRandom.current().nextInt(3, 11);
                }
            }

            String op2 = ((ICPPASTBinaryExpression) conditionNodeAst).getOperand2().toString();

            // get value of op2
            int valueOfOp2;
            try {
                valueOfOp2 = Integer.parseInt(op2);
//                numOfRandLoop = Integer.parseInt(op2);
            } catch (NumberFormatException ex) {
                //find if value of op2 is constant
                Parameters paramaters = new Parameters();
                paramaters.addAll(functionNode.getArgumentsAndGlobalVariables());
                ITestpathInCFG testpathInCFG = new FullTestpath();
                testpathInCFG.getAllCfgNodes().addAll(tmpShortestPath);
                ISymbolicExecution se = new SymbolicExecution(testpathInCFG, paramaters, functionNode);
//                    if (se.getTableMapping().findPhysicalCellByName(op2).getValue())
                if (se.getTableMapping().findPhysicalCellByName(op2) != null) {
                    try {
                        valueOfOp2 = Integer.parseInt(se.getTableMapping().findPhysicalCellByName(op2).getValue());
                    } catch (NumberFormatException e) {
                        return ThreadLocalRandom.current().nextInt(3, 11);
                    }
//                    valueOfOp2 = Integer.parseInt(se.getTableMapping().findPhysicalCellByName(op2).getValue());
//                    numOfRandLoop =  Integer.parseInt(se.getTableMapping().findPhysicalCellByName(op2).getValue());
                } else {
                    return ThreadLocalRandom.current().nextInt(3, 11);
                }
            }
            numOfRandLoop = Math.abs(valueOfOp1 - valueOfOp2);
            int operand = ((ICPPASTBinaryExpression) conditionNodeAst).getOperator();
            if (operand == 10 || operand == 11 || operand == 29)
                numOfRandLoop++;
        }
////        logger.debug("Loop will run " + numOfRandLoop + " times");
        return numOfRandLoop;
    }

    private static void modifyTmpTestPath(int currentIndexInShortestPath, int endIdx, List<ICfgNode> shortestPath, List<ICfgNode> tmpShortestPath, DijkstraShortestPath dijkstraAlg, int numOfRandLoop, Map<String, ICfgNode> mapping) {
        ICfgNode tmpNode = null;
        // find path to iterate through for loop
        int start = currentIndexInShortestPath;
//        if (shortestPath.get(currentIndexInShortestPath) instanceof ConditionForCfgNode)
//            start--;
        for (int i = start; i <= endIdx; i++) {
            tmpNode = shortestPath.get(i);
            tmpShortestPath.add(tmpNode);
            if (tmpNode instanceof ConditionForCfgNode || tmpNode instanceof ConditionWhileCfgNode || tmpNode instanceof ConditionDoCfgNode) {
                ICfgNode targetNode = tmpNode;
//                if (i != shortestPath.size() - 1) {
//                    ICfgNode beginNode = shortestPath.get(i+1);
                ICfgNode beginNode = targetNode.getTrueNode();
                // find additional path
                List<ICfgNode> shortestPath2 = new ArrayList<>();

                ShortestPathAlgorithm.SingleSourcePaths<String, DefaultEdge> iPaths2 = dijkstraAlg.getPaths(toDescription(beginNode));
                org.jgrapht.GraphPath<String, DefaultEdge> shortestTestPath2 = iPaths2.getPath(toDescription(targetNode));
                if (shortestTestPath2 != null) {
                    for (String str : shortestTestPath2.getVertexList()) {
                        ICfgNode n = mapping.get(str);
                        shortestPath2.add(n);
                    }
                }
                logger.debug("Additional path for loop: " + shortestTestPath2.toString());
                // insert additional path into shortestPath
                for (int j = 0; j < numOfRandLoop; j++) {
                    tmpShortestPath.addAll(shortestPath2);
                }
//                }
//                break;
            }
        }
    }

    public static List<ICfgNode> randomIterateForLoop(ICommonFunctionNode functionNode, DijkstraShortestPath dijkstraAlg, Map<String,
            ICfgNode> mapping, List<ICfgNode> shortestPath, int start, int end, int randTimes, IASTCompoundStatement anchor) throws Exception {
        int numOfRandLoop;
        int idxOfFirstLoop = findFirstLoop(shortestPath, start, end);
        // find anchor if null
        if (anchor == null && idxOfFirstLoop != -1) {
            anchor = (IASTCompoundStatement) ((AbstractConditionLoopCfgNode) shortestPath.get(idxOfFirstLoop)).getAst().getParent().getParent();
        }
        // find all peer for loop node
        List<Integer> indexOfPeerForNodes;
        indexOfPeerForNodes = findPeerLoops(shortestPath, start, end, anchor);

        // initiate tmpShortestPath
        List<ICfgNode> tmpShortestPath = new ArrayList<>();
        if (indexOfPeerForNodes.size() > 0) {
            int endIdx = indexOfPeerForNodes.get(0);
//            if (shortestPath.get(idxOfFirstLoop) instanceof ConditionForCfgNode)
//                endIdx -= 2;
//            else
//                endIdx -= 1;
            endIdx--;
            for (int i = start; i <= endIdx; i++) {
                tmpShortestPath.add(shortestPath.get(i));
            }
        }
        // with each loop
        for (int idx = 0; idx < indexOfPeerForNodes.size(); idx++) {
            int currentIndexInShortestPath = indexOfPeerForNodes.get(idx);

            numOfRandLoop = getNumOfRandLoop(randTimes, shortestPath, functionNode, currentIndexInShortestPath, tmpShortestPath);
            // check if has nested loop
            int endIdx;
            if (idx < indexOfPeerForNodes.size() - 1) {
                if (shortestPath.get(indexOfPeerForNodes.get(idx + 1)) instanceof ConditionForCfgNode)
                    endIdx = indexOfPeerForNodes.get(idx + 1) - 1;
                else
                    endIdx = indexOfPeerForNodes.get(idx + 1) - 1;
            } else
                endIdx = end;
            List<ICfgNode> nestedTestPath = findNestedPath(functionNode, shortestPath, tmpShortestPath, dijkstraAlg, mapping, currentIndexInShortestPath, end, endIdx - 1, numOfRandLoop);

            if (nestedTestPath.size() > 0)
                tmpShortestPath.addAll(nestedTestPath);
            else {
                modifyTmpTestPath(currentIndexInShortestPath, endIdx, shortestPath, tmpShortestPath, dijkstraAlg, numOfRandLoop, mapping);
            }
        }
        return tmpShortestPath;
    }
}
