/*
 * This software includes modifications based on code originally written by Sebastiano Vigna,
 * licensed under the terms of the GNU Lesser General Public License v2.1 or later,
 * or the Apache Software License 2.0.
 *
 * Original Copyright (C) 2016-2021 Sebastiano Vigna
 *
 * The original LGPL v2.1 license can be found at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1-standalone.html
 *
 * The original Apache License 2.0 can be found at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * The original software written by Sebastiano Vigna can be found in the class
 * it.unimi.dsi.webgraph.algo.SumSweepDirectedDiameterRadius
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * SPDX-License-Identifier: LGPL-2.1-or-later OR Apache-2.0
 *
 * Modifications made by Jonathan Moiseyev (C) 2024
 */


import StrongConnectivity.StrongConnectivityInspector;
import it.unimi.dsi.logging.ProgressLogger;

import org.javatuples.Pair;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;




public class ExactSumSweep<V, E> extends Diameter_Algorithm<V,E> {
    private final Graph<V,E> reverseGraph;
    private final int numVertices;
    
    /** The global progress logger. */
    private final ProgressLogger pl;

    /** The kind of output requested. */
    private final OutputLevel output;

    /** The map for forward eccentricity values of the vertices. */
    private final Map<V, Integer> eccF;
    /** The map for backward eccentricity values of the vertices. */
    private final Map<V, Integer> eccB;

    /**
     * <var>toCompleteF</var>.get(<var>v</var>) is <var>True</var> if and only if
     * the forward eccentricity of <var>v</var> is not guaranteed, yet.
     */
    private final Map<V, Boolean> toCompleteF; //TODO: replace with a set of the completed.
    /**
     * <var>toCompleteB</var>.get(<var>v</var>) is <var>True</var> if and only if
     * the backward eccentricity of <var>v</var> is not guaranteed, yet.
     */
    private final Map<V, Boolean> toCompleteB; //TODO: replace with a set of the completed.

    /** The set of vertices that can be radial vertices. */
    private final Set<V> accRadial;


    /** Lower bound on the diameter of the graph. */
    private int dL;
    /** Upper bound on the radius of the graph. */
    private int rU;
    /** A vertex whose eccentricity equals the diameter. */
    private V dV;
    /** A vertex whose eccentricity equals the radius. */
    private V rV;


    /** Number of iterations performed until now. */
    private int iter;
    /** Number of iteration before the radius is found. */
    private int iterR;
    /** Number of iteration before the diameter is found. */
    private int iterD;
    /** Number of iteration before all forward eccentricities are found. */
    private int iterAllF;
    /** Number of iteration before all eccentricities are found. */
    private int iterAll;


    /** Lower bound on the forward eccentricities. */
    protected final Map<V, Integer> lF;
    /** Upper bound on the forward eccentricities. */
    protected final Map<V, Integer> uF;
    /** Lower bound on the backward eccentricities. */
    protected final Map<V, Integer> lB;
    /** Upper bound on the backward eccentricities. */
    protected final Map<V, Integer> uB;




    /** Strongly connected inspector for the graph. */
    private final StrongConnectivityInspector<V,E> scc;
    /** The strongly connected components directed graph. */
    private final Graph<Graph<V, E>, DefaultEdge> sccGraph;
    /** The strongly connected components directed graph, with reversed edge. */
    private final Graph<Graph<V, E>, DefaultEdge> reverseSccGraph;


    /**
     * For each edge in the SCC graph, the corresponding edge in the graph
     */
    private final Map<DefaultEdge,E> correspondingEdges;

    /**
     * Total forward distance from already processed vertices (used as tie-break
     * for the choice of the next vertex to process).
     */
    private final Map<V, Integer> totDistF;
    /**
     * Total backward distance from already processed vertices (used as
     * tie-break for the choice of the next vertex to process).
     */
    private final Map<V, Integer> totDistB;


