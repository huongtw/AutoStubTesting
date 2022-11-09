package com.dse.coverage.basicpath;

import auto_testcase_generation.cfg.ICFG;
import auto_testcase_generation.cfg.object.ICfgNode;
import auto_testcase_generation.cfg.object.NormalCfgNode;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

import java.util.*;
import java.util.stream.Collectors;

public class BasicPathsObtain extends BasicPathsFinder {

    public BasicPathsObtain(ICFG cfg) {
        super(cfg);
    }

    @Override
    public Set<BasicPath> set() {
        if (set == null && cfg != null) {
            set = new HashSet<>();
            subPaths = new ArrayList<>();

            List<Edge> edges = constructGraphEdges(cfg);

            Stack<ICfgNode> stack = new Stack<>();

            if (begin != null && exit != null) {
                stack.push(begin);
                while (!stack.isEmpty()) {
                    ICfgNode top = stack.peek();
                    Edge unvisitedEdge = findUnvisitedEdge(edges, top);
                    if (unvisitedEdge == null) {
//                        if (isForIncrement(top.getTrueNode())) {
                            List<ICfgNode> list = new ArrayList<>(stack);
                            SubPath subPath = new SubPath(list);
                            addSubPath(subPaths, subPath);
                            mergePath();
//                        }

                        stack.pop();
                    } else {
                        unvisitedEdge.setVisited(true);
                        ICfgNode head = unvisitedEdge.getHead();
                        if (head == exit) {
                            List<ICfgNode> list = new ArrayList<>(stack);
                            list.add(head);
                            BasicPath basicPath = new BasicPath(list);
                            set.add(basicPath);
                            addMultiDegreeSubPaths(basicPath);
                        } else if (stack.contains(head)) {
                            List<ICfgNode> list = new ArrayList<>(stack);
                            list.add(head);
                            SubPath subPath = new SubPath(list);
                            addSubPath(subPaths, subPath);
                            mergePath();
                        } else {
                            stack.add(head);
                        }
                    }
                }



                System.out.println();
            }
        }

        return set;
    }

    private boolean isForIncrement(ICfgNode n) {
        if (n instanceof NormalCfgNode) {
            IASTNode ast = ((NormalCfgNode) n).getAst();

            IASTForStatement astFor;
            IASTNode cur = ast;
            while (true) {
                if (cur instanceof IASTForStatement) {
                    astFor = (IASTForStatement) cur;
                    break;
                } else if (cur instanceof IASTStatement) {
                    return false;
                } else if (cur == null) {
                    return false;
                } else {
                    cur = cur.getParent();
                }
            }

            return astFor.getIterationExpression().equals(ast);
        }

        return false;
    }

    private void mergePath() {
        List<SubPath> subPathsFromStart = subPaths.stream()
                .filter(sp -> sp.get(0).equals(begin))
                .collect(Collectors.toList());

        for (SubPath spFromStart : subPathsFromStart) {
            if (spFromStart.size() > 1) {
                List<ICfgNode> list = new ArrayList<>(spFromStart);
                ICfgNode last = list.remove(list.size() - 1);
                List<SubPath> subPathsFromLast = subPaths.stream()
                        .filter(sp -> sp.get(0).equals(last))
                        .collect(Collectors.toList());
                if (!subPathsFromLast.isEmpty()) {
                    SubPath subPathFromLast = subPathsFromLast.get(subPathsFromLast.size() - 1);
                    List<ICfgNode> listFull = new ArrayList<>(list);
                    listFull.addAll(subPathFromLast);
                    BasicPath basicPath = new BasicPath(listFull);
                    set.add(basicPath);
                    subPaths.remove(subPathFromLast);
                    subPaths.remove(spFromStart);
                    addMultiDegreeSubPaths(basicPath);
                }
            }
        }
    }
}
