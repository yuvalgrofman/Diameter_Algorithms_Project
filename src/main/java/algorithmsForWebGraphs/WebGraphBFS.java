package algorithmsForWebGraphs;

import it.unimi.dsi.webgraph.ImmutableGraph;

import java.util.*;

public class WebGraphBFS {

    /**
     * a class to store the results of a bfs.
     */
    public static class BFSResult {
        private final int furthestVertex;
        private final int maxDepth;

        public BFSResult(int furthestVertex, int maxDepth) {
            this.furthestVertex = furthestVertex;
            this.maxDepth = maxDepth;
        }

        public int getFurthestVertex() {
            return furthestVertex;
        }

        public int getMaxDepth() {
            return maxDepth;
        }

        @Override
        public String toString() {
            return "Furthest Vertex: " + furthestVertex + ", Max Depth: " + maxDepth;
        }
    }


    /**
     * doing a bfs while initializing the queue of the bfs with the given LinkedList
     *
     * @param graph The immutable graph.
     * @param initQueue The verticies to initiallize the queue with
     * @return the result og the bfs
     */
    public static BFSResult bfsFromQueue(ImmutableGraph graph, LinkedList<Integer> initQueue) {
        if (initQueue.isEmpty()) {
            throw new IllegalArgumentException("initQueue cannot be empty.");
        }

        Queue<Integer> queue = new LinkedList<>(initQueue);
        Set<Integer> visited = new HashSet<>(initQueue);
        Map<Integer, Integer> distances = new HashMap<>();

        // Initialize distances for all nodes in the hittingSet
        for (int node : initQueue) {
            distances.put(node, 0);
        }

        int furthestVertex = -1;
        int maxDistance = -1;

        // BFS from all nodes in the hittingSet
        while (!queue.isEmpty()) {
            int current = queue.poll();
            int currentDistance = distances.get(current);

            int[] neighbors = graph.successorArray(current);
            for (int neighbor : neighbors) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                    distances.put(neighbor, currentDistance + 1);

                    // Check if this is the furthest vertex found so far
                    if (currentDistance + 1 > maxDistance) {
                        maxDistance = currentDistance + 1;
                        furthestVertex = neighbor;
                    }
                }
            }
        }

        return new BFSResult(furthestVertex, maxDistance);
    }



    /**
     * Performs BFS on the graph starting from a given node and returns the eccentricity.
     *
     * @param graph The immutable graph.
     * @param startNode The node to start the BFS from.
     * @return The eccentricity of the startNode (i.e., the maximum depth reached).
     */
    public static int calculateEccentricity2(ImmutableGraph graph, int startNode) {
        Queue<Integer> queue = new LinkedList<>();
        Set<Integer> visited = new HashSet<>();
        Map<Integer, Integer> distances = new HashMap<>();

        queue.add(startNode);
        visited.add(startNode);
        distances.put(startNode, 0);

        int eccentricity = 0;

        // BFS from the startNode
        while (!queue.isEmpty()) {
            int current = queue.poll();
            int currentDistance = distances.get(current);

            int[] neighbors = graph.successorArray(current);
            for (int neighbor : neighbors) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                    distances.put(neighbor, currentDistance + 1);

                    // Update eccentricity (maximum depth reached)
                    eccentricity = Math.max(eccentricity, currentDistance + 1);
                }
            }
        }

        return eccentricity;
    }


    public static int calculateEccentricity(ImmutableGraph graph, int startNode) {
        Queue<Integer> queue = new LinkedList<>();
        boolean[] visited = new boolean[graph.numNodes()];
        int[] distances = new int[graph.numNodes()];

        queue.add(startNode);
        visited[startNode] = true;
        distances[startNode] = 0;

        int eccentricity = 0;

        // BFS from the startNode
        while (!queue.isEmpty()) {
            int current = queue.poll();
            int currentDistance = distances[current];

            int[] neighbors = graph.successorArray(current);
            int degree = graph.outdegree(current);

            for (int i = 0; i < degree; i++) {
                int neighbor = neighbors[i];
                if (!visited[neighbor]) {
                    visited[neighbor] = true;
                    queue.add(neighbor);
                    distances[neighbor] = currentDistance + 1;

                    // Update eccentricity (maximum depth reached)
                    eccentricity = Math.max(eccentricity, distances[neighbor]);
                }
            }
        }

        return eccentricity;
    }
}