    /**
     * Creates a new class for computing diameter and/or radius and/or all
     * eccentricities.
     *
     * @param graph
     *            a graph.
     * @param pl
     *            a progress logger, or {@code null}.
     * @param output
     *            which output is requested: radius, diameter, radius and
     *            diameter, or all eccentricities.
     * @param accRadial
     *            the set of vertices that can be considered radial vertices. If
     *            null, the set is automatically chosen as the set of vertices
     *            that are in the biggest strongly connected component, or that
     *            are able to reach the biggest strongly connected component.
     */
    public ExactSumSweep(Graph<V, E> graph, ProgressLogger pl, OutputLevel output , Set<V> accRadial) {
        super(graph);
        this.reverseGraph = new EdgeReversedGraph<>(graph);

        this.pl = pl;

        numVertices = graph.vertexSet().size();

        eccF = graph.vertexSet().stream().collect(Collectors.toMap(v -> v, v -> 0));
        eccB = new HashMap<>(eccF);

        totDistF = graph.vertexSet().stream().collect(Collectors.toMap(v -> v, v -> 0));
        totDistB = new HashMap<>(totDistF);

        lF = new HashMap<>(totDistF);
        lB = new HashMap<>(totDistF);

        uF = graph.vertexSet().stream().collect(Collectors.toMap(v -> v, v -> numVertices + 1));
        uB = new HashMap<>(uF);

        toCompleteF = graph.vertexSet().stream().collect(Collectors.toMap(v -> v, v -> true));
        toCompleteB = new HashMap<>(toCompleteF);

        scc = new StrongConnectivityInspector<>(graph);
        sccGraph = scc.getCondensation();
        reverseSccGraph = new EdgeReversedGraph<>(sccGraph);


        this.dL = 0;
        this.rU = Integer.MAX_VALUE;
        this.output = output;
        iterR = -1;
        iterD = -1;
        iterAllF = -1;
        iterAll = -1;

        if (accRadial == null) {
            this.accRadial = computeAccRadial();

        } else if (accRadial.size() != numVertices) // TODO: maybe remove this else if
            throw new IllegalArgumentException(
                    "The size of the array of acceptable vertices must be equal to the number of nodes in the graph.");
        else {
            this.accRadial = accRadial;
        }

        correspondingEdges = calculateCorrespondingEdges();
    }


    @Override
    public Integer run() {
        compute();
        return getDiameter();
    }


    private static final boolean DEBUG = true;

    /**
     * Returns the index <var>i</var> such that
     * <code><var>vec</var>[<var>i</var>]</code> is maximum.
     *
     * @param vec
     *            the vector of which we want to compute the argMax
     * @return the value <var>i</var> such that <var>vec</var>[<var>i</var>] is
     *         maximum
     */
    public static int argMax(final double[] vec) {
        double max = Double.MIN_VALUE;
        int argMax = -1;
        for (int i = 0; i < vec.length; i++) {
            if (vec[i] > max) {
                argMax = i;
                max = vec[i];
            }
        }
        return argMax;
    }

    /**
     * Returns the vertex <var>v</var> such that <code><var>values</var>.get(<var>v</var>)</code> is
     * maximum, among all vertices such that <code><var>acc</var>.get(v)</code> is
     * true. In case of tie, the vertex maximizing <var>tieBreak</var> is chosen.
     *
     * @param values
     *            the map of which we want to compute the argMax
     * @param tieBreak
     *            the tiebreak map
     * @param acc
     *            the map used to decide if a vertex is acceptable
     * @return the vertex <var>v</var> such that <code><var>values</var>.get(<var>v</var>)</code> is
     *         maximum
     */
    private V argMax (final Map<V,Integer> values, final Map<V,Integer> tieBreak, final Map<V, Boolean> acc) {

        int max = Integer.MIN_VALUE, tieBreakForCurrentMax = Integer.MIN_VALUE;
        V argMax = null;
        for (var valuesEntry : values.entrySet()) {
            if (acc.get(valuesEntry.getKey()) && (valuesEntry.getValue() > max ||
                        (valuesEntry.getValue() == max && tieBreak.get(valuesEntry.getKey()) > tieBreakForCurrentMax))) {
                argMax = valuesEntry.getKey();
                max = valuesEntry.getValue();
                tieBreakForCurrentMax = tieBreak.get(argMax);
            }
        }
        return argMax;
    }


