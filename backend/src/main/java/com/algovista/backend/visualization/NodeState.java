package com.algovista.backend.visualization;

/** Visual state of a node in one step. Matches the Figma legend. */
public enum NodeState {
    UNVISITED,
    SOURCE,
    CURRENT,
    NEIGHBOR,
    VISITED
}
