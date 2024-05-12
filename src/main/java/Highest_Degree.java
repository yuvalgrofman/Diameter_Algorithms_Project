import org.jgrapht.Graph;

public class Highest_Degree implements VertexChooser {
    @Override
    public <V, E> V getInitialNode(Graph<V, E> g) {
        V maxDegreeVertex = null;
        int maxDegree = 0;
        for (V v : g.vertexSet()) {
            if (maxDegreeVertex == null || g.degreeOf(v) > maxDegree) {
                maxDegreeVertex = v;
                maxDegree = g.degreeOf(v);
            }
        }
        return maxDegreeVertex;
    }
}
