package algorithms;

import org.jgrapht.Graph;
import org.jgrapht.traverse.BreadthFirstIterator;

import java.util.HashSet;
import java.util.Set;

public class NeighborsBFS<V,E> {

        private final Graph<V,E> graph;
        private final V start;
        private final BreadthFirstIterator<V,E> iterator;
        private final Set<V> neighbors;
        private final int depth;

        public NeighborsBFS(Graph<V,E> graph, V start, int numNeighbors) {
            this.graph = graph;
            this.start = start;

            iterator = new BreadthFirstIterator<>(graph, start);
            Set<V> temp = new HashSet<>();

            V current = start;
            for (int i = 0; i < numNeighbors; i++) {
                current = iterator.next();
                temp.add(current);
            }

            depth = iterator.getDepth(current);
            neighbors = temp;
        }

        public Set<V> getNeighbors() {
            return neighbors;
        }

        public int getDepth() {
            return depth;
        }
}

