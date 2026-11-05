package com.algovista.backend.visualization;

public enum StepType {
    /** All distances set to infinity, source set to 0. */
    INITIALIZE,
    /** The unvisited node with the smallest distance becomes current. */
    SELECT_NODE,
    /** One edge from the current node is checked ("relaxed"). */
    RELAX_EDGE,
    /** All neighbors of the current node are done; it is marked visited. */
    MARK_VISITED,
    /** Nothing reachable is left to process. */
    FINISHED
}
