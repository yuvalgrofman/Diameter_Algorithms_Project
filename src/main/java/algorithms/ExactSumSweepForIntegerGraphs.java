package algorithms;/*
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


import algorithms.ArrayMap.ArrayMap;
import algorithms.StrongConnectivity.StrongConnectivityCondenser;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.logging.ProgressLogger;
import org.javatuples.Pair;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.List;

import static java.lang.System.exit;


public class ExactSumSweepForIntegerGraphs extends Diameter_Algorithm<Integer, IntIntPair> {
    private final Graph<Integer, IntIntPair> reverseGraph;
    private final int numVertices;

    /** The global progress logger. */
    private final ProgressLogger pl;

    /** The kind of output requested. */
    private final OutputLevel output;

    /** The map for forward eccentricity values of the vertices. */
    private final Map<Integer, Integer> eccF;
    /** The map for backward eccentricity values of the vertices. */
    private final Map<Integer, Integer> eccB;

    /**
     * <var>toCompleteF</var>.get(<var>v</var>) is <var>True</var> if and only if
     * the forward eccentricity of <var>v</var> is not guaranteed, yet.
     */
    private final Map<Integer, Boolean> toCompleteF; //TODO: replace with a set of the completed.
    /**
     * <var>toCompleteB</var>.get(<var>v</var>) is <var>True</var> if and only if
     * the backward eccentricity of <var>v</var> is not guaranteed, yet.
     */
    private final Map<Integer, Boolean> toCompleteB; //TODO: replace with a set of the completed.

    /** The set of vertices that can be radial vertices. */
    private final Set<Integer> accRadial;


    /** Lower bound on the diameter of the graph. */
    private int dL;
    /** Upper bound on the radius of the graph. */
    private int rU;
    /** A vertex whose eccentricity equals the diameter. */
    private Integer dV;
    /** A vertex whose eccentricity equals the radius. */
    private Integer rV;


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
    protected final Map<Integer, Integer> lF;
    /** Upper bound on the forward eccentricities. */
    protected final Map<Integer, Integer> uF;
    /** Lower bound on the backward eccentricities. */
    protected final Map<Integer, Integer> lB;
    /** Upper bound on the backward eccentricities. */
    protected final Map<Integer, Integer> uB;




    /** Strongly connected inspector for the graph. */
    private final StrongConnectivityCondenser<Integer, IntIntPair> scc;
    /** The strongly connected components directed graph. */
    private final Graph<Integer, IntIntPair> sccGraph;
    /** The strongly connected components directed graph, with reversed edge. */
    // private final Graph<Integer, IntIntPair> reverseSccGraph;


    /**
     * For each edge in the SCC graph, the corresponding edge in the graph
     */
    private final Map<IntIntPair, IntIntPair> correspondingEdges;

    /**
     * Total forward distance from already processed vertices (used as tie-break
     * for the choice of the next vertex to process).
     */
    private final Map<Integer, Integer> totDistF;
    /**
     * Total backward distance from already processed vertices (used as
     * tie-break for the choice of the next vertex to process).
     */
    private final Map<Integer, Integer> totDistB;


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
    public ExactSumSweepForIntegerGraphs(AbstractBaseGraph<Integer, IntIntPair> graph, ProgressLogger pl, OutputLevel output , Set<Integer> accRadial) {
        super(graph);
        this.reverseGraph = new EdgeReversedGraph<>(graph);

        this.pl = pl;

        numVertices = graph.vertexSet().size();

        final int largestVertex = Collections.max(graph.vertexSet());
        eccF = new ArrayMap<Integer>(largestVertex + 1);
        eccB = new ArrayMap<Integer>(largestVertex + 1);


        totDistF = new ArrayMap<Integer>(largestVertex + 1);
        totDistB = new ArrayMap<Integer>(largestVertex + 1);

        lF = new ArrayMap<Integer>(largestVertex + 1);
        lB = new ArrayMap<Integer>(largestVertex + 1);

        uF = new ArrayMap<Integer>(largestVertex + 1);
        uB = new ArrayMap<Integer>(largestVertex + 1);

        toCompleteF = new ArrayMap<Boolean>(largestVertex + 1);
        toCompleteB = new ArrayMap<Boolean>(largestVertex + 1);

        for (int i = 0; i < largestVertex + 1; i++) {
            eccF.put(i, 0);
            eccB.put(i, 0);
            totDistF.put(i, 0);
            totDistB.put(i, 0);
            lF.put(i, 0);
            lB.put(i, 0);
            uF.put(i, numVertices + 1);
            uB.put(i, numVertices + 1);

            toCompleteF.put(i, false);
            toCompleteB.put(i, false);
        }

        for (Integer v : getGraph().vertexSet()) {
            toCompleteF.put(v, true);
            toCompleteB.put(v, true);
        }
        System.out.println("a");
        scc = new StrongConnectivityCondenser<>(graph);
        System.out.println("a");

        sccGraph = scc.condense();
        System.out.println("a");

        //reverseSccGraph = new EdgeReversedGraph<>(sccGraph);


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
        System.out.println("a");

        correspondingEdges = calculateCorrespondingEdges();
        System.out.println("a");

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
//        final double EPSILON = 0.001;
//
//        double max = Double.MIN_VALUE;
//        int argMax = -1;
//        int numMax = 0;
//        for (int i = 0; i < vec.length; i++) {
//            if (vec[i] >= max + EPSILON) {
//                argMax = i;
//                max = vec[i];
//                numMax = 1;
//            } else if (Math.abs(vec[i] - max) <= EPSILON) {
//                numMax++;
//            }
//
//        }
//        if (numMax > 1) {
//            int rand = (int) (Math.random() * numMax);
//            int j = 0, i;
//            for (i = 0; true; i++) {
//                if (Math.abs(vec[i] - max) <= EPSILON) {
//                    j++;
//                    if (j >= rand) break;
//                }
//            }
//            return i;
//        }
//
//        return argMax;


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
    private Integer argMax (final Map<Integer,Integer> values, final Map<Integer,Integer> tieBreak, final Map<Integer, Boolean> acc) {

        int max = Integer.MIN_VALUE, tieBreakForCurrentMax = Integer.MIN_VALUE;
        Integer argMax = null;
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
    private Integer argMin (final Map<Integer,Integer> values, final Map<Integer,Integer> tieBreak, final Set<Integer> acc) {

        int min = Integer.MAX_VALUE, tieBreakForCurrentMin = Integer.MAX_VALUE;
        Integer argMin = null;
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


    private final static Logger LOGGER = LoggerFactory.getLogger(ExactSumSweepForIntegerGraphs.class);


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
    public Integer getRadialVertex() {
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
    public Integer getDiametralVertex() {
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
    public int getForwardEccentricity(final Integer v) {
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
    public int getBackwardEccentricity(final Integer v) {
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
     * {@link #allCCUpperBound(ArrayMap)} function.
     *
     * @return a map containing all the strongly connected sub-graphs as keys, and
     * the pivot of each strongly connected sub-graph (key), as the value for that key.
     */
    private ArrayMap<Integer> findBestPivot() {
        final List<Set<Integer>> stronglyConnectedSets = scc.stronglyConnectedSets();
        final int numSCCs = stronglyConnectedSets.size();

        final ArrayMap<Integer> pivots = new ArrayMap<Integer>(numSCCs);

        Integer pivot;
        long best, current;
        best = 0; //there is no need to initialize, but otherwise the compiler is mad.
        Set<Integer> stronglyConnectedSet;
        for (int i = 0; i < numSCCs; i++) {
            stronglyConnectedSet = stronglyConnectedSets.get(i);
            pivot = null;
            for (Integer v : stronglyConnectedSet) {
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
            pivots.put(i, pivot);
        }


        return pivots;
    }


    /**
     * Computes and returns the set of vertices that are either in the
     * biggest strongly connected component, or that are able to reach
     * vertices in the biggest strongly connected component.
     */
    private Set<Integer> computeAccRadial() {
        if (numVertices == 0) {
            return null;
        }

        final Set<Integer> maxSizeSCC = Collections.max(scc.stronglyConnectedSets(), Comparator.comparingInt(Set::size));

        final Integer v = maxSizeSCC.iterator().next();
        if (v == null) {
            return null;
        }

        final Set<Integer> accRadialToReturn = new HashSet<>(maxSizeSCC.size());

        BreadthFirstIterator<Integer, IntIntPair> iterator = new BreadthFirstIterator<>(reverseGraph, v);
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
    private void stepSumSweep(final Integer start, final boolean forward) {
        if (start == null) {
            return;
        }

        int eccStart;
        Map<Integer, Integer> l, lOther, u, uOther, totDistOther, ecc, eccOther;
        Map<Integer, Boolean> toComplete, toCompleteOther;

        Graph<Integer, IntIntPair> g;

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

        BFS<Integer, IntIntPair> bfs = new BFS<>(g, start);

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

        for (Integer v : g.vertexSet()) { //TODO: find a good way to check only the vertices we saw
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
     * Performs <var>iter</var> steps of the algorithms.SumSweep heuristic, starting from
     * vertex <var>start</var>.
     *
     * @param start
     *            the starting vertex
     * @param iter
     *            the number of iterations
     */
    public void sumSweepHeuristic(final Integer start, final int iter) {

        if (DEBUG)
            LOGGER.debug("Performing initial algorithms.SumSweep visit from " + start + ".");
        stepSumSweep(start, true);

        for (int i = 2; i < iter; i++) {
            if (i % 2 == 0) {
                final Integer v = argMax(totDistB, lB, toCompleteB);
                if (DEBUG)
                    LOGGER.debug("Performing initial algorithms.SumSweep visit from " + v + ".");
                stepSumSweep(v, false);
            } else {
                final Integer v = argMax(totDistF, lF, toCompleteF);
                if (DEBUG)
                    LOGGER.debug("Performing initial algorithms.SumSweep visit from " + v + ".");
                stepSumSweep(v, true);
            }
        }
    }


    /**
     * For each edge in the DAG of strongly connected components, finds a
     * corresponding edge in the graph. These edge are used in the
     * {@link #allCCUpperBound(ArrayMap)} function.
     *
     * @return a map that maps between edges in the scc graph, to corresponding edges in the graph.
     * If there are a number of corresponding edges, for a specific edge in the scc graph, then it will pick
     * the corresponding edge that maximizes<code>graph.outDegreeOf(source) + graph.inDegreeOf(target)</code>
     * (where the edge is from <code>source</code> to <code>target</code>).
     */
    private Map<IntIntPair, IntIntPair> calculateCorrespondingEdges() {
        final Graph<Integer, IntIntPair> graph = getGraph();

        /*
         * the following compare is different in the original algorithms.SumSweep implementation,
         * but I think this implementation is correct and the original is wrong.
         * TODO: check which implementation is better.
         */
        return scc.getCorrespondingEdges(
            Comparator.comparingInt(
                edge ->
                    graph.outDegreeOf(graph.getEdgeSource(edge)) + graph.inDegreeOf(graph.getEdgeSource(edge))
            )
        );
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
    private Pair<Map<Integer, Integer>, Map<Integer, Integer>> computeDistPivot(final ArrayMap<Integer> pivots, final boolean forward) {
        Graph<Integer, IntIntPair> g;
//        Graph<Integer, IntIntPair> otherSccGraph;
        if (forward) {
            g = getGraph();
//            otherSccGraph = this.reverseSccGraph;
        } else {
            g = reverseGraph;
//            otherSccGraph = this.sccGraph;
        }

        Map<Integer, Integer> distFromPivot = new HashMap<>(g.vertexSet().size());
        Map<Integer, Integer> pivotEcc = new ArrayMap<Integer>(pivots.size());
        Map<Integer, Integer> vertexToComponent = scc.vertexToComponentNumber();
        List<Set<Integer>> stronglyConnectedSets = scc.stronglyConnectedSets();




//        TopologicalOrderIterator<Integer, IntIntPair> it = new TopologicalOrderIterator<>(otherSccGraph);
//        List<Integer> sortedPivots = new ArrayList<>(pivots.values().size());
//        while (it.hasNext()) { // TODO: make sure that the order whe are doing is correct
//            sortedPivots.add(pivots.get(it.next()));
//        }

        Object[] pivotsArr = pivots.getArray();
        Iterable<Integer> sortedPivots;
        if (forward) {
            sortedPivots = () -> new Iterator<Integer>() {
                private int index = pivotsArr.length;
                @Override
                public boolean hasNext() {
                    return index > 0;
                }

                @Override
                public Integer next() {
                    index--;
                    return (Integer)pivotsArr[index];
                }
            };
        }
        else {
            sortedPivots = () -> new Iterator<Integer>() {
                private int index = -1;
                @Override
                public boolean hasNext() {
                    return index < pivotsArr.length - 1;
                }

                @Override
                public Integer next() {
                    index++;
                    return (Integer)pivotsArr[index];
                }
            };
        }



        BreadthFirstIterator<Integer, IntIntPair> iterator = new BreadthFirstIterator<Integer, IntIntPair>(g, sortedPivots) {
            private int currentPivotComponent;

            @Override
            protected void encounterVertex(Integer vertex, IntIntPair edge)
            {
                if (edge == null) {
                    currentPivotComponent = vertexToComponent.get(vertex);
                }
                else {
                    int currentVertexComponent = vertexToComponent.get(Graphs.getOppositeVertex(graph, edge, vertex));
                    if (currentVertexComponent != currentPivotComponent) {
                        System.err.println("f");
                        exit(1);
                    }
                }
                super.encounterVertex(vertex, edge);
            }
        };

        while (iterator.hasNext()) iterator.next();

        for (final var stronglyConnectedSetIndex : pivots.keySet()) {
            int maxDepth = -1, currentDepth;
            for (Integer v : stronglyConnectedSets.get(stronglyConnectedSetIndex)) {
                currentDepth = iterator.getDepth(v);
                distFromPivot.put(v, currentDepth);
                if (currentDepth > maxDepth) maxDepth = currentDepth;
            }
            pivotEcc.put(stronglyConnectedSetIndex, maxDepth);
        }




//        for (final var entry : pivots.entrySet()) {
////
////            bfs = new BFS<>(g, entry.getValue());
////            pivotEcc.put(stronglyConnectedComponent, bfs.getEcc());
////            for (Integer v : stronglyConnectedComponent.vertexSet()) {
////                distFromPivot.put(v, bfs.getDepth(v));
////            }
//
//            BreadthFirstIterator<Integer, IntIntPair> iterator = new BreadthFirstIterator<>(g, entry.getValue()) {
//                final int stronglyConnectedComponent = entry.getKey();
//                @Override
//                protected void encounterVertex(Integer vertex, IntIntPair edge)
//                {
//                    if (edge != null) {
//                        int currentStronglyConnectedComponent = vertexToComponent.get(Graphs.getOppositeVertex(graph, edge, vertex));
//                        if (stronglyConnectedComponent != currentStronglyConnectedComponent) {
//                            return;
//                        }
//                    }
//                    super.encounterVertex(vertex, edge);
//                }
//            };
//
//            while (iterator.hasNext()) iterator.next();
//
//
//            final int stronglyConnectedSetIndex = entry.getKey();
//            int maxDepth = -1, currentDepth;
//            for (Integer v : stronglyConnectedSets.get(stronglyConnectedSetIndex)) {
//                currentDepth = iterator.getDepth(v);
//                distFromPivot.put(v, currentDepth);
//                if (currentDepth > maxDepth) maxDepth = currentDepth;
//            }
//            pivotEcc.put(stronglyConnectedSetIndex, maxDepth);
//        }
        return new Pair<>(distFromPivot, pivotEcc);
    }

    /**
     * The ComputePivotBoundsF Procedure from algorithm3 of the article of sumsweep.
     */
    void computePivotBoundsF(final Map<Integer, Integer> pivots, final Map<Integer, Integer> eccPivotF, final Map<Integer, Integer> distPivotF, final Map<Integer, Integer> distPivotB) {
    //Map<Graph<Integer, IntIntPair>, Integer> computePivotBoundsF(final Map<Graph<Integer, IntIntPair>, Integer> pivots, final Map<Graph<Integer, IntIntPair>, Integer> eccPivotF, final Map<Integer, Integer> distPivotF, final Map<Integer, Integer> distPivotB) {
        // Map<Graph<Integer, IntIntPair>, Integer> uPivotF = new HashMap<>(scc.getStronglyConnectedComponents().size());

        Integer currentPivot, start, end;
        int currentScc, endScc;
        int currentVal, uFVal;

//        TopologicalOrderIterator<Integer, IntIntPair> it = new TopologicalOrderIterator<>(reverseSccGraph);
//
//        while (it.hasNext()) { // TODO: make sure that the order whe are doing is correct
//            currentScc = it.next();
        for (currentScc = pivots.size() - 1; currentScc >= 0; currentScc--){
            currentPivot = pivots.get(currentScc);

            currentVal = eccPivotF.get(currentScc);
            uFVal = uF.get(currentPivot);
            if (currentVal >= uFVal) {
                eccPivotF.put(currentScc, uFVal);
                continue;
            }

            for (var edge : sccGraph.edgesOf(currentScc)) { //TODO: WHY BOTH EDGE DIRECTIONS?
                if (edge == null) {
                    System.out.println("s");
                }
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
    void computePivotBoundsB(final Map<Integer, Integer> pivots, final Map<Integer, Integer> eccPivotB, final Map<Integer, Integer> distPivotF, final Map<Integer, Integer> distPivotB) {
        // Map<Graph<Integer, IntIntPair>, Integer> uPivotB = new HashMap<>(scc.getStronglyConnectedComponents().size());

        Integer currentPivot, start, end;
        int currentScc, endScc;
        int currentVal, uBVal;

//        TopologicalOrderIterator<Integer, IntIntPair> it = new TopologicalOrderIterator<>(sccGraph);
//
//        while (it.hasNext()) { // TODO: make sure that the order whe are doing is correct
//            currentScc = it.next();
        final int numPivots = pivots.size();
        for (currentScc = 0; currentScc < numPivots; currentScc++){
            currentPivot = pivots.get(currentScc);

            currentVal = eccPivotB.get(currentScc);
            uBVal = uB.get(currentPivot);
            if (currentVal >= uBVal) {
                eccPivotB.put(currentScc, uBVal);
                continue;
            }

            for (var edge : sccGraph.edgesOf(currentScc)) { //TODO: WHY BOTH EDGE DIRECTIONS?
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
     * {@link #allCCUpperBound(ArrayMap)} function (see the paper for more details).
     *
     * @param pivots
     *            a map between the strongly connected components, and their pivots
     */
    private void allCCUpperBound(final ArrayMap<Integer> pivots) {
        final var distEccForward = computeDistPivot(pivots, true);
        final Map<Integer, Integer> distPivotF = distEccForward.getValue0();
        Map<Integer, Integer> eccPivotF = distEccForward.getValue1();
        final var distEccBackward = computeDistPivot(pivots, false);
        final Map<Integer, Integer> distPivotB = distEccBackward.getValue0();
        Map<Integer, Integer> eccPivotB = distEccBackward.getValue1();

        computePivotBoundsF(pivots, eccPivotF, distPivotF, distPivotB);
        computePivotBoundsB(pivots, eccPivotB, distPivotF, distPivotB);

        int ufValue, pivotEccF, newUfValue, uBValue, pivotEccB, newUbValue;


        List<Set<Integer>> stronglyConnectedSets = scc.stronglyConnectedSets();
        for (int i = 0; i < stronglyConnectedSets.size(); i++) {
            Set<Integer> currentScc = stronglyConnectedSets.get(i);
            pivotEccF = eccPivotF.get(i);
            pivotEccB = eccPivotB.get(i);

            for (Integer v : currentScc) {
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

        for (Integer v : getGraph().vertexSet()) {
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

//        return switch (output) {
//            case RADIUS -> missingR;
//            case DIAMETER -> Math.min(missingDF, missingDB);
//            case RADIUS_DIAMETER -> missingR + Math.min(missingDF, missingDB);
//            case ALL_FORWARD -> missingAllF;
//            default -> missingAllF + missingAllB;
//        };

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
     * {@link #getForwardEccentricity(Integer)}, and
     * {@link #getBackwardEccentricity(Integer)}.
     */
    public void compute() {
        if (pl != null) {
            pl.start("Starting visits...");
            pl.itemsName = "nodes";
            pl.displayLocalSpeed = true;
        }
        Integer maxDegreeVertex = (new Highest_Degree(Highest_Degree.Option.OUT_DEGREE)).getInitialNode(getGraph());

        sumSweepHeuristic(maxDegreeVertex, 6);

        final double[] points = new double[6];
        int missingNodes = findMissingNodes(), oldMissingNodes;

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
                    this.stepSumSweep(argMax(totDistF, uF, toCompleteF), true);
                    break;
            }

            oldMissingNodes = missingNodes;
            missingNodes = this.findMissingNodes();
            points[stepToPerform] = oldMissingNodes - missingNodes;

            if (iter > 20) {
                for (int j = 0; j < points.length; j++) {
                    if (j != stepToPerform && points[j] >= 0) {
                        points[j] = points[j] + 2.0 / iter;
                    }
                    //points[j] += Math.random() / 100;
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











