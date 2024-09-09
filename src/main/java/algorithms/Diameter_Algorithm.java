package algorithms;

import org.jgrapht.Graph;
import org.jgrapht.graph.AbstractBaseGraph;

public abstract class Diameter_Algorithm<V,E> {
    private final Graph<V,E> graph;

    public Diameter_Algorithm(Graph<V,E> graph) {
        this.graph = graph;
    }

    public abstract Integer run();

    public Graph<V,E> getGraph() {
        return graph;
    }
}
