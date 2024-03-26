import org.javatuples.Pair;
import org.jgrapht.Graph;

public interface VertexChooser {
    <V, E> V getInitialNode(Graph<V, E> g);
}
