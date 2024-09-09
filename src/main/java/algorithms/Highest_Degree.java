package algorithms;

import org.jgrapht.Graph;

public class Highest_Degree implements VertexChooser {
    public static enum Option {
        OUT_DEGREE,
        IN_DEGREE,
        TOTAL_DEGREE
    }

    private final Option option;

    public Highest_Degree(Option option) {
        this.option = option;
    }

    public Highest_Degree() {
        this(Option.TOTAL_DEGREE);
    }

    @Override
    public <V, E> V getInitialNode(Graph<V, E> g) {
        V maxDegreeVertex = null;
        int maxDegree = 0;
        for (V v : g.vertexSet()) {

            int degree = switch (option) {
                case IN_DEGREE -> g.inDegreeOf(v);
                case OUT_DEGREE -> g.outDegreeOf(v);
                default -> g.degreeOf(v);
            };

            if (maxDegreeVertex == null || degree > maxDegree) {
                maxDegreeVertex = v;
                maxDegree = degree;
            }
        }
        return maxDegreeVertex;
    }
}
