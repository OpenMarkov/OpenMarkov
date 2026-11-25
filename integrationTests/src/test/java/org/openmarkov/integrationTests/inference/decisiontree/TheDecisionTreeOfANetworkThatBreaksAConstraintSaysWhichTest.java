/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.inference.decisiontree;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.model.decisiontree.EvaluationType;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.constraint.NoCycle;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.DecisionTreeExpansion;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * ID-cycle.pgmx, from the tests of the C++/Qt version, is an influence diagram with the links A → B and B → A: two
 * violations of NoCycle. Evaluating its decision tree said nothing of it and ended in a NullPointerException, shown in
 * the window of unexpected errors (error 451 of the pending list); it now says that the network cannot be evaluated,
 * naming the constraint once. The network is kept among the resources of these tests, not among the networks that
 * other tests open and evaluate one after another.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheDecisionTreeOfANetworkThatBreaksAConstraintSaysWhichTest {

    @Test void theDecisionTreeOfANetworkWithACycleSaysThatItBreaksNoCycle() throws Exception {
        ProbNet probNet = new PGMXReader().read(getClass().getResource("/inference/ID-cycle.pgmx")).probNet();

        NotEvaluableNetworkException.UnsatisfiedConstraints refusal = assertThrows(
                NotEvaluableNetworkException.UnsatisfiedConstraints.class,
                () -> new DecisionTreeExpansion(EvaluationType.UNICRITERION).evaluate(probNet, new EvidenceCase()));

        assertEquals(List.of(NoCycle.class),
                     refusal.unsatisfiedConstraints.stream().map(Object::getClass).toList());
        assertEquals("Some constraints are not satisfied: [No cycles are allowed.]", refusal.toString());
    }
}
