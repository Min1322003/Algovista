package com.algovista.backend.visualization;

/** Hand-built graphs used for demos and tests. Lesson graphs will come from the database later. */
public final class DemoGraphs {

    private DemoGraphs() {
    }

    /** The graph from the Figma wireframe. */
    public static Graph figmaGraph() {
        return new Graph(false)
                .addNode(new Node("A", "A"))
                .addNode(new Node("B", "B"))
                .addNode(new Node("C", "C"))
                .addNode(new Node("D", "D"))
                .addEdge(new Edge("AB", "A", "B", 4))
                .addEdge(new Edge("AC", "A", "C", 2))
                .addEdge(new Edge("BC", "B", "C", 1))
                .addEdge(new Edge("BD", "B", "D", 5))
                .addEdge(new Edge("CD", "C", "D", 8));
    }
}
