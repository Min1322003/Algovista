package com.algovista.backend.visualization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public record DijkstraResult(
        String sourceId,
        Map<String, Integer> distances,
        Map<String, String> previous,
        List<String> visitOrder,
        List<DijkstraStep> steps) {

    /** Node ids from the source to the target, or an empty list if unreachable. */
    public List<String> shortestPath(String targetId) {
        if (!distances.containsKey(targetId) || distances.get(targetId) == null) {
            return List.of();
        }
        List<String> path = new ArrayList<>();
        String at = targetId;
        while (at != null) {
            path.add(at);
            at = previous.get(at);
        }
        Collections.reverse(path);
        return path;
    }
}
