package com.dse.coverage.basicpath;

import auto_testcase_generation.cfg.ICFG;
import auto_testcase_generation.cfg.object.*;

import java.util.*;
import java.util.stream.Collectors;

public class BasicPathsFinder {

    protected Set<BasicPath> set;
    protected List<SubPath> subPaths;

    protected final ICFG cfg;

    protected final ICfgNode begin;
    protected final ICfgNode exit;

    public BasicPathsFinder(ICFG cfg) {
        this.cfg = cfg;

        begin = cfg.getAllNodes().stream()
                .filter(n -> n instanceof BeginFlagCfgNode)
                .findFirst()
                .orElse(null);

        exit = cfg.getAllNodes().stream()
                .filter(n -> n instanceof EndFlagCfgNode)
                .findFirst()
                .orElse(null);
    }

    public Set<BasicPath> set() {
        if (set == null && cfg != null) {
            set = new HashSet<>();
            subPaths = new ArrayList<>();

            List<Edge> edges = constructGraphEdges(cfg);

            Stack<ICfgNode> stack = new Stack<>();

            if (begin != null && exit != null) {
                stack.push(begin);
                step2(stack, edges);
            }
        }

        return set;
    }

    private void step2(Stack<ICfgNode> stack, List<Edge> edges) {
        if (!stack.isEmpty()) {
            step3(stack, edges);
        } else {
            step7();
        }
    }

    private void step3(Stack<ICfgNode> stack, List<Edge> edges) {
        ICfgNode top = stack.peek();
        Edge unvisitedEdge = findUnvisitedEdge(edges, top);
        if (unvisitedEdge == null) {
            stack.pop();
            step2(stack, edges);
        } else {
            step4(stack, edges, unvisitedEdge);
        }
    }

    private void step4(Stack<ICfgNode> stack, List<Edge> edges, Edge unvisitedEdge) {
        unvisitedEdge.setVisited(true);
        ICfgNode head = unvisitedEdge.head;
        if (head == exit) {
            List<ICfgNode> list = new ArrayList<>(stack);
            list.add(head);
            BasicPath basicPath = new BasicPath(list);
            set.add(basicPath);
            addMultiDegreeSubPaths(basicPath);
            step3(stack, edges);
        } else {
            step5(stack, edges, unvisitedEdge);
        }
    }

    private void step5(Stack<ICfgNode> stack, List<Edge> edges, Edge unvisitedEdge) {
        if (!stack.contains(unvisitedEdge.head)) {
            SubPath subPath = subPaths.stream()
                    .filter(sp -> sp.get(0).equals(unvisitedEdge.head))
                    .findFirst()
                    .orElse(null);
            if (subPath != null) {
                List<ICfgNode> list = new ArrayList<>(stack);
                list.addAll(subPath);
                BasicPath basicPath = new BasicPath(list);
                set.add(basicPath);
//                addMultiDegreeSubPaths(basicPath);
                step3(stack, edges);
            } else
                step6(stack, edges, unvisitedEdge);
        } else {
            step6(stack, edges, unvisitedEdge);
        }
    }

    private void step6(Stack<ICfgNode> stack, List<Edge> edges, Edge unvisitedEdge) {
        if (stack.contains(unvisitedEdge.head)) {
            SubPath subPath = new SubPath();
            subPath.addAll(stack);
            subPath.add(unvisitedEdge.head);
            addSubPath(subPaths, subPath);
        } else {
            stack.push(unvisitedEdge.head);
        }
        step2(stack, edges);
    }

    private void step7() {
        List<SubPath> subPathsFromStart = subPaths.stream()
                .filter(sp -> sp.get(0).equals(begin))
                .collect(Collectors.toList());

//        for (int i = 0; i < subPathsFromStart.size(); i++) {
            for (SubPath spFromStart : subPathsFromStart) {
                if (spFromStart.size() > 1) {
                    List<ICfgNode> list = new ArrayList<>(spFromStart);
                    ICfgNode last = list.remove(list.size() - 1);
                    List<SubPath> subPathsFromLast = subPaths.stream()
                            .filter(sp -> sp.get(0).equals(last))
                            .collect(Collectors.toList());
                    for (SubPath subPathFromLast : subPathsFromLast) {
                        if (!list.contains(subPathFromLast.get(1))) {
                            List<ICfgNode> listFull = new ArrayList<>(spFromStart);
                            listFull.addAll(subPathFromLast);
                            BasicPath basicPath = new BasicPath(listFull);
                            set.add(basicPath);
                            addMultiDegreeSubPaths(basicPath);
                        }
                    }
                }
            }
//        }
    }

