package com.algovista.backend.visualization;

public record Edge(String id, String source, String target, int weight) {

    /** Returns the node at the other end of this edge. */
    public String otherEnd(String nodeId) {
        if (nodeId.equals(source)) {
            return target;
        }
        if (nodeId.equals(target)) {
            return source;
        }
        throw new IllegalArgumentException("Node " + nodeId + " is not part of edge " + id);
    }
}
