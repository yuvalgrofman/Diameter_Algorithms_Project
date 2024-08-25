import org.jgrapht.Graph;
import org.jgrapht.graph.AbstractBaseGraph;

public abstract class Diameter_Algorithm<V,E> {
    private final AbstractBaseGraph<V,E> graph;

    public Diameter_Algorithm(AbstractBaseGraph<V,E> graph) {
        this.graph = graph;
    }

    public abstract Integer run();

    public AbstractBaseGraph<V,E> getGraph() {
        return graph;
    }
}
