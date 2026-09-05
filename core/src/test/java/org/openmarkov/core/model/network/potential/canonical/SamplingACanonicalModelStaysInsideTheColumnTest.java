/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential.canonical;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Drawing a value from a canonical model walks a column of the parameters adding them up until it
 * passes the number drawn. Nothing checks that a column adds up to one -the file reader hands over
 * whatever the file says- so the walk has to stop at the end of its column, as the walk of a plain
 * table already does. Without the stop it walked into the next column and answered a state the
 * child does not have, or ran off the end of the leak.
 *
 * @author Manuel Arias
 */
public class SamplingACanonicalModelStaysInsideTheColumnTest {

    private static MaxPotential modelWith(double[] parameters, double[] leak) {
        Variable child = new Variable("C", 3);
        Variable parent = new Variable("P", 3);
        MaxPotential potential = new MaxPotential(List.of(child, parent));
        potential.setNoisyParameters(parent, parameters);
        potential.setLeakyParameters(leak);
        return potential;
    }

    private static Map<Variable, Integer> firstStateOfTheParent(MaxPotential potential) {
        Map<Variable, Integer> parents = new HashMap<>();
        parents.put(potential.getVariable(1), 0);
        return parents;
    }

    @Tag(TestSpeed.FAST)
    @Test public void aColumnThatDoesNotAddUpToOneStillAnswersAStateOfTheChild() {
        MaxPotential potential = modelWith(new double[]{0.1, 0.0, 0.0,
                                                        0.0, 0.0, 1.0,
                                                        1.0, 0.0, 0.0},
                                           new double[]{1.0, 0.0, 0.0});
        Map<Variable, Integer> parents = firstStateOfTheParent(potential);
        Random random = new Random(7);

        for (int draw = 0; draw < 500; ++draw) {
            int state = potential.sampleConditionedVariable(random, parents);
            assertTrue(state >= 0 && state < 3,
                    "the child has three states and the draw answered " + state);
        }
    }

    @Tag(TestSpeed.FAST)
    @Test public void aLeakThatDoesNotAddUpToOneDoesNotRunOffTheEnd() {
        MaxPotential potential = modelWith(new double[]{1.0, 0.0, 0.0,
                                                        1.0, 0.0, 0.0,
                                                        1.0, 0.0, 0.0},
                                           new double[]{0.9, 0.0, 0.0});
        Map<Variable, Integer> parents = firstStateOfTheParent(potential);
        Random random = new Random(7);

        assertDoesNotThrow(() -> {
            for (int draw = 0; draw < 500; ++draw) {
                potential.sampleConditionedVariable(random, parents);
            }
        });
    }

    @Tag(TestSpeed.FAST)
    @Test public void awholeColumnStillGivesEachStateItsShare() {
        MaxPotential potential = modelWith(new double[]{0.5, 0.5, 0.0,
                                                        1.0, 0.0, 0.0,
                                                        1.0, 0.0, 0.0},
                                           new double[]{1.0, 0.0, 0.0});
        Map<Variable, Integer> parents = firstStateOfTheParent(potential);
        Random random = new Random(7);

        int[] counts = new int[3];
        for (int draw = 0; draw < 2000; ++draw) {
            counts[potential.sampleConditionedVariable(random, parents)]++;
        }

        assertTrue(counts[0] > 800 && counts[0] < 1200, "half of the draws for the first state: " + counts[0]);
        assertTrue(counts[1] > 800 && counts[1] < 1200, "half of the draws for the second: " + counts[1]);
    }
}
