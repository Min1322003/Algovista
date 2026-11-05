package com.algovista.backend.controller;

import com.algovista.backend.visualization.DemoGraphs;
import com.algovista.backend.visualization.DijkstraEngine;
import com.algovista.backend.visualization.DijkstraResult;
import com.algovista.backend.visualization.DijkstraStep;
import com.algovista.backend.visualization.Edge;
import com.algovista.backend.visualization.Graph;
import com.algovista.backend.visualization.Node;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** TEMPORARY: lets us see the engine work before lessons exist. Removed in Part 3. */
@RestController
@RequestMapping("/api/demo")
public class DemoController {

    private final DijkstraEngine engine = new DijkstraEngine();

    public record DemoResponse(Graph graph, DijkstraResult result) {
    }

    @GetMapping("/dijkstra")
    public DemoResponse dijkstra(@RequestParam(defaultValue = "A") String source) {
        Graph graph = DemoGraphs.figmaGraph();
        return new DemoResponse(graph, engine.run(graph, source));
    }

    @GetMapping(value = "/dijkstra/trace", produces = "text/plain;charset=UTF-8")
    public String trace(@RequestParam(defaultValue = "A") String source) {
        Graph graph = DemoGraphs.figmaGraph();
        DijkstraResult result = engine.run(graph, source);

        StringBuilder sb = new StringBuilder();
        sb.append("Dijkstra trace, source = ").append(source).append("\n");
        sb.append("Edges: ")
                .append(graph.getEdges().stream()
                        .map(e -> e.source() + "-" + e.target() + " (" + e.weight() + ")")
                        .collect(Collectors.joining(", ")))
                .append("\n\n");

        for (DijkstraStep s : result.steps()) {
            sb.append(String.format("%2d. %-13s %s%n", s.index(), s.type(), s.description()));
            sb.append("      dist = ").append(formatDistances(s.distances()))
                    .append("   visited = ").append(s.visited()).append("\n");
        }

        sb.append("\nFinal shortest paths from ").append(source).append(":\n");
        for (Node n : graph.getNodes()) {
            var path = result.shortestPath(n.id());
            Integer d = result.distances().get(n.id());
            sb.append("  ").append(n.id()).append(": ")
                    .append(d == null ? "unreachable" : d + "  via " + String.join(" \u2192 ", path))
                    .append("\n");
        }
        return sb.toString();
    }

    private String formatDistances(Map<String, Integer> distances) {
        return distances.entrySet().stream()
                .map(e -> e.getKey() + "=" + (e.getValue() == null ? "\u221e" : e.getValue()))
                .collect(Collectors.joining(" "));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }
}
