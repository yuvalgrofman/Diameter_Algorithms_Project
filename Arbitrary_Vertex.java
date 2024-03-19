import org.jgrapht.Graph;

public class Arbitrary_Vertex implements VertexChooser {
    @Override
    public <V, E> V getInitialNode(Graph<V, E> g) {
        return g.vertexSet().iterator().next();
    }
}
