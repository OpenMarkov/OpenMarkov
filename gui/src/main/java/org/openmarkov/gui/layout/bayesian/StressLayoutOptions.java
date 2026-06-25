/*
 * Copyright (c) CISIAD, UNED, Spain. Licensed under the GPLv3 licence.
 */
package org.openmarkov.gui.layout.bayesian;

/**
 * Tunable parameters for {@link StressLayout}. Defaults are picked for
 * Bayesian networks of up to a few hundred nodes; callers normally use
 * {@code new StressLayoutOptions()} unmodified.
 */
public final class StressLayoutOptions {

    /** Maximum number of majorization sweeps. */
    public int maxIterations = 200;

    /** Stop early when relative stress drop falls below this threshold. */
    public double convergenceEpsilon = 1e-4;

    /** Ideal length of one graph hop, in pixels. Pairwise targets are
     *  {@code idealEdgeLength * graphDistance(i, j)}. */
    public double idealEdgeLength = 100.0;

    /** Cap on the hop count used when computing pairwise targets. Without
     *  the cap, long chains stretch the diagram linearly. */
    public int maxHopsForIdealDistance = 4;

    /** Multiplier applied to the diameter for pairs in different connected
     *  components, so unrelated subgraphs spread out instead of collapsing
     *  on top of each other. */
    public double disconnectedDistanceMultiplier = 1.0;

    /** Strength of the directional bias term that pulls every child below
     *  its parent in y. 0 disables the bias (pure stress-majorization);
     *  values around 0.5 keep "cause above effect" readable in &gt;90% of
     *  edges without locking the layout into discrete layers. */
    public double lambdaDirectional = 0.5;

    /** Target vertical drop, in pixels, from a parent to its child for
     *  the directional term. */
    public double directionalDelta = 100.0;

    /** Default node footprint for the overlap-removal post-pass. */
    public double defaultBoxWidth  = 120.0;
    public double defaultBoxHeight = 60.0;

    /** Minimum gap between any two node bounding-box edges after the
     *  overlap-removal post-pass. */
    public double minBoxSeparation = 24.0;

    /** Cap on iterations of the overlap-removal post-pass. */
    public int maxOverlapPasses = 60;

    /** Top-left origin of the laid-out region. */
    public double originX = 80.0;
    public double originY = 80.0;
}
