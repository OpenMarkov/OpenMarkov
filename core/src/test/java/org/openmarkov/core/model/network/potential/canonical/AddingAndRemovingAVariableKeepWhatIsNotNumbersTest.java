/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential.canonical;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * Adding or removing a parent of a canonical model answers a potential of its own: it keeps the
 * comment, the criterion and the properties, and it does not share the parameter arrays with the
 * one it came from. The comment is written to the file, so losing it loses it for good.
 *
 * @author Manuel Arias
 */
public class AddingAndRemovingAVariableKeepWhatIsNotNumbersTest {

    private static final String COMMENT = "a comment worth keeping";

    private ICIPotential prepared(ICIPotential potential) {
        potential.setComment(COMMENT);
        potential.setCriterion(new Criterion("cost"));
        potential.properties.put("key", "value");
        return potential;
    }

    private void assertKeepsWhatIsNotNumbers(Potential result, String what) {
        assertEquals(COMMENT, result.getComment(), what + " lost the comment");
        assertNotNull(result.getCriterion(), what + " lost the criterion");
        assertEquals("cost", result.getCriterion().getCriterionName(), what + " changed the criterion");
        assertEquals("value", result.properties.get("key"), what + " lost the properties");
    }

    private void checkFamily(String family, int states, Function<List<Variable>, ICIPotential> build) {
        Variable child = new Variable("C", states);
        Variable parent = new Variable("P", states);
        Variable added = new Variable("Q", states);
        Variable removed = new Variable("R", states);

        ICIPotential twoVariables = prepared(build.apply(List.of(child, parent)));
        Potential grown = twoVariables.addVariable(added);
        assertKeepsWhatIsNotNumbers(grown, family + ": adding a variable");
        assertNotSame(twoVariables.getNoisyParameters(parent),
                ((ICIPotential) grown).getNoisyParameters(parent),
                family + ": adding a variable shares the parameters of " + parent.getName());
        assertNotSame(twoVariables.getLeakyParameters(), ((ICIPotential) grown).getLeakyParameters(),
                family + ": adding a variable shares the leak parameters");

        ICIPotential threeVariables = prepared(build.apply(List.of(child, parent, removed)));
        Potential shrunk = threeVariables.removeVariable(removed);
        assertKeepsWhatIsNotNumbers(shrunk, family + ": removing a variable");
        assertNotSame(threeVariables.getNoisyParameters(parent),
                ((ICIPotential) shrunk).getNoisyParameters(parent),
                family + ": removing a variable shares the parameters of " + parent.getName());

        Potential leftWithoutParents = prepared(build.apply(List.of(child, parent))).removeVariable(parent);
        assertKeepsWhatIsNotNumbers(leftWithoutParents, family + ": removing the last parent");
    }

    @Tag(TestSpeed.FAST)
    @Test public void theMaximumFamily() {
        checkFamily("MAX", 2, MaxPotential::new);
    }

    @Tag(TestSpeed.FAST)
    @Test public void theMinimumFamily() {
        checkFamily("MIN", 2, MinPotential::new);
    }

    @Tag(TestSpeed.FAST)
    @Test public void theTuningFamily() {
        checkFamily("TUNING", 3, TuningPotential::new);
    }

    @Tag(TestSpeed.FAST)
    @Test public void writingOnTheNewParametersDoesNotReachTheOldOnes() {
        Variable child = new Variable("C", 2);
        Variable parent = new Variable("P", 2);
        MaxPotential original = new MaxPotential(List.of(child, parent));
        original.setNoisyParameters(parent, new double[] { 0.9, 0.1, 0.4, 0.6 });

        ICIPotential grown = (ICIPotential) original.addVariable(new Variable("Q", 2));
        grown.getNoisyParameters(parent)[0] = 99.0;

        assertArrayEquals(new double[] { 0.9, 0.1, 0.4, 0.6 }, original.getNoisyParameters(parent), 1e-12,
                "writing on the new potential reached the one it came from");
    }
}
