package com.algovista.backend.visualization;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Graph {

    private final boolean directed;
    private final Map<String, Node> nodes = new LinkedHashMap<>();
    private final Map<String, Edge> edges = new LinkedHashMap<>();

    public Graph(boolean directed) {
        this.directed = directed;
    }

    public Graph addNode(Node node) {
        if (nodes.putIfAbsent(node.id(), node) != null) {
            throw new IllegalArgumentException("Duplicate node id: " + node.id());
        }
        return this;
    }

    public Graph addEdge(Edge edge) {
        if (!nodes.containsKey(edge.source()) || !nodes.containsKey(edge.target())) {
            throw new IllegalArgumentException("Edge " + edge.id() + " references an unknown node");
        }
        if (edge.weight() < 0) {
            throw new IllegalArgumentException("Dijkstra needs non-negative weights (edge " + edge.id() + ")");
        }
        if (edges.putIfAbsent(edge.id(), edge) != null) {
            throw new IllegalArgumentException("Duplicate edge id: " + edge.id());
        }
        return this;
    }

    public Node getNode(String id) {
        return nodes.get(id);
    }

    public Edge getEdge(String id) {
        return edges.get(id);
    }

    public List<Node> getNodes() {
        return List.copyOf(nodes.values());
    }

    public List<Edge> getEdges() {
        return List.copyOf(edges.values());
    }

    public boolean isDirected() {
        return directed;
    }

    /** Edges you can travel along when leaving the given node. */
    public List<Edge> getOutgoingEdges(String nodeId) {
        List<Edge> result = new ArrayList<>();
        for (Edge e : edges.values()) {
            boolean leavesFromNode = e.source().equals(nodeId) || (!directed && e.target().equals(nodeId));
            if (leavesFromNode) {
                result.add(e);
            }
        }
        return result;
    }

    public List<Node> getNeighbors(String nodeId) {
        List<Node> result = new ArrayList<>();
        for (Edge e : getOutgoingEdges(nodeId)) {
            result.add(nodes.get(e.otherEnd(nodeId)));
        }
        return result;
    }
}