    /**
     * Returns the vertex <var>v</var> such that <code><var>values</var>.get(<var>v</var>)</code> is
     * minimum, among all vertices such that <code><var>acc</var>.contains(v)</code> is
     * true. In case of tie, the vertex minimizing <var>tieBreak</var> is chosen.
     *
     * @param values
     *            the map of which we want to compute the argMax
     * @param tieBreak
     *            the tiebreak map
     * @param acc
     *            the set used to decide if a vertex is acceptable
     * @return the vertex <var>v</var> such that <code><var>values</var>.get(<var>v</var>)</code> is
     *         minimum
     */
    private V argMin (final Map<V,Integer> values, final Map<V,Integer> tieBreak, final Set<V> acc) {

        int min = Integer.MAX_VALUE, tieBreakForCurrentMin = Integer.MAX_VALUE;
        V argMin = null;
        for (var valuesEntry : values.entrySet()) {
            if (acc.contains(valuesEntry.getKey()) && (valuesEntry.getValue() < min ||
                        (valuesEntry.getValue() == min && tieBreak.get(valuesEntry.getKey()) < tieBreakForCurrentMin))) {
                argMin = valuesEntry.getKey();
                min = valuesEntry.getValue();
                tieBreakForCurrentMin = tieBreak.get(argMin);
            }
        }
        return argMin;
    }


    private final static Logger LOGGER = LoggerFactory.getLogger(ExactSumSweep.class);


    /**
     * The type of output requested: radius, diameter, radius and diameter, all
     * forward eccentricities, or all (forward and backward) eccentricities.
     */
    public enum OutputLevel {
        /**
         * Computes only the radius of the graph.
         */
        RADIUS,
        /**
         * Computes only the diameter of the graph.
         */
        DIAMETER,
        /**
         * Computes both radius and diameter.
         */
        RADIUS_DIAMETER,
        /**
         * Computes the radius, the diameter, and all the forward
         * eccentricities.
         */
        ALL_FORWARD,
        /**
         * Computes the radius, the diameter, and all the (forward and backward)
         * eccentricities.
         */
        ALL
    }


    /**
     * Returns the radius of the graph, if it has already been computed
     * (otherwise, an exception is raised).
     *
     * @return the radius
     */
    public int getRadius() {
        if (iterR == -1) {
            throw new UnsupportedOperationException("The radius has not been"
                    + "computed, yet. Please, run the compute method with" + "the correct output.");
        }
        return rU;
    }

    /**
     * Returns the diameter, if it has already been computed (otherwise, an
     * exception is raised).
     *
     * @return the diameter
     */
    public int getDiameter() {
        if (iterD == -1) {
            throw new UnsupportedOperationException("The diameter has not been"
                    + "computed, yet. Please, run the compute method with" + "the correct output.");
        }
        return dL;
    }

    /**
     * Returns a radial vertex, if it has already been computed (otherwise, an
     * exception is raised).
     *
     * @return a radial vertex
     */
    public V getRadialVertex() {
        if (iterR == -1) {
            throw new UnsupportedOperationException("The radius has not been"
                    + "computed, yet. Please, run the compute method with" + "the correct output.");
        }
        return rV;
    }

    /**
     * Returns a diametral vertex, if it has already been computed (otherwise,
     * an exception is raised).
     *
     * @return a diametral vertex
     */
    public V getDiametralVertex() {
        if (iterD == -1) {
            throw new UnsupportedOperationException("The radius has not been"
                    + "computed, yet. Please, run the compute method with" + "the correct output.");
        }
        return dV;
    }

    /**
     * Returns the forward eccentricity of a vertex, if it has already been computed
     * (otherwise, an exception is raised).
     *
     * @param v
     *            the vertex
     * @return the forward eccentricity of <var>v</var>
     */
    public int getForwardEccentricity(final V v) {
        int ecc;
        if (eccF.containsKey(v) && (ecc = eccF.get(v)) >= 0) {
            return ecc;
        }
        throw new UnsupportedOperationException("The forward eccentricity of v has not been"
                + "computed, yet. Please, use the compute method with" + "the correct output.");
    }

    /**
     * Returns the backward eccentricity of a vertex, if it has already been computed
     * (otherwise, an exception is raised).
     *
     * @param v
     *            the vertex
     * @return the backward eccentricity of <var>v</var>
     */
    public int getBackwardEccentricity(final V v) {
        int ecc;
        if (eccB.containsKey(v) && (ecc = eccB.get(v)) >= 0) {
            return ecc;
        }
        throw new UnsupportedOperationException("The backward eccentricity of v has not been"
                + "computed, yet. Please, use the compute method with" + "the correct output.");
    }

