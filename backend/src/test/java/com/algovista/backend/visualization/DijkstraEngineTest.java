package com.algovista.backend.visualization;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

class DijkstraEngineTest {

    private final DijkstraEngine engine = new DijkstraEngine();

    @Test
    void computesShortestDistancesOnFigmaGraph() {
        DijkstraResult r = engine.run(DemoGraphs.figmaGraph(), "A");

        assertThat(r.distances().get("A")).isEqualTo(0);
        assertThat(r.distances().get("B")).isEqualTo(3); // A -> C -> B = 2 + 1
        assertThat(r.distances().get("C")).isEqualTo(2);
        assertThat(r.distances().get("D")).isEqualTo(8); // A -> C -> B -> D = 2 + 1 + 5
    }

    @Test
    void visitsNodesInExpectedOrder() {
        DijkstraResult r = engine.run(DemoGraphs.figmaGraph(), "A");
        assertThat(r.visitOrder()).containsExactly("A", "C", "B", "D");
    }

    @Test
    void reconstructsShortestPath() {
        DijkstraResult r = engine.run(DemoGraphs.figmaGraph(), "A");
        assertThat(r.shortestPath("D")).containsExactly("A", "C", "B", "D");
        assertThat(r.shortestPath("A")).containsExactly("A");
    }

    @Test
    void firstStepInitializesDistances() {
        DijkstraStep first = engine.run(DemoGraphs.figmaGraph(), "A").steps().get(0);

        assertThat(first.type()).isEqualTo(StepType.INITIALIZE);
        assertThat(first.distances().get("A")).isEqualTo(0);
        assertThat(first.distances().get("B")).isNull(); // null = infinity
        assertThat(first.nodeStates().get("A")).isEqualTo(NodeState.SOURCE);
    }

    @Test
    void recordsAnImprovingRelaxation() {
        List<DijkstraStep> steps = engine.run(DemoGraphs.figmaGraph(), "A").steps();

        DijkstraStep step = steps.stream()
                .filter(s -> s.type() == StepType.RELAX_EDGE
                        && "C".equals(s.currentNodeId())
                        && "B".equals(s.neighborNodeId()))
                .findFirst()
                .orElseThrow();

        assertThat(step.updated()).isTrue();
        assertThat(step.candidateDistance()).isEqualTo(3);
        assertThat(step.distances().get("B")).isEqualTo(3); // improved from 4
        assertThat(step.nodeStates().get("B")).isEqualTo(NodeState.NEIGHBOR);
        assertThat(step.nodeStates().get("C")).isEqualTo(NodeState.CURRENT);
    }

    @Test
    void recordsANonImprovingRelaxation() {
        // A->C is 5. A->B is 2. B->C is 8.
        // Path A->B->C is 10, which is worse than the direct path A->C (5).
        Graph g = new Graph(false)
                .addNode(new Node("A", "A"))
                .addNode(new Node("B", "B"))
                .addNode(new Node("C", "C"))
                .addEdge(new Edge("AB", "A", "B", 2))
                .addEdge(new Edge("AC", "A", "C", 5))
                .addEdge(new Edge("BC", "B", "C", 8));

        List<DijkstraStep> steps = engine.run(g, "A").steps();

        DijkstraStep step = steps.stream()
                .filter(s -> s.type() == StepType.RELAX_EDGE
                        && "B".equals(s.currentNodeId())
                        && "C".equals(s.neighborNodeId()))
                .findFirst()
                .orElseThrow();

        assertThat(step.updated()).isFalse(); // 2 + 8 = 10, which is not less than 5
        assertThat(step.distances().get("C")).isEqualTo(5);
    }

    @Test
    void lastStepIsFinishedAndAllReachableNodesVisited() {
        List<DijkstraStep> steps = engine.run(DemoGraphs.figmaGraph(), "A").steps();
        DijkstraStep last = steps.get(steps.size() - 1);

        assertThat(last.type()).isEqualTo(StepType.FINISHED);
        assertThat(last.visited()).containsExactly("A", "C", "B", "D");
    }

    @Test
    void unreachableNodeStaysInfinite() {
        Graph g = DemoGraphs.figmaGraph().addNode(new Node("E", "E"));
        DijkstraResult r = engine.run(g, "A");

        assertThat(r.distances().get("E")).isNull();
        assertThat(r.shortestPath("E")).isEmpty();
        assertThat(r.visitOrder()).doesNotContain("E");
    }

    @Test
    void directedGraphRespectsEdgeDirection() {
        Graph g = new Graph(true)
                .addNode(new Node("A", "A"))
                .addNode(new Node("B", "B"))
                .addEdge(new Edge("AB", "A", "B", 3));

        assertThat(engine.run(g, "A").distances().get("B")).isEqualTo(3);
        assertThat(engine.run(g, "B").distances().get("A")).isNull();
    }

    @Test
    void rejectsUnknownSourceAndNegativeWeights() {
        assertThatThrownBy(() -> engine.run(DemoGraphs.figmaGraph(), "Z"))
                .isInstanceOf(IllegalArgumentException.class);

        Graph g = new Graph(false).addNode(new Node("A", "A")).addNode(new Node("B", "B"));
        assertThatThrownBy(() -> g.addEdge(new Edge("AB", "A", "B", -1)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