    protected void addMultiDegreeSubPaths(BasicPath basicPath) {
        int length = basicPath.size();
        for (int i = 1; i < length; i++) {
            List<ICfgNode> subList = basicPath.subList(i, length);
            SubPath subPath = new SubPath(subList);
            addSubPath(subPaths, subPath);
        }
    }

    protected void addSubPath(List<SubPath> subPaths, SubPath subPath) {
        if (!subPaths.contains(subPath)) {
            ICfgNode first = subPath.get(0);
            boolean exist = false;
            for (int i = 1; i < subPath.size(); i++) {
                if (subPath.get(i).equals(first)) {
                    exist = true;
                    break;
                }
            }
            if (!exist)
                subPaths.add(subPath);
        }
    }

    protected Edge findUnvisitedEdge(List<Edge> edges, ICfgNode tail) {
        List<Edge> possibleEdges = edges.stream()
                .filter(e -> e.isTail(tail) && !e.isVisited())
                .collect(Collectors.toList());

        if (possibleEdges.isEmpty())
            return null;
        else if (possibleEdges.size() == 1)
            return possibleEdges.get(0);
        else {
            if (tail instanceof ConditionCfgNode) {
                return possibleEdges.stream()
                        .filter(e -> e.isHead(getConcrete(tail.getFalseNode())))
                        .findFirst()
                        .orElse(possibleEdges.get(0));
            } else
                return possibleEdges.get(0);
        }
    }

    protected List<Edge> constructGraphEdges(ICFG currentCFG) {
        List<Edge> edges = new ArrayList<>();
        List<ICfgNode> statements = new ArrayList<>(currentCFG.getAllNodes());
//        statements.removeIf(n -> !(n instanceof FlagCfgNode || n instanceof NormalCfgNode));
        for (ICfgNode node : statements) {
            if (node instanceof SwitchCfgNode) {
                for (ICfgNode caseNode : ((SwitchCfgNode) node).getCases()) {
                    Edge edge = createEdge(node, getConcrete(caseNode));
                    edges.add(edge);
                }
            } else {
                ICfgNode trueNode = getConcrete(node.getTrueNode());
                ICfgNode falseNode = getConcrete(node.getFalseNode());
                if (node instanceof SimpleCfgNode) {
                    trueNode = falseNode;
                }

                if (trueNode != null && falseNode != null) {
                    if (!trueNode.equals(falseNode)) {
                        Edge edgeTrue = createEdge(node, trueNode);
                        edges.add(edgeTrue);
                        Edge edgeFalse = createEdge(node, falseNode);
                        edges.add(edgeFalse);
                    } else {
                        Edge edge = createEdge(node, trueNode);
                        edges.add(edge);
                    }
                } else if (trueNode != null) {
                    Edge edge = createEdge(node, trueNode);
                    edges.add(edge);
                } else if (falseNode != null) {
                    Edge edge = createEdge(node, falseNode);
                    edges.add(edge);
                }
            }
        }
        return edges;
    }

    private ICfgNode getConcrete(ICfgNode n) {
        ICfgNode concrete = n;

//        if (concrete != null && !concrete.equals(exit)) {
//            while (!(concrete instanceof NormalCfgNode)) {
//                concrete = concrete.getTrueNode();
//            }
//        }

        return concrete;
    }

    private Edge createEdge(ICfgNode from, ICfgNode to) {
        return new Edge(to, from);
    }

    protected static class Edge {

        private final ICfgNode head, tail;

        private boolean visited = false;

        private Edge(ICfgNode head, ICfgNode tail) {
            this.head = head;
            this.tail = tail;
        }

        public boolean isVisited() {
            return visited;
        }

        public void setVisited(boolean visited) {
            this.visited = visited;
        }

        public boolean isHead(ICfgNode n) {
            return head == n;
        }

        public boolean isTail(ICfgNode n) {
            return tail == n;
        }

        public ICfgNode getHead() {
            return head;
        }

        public ICfgNode getTail() {
            return tail;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass())
                return false;

            Edge edge = (Edge) o;

            if (!Objects.equals(head, edge.head))
                return false;

            return Objects.equals(tail, edge.tail);
        }
    }

    protected static class SubPath extends ArrayList<ICfgNode> {
        public SubPath() {
        }

        public SubPath(Collection<? extends ICfgNode> c) {
            super(c);
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof SubPath) {
                SubPath sp = (SubPath) o;
                if (sp.size() == size()) {
                    for (int i = 0; i < size(); i++) {
                        if (!get(i).equals(sp.get(i))) {
                            return false;
                        }
                    }
                    return true;
                }
            }

            return false;
        }
    }
}
