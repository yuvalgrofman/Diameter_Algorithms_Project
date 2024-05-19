import org.jgrapht.Graph;
import org.jgrapht.traverse.BreadthFirstIterator;

import java.util.HashSet;
import java.util.Set;

public class Layered_BFS<V,E> {
    private final Graph<V,E> graph;
    private final V start;
    private final BreadthFirstIterator<V,E> iterator;
    private final Set<V> layer;

    public Layered_BFS(Graph<V,E> graph, V start, int layer_depth) {
        this.graph = graph;
        this.start = start;

        iterator = new BreadthFirstIterator<>(graph, start);
        Set<V> temp = new HashSet<>();

        V current = start;
        while (iterator.hasNext() && iterator.getDepth(current) <= layer_depth) {
            if (iterator.getDepth(current) == layer_depth) {
                temp.add(current);
            }
            current = iterator.next();
        }

        layer = temp;
    }

    public Set<V> getLayer() {
        return layer;
    }
}
