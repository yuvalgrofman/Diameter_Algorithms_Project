package algorithmsForWebGraphs;

import it.unimi.dsi.webgraph.ImmutableGraph;
import org.javatuples.Pair;

import java.util.LinkedList;
import java.util.Queue;

public class FourSweepForWebGraphs {
    private final int s;
    private final ImmutableGraph g;

    public FourSweepForWebGraphs(ImmutableGraph g, int s) {
        this.g = g;
        this.s = s;
    }

    public Pair<Integer, Integer> four_sweep() {
        BFSResult bfs1 = bfs(s);
        int a1 = bfs1.getFurthest();

        BFSResult bfs2 = bfs(a1);
        int b1 = bfs2.getFurthest();
        int r2 = bfs2.getMid(b1);
        int ecc_a1 = bfs2.getDepth(b1);

        BFSResult bfs3 = bfs(r2);
        int a2 = bfs3.getFurthest();

        BFSResult bfs4 = bfs(a2);
        int b2 = bfs4.getFurthest();
        int r1 = bfs4.getMid(b2);
        int ecc_a2 = bfs4.getDepth(b2);

        return new Pair<>(r1, Math.max(ecc_a1, ecc_a2));
    }

    private BFSResult bfs(int source) {
        int[] distances = new int[g.numNodes()];
        int[] predecessors = new int[g.numNodes()];
        for (int i = 0; i < distances.length; i++) {
            distances[i] = -1;
            predecessors[i] = -1;
        }
        distances[source] = 0;

        Queue<Integer> queue = new LinkedList<>();
        queue.add(source);

        int furthestNode = source;
        int maxDistance = 0;

        while (!queue.isEmpty()) {
            int current = queue.poll();
            int[] successors = g.successorArray(current);
            int degree = g.outdegree(current);

            for (int i = 0; i < degree; i++) {
                int neighbor = successors[i];
                if (distances[neighbor] == -1) { // If not visited
                    distances[neighbor] = distances[current] + 1;
                    predecessors[neighbor] = current;
                    queue.add(neighbor);
                    if (distances[neighbor] > maxDistance) {
                        maxDistance = distances[neighbor];
                        furthestNode = neighbor;
                    }
                }
            }
        }

        return new BFSResult(furthestNode, maxDistance, distances, predecessors);
    }

    private static class BFSResult {
        private final int furthestNode;
        private final int maxDistance;
        private final int[] distances;
        private final int[] predecessors;

        public BFSResult(int furthestNode, int maxDistance, int[] distances, int[] predecessors) {
            this.furthestNode = furthestNode;
            this.maxDistance = maxDistance;
            this.distances = distances;
            this.predecessors = predecessors;
        }

        public int getFurthest() {
            return furthestNode;
        }

        public int getDepth(int node) {
            return distances[node];
        }

        public int getMid(int target) {
            int pathLength = distances[target];
            int midPoint = pathLength / 2;

            int currentNode = target;
            for (int i = 0; i < midPoint; i++) {
                currentNode = predecessors[currentNode];
            }
            return currentNode;
        }
    }
}
