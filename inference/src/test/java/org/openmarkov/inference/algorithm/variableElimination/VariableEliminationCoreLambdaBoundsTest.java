/*
 * Copyright (c) CISIAD, UNED, Spain,  2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.inference.algorithm.variableElimination;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.inference.heuristic.simpleElimination.SimpleElimination;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The willingness-to-pay bounds handed to a cost-effectiveness elimination must survive the
 * constructor that receives them.
 * <p>
 * They did not. The five-argument constructor - the one whose javadoc says it is only for bi-criteria
 * analysis - stored them and then called {@code initialize}, which put the defaults back over them
 * whenever the analysis is bi-criteria, that is, always for that constructor. Anyone asking for an
 * analysis between two thresholds silently got one between zero and infinity.
 * <p>
 * The defect is real but has no caller today: every use in the repository goes through the
 * three-argument constructor. This test is here so that the first caller of the other one gets the
 * bounds it asked for.
 * <p>
 * The network is empty and the heuristic therefore has nothing to hand out, so the constructor runs
 * the elimination loop zero times and only the initialization is under test.
 *
 * @author Manuel Arias
 */
public class VariableEliminationCoreLambdaBoundsTest {

    @Test public void theBoundsGivenToTheConstructorAreTheOnesItKeeps() {
        ProbNet emptyNetwork = new ProbNet();

        VariableEliminationCore elimination = new VariableEliminationCore(
                emptyNetwork, new SimpleElimination(emptyNetwork, List.of()), false, 1000.0, 2000.0);

        assertEquals(1000.0, elimination.lambdaMin, "the lower bound was replaced by the default");
        assertEquals(2000.0, elimination.lambdaMax, "the upper bound was replaced by the default");
    }

    /** And the three-argument constructor, which promises nothing, still gets the defaults. */
    @Test public void theConstructorWithoutBoundsFallsBackToTheWholeRange() {
        ProbNet emptyNetwork = new ProbNet();

        VariableEliminationCore elimination = new VariableEliminationCore(
                emptyNetwork, new SimpleElimination(emptyNetwork, List.of()), false);

        assertEquals(0.0, elimination.lambdaMin);
        assertEquals(Double.POSITIVE_INFINITY, elimination.lambdaMax);
    }
}