    /**
     * Returns the number of iteration needed to compute the radius, if it has
     * already been computed (otherwise, an exception is raised).
     *
     * @return the number of iterations before the radius is found
     */
    public int getRadiusIterations() {
        if (iterR == -1) {
            throw new UnsupportedOperationException("The radius has not been "
                    + "computed, yet. Please, run the compute method with " + "the correct output.");
        }
        return iterR;
    }

    /**
     * Returns the number of iteration needed to compute the diameter, if it has
     * already been computed (otherwise, an exception is raised).
     *
     * @return the number of iterations before the diameter is found
     */
    public int getDiameterIterations() {
        if (iterD == -1) {
            throw new UnsupportedOperationException("The diameter has not been "
                    + "computed, yet. Please, run the compute method with the correct output.");
        }
        return iterD;
    }

    /**
     * Returns the number of iteration needed to compute all forward
     * eccentricities, if they have already been computed (otherwise, an
     * exception is raised).
     *
     * @return the number of iterations before all forward eccentricities are
     *         found
     */
    public int getAllForwardIterations() {
        if (iterAllF == -1) {
            throw new UnsupportedOperationException("All forward eccentricities have not been "
                    + " computed, yet. Please, run the compute method with the correct output.");
        }
        return iterAllF;
    }

    /**
     * Returns the number of iteration needed to compute all eccentricities, if
     * they have already been computed (otherwise, an exception is raised).
     *
     * @return the number of iterations before all eccentricities are found
     */
    public int getAllIterations() {
        if (iterAll == -1) {
            throw new UnsupportedOperationException("All eccentricities have not been "
                    + " computed, yet. Please, run the compute method with the correct output.");
        }
        return iterAll;
    }


    /**
     * Uses a heuristic to decide which is the best pivot to choose in each
     * strongly connected component, in order to perform the
     * {@link #allCCUpperBound(Map)} function.
     *
     * @return a map containing all the strongly connected sub-graphs as keys, and
     * the pivot of each strongly connected sub-graph (key), as the value for that key.
     */
    private Map<Graph<V,E>, V> findBestPivot() {
        final List<Graph<V,E>> stronglyConnectedSubGraphs = scc.getStronglyConnectedComponents();
        final int numSCCs = stronglyConnectedSubGraphs.size();

        final Map<Graph<V,E>, V> pivots = new HashMap<>(numSCCs);

        V pivot;
        long best, current;
        best = 0; //there is no need to initialize, but otherwise the compiler is mad.

        for (Graph<V,E> stronglyConnectedSubGraph : stronglyConnectedSubGraphs) {
            pivot = null;
            for (V v : stronglyConnectedSubGraph.vertexSet()) {
                if (pivot == null) {
                    pivot = v;
                    best = (long) lF.get(pivot) + lB.get(pivot) + (toCompleteF.get(pivot) ? 0 : 1) * numVertices + (toCompleteB.get(pivot) ? 0 : 1) * numVertices;
                    continue;
                }
                current = (long) lF.get(v) + lB.get(v) + (toCompleteF.get(v) ? 0 : 1) * numVertices + (toCompleteB.get(v) ? 0 : 1) * numVertices;

                if (current < best || (current == best && totDistF.get(v) + totDistB.get(v) <= totDistF.get(pivot) + totDistB.get(pivot))) {
                    pivot = v;
                    best = current;
                }
            }
            pivots.put(stronglyConnectedSubGraph, pivot);
        }



        return pivots;
    }


    /**
     * Computes and returns the set of vertices that are either in the
     * biggest strongly connected component, or that are able to reach
     * vertices in the biggest strongly connected component.
     */
    private Set<V> computeAccRadial() {
        if (numVertices == 0) {
            return null;
        }

        final Set<V> maxSizeSCC = Collections.max(scc.stronglyConnectedSets(), Comparator.comparingInt(Set::size));

        final V v = maxSizeSCC.iterator().next();
        if (v == null) {
            return null;
        }

        final Set<V> accRadialToReturn = new HashSet<>(maxSizeSCC.size());

        BreadthFirstIterator<V, E> iterator = new BreadthFirstIterator<>(reverseGraph, v);
        while (iterator.hasNext()) {
            accRadialToReturn.add(iterator.next());
        }

        return accRadialToReturn;
    }


