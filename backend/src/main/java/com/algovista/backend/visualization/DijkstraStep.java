package com.algovista.backend.visualization;

import java.util.List;
import java.util.Map;

/**
 * Snapshot of the algorithm after one step.
 * A null distance means infinity. Fields that don't apply to a step type are null.
 */
public record DijkstraStep(
        int index,
        StepType type,
        String currentNodeId,
        String neighborNodeId,
        String edgeId,
        Integer candidateDistance,
        Boolean updated,
        Map<String, Integer> distances,
        Map<String, String> previous,
        List<String> visited,
        Map<String, NodeState> nodeStates,
        String description) {
}
