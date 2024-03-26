import org.jgrapht.Graph;

public class Highest_Degree implements VertexChooser {
    @Override
    public <V, E> V getInitialNode(Graph<V, E> g) {
        V maxDegreeVertex = null;
        for (V v : g.vertexSet()) {
            if (g.degreeOf(v) > g.degreeOf(maxDegreeVertex)) {
                maxDegreeVertex = v;
            }
        }
        return maxDegreeVertex;
    }
}