    /**
     * Performs a (forward or backward) BFS, updating lower bounds on the
     * eccentricities of all visited vertices.
     *
     * @param start
     *            the starting vertex of the BFS
     * @param forward
     *            if <var>True</var>, the BFS is performed following the
     *            direction of edges, otherwise it is performed in the opposite
     *            direction
     */
    private void stepSumSweep(final V start, final boolean forward) {
        if (start == null) {
            return;
        }

        int eccStart;
        Map<V, Integer> l, lOther, u, uOther, totDistOther, ecc, eccOther;
        Map<V, Boolean> toComplete, toCompleteOther;

        Graph<V, E> g;

        if (forward) {
            l = lF;
            lOther = lB;
            u = uF;
            uOther = uB;
            totDistOther = totDistB;
            g = getGraph();
            ecc = eccF;
            eccOther = eccB;
            toComplete = toCompleteF;
            toCompleteOther = toCompleteB;
        } else {
            l = lB;
            lOther = lF;
            u = uB;
            uOther = uF;
            totDistOther = totDistF;
            g = reverseGraph;
            ecc = eccB;
            eccOther = eccF;
            toComplete = toCompleteB;
            toCompleteOther = toCompleteF;
        }

        BFS<V, E> bfs = new BFS<>(g, start);

        eccStart = bfs.getEcc();

        l.put(start, eccStart);
        u.put(start, eccStart);
        ecc.put(start, eccStart);
        toComplete.put(start, Boolean.FALSE);

        if (dL < eccStart) {
            dL = eccStart;
            dV = start;
        }
        if (forward) {
            if (accRadial.contains(start) && rU > eccStart) {
                rU = eccStart;
                rV = start;
            }
        }

        for (V v : g.vertexSet()) { //TODO: find a good way to check only the vertices we saw
            int vDist;
            try { // TODO: replace this try-catch (this is correlated to the previous TODO)
                vDist = bfs.getDepth(v);
            } catch (NullPointerException exception) {
                continue;
            }


            totDistOther.put(v, totDistOther.get(v) + vDist);

            if (toCompleteOther.get(v)) {
                if (lOther.get(v) < vDist) {
                    lOther.put(v, vDist);
                    if (vDist == uOther.get(v)) {
                        toCompleteOther.put(v, false);
                        eccOther.put(v, vDist);

                        if (!forward && accRadial.contains(v) && vDist < rU) {
                            rU = vDist;
                            rV = v;
                        }
                    }
                }
            }
        }
        this.iter++;
        if (pl != null)
            pl.update();
    }



    /**
     * Performs <var>iter</var> steps of the SumSweep heuristic, starting from
     * vertex <var>start</var>.
     *
     * @param start
     *            the starting vertex
     * @param iter
     *            the number of iterations
     */
    public void sumSweepHeuristic(final V start, final int iter) {

        if (DEBUG)
            LOGGER.debug("Performing initial SumSweep visit from " + start + ".");
        stepSumSweep(start, true);

        for (int i = 2; i < iter; i++) {
            if (i % 2 == 0) {
                final V v = argMax(totDistB, lB, toCompleteB);
                if (DEBUG)
                    LOGGER.debug("Performing initial SumSweep visit from " + v + ".");
                stepSumSweep(v, false);
            } else {
                final V v = argMax(totDistF, lF, toCompleteF);
                if (DEBUG)
                    LOGGER.debug("Performing initial SumSweep visit from " + v + ".");
                stepSumSweep(v, true);
            }
        }
    }


    /**
     * For each edge in the DAG of strongly connected components, finds a
     * corresponding edge in the graph. These edge are used in the
     * {@link #allCCUpperBound(Map)} function.
     *
     * @return a map that maps between edges in the scc graph, to corresponding edges in the graph.
     * If there are a number of corresponding edges, for a specific edge in the scc graph, then it will pick
     * the corresponding edge that maximizes<code>graph.outDegreeOf(source) + graph.inDegreeOf(target)</code>
     * (where the edge is from <code>source</code> to <code>target</code>).
     */
    private Map<DefaultEdge, E> calculateCorrespondingEdges() {
        final Graph<V, E> graph = getGraph();

        /*
         * the following compare is different in the original SumSweep implementation,
         * but I think this implementation is correct and the original is wrong.
         * TODO: check which implementation is better.
         */
        final Map<Pair<Graph<V, E>, Graph<V, E>>, E> correspondingEdgesAsPairs = scc.getCorrespondingEdges(Comparator.comparingInt(
                edge -> graph.outDegreeOf(graph.getEdgeSource(edge)) + graph.inDegreeOf(graph.getEdgeSource(edge))));


        final Map<DefaultEdge, E> correspondingEdges = new HashMap<>(sccGraph.edgeSet().size());
        for (DefaultEdge edge : sccGraph.edgeSet()) {
            Pair<Graph<V,E>, Graph<V,E>> edgeAsPair = new Pair<>(sccGraph.getEdgeSource(edge), sccGraph.getEdgeTarget(edge));
            correspondingEdges.put(edge, correspondingEdgesAsPairs.get(edgeAsPair));
        }

        return correspondingEdges;
    }

