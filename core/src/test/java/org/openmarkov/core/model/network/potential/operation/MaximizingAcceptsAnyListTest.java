package org.openmarkov.core.model.network.potential.operation;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The maximization takes the potentials as a {@code List}, so it must work with any of them
 * and not only with an {@code ArrayList}. It used to cast the argument to {@code ArrayList},
 * so a read-only list broke it at run time.
 *
 * @author Manuel Arias
 */
class MaximizingAcceptsAnyListTest {

    private final Variable a = new Variable("A", "yes", "no");
    private final Variable b = new Variable("B", "yes", "no");

    @Tag(TestSpeed.FAST)
    @Test void anImmutableListIsAccepted() {
        List<Potential> potentials = List.of(
                new TablePotential(List.of(a, b), PotentialRole.CONDITIONAL_PROBABILITY));
        Object[] result = DiscretePotentialOperations.multiplyAndMaximize(
                potentials, List.of(b), a);
        assertEquals(List.of(b), ((TablePotential) result[0]).getVariables());
    }

    @Tag(TestSpeed.FAST)
    @Test void aFixedSizeListIsAccepted() {
        List<Potential> potentials = Arrays.asList(
                (Potential) new TablePotential(List.of(a, b), PotentialRole.CONDITIONAL_PROBABILITY));
        Object[] result = DiscretePotentialOperations.multiplyAndMaximize(
                potentials, List.of(b), a);
        assertEquals(List.of(b), ((TablePotential) result[0]).getVariables());
    }
}
