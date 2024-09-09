/* This software includes modifications based on code originally written by Sebastiano Vigna,
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


package algorithmsForWebGraphs;

import it.unimi.dsi.logging.ProgressLogger;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.LazyIntIterator;
import it.unimi.dsi.webgraph.Transform;
import it.unimi.dsi.webgraph.algo.StronglyConnectedComponents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;


public class SumSweepForWebGraphs {
    private static final boolean DEBUG = true;


    /**
     * Returns the index <var>i</var> such that <var>vec</var>[<var>i</var>] is
     * maximum, among all indices such that <var>acc</var>[<var>i</var>] is
     * true. In case of tie, the index maximizing <var>tieBreak</var> is chosen.
     *
     * @param vec
     *            the vector of which we want to compute the argMax
     * @param tieBreak
     *            the tiebreak vector
     * @param acc
     *            the vector used to decide if an index is acceptable: a
     *            negative value means that the vertex is acceptable
     * @return the value <var>i</var> such that <var>vec</var>[<var>i</var>] is
     *         maximum
     */
    public static int argMax(final int[] vec, final int[] tieBreak, final boolean acc[]) {

        int max = Integer.MIN_VALUE, maxTieBreak = Integer.MIN_VALUE, argMax = -1;
        for (int i = 0; i < vec.length; i++) {
            if (acc[i] && (vec[i] > max || (vec[i] == max && tieBreak[i] > maxTieBreak))) {
                argMax = i;
                max = vec[i];
                maxTieBreak = tieBreak[i];
            }
        }
        return argMax;
    }

    private final static Logger LOGGER = LoggerFactory.getLogger(it.unimi.dsi.webgraph.algo.SumSweepDirectedDiameterRadius.class);

    /** The graph under examination. */
    private final ImmutableGraph graph;
    /** The reversed graph. */
    private final ImmutableGraph revgraph;
    /** The number of nodes. */
    private final int nn;
    /** The global progress logger. */
    private final ProgressLogger pl;

    /** The array of forward eccentricity value. */
    private final int[] eccF;
    /** The array of backward eccentricity value. */
    private final int[] eccB;
    /**
     * <var>toCompleteF</var>[<var>v</var>] is <var>True</var> if and only if
     * the forward eccentricity of <var>v</var> is not guaranteed, yet.
     */
    private final boolean[] toCompleteF;
    /**
     * <var>toCompleteB</var>[<var>v</var>] is <var>True</var> if and only if
     * the backward eccentricity of <var>v</var> is not guaranteed, yet.
     */
    private final boolean[] toCompleteB;

    /** The queue used for each BFS (it is recycled to save some time). */
    private final int[] queue;
    /**
     * The array of distances, used in each BFS (it is recycled to save some
     * time).
     */
    private final int[] dist;
    private int dL;
    protected int lF[];
    protected int uF[];
    protected int lB[];
    protected int uB[];
    private final int totDistF[];
    private final int totDistB[];

    public SumSweepForWebGraphs(final ImmutableGraph graph, final ProgressLogger pl) {
        this.pl = pl;
        this.graph = graph;
        this.revgraph = Transform.transpose(graph);
        this.nn = graph.numNodes();
        this.eccF = new int[nn];
        this.eccB = new int[nn];
        totDistF = new int[nn];
        totDistB = new int[nn];
        lF = new int[nn];
        lB = new int[nn];
        uF = new int[nn];
        uB = new int[nn];
        toCompleteF = new boolean[nn];
        toCompleteB = new boolean[nn];
        queue = new int[nn];
        dist = new int[nn];

        Arrays.fill(eccF, -1);
        Arrays.fill(eccB, -1);
        Arrays.fill(uF, nn + 1);
        Arrays.fill(uB, nn + 1);
        Arrays.fill(toCompleteF, true);
        Arrays.fill(toCompleteB, true);
        this.dL = 0;
    }

    private void stepSumSweep(final int start, final boolean forward) {
        if (start == -1) {
            return;
        }
        final int queue[] = this.queue;
        final int dist[] = this.dist;
        int startQ = 0, endQ = 0;
        int v, w, eccStart;
        int[] l, lOther, u, uOther, totDistOther, ecc, eccOther;
        boolean[] toComplete, toCompleteOther;

        Arrays.fill(dist, -1);

        ImmutableGraph g;

        if (forward) {
            l = lF;
            lOther = lB;
            u = uF;
            uOther = uB;
            totDistOther = totDistB;
            g = graph;
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
            g = revgraph;
            ecc = eccB;
            eccOther = eccF;
            toComplete = toCompleteB;
            toCompleteOther = toCompleteF;
        }

        LazyIntIterator iter;

        queue[endQ++] = start;
        dist[start] = 0;

        while (startQ < endQ) {
            v = queue[startQ++];
            iter = g.successors(v);

            while ((w = iter.nextInt()) != -1) {
                if (dist[w] == -1) {
                    dist[w] = dist[v] + 1;
                    queue[endQ++] = w;
                }
            }
        }

        eccStart = dist[queue[endQ - 1]];

        l[start] = eccStart;
        u[start] = eccStart;
        ecc[start] = eccStart;
        toComplete[start] = false;

        if (dL < eccStart) {
            dL = eccStart;
        }

        for (v = nn - 1; v >= 0; v--) {

            if (dist[v] == -1)
                continue;

            totDistOther[v] += dist[v];

            if (toCompleteOther[v]) {
                if (lOther[v] < dist[v]) {
                    lOther[v] = dist[v];
                    if (lOther[v] == uOther[v]) {
                        toCompleteOther[v] = false;
                        eccOther[v] = lOther[v];
                    }
                }
            }
        }
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
    public int sumSweepHeuristic(final int start, final int iter) {
        if (DEBUG)
            LOGGER.debug("Performing initial SumSweep visit from " + start + ".");
        this.stepSumSweep(start, true);

        for (int i = 2; i < iter; i++) {
            if (i % 2 == 0) {
                final int v = argMax(totDistB, lB, toCompleteB);
                if (DEBUG)
                    LOGGER.debug("Performing initial SumSweep visit from " + v + ".");
                this.stepSumSweep(v, false);
            } else {
                final int v = argMax(totDistF, lF, toCompleteF);
                if (DEBUG)
                    LOGGER.debug("Performing initial SumSweep visit from " + v + ".");
                this.stepSumSweep(v, true);
            }
        }


        return dL;
    }


}