    /**
     * Performs a (forward or backward) BFS inside each strongly connected
     * component, starting from the pivot
     *
     * @param pivots
     *            an array containing in position <var>i</var> the pivot of
     *            the <var>i</var>th strongly connected component
     * @param forward
     *            if <var>True</var>, a forward visit is performed, otherwise a
     *            backward visit
     * @return <var>pair</var> - a pair of maps. The first map <code><var>pair</var>.getValue0()</code> contains the
     *         distance of each vertex from the pivot of its strongly connected component, while the second
     *         map <code><var>pair</var>.getValue1()</code> contains the eccentricity of the pivot of each strongly
     *         connected component.
     */
    private Pair<Map<V, Integer>, Map<Graph<V,E>, Integer>> computeDistPivot(final Map<Graph<V,E>, V> pivots, final boolean forward) {
        Graph<V,E> g;
        if (forward)
            g = getGraph();
        else
            g = reverseGraph;

        Map<V, Integer> distFromPivot = new HashMap<>(g.vertexSet().size());
        Map<Graph<V,E>, Integer> pivotEcc = new HashMap<>(pivots.size());

        BFS<V,E> bfs;
        Graph<V,E> stronglyConnectedComponent;
        for (final var entry : pivots.entrySet()) {
            stronglyConnectedComponent = entry.getKey();
            bfs = new BFS<>(stronglyConnectedComponent, entry.getValue());
            pivotEcc.put(stronglyConnectedComponent, bfs.getEcc());
            for (V v : stronglyConnectedComponent.vertexSet()) {
                distFromPivot.put(v, bfs.getDepth(v));
            }
        }
        return new Pair<>(distFromPivot, pivotEcc);
    }

    /**
     * The ComputePivotBoundsF Procedure from algorithm3 of the article of sumsweep.
     */
    void computePivotBoundsF(final Map<Graph<V,E>, V> pivots, final Map<Graph<V,E>, Integer> eccPivotF, final Map<V, Integer> distPivotF, final Map<V, Integer> distPivotB) {
    //Map<Graph<V,E>, Integer> computePivotBoundsF(final Map<Graph<V,E>, V> pivots, final Map<Graph<V,E>, Integer> eccPivotF, final Map<V, Integer> distPivotF, final Map<V, Integer> distPivotB) {
        // Map<Graph<V,E>, Integer> uPivotF = new HashMap<>(scc.getStronglyConnectedComponents().size());

        V currentPivot, start, end;
        Graph<V,E> currentScc, endScc;
        int currentVal, uFVal;

        TopologicalOrderIterator<Graph<V,E>, DefaultEdge> it = new TopologicalOrderIterator<>(reverseSccGraph);

        while (it.hasNext()) { // TODO: make sure that the order whe are doing is correct
            currentScc = it.next();
            currentPivot = pivots.get(currentScc);

            currentVal = eccPivotF.get(currentScc);
            uFVal = uF.get(currentPivot);
            if (currentVal >= uFVal) {
                eccPivotF.put(currentScc, uFVal);
                continue;
            }

            for (var edge : sccGraph.edgesOf(currentScc)) {
                endScc = sccGraph.getEdgeTarget(edge);
                var correspondingEdge = correspondingEdges.get(edge);
                start = getGraph().getEdgeSource(correspondingEdge);
                end = getGraph().getEdgeTarget(correspondingEdge);

                currentVal = Math.max(currentVal, distPivotF.get(start) + 1 + distPivotB.get(end) + eccPivotF.get(endScc));
                if (currentVal >= uFVal) {
                    currentVal = uFVal;
                    break;
                }
            }

            eccPivotF.put(currentScc, currentVal);
        }
    }


