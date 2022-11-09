package com.dse.test;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

/**
 * Shows how to create an FNSS Topology, convert it to a JGraphT Graph and
 * then compute shortest paths
 * <p>
 * https://www.programcreek.com/java-api-examples/?api=org.jgrapht.alg.DijkstraShortestPath
 */
public class MyDijkstraShortestPath {

    public static void main(String[] args) {
        Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

        graph.addVertex("1");
        graph.addVertex("2");
        graph.addVertex("3");
        graph.addVertex("4");

        graph.addEdge("1", "2");
        graph.addEdge("2", "3");
        graph.addEdge("2", "4");
        graph.addEdge("3", "4");

        // Find shortest paths
        System.out.println("Shortest path from ... to ...:");
        DijkstraShortestPath dijkstraAlg = new DijkstraShortestPath<>(graph);
        ShortestPathAlgorithm.SingleSourcePaths<String, DefaultEdge> iPaths = dijkstraAlg.getPaths("1");
        System.out.println(iPaths.getPath("4") + "\n");
    }
}