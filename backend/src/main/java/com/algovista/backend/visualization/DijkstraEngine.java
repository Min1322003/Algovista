package com.algovista.backend.visualization;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Textbook Dijkstra that records a snapshot after every step.
 * It picks the next node with a simple scan (ties go to the node added first),
 * which keeps the steps deterministic and easy to teach.
 */
public class DijkstraEngine {

    public DijkstraResult run(Graph graph, String sourceId) {
        if (graph.getNode(sourceId) == null) {
            throw new IllegalArgumentException("Unknown source node: " + sourceId);
        }

        Map<String, Integer> dist = new LinkedHashMap<>();
        Map<String, String> prev = new LinkedHashMap<>();
        Set<String> visited = new LinkedHashSet<>();
        List<DijkstraStep> steps = new ArrayList<>();

        for (Node n : graph.getNodes()) {
            dist.put(n.id(), null); // null = infinity
            prev.put(n.id(), null);
        }
        dist.put(sourceId, 0);

        addStep(steps, graph, sourceId, StepType.INITIALIZE, null, null, null, null, null,
                dist, prev, visited,
                "Set the distance of " + name(graph, sourceId) + " to 0 and every other node to \u221e.");

        while (true) {
            String current = pickNext(graph, dist, visited);
            if (current == null) {
                break;
            }

            addStep(steps, graph, sourceId, StepType.SELECT_NODE, current, null, null, null, null,
                    dist, prev, visited,
                    "Pick the unvisited node with the smallest distance: "
                            + name(graph, current) + " (" + dist.get(current) + ").");

            for (Edge edge : graph.getOutgoingEdges(current)) {
                String neighbor = edge.otherEnd(current);
                if (neighbor.equals(current) || visited.contains(neighbor)) {
                    continue;
                }

                int candidate = dist.get(current) + edge.weight();
                Integer old = dist.get(neighbor);
                boolean improved = old == null || candidate < old;

                if (improved) {
                    dist.put(neighbor, candidate);
                    prev.put(neighbor, current);
                }

                String text = "Check edge " + name(graph, current) + " \u2192 " + name(graph, neighbor)
                        + " (weight " + edge.weight() + "): "
                        + dist.get(current) + " + " + edge.weight() + " = " + candidate
                        + (improved
                                ? " is less than " + fmt(old) + ", so update " + name(graph, neighbor)
                                        + " to " + candidate + "."
                                : " is not less than " + fmt(old) + ", so keep " + name(graph, neighbor)
                                        + " at " + fmt(old) + ".");

                addStep(steps, graph, sourceId, StepType.RELAX_EDGE, current, neighbor, edge.id(),
                        candidate, improved, dist, prev, visited, text);
            }

            visited.add(current);
            addStep(steps, graph, sourceId, StepType.MARK_VISITED, current, null, null, null, null,
                    dist, prev, visited,
                    "Mark " + name(graph, current) + " as visited. Its shortest distance is final: "
                            + dist.get(current) + ".");
        }

        addStep(steps, graph, sourceId, StepType.FINISHED, null, null, null, null, null,
                dist, prev, visited,
                "Done. No reachable unvisited nodes are left.");

        return new DijkstraResult(
                sourceId,
                new LinkedHashMap<>(dist),
                new LinkedHashMap<>(prev),
                new ArrayList<>(visited),
                steps);
    }

    private String pickNext(Graph graph, Map<String, Integer> dist, Set<String> visited) {
        String best = null;
        for (Node n : graph.getNodes()) {
            Integer d = dist.get(n.id());
            if (visited.contains(n.id()) || d == null) {
                continue;
            }
            if (best == null || d < dist.get(best)) {
                best = n.id();
            }
        }
        return best;
    }

    private void addStep(List<DijkstraStep> steps, Graph graph, String sourceId, StepType type,
            String current, String neighbor, String edgeId, Integer candidate, Boolean updated,
            Map<String, Integer> dist, Map<String, String> prev, Set<String> visited,
            String description) {

        Map<String, NodeState> states = new LinkedHashMap<>();
        for (Node n : graph.getNodes()) {
            String id = n.id();
            NodeState state;
            if (visited.contains(id)) {
                state = NodeState.VISITED;
            } else if (id.equals(current)) {
                state = NodeState.CURRENT;
            } else if (id.equals(neighbor)) {
                state = NodeState.NEIGHBOR;
            } else if (id.equals(sourceId)) {
                state = NodeState.SOURCE;
            } else {
                state = NodeState.UNVISITED;
            }
            states.put(id, state);
        }

        steps.add(new DijkstraStep(
                steps.size(), type, current, neighbor, edgeId, candidate, updated,
                new LinkedHashMap<>(dist),
                new LinkedHashMap<>(prev),
                new ArrayList<>(visited),
                states,
                description));
    }

    private String name(Graph graph, String id) {
        return graph.getNode(id).name();
    }

    private String fmt(Integer distance) {
        return distance == null ? "\u221e" : String.valueOf(distance);
    }
}