    /**
     * The ComputePivotBoundsB Procedure from algorithm3 of the article of sumsweep.
     */
    void computePivotBoundsB(final Map<Graph<V,E>, V> pivots, final Map<Graph<V,E>, Integer> eccPivotB, final Map<V, Integer> distPivotF, final Map<V, Integer> distPivotB) {
        // Map<Graph<V,E>, Integer> uPivotB = new HashMap<>(scc.getStronglyConnectedComponents().size());

        V currentPivot, start, end;
        Graph<V,E> currentScc, endScc;
        int currentVal, uBVal;

        TopologicalOrderIterator<Graph<V,E>, DefaultEdge> it = new TopologicalOrderIterator<>(sccGraph);

        while (it.hasNext()) { // TODO: make sure that the order whe are doing is correct
            currentScc = it.next();
            currentPivot = pivots.get(currentScc);

            currentVal = eccPivotB.get(currentScc);
            uBVal = uB.get(currentPivot);
            if (currentVal >= uBVal) {
                eccPivotB.put(currentScc, uBVal);
                continue;
            }

            for (var edge : sccGraph.edgesOf(currentScc)) {
                endScc = sccGraph.getEdgeTarget(edge);
                var correspondingEdge = correspondingEdges.get(edge);
                start = getGraph().getEdgeSource(correspondingEdge);
                end = getGraph().getEdgeTarget(correspondingEdge);

                currentVal = Math.max(currentVal, distPivotB.get(end) + 1 + distPivotF.get(start) + eccPivotB.get(endScc));
                if (currentVal >= uBVal) {
                    currentVal = uBVal;
                    break;
                }
            }

            eccPivotB.put(currentScc, currentVal);
        }
    }


    /**
     * Performs a step of the ExactSumSweep algorithm, by performing the
     * {@link #allCCUpperBound(Map)} function (see the paper for more details).
     *
     * @param pivots
     *            a map between the strongly connected components, and their pivots
     */
    private void allCCUpperBound(final Map<Graph<V,E>, V> pivots) {
        final var distEccForward = computeDistPivot(pivots, true);
        final Map<V, Integer> distPivotF = distEccForward.getValue0();
        Map<Graph<V,E>, Integer> eccPivotF = distEccForward.getValue1();

        final var distEccBackward = computeDistPivot(pivots, false);
        final Map<V, Integer> distPivotB = distEccBackward.getValue0();
        Map<Graph<V,E>, Integer> eccPivotB = distEccBackward.getValue1();

        computePivotBoundsF(pivots, eccPivotF, distPivotF, distPivotB);
        computePivotBoundsB(pivots, eccPivotB, distPivotF, distPivotB);

        int ufValue, pivotEccF, newUfValue, uBValue, pivotEccB, newUbValue;


        for (Graph<V,E> currentScc : scc.getStronglyConnectedComponents()) {
            pivotEccF = eccPivotF.get(currentScc);
            pivotEccB = eccPivotB.get(currentScc);

            for (V v : currentScc.vertexSet()) {
                if (toCompleteF.get(v)) {
                    ufValue = uF.get(v);
                    newUfValue = distPivotB.get(v) + pivotEccF;

                    if (newUfValue < ufValue) {
                        uF.put(v, newUfValue);

                        if (lF.get(v) == newUfValue) {
                            toCompleteF.put(v, false);
                            eccF.put(v, newUfValue);

                            if (accRadial.contains(v)) {
                                if (newUfValue < rU) {
                                    rU = newUfValue;
                                    rV = v;
                                }
                            }
                        }
                    }
                }

                if (toCompleteB.get(v)) {
                    uBValue = uB.get(v);
                    newUbValue = distPivotF.get(v) + pivotEccB;

                    if (newUbValue < uBValue) {
                        uB.put(v, newUbValue);

                        if (lB.get(v) == newUbValue) {
                            toCompleteB.put(v, false);
                            eccB.put(v, newUbValue);
                        }
                    }
                }
            }
        }

        this.iter += 3;
    }


    /**
     * Computes how many nodes are still to be processed, before outputting the
     * result
     *
     * @return the number of nodes to be processed
     */
    private int findMissingNodes() {
        int missingR = 0, missingDF = 0, missingDB = 0, missingAllF = 0, missingAllB = 0;
        final int dL = this.dL;
        final int rU = this.rU;

        for (V v : getGraph().vertexSet()) {
            if (toCompleteF.get(v)) {
                missingAllF++;
                if (uF.get(v) > dL) {
                    missingDF++;
                }
                if (accRadial.contains(v) && lF.get(v) < rU) {
                    missingR++;
                }
            }
            if (toCompleteB.get(v)) {
                missingAllB++;
                if (uB.get(v) > dL) {
                    missingDB++;
                }
            }
        }
        if (missingR == 0 && iterR == -1) {
            iterR = iter;
        }
        if ((missingDF == 0 || missingDB == 0) && iterD == -1) {
            iterD = iter;
        }
        if (missingAllF == 0 && iterAllF == -1)
            iterAllF = iter;
        if (missingAllF == 0 && missingAllB == 0)
            iterAll = iter;

        return switch (output) {
            case RADIUS -> missingR;
            case DIAMETER -> Math.min(missingDF, missingDB);
            case RADIUS_DIAMETER -> missingR + Math.min(missingDF, missingDB);
            case ALL_FORWARD -> missingAllF;
            default -> missingAllF + missingAllB;
        };
    }


    /**
     * Computes diameter, radius, and/or all eccentricities. Results can be
     * accessed by methods such as {@link #getDiameter()},
     * {@link #getRadialVertex()},
     * {@link #getForwardEccentricity(V)}, and
     * {@link #getBackwardEccentricity(V)}.
     */
    public void compute() {
        if (pl != null) {
            pl.start("Starting visits...");
            pl.itemsName = "nodes";
            pl.displayLocalSpeed = true;
        }
        V maxDegreeVertex = (new Highest_Degree()).getInitialNode(getGraph());

        sumSweepHeuristic(maxDegreeVertex, 6);

        final double[] points = new double[6];
        int missingNodes = findMissingNodes(), oldMissingNodes = missingNodes;

        Arrays.fill(points, numVertices);

        while (missingNodes > 0) {

            final int stepToPerform = argMax(points);

            switch (stepToPerform) {
                case 0:
                    if (DEBUG)
                        LOGGER.debug("Performing AllCCUpperBound.");
                    this.allCCUpperBound(findBestPivot());
                    break;
                case 1:
                    if (DEBUG)
                        LOGGER.debug("Performing a forward BFS, from a vertex maximizing the upper bound.");
                    this.stepSumSweep(argMax(uF, totDistF, toCompleteF), true);
                    break;
                case 2:
                    if (DEBUG)
                        LOGGER.debug("Performing a forward BFS, from a vertex minimizing the lower bound.");
                    this.stepSumSweep(argMin(lF, totDistF, accRadial), true);
                    break;
                case 3:
                    if (DEBUG)
                        LOGGER.debug("Performing a backward BFS, from a vertex maximizing the upper bound.");
                    this.stepSumSweep(argMax(uB, totDistB, toCompleteB), false);
                    break;
                case 4:
                    if (DEBUG)
                        LOGGER.debug("Performing a backward BFS, from a vertex maximizing the distance sum.");
                    this.stepSumSweep(argMax(totDistB, uB, toCompleteB), false);
                    break;
                case 5:
                    if (DEBUG)
                        LOGGER.debug("Performing a forward BFS, from a vertex maximizing the distance sum.");
                    this.stepSumSweep(argMax(totDistF, uF, toCompleteF), false);
                    break;
            }
            oldMissingNodes = missingNodes;
            missingNodes = this.findMissingNodes();
            points[stepToPerform] = oldMissingNodes - missingNodes;

            for (int j = 0; j < points.length; j++) {
                if (j != stepToPerform && points[j] >= 0) {
                    points[j] = points[j] + 2.0 / iter;
                }
            }
            if (DEBUG)
                LOGGER.debug("    Missing nodes: " + missingNodes + "/" + 2 * numVertices + ".");
        }
        if (DEBUG) {
            if (this.output == OutputLevel.RADIUS || this.output == OutputLevel.RADIUS_DIAMETER)
                LOGGER.debug("Radius: " + rU + " (" + iterR + " iterations).");
            if (this.output == OutputLevel.DIAMETER || this.output == OutputLevel.RADIUS_DIAMETER)
                LOGGER.debug("Diameter: " + dL + " (" + iterD + " iterations).");
        }
        if (pl != null)
            pl.done();
    }
}











